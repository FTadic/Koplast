package com.example.koplast

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // Korisnik je ulogiran - pokreni HomeActivity i završi MainActivity
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                // Nema korisnika - učitaj login/register flow fragment(e) u MainActivity
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.loginFragment)  // ili tvoj početni fragment u nav_graph_main
            }
        }
    }
}