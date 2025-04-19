package com.example.exploralocal

import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exploralocal.databinding.ActivityPlacesListBinding
import com.example.exploralocal.db.Place
import com.example.exploralocal.viewmodels.PlacesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlacesListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlacesListBinding
    private val viewModel: PlacesViewModel by viewModels()
    private lateinit var adapter: PlacesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSorting()
        observePlaces()
        viewModel.loadPlaces()
    }

    private fun setupRecyclerView() {
        adapter = PlacesAdapter { place, action ->
            when(action) {
                PlacesAction.EDIT -> showEditDialog(place)
                PlacesAction.DELETE -> showDeleteConfirmation(place.id)
            }
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlacesListActivity)
            adapter = this@PlacesListActivity.adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupSorting() {
        binding.sortByName.setOnClickListener {
            viewModel.loadPlaces(PlacesViewModel.SortType.NAME)
        }
        binding.sortByRating.setOnClickListener {
            viewModel.loadPlaces(PlacesViewModel.SortType.RATING)
        }
    }

    private fun observePlaces() {
        viewModel.places.observe(this) { places ->
            adapter.submitList(places)
            binding.emptyView.visibility = if (places.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showEditDialog(place: Place) {
        Log.d("EDIT_DIALOG", "Editando lugar con ID: ${place.id}")
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_place, null).apply {
            findViewById<EditText>(R.id.etPlaceName).setText(place.name)
            findViewById<EditText>(R.id.etPlaceDescription).setText(place.description)
            findViewById<EditText>(R.id.etPlaceRating).setText(place.rating.toString())
        }

        AlertDialog.Builder(this)
            .setTitle("Editar lugar")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val updatedPlace = place.copy(
                    name = dialogView.findViewById<EditText>(R.id.etPlaceName).text.toString(),
                    description = dialogView.findViewById<EditText>(R.id.etPlaceDescription).text.toString(),
                    rating = dialogView.findViewById<EditText>(R.id.etPlaceRating).text.toString().toFloatOrNull() ?: 0f
                )
                Log.d("UPDATE_FLOW", "Enviando actualización - ID: ${updatedPlace.id}")
                viewModel.updatePlace(updatedPlace)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmation(id: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar lugar")
            .setMessage("¿Estás seguro de que quieres eliminar este lugar?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deletePlace(id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}