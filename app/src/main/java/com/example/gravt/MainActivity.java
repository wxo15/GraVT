package com.example.gravt;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.gravt.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int SEND_SMS_CODE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
        SharedPreferences settings = this.getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();

        switch (permsRequestCode) {
            case SEND_SMS_CODE:
                boolean SMSAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                editor.putBoolean("send_emergency_contact", SMSAccepted);
                editor.commit();

        }
    }

}