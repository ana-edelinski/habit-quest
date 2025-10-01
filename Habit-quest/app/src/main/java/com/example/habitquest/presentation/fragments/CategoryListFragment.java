package com.example.habitquest.presentation.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.habitquest.domain.model.Category;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.presentation.adapters.CategoryAdapter;
import com.example.habitquest.presentation.viewmodels.CategoryViewModel;
import com.example.habitquest.presentation.viewmodels.factories.CategoryViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.skydoves.colorpickerview.sliders.AlphaSlideBar;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

public class CategoryListFragment extends Fragment {

    private CategoryAdapter adapter;
    private CategoryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerCategories);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddCategory);

        adapter = new CategoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setListener(new CategoryAdapter.CategoryClickListener() {
            @Override
            public void onEditClick(Category category) {
                showEditCategoryDialog(category);
            }

            @Override
            public void onDeleteClick(Category category) {
                viewModel.deleteCategory(category);
            }
        });

        // ViewModel
        viewModel =
                new ViewModelProvider(
                        requireActivity(),
                        new CategoryViewModelFactory(requireContext())
                ).get(CategoryViewModel.class);

        // posmatraj promene u listi
        viewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            adapter.setCategories(categories);
        });

        viewModel.message.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });


        viewModel.startListening();

        fabAdd.setOnClickListener(v -> {
            showAddCategoryDialog();
        });
    }

    private void showAddCategoryDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);

        // standard palette views
        View colorBlack = dialogView.findViewById(R.id.colorBlack);
        View colorWhite = dialogView.findViewById(R.id.colorWhite);
        View colorRed = dialogView.findViewById(R.id.colorRed);
        View colorGreen = dialogView.findViewById(R.id.colorGreen);
        View colorBlue = dialogView.findViewById(R.id.colorBlue);
        View colorYellow = dialogView.findViewById(R.id.colorYellow);

        EditText editName = dialogView.findViewById(R.id.editCategoryName);
        ColorPickerView colorPickerView = dialogView.findViewById(R.id.colorPickerView);
        BrightnessSlideBar brightnessSlide = dialogView.findViewById(R.id.brightnessSlide);
        View colorPreview = dialogView.findViewById(R.id.colorPreview);

        final int[] selectedColor = {Color.WHITE};

        // poveži brightness slider
        colorPickerView.attachBrightnessSlider(brightnessSlide);

        // slušaj promenu boje iz pickera
        colorPickerView.setColorListener((ColorEnvelopeListener) (envelope, fromUser) -> {
            selectedColor[0] = envelope.getColor();
            Drawable bg = colorPreview.getBackground();
            if (bg != null) {
                bg.setTint(selectedColor[0]);
            }
        });

        // listener za paletu
        View.OnClickListener paletteClick = v -> {
            ColorStateList tint = v.getBackgroundTintList();
            if (tint != null) {
                int color = tint.getDefaultColor();
                selectedColor[0] = color;

                Drawable bg = colorPreview.getBackground();
                if (bg != null) {
                    bg.setTint(color);
                }
            }
        };

        colorBlack.setOnClickListener(paletteClick);
        colorWhite.setOnClickListener(paletteClick);
        colorRed.setOnClickListener(paletteClick);
        colorGreen.setOnClickListener(paletteClick);
        colorBlue.setOnClickListener(paletteClick);
        colorYellow.setOnClickListener(paletteClick);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Category")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String hexColor = String.format("#%06X", (0xFFFFFF & selectedColor[0]));

                    if (!name.isEmpty()) {
                        viewModel.createCategory(name, hexColor);
                    } else {
                        Toast.makeText(getContext(), "Enter category name!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    private void showEditCategoryDialog(Category category) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);

        EditText editName = dialogView.findViewById(R.id.editCategoryName);
        ColorPickerView colorPickerView = dialogView.findViewById(R.id.colorPickerView);
        BrightnessSlideBar brightnessSlide = dialogView.findViewById(R.id.brightnessSlide);
        View colorPreview = dialogView.findViewById(R.id.colorPreview);

        View colorBlack = dialogView.findViewById(R.id.colorBlack);
        View colorWhite = dialogView.findViewById(R.id.colorWhite);
        View colorRed = dialogView.findViewById(R.id.colorRed);
        View colorGreen = dialogView.findViewById(R.id.colorGreen);
        View colorBlue = dialogView.findViewById(R.id.colorBlue);
        View colorYellow = dialogView.findViewById(R.id.colorYellow);

        editName.setText(category.getName());

        final int[] selectedColor = { Color.parseColor(category.getColorHex()) };

        // postavi inicijalnu boju u preview
        Drawable bg = colorPreview.getBackground();
        if (bg != null) {
            bg.setTint(selectedColor[0]);
        }

        // poveži brightness slider
        colorPickerView.attachBrightnessSlider(brightnessSlide);

        // slušaj promenu boje iz pickera
        colorPickerView.setColorListener((ColorEnvelopeListener) (envelope, fromUser) -> {
            selectedColor[0] = envelope.getColor();
            Drawable bgInner = colorPreview.getBackground();
            if (bgInner != null) {
                bgInner.setTint(selectedColor[0]);
            }
        });

        // listener za standardnu paletu
        View.OnClickListener paletteClick = v -> {
            int color = ((ColorDrawable) v.getBackground()).getColor();
            selectedColor[0] = color;
            Drawable bgInner = colorPreview.getBackground();
            if (bgInner != null) {
                bgInner.setTint(color);
            }
        };

        colorBlack.setOnClickListener(paletteClick);
        colorWhite.setOnClickListener(paletteClick);
        colorRed.setOnClickListener(paletteClick);
        colorGreen.setOnClickListener(paletteClick);
        colorBlue.setOnClickListener(paletteClick);
        colorYellow.setOnClickListener(paletteClick);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Category")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String hexColor = String.format("#%06X", (0xFFFFFF & selectedColor[0]));

                    if (!name.isEmpty()) {
                        category.setName(name);
                        category.setColorHex(hexColor);
                        viewModel.updateCategory(category);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }




}
