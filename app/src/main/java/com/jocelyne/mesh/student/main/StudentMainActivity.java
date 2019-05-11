package com.jocelyne.mesh.student.main;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jocelyne.mesh.MyApplication;
import com.jocelyne.mesh.R;
import com.jocelyne.mesh.instructor.hype.InstructorApplication;
import com.jocelyne.mesh.session.SessionManager;
import com.jocelyne.mesh.student.hype.StudentApplication;

import java.lang.ref.WeakReference;

public class StudentMainActivity extends AppCompatActivity {

    private static final int REQUEST_ACCESS_COARSE_LOCATION_ID = 0;
    private static WeakReference<StudentMainActivity> defaultInstance;

    private Toolbar toolbar;
    private TextView successTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        successTextView = findViewById(R.id.success_message_tv);

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

    public void confirmCheckIn() {
        successTextView.setText("Check-in successful!");
    }

    public void notifyClassLost() {
        successTextView.setText("Class was lost.");
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
