package com.tutor.noviolence

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.widget.ToolbarWidgetWrapper
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_timer.*

class TimerSetFragment : Fragment(R.layout.fragment_timer){
    private lateinit var npHour : NumberPicker
    private lateinit var npMinute : NumberPicker
    private lateinit var npSecond : NumberPicker

    private lateinit var countDownTimer : CountDownTimer

    private var duration : Long = 0
    private var initialTime:Long = 0

    private var hour : Int = 0
    private var minute : Int = 0
    private var second : Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        setNavigateFromTimer()

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
        npHour.setMaxValue(2)
        npHour.setMinValue(0)
        npMinute.setMaxValue(59)
        npMinute.setMinValue(0)
        npSecond.setMaxValue(59)
        npSecond.setMinValue(0)
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