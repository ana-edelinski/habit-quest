package com.example.habitquest.presentation.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.habitquest.R;
import com.example.habitquest.presentation.fragments.CategoryListFragment;
import com.example.habitquest.presentation.fragments.TaskListFragment;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name); // ← ovde app name
        }
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_categories) {
            setTitle(R.string.title_categories);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_container, new CategoryListFragment(), "CAT")
                    .commit();
            return true;
        } else if (id == R.id.action_tasks) {
            setTitle(R.string.title_tasks);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_container, new TaskListFragment(), "TASK")
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}