package com.example.habitquest.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.presentation.viewmodels.CartViewModel;
import com.example.habitquest.presentation.viewmodels.LoginViewModel;
import com.example.habitquest.presentation.viewmodels.factories.LoginViewModelFactory;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.navigation.ui.AppBarConfiguration;

public class HomeActivity extends AppCompatActivity {

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

        appPreferences = new AppPreferences(this);

        initViewModel();
        setupObservers();
        setupToolbar();
        setupBottomNavigation();

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

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        //toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);

        MenuItem cartItem = menu.findItem(R.id.action_cart);
        View actionView = cartItem.getActionView(); // ako koristiš custom layout
        // Ako ne koristiš custom layout, može direktno na menuItem id:

        BadgeDrawable badge = BadgeDrawable.create(this);
        badge.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        badge.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));
        badge.setNumber(0); // inicijalno 0

        BadgeUtils.attachBadgeDrawable(badge, findViewById(R.id.topAppBar), R.id.action_cart);

        // sačuvaj badge kao polje klase da ga možeš kasnije update-ovati
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

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
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

}
