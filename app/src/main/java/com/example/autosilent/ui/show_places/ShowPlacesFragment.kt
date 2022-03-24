package com.example.autosilent.ui.show_places

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.autosilent.R
import com.example.autosilent.adapters.GeofencesAdapter
import com.example.autosilent.databinding.FragmentShowPlacesBinding
import com.example.autosilent.viewmodels.GeofenceViewModel

class ShowPlacesFragment : Fragment() {

    private var _binding: FragmentShowPlacesBinding? = null
    private val binding get() = _binding!!

    private val geofenceViewModel: GeofenceViewModel by activityViewModels()
    private val geofencesAdapter by lazy { GeofencesAdapter(geofenceViewModel) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        //return inflater.inflate(R.layout.fragment_show_places, container, false)

        _binding = FragmentShowPlacesBinding.inflate(inflater, container, false)
        binding.geofenceViewModel = geofenceViewModel

        setupRecyclerView()
        observeDatabase()

        binding.addPlacesFab.setOnClickListener {
            findNavController().navigate(R.id.action_showPlacesFragment_to_addPlacesFragment)
        }

        return binding.root
    }


    private fun setupRecyclerView() {
        binding.geofencesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.geofencesRecyclerView.adapter = geofencesAdapter
    }

    private fun observeDatabase() {
        geofenceViewModel.readGeofences.observe(viewLifecycleOwner) { geofenceEntity->
            geofencesAdapter.setData(geofenceEntity)
            binding.geofencesRecyclerView.scheduleLayoutAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}