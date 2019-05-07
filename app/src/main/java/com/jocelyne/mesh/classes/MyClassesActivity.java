package com.jocelyne.mesh.classes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.jocelyne.mesh.R;
import com.jocelyne.mesh.instructor.classes.create.CreateClassActivity;

public class MyClassesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_classes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_classes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_class) {
            openCreateClassActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openCreateClassActivity() {
        Intent i = new Intent(MyClassesActivity.this, CreateClassActivity.class);
        startActivity(i);
    }
}
