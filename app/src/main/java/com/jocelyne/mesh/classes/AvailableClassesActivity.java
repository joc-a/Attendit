package com.jocelyne.mesh.classes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.jocelyne.mesh.R;
import com.jocelyne.mesh.session.SessionManager;

public class AvailableClassesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_classes);

        ImageButton signOutBtn = findViewById(R.id.sign_out_btn);
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionManager.Companion.getInstance(getApplicationContext()).signOut();
            }
        });
    }
}
