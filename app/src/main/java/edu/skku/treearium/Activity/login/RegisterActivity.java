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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import edu.skku.treearium.Activity.MainActivity;
import edu.skku.treearium.R;

public class RegisterActivity extends AppCompatActivity {
        EditText id, password, repassword,fullname;
        Button registerbtn;
        TextView login;
        FirebaseAuth mFirebaseAuth;
        FirebaseFirestore fstore;
        String userID;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register);
            mFirebaseAuth = FirebaseAuth.getInstance();
            fstore = FirebaseFirestore.getInstance();
            id= findViewById(R.id.rt_email);
            password=findViewById(R.id.rt_password);
            repassword=findViewById(R.id.rt_repassword);
            registerbtn = findViewById(R.id.btn_register);
            login = findViewById(R.id.toLogin);
            fullname=findViewById(R.id.rt_name);
            registerbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String email = id.getText().toString();
                    String pswrd = password.getText().toString();
                    String repswrd = repassword.getText().toString();
                    String fullName = fullname.getText().toString();
                    if(email.isEmpty()){
                        id.setError("Please insert Email");
                        id.requestFocus();
                    }
                    else if(pswrd.isEmpty()){
                        password.setError("Please insert Password");
                        password.requestFocus();
                    }
                    else if(repswrd.isEmpty()){
                        repassword.setError("Please insert Password");
                        repassword.requestFocus();
                    }
                    else if (!(pswrd.isEmpty()&&email.isEmpty())){
                        if(!(password.getText().toString().equals(repassword.getText().toString()))){
                            Toast.makeText(RegisterActivity.this, "Retype password different", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            mFirebaseAuth.createUserWithEmailAndPassword(email, pswrd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        userID= mFirebaseAuth.getCurrentUser().getUid();
                                        DocumentReference documentReference = fstore.collection("users").document(userID);
                                        Map<String,Object> user = new HashMap<>();
                                        user.put("fName",fullName);
                                        user.put("email",email);
                                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void aVoid) {
                                               registerbtn.setEnabled(false);                                           }
                                       });
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    }
                                }
                            });
                        }
                    }
                    else
                        Toast.makeText(RegisterActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                }
            });
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                }
            });


        }
    @Override
    protected void onResume() {
        super.onResume();

        registerbtn.setEnabled(true);
    }
    }