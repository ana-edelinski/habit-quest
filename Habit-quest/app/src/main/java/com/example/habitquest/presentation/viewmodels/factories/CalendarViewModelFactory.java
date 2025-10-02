package com.example.habitquest.presentation.viewmodels.factories;



import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.TaskRepository;
import com.example.habitquest.data.repositories.CategoryRepository;
import com.example.habitquest.data.repositories.TaskOccurrenceRepository;
import com.example.habitquest.presentation.viewmodels.CalendarViewModel;

public class CalendarViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public CalendarViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CalendarViewModel.class)) {
            TaskRepository taskRepo = new TaskRepository(context);
            CategoryRepository categoryRepo = new CategoryRepository(context);
            TaskOccurrenceRepository occurrenceRepo = new TaskOccurrenceRepository(context);
            return (T) new CalendarViewModel(new AppPreferences(context),
                    taskRepo,
                    categoryRepo,
                    occurrenceRepo
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

