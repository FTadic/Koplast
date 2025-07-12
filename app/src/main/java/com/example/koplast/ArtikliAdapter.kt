package com.example.koplast

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArtikliAdapter(
    private val artikli: List<Artikl>,
    private val onEditClick: (Artikl) -> Unit,
    private val onDeleteClick: (Artikl) -> Unit
) : RecyclerView.Adapter<ArtikliAdapter.ArtiklViewHolder>() {

    inner class ArtiklViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNaziv = itemView.findViewById<TextView>(R.id.tvNaziv_Item)
        val tvKategorija = itemView.findViewById<TextView>(R.id.tvKategorija_Item)
        val tvKolicina = itemView.findViewById<TextView>(R.id.tvKolicina_Item)
        val tvCijena = itemView.findViewById<TextView>(R.id.tvJCijena_Item)
        val btnEdit_Item = itemView.findViewById<ImageView>(R.id.btnEdit_Item)
        val ivDelete = itemView.findViewById<ImageView>(R.id.btnDelete_Item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtiklViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_database, parent, false)
        return ArtiklViewHolder(view)
    }

    override fun getItemCount(): Int = artikli.size

    override fun onBindViewHolder(holder: ArtiklViewHolder, position: Int) {
        val artikl = artikli[position]
        holder.tvNaziv.text = artikl.naziv
        holder.tvKategorija.text = artikl.kategorija
        holder.tvKolicina.text = "${artikl.kolicina} kom."
        holder.tvCijena.text = String.format("%.2f €", artikl.cijena)

        holder.btnEdit_Item.setOnClickListener { onEditClick(artikl) }
        holder.ivDelete.setOnClickListener { onDeleteClick(artikl) }
    }
}
