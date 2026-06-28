package com.mantao.star;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageView ivTogglePassword;
    private TextView tvRegister;
    private LinearLayout btnGoogle, btnFacebook;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sembunyikan action bar jika ada
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.login_activity);

        initViews();
        setupListeners();
    }

    // ─────────────────────────────────────────
    //  Init Views
    // ─────────────────────────────────────────

    private void initViews() {
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        tvRegister       = findViewById(R.id.tvRegister);
        btnGoogle        = findViewById(R.id.btnGoogle);
        btnFacebook      = findViewById(R.id.btnFacebook);
    }

    // ─────────────────────────────────────────
    //  Setup Listeners
    // ─────────────────────────────────────────

    private void setupListeners() {

        // Tombol Login utama
        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                performLogin(email, password);
            }
        });

        // Toggle show/hide password
        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        // Daftar Sekarang
        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                // TODO: arahkan ke RegisterActivity
                // startActivity(new Intent(this, RegisterActivity.class));
                Toast.makeText(this, "Fitur daftar segera hadir!", Toast.LENGTH_SHORT).show();
            });
        }

        // Google login
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> {
                // TODO: implementasi Google Sign-In
                Toast.makeText(this, "Login dengan Google segera hadir!", Toast.LENGTH_SHORT).show();
            });
        }

        // Facebook login
        if (btnFacebook != null) {
            btnFacebook.setOnClickListener(v -> {
                // TODO: implementasi Facebook Login
                Toast.makeText(this, "Login dengan Facebook segera hadir!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // ─────────────────────────────────────────
    //  Validasi Input
    // ─────────────────────────────────────────

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email tidak boleh kosong");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email tidak valid");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password tidak boleh kosong");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    // ─────────────────────────────────────────
    //  Proses Login
    // ─────────────────────────────────────────

    private void performLogin(String email, String password) {
        // Nonaktifkan tombol sementara agar tidak diklik dua kali
        btnLogin.setEnabled(false);
        btnLogin.setText("Memproses...");

        // ──────────────────────────────────────────────────────────────
        // TODO: Ganti blok ini dengan panggilan API sungguhan.
        //       Contoh dengan Retrofit:
        //
        //   ApiService api = RetrofitClient.getInstance().getApi();
        //   api.login(new LoginRequest(email, password))
        //      .enqueue(new Callback<LoginResponse>() {
        //          @Override public void onResponse(...) {
        //              if (response.isSuccessful()) {
        //                  saveSession(response.body().getToken());
        //                  goToMain(response.body().getUsername());
        //              } else {
        //                  showLoginError();
        //              }
        //          }
        //          @Override public void onFailure(...) { showLoginError(); }
        //      });
        //
        // Untuk sementara: simulasi login berhasil langsung
        // ──────────────────────────────────────────────────────────────

        goToMain(email.split("@")[0]); // kirim nama depan email sebagai username sementara
    }

    // ─────────────────────────────────────────
    //  Navigasi ke MainActivity
    // ─────────────────────────────────────────

    private void goToMain(String username) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

        // Kirim username ke MainActivity
        intent.putExtra("USERNAME", username);

        // Hapus back stack agar user tidak bisa kembali ke login dengan tombol back
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    // ─────────────────────────────────────────
    //  Tampilkan error login
    // ─────────────────────────────────────────

    private void showLoginError() {
        runOnUiThread(() -> {
            btnLogin.setEnabled(true);
            btnLogin.setText("Masuk Sekarang");
            Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show();
        });
    }

    // ─────────────────────────────────────────
    //  Toggle Password
    // ─────────────────────────────────────────

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Tampilkan password
            etPassword.setInputType(
                    android.text.InputType.TYPE_CLASS_TEXT |
                            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );
            ivTogglePassword.setImageResource(R.drawable.visibility);
        } else {
            // Sembunyikan password
            etPassword.setInputType(
                    android.text.InputType.TYPE_CLASS_TEXT |
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            ivTogglePassword.setImageResource(R.drawable.visibility);
        }

        // Pindahkan kursor ke akhir teks
        etPassword.setSelection(etPassword.getText().length());
    }
}