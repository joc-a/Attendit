package com.jocelyne.mesh.instructor.classes

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jocelyne.mesh.R
import com.jocelyne.mesh.session_management.Student
import kotlinx.android.synthetic.main.activity_sign_up.view.*
import kotlinx.android.synthetic.main.item_student.view.*

class StudentAdapter(private val mValues: List<Student>) : RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_student, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mValues.size

    override fun onBindViewHolder(holder: StudentAdapter.ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mIdView.text = item.studentID
        holder.mFirstNameView.text = item.firstName
        holder.mLastNameView.text = item.lastName
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.student_id
        val mFirstNameView: TextView = mView.f_name
        val mLastNameView: TextView = mView.last_name
    }
}