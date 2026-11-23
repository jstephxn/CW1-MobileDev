package com.example.cw1_trafficapplication_s2534002.adapter;

import com.example.cw1_trafficapplication_s2534002.model.Situation;
import com.example.cw1_trafficapplication_s2534002.R;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.util.ArrayList;

public class TrafficSituationAdapter
        extends RecyclerView.Adapter<TrafficSituationAdapter.ViewHolder> {

    private ArrayList<Situation> situations;
    private OnSituationClickListener listener;

    public interface OnSituationClickListener {
        void onSituationClick(Situation situation);
    }

    // Constructor
    public TrafficSituationAdapter(
            ArrayList<Situation> situations,
            OnSituationClickListener listener
    ) {
        this.situations = situations;
        this.listener = listener;
    }

    // String Formatting methods
    private String formatIncidentType(String type) {

        if (type == null) {
            return "Unknown incident";
        }

        switch (type) {
            case "RoadOrCarriagewayOrLaneManagement":
                return "Road closure / lane management";

            case "AbnormalTraffic":
                return "Abnormal traffic";

            default:
                return type;
        }
    }

    private int getSituationIcon(String type) {

        if (type == null) {
            return R.drawable.ic_traffic_default;
        }

        switch (type) {
            case "RoadOrCarriagewayOrLaneManagement":
                return R.drawable.ic_roadworks;

            case "GeneralObstruction":
                return R.drawable.ic_obstruction;

            case "AbnormalTraffic":
                return R.drawable.ic_abnormal_traffic;

            case "Accident":
                return R.drawable.ic_accident;

            default:
                return R.drawable.ic_traffic_default;
        }
    }

    private String formatDirection(String direction) {

        if (direction == null || direction.isEmpty()) {
            return "Direction unknown";
        }

        switch (direction) {
            case "northBound":
                return "Northbound";

            case "southBound":
                return "Southbound";

            case "eastBound":
                return "Eastbound";

            case "westBound":
                return "Westbound";

            default:
                return direction;
        }
    }

    private String formatStartTime(String startTime) {

        if (startTime == null || startTime.isEmpty()) {
            return "Start time unknown";
        }

        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ssXXX",
                            Locale.UK
                    );

            Date date = inputFormat.parse(startTime);

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat(
                            "dd MMM yyyy, HH:mm",
                            Locale.UK
                    );

            return outputFormat.format(date);

        } catch (ParseException e) {
            return startTime;
        }
    }

    // Create a new card layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_traffic_situation, parent, false);

        return new ViewHolder(view);
    }

    // Populate the card with data
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        Situation situation = situations.get(position);

        holder.incidentTitle.setText(situation.getDescription());
        holder.incidentType.setText(formatIncidentType(situation.getType()));
        holder.incidentDirection.setText(formatDirection(situation.getDirection()));
        holder.incidentStartTime.setText(formatStartTime(situation.getStartTime()));

        holder.incidentIndicator.setBackgroundColor(getSeverityColor(situation.getSeverity()));
        holder.incidentIcon.setImageResource(getSituationIcon(situation.getType()));

        // Handle Card clicks
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSituationClick(situation);
            }
        });
    }

    // Return the number of cards required
    @Override
    public int getItemCount() {
        return situations.size();
    }

    private int getSeverityColor(String severity)
    {
        switch (severity)
        {
            case "HIGH":
                return Color.parseColor("#D32F2F"); // Red

            case "MODERATE":
                return Color.parseColor("#F9A825"); // Amber

            default:
                return Color.parseColor("#9E9E9E"); // Grey
        }
    }
    // Holds references to the card's views
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView incidentTitle;
        TextView incidentType;
        TextView incidentDirection;
        TextView incidentStartTime;
        View incidentIndicator;
        ImageView incidentIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            incidentTitle = itemView.findViewById(R.id.incidentTitle);
            incidentType = itemView.findViewById(R.id.incidentType);
            incidentDirection = itemView.findViewById(R.id.incidentDirection);
            incidentStartTime = itemView.findViewById(R.id.incidentStartTime);
            incidentIndicator = itemView.findViewById(R.id.incidentIndicator);
            incidentIcon = itemView.findViewById(R.id.incidentIcon);
        }
    }
}