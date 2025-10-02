package com.example.habitquest.presentation.fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.CalendarTaskItem;
import com.example.habitquest.presentation.adapters.CalendarItemsAdapter;
import com.example.habitquest.presentation.viewmodels.CalendarViewModel;
import com.example.habitquest.presentation.viewmodels.TaskViewModel;
import com.example.habitquest.presentation.viewmodels.factories.CalendarViewModelFactory;
import com.example.habitquest.presentation.viewmodels.factories.TaskViewModelFactory;
import com.example.habitquest.utils.LiveDataUtils;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private RecyclerView recyclerTasks;

    private CalendarItemsAdapter calendarAdapter;
    private CalendarViewModel viewModel;

    private LocalDate selectedDate;
    private TaskViewModel taskViewModel;

    private Map<LocalDate, List<CalendarTaskItem>> itemsByDate = Collections.emptyMap();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        taskViewModel = new ViewModelProvider(
                this,
                new TaskViewModelFactory(requireContext())
        ).get(TaskViewModel.class);

        calendarView   = root.findViewById(R.id.calendarView);
        tvSelectedDate = root.findViewById(R.id.tvSelectedDate);
        recyclerTasks  = root.findViewById(R.id.recyclerTasks);
        TextView tvMonthYear = root.findViewById(R.id.tvMonthYear);

        // Adapter za listu ispod (koristi ugrađeni layout)
        calendarAdapter = new CalendarItemsAdapter(item -> {
            NavController navController = Navigation.findNavController(requireView());

            NavOptions options = new NavOptions.Builder()
                    .setPopUpTo(R.id.calendarFragment, false)
                    .build();

            if (item.isOccurrence()) {
                String taskId = item.getParentId();
                String occurrenceId = item.getId();

                taskViewModel.getTaskByIdLive(taskId).observe(getViewLifecycleOwner(), task -> {
                    if (task != null) {
                        taskViewModel.getOccurrenceByIdLive(taskId, occurrenceId).observe(getViewLifecycleOwner(), occ -> {
                            if (occ != null) {
                                OccurrenceDetailFragment f = OccurrenceDetailFragment.newInstance(occ, task);
                                navController.navigate(R.id.occurrenceDetailFragment, f.getArguments(), options);
                            }
                        });
                    }
                });

            } else {
                String taskId = item.getId();

                taskViewModel.getTaskByIdLive(taskId).observe(getViewLifecycleOwner(), task -> {
                    if (task != null) {
                        TaskDetailFragment f = TaskDetailFragment.newInstance(task);
                        navController.navigate(R.id.taskDetailFragment, f.getArguments(), options);
                    }
                });
            }
        });


        recyclerTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTasks.setAdapter(calendarAdapter);

        // ViewModel
        viewModel = new ViewModelProvider(
                this,
                new CalendarViewModelFactory(requireContext())
        ).get(CalendarViewModel.class);

        viewModel.getCalendarItems().observe(getViewLifecycleOwner(), map -> {
            itemsByDate = (map != null) ? map : Collections.emptyMap();
            calendarView.notifyCalendarChanged();
            if (selectedDate != null) updateListFor(selectedDate);
        });

        // Month/Year picker
        tvMonthYear.setOnClickListener(v -> {
            LocalDate today = LocalDate.now();
            DatePickerDialog dlg = new DatePickerDialog(
                    requireContext(),
                    (view1, year, month, day) -> calendarView.scrollToMonth(YearMonth.of(year, month + 1)),
                    today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth()
            );
            dlg.show();
        });

        // Header text
        calendarView.setMonthScrollListener(month -> {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM yyyy");
            ((TextView) root.findViewById(R.id.tvMonthYear)).setText(month.getYearMonth().format(fmt));
            return null;
        });

        // Setup kalendara
        YearMonth currentMonth = YearMonth.now();
        calendarView.setup(currentMonth.minusMonths(6), currentMonth.plusMonths(6), DayOfWeek.MONDAY);
        calendarView.scrollToMonth(currentMonth);


        // Binder
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer c, @NonNull CalendarDay day) {
                LocalDate date = day.getDate();
                c.textView.setText(String.valueOf(date.getDayOfMonth()));
                c.textView.setAlpha(day.getPosition() == DayPosition.MonthDate ? 1f : 0.4f);

                int todayColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary);
                int normalColor = ContextCompat.getColor(requireContext(), R.color.white);

                if (date.equals(LocalDate.now())) {
                    c.textView.setTextColor(todayColor);
                    c.textView.setTypeface(null, Typeface.BOLD);
                } else {
                    c.textView.setTextColor(normalColor);
                    c.textView.setTypeface(null, Typeface.NORMAL);
                }

                boolean isSelected = date.equals(selectedDate);
                c.root.setBackgroundResource(isSelected ? R.drawable.bg_calendar_day : 0);


                // --- tačkice ---
                c.dotsContainer.removeAllViews();
                List<CalendarTaskItem> items = itemsByDate.get(date);
                if (items != null && !items.isEmpty()) {
                    Set<String> uniqueColors = new HashSet<>();
                    int count = 0;
                    for (CalendarTaskItem it : items) {
                        if (uniqueColors.add(it.getCategoryColor())) {
                            if (count >= 3) break; // maksimalno 3 tačkice
                            View dot = new View(requireContext());
                            int size = dp(6);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                            lp.setMargins(dp(1), 0, dp(1), 0);
                            dot.setLayoutParams(lp);

                            GradientDrawable bg = new GradientDrawable();
                            bg.setShape(GradientDrawable.OVAL);
                            try { bg.setColor(Color.parseColor(it.getCategoryColor())); }
                            catch (Exception e) { bg.setColor(Color.GRAY); }

                            dot.setBackground(bg);
                            c.dotsContainer.addView(dot);
                            count++;
                        }
                    }
                }


                c.textView.setOnClickListener(v -> {
                    if (day.getPosition() != DayPosition.MonthDate) return;
                    LocalDate old = selectedDate;
                    selectedDate = date;
                    String nice = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    tvSelectedDate.setText("Selected: " + nice);


                    if (old != null) calendarView.notifyDateChanged(old);
                    calendarView.notifyDateChanged(selectedDate);

                    updateListFor(selectedDate);
                });
            }
        });

        // Učitaj iz VM
        viewModel.loadData();
    }

    private void updateListFor(LocalDate date) {
        List<CalendarTaskItem> items = viewModel.getItemsFor(date);
        calendarAdapter.setItems(items);
    }

    private GradientDrawable makeBottomDot(String hex, int sizeDp) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        try { d.setColor(Color.parseColor(hex)); } catch (Exception e) { d.setColor(Color.GRAY); }
        int px = dp(sizeDp);
        d.setSize(px, px); // važno za compound drawables
        return d;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    public static class DayViewContainer extends ViewContainer {
        final TextView textView;
        final LinearLayout dotsContainer;
        final FrameLayout root;

        public DayViewContainer(@NonNull View view) {
            super(view);
            root = view.findViewById(R.id.dayRoot);
            textView = view.findViewById(R.id.calendarDayText);
            dotsContainer = view.findViewById(R.id.dotsContainer);
        }
    }
}
