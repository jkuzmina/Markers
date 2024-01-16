package ru.netology.markers.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import android.widget.AdapterView.AdapterContextMenuInfo
import androidx.core.view.MenuProvider
import androidx.fragment.app.ListFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.yandex.mapkit.Animation
import com.yandex.mapkit.map.CameraPosition
import ru.netology.markers.R
import ru.netology.markers.databinding.FragmentMarkersListBinding
import ru.netology.markers.dto.Marker
import ru.netology.markers.viewmodel.MarkerViewModel


class MarkersList : ListFragment(), MenuProvider {

    private val viewModel: MarkerViewModel by activityViewModels()
    var markers: MutableList<Marker> = mutableListOf<Marker>()
    var fragmentBinding: FragmentMarkersListBinding? = null
    private var markerListAdapter: MarkerListAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMarkersListBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().setTitle(R.string.app_name)
        viewModel.data.value?.let{
            markers = it.toMutableList()
        }
        val adapter: ListAdapter = MarkerListAdapter(
            requireActivity(),
            R.layout.fragment_markers_list,
            markers,
        )
        listAdapter = adapter
        markerListAdapter = listAdapter as MarkerListAdapter
        registerForContextMenu(listView)

        //добавляем меню
        val menuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        //requireActivity().actionBar?.setHomeButtonEnabled(true)
    }
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_marker_list, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.delete -> {
                viewModel.removeAll()
                markers.clear()
                markerListAdapter?.notifyDataSetChanged()
                true
            }
            else -> false
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.options_marker, menu)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val marker = markers.get(position)
        val bundle = Bundle()
        bundle.putInt("markerId", marker.id)
        val navController = findNavController()
        navController.navigate(R.id.action_markersList_to_mapCurrentMarker, bundle)
    }
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val acmi = item.menuInfo as AdapterContextMenuInfo
        val marker = markers.get(acmi.position)
        when (item.itemId) {
            R.id.edit -> {
                val bundle = Bundle()
                bundle.putInt("markerId", marker.id)
                val navController = findNavController()
                navController.navigate(R.id.action_markersList_to_editMarker, bundle)
                return true
            }
            R.id.remove -> {
                viewModel.removeById(marker.id)
                markers.remove(marker)
                markerListAdapter?.notifyDataSetChanged()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }
    class MarkerListAdapter(
        context: Context,
        textViewResourceId: Int,
        objects: List<Marker>?,
    ) :
        ArrayAdapter<Marker>(context, textViewResourceId, objects!!) {
        private val mContext: Context
        init {
            mContext = context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val row: View = inflater.inflate(
                R.layout.listfragment_row, parent,
                false
            )
            val marker = getItem(position)
            val pointText = row.findViewById<View>(R.id.point) as TextView
            val description = row.findViewById<View>(R.id.description) as TextView
            val icon = row.findViewById<View>(R.id.icon) as ImageView
            marker?.let{
                pointText.setText("${marker.point.latitude}, ${marker.point.longitude}")
                description.setText("${marker.description}")
                icon.setImageResource(R.drawable.ic_baseline_location_on_48)
            }
            return row
        }
    }

}