package com.openway.square.wrumwrum.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.openway.square.wrumwrum.R;
import com.openway.square.wrumwrum.data.model.Operation;
import com.openway.square.wrumwrum.data.model.ResponseWrapper;
import com.openway.square.wrumwrum.data.model.Tenant;
import com.openway.square.wrumwrum.data.remote.APIService;
import com.openway.square.wrumwrum.data.remote.ApiUtils;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HistoryFragment extends Fragment {

    @BindView(R.id.history_recycler)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_container_history)
    SwipeRefreshLayout swipeContainer;

    private APIService apiService;
    Unbinder unbinder;
    private HistoryRecyclerAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_history, container, false);
        unbinder = ButterKnife.bind(this, inflate);
        apiService = ApiUtils.getAPIService();
        if (getArguments() == null) {
            return inflate;
        }

        final Tenant tenant = (Tenant) getArguments().getSerializable("tenant");
        getHistory(tenant.getFormattedToken());

        adapter = new HistoryRecyclerAdapter(Collections.emptyList());
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        swipeContainer.setOnRefreshListener(() -> getHistory(tenant.getFormattedToken()));

        return inflate;
    }

    private void onFailureLoadHistory(final String message) {
        Toast.makeText(getContext().getApplicationContext(), "Load history failed: " + message, Toast.LENGTH_LONG).show();
        swipeContainer.setRefreshing(false);
    }

    private void onSuccessLoadHistory(List<Operation> operations) {
        adapter.clear();
        adapter.addAll(operations);
        swipeContainer.setRefreshing(false);
    }

    public void getHistory(final String token) {
        apiService.operationsHistory(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<List<Operation>>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailureLoadHistory(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<List<Operation>> responseWrapper) {
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            onFailureLoadHistory(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessLoadHistory(responseWrapper.getRawData());
                        }
                    }
                });
    }
}
