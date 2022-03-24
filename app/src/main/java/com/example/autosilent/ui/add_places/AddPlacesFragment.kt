package com.example.autosilent.ui.add_places

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.autosilent.R
import android.annotation.SuppressLint
import android.location.Geocoder
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.autosilent.databinding.FragmentAddPlacesBinding
import com.example.autosilent.viewmodels.GeofenceViewModel
import com.example.autosilent.viewmodels.Step1ViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import java.io.IOException

class AddPlacesFragment : Fragment() {

    private var _binding: FragmentAddPlacesBinding? = null
    private val binding get() = _binding!!

    private val geofenceViewModel: GeofenceViewModel by activityViewModels()
    private val step1ViewModel: Step1ViewModel by viewModels()

    private lateinit var geoCoder: Geocoder
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireContext())
        geoCoder = Geocoder(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlacesBinding.inflate(layoutInflater, container, false)
        binding.geofenceViewModel = geofenceViewModel
        binding.step1ViewModel = step1ViewModel
        binding.lifecycleOwner = this

        binding.step1Next.setOnClickListener {
            geofenceViewModel.geoRadius = binding.slider.value
            geofenceViewModel.geofenceReady = true
            findNavController().navigate(R.id.action_addPlacesFragment_to_mapsFragment)
            Log.d("Step3Fragment", geofenceViewModel.geoRadius.toString())
        }

        getCountryCodeFromCurrentLocation()

        return binding.root
    }

    @SuppressLint("MissingPermission")
    private fun getCountryCodeFromCurrentLocation() {
        lifecycleScope.launch {
            val placeFields = listOf(Place.Field.LAT_LNG)
            val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    try {
                        val response = task.result
                        val latLng = response.placeLikelihoods.first().place.latLng!!
                        geofenceViewModel.geoLatLng = latLng
                        val address = geoCoder.getFromLocation(
                            latLng.latitude,
                            latLng.longitude,
                            1
                        )
                        geofenceViewModel.geoCountryCode = address.first().countryCode
                        Log.i("Step1Fragment",geofenceViewModel.geoCountryCode)
                    } catch (exception: IOException) {
                        Log.e("Step1Fragment", "getFromLocation FAILED")
                    } finally {
                        Log.i("Step1Fragment",geofenceViewModel.geoCountryCode)
                        enableNextButton()
                    }
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e("Step1Fragment", exception.statusCode.toString())
                        Log.e("Step1Fragment", exception.message.toString())
                        Log.e("Step1Fragment", exception.cause?.stackTrace.toString())
                    }
                    enableNextButton()
                }
            }
        }
    }

    private fun enableNextButton() {
        if (geofenceViewModel.geoName.isNotEmpty()) {
            step1ViewModel.enableNextButton(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}