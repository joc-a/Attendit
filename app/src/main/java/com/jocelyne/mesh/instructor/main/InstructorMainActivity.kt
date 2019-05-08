package com.jocelyne.mesh.instructor.main

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.jocelyne.mesh.R
import com.jocelyne.mesh.instructor.classes.Class
import com.jocelyne.mesh.instructor.classes.ClassesFragment
import kotlinx.android.synthetic.main.activity_instructor_main.*

class InstructorMainActivity : AppCompatActivity(), ClassesFragment.OnClassesFragmentInteractionListener {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_classes -> {
                showClasses()
                supportActionBar?.title = "Classes"
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
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
        val classesFragment = ClassesFragment.newInstance(1)
        openFragment(classesFragment)
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onClassesFragmentInteraction(item: Class?) {
        // go to class activity
    }
}
