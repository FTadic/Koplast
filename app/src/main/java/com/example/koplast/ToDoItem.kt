package com.example.koplast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class ToDoItem (
    val naziv: String,
    var kolicina: Int,
    var jedinicnaCijena: Double
)

class SharedViewModel : ViewModel() {
    private val _items = MutableLiveData<MutableList<ToDoItem>>(mutableListOf())
    val items: LiveData<MutableList<ToDoItem>> = _items

    val artikliDocIds = mutableMapOf<String, String>()

    fun addItem(item: ToDoItem) {
        val currentList = _items.value ?: mutableListOf()
        val existingItem = currentList.find { it.naziv == item.naziv }

        if (existingItem != null) {
            // Ažuriraj količinu i zbroji cijenu
            existingItem.kolicina += item.kolicina
            existingItem.jedinicnaCijena += item.jedinicnaCijena
        } else {
            currentList.add(item)
        }

        _items.value = currentList
    }

    fun removeItem(item: ToDoItem) {
        _items.value = _items.value?.toMutableList()?.apply {
            remove(item)
        }
    }

    fun clearAllItems() {
        _items.value = mutableListOf()
    }
}