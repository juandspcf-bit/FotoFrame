package com.learning.fotoframe

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.learning.fotoframe.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private lateinit var binding:FragmentSignUpBinding
    private lateinit var viewModel: SignUpViewModel
    private lateinit var controller: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = NavHostFragment.findNavController(this)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_sign_up,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        focusEmailChangeListener()
        focusPassword1ChangeListener()
        focusPassword2ChangeListener()

        binding.backFromSingUpButton.setOnClickListener {
            controller.navigate(R.id.action_signUpFragment_to_listPhotosFragmentV22)

        }

        binding.signUpButton.setOnClickListener {
            val email = binding.emailTextInput.text.toString()
            val passwordText1 = binding.password1TextInput.text.toString()
            val passwordText2 = binding.password2TextInput.text.toString()
            val isValidEmail = validatedEmail(email)
            val isValidPassword1 = validatedPasswordEmail(passwordText1)
            val isValidPassword2 = validatedPasswordEmail(passwordText2)

            Log.d("TAG", "signUpButton: $isValidEmail, $isValidPassword1, $isValidPassword2 ")

            val isValidFormData = isValidEmail &&
                    isValidPassword1 &&
                    isValidPassword2 &&
                    (binding.password1TextInput.text.toString() ==
                    binding.password2TextInput.text.toString())


            if (isValidFormData.not()) {
                Toast.makeText(context, "invalid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(context, "valid email", Toast.LENGTH_SHORT).show()

            val currentUser = auth.currentUser
            if(currentUser == null){
                signUpWithEmail(email, passwordText1)
            }else{
                Toast.makeText(context, "email ${currentUser.email}", Toast.LENGTH_SHORT).show()


            }





        }

    }

    private fun signUpWithEmail(email: String, passwordText1: String) {
        activity?.let {
            auth.createUserWithEmailAndPassword(email, passwordText1)
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(context, "created", Toast.LENGTH_SHORT).show()
                        val user = auth.currentUser

                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(context, "not created", Toast.LENGTH_SHORT).show()


                    }
                }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            Toast.makeText(context, "not sign up", Toast.LENGTH_SHORT).show()
        }
    }

    private fun focusEmailChangeListener(){
        binding.emailTextInput.setOnFocusChangeListener { _, hasFocus ->

            if(hasFocus.not()){
                binding.emailTextField.helperText = validEmail()
            }

        }
    }

    private fun validEmail(): String {
        val emailText = binding.emailTextInput.text.toString()
        if(Patterns.EMAIL_ADDRESS.matcher(emailText).matches().not()){
            return "invalid"
        }
        return ""
    }

    private fun validatedEmail(email: String) =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()



    private fun focusPassword1ChangeListener(){
        binding.password1TextInput.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus.not()){
                binding.password1TextField.helperText = validPasswordEmail()
            }
        }

        binding.password1TextInput.addTextChangedListener {
            val passwordText1 = binding.password1TextInput.text.toString()
            val passwordText2 = binding.password2TextInput.text.toString()
            if (passwordText1!=passwordText2){
                binding.password2TextField.helperText = "password should be the same"
            }else{
                binding.password2TextField.helperText = ""
            }
        }
    }

    private fun validPasswordEmail(): String {
        val passwordText = binding.password1TextInput.text.toString()
        if(passwordText.length<8){
            return "Minimum 8 characters password"
        }

        if(passwordText.matches(".*[A-Z].*".toRegex()).not()){
            return "Must Contain 1 Upper-case Character"
        }

        if(passwordText.matches(".*[a-z].*".toRegex()).not()){
            return "Must Contain 1 Upper-case Character"
        }

        if(passwordText.matches(".*[@#\$%^&+=].*".toRegex()).not()){
            return "Must Contain 1 Special Character [@#\$%^&+=]"
        }
        return ""
    }

    private fun validatedPasswordEmail(passwordText:String): Boolean {
        val isValidLength = passwordText.length>8
        val doesContainsCapitalLetter = passwordText.matches(".*[A-Z].*".toRegex())
        val doesContainsLowerLetter = passwordText.matches(".*[a-z].*".toRegex())
        val doesContainsSpecialChar = passwordText.matches(".*[@#\$%^&+=].*".toRegex())

        return isValidLength &&
                doesContainsCapitalLetter &&
                doesContainsLowerLetter &&
                doesContainsSpecialChar
    }

    private fun focusPassword2ChangeListener(){
        binding.password2TextInput.setOnFocusChangeListener { _, hasFocus ->

            if(hasFocus.not()){
                val passwordText1 = binding.password1TextInput.text.toString()
                val passwordText2 = binding.password2TextInput.text.toString()
                Log.d("Focus", "focusPassword2ChangeListener: focus")
                if (passwordText1!=passwordText2){
                    binding.password2TextField.helperText = "password should be the same"
                }else{
                    binding.password2TextField.helperText = ""
                }
            }

        }

        binding.password2TextInput.addTextChangedListener {
            val passwordText1 = binding.password1TextInput.text.toString()
            val passwordText2 = binding.password2TextInput.text.toString()
            if (passwordText1!=passwordText2){
                binding.password2TextField.helperText = "password should be the same"
            }else{
                binding.password2TextField.helperText = ""
            }
        }
    }

}