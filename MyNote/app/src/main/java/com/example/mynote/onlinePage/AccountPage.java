package com.example.mynote.onlinePage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mynote.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class AccountPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
//    private FirebaseAuth.AuthStateListener mAuthListener;
    TextView buatAkun;
    EditText masukEmail, masukPass;
    Button btnMasuk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_page);

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

        // pengecekan jika user sudah login
        if (mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), OnlineSpace.class));
            finish();
        }

        buatAkun = findViewById(R.id.buatAkunBaru);
        buatAkun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterPage.class));
            }
        });

        masukEmail = findViewById(R.id.masukEmail);
        masukPass = findViewById(R.id.masukPassword);

        btnMasuk = findViewById(R.id.btnMasuk);
        btnMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAkun();
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

    private void loginAkun(){
        String email = masukEmail.getText().toString().trim();
        String pass = masukPass.getText().toString().trim();

        if (email.isEmpty()){
            masukEmail.setError("Masukan Email");
            masukEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            masukEmail.setError("Masukan Email yang valid");
            masukEmail.requestFocus();
            return;
        }

        if (pass.isEmpty()){
            masukPass.setError("Masukan Password");
            masukPass.requestFocus();
            return;
        }
        if (pass.length() < 5){
            masukPass.setError("Password minimal 6 karakter");
            masukPass.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            startActivity(new Intent(getApplicationContext(), OnlineSpace.class));
                            finish();
                        }
                        else{
                            Toast.makeText(AccountPage.this, "User tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        String message = e.getMessage();
                        Toast.makeText(AccountPage.this, message, Toast.LENGTH_SHORT).show();
                    }
                });

    }
}