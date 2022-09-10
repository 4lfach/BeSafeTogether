package com.tutor.noviolence.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.tutor.noviolence.R
import com.tutor.noviolence.db.DatabaseHandler
import com.tutor.noviolence.models.ContactModel
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(R.layout.fragment_home), PermissionListener {
    private lateinit var locationManager: LocationManager
    private lateinit var dbHandler: DatabaseHandler

    private var safetyProgress : Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createDbHandler()

        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        btnToTimer.setOnClickListener{
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && dbHandler.getAllContacts().size >=1){
                val action = HomeFragmentDirections.actionHomeFragmentToTimerFragment()
                findNavController().navigate(action)
            } else if (dbHandler.getAllContacts().size <1){
                Toast.makeText(requireContext(), "Please add new contact to set timer",Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(requireContext(), "Please turn on the GPS to proceed",Toast.LENGTH_SHORT).show()
            }
        }

        setTicksToCheckBox()
        setSafetyProgress()
    }

    private fun setTicksToCheckBox() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            cbGps.isChecked = true
            safetyProgress++
        }
        val contacts : ArrayList<ContactModel> = dbHandler.getAllContacts()
        if (contacts.size >= 1){
            cbContacts.isChecked = true
            safetyProgress++
        }
        for (cnt in contacts){
            if (cnt.gpsIsOn == true){
                cbGetGps.isChecked = true
                safetyProgress++
            }
        }

        if (dbHandler.getMessageById() != null && dbHandler.getMessageById().message != ""){
            cbDefaultMessage.isChecked = true
            safetyProgress++
        }
    }

    private fun setSafetyProgress(){
        if (cbContacts.isChecked == false){
            tvDetailProgress.text = "Contacts are not set"
            tvMainProgress.text = "You are not safe"
            return
        }
        when(safetyProgress){
            0 ->{
                tvDetailProgress.text = "Please finish instructions below to stay safe"
                tvMainProgress.text = "You are not safe"
            }
            1 ->{
                tvDetailProgress.text = "You are only 25% safe"
                tvMainProgress.text = "You are not safe"
            }
            2 ->{
                tvDetailProgress.text = "You are only 50% safe"
                tvMainProgress.text = "You are not safe"
            }
            3 ->{
                tvDetailProgress.text = "You are only 75% safe"
                tvMainProgress.text = "You are not safe"
            }
            4 ->{
                tvDetailProgress.text = "You are 100% safe"
                tvMainProgress.text = "You are safe"
            }
        }
    }

    private fun createDbHandler(){
        dbHandler = DatabaseHandler(requireContext())
    }

    private fun requestPermissionForGPS() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(this)
            .check()
    }

    override fun onPermissionGranted(permissionRequest: PermissionGrantedResponse?) {

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
}