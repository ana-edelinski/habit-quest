package com.example.habitquest.presentation.viewmodels.factories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.BattleStatsRepository;
import com.example.habitquest.data.repositories.BossRepository;
import com.example.habitquest.data.repositories.EquipmentRepository;
import com.example.habitquest.data.repositories.TaskOccurrenceRepository;
import com.example.habitquest.data.repositories.TaskRepository;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.data.repositories.UserXpLogRepository;
import com.example.habitquest.domain.managers.StagePerformanceCalculator;
import com.example.habitquest.domain.model.TaskOccurrence;
import com.example.habitquest.presentation.viewmodels.AccountViewModel;
import com.example.habitquest.presentation.viewmodels.BossFightViewModel;

public class BossFightViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public BossFightViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BossFightViewModel.class)) {
            BossRepository repo = new BossRepository(context);
            UserRepository userRepo = new UserRepository(context);
            UserXpLogRepository xpRepo = new UserXpLogRepository(context);
            TaskRepository taskRepo = new TaskRepository(context);
            TaskOccurrenceRepository occurrenceRepository = new TaskOccurrenceRepository(context);
            BattleStatsRepository battleStatsRepository = new BattleStatsRepository(context);
            EquipmentRepository equipmentRepository = new EquipmentRepository();
            StagePerformanceCalculator stagePerformanceCalculator = new StagePerformanceCalculator(userRepo,xpRepo, taskRepo, occurrenceRepository);
            return (T) new BossFightViewModel(new AppPreferences(context), repo, userRepo, battleStatsRepository, equipmentRepository, stagePerformanceCalculator );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
