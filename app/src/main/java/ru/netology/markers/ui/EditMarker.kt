package ru.netology.markers.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.geometry.Point
import ru.netology.markers.databinding.FragmentEditMarkerBinding
import ru.netology.markers.dto.Marker
import ru.netology.markers.viewmodel.MarkerViewModel

private const val MARKER_ID = "markerId"
class EditMarker : Fragment() {

    private val viewModel: MarkerViewModel by activityViewModels()
    private var marker = Marker(0, Point(0.0, 0.0), "")


    private var markerId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            markerId = it.getInt(MARKER_ID)
            val editedMarker = viewModel.getMarkerById(markerId)
            if(editedMarker != null){
                marker = editedMarker
            }

        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditMarkerBinding.inflate(
            inflater,
            container,
            false
        )

        binding.description.setHint(marker.getString(requireContext()))
        binding.edit.setText(marker.description)
        binding.edit.requestFocus()

        binding.buttonOk.setOnClickListener{
            viewModel.edit(marker)
            viewModel.changeDescription(binding.edit.text.toString())
        }

        viewModel.descriptionChanged.observe(viewLifecycleOwner){
            viewModel.save(viewModel.edited.value!!)
            findNavController().navigateUp()
        }

        binding.buttonCancel.setOnClickListener{
            findNavController().navigateUp()
        }
        return binding.root
    }


}