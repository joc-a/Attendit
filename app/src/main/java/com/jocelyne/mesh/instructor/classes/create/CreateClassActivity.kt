package com.jocelyne.mesh.instructor.classes.create

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jocelyne.mesh.R
import com.jocelyne.mesh.instructor.classes.Class
import kotlinx.android.synthetic.main.activity_create_class.*


class CreateClassActivity : AppCompatActivity() {

    private val TAG = "CreateClassActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_class)

        start_time_picker.setIs24HourView(false)
        end_time_picker.setIs24HourView(false)

        create_btn.setOnClickListener {
            checkFields()
        }
    }

    private fun checkFields() {
        // Reset errors
        prefix_tv.error = null
        number_tv.error = null
        name_tv.error = null
        crn_et.error = null

        // Store values
        val prefix = prefix_tv.text.toString()
        val number = number_tv.text.toString()
        val name = name_tv.text.toString()
        val crn = crn_et.text.toString()
        var hour: Int
        var minute: Int
        var ending = "am"
        val startTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = start_time_picker.hour
            minute = start_time_picker.minute
            if (hour > 12) {
                hour -= 12
                ending = "pm"
            }
            hour.toString() + ":" + (if (minute == 0) "00" else minute.toString()) + " " + ending
        } else {
            hour = start_time_picker.currentHour
            minute = start_time_picker.currentMinute
            if (hour > 12) {
                hour -= 12
                ending = "pm"
            }
            hour.toString() + ":" + (if (minute == 0) "00" else minute.toString()) + " " + ending
        }
        ending = "am"
        val endTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = end_time_picker.hour
            minute = end_time_picker.minute
            if (hour > 12) {
                hour -= 12
                ending = "pm"
            }
            hour.toString() + ":" + (if (minute == 0) "00" else minute.toString()) + " " + ending
        } else {
            hour = end_time_picker.currentHour
            minute = end_time_picker.currentMinute
            if (hour > 12) {
                hour -= 12
            }
            hour.toString() + ":" + (if (minute == 0) "00" else minute.toString()) + " " + ending
        }

        val dayslist = weekdays_picker.selectedDays
        val daysOfTheWeek = weekdaysInitials(dayslist)

        var cancel = false
        var focusView: View? = null

        if (daysOfTheWeek.isEmpty()) {
            focusView = weekdays_picker
            cancel = true
            Toast.makeText(this, "Please pick the weekdays.", Toast.LENGTH_SHORT).show()
        } else {

        }

        if (TextUtils.isEmpty(crn)) {
            crn_et.error = getString(R.string.error_field_required)
            focusView = crn_et
            cancel = true
        }

        if (TextUtils.isEmpty(name)) {
            name_tv.error = getString(R.string.error_field_required)
            focusView = name_tv
            cancel = true
        }

        if (TextUtils.isEmpty(number)) {
            number_tv.error = getString(R.string.error_field_required)
            focusView = number_tv
            cancel = true
        }

        if (TextUtils.isEmpty(prefix)) {
            prefix_tv.error = getString(R.string.error_field_required)
            focusView = prefix_tv
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            showProgress(true)
            val newClass = Class(prefix, number, name, crn, startTime, endTime, daysOfTheWeek)
            createClass(newClass)
        }
    }

    private fun weekdaysInitials(selectedDaysText: List<MaterialDayPicker.Weekday>): String {
        var res = ""
        for (day: MaterialDayPicker.Weekday in selectedDaysText) {
            res += when (day) {
                MaterialDayPicker.Weekday.MONDAY -> "M"
                MaterialDayPicker.Weekday.TUESDAY -> "T"
                MaterialDayPicker.Weekday.WEDNESDAY -> "W"
                MaterialDayPicker.Weekday.THURSDAY -> "R"
                MaterialDayPicker.Weekday.FRIDAY -> "F"
                MaterialDayPicker.Weekday.SATURDAY -> "Sat"
                MaterialDayPicker.Weekday.SUNDAY -> "Sun"
            }
        }
        return res
    }

    /**
     * Shows the progress UI and hides the create form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        scrollView.visibility = if (show) View.GONE else View.VISIBLE
        scrollView.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                scrollView.visibility = if (show) View.GONE else View.VISIBLE
            }
        })

        create_class_progress.visibility = if (show) View.VISIBLE else View.GONE
        create_class_progress.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                create_class_progress.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }

    private fun createClass(new_class: Class) {
        val id = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore.getInstance().collection("INSTRUCTORS").document(id)
                .collection("MY_CLASSES").document(new_class.CRN).set(new_class)
                .addOnCompleteListener { task ->
                    showProgress(false)
                    if (task.isSuccessful) {
                        Log.d(TAG, "new class successfully created with ID: " + new_class.CRN)
                        Toast.makeText(this@CreateClassActivity, "Class added.",
                                Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    } else {
                        Log.w(TAG, "create new class: failure", task.exception)
                        Snackbar.make(findViewById(R.id.constraintLayout),
                                "Something went wrong. Please try again.", Snackbar.LENGTH_SHORT).show()
                    }
                }
    }
}