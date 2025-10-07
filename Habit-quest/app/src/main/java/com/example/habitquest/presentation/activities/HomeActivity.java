package com.example.habitquest.presentation.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.remote.AllianceRemoteDataSource;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.presentation.viewmodels.CartViewModel;
import com.example.habitquest.presentation.viewmodels.LoginViewModel;
import com.example.habitquest.presentation.viewmodels.factories.LoginViewModelFactory;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    private AppPreferences appPreferences;
    private LoginViewModel loginViewModel;
    private CartViewModel cartViewModel;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private BadgeDrawable cartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .setOpenableLayout(drawerLayout)
                .build();

        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);
        ImageView imgAvatar = headerView.findViewById(R.id.imgAvatarHeader);
        TextView tvUsername = headerView.findViewById(R.id.tvUsernameHeader);
        TextView tvTitle = headerView.findViewById(R.id.tvTitleHeader);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            UserRepository repo = new UserRepository(this);
            repo.getUser(uid, new RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    tvUsername.setText(user.getUsername());
                    tvTitle.setText(user.getTitle());
                    imgAvatar.setImageResource(getAvatarResource(user.getAvatar()));
                }

                @Override
                public void onFailure(Exception e) {
                    tvUsername.setText("Unknown user");
                    tvTitle.setText("");
                }
            });
        }

        appPreferences = new AppPreferences(this);

        initViewModel();
        setupObservers();
        setupToolbar();
        setupBottomNavigation();

        requestNotificationPermission();
        handleDeepLink(getIntent());

        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            AllianceRemoteDataSource allianceRemote = new AllianceRemoteDataSource();
            allianceRemote.listenForAllianceInvites(uid, this);
            allianceRemote.listenForAllianceAccepts(uid, this);
        }

        if (getIntent() != null && getIntent().getBooleanExtra("navigateToAllianceDetails", false)) {
            String allianceId = getIntent().getStringExtra("allianceId");
            if (allianceId != null) {
                Bundle bundle = new Bundle();
                bundle.putString("allianceId", allianceId);
                navController.navigate(R.id.allianceDetailsFragment, bundle);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDeepLink(intent);

        if (intent != null && intent.getBooleanExtra("navigateToAllianceDetails", false)) {
            String allianceId = intent.getStringExtra("allianceId");
            if (allianceId != null) {
                Bundle bundle = new Bundle();
                bundle.putString("allianceId", allianceId);
                navController.navigate(R.id.allianceDetailsFragment, bundle);
            }
        }
    }

    private void handleDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) return;

        Uri data = intent.getData();
        if ("habitquest".equals(data.getScheme()) && "addfriend".equals(data.getHost())) {
            String scannedUid = data.getQueryParameter("uid");
            String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (scannedUid != null && !scannedUid.equals(currentUid)) {
                UserRepository repo = new UserRepository(this);
                repo.sendFriendRequest(currentUid, scannedUid, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(HomeActivity.this,
                                "Friend request sent via QR", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(HomeActivity.this,
                                "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "You cannot add yourself as a friend!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViewModel() {
        LoginViewModelFactory factory = new LoginViewModelFactory(this);
        loginViewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
    }

    private void setupObservers() {
        loginViewModel.logoutSuccess.observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                navigateToLogin();
            }
        });

        cartViewModel.getCartItems().observe(this, items -> {
            int count = (items != null) ? items.size() : 0;
            updateCartBadge(count);
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.open());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        bottomNavigationView.setOnItemReselectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                navController.popBackStack(R.id.nav_home, false);
            } else if (id == R.id.nav_explore) {
                navController.popBackStack(R.id.nav_explore, false);
            } else if (id == R.id.nav_store) {
                navController.popBackStack(R.id.nav_store, false);
            } else if (id == R.id.nav_account) {
                navController.popBackStack(R.id.nav_account, false);
            }
        });
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);

        MenuItem cartItem = menu.findItem(R.id.action_cart);
        View actionView = cartItem.getActionView();

        BadgeDrawable badge = BadgeDrawable.create(this);
        badge.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        badge.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));
        badge.setNumber(0);

        BadgeUtils.attachBadgeDrawable(badge, findViewById(R.id.topAppBar), R.id.action_cart);
        this.cartBadge = badge;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_cart) {
            navController.navigate(R.id.cartFragment);
            return true;
        } else if (id == R.id.action_logout) {
            loginViewModel.logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateCartBadge(int count) {
        if (cartBadge == null) return;

        if (count > 0) {
            cartBadge.setVisible(true);
            cartBadge.setNumber(count);
        } else {
            cartBadge.setVisible(false);
        }
    }

    private int getAvatarResource(int index) {
        switch (index) {
            case 1: return R.drawable.avatar1;
            case 2: return R.drawable.avatar2;
            case 3: return R.drawable.avatar3;
            case 4: return R.drawable.avatar4;
            case 5: return R.drawable.avatar5;
            default: return R.drawable.avatar1;
        }
    }
}
