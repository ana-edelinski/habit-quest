package com.example.habitquest.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.R;
import com.example.habitquest.presentation.viewmodels.SignUpViewModel;
import com.example.habitquest.presentation.viewmodels.factories.SignUpViewModelFactory;
import com.google.android.material.button.MaterialButton;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private ImageView avatar1, avatar2, avatar3, avatar4, avatar5;
    private int selectedAvatar = -1;
    private SignUpViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initViewModel();
        initUI();
        setupAvatarSelection();
        setupRegisterButton();
        observeViewModel();
        setupNavigation();
    }

    private void initViewModel() {
        SignUpViewModelFactory factory = new SignUpViewModelFactory(this);
        viewModel = new ViewModelProvider(this, factory).get(SignUpViewModel.class);
    }

    private void initUI() {
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        avatar1 = findViewById(R.id.avatar1);
        avatar2 = findViewById(R.id.avatar2);
        avatar3 = findViewById(R.id.avatar3);
        avatar4 = findViewById(R.id.avatar4);
        avatar5 = findViewById(R.id.avatar5);
    }

    private void setupAvatarSelection() {
        View.OnClickListener avatarClickListener = view -> {
            resetAvatarBorders();
            view.setBackgroundResource(R.drawable.avatar_selected_border);
            selectedAvatar = Integer.parseInt(view.getTag().toString());
        };

        avatar1.setOnClickListener(avatarClickListener);
        avatar2.setOnClickListener(avatarClickListener);
        avatar3.setOnClickListener(avatarClickListener);
        avatar4.setOnClickListener(avatarClickListener);
        avatar5.setOnClickListener(avatarClickListener);
    }

    private void setupRegisterButton() {
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            viewModel.registerUser(email, username, password, confirmPassword, selectedAvatar);
        });

    }

    private void observeViewModel() {
        viewModel.registrationSuccess.observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Registered successfully! Check your email.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        viewModel.errorMessage.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
        TextView tvHaveAccount = findViewById(R.id.tvHaveAccount);
        tvHaveAccount.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void resetAvatarBorders() {
        avatar1.setBackground(null);
        avatar2.setBackground(null);
        avatar3.setBackground(null);
        avatar4.setBackground(null);
        avatar5.setBackground(null);
    }
}
