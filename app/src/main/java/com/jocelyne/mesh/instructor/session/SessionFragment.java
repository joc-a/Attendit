package com.jocelyne.mesh.instructor.session;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Toast;
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
import com.jocelyne.mesh.session_management.Student;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;

public class SessionFragment extends Fragment {

    private String TAG = "SessionFragment";
    private String STATE_PREF = "statePref";
    private String KEY_ONGOING_CLASS = "ongoingClass";

    private String currentUserID;
    private ArrayList<Class> classList;
    private Class selectedClass;
    private Map<String, Student> registeredStudentsMap;
    private ClassSpinnerAdapter classSpinnerAdapter;

    private SharedPreferences pref;
    private boolean ongoingClass;

    // Hype variables
    private static final int REQUEST_ACCESS_COARSE_LOCATION_ID = 0;
    private static WeakReference<SessionFragment> defaultInstance;

    // UI components
    private TextView titleTextView;
    private TextView descriptionTextView;
    private GifImageView gifImageView;
    private TextView spinnerPromptTextView;
    private Spinner classesSpinner;
    private Button startBtn;
    private Button cancelBtn;
    private Button saveBtn;
    private View ongoingClassLayout;
    private View getOngoingClassButtonsLayout;
    private ListView listView;
    private TextView hypeInstancesText;
    private ProgressBar progressBar;
    private VideoView mVideoView;
    private TextView prefix;
    private TextView number;
    private TextView name;
    private TextView daysOfTheWeek;
    private TextView startTime;
    private TextView endTime;
    private TextView numStudents;

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

        setHasOptionsMenu(true);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_session, container, false);

        // restore state
        pref = getActivity().getSharedPreferences(STATE_PREF, Context.MODE_PRIVATE);
        ongoingClass = pref.getBoolean(KEY_ONGOING_CLASS, false);

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

        titleTextView = view.findViewById(R.id.title);
        descriptionTextView = view.findViewById(R.id.description);
        gifImageView = view.findViewById(R.id.gif_view);
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

        cancelBtn = view.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelSession();
            }
        });

        saveBtn = view.findViewById(R.id.save_session_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSession();
            }
        });

        ongoingClassLayout = view.findViewById(R.id.ongoing_class_layout);
        getOngoingClassButtonsLayout = view.findViewById(R.id.ongoing_class_buttons_layout);
        listView = view.findViewById(R.id.present_students_list);
        hypeInstancesText = view.findViewById(R.id.no_of_students);
        progressBar = view.findViewById(R.id.progress_bar);

        prefix = view.findViewById(R.id.prefix_tv);
        number = view.findViewById(R.id.number_tv);
        name = view.findViewById(R.id.name_tv);
        daysOfTheWeek = view.findViewById(R.id.days_of_the_week);
        startTime = view.findViewById(R.id.start_time);
        endTime = view.findViewById(R.id.end_time);
        numStudents = view.findViewById(R.id.no_of_students);

        // load classes into spinner
        loadMyClasses(currentUserID);

        if (ongoingClass) {
            showOngoingClassUI();
        } else {
            showNoClassUI();
        }
    }

    private void startSession() {
        showOngoingClassUI();

        if (selectedClass != null) {
            prefix.setText(selectedClass.prefix);
            number.setText(selectedClass.number);
            name.setText(selectedClass.name);
            daysOfTheWeek.setText(selectedClass.daysOfTheWeek);
            startTime.setText(selectedClass.startTime);
            endTime.setText(selectedClass.endTime);
        }

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
        pref.edit().putBoolean(KEY_ONGOING_CLASS, ongoingClass).commit();
    }

    private void cancelSession() {
        showNoClassUI();

        ongoingClass = false;
        pref.edit().putBoolean(KEY_ONGOING_CLASS, ongoingClass).commit();
    }

    private void saveSession() {
        showNoClassUI();

        ongoingClass = false;
        pref.edit().putBoolean(KEY_ONGOING_CLASS, ongoingClass).commit();

        // save session to db
        Toast.makeText(getContext(), "Session saved!", Toast.LENGTH_SHORT).show();
    }

    private void showOngoingClassUI() {
        // hide UI components
        titleTextView.setVisibility(View.GONE);
        descriptionTextView.setVisibility(View.GONE);
        gifImageView.setVisibility(View.GONE);
        spinnerPromptTextView.setVisibility(View.GONE);
        classesSpinner.setVisibility(View.GONE);
        startBtn.setVisibility(View.GONE);

        // show UI components
        ongoingClassLayout.setVisibility(View.VISIBLE);
        layoutElemanlarininGorunumunuDegistir(ongoingClassLayout, true);
//        startTime.setVisibility(View.VISIBLE);
//        endTime.setVisibility(View.VISIBLE);
//        getOngoingClassButtonsLayout.setVisibility(View.VISIBLE);
//        cancelBtn.setVisibility(View.VISIBLE);
//        saveBtn.setVisibility(View.VISIBLE);
//        numStudents.setVisibility(View.VISIBLE);
//        listView.setVisibility(View.VISIBLE);
    }

    private void showNoClassUI() {
        // show UI components
        titleTextView.setVisibility(View.VISIBLE);
        descriptionTextView.setVisibility(View.VISIBLE);
        gifImageView.setVisibility(View.VISIBLE);
        spinnerPromptTextView.setVisibility(View.VISIBLE);
        classesSpinner.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.VISIBLE);

        // hide UI components
        ongoingClassLayout.setVisibility(View.GONE);
        layoutElemanlarininGorunumunuDegistir(ongoingClassLayout, false);
//        startTime.setVisibility(View.GONE);
//        endTime.setVisibility(View.GONE);
//        getOngoingClassButtonsLayout.setVisibility(View.GONE);
//        cancelBtn.setVisibility(View.GONE);
//        saveBtn.setVisibility(View.GONE);
//        numStudents.setVisibility(View.GONE);
//        listView.setVisibility(View.GONE);
    }

    private void layoutElemanlarininGorunumunuDegistir(View view, boolean gorunur_mu_olsun) {
        ViewGroup view_group;
        try {
            view_group = (ViewGroup) view;
        } catch (ClassCastException e) {
            return;
        }

        int view_eleman_sayisi = view_group.getChildCount();
        for (int i = 0; i < view_eleman_sayisi; i++) {
            View view_group_eleman = view_group.getChildAt(i);
            if (gorunur_mu_olsun) {
                view_group_eleman.setVisibility(View.VISIBLE);
            } else {
                view_group_eleman.setVisibility(View.GONE);
            }
            layoutElemanlarininGorunumunuDegistir(view_group_eleman, gorunur_mu_olsun);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pref.edit().putBoolean(KEY_ONGOING_CLASS, ongoingClass).commit();
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
            hypeInstancesText.setText("Checked in students: 0");
        else
            hypeInstancesText.setText("Checked in students: " + nHypeInstances);
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

    /*
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
    */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_session, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            mListener.openSettings();
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
        void openSettings();
    }
}