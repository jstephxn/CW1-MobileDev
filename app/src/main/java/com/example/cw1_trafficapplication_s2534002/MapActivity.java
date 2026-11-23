package com.example.cw1_trafficapplication_s2534002;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cw1_trafficapplication_s2534002.model.Situation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private GoogleMap googleMap;
    private ArrayList<Situation> situations;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        situations = (ArrayList<Situation>)
                getIntent().getSerializableExtra("situations");

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null)
        {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        googleMap = map;

        LatLng scotland = new LatLng(56.4907, -4.2026);

        googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(scotland, 6f)
        );

        displaySituationMarkers();
    }

    private void displaySituationMarkers()
    {
        if (situations == null || situations.isEmpty())
        {
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        int markerCount = 0;

        for (Situation situation : situations)
        {
            Double latitude = situation.getLatitude();
            Double longitude = situation.getLongitude();

            // Skip situations without valid coordinates
            if (latitude == null || longitude == null)
            {
                continue;
            }

            LatLng position = new LatLng(latitude, longitude);

            float markerColour;

            switch (situation.getSeverity())
            {
                case "HIGH":
                    markerColour = BitmapDescriptorFactory.HUE_RED;
                    break;

                case "MODERATE":
                    markerColour = BitmapDescriptorFactory.HUE_ORANGE;
                    break;

                default:
                    markerColour = BitmapDescriptorFactory.HUE_AZURE;
                    break;
            }

            String description = situation.getDescription() != null
                    ? situation.getDescription()
                    : "Traffic situation";

            googleMap.addMarker(
                    new MarkerOptions()
                            .position(position)
                            .title(description)
                            .snippet("Severity: " + situation.getSeverity())
                            .icon(BitmapDescriptorFactory.defaultMarker(markerColour))
            );

            boundsBuilder.include(position);
            markerCount++;
        }

        // Adjust camera to show all markers
        if (markerCount > 1)
        {
            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                            boundsBuilder.build(),
                            100
                    )
            );
        }
        else if (markerCount == 1)
        {
            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            boundsBuilder.build().getCenter(),
                            10f
                    )
            );
        }
    }
}