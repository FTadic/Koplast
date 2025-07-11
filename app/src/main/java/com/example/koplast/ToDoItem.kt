package com.example.koplast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class ToDoItem (
    val naziv: String,
    val kolicina: Int,
    val jedinicnaCijena: Double
)

class SharedViewModel : ViewModel() {
    private val _items = MutableLiveData<MutableList<ToDoItem>>(mutableListOf())
    val items: LiveData<MutableList<ToDoItem>> = _items

    fun addItem(item: ToDoItem) {
        _items.value?.add(item)
        _items.value = _items.value
    }

    fun removeItem(item: ToDoItem) {
        _items.value = _items.value?.toMutableList()?.apply {
            remove(item)
        }
    }
}