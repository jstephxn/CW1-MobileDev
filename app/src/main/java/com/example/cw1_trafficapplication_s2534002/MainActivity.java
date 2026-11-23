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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Locale;

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

    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermission();

        Button incidentsButton = findViewById(R.id.incidentsButton);
        Button roadworksButton = findViewById(R.id.roadworksButton);
        incidentsButton.setOnClickListener(v -> filterSituations(false));
        roadworksButton.setOnClickListener(v -> filterSituations(true));

    }

    private float calculateDistance(Situation situation) {

        if (userLocation == null ||
                situation.getLatitude() == null ||
                situation.getLongitude() == null) {
            return -1;
        }

        Location situationLocation = new Location("situation");

        situationLocation.setLatitude(situation.getLatitude());
        situationLocation.setLongitude(situation.getLongitude());

        float distance = userLocation.distanceTo(situationLocation);

        return distance;
    }

    private String formatDistance(float distance) {

        if (distance < 0) {
            return "Distance unavailable";
        }

        if (distance < 1000) {
            return Math.round(distance) + " m away";
        }

        return String.format(Locale.UK, "%.1f km away", distance / 1000);
    }

    private void updateSituationDistances() {

        if (userLocation == null || allSituations == null) {
            return;
        }

        for (Situation situation : allSituations) {
            float distance = calculateDistance(situation);
            situation.setDistanceFromUser(distance);
        }
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

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_REQUEST) {

            if (grantResults.length > 0) {

                boolean permissionGranted = false;

                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = true;
                        break;
                    }
                }

                if (permissionGranted) {
                    getUserLocation();
                }
            }
        }
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

        // Sort by distance (nearest first), unavailable distances last
        filteredSituations.sort((s1, s2) -> {

            float d1 = s1.getDistanceFromUser();
            float d2 = s2.getDistanceFromUser();

            if (d1 < 0 && d2 < 0) return 0;
            if (d1 < 0) return 1;
            if (d2 < 0) return -1;

            return Float.compare(d1, d2);
        });

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

    private void getUserLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                )
                .addOnSuccessListener(location -> {

                    if (location != null) {
                        userLocation = location;
                        updateSituationDistances();

                        filterSituations(showingRoadworks);

                    }
                });
    }

    private void requestLocationPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            getUserLocation();

        } else {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );
        }
    }


    private class Task implements Runnable
    {
        public Task() { }

        @Override
        public void run() {

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

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    // Read the XML response from the API
                    InputStream inputStream = urlConnection.getInputStream();

                    // Parse the XML using our existing parser
                    ArrayList<Situation> situations =
                            TrafficXmlParser.parse(inputStream);

                    allSituations.clear();
                    allSituations.addAll(situations);

                    inputStream.close();

                    // Update the RecyclerView on the UI thread
                    // Update the RecyclerView on the UI thread
                    MainActivity.this.runOnUiThread(() -> {

                        updateSituationDistances();

                        filterSituations(showingRoadworks);

                    });

                } else {

                MainActivity.this.runOnUiThread(() -> {

                    errorMessage.setText(
                            "Unable to retrieve traffic data. Please try again."
                    );

                    errorMessage.setVisibility(View.VISIBLE);

                });

            }

            } catch (Exception e) {

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

