package com.easy.trip.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.amitshekhar.DebugDB

import com.easy.trip.R
import com.easy.trip.adapters.InfoWindowAdapter
import com.easy.trip.databases.viewmodels.BookmarkVM
import com.easy.trip.models.Bookmark
import com.easy.trip.utilities.ImageUtils

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.Place

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*
import java.util.Arrays.asList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var locationManager: LocationManager               // 當網路或GPS都正常運作時，user當前位置用這個來擷取
    private lateinit var unknownLocation: FusedLocationProviderClient   // 當網路或GPS都不能運作時，user當前位置用這個來擷取
    private lateinit var placesClient: PlacesClient
    private lateinit var bookmarkVM: BookmarkVM

    // 這裡是設定取得Place更多資訊時要撈回那些欄位
    // 撈取的欄位越多，Google API被收取的費用就越多
    private val capturePlaceFields = asList(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.ADDRESS,
        Place.Field.PHONE_NUMBER,
        Place.Field.PHOTO_METADATAS,
        Place.Field.LAT_LNG
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // 開啟SQLite偵錯工具
        // 來源：https://github.com/amitshekhariitbhu/Android-Debug-Database
        DebugDB.getAddressLog()

        // 獲取SupportMapFragment並在map已經就緒且可被使用後接收通知.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        unknownLocation = LocationServices.getFusedLocationProviderClient(this)

        // Google place API & send request client設定
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)

        // 當右下角回到當前位置按鈕被點擊時把鏡頭移動到使用者當前位置
        fab.setOnClickListener { view ->
            moveCameraToCurrentLocation(true)
        }

        // 把資料庫裡的資料畫成marker放到map上
        bookmarkVM = ViewModelProviders.of(this).get(BookmarkVM::class.java)
        bookmarkVM.listBookmarks()?.observe(this, androidx.lifecycle.Observer {
            map.clear()
            for(bookmark in it){
                addMarkerOnMap(bookmark)
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // 建立地圖
        map = googleMap

        // 調整地圖UI設定
        map.uiSettings.apply {
            isMapToolbarEnabled = false
            isMyLocationButtonEnabled = false
        }

        // 把鏡頭移動到使用者當前位置
        moveCameraToCurrentLocation(false)

        // 修改InfoWindow排版 & InfoWindow點擊時的動作
        map.setInfoWindowAdapter(InfoWindowAdapter(this))
        map.setOnInfoWindowClickListener { it: Marker ->
            val bookmark = it.tag as Bookmark
            val image = ImageUtils.loadBitmapFromFile(applicationContext, "${bookmark.placeId}.png")
            openForm(bookmark, image!!,false)
        }

        // 當地圖上POI被點取時，取得POI的基本資訊
        map.setOnPoiClickListener { it:PointOfInterest ->
            val bookmark = bookmarkVM.findBookmarkByPlace(it.placeId)
            // 如果資料庫中有紀錄，則擷取資料庫的資料
            if(bookmark != null) {
                val image = ImageUtils.loadBitmapFromFile(applicationContext, "${bookmark.placeId}.png")
                openForm(bookmark, image!!, false)
            }
            // 如果沒有，就向Google Place API擷取資料（要注意網路是否有通）
            else {
                // 取得Place的詳細資料
                val request = FetchPlaceRequest.builder(it.placeId, capturePlaceFields).build()
                placesClient
                    .fetchPlace(request)
                    .addOnSuccessListener { placeResponse ->
                        val place = placeResponse.place
                        if(!place.photoMetadatas.isNullOrEmpty()) {
                            place.photoMetadatas!![0]?.apply {
                                // 取得Place的照片
                                val requestPhoto = FetchPhotoRequest.builder(this).setMaxHeight(270).setMaxWidth(480).build()
                                placesClient.fetchPhoto(requestPhoto).addOnSuccessListener { photoResponse ->
                                    val bookmark = Bookmark(
                                        id = 0,
                                        placeId = place.id,
                                        name = place.name,
                                        address = place.address,
                                        phone = place.phoneNumber,
                                        latitude = place.latLng!!.latitude,
                                        longitude = place.latLng!!.longitude,
                                        comment = ""
                                    )
                                    openForm(bookmark, photoResponse.bitmap, true)
                                }.addOnFailureListener { e ->
                                    Log.e("com.easy.trip.activities.MapsActivity", "placesClient.fetchPhoto.failed: ${e.message}")
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("com.easy.trip.activities.MapsActivity", "placesClient.fetchPlace.failed: ${e.message}")
                    }
            }
        }
    }

    /**
     * 將Bookmark畫到map上
     */
    private fun addMarkerOnMap(bookmark: Bookmark) {
        val position = LatLng(bookmark.latitude, bookmark.longitude)
        val marker = map.addMarker(MarkerOptions()
            .position(position)
            .title(bookmark.name)
            .snippet(bookmark.comment)
        )
        marker.tag = bookmark
    }

    /**
     * 打開FormActivity
     */
    private fun openForm(bookmark: Bookmark, image: Bitmap, isCreateMode: Boolean) {
        val intent = Intent(this, FormActivity::class.java)
        val bundle = Bundle()
        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        bundle.putBoolean("isCreateMode", isCreateMode)
        bundle.putParcelable("bookmark", bookmark)
        bundle.putByteArray("image", byteArray)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    /**
     * 將鏡頭移動到當前位置，並決定是否用動畫移動的方式移動
     */
    private fun moveCameraToCurrentLocation(isAnimation: Boolean) {
        try {
            val location:Location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val position = LatLng(location.latitude, location.longitude)
            when (isAnimation) {
                true -> map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16.0f))
                false -> map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16.0f))
            }
        }
        catch(ex: SecurityException){
            unknownLocation.lastLocation.addOnCompleteListener{
                if(it.result != null) {
                    val position = LatLng(it.result!!.latitude, it.result!!.longitude)
                    when (isAnimation) {
                        true -> map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16.0f))
                        false -> map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16.0f))
                    }
                }
            }
        }
    }
}
