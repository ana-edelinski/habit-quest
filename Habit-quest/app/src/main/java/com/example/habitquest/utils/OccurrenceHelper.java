package com.example.habitquest.utils;

import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskOccurrence;
import com.example.habitquest.domain.model.TaskStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OccurrenceHelper {

    private OccurrenceHelper() {}

    public static List<TaskOccurrence> generateOccurrences(Task task) {
        List<TaskOccurrence> occurrences = new ArrayList<>();

        // 🔹 Normalizuj start na 00:00
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(task.getStartDate());
        resetTime(cal);
        long current = cal.getTimeInMillis();

        // 🔹 Normalizuj end na 00:00
        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(task.getEndDate());
        resetTime(endCal);
        long end = endCal.getTimeInMillis();

        while (current <= end) {
            TaskOccurrence occ = new TaskOccurrence();
            occ.setTaskId(task.getId());
            occ.setStatus(TaskStatus.ACTIVE);
            occ.setDate(current);
            occurrences.add(occ);

            // pomeraj dalje
            cal.setTimeInMillis(current);
            switch (task.getUnit().toUpperCase()) {
                case "DAY":
                    cal.add(Calendar.DAY_OF_MONTH, task.getInterval());
                    break;
                case "WEEK":
                    cal.add(Calendar.WEEK_OF_YEAR, task.getInterval());
                    break;
                case "MONTH":
                    cal.add(Calendar.MONTH, task.getInterval());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown unit: " + task.getUnit());
            }
            // normalizuj vreme posle svakog pomeranja
            resetTime(cal);
            current = cal.getTimeInMillis();
        }

        return occurrences;
    }

    private static void resetTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}

