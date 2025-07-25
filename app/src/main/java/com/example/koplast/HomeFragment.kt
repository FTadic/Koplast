package com.example.koplast

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth


class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var auth : FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnOrder = view.findViewById<Button>(R.id.btnOrder)
        val btnDatabase = view.findViewById<Button>(R.id.btnDatabase)

        val tvUser = view.findViewById<TextView>(R.id.tvUser)

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            tvUser.text = user.email
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            /*parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, LoginFragment())
                .commit()*/

            // Vrati se na Login ekran (MainActivity)
            val intent = Intent(requireContext(), MainActivity::class.java)
            // Očisti backstack da korisnik ne može "nazad" u HomeActivity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        btnOrder.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav.selectedItemId = R.id.orderFragment
        }

        btnDatabase.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav.selectedItemId = R.id.databaseFragment
        }
    }
}