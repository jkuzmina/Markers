package ru.netology.markers.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.PointF
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import ru.netology.markers.R
import ru.netology.markers.databinding.FragmentMapBinding
import ru.netology.markers.dto.Marker
import ru.netology.markers.util.AndroidUtils.addMarkerOnMap
import ru.netology.markers.util.AndroidUtils.moveCamera
import ru.netology.markers.viewmodel.MarkerViewModel


class MapFragment : Fragment(), MenuProvider, UserLocationObjectListener, CameraListener{
    private val COMFORTABLE_ZOOM_LEVEL = 18f
    private val viewModel: MarkerViewModel by activityViewModels()
    lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    private var mapObjects: MapObjectCollection? = null
    private var liveData: LiveData<List<Marker>>? = null
    private var binding: FragmentMapBinding? = null
    private lateinit var navController: NavController
    private var followUserLocation = false
    private var permissionLocation = false
    private var myLocation = Point(0.0, 0.0)
    private lateinit var checkLocationPermission: ActivityResultLauncher<Array<String>>
    private val inputListener: InputListener = object : InputListener {
        override fun onMapTap(p0: Map, p1: Point) {
            //записали маркер в файл
            createMarker(p1)
            //отрисовали на карте
            addMarkerOnMap(requireContext(), mapView, p1)
        }

        override fun onMapLongTap(p0: Map, p1: Point) {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentMapBinding.inflate(
            inflater,
            container,
            false
        )

        mapView = fragmentBinding.mapView
        binding = fragmentBinding
        liveData = viewModel.data
        navController = this.findNavController()
        checkLocationPermission = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[ACCESS_FINE_LOCATION] == true ||
                permissions[ACCESS_COARSE_LOCATION] == true) {
                onMapReady()
            }
        }

        checkPermission()

        userInterface()

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MapKitFactory.initialize(context)

        mapObjects = mapView.map.mapObjects

        mapView.map.addInputListener(inputListener)

        viewModel.data.observe(viewLifecycleOwner) {
            liveData = viewModel.data
        }

        viewModel.markerCreated.observe(viewLifecycleOwner) {
            val marker = viewModel.lastMarker()
            if (marker != null) {
                //передаем Id маркера на фрагмент ввода описания
                val bundle = Bundle()
                bundle.putInt("markerId", marker.id)
                val navController = findNavController()
                navController.navigate(R.id.action_mapFragment_to_editMarker, bundle)
            }
        }
        addAllMarkers()
        if(viewModel.data.value?.isEmpty() == false){
            val marker = viewModel.lastMarker()
            if (marker != null) {
                moveCamera(mapView, marker.point, COMFORTABLE_ZOOM_LEVEL)
            }
        } else {
            moveCamera(mapView, myLocation, COMFORTABLE_ZOOM_LEVEL)
        }
        //добавляем меню
        val menuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_app_bar, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.location -> {
                userInterface()
                true
            }
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
            R.id.markerList ->{
                findNavController().navigate(R.id.action_mapFragment_to_markersList)
                true
            }
            else -> false
        }
    }


    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        //locationListener?.let { locationManager?.unsubscribe(it) };
        super.onStop()
    }

    override fun onStart() {

        mapView.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()


    }
    //проверка разрешения на запрос местоположения
    private fun checkPermission() {
        if (checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED ||
            checkSelfPermission(requireContext(), ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
        ) {
            onMapReady()
        } else {
            checkLocationPermission.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        }
    }


    private fun addAllMarkers() {
        liveData?.value?.forEach {
            addMarkerOnMap(requireContext(), mapView, it.point)
        }
    }


    fun createMarker(point: Point) {
        viewModel.create(point)
    }

    override fun onObjectAdded(p0: UserLocationView) {
        userLocationLayer.setAnchor(
            PointF((mapView.width()* 0.5).toFloat(), (mapView.height()* 0.5).toFloat()),
            PointF((mapView.width()* 0.5).toFloat(), (mapView.height()* 0.83).toFloat())
            )
        followUserLocation = false
    }

    override fun onObjectRemoved(p0: UserLocationView) {
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
    }

    private fun userInterface() {
        if (permissionLocation) {
            cameraUserPosition()

            followUserLocation = true
        } else {
            checkPermission()
        }
    }

    private fun onMapReady() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(this)

        mapView.map.addCameraListener(this)

        cameraUserPosition()

        permissionLocation = true
    }

    //фокус на местоположение пользователя
    private fun cameraUserPosition() {
        if (userLocationLayer.cameraPosition() != null) {
            myLocation = userLocationLayer.cameraPosition()!!.target
            moveCamera(mapView, myLocation, COMFORTABLE_ZOOM_LEVEL)
        } else {
            moveCamera(mapView, Point(0.0, 0.0), COMFORTABLE_ZOOM_LEVEL)
        }
    }

    override fun onCameraPositionChanged(
        p0: Map,
        p1: CameraPosition,
        p2: CameraUpdateReason,
        finish: Boolean
    ) {
        if (finish) {
            if (followUserLocation) {
                setAnchor()
            }
        } else {
            if (!followUserLocation) {
                noAnchor()
            }
        }
    }

    private fun setAnchor() {
        userLocationLayer.setAnchor(
            PointF(
                (mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()
            ),
            PointF(
                (mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat()
            )
        )

        followUserLocation = false
    }

    private fun noAnchor() {
        userLocationLayer.resetAnchor()
    }
}