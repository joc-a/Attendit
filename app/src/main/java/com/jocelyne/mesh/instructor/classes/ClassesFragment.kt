package com.jocelyne.mesh.instructor.classes

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jocelyne.mesh.R

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ClassesFragment.OnClassesFragmentInteractionListener] interface.
 */
class ClassesFragment : Fragment() {

    private val TAG = "ClassesFragment"

    private var columnCount = 1

    private var listener: OnClassesFragmentInteractionListener? = null

    private lateinit var currentUserID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_classes, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
            }

            currentUserID = FirebaseAuth.getInstance().currentUser?.uid!!

            val query = FirebaseFirestore.getInstance()
                    .collection("INSTRUCTORS")
                    .document(currentUserID)
                    .collection("MY_CLASSES")

            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val list = ArrayList<Class>()
                    for (document in task.result!!) {
                        val classItem = document.toObject(Class::class.java!!)
                        list.add(classItem)
                    }
                    val classAdapter = ClassAdapter(list, listener)
                    view.adapter = classAdapter
                }
            }

        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnClassesFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnClassesFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnClassesFragmentInteractionListener {
        fun onClassesFragmentInteraction(item: Class?)
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
                ClassesFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}
