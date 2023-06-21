package com.example.recipe.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.recipe.fragment.FragmentFavorite;
import com.example.recipe.fragment.FragmentHome;
import com.example.recipe.fragment.FragmentUser;

public class AdapterViewPager extends FragmentStatePagerAdapter {

    private int numPage;

    public AdapterViewPager(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        this.numPage = behavior;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentHome();
//            case 1:
//                return new FragmentSearch();
            case 1:
                return new FragmentFavorite();
            case 2:
                return new FragmentUser();
        }
        return new FragmentHome();
    }

    @Override
    public int getCount() {
        return numPage;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Home";
//            case 1:
//                return "Search";
            case 1:
                return "Favorite";
            case 2:
                return "User";
        }
        return super.getPageTitle(position);
    }
}
