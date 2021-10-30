package com.example.gravt.ui.settings;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.gravt.MainActivity;
import com.example.gravt.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    private static final int SEND_SMS_CODE = 23;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        /*
        final TextView textView = binding.textSettings;
        settingsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
         */

        return root;
    }

    public void onResume() {
        super.onResume();

        // populate setting based on preferences
        SharedPreferences settings = this.getActivity().getSharedPreferences("settings", 0);
        binding.sendEmergencyContactSwitch.setChecked(settings.getBoolean("send_emergency_contact", false));
        binding.emergencyPhoneNumber.setText(settings.getString("emergency_phone_number", "999"));
        binding.editHighThreshold.setText(Integer.toString(settings.getInt("high_thres", 30)));
        binding.editLowThreshold.setText(Integer.toString(settings.getInt("low_thres", 2)));
        binding.editInactivityTimer.setText(Integer.toString(settings.getInt("inactivity_timer", 10)));
    }

    @Override
    public void onPause() {
        super.onPause();
        // write to preferences
        // check if have permission. If not, prompt.
        SharedPreferences settings = this.getActivity().getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        if (binding.sendEmergencyContactSwitch.isChecked() && ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestSmsSendPermission();
        } else {
            editor.putBoolean("send_emergency_contact", binding.sendEmergencyContactSwitch.isChecked());
        }
        editor.putString("emergency_phone_number", binding.emergencyPhoneNumber.getText().toString());
        editor.putInt("high_thres", Integer.parseInt(binding.editHighThreshold.getText().toString()));
        editor.putInt("low_thres", Integer.parseInt(binding.editLowThreshold.getText().toString()));
        editor.putInt("inactivity_timer", Integer.parseInt(binding.editInactivityTimer.getText().toString()));
        editor.commit();
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //Requesting permission
    private void requestSmsSendPermission() {
        //Ask for the permission
        ActivityCompat.requestPermissions(this.getActivity(), new String[] { Manifest.permission.SEND_SMS },
                SEND_SMS_CODE);
    }

}