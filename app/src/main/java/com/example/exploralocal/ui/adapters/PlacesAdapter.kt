package com.example.exploralocal.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.exploralocal.utils.PlacesAction
import com.example.exploralocal.databinding.ItemPlaceBinding
import com.example.exploralocal.db.Place

class PlacesAdapter(
    private val onActionClick: (Place, PlacesAction) -> Unit
) : ListAdapter<Place, PlacesAdapter.PlaceViewHolder>(DiffCallback()) {

    inner class PlaceViewHolder(private val binding: ItemPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(place: Place) {
            binding.apply {
                tvName.text = place.name
                tvDescription.text = place.description
                ratingBar.rating = place.rating

                btnEdit.setOnClickListener {
                    onActionClick(place, PlacesAction.EDIT)
                }
                btnDelete.setOnClickListener {
                    onActionClick(place, PlacesAction.DELETE)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Place, newItem: Place) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}