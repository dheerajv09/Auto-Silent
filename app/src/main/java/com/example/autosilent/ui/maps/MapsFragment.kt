package com.example.autosilent.ui.maps

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.autosilent.databinding.FragmentMapsBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.autosilent.R
import com.example.autosilent.adapters.PredictionsAdapter
import com.example.autosilent.util.ExtensionFunctions.disable
import com.example.autosilent.util.ExtensionFunctions.enable
import com.example.autosilent.util.ExtensionFunctions.hide
import com.example.autosilent.util.ExtensionFunctions.show
import com.example.autosilent.util.NetworkListener
import com.example.autosilent.util.Permissions.hasBackgroundLocationPermission
import com.example.autosilent.util.Permissions.requestsBackgroundLocationPermission
import com.example.autosilent.viewmodels.GeofenceViewModel
import com.example.autosilent.viewmodels.Step2ViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MapsFragment : Fragment(), OnMapReadyCallback , GoogleMap.OnMapLongClickListener,
    EasyPermissions.PermissionCallbacks, GoogleMap.SnapshotReadyCallback{

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val predictionsAdapter by lazy { PredictionsAdapter() }

    private val args by navArgs<MapsFragmentArgs>()

    private val geofenceViewModel: GeofenceViewModel by activityViewModels()
    private val step2ViewModel: Step2ViewModel by viewModels()

    private lateinit var placesClient: PlacesClient

    private lateinit var networkListener: NetworkListener

    private lateinit var map: GoogleMap
    private lateinit var circle: Circle

    //    private val callback = OnMapReadyCallback { googleMap ->
//        /**
//         * Manipulates the map once available.
//         * This callback is triggered when the map is ready to be used.
//         * This is where we can add markers or lines, add listeners or move the camera.
//         * In this case, we just add a marker near Sydney, Australia.
//         * If Google Play services is not installed on the device, the user will be prompted to
//         * install it inside the SupportMapFragment. This method will only be triggered once the
//         * user has installed Google Play services and returned to the app.
//         */
//        val sydney = LatLng(-34.0, 151.0)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireContext())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //return inflater.inflate(R.layout.fragment_maps, container, false)

        _binding = FragmentMapsBinding.inflate(layoutInflater, container, false)

        binding.geofenceViewModel = geofenceViewModel
        binding.step2ViewModel = step2ViewModel
        binding.lifecycleOwner = this

        checkInternetConnection()

        binding.addGeofenceFab.setOnClickListener {
            findNavController().navigate(R.id.action_global_showPlacesFragment)
        }

        binding.geofencesFab.setOnClickListener {
            //findNavController().navigate(R.id.action_mapsFragment_to_geofencesFragment)
        }

        setupRecyclerView()

        binding.geofenceLocationEt.doOnTextChanged { text, _, _, _ ->
            //handleNextButton(text)
            getPlaces(text)
        }

        subscribeToObservers()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.predictionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.predictionsRecyclerView.adapter = predictionsAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun getPlaces(text: CharSequence?) {
        if (geofenceViewModel.checkDeviceLocationSettings(requireContext())) {
            lifecycleScope.launch {
                if (text.isNullOrEmpty()) {
                    predictionsAdapter.setData(emptyList())
                } else {
                    val token = AutocompleteSessionToken.newInstance()

                    val request =
                        FindAutocompletePredictionsRequest.builder()
                            .setCountries(geofenceViewModel.geoCountryCode)
                            .setTypeFilter(TypeFilter.CITIES)
                            .setSessionToken(token)
                            .setQuery(text.toString())
                            .build()
                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            predictionsAdapter.setData(response.autocompletePredictions)
                            binding.predictionsRecyclerView.scheduleLayoutAnimation()
                        }
                        .addOnFailureListener { exception: Exception? ->
                            if (exception is ApiException) {
                                Log.e("Step2Fragment", exception.statusCode.toString())
                            }
                        }
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Please Enable Location Settings.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun subscribeToObservers() {
        lifecycleScope.launch {
            predictionsAdapter.placeId.collectLatest { placeId ->
                if (placeId.isNotEmpty()) {
                    onCitySelected(placeId)
                }
            }
        }
    }

    private fun onCitySelected(placeId: String) {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.LAT_LNG,
            Place.Field.NAME
        )
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                geofenceViewModel.geoLatLng = response.place.latLng!!
                geofenceViewModel.geoLocationName = response.place.name!!
                geofenceViewModel.geoCitySelected = true
                binding.geofenceLocationEt.setText(geofenceViewModel.geoLocationName)
                binding.geofenceLocationEt.setSelection(geofenceViewModel.geoLocationName.length)
                binding.predictionsRecyclerView.hide()
                step2ViewModel.enableNextButton(true)
            }
            .addOnFailureListener { exception ->
                Log.e("Step2Fragment", exception.message.toString())
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkInternetConnection() {
        lifecycleScope.launch {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext()).collect { online ->
                Log.d("Internet", online.toString())
                step2ViewModel.setInternetAvailability(online)
                if (online && geofenceViewModel.geoCitySelected) {
                    step2ViewModel.enableNextButton(true)
                } else {
                    step2ViewModel.enableNextButton(false)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyle))
        map.setOnMapLongClickListener(this)
        map.isMyLocationEnabled = true
        map.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = false
        }
        onGeofenceReady()
        observeDatabase()
        backFromGeofencesFragment()
    }

    private fun onGeofenceReady() {
        if (geofenceViewModel.geofenceReady) {
            geofenceViewModel.geofenceReady = false
            geofenceViewModel.geofencePrepared = true
            displayInfoMessage()
            zoomToSelectedLocation()
        }
    }

    private fun displayInfoMessage() {
        lifecycleScope.launch {
            binding.infoMessageTextView.show()
            delay(2000)
            binding.infoMessageTextView.animate().alpha(0f).duration = 800
            delay(1000)
            binding.infoMessageTextView.hide()
        }
    }

    private fun zoomToSelectedLocation() {
        //Todo: add listner to zoom location as onCitySelected() function is called
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(geofenceViewModel.geoLatLng, 15f), 2000, null
        )
    }

    private fun observeDatabase() {
        geofenceViewModel.readGeofences.observe(viewLifecycleOwner) { geofenceEntity ->
            map.clear()
            geofenceEntity.forEach { geofence ->
                drawCircle(LatLng(geofence.latitude, geofence.longitude), geofence.radius)
                drawMarker(LatLng(geofence.latitude, geofence.longitude), geofence.name)
            }
        }
    }

    private fun backFromGeofencesFragment() {
        if (args.geofenceEntity != null) {
            val selectedGeofence = LatLng(
                args.geofenceEntity!!.latitude,
                args.geofenceEntity!!.longitude
            )
            zoomToGeofence(selectedGeofence, args.geofenceEntity!!.radius)
        }
    }

    override fun onMapLongClick(location: LatLng) {
        if (hasBackgroundLocationPermission(requireContext())) {
            //if (sharedViewModel.geofencePrepared && location != null)
            if (geofenceViewModel.geofencePrepared) {
                setupGeofence(location)
            } else {
                Toast.makeText(
                    requireContext(),
                    "You need to create a new Geofence first.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            requestsBackgroundLocationPermission(this)
        }
    }

    private fun setupGeofence(location: LatLng) {
        lifecycleScope.launch {
            if (geofenceViewModel.checkDeviceLocationSettings(requireContext())) {
                binding.geofencesFab.disable()
                binding.addGeofenceFab.disable()
                binding.geofenceProgressBar.show()

                drawCircle(location, geofenceViewModel.geoRadius)
                drawMarker(location, geofenceViewModel.geoName)
                zoomToGeofence(circle.center, circle.radius.toFloat())

                delay(1500)
                map.snapshot(this@MapsFragment)
                delay(2000)
                geofenceViewModel.addGeofenceToDatabase(location)
                delay(2000)
                geofenceViewModel.startGeofence(location.latitude, location.longitude)
                geofenceViewModel.resetSharedValues()

                binding.geofencesFab.enable()
                binding.addGeofenceFab.enable()
                binding.geofenceProgressBar.hide()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enable Location Settings.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun drawCircle(location: LatLng, radius: Float) {
        circle = map.addCircle(
            CircleOptions().center(location).radius(radius.toDouble())
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.blue_700))
                .fillColor(ContextCompat.getColor(requireContext(), R.color.blue_transparent))
        )
    }

    private fun drawMarker(location: LatLng, name: String) {
        map.addMarker(
            MarkerOptions().position(location).title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
    }

    private fun zoomToGeofence(center: LatLng, radius: Float) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                geofenceViewModel.getBounds(center, radius), 10
            ), 1000, null
        )
    }

    override fun onSnapshotReady(snapshot: Bitmap?) {
        geofenceViewModel.geoSnapshot = snapshot
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestsBackgroundLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onGeofenceReady()
        Toast.makeText(
            requireContext(),
            "Permission Granted! Long Press on the Map to add a Geofence.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}