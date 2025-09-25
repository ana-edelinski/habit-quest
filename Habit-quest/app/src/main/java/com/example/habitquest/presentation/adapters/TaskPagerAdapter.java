package com.example.habitquest.presentation.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.habitquest.presentation.fragments.OneTimeTasksFragment;
import com.example.habitquest.presentation.fragments.RecurringTasksFragment;

public class TaskPagerAdapter extends FragmentStateAdapter {

    public TaskPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new OneTimeTasksFragment();
        } else {
            return new RecurringTasksFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // two tabs
    }
}
