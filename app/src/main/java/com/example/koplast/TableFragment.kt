package com.example.koplast

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream


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

        val btnIzradiRacun = view.findViewById<Button>(R.id.btnIzradiRacun)

        btnIzradiRacun.setOnClickListener {
            /*val input = EditText(requireContext())
            input.hint = "Unesite ime kupca"

            AlertDialog.Builder(requireContext())
                .setTitle("Ime kupca")
                .setView(input)
                .setPositiveButton("Unesi") { _, _ ->
                    val imeKupca = input.text.toString().trim()
                    if (imeKupca.isNotBlank()) {
                        val items = viewModel.items.value.orEmpty()
                        generateAndSharePDF(items, imeKupca)
                    } else {
                        Toast.makeText(requireContext(), "Ime kupca je obavezno", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Odustani", null)
                .show()*/


            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.unos_kupca_pdf, null)
            val builder = AlertDialog.Builder(requireContext())
                .setTitle("Unesi kupca")
                .setView(dialogView)
                .setPositiveButton("Unesi", null)
                .setNegativeButton("Odustani", null)

            val etImeKupca = dialogView.findViewById<EditText>(R.id.etImeKupca)
            val tilKupac = dialogView.findViewById<TextInputLayout>(R.id.tilKupac)

            val dialog = builder.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    tilKupac.error = null

                    val imeKupca = etImeKupca.text.toString().trim()

                    var valid = true

                    if (imeKupca.isEmpty()) {
                        tilKupac.error = "Ime kupca je obavezno"
                        valid = false
                    } else {
                        val items = viewModel.items.value.orEmpty()
                        generateAndSharePDF(items, imeKupca)
                        dialog.dismiss()
                    }

                    if (!valid) return@setOnClickListener
                }
            }
            dialog.show()

            /*viewModel.items.value?.let {
                generateAndSharePDF(it.toList(), imeKupca)
            }*/
        }
    }

    private fun generateAndSharePDF(items: List<ToDoItem>, kupac: String) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 16f
        paint.isFakeBoldText = true

        // Naslov
        canvas.drawText("Račun", 297.5f, 50f, paint)


        paint.textSize = 14f
        canvas.drawText("Kupac: $kupac", 297.5f, 75f, paint)  // Dodano ime kupca


        // Tablica zaglavlja
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 14f
        paint.isFakeBoldText = true

        val startX = 50f
        var startY = 100f
        val rowHeight = 30f
        val col1 = 50f
        val col2 = 250f
        val col3 = 400f

        canvas.drawText("Artikl", col1, startY, paint)
        canvas.drawText("Količina", col2, startY, paint)
        canvas.drawText("Cijena (€)", col3, startY, paint)
        startY += rowHeight

        paint.isFakeBoldText = false

        // Redovi
        for (item in items) {
            canvas.drawText(item.naziv, col1, startY, paint)
            canvas.drawText(item.kolicina.toString(), col2, startY, paint)
            canvas.drawText("%.2f".format(item.jedinicnaCijena), col3, startY, paint)
            startY += rowHeight
        }

        // Izračun ukupne cijene
        val ukupnaCijena = items.sumOf { it.kolicina * it.jedinicnaCijena }

        // Dodaj razmak prije ispisa ukupne cijene
        startY += 20f

        paint.isFakeBoldText = true
        canvas.drawText("Ukupno:", col2, startY, paint)
        canvas.drawText("%.2f €".format(ukupnaCijena), col3, startY, paint)


        pdfDocument.finishPage(page)

        // Spremanje
        val file = File(requireContext().cacheDir, "narudzba.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        // Dijeljenje
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Pošalji PDF"))
        viewModel.clearAllItems()
    }

}