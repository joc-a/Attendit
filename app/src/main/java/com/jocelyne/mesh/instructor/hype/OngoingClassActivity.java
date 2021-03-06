package com.jocelyne.mesh.instructor.hype;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

import com.jocelyne.mesh.MyApplication;
import com.jocelyne.mesh.R;

import java.lang.ref.WeakReference;

public class OngoingClassActivity extends AppCompatActivity {

    private static final int REQUEST_ACCESS_COARSE_LOCATION_ID = 0;
    private static WeakReference<OngoingClassActivity> defaultInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing_class);

        ListView listView;

        MyApplication myApplication = (MyApplication) getApplication();
        final OngoingClassActivity ongoingClassActivity = this;
        myApplication.setActivity(this);

        listView = (ListView) findViewById(R.id.present_students_list);
        listView.setAdapter(new PresentStudentAdapter(this, myApplication.getPresentStudentsMap()));

        setOngoingClassActivity(this);
    }



    @Override
    protected void onResume() {
        super.onResume();

        MyApplication myApplication = (MyApplication) getApplication();
        myApplication.configureHype();

        // Updates the UI on the press of a back button
        updateInterface();
    }

    public static OngoingClassActivity getDefaultInstance() {
        return defaultInstance != null ? defaultInstance.get() : null;
    }

    private static void setOngoingClassActivity(OngoingClassActivity instance) {
        defaultInstance = new WeakReference<>(instance);
    }

    public void notifyStudentsChanged() {
        updateInterface();
    }

    protected void updateInterface() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListView listView = (ListView) findViewById(R.id.present_students_list);
                updateHypeInstancesLabel(listView.getAdapter().getCount());

                ((PresentStudentAdapter)listView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    private void updateHypeInstancesLabel(int nHypeInstances)
    {
        TextView hypeInstancesText = (TextView) findViewById(R.id.no_of_students);

        if(nHypeInstances == 0)
            hypeInstancesText.setText("No Hype Devices Found");
        else
            hypeInstancesText.setText("Hype Devices Found: " + nHypeInstances);
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
}
