package com.example.myrestaurantshipperv2kotlin

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.example.myrestaurantshipperv2kotlin.common.Common
import com.example.myrestaurantshipperv2kotlin.common.LatLngInterpolator
import com.example.myrestaurantshipperv2kotlin.common.MarkerAnimation
import com.example.myrestaurantshipperv2kotlin.databinding.ActivityShippingBinding
import com.example.myrestaurantshipperv2kotlin.model.ShipperOrderModel
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import io.paperdb.Paper
import java.text.SimpleDateFormat

class ShippingActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityShippingBinding

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var shipperMarker: Marker? = null
    private var shipperOrderModel: ShipperOrderModel? = null

    var isInit = false
    var previousLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShippingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buildLocationRequest()
        buildLocationCallback()

        setShipperOrderModel()

        Dexter.withContext(this).withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        val mapFragment = supportFragmentManager
                                .findFragmentById(R.id.map) as SupportMapFragment
                        mapFragment.getMapAsync(this@ShippingActivity)

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@ShippingActivity)
                        if (ActivityCompat.checkSelfPermission(
                                        this@ShippingActivity,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                        this@ShippingActivity,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                                Looper.myLooper())
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        Toast.makeText(this@ShippingActivity, "You must allow this permission", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                            p0: PermissionRequest?,
                            p1: PermissionToken?
                    ) {
                        TODO("Not yet implemented")
                    }

                }).check()
    }

    private fun setShipperOrderModel() {
        Paper.init(this)
        val data = Paper.book().read<String>(Common.SHIPPING_DATA)
        if (!TextUtils.isEmpty(data)) {
            shipperOrderModel = Gson()
                    .fromJson<ShipperOrderModel>(data, object : TypeToken<ShipperOrderModel>() {}.type)
            if (shipperOrderModel != null) {
                Common.setSpanStringColor(
                        "Name:",
                        shipperOrderModel!!.orderModel!!.userName!!,
                        binding.txtName,
                        Color.parseColor("#333639")
                )
                Common.setSpanStringColor(
                        "Address:",
                        shipperOrderModel!!.orderModel!!.shippingAddress!!,
                        binding.txtAddress,
                        Color.parseColor("#673ab7")
                )
                Common.setSpanStringColor(
                        "Name:",
                        shipperOrderModel!!.orderModel!!.key!!,
                        binding.txtOrderNumber,
                        Color.parseColor("#795548")
                )

                binding.txtDate.text = StringBuilder().append(SimpleDateFormat("dd-MM-yyy HH:mm:ss").format(
                        shipperOrderModel!!.orderModel!!.createdDate
                ))

                Glide.with(this).load(shipperOrderModel!!.orderModel!!.cartItemList!![0].foodImage)
                        .into(binding.imgFoodImage)

            }
        } else {
            Toast.makeText(this, "Shipping Order Model is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                val locationShipper = LatLng(p0.lastLocation.latitude, p0.lastLocation.longitude)
                if (shipperMarker == null) {
                    val height = 80
                    val width = 80
                    val bitmapDrawable =
                            ContextCompat.getDrawable(this@ShippingActivity, R.drawable.shipper)
                    val b = bitmapDrawable!!.toBitmap()
                    val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
                    shipperMarker = mMap.addMarker(
                            MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                    .position(locationShipper)
                                    .title("You")
                    )
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 15f))
                } else {
                    shipperMarker!!.position = locationShipper
                }

                if (isInit && previousLocation != null){
                    val previousLocationLatLng = LatLng(previousLocation!!.latitude, previousLocation!!.longitude)
                    MarkerAnimation.animateMarkerToGB(shipperMarker!!, locationShipper, LatLngInterpolator.Spherical())
                    shipperMarker!!.rotation = Common.getBearing(previousLocationLatLng, locationShipper)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(locationShipper))

                    previousLocation = p0.lastLocation
                }
                if(!isInit){
                    isInit = true
                    previousLocation = p0.lastLocation
                }

            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 15000
        locationRequest.fastestInterval = 10000
        locationRequest.smallestDisplacement = 20f


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        try {
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,
                    R.raw.uber_light_with_label))
            if (!success)
                Log.d("PHAMTHUC", "Failed to load map style")
        } catch (ex: Resources.NotFoundException) {
            Log.d("PHAMTHUC", "Not found json string for map style")
        }

/*        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/
    }

    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}