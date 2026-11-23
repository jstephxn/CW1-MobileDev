/*  Starter project for Mobile Platform Development - 1st diet 26/27
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required source and displays the
    raw data in a TextField
*/

//
// Name                 James Stephen
// Student ID           S2534002
// Program of Study   Bsc Software Development
//

// UPDATE THE PACKAGE NAME to include your Student Identifier
package com.example.cw1_trafficapplication_s2534002;

import com.example.cw1_trafficapplication_s2534002.model.Situation;
import com.example.cw1_trafficapplication_s2534002.parser.TrafficXmlParser;
import com.example.cw1_trafficapplication_s2534002.adapter.TrafficSituationAdapter;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.graphics.Color;
import android.content.res.ColorStateList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.app.AlertDialog;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import android.util.Base64;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;


public class MainActivity extends AppCompatActivity implements OnClickListener {

    private static final long REFRESH_INTERVAL = 5 * 60 * 1000; // 5 minutes

    private Handler refreshHandler = new Handler(Looper.getMainLooper());

    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            startProgress();

            refreshHandler.postDelayed(this, REFRESH_INTERVAL);
        }
    };

    private RecyclerView trafficRecyclerView;
    private Button startButton;
    private TrafficSituationAdapter trafficSituationAdapter;
    private ProgressBar loadingProgress;
    private TextView errorMessage;
    private ArrayList<Situation> allSituations = new ArrayList<>();
    private EditText roadSearch;
    private boolean showingRoadworks = false;

    final String unplannedEventsURL = "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button mapButton = findViewById(R.id.mapButton);

        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putExtra("situations", allSituations);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the raw links to the graphical components
        trafficRecyclerView = findViewById(R.id.trafficRecyclerView);
        trafficRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        loadingProgress = findViewById(R.id.loadingProgress);
        errorMessage = findViewById(R.id.errorMessage);

        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        roadSearch = findViewById(R.id.roadSearch);

        roadSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSituations(showingRoadworks);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Button incidentsButton = findViewById(R.id.incidentsButton);
        Button roadworksButton = findViewById(R.id.roadworksButton);
        incidentsButton.setOnClickListener(v -> filterSituations(false));
        roadworksButton.setOnClickListener(v -> filterSituations(true));

    }

    public void onClick(View aview)
    {
        startProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshHandler.removeCallbacks(refreshRunnable);

        startProgress();

        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();

        refreshHandler.removeCallbacks(refreshRunnable);
    }

    public void startProgress()
    {
        errorMessage.setVisibility(View.GONE);

        loadingProgress.setVisibility(View.VISIBLE);

        startButton.setEnabled(false);

        new Thread(new Task()).start();
    }

    private void filterSituations(boolean showRoadworks) {

        showingRoadworks = showRoadworks;

        ArrayList<Situation> filteredSituations = new ArrayList<>();

        String searchTerm = roadSearch.getText().toString().trim().toLowerCase();

        for (Situation situation : allSituations) {

            boolean isRoadwork = situation.getRoadManagementType() != null;

            boolean matchesCategory =
                    showRoadworks == isRoadwork;

            String description = situation.getDescription() != null
                    ? situation.getDescription().toLowerCase()
                    : "";

            boolean matchesSearch = description.contains(searchTerm);

            if (matchesCategory && matchesSearch) {
                filteredSituations.add(situation);
            }
        }

        trafficRecyclerView.setAdapter(
                new TrafficSituationAdapter(
                        filteredSituations,
                        situation -> showSituationDetails(situation)
                )
        );

        updateCategoryButtons(showRoadworks);

        trafficRecyclerView.setAdapter(new TrafficSituationAdapter(
                        filteredSituations,
                        situation -> showSituationDetails(situation)
                )
        );
    }

    private void updateCategoryButtons(boolean showRoadworks) {

        Button incidentsButton = findViewById(R.id.incidentsButton);
        Button roadworksButton = findViewById(R.id.roadworksButton);

        int selectedColour = Color.parseColor("#1976D2");
        int unselectedColour = Color.parseColor("#BDBDBD");

        incidentsButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        showRoadworks ? unselectedColour : selectedColour
                )
        );

        roadworksButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        showRoadworks ? selectedColour : unselectedColour
                )
        );
    }

    private void showSituationDetails(Situation situation) {

        StringBuilder details = new StringBuilder();

        details.append("Type: ")
                .append(situation.getType() != null ?
                        situation.getType() : "Unknown")
                .append("\n\n");

        details.append("Description: ")
                .append(situation.getDescription() != null ?
                        situation.getDescription() : "No description available")
                .append("\n\n");

        details.append("Direction: ")
                .append(situation.getDirection() != null ?
                        situation.getDirection() : "Unknown")
                .append("\n\n");

        details.append("Start time: ")
                .append(situation.getStartTime() != null ?
                        situation.getStartTime() : "Unknown")
                .append("\n\n");

        details.append("End time: ")
                .append(situation.getEndTime() != null ?
                        situation.getEndTime() : "Unknown")
                .append("\n\n");

        details.append("Road management: ")
                .append(situation.getRoadManagementType() != null ?
                        situation.getRoadManagementType() : "N/A")
                .append("\n\n");

        details.append("Abnormal traffic: ")
                .append(situation.getAbnormalTrafficType() != null ?
                        situation.getAbnormalTrafficType() : "N/A")
                .append("\n\n");

        details.append("Delays: ")
                .append(situation.getDelaysType() != null ?
                        situation.getDelaysType() : "N/A");

        new AlertDialog.Builder(this)
                .setTitle("Traffic Situation Details")
                .setMessage(details.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    // Need separate thread to access the internet resource over network
    // Other neater solutions should be adopted in later iterations.
    private class Task implements Runnable
    {
        public Task() { }

        @Override
        public void run() {

            Log.d("MyTask", "Starting live traffic request");

            HttpURLConnection urlConnection = null;

            try {

                // Connect to the Traffic Scotland API
                URL url = new URL(unplannedEventsURL);

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);

                urlConnection.setRequestProperty("Accept", "application/xml");

                // Prepare API authentication
                String credentials =
                        BuildConfig.TRAFFIC_CLIENT_ID + ":" +
                                BuildConfig.TRAFFIC_CLIENT_KEY;

                String encodedCredentials = Base64.encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8),
                        Base64.NO_WRAP
                );

                urlConnection.setRequestProperty(
                        "Authorization",
                        "Basic " + encodedCredentials
                );

                // Make the request
                int responseCode = urlConnection.getResponseCode();

                Log.d("TRAFFIC_APP", "HTTP Response: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    // Read the XML response from the API
                    InputStream inputStream = urlConnection.getInputStream();

                    // Parse the XML using our existing parser
                    ArrayList<Situation> situations =
                            TrafficXmlParser.parse(inputStream);

                    allSituations.clear();
                    allSituations.addAll(situations);

                    for (Situation situation : allSituations) {
                        Log.d("SITUATION_TYPE", "Type: " + situation.getType());
                    }

                    inputStream.close();

                    // Update the RecyclerView on the UI thread
                    MainActivity.this.runOnUiThread(() -> {

                        filterSituations(showingRoadworks);

                    });

                } else {

                Log.e("TRAFFIC_APP",
                        "API request failed. HTTP: " + responseCode);

                MainActivity.this.runOnUiThread(() -> {

                    errorMessage.setText(
                            "Unable to retrieve traffic data. Please try again."
                    );

                    errorMessage.setVisibility(View.VISIBLE);

                });

            }

            } catch (Exception e) {

                Log.e("TRAFFIC_APP",
                        "Error retrieving live traffic data", e);

                MainActivity.this.runOnUiThread(() -> {

                    errorMessage.setText(
                            "Connection failed. Check your internet connection and try again."
                    );

                    errorMessage.setVisibility(View.VISIBLE);

                });

            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                MainActivity.this.runOnUiThread(() -> {

                    loadingProgress.setVisibility(View.GONE);

                    startButton.setEnabled(true);

                });

            }

        }

    }

}

