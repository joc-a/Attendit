package com.jocelyne.mesh.student.main;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jocelyne.mesh.MyApplication;
import com.jocelyne.mesh.R;
import com.jocelyne.mesh.session_management.SessionManager;

import java.lang.ref.WeakReference;

public class StudentMainActivity extends AppCompatActivity {

    private static final int REQUEST_ACCESS_COARSE_LOCATION_ID = 0;
    private static WeakReference<StudentMainActivity> defaultInstance;

    private Toolbar toolbar;
    private View confirmationView;
    private TextView classNameTextView;
    private TextView absencesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        confirmationView = findViewById(R.id.confirmation_layout);
        classNameTextView = findViewById(R.id.class_name);
        absencesTextView = findViewById(R.id.absences);

        final MyApplication myApplication = (MyApplication) getApplication();
        final StudentMainActivity studentMainActivity = this;
        myApplication.setActivity(this);

        setStudentMainActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyApplication myApplication = (MyApplication) getApplication();
        myApplication.configureHype();
    }

    public static StudentMainActivity getDefaultInstance() {
        return defaultInstance != null ? defaultInstance.get() : null;
    }

    private static void setStudentMainActivity(StudentMainActivity instance) {
        defaultInstance = new WeakReference<>(instance);
    }

    public void confirmCheckIn(String className) {
        confirmationView.setVisibility(View.VISIBLE);
        classNameTextView.setText(className);
    }

    public void notifyClassLost() {
    }

    public void requestPermissions(Activity activity) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION_ID);
        }
        else {
            MyApplication myApplication = (MyApplication) getApplication();
            myApplication.requestHypeToStart();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION_ID:

                MyApplication myApplication = (MyApplication) getApplication();
                myApplication.requestHypeToStart();

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            SessionManager.Companion.getInstance(getApplicationContext()).signOut();
        }
        return super.onOptionsItemSelected(item);
    }
}
