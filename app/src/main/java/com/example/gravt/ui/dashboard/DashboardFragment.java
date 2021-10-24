package com.example.gravt.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.gravt.R;
import com.example.gravt.databinding.FragmentDashboardBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    // items on dashboard
    TextView txt_acc, txt_acc_curr;
    CheckBox fall_checkbox, impact_checkbox;
    ProgressBar inactivity_progress_bar;

    // txt output variables
    private double acc_curr_val;

    // chart plot variables
    private int num_datapoints = 0;
    private boolean send_emergency_contact;
    private int high_thres;
    private int low_thres;
    private int inactivity_timer;
    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {});
    LineGraphSeries<DataPoint> low_thres_series = new LineGraphSeries<>(new DataPoint[] {});
    LineGraphSeries<DataPoint> high_thres_series = new LineGraphSeries<>(new DataPoint[] {});
    Viewport viewport;
    GraphView graph;

    // fall detection algorithm variable
    private int stage_cooldown = 300;
    private int low_trigger = 0;
    private int high_trigger = 0;
    private long inactivity_start_time = 0;
    private boolean all_triggered = false;

    // sensor variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            acc_curr_val = Math.sqrt(x * x + y * y + z * z);

            num_datapoints ++;
            // write to text and append to series
            series.appendData(new DataPoint(num_datapoints, acc_curr_val), true, num_datapoints);
            low_thres_series.resetData(new DataPoint[] {
                    new DataPoint(series.getLowestValueX(), low_thres),
                    new DataPoint(num_datapoints, low_thres)
            });
            high_thres_series.resetData(new DataPoint[] {
                    new DataPoint(series.getLowestValueX(), high_thres),
                    new DataPoint(num_datapoints, high_thres)
            });
            txt_acc.setText("MinX: "+ series.getLowestValueX());
            txt_acc_curr.setText("Current: " + Math.round( acc_curr_val * Math.pow(10, 3))/ Math.pow(10, 3) );

            // reset old data if built-up
            if (series.getLowestValueX() < num_datapoints - 1000) {
                List<DataPoint> dataPoints = new ArrayList<>();
                Iterator itr = series.getValues(num_datapoints-500, num_datapoints);
                while(itr.hasNext()) {
                    dataPoints.add((DataPoint) itr.next());
                }
                series.resetData(dataPoints.toArray(new DataPoint[0]));
            }

            // Detection algorithm
            detectFall();
            detectImpact();
            detectInactivity();

            // reset axis and background color
            viewport.setMinX(Math.max(0,num_datapoints - 500));
            viewport.setMaxX(num_datapoints);
            viewport.setMinY(0);
            viewport.setMaxY(40);
            if (acc_curr_val > high_thres || acc_curr_val < low_thres){
                graph.setBackgroundColor(Color.YELLOW);
            } else {
                graph.setBackgroundColor(Color.WHITE);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // import text values on dashboard
        txt_acc = root.findViewById(R.id.txt_acc);
        txt_acc_curr = root.findViewById(R.id.txt_acc_curr);

        // import checkboxes on dashboard
        fall_checkbox = root.findViewById(R.id.fall_checkbox);
        impact_checkbox = root.findViewById(R.id.impact_checkbox);
        inactivity_progress_bar = root.findViewById(R.id.inactivity_progress_bar);


        // import sensor objects
        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Set up graph
        graph = (GraphView) root.findViewById(R.id.graph);
        viewport = graph.getViewport();
        viewport.setScrollable(false);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(40);
        series.setTitle("Net");
        low_thres_series.setTitle("Fall");
        high_thres_series.setTitle("Impact");
        series.setColor(Color.BLACK);
        low_thres_series.setColor(Color.RED);
        high_thres_series.setColor(Color.RED);
        graph.addSeries(high_thres_series);
        graph.addSeries(series);
        graph.addSeries(low_thres_series);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setBackgroundColor(Color.WHITE);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        return root;
    }

    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL*1000);

        // populate setting based on preferences
        SharedPreferences settings = this.getActivity().getSharedPreferences("settings", 0);
        send_emergency_contact = settings.getBoolean("send_emergency_contact", false);
        high_thres = settings.getInt("high_thres", 30);
        low_thres = settings.getInt("low_thres", 2);
        inactivity_timer = settings.getInt("inactivity_timer", 10);
    }

    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void detectFall() {
        // If value is below threshold, set low_trigger to stage_cooldown.
        // Else, reduce by 1 for each cycle.
        if (acc_curr_val < low_thres) {
            low_trigger = stage_cooldown;
        } else {
            low_trigger = Math.max(0, low_trigger - 1);
        }
        fall_checkbox.setChecked(low_trigger > 0);
    }

    public void detectImpact() {
        // If value is above threshold and low_trigger not expired, set high_trigger to stage_cooldown.
        // Else, reduce by 1 for each cycle.
        if (acc_curr_val > high_thres && low_trigger > 0) {
            high_trigger = stage_cooldown;
        } else {
            high_trigger = Math.max(0, high_trigger - 1);
        }
        impact_checkbox.setChecked(high_trigger > 0);
    }

    public void detectInactivity() {
        // If high_trigger not expired and inactivity start_time is not set, save current time
        if (high_trigger > 0 && inactivity_start_time == 0) {
            inactivity_start_time = System.currentTimeMillis();
        }
        // If value is closed to 9.81 (within 1) and inactivity_start_time is set, write to progress bar
        // Formula: 100 * (time in ms now - inactivity_start_time in ms) / (1000 * inactivity_timer in s)
        // Else, reset inactivity_start_time and progress bar
        if (Math.abs(acc_curr_val - 9.81) < 1 && inactivity_start_time > 0) {
            inactivity_progress_bar.setProgress(((int)(System.currentTimeMillis() - inactivity_start_time))/(10 * inactivity_timer));
            all_triggered = (int)(System.currentTimeMillis() - inactivity_start_time) > 10 * inactivity_timer;
        } else {
            inactivity_start_time = 0;
            inactivity_progress_bar.setProgress(0);
        }
    }

}