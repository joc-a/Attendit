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

    fun checkFields() {
        // Reset errors
        prefix_et.error = null
        number_et.error = null
        name_et.error = null
        crn_et.error = null

        // Store values
        val prefix = prefix_et.text.toString()
        val number = number_et.text.toString()
        val name = name_et.text.toString()
        val crn = crn_et.text.toString()
        val start_time = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            start_time_picker.hour.toString()
        } else {
            start_time_picker.currentHour.toString()
        }
        val end_time = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            end_time_picker.hour.toString()
        } else {
            end_time_picker.currentHour.toString()
        }

        var cancel = false
        var focusView: View? = null

        if (TextUtils.isEmpty(crn)) {
            crn_et.error = getString(R.string.error_field_required)
            focusView = crn_et
            cancel = true
        }

        if (TextUtils.isEmpty(name)) {
            name_et.error = getString(R.string.error_field_required)
            focusView = name_et
            cancel = true
        }

        if (TextUtils.isEmpty(number)) {
            number_et.error = getString(R.string.error_field_required)
            focusView = number_et
            cancel = true
        }

        if (TextUtils.isEmpty(prefix)) {
            prefix_et.error = getString(R.string.error_field_required)
            focusView = prefix_et
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            showProgress(true)
            val new_class = Class(prefix, number, name, crn, start_time, end_time, "MWF")
            createClass(new_class)
        }
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

    fun createClass(new_class: Class) {
        val id = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore.getInstance().collection("INSTRUCTORS").document(id)
                .collection("MY_CLASSES").document(new_class.CRN).set(new_class)
                .addOnCompleteListener { task ->
                    showProgress(false)
                    if (task.isSuccessful) {
                        Log.d(TAG, "new class successfully created with ID: " + new_class.CRN)
                        Toast.makeText(this@CreateClassActivity, "Class added.",
                                Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w(TAG, "create new class: failure", task.exception)
                        Snackbar.make(findViewById(R.id.constraintLayout),
                                "Something went wrong. Please try again.", Snackbar.LENGTH_SHORT).show()
                    }
                }
    }
}