package com.openway.square.wrumwrum.ui.maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterItemMarker implements ClusterItem {

    private final LatLng position;
    private String title;
    private String snippet;

    public ClusterItemMarker(double lat, double lng) {
        position = new LatLng(lat, lng);
    }

    public ClusterItemMarker(double lat, double lng, String title, String snippet) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }
}
