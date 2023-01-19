package com.learning.fotoframe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.learning.fotoframe.databinding.FragmentLoginBinding
import com.learning.fotoframe.databinding.FragmentSignUpBinding

class FragmentLogin : Fragment() {
    private lateinit var binding: FragmentLoginBinding
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
            R.layout.fragment_login,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backFromLoginButton.setOnClickListener {
            controller.popBackStack()
        }

        binding.toSignUpPageButton.setOnClickListener {
            val currentUser = auth.currentUser
            if(currentUser == null){
                controller.navigate(R.id.action_fragmentLogin_to_signUpFragment)
            }else{
                Toast.makeText(context, "you are already signed with ${currentUser.email}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.toLogOut.setOnClickListener {
            auth.signOut()
            Toast.makeText(context, "logged out", Toast.LENGTH_SHORT).show()

            //controller.navigate(R.id.action_fragmentLogin_to_listPhotosFragmentV2)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailLoginTextInput.text.toString()
            val password = binding.password1LoginTextInput.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            activity?.let { it1 ->
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(it1) { task ->
                        if (task.isSuccessful) {
                            controller.navigate(R.id.action_fragmentLogin_to_listPhotosFragmentV2)
                            Toast.makeText(context, "logged with $email", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "unsuccessful logged with $email", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

}