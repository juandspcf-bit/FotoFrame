package com.learning.fotoframe

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FirebaseUtilsApp {

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private val myLinkList: MutableList<ListPhotosFragmentV2.MyLink> = ArrayList()

    fun addToFireStorage(uri: Uri?,
                         set: String,
                         context: Context?,
                         activity: Activity?,
                         user: String?,
                         resultStatus: (Boolean) -> Unit) {
        uri?.let {
            Log.d("PATH", "storeImageInDB: $user-memories")
            storeImageInDB(uri, set, db, storageReference,context, activity, user,resultStatus)
        }
    }

    private fun storeImageInDB(uriMain: Uri,
                               set: String,
                               db: FirebaseFirestore,
                               storageReference: StorageReference,
                               context: Context?,
                               activity: Activity?,
                               user: String?,
                               resultStatus: (Boolean) -> Unit) {
        val name = "my_image_" + Timestamp.now().seconds
        Log.d("PATH", "storeImageInDB: $user-memories")
        val filePath = storageReference
            .child("$user-memories")
            .child(name)

        val collectionReference = db
            .collection("$user-links")

        filePath.putFile(uriMain).addOnSuccessListener {
            filePath.downloadUrl.addOnSuccessListener { uri ->
                val uriThumbnail = extractUriThumbnail(uriMain, name, filePath, context, activity)
                if (uriThumbnail==null){
                }else{
                    storeUriThumbnail(user, uriThumbnail, name, storageReference){
                        storeMyLinkInDB(set, uri, filePath, collectionReference, it, resultStatus)
                    }
                }


            }.addOnFailureListener { resultStatus(false) }
        }.addOnFailureListener { resultStatus(false) }
    }

    private fun storeUriThumbnail(user: String?,
                                  uriThumbnail: Uri,
                                  name: String,
                                  storageReference: StorageReference,
                                  callback:(Uri)->Unit) {
        val filePath = storageReference
            .child("$user-memoriesThumbnail")
            .child(name)
        filePath.putFile(uriThumbnail).addOnSuccessListener {
            filePath.downloadUrl.addOnSuccessListener {  uri ->
                callback(uri)
            }.addOnFailureListener{

            }
        }.addOnFailureListener{

        }

    }

    private fun storeMyLinkInDB(
        set: String,
        uri: Uri,
        filePath: StorageReference,
        collectionReference: CollectionReference,
        uriThumbnail: Uri,
        resultStatus: (Boolean) -> Unit
    ) {
        val myLink =
            ListPhotosFragmentV2.MyLink(set, uri.toString(), filePath.name, uriThumbnail.toString())
        collectionReference.document(filePath.name)
            .set(myLink).addOnSuccessListener { resultStatus(true) }
            .addOnFailureListener { resultStatus(false) }
    }

    private fun extractUriThumbnail(
        uri: Uri,
        name: String,
        filePath: StorageReference,
        context: Context?,
        activity: Activity?
    ): Uri? {
        val pFileDescriptor = activity?.contentResolver?.openFileDescriptor(uri, "r")
        val fileDescriptor = pFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        pFileDescriptor?.close()

        image?.let {
            val extractThumbnail = ThumbnailUtils.extractThumbnail(
                image,
                256,
                256
            )


            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                return savePhotoToExternalStorage("thumbnail", extractThumbnail, activity)
            }else{
                try {
                    if (isPermissionsAllowed(context).not()) {
                        Log.d("Permission", "extractThumbnail: not allowed")
                        return null
                    }
                    val file = File(
                        Environment.getExternalStorageDirectory()
                            .toString() + File.separator + "/Android/data/thumb.jpg"
                    )
                    file.createNewFile()

                    //Convert bitmap to byte array
                    val bos = ByteArrayOutputStream()
                    extractThumbnail.compress(
                        Bitmap.CompressFormat.JPEG,
                        60,
                        bos
                    ) // YOU can also save it in JPEG
                    val bitmapData = bos.toByteArray()

                    //write the bytes in file
                    val fos = FileOutputStream(file)
                    fos.write(bitmapData)
                    fos.flush()
                    fos.close()

                    context?.let { it1 ->
                        return FileProvider.getUriForFile(it1, it1.packageName + ".provider", file)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return Uri.EMPTY
                }

            }


        }
        return null
    }

    private fun sdkCheck(): Boolean{
        return Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q
    }

    private fun savePhotoToExternalStorage(name: String, bmp: Bitmap, activity: Activity?):Uri{
        val imageCollection: Uri = if (sdkCheck()){
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        }else{
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            bmp?.let {
                put(MediaStore.Images.Media.WIDTH, bmp.width)
                put(MediaStore.Images.Media.HEIGHT, bmp.height)
            }
        }

        return try {
            activity?.contentResolver?.insert(imageCollection, contentValues)?.also { uri->
                Log.d("MediaStore", "savePhotoToExternalStorage: $uri")
                activity.contentResolver?.openOutputStream(uri).use {outputStream ->
                    bmp?.let {
                        if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)){
                            throw IOException("Failed to save Bitmap")
                        }
                    }
                }
            return uri
            } ?: throw IOException("Failed to save Bitmap")

        }catch (e: IOException){
            e.printStackTrace()
            return Uri.EMPTY
        }
    }


    fun getSets(user: String?, callback: (MutableList<String>)-> Unit){
        val collectionReferenceSets = db.collection("$user-sets")

        collectionReferenceSets.addSnapshotListener { querySnapshot, _ ->

            querySnapshot?.let { querySnapshotNotNull ->
                if (querySnapshotNotNull.isEmpty) return@addSnapshotListener
                val mapList = querySnapshotNotNull
                    .documents
                    .map { it.data?.get("name") as String }

                callback(mapList.toMutableList())


            }
        }
    }

    fun getSettings(user: String?, callback: (SettingsFragment.MySettings) -> Unit){
        val collectionReferenceSets = db.collection("$user-settings")

        collectionReferenceSets.addSnapshotListener { querySnapshot, _ ->


            querySnapshot?.let { querySnapshotNotNull ->


                if (querySnapshotNotNull.isEmpty) return@addSnapshotListener
                val mapList = querySnapshotNotNull
                    .documents

                val cycle = mapList[0].get("cycle").toString().toInt()
                val indicator = mapList[0].get("indicator").toString().toInt()
                val transitionAnimation = mapList[0].get("transitionAnimation").toString().toInt()
                val delay = mapList[0].get("delay").toString().toInt()
                callback(SettingsFragment.MySettings(transitionAnimation,  indicator, cycle, delay))
                //return SettingsFragment.MySettings(0, 0 , 0)


            }
        }
    }

    fun addSettingsToDataBase(user: String?, setting: SettingsFragment.MySettings){
        val collectionReferenceSettings = db.collection("$user-settings")
        collectionReferenceSettings.document("$user")
            .set(setting).addOnSuccessListener {  }
            .addOnFailureListener {  }

    }



    fun addSetToDataBase(user: String?, data: MutableMap<String, Any>, callback: (String) -> Unit){
        val collectionReferenceSets = db.collection("$user-sets")
        collectionReferenceSets.add(data).addOnSuccessListener {
            callback("success")
        }.addOnFailureListener {
            callback("Error")
        }
    }


    fun getAllElements(user:String?, set:String, callback: (List<ListPhotosFragmentV2.MyLink>) -> Unit){
        val collectionReference = db.collection("$user-links")
        collectionReference.whereEqualTo("set", set)
            .get()
            .addOnCompleteListener { task ->
                myLinkList.clear()
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val data = document.data
                        val myLink = ListPhotosFragmentV2.MyLink(
                            data["set"].toString(),
                            data["link"].toString(),
                            data["storageName"].toString(),
                            data["linkThumbnail"].toString()
                        )
                        myLinkList.add(myLink)
                    }
                    callback(myLinkList)

                } else {
                    val emptyList = emptyList<ListPhotosFragmentV2.MyLink>()
                    callback(emptyList)
                }
            }
    }


    private fun isPermissionsAllowed(context: Context?): Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } == PackageManager.PERMISSION_GRANTED
    }


    private fun setupPermissionsWrite(context: Context?, activity: Activity?) {
        val permission = context?.let {
            Log.i("Permission", "not null")
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "Permission denied")
            makeRequestWrite(activity)
        }
    }

    private fun setupPermissionsRead(context: Context?, activity: Activity?) {
        val permission = context?.let {
            Log.i("Permission", "not null")
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "Permission denied")
            makeRequestWrite(activity)
        }
    }

    private val RECORD_REQUEST_CODE = 101
    private fun makeRequestWrite(activity: Activity?) {
        if (activity != null) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE
            )
        }
    }

    private fun makeRequestRead(activity: Activity?) {
        if (activity != null) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE
            )
        }
    }

}