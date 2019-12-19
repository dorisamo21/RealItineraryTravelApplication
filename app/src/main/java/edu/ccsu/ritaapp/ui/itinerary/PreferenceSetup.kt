package edu.ccsu.ritaapp.ui.itinerary

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.util.Log.d
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import edu.ccsu.ritaapp.R
import java.util.*


class PreferenceSetup (fContext: Context, fActivity: FragmentActivity, fRoot: View){
    private val activity: FragmentActivity = fActivity
    private val context: Context = fContext
    private val c = Calendar.getInstance()
    private val curYear = c.get(Calendar.YEAR).toString()
    private val curMonth = c.get(Calendar.MONTH).toString()
    private val curDay = c.get(Calendar.DAY_OF_MONTH).toString()

    private val root = fRoot

    fun getPrefParamString() : String {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        return sharedPref.getString("PreferenceString", "1111111111111111")!!
    }

    fun getStartDay() : String {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        with (sharedPref) {
            val year = getString("Start_Day_Year", curYear)!!
            var month = (Integer.parseInt(getString("Start_Day_Month", curMonth)!!)+1).toString()
            if(month.length == 1){
                month = "0$month"
            }
            var day = getString("Start_Day_Day", curDay)!!
            if(day.length == 1){
                day = "0$day"
            }
            return "$year-$month-$day"
        }
    }

    fun getEndDay() : String {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        with (sharedPref) {
            val year = getString("End_Day_Year", curYear)!!
            var month = (Integer.parseInt(getString("End_Day_Month", curMonth)!!)+1).toString()
            if(month.length == 1){
                month = "0$month"
            }
            var day = getString("End_Day_Day", curDay)!!
            if(day.length == 1){
                day = "0$day"
            }
            return "$year-$month-$day"
        }
    }

    fun getStartTime() : String {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        with (sharedPref) {
            var hour = getString("Start_Time_Hour", Calendar.HOUR.toString())!!
            if(hour == "0"){
                hour = "24"
            }
            if(hour.length == 1){
                hour = "0$hour"
            }
            var minute = getString("Start_Time_Minute", Calendar.MINUTE.toString())!!
            if(minute.length == 1){
                minute = "0$minute"
            }
            return "$hour:$minute"
        }
    }

    fun getEndTime() : String {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        with (sharedPref) {
            var hour = getString("End_Time_Hour", Calendar.HOUR.toString())!!
            if(hour == "0"){
                hour = "24"
            }
            if(hour.length == 1){
                hour = "0$hour"
            }
            var minute = getString("End_Time_Minute", Calendar.MINUTE.toString())!!
            if(minute.length == 1){
                minute = "0$minute"
            }
            return "$hour:$minute"
        }
    }

