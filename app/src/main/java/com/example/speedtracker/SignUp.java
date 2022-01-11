package com.example.speedtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText mFullName,mEmail,mPhone,mPassword,mPassword1;
    Button mSignUp;
    TextView mSignIn;
    FirebaseFirestore mStore;
    String userId;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mPhone = findViewById(R.id.phone);
        mPassword = findViewById(R.id.password);
        mPassword1 = findViewById(R.id.password1);
        mSignUp = findViewById(R.id.signUp);
        mSignIn = findViewById(R.id.signIn);
        progressBar = findViewById(R.id.progressBar);

        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(SignUp.this,MapsActivity.class));
            finish();
        }
        progressBar.setVisibility(View.GONE);

        mSignUp.setOnClickListener(new View.OnClickListener() {
            private String tag;

            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String password1 = mPassword1.getText().toString().trim();
                final String fullName = mFullName.getText().toString();
                final String phone    = mPhone.getText().toString();

                if(TextUtils.isEmpty(fullName)){
                    mFullName.setError("Name is Required.");
                    return;
                }

                if(TextUtils.isEmpty(phone)){
                    mPhone.setError("Phone Number is Required.");
                    return;
                }

                if(phone.length() != 10 ){
                    mPhone.setError("Phone Number Must be 10 Characters");
                    return;
                }

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required.");
                    return;
                }
                if(TextUtils.isEmpty(password1)){
                    mPassword1.setError("Password is Required.");
                    return;
                }

                if(password.length() < 6){
                    mPassword.setError("Password Must be >= 6 Characters");
                    return;
                }
                if(password1.length() < 6){
                    mPassword1.setError("Password Must be >= 6 Characters");
                    return;
                }
                if (!password.equals(password1)){
                    mPassword.setError("Enter same password !");
                    mPassword1.setError("Enter same password !");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SignUp.this, "User Created.", Toast.LENGTH_SHORT).show();
                            userId = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = mStore.collection("users").document(userId);
                            Map<String,Object> user = new HashMap<>();
                            user.put("fullName",fullName);
                            user.put("email",email);
                            user.put("phone",phone);
                            user.put("admin",0);
                            user.put("password", password);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(tag,"onSuccess: user Profile is created for "+ userId);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(tag, "onFailure: " + e.toString());
                                }
                            });

                            progressBar.setVisibility(View.GONE);

                            Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        }else {
                            Toast.makeText(SignUp.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}






