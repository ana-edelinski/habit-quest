package com.example.habitquest.presentation.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.presentation.viewmodels.AccountViewModel;
import com.example.habitquest.presentation.viewmodels.factories.AccountViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class AccountFragment extends Fragment {

    private AccountViewModel viewModel;
    private ImageView imgAvatar;
    private TextView txtUsername;
    private TextView txtXp;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        txtUsername = view.findViewById(R.id.tvUsername);
        txtXp = view.findViewById(R.id.tvXP);
        ImageView imgQrCode = view.findViewById(R.id.imgQRCode);

        //testiranje napredak kroz nivoe
//        imgAvatar.setOnLongClickListener(v -> {
//            AppPreferences prefs = new AppPreferences(requireContext());
//            String remoteUid = prefs.getFirebaseUid();
//            long localUserId = Long.parseLong(prefs.getUserId());
//
//            // Dodaj 200 XP
//            viewModel.grantXpForTesting(localUserId, remoteUid, 200);
//
//            return true; // označi da je event potrošen
//        });

        View btnSettings = view.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.changePasswordFragment);
        });

        AccountViewModelFactory factory = new AccountViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(AccountViewModel.class);

        viewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                txtUsername.setText(user.getUsername());

                int resId;
                switch (user.getAvatar()) {
                    case 1: resId = R.drawable.avatar1; break;
                    case 2: resId = R.drawable.avatar2; break;
                    case 3: resId = R.drawable.avatar3; break;
                    case 4: resId = R.drawable.avatar4; break;
                    case 5: resId = R.drawable.avatar5; break;
                    default: resId = R.drawable.avatar5;
                }
                imgAvatar.setImageResource(resId);

                // QR kod za uid
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    try {
                        String qrContent = firebaseUser.getUid();
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.encodeBitmap(
                                qrContent,
                                com.google.zxing.BarcodeFormat.QR_CODE,
                                400,
                                400
                        );
                        imgQrCode.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        viewModel.totalXp.observe(getViewLifecycleOwner(), totalXp -> {
            txtXp.setText("XP: " + totalXp);
        });

        // sada pozivamo user repo (remote + local)
        viewModel.loadUser();
    }

}
