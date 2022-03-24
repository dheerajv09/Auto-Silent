package com.example.autosilent.bindingadapters

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.autosilent.R
import com.example.autosilent.viewmodels.GeofenceViewModel
import com.google.android.material.slider.Slider

@BindingAdapter("updateSliderValueTextView", "getGeoRadius", requireAll = true)
fun Slider.updateSliderValue(textView: TextView, geofenceViewModel: GeofenceViewModel) {
    updateSliderValueTextView(geofenceViewModel.geoRadius, textView)
    this.addOnChangeListener { _, value, _ ->
        geofenceViewModel.geoRadius = value
        updateSliderValueTextView(geofenceViewModel.geoRadius, textView)
    }
}

fun Slider.updateSliderValueTextView(geoRadius: Float, textView: TextView) {
    val kilometers = geoRadius / 1000
    if (geoRadius >= 1000f) {
        textView.text = context.getString(R.string.display_kilometers, kilometers.toString())
    } else {
        textView.text = context.getString(R.string.display_meters, geoRadius.toString())
    }
    this.value = geoRadius
}