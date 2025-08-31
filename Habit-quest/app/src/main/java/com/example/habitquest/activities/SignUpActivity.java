package com.example.habitquest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habitquest.R;
import com.google.android.material.button.MaterialButton;

public class SignUpActivity extends AppCompatActivity {

    EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    ImageView avatar1, avatar2, avatar3, avatar4, avatar5;
    int selectedAvatar = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        avatar1 = findViewById(R.id.avatar1);
        avatar2 = findViewById(R.id.avatar2);
        avatar3 = findViewById(R.id.avatar3);
        avatar4 = findViewById(R.id.avatar4);
        avatar5 = findViewById(R.id.avatar5);

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

        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedAvatar == -1) {
                Toast.makeText(this, "Please select an avatar", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO
            Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        TextView tvHaveAccount = findViewById(R.id.tvHaveAccount);
        tvHaveAccount.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
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
