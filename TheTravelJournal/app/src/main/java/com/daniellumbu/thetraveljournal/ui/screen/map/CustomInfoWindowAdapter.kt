package com.daniellumbu.thetraveljournal.ui.screen.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.daniellumbu.thetraveljournal.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        // Return null to use the default info window background
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        // Inflate the custom info window layout
        val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)

        // Bind data to the views
        val titleTextView = view.findViewById<TextView>(R.id.info_window_title)
        val snippetTextView = view.findViewById<TextView>(R.id.info_window_snippet)
        val imageView = view.findViewById<ImageView>(R.id.info_window_image)

        // Set the title and snippet from the marker
        titleTextView.text = marker.title
        snippetTextView.text = marker.snippet

        // Set an image (replace with actual image logic)
        imageView.setImageResource(R.drawable.img) // Default placeholder image

        // Optionally, load an image dynamically (e.g., using Glide or Picasso)
        // Glide.with(context).load("https://example.com/image.jpg").into(imageView)

        return view
    }
}
