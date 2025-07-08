package com.example.koplast

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth


class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val btnCancelRegistration = view.findViewById<Button>(R.id.btnCancelRegistration)
        val btnRegisterRegistration = view.findViewById<Button>(R.id.btnRegisterRegistration)

        val etEmailRegistration = view.findViewById<EditText>(R.id.etEmailRegistration)
        val etPasswordRegistration1 = view.findViewById<EditText>(R.id.etPasswordRegistration1)
        val etPasswordRegistration2 = view.findViewById<EditText>(R.id.etPasswordRegistration2)

        btnCancelRegistration.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnRegisterRegistration.setOnClickListener {
            val email = etEmailRegistration.text.toString()
            val password1 = etPasswordRegistration1.text.toString()
            val password2 = etPasswordRegistration2.text.toString()

            if (email.isNotEmpty() && password1 == password2) {
                auth.createUserWithEmailAndPassword(email, password1)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        } else {
                            Toast.makeText(requireContext(), "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Passwords do not match or email is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}