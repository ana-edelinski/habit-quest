package com.example.habitquest.domain.model;

import java.time.LocalDate;

public class CalendarTaskItem {
    private final String id;
    private final String parentId;
    private final Long date;
    private final String title;
    private final String categoryColor; // boja iz kategorije
    private final boolean isOccurrence; // true ako je occurrence, false ako je one-time

    public CalendarTaskItem(String id, String parentId, Long date, String title, String categoryColor, boolean isOccurrence) {
        this.id = id;
        this.parentId = parentId;
        this.date = date;
        this.title = title;
        this.categoryColor = categoryColor;
        this.isOccurrence = isOccurrence;
    }

    public String getId() { return id; }
    public String getParentId() { return parentId; }
    public Long getDate() { return date; }
    public String getTitle() { return title; }
    public String getCategoryColor() { return categoryColor; }
    public boolean isOccurrence() { return isOccurrence; }

    // ---- FACTORY METODE ----
    public static CalendarTaskItem fromTask(Task task, Category category) {
        return new CalendarTaskItem(
                task.getId(),
                null,
                task.getDate(),
                task.getName(),
                category != null ? category.getColorHex() : null,
                false
        );
    }

    public static CalendarTaskItem fromOccurrence(TaskOccurrence occ, Task parentTask, Category category) {
        return new CalendarTaskItem(
                occ.getId(),
                parentTask.getId(),
                occ.getDate(),
                parentTask != null ? parentTask.getName() : "(Task)",
                category != null ? category.getColorHex() : null,
                true
        );
    }

    private static String sanitizeHex(String hex) {
        if (hex == null || hex.isEmpty()) return "#808080"; // fallback siva
        return hex.startsWith("#") ? hex : ("#" + hex);
    }

}
