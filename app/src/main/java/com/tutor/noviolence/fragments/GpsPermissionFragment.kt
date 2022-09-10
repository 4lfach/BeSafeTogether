package com.tutor.noviolence.fragments

import android.app.AlertDialog
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.tutor.noviolence.R
import kotlinx.android.synthetic.main.fragment_gps_permission.*


class GpsPermissionFragment :PermissionListener, Fragment (R.layout.fragment_gps_permission) {
    lateinit var mLocationManager : LocationManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLocationManager = requireContext().getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager

        if(checkGpsPermission()){
            val action = GpsPermissionFragmentDirections.actionGpsPermissionFragmentToHomeFragment()
            findNavController().navigate(action)
        }

        btnRequestGps.setOnClickListener {
            if (!checkGpsPermission()){
                Toast.makeText(requireContext(), "Turn on the GPS", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            } else{
                val action = GpsPermissionFragmentDirections.actionGpsPermissionFragmentToHomeFragment()
                findNavController().navigate(action)
            }

            /*Dexter.withContext(requireContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(this)
                .check()*/
        }
    }

    override fun onPermissionGranted(permissionRequest: PermissionGrantedResponse?) {
        val action = GpsPermissionFragmentDirections.actionGpsPermissionFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse) {
        if (response.isPermanentlyDenied) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Permission Denied")
                .setMessage("Permission to access device location is permanently denied. you need to go to setting to allow the permission.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK") { dialog, which ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.fromParts("package", requireActivity().packageName, null)
                }
                .show()
        } else {
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPermissionRationaleShouldBeShown(permissionRequest: PermissionRequest?, token: PermissionToken?) {
        token!!.continuePermissionRequest()
    }

    private fun checkGpsPermission(): Boolean {
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true
        }
        return false
    }
}