package com.example.recipe.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.recipe.R;
import com.example.recipe.adapter.CommentRVAdapter;
import com.example.recipe.model.Comment;
import com.example.recipe.model.Recipe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity implements CommentRVAdapter.CommentItemListener {
    Recipe recipe;
    ImageView ivRecipe, ivAuthor, ivCurrentAvatar;
    TextView tvTitle, tvDescription, tvTime, tvIngr, tvSteps, tvAuthor, tvDate;
    EditText edComment;
    ImageButton btSend;
    RatingBar ratingBar;
    RecyclerView rvComment;
    CommentRVAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Recipe Details");
        actionBar.setDisplayHomeAsUpEnabled(true);

        initViews();

        Intent intent = getIntent();
        recipe = (Recipe) intent.getSerializableExtra("recipe");
        String key = recipe.getKey();

        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference().child("recipes").child(key);
        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipe = snapshot.getValue(Recipe.class);
                setViews();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("recipes").child(key).child("comments");
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> list = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Comment comment = itemSnapshot.getValue(Comment.class);
                    comment.setKey(itemSnapshot.getKey());
                    list.add(comment);
                }
                adapter.setList(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        setViews();

    }

    private void setViews() {
        Glide.with(getApplicationContext()).load(recipe.getUphoto()).into(ivAuthor);
        tvAuthor.setText(recipe.getUname());
        Date created = new Date(recipe.getCreated());
        tvDate.setText(new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(created));

        tvTitle.setText(recipe.getTitle());
        tvDescription.setText(recipe.getDescription());
        int time = recipe.getCookingTime();
        tvTime.setText(time == 1 ? time + " minute" : time + " minutes");
        tvIngr.setText(recipe.getIngredients());
        tvSteps.setText(recipe.getSteps());
        ratingBar.setRating(recipe.getDifficulty());
        Glide.with(getApplicationContext()).load(recipe.getImageUrl()).into(ivRecipe);

        Glide.with(getApplicationContext()).load(MainActivity.firebaseUser.getPhotoUrl()).into(ivCurrentAvatar);

        adapter = new CommentRVAdapter(RecipeDetailActivity.this, recipe.getKey());
        LinearLayoutManager manager = new LinearLayoutManager(RecipeDetailActivity.this, RecyclerView.VERTICAL, false);
        rvComment.setLayoutManager(manager);
        rvComment.setAdapter(adapter);
        adapter.setItemListener(this);
        adapter.setList(recipe.getListComments());

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment_content = edComment.getText().toString();
                if (comment_content.isEmpty()) {
                    Toast.makeText(RecipeDetailActivity.this, "Enter comment", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseUser user = MainActivity.firebaseUser;
                String uid = user.getUid();
                String uname = user.getDisplayName();
                String uphoto = user.getPhotoUrl().toString();
                Comment comment = new Comment(comment_content, new Date().getTime(), uid, uname, uphoto);

                DatabaseReference newCommentRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipe.getKey()).child("comments").push();
                String commentKey = newCommentRef.getKey();
                newCommentRef.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(RecipeDetailActivity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                        edComment.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RecipeDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initViews() {
        ivAuthor = findViewById(R.id.ivAuthorAvatar);
        tvAuthor = findViewById(R.id.tvAuthorName);
        tvDate = findViewById(R.id.tvDate);

        ivRecipe = findViewById(R.id.img);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvTime = findViewById(R.id.tvTime);
        tvIngr = findViewById(R.id.tvIngr);
        tvSteps = findViewById(R.id.tvSteps);
        ratingBar = findViewById(R.id.ratingBar);
        ratingBar.setIsIndicator(true);

        ivCurrentAvatar = findViewById(R.id.ivCurrentAvatar);
        edComment = findViewById(R.id.edComment);
        btSend = findViewById(R.id.btSend);
        rvComment = findViewById(R.id.rvComment);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.mEdit:
                edit();
                return true;
            case R.id.mDelete:
                delete();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void edit() {
        Intent intent = new Intent(RecipeDetailActivity.this, EditActivity.class);
        intent.putExtra("recipe", recipe);
        startActivity(intent);
//        finish();
    }

    private void delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeDetailActivity.this);
        builder.setTitle("Delete confirm");
        builder.setMessage("Are you sure you want to delete this recipe?");
        builder.setIcon(R.drawable.ic_baseline_delete_24);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteRecipe();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteRecipe() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("recipes").child(recipe.getKey());
        databaseReference.removeValue();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(recipe.getImageUrl());
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                databaseReference.child(recipe.getKey()).removeValue();
                Toast.makeText(RecipeDetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                finish();
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        FirebaseUser user = MainActivity.firebaseUser;
        if (user.getUid().equals(recipe.getUid()))
            getMenuInflater().inflate(R.menu.update_delete_menu, menu);
        return true;
    }

    @Override
    public void ItemClick(View view, int position) {

    }
}