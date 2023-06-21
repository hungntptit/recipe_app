package com.example.recipe.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipe.R;
import com.example.recipe.activities.MainActivity;
import com.example.recipe.activities.RecipeDetailActivity;
import com.example.recipe.model.Comment;
import com.example.recipe.model.Recipe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentRVAdapter extends RecyclerView.Adapter<CommentRVAdapter.CommentViewHolder> {
    private List<Comment> list;
    private CommentItemListener itemListener;
    Context context;
    String recipeKey;

    public CommentRVAdapter(Context context, String recipeKey) {
        this.list = new ArrayList<>();
        this.context = context;
        this.recipeKey = recipeKey;
    }

    public void setItemListener(CommentItemListener itemListener) {
        this.itemListener = itemListener;
    }

    public void setList(List<Comment> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public Comment getRecipe(int position) {
        return list.get(position);
    }

    @NonNull
    @Override
    public CommentRVAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment item = list.get(position);
        Glide.with(context).load(Uri.parse(item.getUphoto())).into(holder.ivAvatar);
        holder.tvUsername.setText(item.getUname());
        Date created = new Date(item.getCreated());
        holder.tvTime.setText(new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(created));
        holder.tvContent.setText(item.getContent());
        if (MainActivity.firebaseUser.getUid().equals(item.getUid())) {
            holder.btDeleteComment.setVisibility(View.VISIBLE);
            holder.btDeleteComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteComment(item);
                }
            });
        }
    }

    private void deleteComment(Comment comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete confirm");
        builder.setMessage("Are you sure you want to delete this comment?");
        builder.setIcon(R.drawable.ic_baseline_delete_24);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeKey).child("comments").child(comment.getKey());
                commentRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvUsername, tvTime, tvContent;
        ImageView ivAvatar;
        ImageButton btDeleteComment;

        public CommentViewHolder(@NonNull View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.ivCommentAvatar);
            tvUsername = view.findViewById(R.id.tvCommentUsername);
            tvTime = view.findViewById(R.id.tvCommentTime);
            tvContent = view.findViewById(R.id.tvCommentContent);
            btDeleteComment = view.findViewById(R.id.btDeleteComment);
//            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
//            if (itemListener != null) {
//                itemListener.ItemClick(v, getAdapterPosition());
//            }
//            if (v.getId() == R.id.btDeleteComment) {
//                Toast.makeText(context, "Button delete comment", Toast.LENGTH_SHORT).show();
//            }
        }
    }

    public interface CommentItemListener {
        void ItemClick(View view, int position);
    }
}