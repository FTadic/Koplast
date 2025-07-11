package com.example.koplast

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore


class TableFragment : Fragment(R.layout.fragment_table) {

    private lateinit var viewModel: SharedViewModel
    private lateinit var adapter: MyAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.materialToolbar)
        val navController = findNavController()
        val tvUkupnaCijena = view.findViewById<TextView>(R.id.tvUkupnaCijena)
        val rootLayout = view.findViewById<View>(R.id.rootTableLayout)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom + 200)
            insets
        }

        NavigationUI.setupWithNavController(toolbar, navController)

        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = MyAdapter { itemToDelete ->
            val db = FirebaseFirestore.getInstance()
            val artiklId = viewModel.artikliDocIds[itemToDelete.naziv]

            if (artiklId != null) {
                val artiklRef = db.collection("artikli").document(artiklId)
                artiklRef.get().addOnSuccessListener { document ->
                    val trenutnaKolicina = document.getLong("kolicina")?.toInt() ?: 0
                    val novaKolicina = trenutnaKolicina + itemToDelete.kolicina

                    artiklRef.update("kolicina", novaKolicina)
                        .addOnSuccessListener {
                            // Količina uspješno vraćena
                            Toast.makeText(requireContext(), "Količina vraćena u skladište", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Greška pri vraćanju količine", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            // Nakon vraćanja količine, ukloni item iz liste
            viewModel.removeItem(itemToDelete)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.items.observe(viewLifecycleOwner) { itemList ->
            adapter.setItems(itemList.toList())

            val ukupno = itemList.sumOf {
                it.kolicina * it.jedinicnaCijena
            }
            tvUkupnaCijena.text = "Ukupno %.2f €".format(ukupno)
        }
    }
}