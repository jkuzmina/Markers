package ru.netology.markers.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.markers.dto.Marker
import ru.netology.markers.repository.MarkerRepository
import ru.netology.markers.repository.MarkerRepositoryImpl
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.launch
import ru.netology.markers.util.SingleLiveEvent

private val empty = Marker(
    id = 0,
    point = Point(0.0, 0.0),
    description = ""
)

class MarkerViewModel(application: Application): AndroidViewModel(application) {

    private val repository: MarkerRepository = MarkerRepositoryImpl(application)
    val data = repository.getAll()
    val edited = MutableLiveData(empty)
    private val _markerCreated = SingleLiveEvent<Unit>()
    val markerCreated: LiveData<Unit>
        get() = _markerCreated
    private val _descriptionChanged = SingleLiveEvent<Unit>()
    val descriptionChanged: LiveData<Unit>
        get() = _descriptionChanged

    fun create(point: Point) {
        repository.save(Marker(0, point, ""))
        _markerCreated.value = Unit
        /*edited.value?.point = point
        edited.value?.let {
            _markerCreated.value = Unit
            repository.save(it.copy(point = point))
        }
        edited.value = empty*/
    }

    fun save(marker: Marker) {
        repository.save(marker)
    }

    fun edit(marker: Marker) {
        edited.value = marker
    }

    fun changeDescription(description: String) {
        val text = description.trim()
        if (edited.value?.description == text) {
            return
        }
        edited.value = edited.value?.copy(description = text)
        _descriptionChanged.value = Unit
    }
    fun removeById(id: Int) = repository.removeById(id)

    fun removeAll() = repository.removeAll()

    fun lastMarker(): Marker?{
        return data.value?.maxBy { it.id }
    }

    fun getMarkerById(id: Int): Marker?{
        return data.value?.find {
            it.id == id
        }
    }
}