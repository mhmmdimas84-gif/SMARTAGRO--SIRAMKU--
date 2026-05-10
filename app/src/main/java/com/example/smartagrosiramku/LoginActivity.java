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
    private CheckBox cbCaptcha;
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
        cbCaptcha = findViewById(R.id.cbCaptcha);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
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

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Coba Login dengan Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Login sukses
                    performLogin(email);
                } else {
                    // Jika gagal login, cek apakah karena user belum terdaftar atau salah password
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "";
                    
                    if (errorMsg.contains("There is no user record") || errorMsg.contains("INVALID_LOGIN_CREDENTIALS")) {
                        // User belum ada atau password salah. Firebase Auth error messages bisa bervariasi.
                        // Berikan opsi daftar jika memang mau daftar, atau reset jika salah password.
                        new android.app.AlertDialog.Builder(this)
                            .setTitle("Login Gagal")
                            .setMessage("Akun tidak ditemukan atau password salah.\n\nPilih tindakan yang ingin dilakukan:")
                            .setPositiveButton("Daftar Baru", (dialog, which) -> {
                                if (password.length() < 6) {
                                    Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                // Daftarkan user ke Firebase
                                mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(this, taskReg -> {
                                        if (taskReg.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user != null) {
                                                String uid = user.getUid();
                                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                                                HashMap<String, Object> userData = new HashMap<>();
                                                userData.put("nama", email.split("@")[0]);
                                                userData.put("email", email);
                                                userData.put("login_perangkat", android.os.Build.MODEL);
                                                userRef.setValue(userData);
                                            }
                                            Toast.makeText(this, "Akun berhasil dibuat! Silakan login kembali.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, "Gagal mendaftar: " + taskReg.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            })
                            .setNeutralButton("Reset Password", (dialog, which) -> {
                                mAuth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(taskReset -> {
                                        if (taskReset.isSuccessful()) {
                                            Toast.makeText(this, "Email reset password telah dikirim ke " + email, Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(this, "Gagal mengirim email: " + taskReset.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                    } else {
                         Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
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
