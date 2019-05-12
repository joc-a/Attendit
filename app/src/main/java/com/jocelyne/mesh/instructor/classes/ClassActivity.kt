package com.jocelyne.mesh.instructor.classes

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.jocelyne.mesh.R
import com.jocelyne.mesh.session_management.Student
import kotlinx.android.synthetic.main.activity_class.*


class ClassActivity : AppCompatActivity() {

    private val TAG = "ClassActivity"

    private lateinit var currentUserID: String
    private lateinit var classID: String
    private var myClass: Class? = null

    private lateinit var studentsMap: Map<String, Student>
    private lateinit var studentsList: ArrayList<Student>

    private lateinit var recyclerView: RecyclerView
    private lateinit var studentAdapter: StudentAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class)

        classID = intent.getStringExtra("classID")
        currentUserID = FirebaseAuth.getInstance().currentUser?.uid!!

        viewManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.students_list)
        with(recyclerView) {
            layoutManager = viewManager
        }

        val docRef = FirebaseFirestore.getInstance()
                .collection("INSTRUCTORS").document(currentUserID)
                .collection("MY_CLASSES").document(classID)

        docRef.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@EventListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                myClass = snapshot.toObject(Class::class.java)
                displayClassInfo()
                displayStudents()
            } else {
                Log.d(TAG, "Current data: null")
            }
        })
    }

    private fun displayClassInfo() {
        prefix_tv.text = myClass!!.prefix
        number_tv.text = myClass!!.number
        name_tv.text = myClass!!.name
        crn_tv.text = "CRN: " + myClass!!.CRN
        start_time.text = myClass!!.startTime
        end_time.text = myClass!!.endTime
    }

    private fun displayStudents() {
        if (myClass!!.studentsMap != null) {
            studentsMap = myClass!!.studentsMap
            studentsList = ArrayList()
            studentsMap.forEach { (key, value) -> studentsList.add(value) }
            studentAdapter = StudentAdapter(studentsList)
            recyclerView.adapter = studentAdapter
        }
    }
}
