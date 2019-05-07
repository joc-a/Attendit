package com.jocelyne.mesh.instructor.classes


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jocelyne.mesh.R
import com.jocelyne.mesh.instructor.classes.ClassesFragment.OnClassesFragmentInteractionListener
import kotlinx.android.synthetic.main.item_class.view.*

/**
 * [RecyclerView.Adapter] that can display a [ClassItem] and makes a call to the
 * specified [OnClassesFragmentInteractionListener].
 */
class ClassAdapter(
        private val mValues: List<Class>,
        private val mListener: OnClassesFragmentInteractionListener?)
    : RecyclerView.Adapter<ClassAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Class
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onClassesFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_class, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mPrefixView.text = item.prefix
        holder.mNumView.text = item.number
        holder.mNameView.text = item.name
        holder.mDaysOfWeek.text = item.daysOfTheWeek
        holder.mStartTime.text = item.startTime
        holder.mEndTime.text = item.endTime

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mPrefixView: TextView = mView.prefix_et
        val mNumView: TextView = mView.number_et
        val mNameView: TextView = mView.name_et
        val mDaysOfWeek: TextView = mView.days_of_the_week
        val mStartTime: TextView = mView.start_time
        val mEndTime: TextView = mView.end_time

//        override fun toString(): String {
//            return super.toString() + " '" + mContentView.text + "'"
//        }
    }
}
