package com.jocelyne.mesh.instructor.session;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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
import android.widget.VideoView;

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
import com.jocelyne.mesh.session_management.SessionManager;
import com.jocelyne.mesh.session_management.Student;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;

public class SessionFragment extends Fragment {

    private String TAG = "SessionFragment";
    private String KEY_ONGOING_CLASS = "ongoingClass";

    private String currentUserID;
    private ArrayList<Class> classList;
    private Class selectedClass;
    private Map<String, Student> registeredStudentsMap;
    private ClassSpinnerAdapter classSpinnerAdapter;
    private boolean ongoingClass;

    // Hype variables
    private static final int REQUEST_ACCESS_COARSE_LOCATION_ID = 0;
    private static WeakReference<SessionFragment> defaultInstance;

    // UI components
    private TextView spinnerPromptTextView;
    private Spinner classesSpinner;
    private Button startBtn;
    private View ongoingClassLayout;
    private ListView listView;
    private TextView hypeInstancesText;
    private ProgressBar progressBar;
    private VideoView mVideoView;

    private OnSessionFragmentInteractionListener mListener;

    public SessionFragment() {
        // Required empty public constructor
    }

    public static SessionFragment newInstance() {
        return new SessionFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionFragmentInteractionListener) {
            mListener = (OnSessionFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setHasOptionsMenu(true);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_session, container, false);

        // restore state
        if (savedInstanceState != null) {
            ongoingClass = savedInstanceState.getBoolean(KEY_ONGOING_CLASS);
        } else {
            ongoingClass = false;
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mVideoView = (VideoView) view.findViewById(R.id.bgVideoView);
//
//        Uri uri = Uri.parse("android.resource://" + getActivity().getPackageName()+"/"+R.raw.mesh_network_video);
//
//        mVideoView.setVideoURI(uri);
//        mVideoView.start();
//
//        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                mediaPlayer.setLooping(true);
//            }
//        });

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

        startBtn = view.findViewById(R.id.start_session_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSession();
            }
        });

        Button cancelBtn = view.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelSession();
            }
        });

        Button saveBtn = view.findViewById(R.id.save_session_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSession();
            }
        });

        ongoingClassLayout = view.findViewById(R.id.ongoing_class_layout);
        listView = view.findViewById(R.id.present_students_list);
        hypeInstancesText = view.findViewById(R.id.hype_instances_label);
        progressBar = view.findViewById(R.id.progress_bar);

        showProgress(true);

        // load classes into spinner
        loadMyClasses(currentUserID);

        showProgress(false);

        if (ongoingClass) {
            showOngoingClassUI();
        } else {
            showNoClassUI();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ONGOING_CLASS, ongoingClass);
    }

    private void startSession() {
        showOngoingClassUI();

        // get students map of selected class
        registeredStudentsMap = selectedClass.studentsMap;

        // Hype it up
        MyApplication myApplication = (MyApplication) getActivity().getApplication();
        myApplication.setRegisteredStudentsMap(registeredStudentsMap);
        myApplication.setSelectedClass(selectedClass);
        listView.setAdapter(new PresentStudentAdapter(getActivity(), myApplication.getPresentStudentsMap()));
        myApplication.setActivity(getActivity());
        setDashboardFragment(this);
        myApplication.configureHype();

        // for restoring fragment state after leaving it
        ongoingClass = true;
    }

    private void cancelSession() {
        showNoClassUI();

        ongoingClass = false;

        // clean up
        MyApplication myApplication = (MyApplication) getActivity().getApplication();
    }

    private void saveSession() {
        showNoClassUI();

        // Stop Hype
        MyApplication myApplication = (MyApplication) getActivity().getApplication();
        myApplication.requestHypeToStop();

        ongoingClass = false;

        // save session to db

    }

    private void showOngoingClassUI() {
        // hide UI components
        spinnerPromptTextView.setVisibility(View.GONE);
        classesSpinner.setVisibility(View.GONE);
        startBtn.setVisibility(View.GONE);

        // show UI components
        ongoingClassLayout.setVisibility(View.VISIBLE);
    }

    private void showNoClassUI() {
        // show UI components
        spinnerPromptTextView.setVisibility(View.VISIBLE);
        classesSpinner.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.VISIBLE);

        // hide UI components
        ongoingClassLayout.setVisibility(View.GONE);
    }

    public static SessionFragment getDefaultInstance() {
        return defaultInstance != null ? defaultInstance.get() : null;
    }

    private static void setDashboardFragment(SessionFragment instance) {
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

        startBtn.setVisibility(show ? View.GONE : View.VISIBLE);
        startBtn.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startBtn.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        ongoingClassLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        ongoingClassLayout.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ongoingClassLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
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
    public interface OnSessionFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSessionFragmentInteraction(Uri uri);
        void startClass();
    }
}