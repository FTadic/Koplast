package com.example.koplast

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout

class DatabaseFragment : Fragment(R.layout.fragment_database) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArtikliAdapter
    private val artikliList = mutableListOf<Artikl>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab = view.findViewById<FloatingActionButton>(R.id.fabDodajArtikl)
        fab.setOnClickListener {
            prikaziDialogZaUnosArtikla()
        }

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
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_artikl_database, null)

        val etNaziv = dialogView.findViewById<EditText>(R.id.etNaziv_Edit)
        val etKategorija = dialogView.findViewById<EditText>(R.id.etKategorija_Edit)
        val etKolicina = dialogView.findViewById<EditText>(R.id.etKolicina_Edit)
        val etCijena = dialogView.findViewById<EditText>(R.id.etCijena_Edit)

        // Postavi postojeće vrijednosti
        etNaziv.setText(artikl.naziv)
        etKategorija.setText(artikl.kategorija)
        etKolicina.setText(artikl.kolicina.toString())
        etCijena.setText(artikl.cijena.toString())

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Uredi artikl")
            .setView(dialogView)
            .setPositiveButton("Spremi", null)
            .setNegativeButton("Odustani", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val naziv = etNaziv.text.toString().trim()
                val kategorija = etKategorija.text.toString().trim().lowercase()
                val kolicina = etKolicina.text.toString().trim().toIntOrNull()
                val cijena = etCijena.text.toString().trim().toDoubleOrNull()

                if (naziv.isEmpty() || kategorija.isEmpty() || kolicina == null || cijena == null) {
                    Toast.makeText(requireContext(), "Sva polja moraju biti ispravno popunjena", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val db = FirebaseFirestore.getInstance()
                db.collection("artikli").document(artikl.id)
                    .update(
                        mapOf(
                            "naziv" to naziv,
                            "kategorija" to kategorija,
                            "kolicina" to kolicina,
                            "cijena" to cijena
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Artikl ažuriran", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Greška pri ažuriranju", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        dialog.show()
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

    private fun prikaziDialogZaUnosArtikla() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dodaj_artikl_database, null)

        val tilId = dialogView.findViewById<TextInputLayout>(R.id.tilId)
        val tilNaziv = dialogView.findViewById<TextInputLayout>(R.id.tilNaziv)
        val tilKategorija = dialogView.findViewById<TextInputLayout>(R.id.tilKategorija)
        val tilKolicina = dialogView.findViewById<TextInputLayout>(R.id.tilKolicina)
        val tilCijena = dialogView.findViewById<TextInputLayout>(R.id.tilCijena)

        val etId = dialogView.findViewById<EditText>(R.id.etId)
        val etNaziv = dialogView.findViewById<EditText>(R.id.etNaziv)
        val etKategorija = dialogView.findViewById<EditText>(R.id.etKategorija)
        val etKolicina = dialogView.findViewById<EditText>(R.id.etKolicina)
        val etCijena = dialogView.findViewById<EditText>(R.id.etCijena)

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Dodaj artikl")
            .setView(dialogView)
            .setPositiveButton("Dodaj", null)
            .setNegativeButton("Odustani", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                // Resetiraj sve greške
                tilId.error = null
                tilNaziv.error = null
                tilKategorija.error = null
                tilKolicina.error = null
                tilCijena.error = null

                val id = etId.text.toString().trim()
                val naziv = etNaziv.text.toString().trim()
                val kategorija = etKategorija.text.toString().trim()
                val kolicinaStr = etKolicina.text.toString().trim()
                val cijenaStr = etCijena.text.toString().trim()

                var valid = true

                if (id.isEmpty()) {
                    tilId.error = "ID je obavezan"
                    valid = false
                }
                if (naziv.isEmpty()) {
                    tilNaziv.error = "Naziv je obavezan"
                    valid = false
                }
                if (kategorija.isEmpty()) {
                    tilKategorija.error = "Kategorija je obavezna"
                    valid = false
                }
                if (kolicinaStr.isEmpty()) {
                    tilKolicina.error = "Količina je obavezna"
                    valid = false
                }
                if (cijenaStr.isEmpty()) {
                    tilCijena.error = "Cijena je obavezna"
                    valid = false
                }

                val kolicina = kolicinaStr.toIntOrNull()
                val cijena = cijenaStr.toDoubleOrNull()

                if (kolicinaStr.isNotEmpty() && kolicina == null) {
                    tilKolicina.error = "Unesite ispravan broj"
                    valid = false
                }
                if (cijenaStr.isNotEmpty() && cijena == null) {
                    tilCijena.error = "Unesite ispravnu cijenu"
                    valid = false
                }

                if (!valid) return@setOnClickListener

                val db = FirebaseFirestore.getInstance()
                val artikliRef = db.collection("artikli").document(id)

                artikliRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        tilId.error = "ID već postoji"
                    } else {
                        val artikl = hashMapOf(
                            "naziv" to naziv,
                            "kategorija" to kategorija,
                            "kolicina" to kolicina,
                            "cijena" to cijena
                        )

                        artikliRef.set(artikl)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Artikl dodan", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Greška pri dodavanju", Toast.LENGTH_SHORT).show()
                            }
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Greška pri provjeri ID-a", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }


}
