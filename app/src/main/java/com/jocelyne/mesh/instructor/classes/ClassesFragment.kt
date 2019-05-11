package com.jocelyne.mesh.instructor.classes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jocelyne.mesh.R
import com.jocelyne.mesh.instructor.classes.create.CreateClassActivity
import com.jocelyne.mesh.instructor.classes.create.CreateClassEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


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
    private lateinit var classAdapter: ClassAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

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
                    Log.d(TAG, "my classes retrieved successfully")
                    val list = ArrayList<Class>()
                    for (document in task.result!!) {
                        val classItem = document.toObject(Class::class.java!!)
                        list.add(classItem)
                    }
                    classAdapter = ClassAdapter(list, listener)
                    view.adapter = classAdapter
                } else {
                    Log.w(TAG, "failed to retrieve my classes", task.exception)
                }
            }

        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_classes, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_add_class -> createNewClass()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createNewClass() {
        val i = Intent(activity, CreateClassActivity::class.java)
        startActivity(i)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onCreateClassEvent(event: CreateClassEvent) {
        classAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
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
