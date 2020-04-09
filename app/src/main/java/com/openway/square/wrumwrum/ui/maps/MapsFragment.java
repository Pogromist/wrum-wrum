package com.openway.square.wrumwrum.ui.maps;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.openway.square.wrumwrum.R;
import com.openway.square.wrumwrum.data.model.ResponseWrapper;
import com.openway.square.wrumwrum.data.model.Scooter;
import com.openway.square.wrumwrum.data.model.Tenant;
import com.openway.square.wrumwrum.data.remote.APIService;
import com.openway.square.wrumwrum.data.remote.ApiUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MapsFragment extends Fragment implements
        OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap gMap;
    private ClusterManager<ClusterItemMarker> clusterManager;
    private HashMap<ClusterItemMarker, Scooter> scootersMarkerMap = new HashMap<>();
    private APIService apiService;
    private MapsFragmentListener mapsFragmentListener;

    Tenant tenant;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_maps, container, false);
        mapsFragmentListener = (MapsFragmentListener) getActivity();
        apiService = ApiUtils.getAPIService();
        if (getArguments() == null) {
            return inflate;
        }

        mapView = inflate.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        try {
            MapsInitializer.initialize(Objects.requireNonNull(getActivity()).getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);

        tenant = (Tenant) getArguments().getSerializable("tenant");
        getScooters(tenant.getFormattedToken());
        return inflate;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        setUpClusterer();
        // TODO: gMap.setMyLocationEnabled(true);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(59.963350, 30.333780))
                .zoom(12)
                .build();
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void setUpClusterer() {
        clusterManager = new ClusterManager<>(Objects.requireNonNull(getActivity()), gMap);
        clusterManager.setOnClusterClickListener(cluster -> {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(cluster.getPosition()).zoom(12).build();
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            return true;
        });
        clusterManager.setOnClusterItemClickListener(marker -> {
            if (scootersMarkerMap.containsKey(marker)) {
                mapsFragmentListener.scooterClicked(scootersMarkerMap.get(marker));
            }
            return true;
        });
        gMap.setOnMarkerClickListener(clusterManager);
        gMap.setOnCameraIdleListener(clusterManager);
        ScooterItemRenderer renderer = new ScooterItemRenderer(getActivity(), gMap, clusterManager);
        clusterManager.setRenderer(renderer);
        // TODO: clusterManager.cluster();
    }

    private void onFailureGetScooters(final String message) {
        Toast.makeText(getContext().getApplicationContext(), "Getting scooters failed: " + message, Toast.LENGTH_LONG).show();
    }

    private void onSuccessGetScooters(List<Scooter> scooters) {
        clusterManager.clearItems();
        for (Scooter sc : scooters) {
            String fuelLevel = (sc.getFuel() < 25.0) ? "1" : ((sc.getFuel() < 75.0) ? "2" : "3");
            ClusterItemMarker marker = new ClusterItemMarker(sc.getLat(), sc.getLng(), sc.getNumber().toString(), fuelLevel);
            clusterManager.addItem(marker);
            scootersMarkerMap.put(marker, sc);
        }
    }

    public void getScooters(final String token) {
        apiService.getScooters(token).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<List<Scooter>>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailureGetScooters(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<List<Scooter>> responseWrapper) {
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            onFailureGetScooters(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessGetScooters(responseWrapper.getRawData());
                        }
                    }
                });
    }
}
