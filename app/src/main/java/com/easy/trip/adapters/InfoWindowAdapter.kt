package com.easy.trip.adapters

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.easy.trip.R
import com.easy.trip.models.Bookmark
import com.easy.trip.utilities.ImageUtils
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class InfoWindowAdapter(private val context: Activity) : GoogleMap.InfoWindowAdapter {

    private var contents: View

    init {
        contents = context.layoutInflater.inflate(R.layout.info_window_layout, null)
    }

    override fun getInfoContents(marker: Marker): View? {
        val bookmark = marker.tag as Bookmark
        val title = contents.findViewById<TextView>(R.id.txtPositionTitle)
        val snippet = contents.findViewById<TextView>(R.id.txtPositionSnippet)
        val image = contents.findViewById<ImageView>(R.id.imageView)
        title.text = bookmark.name
        snippet.text = bookmark.comment
        image.setImageBitmap(ImageUtils.loadBitmapFromFile(context, "${bookmark.placeId}.png"))
        return contents
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
}