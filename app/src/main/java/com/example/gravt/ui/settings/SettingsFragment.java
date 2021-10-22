package com.example.gravt.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.gravt.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

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
        binding.editHighThreshold.setText(Integer.toString(settings.getInt("high_thres", 30)));
        binding.editLowThreshold.setText(Integer.toString(settings.getInt("low_thres", 5)));
        binding.editInactivityTimer.setText(Integer.toString(settings.getInt("inactivity_timer", 10)));
    }

    @Override
    public void onPause() {
        super.onPause();
        // write to preferences
        SharedPreferences settings = this.getActivity().getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("send_emergency_contact", binding.sendEmergencyContactSwitch.isChecked());
        editor.putInt("high_thres", Integer.parseInt(binding.editHighThreshold.getText().toString()));
        editor.putInt("low_thres", Integer.parseInt(binding.editLowThreshold.getText().toString()));
        editor.putInt("inactivity_timer", Integer.parseInt(binding.editInactivityTimer.getText().toString()));
        editor.commit();
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}