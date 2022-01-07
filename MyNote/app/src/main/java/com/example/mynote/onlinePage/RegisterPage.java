package com.example.mynote.onlinePage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mynote.R;
import com.example.mynote.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class RegisterPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText daftarNama, daftarEmail, daftarPassword;
    Button daftarBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        // mencegah items terdorong naik ketika keyboard muncul
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // membuat app fullscreen, dengan taskbar on (transparent)
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        // membuat icon color taskbar menyesuaikan dengan background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        mAuth = FirebaseAuth.getInstance();

        daftarNama = findViewById(R.id.daftarAkunNama);
        daftarEmail = findViewById(R.id.daftarAkunEmail);
        daftarPassword = findViewById(R.id.daftarAkunPass);

        daftarBtn = findViewById(R.id.btnDaftar);
        daftarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void register(){
        String nama = daftarNama.getText().toString().trim();
        String email = daftarEmail.getText().toString().trim();
        String pass = daftarPassword.getText().toString().trim();

        if (nama.isEmpty()){
            daftarNama.setError("Nama tidak boleh kosong");
            daftarNama.requestFocus();
            return;
        }

        if (email.isEmpty()){
            daftarEmail.setError("Email tidak boleh kosong");
            daftarEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            daftarEmail.setError("Masukan alamat email yg valid");
            daftarEmail.requestFocus();
            return;
        }

        if (pass.isEmpty()){
            daftarPassword.setError("Password tidak boleh kosong");
            daftarPassword.requestFocus();
            return;
        }

        if (pass.length() < 5){
            daftarPassword.setError("Password minimal 6 karakter");
            daftarPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            User user = new User(nama, email, pass);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(RegisterPage.this, "Akun berhasil dibuat", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(RegisterPage.this, "Akun gagal dibuat (1)", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(RegisterPage.this, "Akun gagal dibuat (2)", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        String message = e.getMessage();
                        Toast.makeText(RegisterPage.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}