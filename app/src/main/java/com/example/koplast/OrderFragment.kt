package com.example.koplast

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrderFragment : Fragment(R.layout.fragment_order) {
    private lateinit var auth : FirebaseAuth

    private lateinit var viewModel: SharedViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val spKategorije = view.findViewById<Spinner>(R.id.spKategorije)
        val spArtikli = view.findViewById<Spinner>(R.id.spArtikli)
        val etCijena = view.findViewById<EditText>(R.id.etCijena)
        val etKolicina = view.findViewById<EditText>(R.id.etKolicina)

        val btnDodaj = view.findViewById<Button>(R.id.btnDodaj)
        val btnTablica = view.findViewById<Button>(R.id.btnTablica)

        Log.d("ARTIKL", "Kategorije: $spKategorije, artikli: $spArtikli, cijena: $etCijena, kolicina: $etKolicina")

        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        val db = FirebaseFirestore.getInstance()

        val kategorijeList = mutableListOf<String>()
        val kategorijeIds = mutableListOf<String>()
        val artikliList = mutableListOf<String>()
        val artikliMap = mutableMapOf<String, Int>()
        val artikliMapCijena = mutableMapOf<String, Double>()

        db.collection("kategorije")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                kategorijeList.clear()
                kategorijeIds.clear()

                for (doc in snapshots.documents) {
                    val naziv = doc.getString("naziv") ?: continue
                    kategorijeList.add(naziv)
                    kategorijeIds.add(doc.id)
                }

                context?.let { ctx ->
                    val adapterKategorije = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, kategorijeList)
                    adapterKategorije.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spKategorije.adapter = adapterKategorije
                }
            }

        spKategorije.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val kategorijaId = kategorijeIds[position]

                db.collection("artikli")
                    .whereEqualTo("kategorija", kategorijaId)
                    .addSnapshotListener { snapshots, error ->
                        if (error != null || snapshots == null)
                            return@addSnapshotListener

                        artikliList.clear()
                        artikliMap.clear()
                        artikliMapCijena.clear()

                        for (doc in snapshots.documents) {
                            val naziv = doc.getString("naziv") ?: continue
                            val kolicina = when (val koli = doc.get("kolicina")) {
                                is Long -> koli.toInt()
                                is Double -> koli.toInt()
                                else -> 0
                            }
                            val cijena = when (val c = doc.get("cijena")) {
                                is Long -> c.toDouble()
                                is Double -> c
                                else -> 0.0
                            }
                            artikliList.add(naziv)
                            artikliMap[naziv] = kolicina
                            artikliMapCijena[naziv] = cijena
                            viewModel.artikliDocIds[naziv] = doc.id
                        }

                        context?.let { ctx ->
                            val adapterArtikli = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, artikliList)
                            adapterArtikli.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spArtikli.adapter = adapterArtikli
                        }

                        spArtikli.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val odabraniArtikl = artikliList[position]
                                val kolicina = artikliMap[odabraniArtikl] ?: 0
                                val cijena = artikliMapCijena[odabraniArtikl] ?: 0.0

                                etKolicina.setText(kolicina.toString())
                                etKolicina.filters = arrayOf(InputFilterMinMax(0, kolicina))
                                etCijena.setText(String.format("%.2f", cijena))
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }

                    }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnTablica.setOnClickListener {
            findNavController().navigate(R.id.action_orderFragment_to_tableFragment)
        }

        btnDodaj.setOnClickListener {
            val naziv = spArtikli.selectedItem.toString() ?: return@setOnClickListener
            val kolicinaText = etKolicina.text.toString()
            val cijenaText = etCijena.text.toString()

            if (kolicinaText.isNotBlank() && cijenaText.isNotBlank()) {
                val kolicina = kolicinaText.toIntOrNull() ?: return@setOnClickListener
                val cijena = cijenaText.toDoubleOrNull() ?: return@setOnClickListener

                viewModel.addItem(ToDoItem(naziv, kolicina, cijena))

                var artiklId = viewModel.artikliDocIds[naziv] ?: return@setOnClickListener

                val artiklRef = db.collection("artikli").document(artiklId)

                artiklRef.get().addOnSuccessListener { document ->
                    val trenutnaKolicina = document.getLong("kolicina")?.toInt() ?: 0
                    val novaKolicina = (trenutnaKolicina - kolicina).coerceAtLeast(0)

                    artiklRef.update("kolicina", novaKolicina)
                        .addOnSuccessListener {
                            artikliMap[naziv] = novaKolicina

                            etKolicina.setText(novaKolicina.toString())
                            etKolicina.filters = arrayOf(InputFilterMinMax(0, novaKolicina))
                        }
                        .addOnFailureListener {
                            context?.let {
                                Toast.makeText(it, "Neuspješno ažuriranje količine", Toast.LENGTH_SHORT).show()
                            }

                        }
                }

                etKolicina.text.clear()
            }
        }
    }
}
