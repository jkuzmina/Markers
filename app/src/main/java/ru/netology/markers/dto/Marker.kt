package ru.netology.markers.dto

import android.content.Context
import android.provider.Settings.Global.getString
import com.yandex.mapkit.geometry.Point
import ru.netology.markers.R

data class Marker(
    val id: Int,
    var point: Point,
    var description: String = "")
{
    fun getString(context: Context): String{
        return if(description.isNullOrBlank()){
            context.resources.getString(R.string.description, point.latitude, point.longitude)
        } else{
            description
        }
    }

    fun getTitle(context: Context): String{
        return if(description.isNullOrBlank()){
            context.resources.getString(R.string.coordinates, point.latitude, point.longitude)
        } else{
            description
        }
    }

}