    fun setUpPreferences(){
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return


        var editText = root.findViewById<TextView>(R.id.Start_Day)
        var day = sharedPref.getString(editText.tag.toString()+ "_Year", curYear)!!
        day += "/" +  sharedPref.getString(editText.tag.toString()+ "_Month", curMonth)
        day += "/" +  sharedPref.getString(editText.tag.toString() + "_Day", curDay)
        editText.text = day

        editText = root.findViewById(R.id.End_Day)
        day = sharedPref.getString(editText.tag.toString() + "_Year", curYear)!!
        day += "/" +  sharedPref.getString(editText.tag.toString() + "_Month", curMonth)
        day += "/" +  sharedPref.getString(editText.tag.toString() + "_Day", curDay)
        editText.text = day

        editText = root.findViewById(R.id.Start_Time)
        var time = ""
        var hour = sharedPref.getString(editText.tag.toString() + "_Hour", "12")!!.toInt()
        var minute = sharedPref.getString(editText.tag.toString() + "_Minute", "00")
        if (minute!!.length == 1){minute = "0$minute"
        }
        time += when (hour) {
            in 13..23 -> (hour - 12).toString() + ":" + minute + " pm"
            0 -> "12:$minute am"
            12 -> "12:$minute pm"
            else -> "$hour:$minute am"
        }
        editText.text = time

        editText = root.findViewById(R.id.End_Time)
        time = ""
        hour = sharedPref.getString(editText.tag.toString() + "_Hour", "24")!!.toInt()
        minute = sharedPref.getString(editText.tag.toString() + "_Minute", "00")
        if (minute!!.length == 1){minute = "0$minute"
        }
        time += when (hour) {
            in 13..23 -> (hour - 12).toString() + ":" + minute + " pm"
            0 -> "12:$minute am"
            12 -> "12:$minute pm"
            else -> "$hour:$minute am"
        }
        editText.text = time


        var rg = root.findViewById<RadioGroup>(R.id.RG_museums)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_tours)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))
        d("checked", sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId).toString())

        rg = root.findViewById(R.id.RG_markets)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_family)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_landmarks)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_community)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_sports)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_shopping)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_food_n_drink)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_concerts_festivals)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_music_performances)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_theaters_shows_expos)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_nightlife_entertainment)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_natural_geographical)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_accommodation)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = root.findViewById(R.id.RG_outdoor_parks_zoos)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        setListeners(root)
    }

    private fun setListeners(root: View){

        var editText = root.findViewById<TextView>(R.id.Start_Day)
        editText.setOnClickListener{view -> onDatePickerClicked(view)}

        editText = root.findViewById(R.id.End_Day)
        editText.setOnClickListener{view -> onDatePickerClicked(view)}

        editText = root.findViewById(R.id.Start_Time)
        editText.setOnClickListener{view -> onTimePickerClicked(view)}

        editText = root.findViewById(R.id.End_Time)
        editText.setOnClickListener{view -> onTimePickerClicked(view)}

        var rg = root.findViewById<RadioGroup>(R.id.RG_museums)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_tours)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_markets)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_family)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_landmarks)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_community)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_sports)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_shopping)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_food_n_drink)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_concerts_festivals)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_music_performances)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_theaters_shows_expos)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_nightlife_entertainment)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_natural_geographical)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_accommodation)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_outdoor_parks_zoos)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}
    }

    private fun onRadioGroupClicked(view: View, i: Int){
        val rg = view as RadioGroup
        val rb0 = rg.getChildAt(1)
        val rb1 = rg.getChildAt(2)
        val rb2 = rg.getChildAt(3)
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putInt(view.tag.toString(), rg.checkedRadioButtonId)

            var prefString = sharedPref.getString("PreferenceString", "1111111111111111")!!
            val position = rg.tag.toString().substring(0..1).toInt()
            when (i.toString()) {
                rb0.id.toString() -> prefString = prefString.replaceRange(position..position, "0")
                rb1.id.toString() -> prefString = prefString.replaceRange(position..position, "1")
                rb2.id.toString() -> prefString = prefString.replaceRange(position..position, "2")
            }
            putString("PreferenceString", prefString)
            commit()
        }
    }

    private fun onDatePickerClicked(view: View){
        val editText = view as TextView
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)

        val year = sharedPref.getString(view.tag.toString() + "_Year", curYear)!!.toInt()
        val month = sharedPref.getString(view.tag.toString() + "_Month", curMonth)!!.toInt()
        val day = sharedPref.getString(view.tag.toString() + "_Day", curDay)!!.toInt()

        val dpd = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, dpdYear, dpdMonth, dpdDay ->
            val m = dpdMonth+1
            val date = "$dpdYear/$m/$dpdDay"
            editText.text = date
            with (sharedPref.edit()) {
                putString(view.tag.toString() + "_Year", dpdYear.toString())
                putString(view.tag.toString() + "_Month", dpdMonth.toString())
                putString(view.tag.toString() + "_Day", dpdDay.toString())
                commit()
            }
        }, year, month, day)

        dpd.show()

    }

    private fun onTimePickerClicked(view: View){

        val editText = view as TextView
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)

        val hour = sharedPref.getString(view.tag.toString() + "_Hour", "12")!!.toInt()
        val minute = sharedPref.getString(view.tag.toString() + "_Minute", "0")!!.toInt()

        val tpd = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { timePicker, tpdHour, tpdMinute ->
            with (sharedPref.edit()) {
                putString(view.tag.toString() + "_Hour", tpdHour.toString())
                putString(view.tag.toString() + "_Minute", tpdMinute.toString())
                commit()
            }

            var dayOrNight = "am"
            var pickedHour = tpdHour
            when {
                timePicker.hour in 13..23 -> {
                    dayOrNight = "pm"
                    pickedHour -= 12
                }
                timePicker.hour == 12 -> {
                    dayOrNight = "pm"
                    pickedHour = 12
                }
                timePicker.hour == 0 -> {
                    dayOrNight = "am"
                    pickedHour = 12
                }
            }
            var pickedMinute = tpdMinute.toString()
            if (pickedMinute.length == 1) {pickedMinute = "0$pickedMinute"}

            val time = "$pickedHour:$pickedMinute $dayOrNight"
            editText.text = time
        }, hour, minute, false)

        tpd.show()
    }
}

