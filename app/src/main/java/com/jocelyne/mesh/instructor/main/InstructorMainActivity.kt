package com.jocelyne.mesh.instructor.main

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.jocelyne.mesh.R
import com.jocelyne.mesh.instructor.classes.create.CreateClassActivity
import com.jocelyne.mesh.instructor.classes.Class
import com.jocelyne.mesh.instructor.classes.ClassesFragment

class InstructorMainActivity : AppCompatActivity(), ClassesFragment.OnClassesFragmentInteractionListener {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_classes -> {
                showClasses()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructor_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if(currentFragment is ClassesFragment) {
            menuInflater.inflate(R.menu.menu_classes, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_add_class -> createNewClass()
        }
        return super.onOptionsItemSelected(item)
    }

    fun createNewClass() {
        val i = Intent(this, CreateClassActivity::class.java)
        startActivity(i)
    }

    override fun onClassesFragmentInteraction(item: Class?) {
        // go to class activity
    }
}
