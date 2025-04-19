package com.example.exploralocal

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.exploralocal.databinding.ActivityMapsBinding
import com.example.exploralocal.db.Place
import com.example.exploralocal.viewmodels.PlacesViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val viewModel: PlacesViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        isGranted ->
        if (isGranted) {
            enableMyLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.fabList.setOnClickListener {
            startActivity(Intent(this, PlacesListActivity::class.java))
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        mMap.setOnMapClickListener { latLng ->
            showAddPlaceDialog(latLng)
        }
        viewModel.places.observe(this) { places ->
            places.forEach { place ->
                val location = LatLng(place.latitude, place.longitude)
                mMap.addMarker(MarkerOptions()
                    .position(location)
                    .title(place.name)
                    .snippet("Rating: ${place.rating}"))
            }
        }

        checkLocationPermission()

        viewModel.loadPlaces()
    }

    private fun showAddPlaceDialog(latLng: LatLng) {
        val dialogView = layoutInflater.inflate((R.layout.dialog_add_place), null)
        val nameInput = dialogView.findViewById<EditText>(R.id.etPlaceName)
        val descInput = dialogView.findViewById<EditText>(R.id.etPlaceDescription)
        val ratingInput = dialogView.findViewById<EditText>(R.id.etPlaceRating)

        AlertDialog.Builder(this)
            .setTitle("Agregar nuevo lugar")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _, ->
                val name = nameInput.text.toString()
                val description = descInput.text.toString()
                val rating = ratingInput.text.toString().toFloatOrNull() ?: 0f

                if (name.isNotBlank()) {
                    val newPlace = Place(
                        name = name,
                        description = description,
                        rating = rating,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        photoPath = null
                    )
                    viewModel.addPlace(newPlace)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableMyLocation()
            } else -> {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        }
    }
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        try {
            mMap.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLatLng,
                            15f
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}