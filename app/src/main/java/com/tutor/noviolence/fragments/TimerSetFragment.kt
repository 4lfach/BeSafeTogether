package com.tutor.noviolence.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.telephony.SmsManager
import android.view.Gravity
import android.view.View
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.tutor.noviolence.R
import com.tutor.noviolence.db.DatabaseHandler
import com.tutor.noviolence.models.MessageModel
import kotlinx.android.synthetic.main.fragment_timer.*


class TimerSetFragment : Fragment(R.layout.fragment_timer){
    private lateinit var myCurrentLocation: LatLng
    private lateinit var npHour : NumberPicker
    private lateinit var npMinute : NumberPicker
    private lateinit var npSecond : NumberPicker

    private lateinit var countDownTimer : CountDownTimer
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var duration : Long = 0
    private var initialTime:Long = 0

    private var hour : Int = 0
    private var minute : Int = 0
    private var second : Int = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        npHour = view.findViewById(R.id.npHour)
        npMinute = view.findViewById(R.id.npMinute)
        npSecond = view.findViewById(R.id.npSecond)

        setValuesForNumberPicker()

        npHour.setOnValueChangedListener{  _, _, newVal ->
            hour = newVal
        }
        npMinute.setOnValueChangedListener{  _, _, newVal ->
            minute = newVal
        }
        npSecond.setOnValueChangedListener{  _, _, newVal ->
            second = newVal
        }

        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS), 111)
        }


        btnStartTimer.setOnClickListener { view ->
            if(checkTimeDuration()){
                showAlertDialogBeforeTimer()
            }
            else
                Toast.makeText(requireContext(), "Timer duration must be more than 10 minutes.", Toast.LENGTH_SHORT).show()
        }

        btnUserSafe.setOnClickListener{
            showAlertDialogAfterTimer()
        }

        btnExtendTime.setOnClickListener{
            duration = initialTime
            countDownTimer.cancel()
            startTimer()
        }
        btnUserDanger.setOnClickListener{
            if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS), 111)
            }
            else{
                sendMessageToContacts()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    if (duration!=0L){
                        Toast.makeText(requireContext(), "Timer is still running", Toast.LENGTH_SHORT).show()
                        return
                    }
                    Toast.makeText(requireContext(), "Timer is still running", Toast.LENGTH_SHORT).show()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 111 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            sendMessageToContacts()
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendMessageToContacts(){
        val dbHandler = DatabaseHandler(requireContext())
        val contactList = dbHandler.getAllContacts()
        val msgM = MessageModel()
        if(checkPermissions()){
            mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                val location: Location? = task.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    myCurrentLocation = LatLng(location.latitude, location.longitude)
                    val msg = dbHandler.getMessageById().message + "\nYou can find me here: " + "http://www.google.com/maps/place/" + myCurrentLocation.latitude + "," + myCurrentLocation.longitude
                    for (cnt in contactList){
                        if(cnt.gpsIsOn) {
                            sendSMS(cnt.phone, msg)
                        }
                    }
                }
            }
        }

    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()!!
        )
    }

    // If current location could not be located, use last location
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            myCurrentLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
        }
    }

    private fun sendSMS(phone: String, message: String) {
        try {
            var sms = SmsManager.getDefault()
            sms.sendTextMessage(phone, "USER", message, null, null)
            val toast = Toast.makeText(requireContext(), "Message is sent", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
        }
        catch(e: Exception){
            e.printStackTrace()
            val toast = Toast.makeText(requireContext(), "Failed to send a message", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
        }
    }

    private fun checkTimeDuration(): Boolean {
        val duration = calculateDuration()
        if (duration < 600)
            return false
        return true
    }

    private fun showAlertDialogBeforeTimer() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Set timer?")

        val timerMsg = "$hour h. $minute m. $second s."
        builder.setMessage("If you won't press the button again in $timerMsg app will launch SOS signal!")
        builder.setIcon(R.drawable.ic_danger)

        builder.setPositiveButton("Accept"){dialogInterface, which ->
            rlPrepareTimer.visibility = View.GONE
            rlReadyTimer.visibility = View.VISIBLE
            startTimer()
        }

        builder.setNegativeButton("Cancel"){dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog : AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun showAlertDialogAfterTimer() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Are you safe?")

        builder.setMessage("If you feel safe press YES button")
        builder.setIcon(R.drawable.ic_danger)

        builder.setPositiveButton("Yes"){dialogInterface, which ->
            rlPrepareTimer.visibility = View.VISIBLE
            rlReadyTimer.visibility = View.GONE
            countDownTimer.cancel()
            finishTimer()
        }

        builder.setNegativeButton("No"){dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog : AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun setValuesForNumberPicker(){
        npHour.maxValue = 2
        npHour.minValue = 0
        npMinute.maxValue = 59
        npMinute.minValue = 0
        npSecond.maxValue = 59
        npSecond.minValue = 0
    }

    private fun calculateDuration(): Long{
        return (hour * 60 * 60 + minute* 60 + second).toLong()
    }

    private fun startTimer(){
        duration = calculateDuration()
        initialTime = duration

        countDownTimer = object : CountDownTimer(duration * 1000, 1000 ){
            override fun onTick(millisUntilFinished: Long) {
                duration = millisUntilFinished
                setTextTimer()
            }

            override fun onFinish() {
                Toast.makeText(requireContext(),"end timer", Toast.LENGTH_SHORT).show()
                sendMessageToContacts()
                finishTimer()
            }

        }.start()
    }

    private fun setTextTimer() {
        val h = ((duration / 1000) / 3600).toInt()
        val m = ((duration / 1000) /60 - h * 60).toInt()
        val s = (duration / 1000) % 60

        tvTimerHours.text = h.toString() + "h"
        tvTimerMinutes.text = m.toString() + "m"
        tvTimerSeconds.text = s.toString() + "s"
    }

    private fun finishTimer(){
        rlPrepareTimer.visibility = View.VISIBLE
        rlReadyTimer.visibility = View.GONE
    }



    private fun setNavigateFromTimer() {
        var toolbar = activity?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        /*toolbar?.setNavigationOnClickListener {
            findNavController().addOnDestinationChangedListener { controller, destination, arguments ->
                when(destination.id){
                    R.layout.fragment_timer -> {
                        if (duration == 0L){
                            findNavController().popBackStack()
                        } else{
                            Toast.makeText(requireContext(), "Turn off the timer by pressing I'm safe button", Toast.LENGTH_SHORT).show()
                        }
                    }
                    R.layout.fragment_home ->{
                        findNavController().popBackStack()
                    }
                }
            }
        }*/
    }
}