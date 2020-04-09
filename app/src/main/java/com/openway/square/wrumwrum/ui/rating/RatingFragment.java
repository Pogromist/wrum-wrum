package com.openway.square.wrumwrum.ui.rating;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.openway.square.wrumwrum.R;
import com.openway.square.wrumwrum.data.model.RatingData;
import com.openway.square.wrumwrum.data.model.ResponseWrapper;
import com.openway.square.wrumwrum.data.model.Tenant;
import com.openway.square.wrumwrum.data.remote.APIService;
import com.openway.square.wrumwrum.data.remote.ApiUtils;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RatingFragment extends Fragment {

    @BindView(R.id.rating_recycler)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_container_rating)
    SwipeRefreshLayout swipeContainer;
    @BindView(R.id.tv_rating_your_points)
    TextView textViewTenantPoints;
    @BindView(R.id.tv_rating_your_place)
    TextView textViewTenantPlace;
    @BindView(R.id.tv_rating_end_date)
    TextView textViewTenantEndDate;

    private APIService apiService;
    Unbinder unbinder;
    private RatingRecyclerAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    public RatingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_rating, container, false);
        unbinder = ButterKnife.bind(this, inflate);
        apiService = ApiUtils.getAPIService();
        if (getArguments() == null) {
            return inflate;
        }

        final Tenant tenant = (Tenant) getArguments().getSerializable("tenant");
        getRating(tenant.getFormattedToken());

        RatingData ratingData = new RatingData();
        ratingData.setTopTenants(Collections.emptyList());
        ratingData.setYourPlace(0);
        ratingData.setYourPoints(0);
        ratingData.setCompetitionEnds("");

        adapter = new RatingRecyclerAdapter(ratingData);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        swipeContainer.setOnRefreshListener(() -> getRating(tenant.getFormattedToken()));

        return inflate;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void onFailureLoadRating(final String message) {
        Toast.makeText(getContext().getApplicationContext(), "Load history failed: " + message, Toast.LENGTH_LONG).show();
        swipeContainer.setRefreshing(false);
    }

    private void onSuccessLoadRating(RatingData ratingData) {
        textViewTenantPoints.setText("Your points: " + ratingData.getYourPoints().toString());
        textViewTenantPlace.setText("Your place: " + ratingData.getYourPlace().toString());
        textViewTenantEndDate.setText("Competition ends " + ratingData.getCompetitionEnds());
        adapter.clear();
        adapter.update(ratingData);
        swipeContainer.setRefreshing(false);
    }

    public void getRating(final String token) {
        apiService.getRating(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<RatingData>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailureLoadRating(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<RatingData> responseWrapper) {
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            onFailureLoadRating(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessLoadRating(responseWrapper.getRawData());
                        }
                    }
                });
    }
}
