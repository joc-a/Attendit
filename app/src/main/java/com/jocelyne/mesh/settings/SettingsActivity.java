package com.jocelyne.mesh.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jocelyne.mesh.R;
import com.jocelyne.mesh.session_management.SessionManager;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        View signout = findViewById(R.id.signout_layout);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionManager.Companion.getInstance(getApplicationContext()).signOut();
            }
        });
    }
}
