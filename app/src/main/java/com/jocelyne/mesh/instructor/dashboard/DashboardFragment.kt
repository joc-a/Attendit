package com.jocelyne.mesh.instructor.dashboard

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.jocelyne.mesh.MyApplication

import com.jocelyne.mesh.R
import com.jocelyne.mesh.instructor.classes.Class
import com.jocelyne.mesh.instructor.hype.OngoingClassActivity
import com.jocelyne.mesh.instructor.hype.PresentStudentAdapter
import com.jocelyne.mesh.session.SessionManager
import kotlinx.android.synthetic.main.fragment_dashboard.*
import java.lang.ref.WeakReference

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DashboardFragment.OnDashboardFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class DashboardFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val TAG = "DashboardFragment"

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnDashboardFragmentInteractionListener? = null

    private lateinit var currentUserID: String
    private lateinit var classList: ArrayList<Class>
    private lateinit var selectedClass: Class
    private lateinit var classSpinnerAdapter: ClassSpinnerAdapter

    // Hype variables
    private val REQUEST_ACCESS_COARSE_LOCATION_ID = 0
    private var defaultInstance: WeakReference<DashboardFragment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        currentUserID = FirebaseAuth.getInstance().currentUser?.uid!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showProgress(true)

        // load classes into spinner
        loadMyClasses(currentUserID)

        showProgress(false)

        classes_spinner.onItemSelectedListener = this

        control_btn.setOnClickListener {
            listener?.startClass()
        }
    }

    private fun startClass() {
        // hide UI components
        class_spinner_prompt.visibility = View.GONE
        classes_spinner.visibility = View.GONE
        control_btn.setText(R.string.end_class)

        // show UI components
        ongoing_class_layout.visibility = View.VISIBLE

        // Hype it up
        val myApplication = activity?.getApplication() as MyApplication
        present_students_list.adapter = PresentStudentAdapter(context, myApplication.presentStudentsMap)
        myApplication.configureHype()
    }

    fun getDefaultInstance(): DashboardFragment? {
        return if (defaultInstance != null) defaultInstance!!.get() else null
    }

    private fun setDashboardFragment(instance: DashboardFragment) {
        defaultInstance = WeakReference<DashboardFragment>(instance)
    }

    fun notifyStudentsChanged() {
        updateInterface()
    }

    protected fun updateInterface() {
        activity?.runOnUiThread(Runnable {
            updateHypeInstancesLabel(present_students_list.adapter.count)

            (present_students_list.adapter as PresentStudentAdapter).notifyDataSetChanged()
        })
    }

    private fun updateHypeInstancesLabel(nHypeInstances: Int) {
        if (nHypeInstances == 0)
            hype_instances_label.text = "No Hype Devices Found"
        else
            hype_instances_label.text = "Hype Devices Found: $nHypeInstances"
    }

    fun requestPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_ACCESS_COARSE_LOCATION_ID)
        } else {
            val myApplication = getActivity()?.getApplication() as MyApplication
            myApplication.requestHypeToStart()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_ACCESS_COARSE_LOCATION_ID -> {

                val myApplication = activity?.getApplication() as MyApplication
                myApplication.requestHypeToStart()
            }
        }
    }

    private fun loadMyClasses(currentUserID: String) {
        val docRef = FirebaseFirestore.getInstance()
                .collection("INSTRUCTORS")
                .document(currentUserID)
                .collection("MY_CLASSES")
        docRef.addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.w(TAG, "Spinner classes listen failed.", e)
                return@EventListener
            }

            classList = ArrayList<Class>()
            for (doc in value!!) {
                val classItem = doc.toObject(Class::class.java!!)
                classList.add(classItem)
            }
            classSpinnerAdapter = ClassSpinnerAdapter(context, classList)
            classes_spinner.adapter = classSpinnerAdapter
            Log.d(TAG, "my spinner classes retrieved successfully")
        })
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
        selectedClass = classList[position]
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        class_spinner_prompt.visibility = if (show) View.GONE else View.VISIBLE
        class_spinner_prompt.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                class_spinner_prompt.visibility = if (show) View.GONE else View.VISIBLE
            }
        })

        classes_spinner.visibility = if (show) View.GONE else View.VISIBLE
        classes_spinner.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                classes_spinner.visibility = if (show) View.GONE else View.VISIBLE
            }
        })

        control_btn.visibility = if (show) View.GONE else View.VISIBLE
        control_btn.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                control_btn.visibility = if (show) View.GONE else View.VISIBLE
            }
        })

        dashboard_progress.visibility = if (show) View.VISIBLE else View.GONE
        dashboard_progress.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                dashboard_progress.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_connect, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_sign_out -> SessionManager.getInstance(requireContext()).signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onDashboardFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnDashboardFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnDashboardFragmentInteractionListener")
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
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnDashboardFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onDashboardFragmentInteraction(uri: Uri)
        fun startClass()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                DashboardFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
