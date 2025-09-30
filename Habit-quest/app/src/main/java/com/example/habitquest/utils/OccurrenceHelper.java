package com.example.habitquest.utils;

import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskOccurrence;
import com.example.habitquest.domain.model.TaskStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OccurrenceHelper {

    public static List<TaskOccurrence> generateOccurrences(Task task) {
        List<TaskOccurrence> occurrences = new ArrayList<>();

        long current = task.getStartDate();
        long end = task.getEndDate();

        while (current <= end) {
            TaskOccurrence occ = new TaskOccurrence();
            occ.setTaskId(task.getId());
            occ.setStatus(TaskStatus.ACTIVE);
            occ.setDate(current);
            occurrences.add(occ);

            // pomeri current na sledeću pojavu
            switch (task.getUnit()) {
                case "DAY":
                    current += task.getInterval() * 24L * 60L * 60L * 1000L;
                    break;
                case "WEEK":
                    current += task.getInterval() * 7L * 24L * 60L * 60L * 1000L;
                    break;
                case "MONTH":
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(current);
                    cal.add(Calendar.MONTH, task.getInterval());
                    current = cal.getTimeInMillis();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown unit: " + task.getUnit());
            }
        }

        return occurrences;
    }
}
