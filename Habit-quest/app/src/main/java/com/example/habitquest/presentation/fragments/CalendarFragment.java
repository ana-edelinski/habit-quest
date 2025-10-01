package com.example.habitquest.presentation.fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.habitquest.R;
import com.example.habitquest.presentation.adapters.TaskAdapter;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import java.time.*;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private RecyclerView recyclerTasks;
    private TaskAdapter taskAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    private LocalDate selectedDate;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        recyclerTasks = view.findViewById(R.id.recyclerTasks);

        taskAdapter = new TaskAdapter(null);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTasks.setAdapter(taskAdapter);

        TextView tvMonthYear = view.findViewById(R.id.tvMonthYear);

        tvMonthYear.setOnClickListener(v -> {
            LocalDate today = LocalDate.now();

            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        // Kad korisnik odabere datum, skroluj kalendar na taj mesec
                        YearMonth selectedYm = YearMonth.of(year, month + 1); // month je 0–11
                        calendarView.scrollToMonth(selectedYm);
                    },
                    today.getYear(),              // početna godina
                    today.getMonthValue() - 1,    // početni mesec (0–11)
                    today.getDayOfMonth()         // početni dan
            );

            dialog.show();
        });

        calendarView.setMonthScrollListener(calendarMonth -> {
            YearMonth ym = calendarMonth.getYearMonth();
            String text = ym.getMonth().toString() + " " + ym.getYear(); // JANUARY 2025
            // Ako želiš lepši prikaz:
            java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy");
            tvMonthYear.setText(ym.format(formatter));
            return null;
        });

        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(6);
        YearMonth endMonth   = currentMonth.plusMonths(6);
        DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;

        calendarView.setup(startMonth, endMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);

        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
                container.textView.setText(String.valueOf(day.getDate().getDayOfMonth()));

                // Van tekućeg meseca – izbledeo prikaz
                container.textView.setAlpha(day.getPosition() == DayPosition.MonthDate ? 1f : 0.4f);

                if (day.getDate().equals(LocalDate.now())) {
                    container.textView.setTextColor(Color.GREEN);
                    container.textView.setTypeface(null, Typeface.BOLD);
                } else {
                    container.textView.setTextColor(Color.WHITE);
                    container.textView.setTypeface(null, Typeface.NORMAL);
                }

                // Selektovan dan – jednostavan highlight
                boolean isSelected = day.getDate().equals(selectedDate);
                container.textView.setBackgroundResource(isSelected ? R.drawable.bg_calendar_day : 0);

                container.textView.setOnClickListener(v -> {
                    if (day.getPosition() != DayPosition.MonthDate) return;
                    LocalDate old = selectedDate;
                    selectedDate = day.getDate();
                    tvSelectedDate.setText("Selected: " + selectedDate);

                    if (old != null) calendarView.notifyDateChanged(old);
                    calendarView.notifyDateChanged(selectedDate);

                    // TODO: viewModel query po datumu pa: taskAdapter.submitList(tasksFor(selectedDate));
                });
            }
        });
    }

    public static class DayViewContainer extends ViewContainer {
        final TextView textView;
        public DayViewContainer(@NonNull View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
        }
    }
}
