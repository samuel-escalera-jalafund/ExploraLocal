package com.example.exploralocal.viewmodels

import android.util.Log
import com.example.exploralocal.db.Place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.exploralocal.repositories.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val repository: PlaceRepository
) : ViewModel() {

    private val _places = MutableLiveData<List<Place>>()
    val places: LiveData<List<Place>> = _places

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    fun loadPlaces(sortBy: SortType = SortType.NAME) {
        viewModelScope.launch {
            try {
                when (sortBy) {
                    SortType.NAME -> repository.getPlacesByName().collect { _places.value = it }
                    SortType.RATING -> repository.getPlacesByRating().collect { _places.value = it }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar lugares: ${e.message}"
            }
        }
    }

    fun addPlace(place: Place) {
        viewModelScope.launch {
            try {
                repository.insertPlace(place)
                _successMessage.value = "Lugar agregado correctamente"
                loadPlaces()
            } catch (e: Exception) {
                _errorMessage.value = "Error al agregar lugar: ${e.message}"
            }
        }
    }

    fun updatePlace(place: Place) {
        viewModelScope.launch {
            try {
                Log.d("PLACES_DEBUG", "Iniciando actualización para: ${place.id}")
                val rowsUpdated = repository.updatePlace(place)
                Log.d("PLACES_DEBUG", "Filas actualizadas: $rowsUpdated")

                _successMessage.value = "Lugar actualizado"
                loadPlaces()
            } catch (e: Exception) {
                Log.e("PLACES_ERROR", "Error al actualizar: ${e.message}", e)
                _errorMessage.value = "Error al actualizar: ${e.message}"
            }
        }
    }

    fun deletePlace(id: Int) {
        viewModelScope.launch {
            try {
                repository.deletePlace(id)
                _successMessage.value = "Lugar eliminado"
                loadPlaces()
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar: ${e.message}"
            }
        }
    }

    fun searchPlaces(query: String) {
        viewModelScope.launch {
            try {
                repository.searchPlaces(query).collect { _places.value = it }
            } catch (e: Exception) {
                _errorMessage.value = "Error en búsqueda: ${e.message}"
            }
        }
    }

    enum class SortType { NAME, RATING }
}