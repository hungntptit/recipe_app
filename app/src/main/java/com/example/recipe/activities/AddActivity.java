package com.example.recipe.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.recipe.R;
import com.example.recipe.Utilities;
import com.example.recipe.model.Comment;
import com.example.recipe.model.Recipe;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class AddActivity extends AppCompatActivity {

    ImageView img;
    EditText edTitle, edDescription, edTime, edIngr, edSteps;
    RatingBar ratingBar;
    String imageUrl;
    Uri uri;
    final static int galleryPick = 1;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference recipesRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("New Recipe");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_clear_24);

        firebaseDatabase = FirebaseDatabase.getInstance();
        recipesRef = firebaseDatabase.getReference("recipes");

        initViews();

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        System.out.println("result code " + result.getResultCode());
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            uri = data.getData();
                            System.out.println(uri);
//                            img.setImageURI(uri);
                            Glide.with(getApplicationContext()).load(uri).into(img);
                        } else {
                            Toast.makeText(AddActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imagePicker = new Intent(Intent.ACTION_GET_CONTENT);
                imagePicker.setType("image/*");
                activityResultLauncher.launch(imagePicker);
            }
        });
    }

    private void initViews() {
        img = findViewById(R.id.uploadImg);
        edTitle = findViewById(R.id.edTitle);
        edDescription = findViewById(R.id.edDesrciption);
        edTime = findViewById(R.id.edTime);
        edIngr = findViewById(R.id.edIngr);
        edSteps = findViewById(R.id.edSteps);
        ratingBar = findViewById(R.id.ratingBar);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.mDone:
                // save
                saveData();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
        builder.setTitle("Confirm discard changes");
        builder.setMessage("Are you sure you want to discard changes?");
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done_menu, menu);
        return true;
    }

    private void saveData() {
        String title = edTitle.getText().toString();
        String desc = edDescription.getText().toString();
        String stime = edTime.getText().toString();
        String ingr = edIngr.getText().toString();
        String steps = edSteps.getText().toString();
        float difficulty = ratingBar.getRating();
        String now = Utilities.getISOCurrentDateTime();

        if (title.isEmpty() || desc.isEmpty() || stime.isEmpty() || ingr.isEmpty() || steps.isEmpty()) {
            Toast.makeText(AddActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uri == null) {
            Toast.makeText(AddActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("recipe_images")
                .child(now + uri.getLastPathSegment());
        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        int time = Integer.parseInt(stime);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete()) ;
                Uri urlImage = uriTask.getResult();
                imageUrl = urlImage.toString();

                Toast.makeText(AddActivity.this, "Upload successfully", Toast.LENGTH_SHORT).show();

                //
                FirebaseUser user = MainActivity.firebaseUser;
                Recipe recipe = new Recipe(title, desc, imageUrl, ingr, steps, time, difficulty, new Date().getTime(), new HashMap<String, Comment>(), user.getUid(), user.getDisplayName(), user.getPhotoUrl().toString());

                DatabaseReference newRecipeRef = recipesRef.push();
                recipe.setKey(newRecipeRef.getKey());
                newRecipeRef.setValue(recipe).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddActivity.this, "Recipe saved", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                //

                dialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

    }
}