package com.example.recipe.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.recipe.Utilities;
import com.example.recipe.activities.AddActivity;
import com.example.recipe.activities.MainActivity;
import com.example.recipe.R;
import com.example.recipe.activities.RegisterActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;


public class FragmentUser extends Fragment {

    EditText edEmail, edUsername, edJoined;
    Button btResetPassword, btSave;
    ProgressBar progressBar;
    ImageView ivAvatar;
    Uri avatarUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edEmail = view.findViewById(R.id.edEmail);
        edUsername = view.findViewById(R.id.edUsername);
        edJoined = view.findViewById(R.id.edJoined);
        btResetPassword = view.findViewById(R.id.btResetPassword);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        btSave = view.findViewById(R.id.btSave);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        FirebaseUser user = MainActivity.firebaseUser;
        edEmail.setText(user.getEmail());
        edUsername.setText(user.getDisplayName());
        Glide.with(this).load(user.getPhotoUrl()).into(ivAvatar);
        avatarUri = user.getPhotoUrl();

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                System.out.println("result code " + result.getResultCode());
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    avatarUri = data.getData();
                    System.out.println(avatarUri);
                    Glide.with(getActivity()).load(avatarUri).into(ivAvatar);
                } else {
                    avatarUri = user.getPhotoUrl();
//                            Toast.makeText(getActivity(), "No image selected", Toast.LENGTH_SHORT).show();
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

        Date joined = null;
        joined = new Date(user.getMetadata().getCreationTimestamp());
        edJoined.setText(new SimpleDateFormat("dd/MM/yyyy").format(joined));
        btResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = user.getEmail();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Send reset email");
                builder.setMessage("Are you sure you want to send an reset password email to " + email + "?");
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getActivity(), "Reset password email sent", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edUsername.getText().toString();
                if (username.isEmpty()) {
                    Toast.makeText(getActivity(), "Empty username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (avatarUri == null) {
                    Toast.makeText(getActivity(), "No avatar selected", Toast.LENGTH_SHORT).show();
                    return;
                }
                addUserInfo(username, avatarUri, MainActivity.firebaseUser);
            }
        });
    }

    private void addUserInfo(String username, Uri avatarUri, FirebaseUser user) {
        loading(true);
        String now = Utilities.getISOCurrentDateTime();
        if (!avatarUri.equals(user.getPhotoUrl())) {
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
                            Toast.makeText(getActivity(), "Saved successfully", Toast.LENGTH_SHORT).show();
                            loading(false);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            loading(false);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    loading(false);
                }
            });
        } else {
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
            user.updateProfile(profileUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(getActivity(), "Saved successfully", Toast.LENGTH_SHORT).show();
                    loading(false);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    loading(false);
                }
            });
        }

    }

    private void loading(boolean b) {
        if (b) {
            btSave.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            btSave.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }
}