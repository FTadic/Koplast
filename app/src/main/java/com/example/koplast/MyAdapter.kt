package com.example.koplast

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val onDelete: (ToDoItem) -> Unit) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private var toDoItems: List<ToDoItem> = listOf()

    fun setItems(newToDoItems: List<ToDoItem>) {
        toDoItems = newToDoItems
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNaziv: TextView = itemView.findViewById(R.id.tvNaziv)
        val tvKolicina: TextView = itemView.findViewById(R.id.tvKolicina)
        val tvJCijena: TextView = itemView.findViewById(R.id.tvJCijena)
        val tvUkupno: TextView = itemView.findViewById(R.id.tvUkupno)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = toDoItems[position]
        holder.tvNaziv.text = item.naziv
        holder.tvKolicina.text = item.kolicina.toString() + " kom."
        holder.tvJCijena.text = String.format("%.2f €", item.jedinicnaCijena)
        val ukupno = item.kolicina * item.jedinicnaCijena
        holder.tvUkupno.text = String.format("%.2f €", ukupno)

        holder.btnDelete.setOnClickListener {
            onDelete(item)
        }
    }

    override fun getItemCount(): Int = toDoItems.size
}