package com.example.habitquest.domain.model;

import java.time.LocalDate;

public class CalendarTaskItem {
    private final String id;
    private final Long date;
    private final String title;
    private final String categoryColor; // boja iz kategorije
    private final boolean isOccurrence; // true ako je occurrence, false ako je one-time

    public CalendarTaskItem(String id, Long date, String title, String categoryColor, boolean isOccurrence) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.categoryColor = categoryColor;
        this.isOccurrence = isOccurrence;
    }

    public String getId() { return id; }
    public Long getDate() { return date; }
    public String getTitle() { return title; }
    public String getCategoryColor() { return categoryColor; }
    public boolean isOccurrence() { return isOccurrence; }

    private CalendarTaskItem toCalendarItem(Task task, Category category) {
        return new CalendarTaskItem(
                task.getId(),
                task.getDate(),
                task.getName(),
                category.getColorHex(),
                false
        );
    }

    private CalendarTaskItem toCalendarItem(TaskOccurrence occ, Task parentTask, Category category) {
        return new CalendarTaskItem(
                occ.getId(),
                occ.getDate(),
                parentTask.getName(),
                category.getColorHex(),
                true
        );
    }

}
