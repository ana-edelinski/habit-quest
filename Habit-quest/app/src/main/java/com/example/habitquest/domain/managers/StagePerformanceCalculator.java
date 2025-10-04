package com.example.habitquest.domain.managers;

import android.util.Pair;

import com.example.habitquest.domain.model.User;
import com.example.habitquest.domain.model.UserXpLog;
import com.example.habitquest.domain.repositoryinterfaces.IUserRepository;
import com.example.habitquest.domain.repositoryinterfaces.IUserXpLogRepository;
import com.example.habitquest.domain.repositoryinterfaces.ITaskRepository;
import com.example.habitquest.domain.repositoryinterfaces.ITaskOccurrenceRepository;
import com.example.habitquest.utils.LevelUtils;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.Comparator;
import java.util.List;

/**
 * Servis koji izračunava uspešnost korisnika (success rate)
 * za tekuću etapu između dva nivoa.
 *
 * Koristi više repozitorijuma: XP logove, zadatke i occurrence-e.
 */
public class StagePerformanceCalculator {

    private final IUserRepository userRepository;
    private final IUserXpLogRepository xpLogRepository;
    private final ITaskRepository taskRepository;
    private final ITaskOccurrenceRepository occurrenceRepository;

    public StagePerformanceCalculator(
            IUserRepository userRepository,
            IUserXpLogRepository xpLogRepository,
            ITaskRepository taskRepository,
            ITaskOccurrenceRepository occurrenceRepository
    ) {
        this.userRepository = userRepository;
        this.xpLogRepository = xpLogRepository;
        this.taskRepository = taskRepository;
        this.occurrenceRepository = occurrenceRepository;
    }

    /**
     * Glavna metoda koja računa uspešnost etape.
     * 1. Određuje vremenske granice etape (početak i kraj).
     * 2. Broji sve relevantne zadatke i occurrence-e u tom periodu.
     * 3. Broji XP logove (uspešno završene zadatke).
     * 4. Izračunava procenat uspešnosti.
     */
    public void calculateStageSuccessRate(String firebaseUid, long localUserId, RepositoryCallback<Double> callback) {

        // 1️⃣ - Prvo dohvatamo korisnika da bismo odredili trenutni XP i nivo
        userRepository.getUser(firebaseUid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                int currentLevel = LevelUtils.calculateLevelFromXp(user.getTotalXp());

                // 2️⃣ - Zatim dobavljamo sve XP logove korisnika
                xpLogRepository.fetchAll(localUserId, firebaseUid, new RepositoryCallback<List<UserXpLog>>() {
                    @Override
                    public void onSuccess(List<UserXpLog> logs) {
                        // Izračunaj vreme početka i kraja etape
                        Pair<Long, Long> boundaries = calculateStageBoundaries(logs, currentLevel);
                        long start = boundaries.first;
                        long end = boundaries.second;

                        // 3️⃣ - Broj one-time zadataka u datom periodu
                        taskRepository.countOneTimeTasksInPeriod(firebaseUid, start, end, new RepositoryCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer oneTimeCount) {

                                // 4️⃣ - Broj occurrence-a u istom periodu
                                occurrenceRepository.countOccurrencesInPeriod(firebaseUid, start, end, new RepositoryCallback<Integer>() {
                                    @Override
                                    public void onSuccess(Integer occurrenceCount) {
                                        int totalTasks = oneTimeCount + occurrenceCount;

                                        // 5️⃣ - Broj uspešnih zadataka (na osnovu XP logova)
                                        int successfulTasks = countSuccessfulTasksInPeriod(logs, start, end);

                                        // 6️⃣ - Izračunaj success rate
                                        double successRate = totalTasks == 0
                                                ? 0.0
                                                : ((double) successfulTasks / totalTasks) * 100.0;

                                        callback.onSuccess(successRate);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        callback.onFailure(e);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ---------------------------------------------------------------
    // 🔹 POMOĆNE FUNKCIJE
    // ---------------------------------------------------------------

    /**
     * Izračunava početak i kraj etape na osnovu XP logova.
     */
    private Pair<Long, Long> calculateStageBoundaries(List<UserXpLog> logs, int currentLevel) {
        if (logs == null || logs.isEmpty()) {
            long now = System.currentTimeMillis();
            return new Pair<>(now, now);
        }

        // Sortiraj logove po vremenu završetka
        logs.sort(Comparator.comparingLong(UserXpLog::getCompletedAt));

        int prevThreshold = LevelUtils.getXpThresholdForLevel(currentLevel - 1);
        int nextThreshold = LevelUtils.getXpThresholdForLevel(currentLevel);

        int cumulativeXp = 0;
        long stageStart = logs.get(0).getCompletedAt();
        Long stageEnd = null;

        for (UserXpLog log : logs) {
            cumulativeXp += log.getXpGained();

            // Kad prvi put pređemo prag prethodnog nivoa — to je početak etape
            if (cumulativeXp >= prevThreshold && stageStart == logs.get(0).getCompletedAt()) {
                stageStart = log.getCompletedAt();
            }

            // Kad pređemo prag sledećeg nivoa — to je kraj etape
            if (cumulativeXp >= nextThreshold) {
                stageEnd = log.getCompletedAt();
                break;
            }
        }

        // Ako korisnik nije još prešao nivo, kraj je sadašnji trenutak
        if (stageEnd == null) stageEnd = System.currentTimeMillis();

        return new Pair<>(stageStart, stageEnd);
    }

    /**
     * Broji koliko je zadataka (XP logova) završeno u okviru vremenskog perioda.
     */
    private int countSuccessfulTasksInPeriod(List<UserXpLog> logs, long start, long end) {
        if (logs == null || logs.isEmpty()) return 0;

        int count = 0;
        for (UserXpLog log : logs) {
            long t = log.getCompletedAt();
            if (t >= start && t <= end) {
                count++;
            }
        }
        return count;
    }
}
