package com.example.recipe.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe.R;
import com.example.recipe.activities.MainActivity;
import com.example.recipe.activities.RecipeDetailActivity;
import com.example.recipe.adapter.RecipeRVAdapter;
import com.example.recipe.model.Recipe;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class FragmentFavorite extends Fragment implements RecipeRVAdapter.ItemListener {
    RecipeRVAdapter adapter;
    SearchView searchView;
    Spinner sortSpinner;
    RecyclerView recyclerView;
    List<String> favList;
    List<Recipe> recipeList;
    DatabaseReference favRef;
    DatabaseReference recipeRef;
    String sortingOption;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);
        sortSpinner = view.findViewById(R.id.spinner);
        sortingOption = (String) sortSpinner.getSelectedItem();

        adapter = new RecipeRVAdapter(getActivity());
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.setItemListener(this);

        favList = new ArrayList<>();
        recipeList = new ArrayList<>();

        FirebaseUser user = MainActivity.firebaseUser;
        favRef = FirebaseDatabase.getInstance().getReference().child("favorites").child(user.getUid());

        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                System.out.println("FAV CHANGED");
                favList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String recipeKey = itemSnapshot.getKey();
                    favList.add(recipeKey);
                }
                changeData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Recipe> newList = new ArrayList<>();
                for (Recipe r : recipeList) {
                    if (r.getTitle().toLowerCase().contains(newText.toLowerCase())) {
                        newList.add(r);
                    }
                }
                adapter.setList(newList);
                return true;
            }
        });

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortingOption = (String) parent.getItemAtPosition(position);
                doSort(sortingOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void doSort(String sortingOption) {
        switch (sortingOption) {
            case "date ascending":
                recipeList.sort(new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe o1, Recipe o2) {
                        if (o1.getCreated() > o2.getCreated()) return 1;
                        else if (o1.getCreated() < o2.getCreated()) return -1;
                        return 0;
                    }
                });
                break;
            case "date descending":
                recipeList.sort(new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe o1, Recipe o2) {
                        if (o1.getCreated() > o2.getCreated()) return -1;
                        else if (o1.getCreated() < o2.getCreated()) return 1;
                        return 0;
                    }
                });
                break;
            case "cooking time ascending":
                recipeList.sort(new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe o1, Recipe o2) {
                        if (o1.getCookingTime() > o2.getCookingTime()) return 1;
                        else if (o1.getCookingTime() < o2.getCookingTime()) return -1;
                        return 0;
                    }
                });
                break;
            case "cooking time descending":
                recipeList.sort(new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe o1, Recipe o2) {
                        if (o1.getCookingTime() > o2.getCookingTime()) return -1;
                        else if (o1.getCookingTime() < o2.getCookingTime()) return 1;
                        return 0;
                    }
                });
                break;
            case "difficulty ascending":
                recipeList.sort(new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe o1, Recipe o2) {
                        if (o1.getDifficulty() > o2.getDifficulty()) return 1;
                        else if (o1.getDifficulty() < o2.getDifficulty()) return -1;
                        return 0;
                    }
                });
                break;
            case "difficulty descending":
                recipeList.sort(new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe o1, Recipe o2) {
                        if (o1.getDifficulty() > o2.getDifficulty()) return -1;
                        else if (o1.getDifficulty() < o2.getDifficulty()) return 1;
                        return 0;
                    }
                });
                break;
            default:
                break;
        }
        recyclerView.setAdapter(null);
        adapter = new RecipeRVAdapter(getActivity());
        adapter.setItemListener(this);
        recyclerView.setAdapter(adapter);
        adapter.setList(recipeList);
    }

    private void changeData() {
        recipeList.clear();
        adapter.setList(recipeList);
        for (String key : favList) {
            DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference().child("recipes").child(key);
            recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Recipe recipe = snapshot.getValue(Recipe.class);
                        recipeList.add(recipe);
                        doSort(sortingOption);
                    }
                    adapter.setList(recipeList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }


    @Override
    public void ItemClick(View view, int position) {
        Recipe recipe = adapter.getRecipe(position);
        System.out.println(recipe.getTitle());
        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
        intent.putExtra("recipe", recipe);
        startActivity(intent);
    }
}