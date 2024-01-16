package ru.netology.markers.ui

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.ui_view.ViewProvider
import ru.netology.markers.R
import ru.netology.markers.databinding.FragmentMapCurrentMarkerBinding
import ru.netology.markers.dto.Marker
import ru.netology.markers.util.AndroidUtils.addMarkerOnMap
import ru.netology.markers.viewmodel.MarkerViewModel
import ru.netology.markers.util.AndroidUtils.moveCamera

private const val MARKER_ID = "markerId"
class MapCurrentMarker : Fragment(), MenuProvider {

    val COMFORTABLE_ZOOM_LEVEL = 18f
    private val viewModel: MarkerViewModel by activityViewModels()
    private var marker = Marker(0, Point(0.0, 0.0), "")
    private var markerId: Int = 0
    lateinit var mapView: MapView
    private var mapObjects: MapObjectCollection? = null
    private var binding: FragmentMapCurrentMarkerBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            markerId = it.getInt(MARKER_ID)
            marker = viewModel.getMarkerById(markerId)!!
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentMapCurrentMarkerBinding.inflate(
            inflater,
            container,
            false
        )

        mapView = fragmentBinding.mapViewCurrent
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.description?.text = marker.description
        MapKitFactory.initialize(context)
        mapObjects = mapView.getMap().getMapObjects()
        addMarkerOnMap(requireContext(), mapView, marker.point)
        moveCamera(mapView, marker.point, COMFORTABLE_ZOOM_LEVEL)

        //добавляем меню
        val menuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        menuHost.setTitle(marker.getTitle(requireContext()))
    }



    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_app_bar_current, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.zoomInMenu -> {
                val cameraPosition = mapView.map.cameraPosition
                mapView.map.move(
                    CameraPosition(
                        cameraPosition.target,
                        cameraPosition.zoom + 0.9f,
                        0.0f,
                        0.0f
                    ),
                    Animation(Animation.Type.SMOOTH, 1f),
                    null
                )
                true
            }
            R.id.zoomOutMenu -> {
                val cameraPosition = mapView.map.cameraPosition
                mapView.map.move(
                    CameraPosition(
                        cameraPosition.target,
                        cameraPosition.zoom - 0.9f,
                        0.0f,
                        0.0f
                    ),
                    Animation(Animation.Type.SMOOTH, 1f),
                    null
                )
                true
            }
            else -> false
        }
    }
}