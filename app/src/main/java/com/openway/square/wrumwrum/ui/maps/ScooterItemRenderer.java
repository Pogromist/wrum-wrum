package com.openway.square.wrumwrum.ui.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.openway.square.wrumwrum.R;

public class ScooterItemRenderer extends DefaultClusterRenderer<ClusterItemMarker> {

    private Context context;

    public ScooterItemRenderer(Context context, GoogleMap map, ClusterManager<ClusterItemMarker> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterItemMarker item, MarkerOptions markerOptions) {
        Integer resIconId;
        if (item.getSnippet().equals("1")) {
            resIconId = R.drawable.ic_scooter_red_52;
        } else if (item.getSnippet().equals("2")) {
            resIconId = R.drawable.ic_scooter_yellow_52;
        } else {
            resIconId = R.drawable.ic_scooter_green_52;
        }
        BitmapDescriptor scooterIcon = bitmapDescriptorFromVector(context, resIconId);
        markerOptions.icon(scooterIcon);
    }
}
