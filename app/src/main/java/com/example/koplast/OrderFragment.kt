package com.example.koplast

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrderFragment : Fragment(R.layout.fragment_order) {
    private lateinit var auth : FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val spKategorije = view.findViewById<Spinner>(R.id.spKategorije)
        val spArtikli = view.findViewById<Spinner>(R.id.spArtikli)
        val etKolicina = view.findViewById<EditText>(R.id.etKolicina)

        val db = FirebaseFirestore.getInstance()

        val kategorijeList = mutableListOf<String>()
        val kategorijeIds = mutableListOf<String>()
        val artikliList = mutableListOf<String>()
        val artikliMap = mutableMapOf<String, Int>()

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

                val adapterKategorije = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, kategorijeList)
                adapterKategorije.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spKategorije.adapter = adapterKategorije
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

                        for (doc in snapshots.documents) {
                            val naziv = doc.getString("naziv") ?: continue
                            val kolicina = doc.getLong("kolicina") ?.toInt() ?: 0
                            artikliList.add(naziv)
                            artikliMap[naziv] = kolicina
                        }

                        val adapterArtikli = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, artikliList)
                        adapterArtikli.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spArtikli.adapter = adapterArtikli

                        spArtikli.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val odabraniArtikl = artikliList[position]
                                val kolicina = artikliMap[odabraniArtikl] ?: 0
                                etKolicina.setText(kolicina.toString())

                                etKolicina.filters = arrayOf(InputFilterMinMax(0, kolicina))
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }

                    }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
