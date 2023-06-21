package com.example.recipe.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.recipe.R;
import com.example.recipe.activities.MainActivity;
import com.example.recipe.model.Recipe;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecipeRVAdapter extends RecyclerView.Adapter<RecipeRVAdapter.RecipeViewHolder> {

    private List<Recipe> list;
    private ItemListener itemListener;
    Context context;

    public RecipeRVAdapter(Context context) {
        this.list = new ArrayList<>();
        this.context = context;
    }

    public void setItemListener(ItemListener itemListener) {
        this.itemListener = itemListener;
    }

    public void setList(List<Recipe> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public Recipe getRecipe(int position) {
        return list.get(position);
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe item = list.get(position);
        Glide.with(context).load(Uri.parse(item.getImageUrl())).into(holder.img);
        holder.tvUsername.setText(item.getUname());
        Date created = new Date(item.getCreated());
        holder.tvDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(created));
        holder.tvTitle.setText(item.getTitle());
        int time = item.getCookingTime();
        holder.tvTime.setText(time == 1 ? time + " minute" : time + " minutes");
        holder.ratingBar.setRating(item.getDifficulty());

        FirebaseUser user = MainActivity.firebaseUser;
        DatabaseReference check = FirebaseDatabase.getInstance().getReference().child("favorites").child(user.getUid()).child(item.getKey());

        check.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.imgFav.setImageResource(R.drawable.ic_baseline_favorite_24);
                    holder.fav = true;
                } else {
                    holder.imgFav.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    holder.fav = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.imgFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("FAV CLicked " + item.getKey() + " - " + item.getTitle());
                if (!holder.fav) {
                    // add to fav
                    DatabaseReference newFavRef = FirebaseDatabase.getInstance().getReference().child("favorites").child(user.getUid());
                    newFavRef.child(item.getKey()).setValue(item.getTitle());
                    holder.imgFav.setImageResource(R.drawable.ic_baseline_favorite_24);
                    holder.fav = true;
                } else {
                    // remove from fav
                    DatabaseReference favRef = FirebaseDatabase.getInstance().getReference().child("favorites").child(user.getUid()).child(item.getKey());
                    favRef.removeValue();
                    holder.imgFav.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    holder.fav = false;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvUsername, tvDate, tvTitle, tvTime;
        ImageView img, imgFav;
        RatingBar ratingBar;
        boolean fav = false;

        public RecipeViewHolder(@NonNull View view) {
            super(view);
            img = view.findViewById(R.id.img);
            imgFav = view.findViewById(R.id.imgFav);
            tvUsername = view.findViewById(R.id.tvUsername);
            tvDate = view.findViewById(R.id.tvDate);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvDate = view.findViewById(R.id.tvDate);
            tvTime = view.findViewById(R.id.tvTime);
            ratingBar = view.findViewById(R.id.ratingBar);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemListener != null) {
                itemListener.ItemClick(v, getAdapterPosition());
            }
        }
    }

    public interface ItemListener {
        void ItemClick(View view, int position);
    }
}
