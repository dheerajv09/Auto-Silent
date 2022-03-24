package com.example.autosilent.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.autosilent.R
import com.example.autosilent.data.GeofenceEntity
import com.example.autosilent.databinding.GeofencesRowLayoutBinding
import com.example.autosilent.ui.show_places.ShowPlacesFragmentDirections
import com.example.autosilent.util.MyDiffUtil
import com.example.autosilent.viewmodels.GeofenceViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GeofencesAdapter(private val geofenceViewModel: GeofenceViewModel) :
    RecyclerView.Adapter<GeofencesAdapter.MyViewHolder>() {

    private var geofenceEntity = mutableListOf<GeofenceEntity>()

    class MyViewHolder(val binding: GeofencesRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(geofenceEntity: GeofenceEntity) {
            binding.geofencesEntity = geofenceEntity
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GeofencesRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentGeofence = geofenceEntity[position]
        holder.bind(currentGeofence)

        holder.binding.deleteImageView.setOnClickListener {
            removeItem(holder, position)
        }

        holder.binding.snapshotImageView.setOnClickListener {
            val action = ShowPlacesFragmentDirections.actionShowPlacesFragmentToMapsFragment(currentGeofence)//AddPlacesFragmentDirections.actionAddPlacesFragmentToMapsFragment(currentGeofence)
            holder.itemView.findNavController().navigate(action)
        }
    }

    private fun removeItem(holder: MyViewHolder, position: Int) {
        geofenceViewModel.viewModelScope.launch {
            val geofenceStopped =
                geofenceViewModel.stopGeofence(listOf(geofenceEntity[position].geoId))
            if (geofenceStopped) {
                geofenceViewModel.removeGeofence(geofenceEntity[position])
                showSnackBar(holder, geofenceEntity[position])
            } else {
                Log.d("GeofencesAdapter", "Geofence NOT REMOVED!")
            }
        }
    }

    private fun showSnackBar(
        holder: MyViewHolder,
        removedItem: GeofenceEntity
    ) {
        Snackbar.make(
            holder.itemView,
            "Removed " + removedItem.name,
            Snackbar.LENGTH_LONG
        ).setAction("UNDO") {
            undoRemoval(holder, removedItem)
        }.show()
    }

    private fun undoRemoval(holder: MyViewHolder, removedItem: GeofenceEntity) {
        holder.binding.motionLayout.transitionToState(R.id.start)
        geofenceViewModel.addGeofence(removedItem)
        geofenceViewModel.startGeofence(
            removedItem.latitude,
            removedItem.longitude
        )
    }

    override fun getItemCount(): Int {
        return geofenceEntity.size
    }

    fun setData(newGeofenceEntity: MutableList<GeofenceEntity>) {
        val geofenceDiffUtil = MyDiffUtil(geofenceEntity, newGeofenceEntity)
        val diffUtilResult = DiffUtil.calculateDiff(geofenceDiffUtil)
        geofenceEntity = newGeofenceEntity
        diffUtilResult.dispatchUpdatesTo(this)
    }
}










