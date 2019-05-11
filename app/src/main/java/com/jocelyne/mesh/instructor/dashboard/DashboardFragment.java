package com.jocelyne.mesh.instructor.dashboard;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jocelyne.mesh.MyApplication;
import com.jocelyne.mesh.R;
import com.jocelyne.mesh.instructor.classes.Class;
import com.jocelyne.mesh.instructor.hype.PresentStudentAdapter;
import com.jocelyne.mesh.session.SessionManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private String TAG = "DashboardFragment";

    private String currentUserID;
    private ArrayList<Class> classList;
    private Class selectedClass;
    private ClassSpinnerAdapter classSpinnerAdapter;

    // Hype variables
    private static final int REQUEST_ACCESS_COARSE_LOCATION_ID = 0;
    private static WeakReference<DashboardFragment> defaultInstance;

    // UI components
    private TextView spinnerPromptTextView;
    private Spinner classesSpinner;
    private Button controlBtn;
    private View ongoingClassLayout;
    private ListView listView;
    private TextView hypeInstancesText;
    private ProgressBar dashboardProgress;

    private OnDashboardFragmentInteractionListener mListener;

    public DashboardFragment() {
        // Required empty public constructor
    }

    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerPromptTextView = view.findViewById(R.id.class_spinner_prompt);
        classesSpinner = view.findViewById(R.id.classes_spinner);
        classesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedClass = classList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        controlBtn = view.findViewById(R.id.control_btn);
        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startClass();
            }
        });

        ongoingClassLayout = view.findViewById(R.id.ongoing_class_layout);
        listView = view.findViewById(R.id.present_students_list);
        hypeInstancesText = view.findViewById(R.id.hype_instances_label);
        dashboardProgress = view.findViewById(R.id.dashboard_progress);

        showProgress(true);

        // load classes into spinner
        loadMyClasses(currentUserID);

        showProgress(false);
    }

    private void startClass() {
        // hide UI components
        spinnerPromptTextView.setVisibility(View.GONE);
        classesSpinner.setVisibility(View.GONE);
        controlBtn.setText(R.string.end_class);

        // show UI components
        ongoingClassLayout.setVisibility(View.VISIBLE);

        // Hype it up
        MyApplication myApplication = (MyApplication) getActivity().getApplication();
        listView.setAdapter(new PresentStudentAdapter(getActivity(), myApplication.getPresentStudentsMap()));
        myApplication.setActivity(getActivity());
        setDashboardFragment(this);
        myApplication.configureHype();
    }

    public static DashboardFragment getDefaultInstance() {
        return defaultInstance != null ? defaultInstance.get() : null;
    }

    private static void setDashboardFragment(DashboardFragment instance) {
        defaultInstance = new WeakReference<>(instance);
    }

    public void notifyStudentsChanged() {
        updateInterface();
    }

    protected void updateInterface() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateHypeInstancesLabel(listView.getAdapter().getCount());

                ((PresentStudentAdapter)listView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    private void updateHypeInstancesLabel(int nHypeInstances)
    {
        if(nHypeInstances == 0)
            hypeInstancesText.setText("No Hype Devices Found");
        else
            hypeInstancesText.setText("Hype Devices Found: " + nHypeInstances);
    }

    public void requestPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION_ID);
        }
        else {
            MyApplication myApplication = (MyApplication) getActivity().getApplication();
            myApplication.requestHypeToStart();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION_ID:

                MyApplication myApplication = (MyApplication) getActivity().getApplication();
                myApplication.requestHypeToStart();

                break;
        }
    }

    private void loadMyClasses(String currentUserID) {
        CollectionReference collectionRef = FirebaseFirestore.getInstance()
                .collection("INSTRUCTORS")
                .document(currentUserID)
                .collection("MY_CLASSES");

        collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Spinner classes listen failed.", e);
                    return;
                }

                classList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("name") != null) {
                        Class classItem = doc.toObject(Class.class);
                        classList.add(classItem);
                    }
                }
                classSpinnerAdapter = new ClassSpinnerAdapter(getContext(), classList);
                classesSpinner.setAdapter(classSpinnerAdapter);
                Log.d(TAG, "my spinner classes retrieved successfully");
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        spinnerPromptTextView.setVisibility(show ? View.GONE : View.VISIBLE);
        spinnerPromptTextView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                spinnerPromptTextView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        classesSpinner.setVisibility(show ? View.GONE : View.VISIBLE);
        classesSpinner.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                classesSpinner.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        controlBtn.setVisibility(show ? View.GONE : View.VISIBLE);
        controlBtn.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                controlBtn.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        dashboardProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        dashboardProgress.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dashboardProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_connect, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            SessionManager.Companion.getInstance(requireContext()).signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDashboardFragmentInteractionListener) {
            mListener = (OnDashboardFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDashboardFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDashboardFragmentInteractionListener {
        // TODO: Update argument type and name
        void onDashboardFragmentInteraction(Uri uri);
        void startClass();
    }
}