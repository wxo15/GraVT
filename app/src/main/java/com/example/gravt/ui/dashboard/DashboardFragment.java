package com.example.gravt.ui.dashboard;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.gravt.R;
import com.example.gravt.databinding.ActivityMainBinding;
import com.example.gravt.databinding.FragmentDashboardBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;

    TextView txt_acc, txt_acc_curr;

    // txt output variables
    private double acc_curr_val;

    // chart plot variables
    private int num_datapoints = 0;
    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {});
    Viewport viewport;

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
            txt_acc.setText("MinX: "+ series.getLowestValueX());
            txt_acc_curr.setText("Current: " + Math.round( acc_curr_val * Math.pow(10, 3))/ Math.pow(10, 3));

            // reset old data if built-up
            if (series.getLowestValueX() < num_datapoints - 3000) {
                List<DataPoint> dataPoints = new ArrayList<>();
                Iterator itr = series.getValues(num_datapoints-1000, num_datapoints);
                while(itr.hasNext()) {
                    dataPoints.add((DataPoint) itr.next());
                }
                series.resetData(dataPoints.toArray(new DataPoint[0]));
            }

            // reset axis
            viewport.setMinX(Math.max(0,num_datapoints - 1000));
            viewport.setMaxX(num_datapoints);
            viewport.setMinY(0);
            viewport.setMaxY(40);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // import text values on dashboard
        txt_acc = root.findViewById(R.id.txt_acc);
        txt_acc_curr = root.findViewById(R.id.txt_acc_curr);

        // import sensor objects
        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Push to graph
        GraphView graph = (GraphView) root.findViewById(R.id.graph);
        viewport = graph.getViewport();
        viewport.setScrollable(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(40);
        series.setTitle("Net");
        graph.addSeries(series);
        return root;
    }

    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL*1000);
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

}