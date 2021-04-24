package com.communisolve.foodversy.ui

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.communisolve.foodversy.R
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.remote.IGoogleAPI
import com.communisolve.foodversy.remote.RetrofitClient
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.lang.StringBuilder

class TrackingOrderActivity : FragmentActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null

    private var shipperMarker: Marker? = null
    private var polylineOptions: PolylineOptions? = null
    private var blackPolylineOptions: PolylineOptions? = null
    private var redPolyline: Polyline? = null
    private var polylineList:List<LatLng> = ArrayList()

    private lateinit var iGoogleApi: IGoogleAPI
    private val compositeDisposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_order)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        iGoogleApi = RetrofitClient.instance!!.create(IGoogleAPI::class.java)
        mMap!!.uiSettings.isZoomControlsEnabled = true
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.uber_light_with_label
                )
            )
            if (!success) {

            }
        } catch (ex: Resources.NotFoundException) {

        }

        drawRoutes()
    }

    private fun drawRoutes() {
        val locationOrder = LatLng(
            Common.currentShippingOrder!!.orderModel!!.lat,
            Common.currentShippingOrder!!.orderModel!!.lng
        )

        val locationShipper = LatLng(
            Common.currentShippingOrder!!.currentLat,
            Common.currentShippingOrder!!.currentLng
        )

        //Addbox
        mMap!!.addMarker(
            MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.box))
                .title(Common.currentShippingOrder!!.orderModel!!.userName)
                .snippet(Common.currentShippingOrder!!.orderModel!!.shippingAddress)
                .position(locationOrder)
        )

        //AddShipper
        if (shipperMarker == null) {
            val height = 80
            val width=80
            val bitmapDrawable = ContextCompat.getDrawable(this,R.drawable.shippernew) as BitmapDrawable
            val resized = Bitmap.createScaledBitmap(bitmapDrawable.bitmap,width,height,false)

           shipperMarker = mMap!!.addMarker(
                MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resized))
                    .title(Common.currentShippingOrder!!.shipperName)
                    .snippet(Common.currentShippingOrder!!.shipperPhone)
                    .position(locationShipper)
            )

            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper,18.0f))
        }else{
            shipperMarker!!.position = locationShipper
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper,18.0f))
        }

        //Draw route

        val to = StringBuilder().append(Common.currentShippingOrder!!.orderModel!!.lat)
            .append(",")
            .append(Common.currentShippingOrder!!.orderModel!!.lng)
            .toString()

        val from = StringBuilder().append(Common.currentShippingOrder!!.currentLat)
            .append(",")
            .append(Common.currentShippingOrder!!.currentLng)
            .toString()

        compositeDisposable.add(
            iGoogleApi!!.getDirections(
                "driving", "less_driving",
                from, to, getString(R.string.google_maps_key)
            )!!
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    try {
                        val jsonObject = JSONObject(it)
                        val jsonArray = jsonObject.getJSONArray("routes")
                        for (i in 0 until jsonArray.length()) {
                            val route = jsonArray.getJSONObject(i)
                            val poly = route.getJSONObject("overview_polyline")
                            val polyline = poly.getString("points")
                            polylineList = Common.decodePoly(polyline)
                        }

                        polylineOptions = PolylineOptions()
                        polylineOptions!!.color(Color.RED)
                        polylineOptions!!.width(12.0f)
                        polylineOptions!!.startCap(SquareCap())
                        polylineOptions!!.endCap(SquareCap())
                        polylineOptions!!.jointType(JointType.ROUND)
                        polylineOptions!!.addAll(polylineList)

                        redPolyline = mMap!!.addPolyline(polylineOptions)

                    } catch (e: Exception) {
                        Log.d("DEBUG", "moveMarkerAnimation: ${e.message}")
                    }
                }, {
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                })
        )
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }
}