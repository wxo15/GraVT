package com.example.gravt;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.gravt.databinding.ActivityMainBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    TextView txt_acc, txt_acc_curr, txt_acc_prev;

    // txt output variables
    private double acc_curr_val;
    private double acc_prev_val;
    private double acc_change_val;

    // chart plot variables
    private int num_datapoints = 5;
    private int graphIntervalCounter = 0;
    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
    });
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
            acc_prev_val = acc_curr_val;
            acc_curr_val = Math.sqrt(x * x + y * y + z * z);
            acc_change_val = acc_curr_val - acc_prev_val;

            num_datapoints ++;
            series.appendData(new DataPoint(num_datapoints, acc_curr_val), true, num_datapoints);
            txt_acc.setText("MinX: "+ series.getLowestValueX());
            txt_acc_curr.setText("Current: " + acc_curr_val);
            txt_acc_prev.setText("Previous: " + acc_prev_val);

            // reset old data
            if (series.getLowestValueX() < num_datapoints - 3000) {
                /*DataPoint[] old_values = new DataPoint[1000];
                for(int i=0;i<1000;i++){
                    Log.d("Error","Here:" + i);
                    DataPoint old_value = series.findDataPointAtX(num_datapoints - i);
                    if (old_value != null) {
                        old_values[i] = old_value;
                    }
                }
                Log.d("Error","Done");
                */

                List<DataPoint> dataPoints = new ArrayList<>();
                Iterator itr = series.getValues(num_datapoints-1000, num_datapoints);
                while(itr.hasNext()) {
                    dataPoints.add((DataPoint) itr.next());
                }
                series.resetData((DataPoint[]) dataPoints.toArray(new DataPoint[0]));
            }
            viewport.setMinX(num_datapoints - 1000);
            viewport.setMaxX(num_datapoints);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

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

        // import text values on dashboard
        txt_acc = findViewById(R.id.txt_acc);
        txt_acc_prev = findViewById(R.id.txt_acc_prev);
        txt_acc_curr = findViewById(R.id.txt_acc_curr);

        // import sensor objects
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Push to graph
        GraphView graph = (GraphView) findViewById(R.id.graph);
        viewport = graph.getViewport();
        viewport.setScrollable(true);
        viewport.setXAxisBoundsManual(true);
        graph.addSeries(series);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL*1000);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(sensorEventListener);
    }


}