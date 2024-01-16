package ru.netology.markers.repository

import androidx.lifecycle.LiveData
import ru.netology.markers.dto.Marker

interface MarkerRepository {
    fun getAll(): LiveData<List<Marker>>
    fun save(marker: Marker)
    fun removeById(id: Int)
    fun removeAll()
    fun nextId(): Int
}