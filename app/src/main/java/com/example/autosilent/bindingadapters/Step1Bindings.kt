package com.example.autosilent.bindingadapters

import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import com.example.autosilent.R
import com.example.autosilent.viewmodels.GeofenceViewModel
import com.example.autosilent.viewmodels.Step1ViewModel
import com.google.android.material.textfield.TextInputEditText

@BindingAdapter("updateGeofenceName", "enableNextButton", requireAll = true)
fun TextInputEditText.onTextChanged(
    geofenceViewModel: GeofenceViewModel,
    step1ViewModel: Step1ViewModel
) {
    this.setText(geofenceViewModel.geoName)
    Log.d("Bindings", geofenceViewModel.geoName)
    this.doOnTextChanged { text, _, _, _ ->
        if (text.isNullOrEmpty()) {
            step1ViewModel.enableNextButton(false)
        } else {
            step1ViewModel.enableNextButton(true)
        }
        geofenceViewModel.geoName = text.toString()
        Log.d("Bindings", geofenceViewModel.geoName)
    }
}

@BindingAdapter("nextButtonEnabled", "saveGeofenceId", requireAll = true)
fun TextView.step1NextClicked(nextButtonEnabled: Boolean, geofenceViewModel: GeofenceViewModel) {
    this.setOnClickListener {
        if (nextButtonEnabled) {
            geofenceViewModel.geoId = System.currentTimeMillis()
            this.findNavController().navigate(R.id.action_addPlacesFragment_to_mapsFragment)
        }
    }
}

@BindingAdapter("setProgressVisibility")
fun ProgressBar.setProgressVisibility(nextButtonEnabled: Boolean) {
    if (nextButtonEnabled) {
        this.visibility = View.GONE
    } else {
        this.visibility = View.VISIBLE
    }
}







