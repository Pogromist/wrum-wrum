package com.openway.square.wrumwrum.ui.maps;

import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

public class ClusterItemMarkerBounce implements ClusterManager.OnClusterItemClickListener {

    private final Handler mHandler;
    private Runnable mAnimation;

    public ClusterItemMarkerBounce() {
        mHandler = new Handler();
    }

    @Override
    public boolean onClusterItemClick(ClusterItem clusterItem) {
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500L;
        mHandler.removeCallbacks(mAnimation);
        mAnimation = new BounceAnimation(start, duration, clusterItem, mHandler);
        mHandler.post(mAnimation);
        return false;
    }

    private static class BounceAnimation implements Runnable {

        private final long mStart, mDuration;
        private final Interpolator mInterpolator;
        private final ClusterItem mMarker;
        private final Handler mHandler;

        private BounceAnimation(long start, long duration, ClusterItem marker, Handler handler) {
            mStart = start;
            mDuration = duration;
            mMarker = marker;
            mHandler = handler;
            mInterpolator = new BounceInterpolator();
        }

        @Override
        public void run() {
            long elapsed = SystemClock.uptimeMillis() - mStart;
            float t = Math.max(1 - mInterpolator.getInterpolation((float) elapsed / mDuration), 0f);
            // mMarker.setAnchor(0.5f, 1.0f + 1.2f * t);

            if (t > 0.0) {
                // Post again 16ms later.
                mHandler.postDelayed(this, 16L);
            }
        }
    }
}
