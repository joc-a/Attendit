package com.jocelyne.mesh.welcome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.jocelyne.mesh.R
import com.jocelyne.mesh.classes.AvailableClassesActivity
import com.jocelyne.mesh.classes.MyClassesActivity
import com.jocelyne.mesh.login.LoginActivity
import com.jocelyne.mesh.session.SessionManager

class WelcomeActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT : Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        Handler().postDelayed({
            val mainIntent = Intent(this@WelcomeActivity,
                    if (SessionManager.getInstance(applicationContext).isLoggedIn()) {
                        if(SessionManager.getInstance(applicationContext).isInstructor())
                            MyClassesActivity::class.java
                        else
                            AvailableClassesActivity::class.java
                    } else {
                        LoginActivity::class.java
                    })
            startActivity(mainIntent)
            finish()
        }, SPLASH_TIME_OUT)
    }
}
