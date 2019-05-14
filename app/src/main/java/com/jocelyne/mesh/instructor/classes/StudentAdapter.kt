package com.jocelyne.mesh.instructor.classes

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.jocelyne.mesh.R
import kotlinx.android.synthetic.main.item_student.view.*

class StudentAdapter(private val mValues: List<String>, private val mListener: OnDeleteListener) : RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_student, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mValues.size

    override fun onBindViewHolder(holder: StudentAdapter.ViewHolder, position: Int) {
        val item = mValues[position] // item is the student id
        holder.mIdView.text = item
        holder.mDeleteBtn.setOnClickListener {
            mListener.deleteStudent(item)
        }
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.student_id
        val mDeleteBtn: ImageButton = mView.delete_btn
    }

    interface OnDeleteListener {
        fun deleteStudent(id: String)
    }
}