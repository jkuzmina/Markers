package ru.netology.markers.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.mapkit.geometry.Point
import ru.netology.markers.dto.Marker

class MarkerRepositoryImpl(private val context: Context) : MarkerRepository {
    private val gson = Gson()
    private val type = TypeToken.getParameterized(List::class.java, Marker::class.java).type
    private val filename = "markers.json"
    private var markers = emptyList<Marker>()
    private val data = MutableLiveData(markers)

    init {
        val file = context.filesDir.resolve(filename)
        if (file.exists()) {
            // если файл есть - читаем
            context.openFileInput(filename).bufferedReader().use {
                markers = gson.fromJson(it, type)
                data.value = markers
            }
        } else {
            // если нет, записываем пустой массив
            sync()
        }
    }

    override fun getAll(): LiveData<List<Marker>> = data

    override fun save(marker: Marker) {
        if (marker.id == 0) {
            val id = nextId()
            markers = listOf(
                marker.copy(
                    id = id,
                    marker.point,
                    ""
                )
            ) + markers
            Log.d("NEW_MARKER", "{${id}_${marker.point.latitude}_${marker.point.longitude}}")
            data.value = markers
            sync()
            return
        }
        //редактирование
        markers = markers.map {
            if (it.id != marker.id) it else it.copy(description = marker.description)
        }
        data.value = markers
        sync()
    }

    override fun removeById(id: Int) {
        markers = markers.filter { it.id != id }
        data.value = markers
        sync()
    }

    override fun removeAll() {
        markers = emptyList<Marker>()
        data.value = markers
        sync()
    }

    private fun sync() {
        context.openFileOutput(filename, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(markers))
        }
    }

    override fun nextId(): Int {
        val lastId = markers.maxByOrNull { it.id }?.id
        return if(lastId != null) {
            lastId + 1
        } else{
            1
        }
    }
}