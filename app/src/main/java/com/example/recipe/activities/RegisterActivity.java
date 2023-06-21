package com.example.recipe.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.recipe.R;
import com.example.recipe.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {
    EditText edEmail, edUsername, edPassword, edCPassword;
    Button btRegister;
    ProgressBar progressBar;
    ImageView ivAvatar;

    Uri avatarUri;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Register");
        actionBar.setDisplayHomeAsUpEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference().child("users");
        initViews();
    }

    private void initViews() {
        edEmail = findViewById(R.id.edEmail);
        edUsername = findViewById(R.id.edUsername);
        edPassword = findViewById(R.id.edPassword);
        edCPassword = findViewById(R.id.edCPassword);
        btRegister = findViewById(R.id.btRegister);
        progressBar = findViewById(R.id.progressBar);
        ivAvatar = findViewById(R.id.ivAvatar);
        progressBar.setVisibility(View.GONE);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        System.out.println("result code " + result.getResultCode());
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            avatarUri = data.getData();
                            System.out.println(avatarUri);
//                            img.setImageURI(uri);
                            Glide.with(getApplicationContext()).load(avatarUri).into(ivAvatar);
                        } else {
                            Toast.makeText(RegisterActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imagePicker = new Intent(Intent.ACTION_GET_CONTENT);
                imagePicker.setType("image/*");
                activityResultLauncher.launch(imagePicker);
            }
        });

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading(true);
                String email = edEmail.getText().toString();
                String username = edUsername.getText().toString();
                String password = edPassword.getText().toString();
                String cpassword = edCPassword.getText().toString();
                if (email.isEmpty() || username.isEmpty() || password.isEmpty() || cpassword.isEmpty() || avatarUri == null) {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    loading(false);
                } else if (!password.equals(cpassword)) {
                    Toast.makeText(RegisterActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
                    loading(false);
                } else {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            addUserInfo(username, avatarUri, firebaseAuth.getCurrentUser());
                            loading(false);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            loading(false);
                        }
                    });
                }

            }
        });
    }

    private void addUserInfo(String username, Uri avatarUri, FirebaseUser user) {
        String now = Utilities.getISOCurrentDateTime();
        StorageReference photosRef = FirebaseStorage.getInstance().getReference().child("users_photos").child(now + avatarUri.getLastPathSegment());
        photosRef.putFile(avatarUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete()) ;
                Uri urlImage = uriTask.getResult();

                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(username).setPhotoUri(urlImage).build();
                user.updateProfile(profileUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(RegisterActivity.this, "Register successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loading(boolean b) {
        if (b) {
            btRegister.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            btRegister.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }
}