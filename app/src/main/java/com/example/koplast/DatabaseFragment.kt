package com.example.koplast

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DatabaseFragment : Fragment(R.layout.fragment_database) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArtikliAdapter
    private val artikliList = mutableListOf<Artikl>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewDatabase)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ArtikliAdapter(artikliList,
            onEditClick = { artikl -> editArtikl(artikl) },
            onDeleteClick = { artikl -> deleteArtikl(artikl) })

        recyclerView.adapter = adapter

        val db = FirebaseFirestore.getInstance()
        db.collection("artikli")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                artikliList.clear()
                for (doc in snapshots.documents) {
                    val artikl = Artikl(
                        id = doc.id,
                        naziv = doc.getString("naziv") ?: "",
                        kategorija = doc.getString("kategorija") ?: "",
                        kolicina = (doc.getLong("kolicina") ?: 0L).toInt(),
                        cijena = (doc.getDouble("cijena") ?: 0.0)
                    )
                    artikliList.add(artikl)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun editArtikl(artikl: Artikl) {
        // Otvori dijalog ili novu aktivnost za uređivanje
        Toast.makeText(requireContext(), "Uredi: ${artikl.naziv}", Toast.LENGTH_SHORT).show()
    }

    private fun deleteArtikl(artikl: Artikl) {
        val db = FirebaseFirestore.getInstance()
        db.collection("artikli").document(artikl.id).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Obrisano: ${artikl.naziv}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri brisanju", Toast.LENGTH_SHORT).show()
            }
    }
}
