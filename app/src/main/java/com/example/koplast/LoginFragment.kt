package com.example.koplast

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var auth : FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val btnRegisterLogin = view.findViewById<Button>(R.id.btnRegisterLogin)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)

        val etEmailLogin = view.findViewById<EditText>(R.id.etEmailLogin)
        val etPasswordLogin = view.findViewById<EditText>(R.id.etPasswordLogin)

        btnRegisterLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        btnLogin.setOnClickListener {
            val email = etEmailLogin.text.toString()
            val password = etPasswordLogin.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            Toast.makeText(requireContext(), "Login failed!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Email and Password required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}