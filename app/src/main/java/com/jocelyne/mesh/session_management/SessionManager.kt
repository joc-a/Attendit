package com.jocelyne.mesh.session_management

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.jocelyne.mesh.login.LoginActivity

class SessionManager {

    private var pref: SharedPreferences
    private var editor: SharedPreferences.Editor
    private var context: Context

    private val PREF_NAME = "SessionManager"

    val KEY_USER_ID = "userID"
    val KEY_FNAME = "fname"
    val KEY_LNAME = "lname"
    val KEY_EMAIL = "email"
    val KEY_IS_INSTRUCTOR = "isInstructor"

    val KEY_STUDENT_ID = "studentID"

    // used because kotlin cannot have more than one "static" function
    companion object {
        fun getInstance(context: Context): SessionManager {
            return SessionManager(context)
        }
    }

    constructor(context: Context) {
        this.context = context
        pref = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = pref.edit()
    }

    fun createLoginSession(user: User, id: String, isInstructor: Boolean) {
        // store user info in pref
        editor.putString(KEY_USER_ID, id)
        editor.putString(KEY_FNAME, user.firstName)
        editor.putString(KEY_LNAME, user.lastName)
        editor.putString(KEY_EMAIL, user.email)
        editor.putBoolean(KEY_IS_INSTRUCTOR, isInstructor)

        if (user is Student) {
            editor.putString(KEY_STUDENT_ID, user.studentID)
        }

        // commit changes
        editor.commit()
    }

    fun getStudentId(): Int {
        return pref.getString(KEY_STUDENT_ID, "").toInt()
    }

    fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun isInstructor(): Boolean {
        return pref.getBoolean(KEY_IS_INSTRUCTOR, false)
    }

    // to be called in activities always, not fragments
    fun signOut() {
        editor.clear().commit()
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(context, intent, null) // TODO modify signout
    }

    fun getUserID(): String {
        return pref.getString(KEY_USER_ID, "")
    }

    fun updateFirstName(newFirstName: String) {
        editor.putString(KEY_FNAME, newFirstName).commit()
    }

    fun updateLastName(newLastName: String) {
        editor.putString(KEY_LNAME, newLastName).commit()
    }

    fun updateEmail(newEmail: String) {
        editor.putString(KEY_EMAIL, newEmail).commit()
    }

}