package com.example.habitquest.presentation.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.habitquest.R;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.presentation.viewmodels.UserProfileViewModel;
import com.example.habitquest.presentation.viewmodels.factories.UserProfileViewModelFactory;
import com.example.habitquest.utils.RepositoryCallback;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class UserProfileFragment extends Fragment {

    private ImageView imgAvatar, imgQrCode;
    private TextView txtUsername, txtXp, txtLevel, txtTitle, txtPp, txtCoins;
    private UserProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        imgQrCode = view.findViewById(R.id.imgQRCode);
        txtUsername = view.findViewById(R.id.tvUsername);
        txtXp = view.findViewById(R.id.tvXP);
        txtLevel = view.findViewById(R.id.tvLevel);
        txtTitle = view.findViewById(R.id.tvTitle);
        txtPp = view.findViewById(R.id.tvPP);
        txtCoins = view.findViewById(R.id.tvCoins);
        Button btnAddFriend = view.findViewById(R.id.btnAddFriend);

        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        String userId = getArguments() != null ? getArguments().getString("userId") : null;
        if (userId == null) return;

        UserProfileViewModelFactory factory = new UserProfileViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(UserProfileViewModel.class);

        viewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                txtUsername.setText("Error loading user");
                return;
            }

            txtUsername.setText(user.getUsername());
            txtLevel.setText("Level " + user.getLevel());
            txtTitle.setText(user.getTitle() != null ? user.getTitle() : "Beginner");
            txtPp.setText("PP: " + user.getPp());
            txtCoins.setText("Coins: " + user.getCoins());
            txtXp.setText("XP: " + user.getTotalXp());

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

            // QR kod
            try {
                String qrContent = "habitquest://addfriend?uid=" + user.getUid();

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

        });

        viewModel.currentUser.observe(getViewLifecycleOwner(), currentUser -> {
            if (currentUser == null) return;
            boolean isRequestSent = currentUser.getFriendRequestsSent().contains(userId);
            btnAddFriend.setText(isRequestSent ? "Request Sent" : "Add Friend");

            btnAddFriend.setOnClickListener(v -> {
                if (isRequestSent) {
                    viewModel.cancelFriendRequest(currentUid, userId, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(requireContext(), "Friend request cancelled", Toast.LENGTH_SHORT).show();
                            viewModel.loadCurrentUser(currentUid);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    viewModel.sendFriendRequest(currentUid, userId, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(requireContext(), "Friend request sent", Toast.LENGTH_SHORT).show();
                            viewModel.loadCurrentUser(currentUid);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });

        viewModel.loadUser(userId);
        viewModel.loadCurrentUser(currentUid);

    }
}
