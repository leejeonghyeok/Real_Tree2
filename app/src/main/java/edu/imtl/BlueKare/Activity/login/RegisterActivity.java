package edu.imtl.BlueKare.Activity.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
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
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import edu.imtl.BlueKare.R;

public class RegisterActivity extends AppCompatActivity {
        EditText team, id, password, repassword,fullname;
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
            team = findViewById(R.id.rt_team);
            registerbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String teamname = team.getText().toString();
                    String email = id.getText().toString();
                    String pswrd = password.getText().toString();
                    String repswrd = repassword.getText().toString();
                    String fullName = fullname.getText().toString();

                    if(teamname.isEmpty()){
                        id.setError("Please insert Team name");
                        id.requestFocus();
                    }
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
                                    registerbtn.setEnabled(false);
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                    } else {

                                        userID= mFirebaseAuth.getCurrentUser().getUid();
                                        DocumentReference users = fstore.collection("users").document(userID);
                                        DocumentReference Teammate = fstore.collection("Team").document(teamname).collection("Teammate").document(fullName);
                                        Map<String,Object> user = new HashMap<>();
                                        Map<String,Object> userids = new HashMap<>();
                                        userids.put("userID",userID);
                                        user.put("userID", userID);
                                        user.put("fName",fullName);
                                        user.put("email",email);
                                        user.put("Team",teamname);
                                        user.put("IsAdmin",false);
                                        user.put("IsWritable",true);
                                        users.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void aVoid) {
                                           }
                                       });
                                        Teammate.set(userids, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                            }
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