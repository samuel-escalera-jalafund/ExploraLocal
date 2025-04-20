package com.example.exploralocal.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.exploralocal.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.exploralocal.databinding.ActivityMapsBinding
import com.example.exploralocal.db.Place
import com.example.exploralocal.ui.viewmodels.PlacesViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val viewModel: PlacesViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var photoUri: Uri? = null

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

        mMap.setOnMarkerClickListener { marker ->
            val place = viewModel.places.value?.find { place ->
                marker.position == LatLng(place.latitude, place.longitude)
            }

            place?.let {
                showPlaceInfoDialog(it)
            }

            true
        }

        viewModel.places.observe(this) { places ->
            mMap.clear()
            places.forEach { place ->
                val location = LatLng(place.latitude, place.longitude)
                mMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(place.name)
                        .snippet("Rating: ${place.rating}")
                )
            }
        }

        checkLocationPermission()
        viewModel.loadPlaces()
    }

    private fun showPlaceInfoDialog(place: Place) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_place_info, null)
        val builder = AlertDialog.Builder(this)
            .setTitle(place.name)
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)

        val descriptionText = dialogView.findViewById<TextView>(R.id.tvDescription)
        val ratingText = dialogView.findViewById<TextView>(R.id.tvRating)
        val placeImage = dialogView.findViewById<ImageView>(R.id.ivPlaceImage)
        val shareButton = dialogView.findViewById<Button>(R.id.btnShare)

        descriptionText.text = place.description
        ratingText.text = "Rating: ${place.rating}"

        shareButton.setOnClickListener {
            sharePlace(place)
        }

        place.photoPath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(path)
                    placeImage.setImageBitmap(bitmap)
                    placeImage.visibility = View.VISIBLE
                } else {
                    placeImage.visibility = View.GONE
                }
            } catch (e: Exception) {
                placeImage.visibility = View.GONE
                Log.e("PLACE_INFO", "Error loading image", e)
            }
        } ?: run {
            placeImage.visibility = View.GONE
        }

        builder.show()
    }

    private fun sharePlace(place: Place) {
        val shareText = buildString {
            append("Nombre: ${place.name}\n")
            append("Descripción: ${place.description}\n")
            append("Rating: ${place.rating}\n")
            append("Ubicación: https://maps.google.com/?q=${place.latitude},${place.longitude}")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"

            place.photoPath?.let { path ->
                val photoUri = FileProvider.getUriForFile(
                    this@MapsActivity,
                    "${applicationContext.packageName}.fileprovider",
                    File(path)
                )
                putExtra(Intent.EXTRA_STREAM, photoUri)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Compartir lugar"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error al compartir: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("SHARE", "Error sharing place", e)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaces()
    }

    private fun showAddPlaceDialog(latLng: LatLng) {
        val dialogView = layoutInflater.inflate((R.layout.dialog_add_place), null)
        val nameInput = dialogView.findViewById<EditText>(R.id.etPlaceName)
        val descInput = dialogView.findViewById<EditText>(R.id.etPlaceDescription)
        val ratingInput = dialogView.findViewById<EditText>(R.id.etPlaceRating)
        val takePhotoButton = dialogView.findViewById<Button>(R.id.btnTakePhoto)

        takePhotoButton.setOnClickListener {
            photoUri = null
            checkCameraPermissionAndTakePhoto()
        }

        AlertDialog.Builder(this)
            .setTitle("Agregar nuevo lugar")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = nameInput.text.toString()
                val description = descInput.text.toString()
                val rating = ratingInput.text.toString().toFloatOrNull() ?: 0f

                if (name.isNotBlank()) {
                    val photoPath = photoUri?.let { uri ->
                        getRealPathFromUri(uri) ?: run {
                            Log.d("PHOTO_PATH", "No se pudo obtener path desde: $uri")
                            null
                        }
                    }

                    Log.d("PHOTO_DEBUG", "Guardando lugar con photoPath: $photoPath")

                    val newPlace = Place(
                        name = name,
                        description = description,
                        rating = rating,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        photoPath = photoPath
                    )
                    viewModel.addPlace(newPlace)
                    photoUri = null
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        return try {

            val fileName = uri.lastPathSegment?.substringAfterLast("/")
            fileName?.let {
                val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), it)
                if (file.exists()) {
                    file.absolutePath
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("URI_CONVERSION", "Error getting real path from URI", e)
            null
        }
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

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePhoto()
        } else {
            Toast.makeText(this, "Se necesita permiso de la camara", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Toast.makeText(this, "Foto tomada correctamente", Toast.LENGTH_SHORT).show()
        } else {
            photoUri = null
            Toast.makeText(this, "Error al tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCameraPermissionAndTakePhoto() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePhoto()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun takePhoto() {
        val photoFile = createImageFile()
        val currentPhotoUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )

        photoUri = currentPhotoUri

        takePictureLauncher.launch(currentPhotoUri)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
}