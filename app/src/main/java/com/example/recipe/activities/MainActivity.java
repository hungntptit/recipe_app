package com.example.recipe.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.recipe.R;
import com.example.recipe.adapter.AdapterViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    ViewPager viewPager;
    FloatingActionButton fabAdd;
    ExtendedFloatingActionButton fabLogout;

    AdapterViewPager adapterViewPager;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference userRef;

    public static FirebaseUser firebaseUser;

    public FloatingActionButton getFabAdd() {
        return fabAdd;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        viewPager = findViewById(R.id.viewPagerBottom);
        fabAdd = findViewById(R.id.fabAdd);
        fabLogout = findViewById(R.id.fabLogout);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });

        fabLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        fabLogout.hide();


        adapterViewPager = new AdapterViewPager(getSupportFragmentManager(), 3);
        viewPager.setAdapter(adapterViewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        fabAdd.show();
                        fabLogout.hide();
                        bottomNavigationView.getMenu().findItem(R.id.mHome).setChecked(true);
                        break;
//                    case 1:
//                        fabAdd.hide();
//                        fabLogout.hide();
//                        bottomNavigationView.getMenu().findItem(R.id.mSearch).setChecked(true);
//                        break;
                    case 1:
                        fabAdd.hide();
                        fabLogout.hide();
                        bottomNavigationView.getMenu().findItem(R.id.mFavorite).setChecked(true);
                        break;
                    case 2:
                        fabAdd.hide();
                        fabLogout.show();
                        bottomNavigationView.getMenu().findItem(R.id.mUser).setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mHome:
                        viewPager.setCurrentItem(0);
                        break;
//                    case R.id.mSearch:
//                        viewPager.setCurrentItem(1);
//                        break;
                    case R.id.mFavorite:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.mUser:
                        viewPager.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });

    }

}