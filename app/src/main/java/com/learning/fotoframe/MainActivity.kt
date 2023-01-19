package com.learning.fotoframe

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ActionMode
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.learning.fotoframe.viewmodels.AppMainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var auth: FirebaseAuth
    private var appMainViewModel: AppMainViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        auth = Firebase.auth
        appMainViewModel = ViewModelProvider(this)[AppMainViewModel::class.java]

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (isCanWriteSettings(this)) {
            Log.d(TAG, "onCreate: success")
            setupLight(this, 255)//0~255
            //startTask()

        } else {
            Log.d(TAG, "onCreate: fail")
            requestCanWriteSettings(this)
        }




    }

    private fun startTask() {
        coroutineScope.launch(Dispatchers.Main) {
            while (true){
                performSlowTask()
            }
        }

    }


    private suspend fun performSlowTask() {
        Log.i(TAG, "performSlowTask before")
        delay(3_000) // simulates long running task
        setupLight(this, 255)//0~255
        Log.i(TAG, "performSlowTask after")
    }


    private fun isCanWriteSettings(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(context)
    }

    private fun requestCanWriteSettings(activity: Activity) {
        if (isCanWriteSettings(context = activity))
            return //not need
        try {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + activity.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            resultLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("requestCanWriteSettings", "requestCanWriteSettings $e")
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

            }
        }

    private fun setupLight(context: Context, light: Int) {
        try {

            val brightnessMode = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE
            )
            if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
            }
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                light
            )
        } catch (e: Exception) {
            Log.e("setupLight", "Exception $e")
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser!=null){
            Toast.makeText(this, "email ${currentUser.email}", Toast.LENGTH_SHORT).show()
        }


    }




}