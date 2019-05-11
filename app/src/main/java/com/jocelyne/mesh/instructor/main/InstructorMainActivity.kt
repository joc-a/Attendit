package com.jocelyne.mesh.instructor.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.jocelyne.mesh.R
import com.jocelyne.mesh.instructor.hype.OngoingClassActivity
import com.jocelyne.mesh.instructor.classes.Class
import com.jocelyne.mesh.instructor.classes.ClassActivity
import com.jocelyne.mesh.instructor.classes.ClassesFragment
import com.jocelyne.mesh.instructor.dashboard.DashboardFragment
import kotlinx.android.synthetic.main.activity_instructor_main.*

class InstructorMainActivity : AppCompatActivity(),
        ClassesFragment.OnClassesFragmentInteractionListener, DashboardFragment.OnDashboardFragmentInteractionListener {

    private lateinit var classesFragment: ClassesFragment
    private lateinit var connectFragment: DashboardFragment

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_classes -> {
                showClasses()
                supportActionBar?.title = "Classes"
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                showConnect()
                supportActionBar?.title = "Dashboard"
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructor_main)

        setSupportActionBar(toolbar)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        navView.selectedItemId = R.id.navigation_dashboard
    }

    private fun showClasses() {
        classesFragment = ClassesFragment.newInstance(1)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.hide(connectFragment)
                .add(R.id.fragment_container, classesFragment)
                .addToBackStack(null)
                .commit()
    }

    private fun showConnect() {
        connectFragment = DashboardFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, connectFragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onClassesFragmentInteraction(item: Class?) {
        // go to class activity
        val intent = Intent(this, ClassActivity::class.java)
        intent.putExtra("classID", item!!.CRN)
        startActivity(intent)
    }

    override fun onDashboardFragmentInteraction(uri: Uri) {
        val intent = Intent(this, OngoingClassActivity::class.java)
        startActivity(intent)
    }

    override fun startClass() {
        val intent = Intent(this, OngoingClassActivity::class.java)
        startActivity(intent)
    }
}
