package ru.netology.markers.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.ui_view.ViewProvider
import ru.netology.markers.R
import ru.netology.markers.dto.Marker
import ru.netology.markers.viewmodel.MarkerViewModel

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun moveCamera(mapView: MapView, point: Point, zoom: Float) {
        mapView.map.move(
            CameraPosition(point, zoom, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0f),
            null
        )
    }

    fun addMarkerOnMap(context: Context, mapView: MapView, point: Point){
        val markerView = View(context).apply {
            background = context.getDrawable(R.drawable.ic_baseline_location_on_48)
        }
        mapView.getMap().getMapObjects()?.addPlacemark(point, ViewProvider(markerView))
    }

}