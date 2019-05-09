package com.jocelyne.mesh.student;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jocelyne.mesh.R;

import java.lang.ref.WeakReference;

public class StudentMainActivity extends AppCompatActivity {

    private static WeakReference<StudentMainActivity> defaultInstance;

    private TextView successTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        successTextView = findViewById(R.id.success_message_tv);
    }

    public static StudentMainActivity getDefaultInstance() {
        return defaultInstance != null ? defaultInstance.get() : null;
    }

    public void confirmCheckIn() {
        successTextView.setText("Check-in successful!");
    }

    public void notifyClassLost() {
        successTextView.setText("Class was lost.");
    }
}
