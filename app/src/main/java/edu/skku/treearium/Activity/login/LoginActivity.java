package edu.skku.treearium.Activity.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.skku.treearium.Activity.MainActivity;
import edu.skku.treearium.R;

public class LoginActivity extends AppCompatActivity {
    EditText id, password;
    Button loginbtn;
    TextView register;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateLisnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mFirebaseAuth = FirebaseAuth.getInstance();
        id = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        loginbtn = findViewById(R.id.btn_login);
        register = findViewById(R.id.toRegister);


        mAuthStateLisnter = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else
                    Toast.makeText(LoginActivity.this, "Please Login", Toast.LENGTH_SHORT).show();
            }
        };


        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = id.getText().toString();
                String pswrd = password.getText().toString();
                if (email.isEmpty()) {
                    id.setError("Please insert Email");
                    id.requestFocus();
                } else if (pswrd.isEmpty()) {
                    password.setError("Please insert Password");
                    password.requestFocus();
                } else if (!(pswrd.isEmpty() && email.isEmpty())) {
                    mFirebaseAuth.signInWithEmailAndPassword(email, pswrd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Login Failed, Try again!", Toast.LENGTH_SHORT).show();
                            } else {
                                loginbtn.setEnabled(false);
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            }
                        }
                    });
                } else
                    Toast.makeText(LoginActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateLisnter);
    }


    @Override
    protected void onResume() {
        super.onResume();

        loginbtn.setEnabled(true);
    }

}