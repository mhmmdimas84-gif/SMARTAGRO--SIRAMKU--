package com.example.smartagrosiramku;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.view.WindowManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private CheckBox cbCaptcha;
    private DatabaseHelper db;
    private SharedPreferences sharedPreferences;

    // Firebase variables
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );
        
        sharedPreferences = getSharedPreferences("SiramkuPrefs", MODE_PRIVATE);
        
        // Cek jika user sudah login sebelumnya (validasi dua lapis: Firebase + SharedPreferences)
        FirebaseAuth authCheck = FirebaseAuth.getInstance();
        if (authCheck.getCurrentUser() != null && sharedPreferences.getBoolean("is_logged_in", false)) {
            Intent intent = new Intent(LoginActivity.this, Dashboard.class);
            startActivity(intent);
            finish();
            return;
        } else {
            // Jika Firebase session habis, clear SharedPreferences supaya tidak bypass login
            sharedPreferences.edit().putBoolean("is_logged_in", false).apply();
        }

        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        initViews();
        // setCurrentTime(); // Removed because tvTime is not in layout
        setupListeners();
        
        // Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Konfigurasi Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) 
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnMasuk);
        tvRegister = findViewById(R.id.tvDaftar);
        cbCaptcha = findViewById(R.id.cbCaptcha);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Masukkan email dan password lalu klik Masuk untuk mendaftar otomatis", Toast.LENGTH_LONG).show();
            }
        });

        Button btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(v -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Login Google Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                        
                        HashMap<String, Object> userData = new HashMap<>();
                        userData.put("nama", user.getDisplayName());
                        userData.put("email", user.getEmail());
                        userData.put("login_perangkat", android.os.Build.MODEL);
                        
                        userRef.setValue(userData);
                        
                        performLogin(user.getEmail());
                    }
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(this, "Autentikasi Firebase Gagal: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbCaptcha.isChecked()) {
            Toast.makeText(this, "Silakan centang 'Saya bukan robot'", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi format email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cek apakah email sudah terdaftar
        if (!db.checkUserExists(email)) {
            // Email belum terdaftar — tawari untuk mendaftar
            new android.app.AlertDialog.Builder(this)
                .setTitle("Akun Tidak Ditemukan")
                .setMessage("Email \"" + email + "\" belum terdaftar.\n\nApakah Anda ingin mendaftar dengan email dan password ini?")
                .setPositiveButton("Daftar Sekarang", (dialog, which) -> {
                    if (password.length() < 6) {
                        Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String name = email.split("@")[0];
                    db.registerUser(name, email, password);
                    Toast.makeText(this, "Akun berhasil dibuat! Silakan login.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
            return;
        }

        // Email terdaftar — cek password
        if (db.checkLogin(email, password)) {
            performLogin(email);
        } else {
            // Password salah — tampilkan dialog lupa password
            new android.app.AlertDialog.Builder(this)
                .setTitle("Password Salah")
                .setMessage("Password yang Anda masukkan tidak sesuai.\n\nJika lupa password, klik \"Reset Password\" untuk membuat password baru.")
                .setPositiveButton("Reset Password", (dialog, which) -> {
                    // Dialog untuk ganti password
                    android.widget.EditText etNewPass = new android.widget.EditText(this);
                    etNewPass.setHint("Masukkan password baru (min. 6 karakter)");
                    etNewPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    new android.app.AlertDialog.Builder(this)
                        .setTitle("Buat Password Baru")
                        .setView(etNewPass)
                        .setPositiveButton("Simpan", (d2, w2) -> {
                            String newPass = etNewPass.getText().toString().trim();
                            if (newPass.length() < 6) {
                                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            db.updatePassword(email, newPass);
                            Toast.makeText(this, "Password berhasil diubah! Silakan login.", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Batal", null)
                        .show();
                })
                .setNegativeButton("Coba Lagi", null)
                .show();
        }
    }

    private void performLogin(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_email", email);
        editor.putBoolean("is_logged_in", true);
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, Dashboard.class);
        startActivity(intent);
        finish();
    }
}
