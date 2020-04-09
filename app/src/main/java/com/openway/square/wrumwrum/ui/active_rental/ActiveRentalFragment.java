package com.openway.square.wrumwrum.ui.active_rental;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.openway.square.wrumwrum.R;
import com.openway.square.wrumwrum.data.model.Rent;
import com.openway.square.wrumwrum.data.model.ResponseWrapper;
import com.openway.square.wrumwrum.data.model.Tariff;
import com.openway.square.wrumwrum.data.model.Tenant;
import com.openway.square.wrumwrum.data.remote.APIService;
import com.openway.square.wrumwrum.data.remote.ApiUtils;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ActiveRentalFragment extends Fragment {

    private APIService apiService;
    Unbinder unbinder;
    private ActiveRentalFragmentListener activeRentalFragmentListener;

    @BindView(R.id.layout_progress)
    RelativeLayout progressBar;
    @BindView(R.id.btn_unlock)
    Button buttonUnlockScooter;
    @BindView(R.id.btn_finish_ride)
    Button buttonFinishRide;

    @BindView(R.id.timer_label)
    TextView timerLabel;
    @BindView(R.id.chrono)
    Chronometer timer;
    @BindView(R.id.scooter_num)
    TextView scooterNumber;
    @BindView(R.id.scooter_tariff)
    TextView tariffTitle;
    @BindView(R.id.tax)
    TextView tariff_price;
    @BindView(R.id.tariff_time)
    TextView tariff_time;

    private Rent activeRent;

    public ActiveRentalFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_active_rental, container, false);
        activeRentalFragmentListener = (ActiveRentalFragmentListener) getActivity();
        unbinder = ButterKnife.bind(this, inflate);
        apiService = ApiUtils.getAPIService();
        if (getArguments() == null) {
            return inflate;
        }

        final Tenant tenant = (Tenant) getArguments().getSerializable("tenant");
        buttonUnlockScooter.setOnClickListener(button -> unlockScooter(tenant.getFormattedToken()));
        buttonFinishRide.setOnClickListener(button -> finishRide(tenant.getFormattedToken()));

        if (tenant.getActiveRent() != null) {
            showTimerInfo(tenant.getActiveRent());
        }
        return inflate;
    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @SuppressLint("SetTextI18n")
    public void showTimerInfo(final Rent rent) {
        this.activeRent = rent;
        buttonUnlockScooter.setEnabled(true);
        buttonFinishRide.setEnabled(true);
        scooterNumber.setText("Scooter #" + activeRent.getScooterNumber().toString());
        try {
            configureTimer(activeRent.getStartDate());
        } catch (Exception e) {
            Toast.makeText(getContext().getApplicationContext(), "Cannot parse date", Toast.LENGTH_SHORT).show();
        }
        timer.start();
    }

    public void showTariffInfo(final Tariff tariff) {
        tariffTitle.setText(tariff.getTitle());
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        final String currency = format.format(tariff.getPrice());
        tariff_price.setText(currency);
        tariff_time.setText(convertTime(tariff.getDays(), tariff.getHours(), tariff.getMinutes()));
    }

    private String convertTime(final Integer days, final Integer hours, final Integer minutes) {
        return "per " + (days > 0 ? (days.toString() + " days ") : "") +
                (hours > 0 ? (hours.toString() + " hours ") : "") +
                (minutes > 0 ? (minutes.toString() + " minutes") : "");
    }

    private void configureTimer(final String startDateString) throws Exception {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date startDate = dateFormatter.parse(startDateString);
        if (startDate == null) {
            Toast.makeText(getContext().getApplicationContext(), "Cannot parse date", Toast.LENGTH_SHORT).show();
            return;
        }
        Date now = new Date(System.currentTimeMillis());
        final long delta = now.getTime() - startDate.getTime();
        if (delta < 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                timer.setCountDown(true);
                timer.setBase(SystemClock.elapsedRealtime() - delta);
                timer.setOnChronometerTickListener(v -> {
                    Log.i("timer", v.getText().toString());
                    if (v.getText().toString().equals("−00:01")) {
                        onSuccessFinishRental("Rental is finished. You did not have time to get to the scooter!");
                    }
                });
                timerLabel.setText(R.string.time_of_reservation);
            } else {
                Toast.makeText(getContext().getApplicationContext(), "Old version", Toast.LENGTH_SHORT).show();
            }
        } else {
            timer.setBase(SystemClock.elapsedRealtime() - delta);
        }
    }

    private void onFailureUnlockScooter(final String message) {
        Toast.makeText(getContext().getApplicationContext(), "Rental failed: " + message, Toast.LENGTH_LONG).show();
    }

    private void onSuccessUnlockScooter(final String message) {
        buttonUnlockScooter.setEnabled(false);
        buttonFinishRide.setEnabled(true);
        timerLabel.setText(R.string.travel_time);
        timer.setBase(SystemClock.elapsedRealtime());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            timer.setCountDown(false);
        }
        Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void unlockScooter(final String token) {
        show_progress();
        apiService.unlockScooter(token).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<String>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismiss_progress();
                        onFailureUnlockScooter(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<String> responseWrapper) {
                        dismiss_progress();
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            onFailureUnlockScooter(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessUnlockScooter("Unlocked");
                        }
                    }
                });
    }

    private void onFailureFinishRental(final String message) {
        Toast.makeText(getContext().getApplicationContext(), "Finish ride failed: " + message, Toast.LENGTH_LONG).show();
    }

    private void onSuccessFinishRental(final String message) {
        Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        timer.stop();
        buttonUnlockScooter.setEnabled(false);
        buttonFinishRide.setEnabled(false);
        activeRentalFragmentListener.onFinishRental();
    }

    private void finishRide(final String token) {
        show_progress();
        apiService.finishRent(token).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<String>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismiss_progress();
                        onFailureFinishRental(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<String> responseWrapper) {
                        dismiss_progress();
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            onFailureFinishRental(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessFinishRental("Finished");
                        }
                    }
                });
    }

    private void show_progress() {
        progressBar.setVisibility(RelativeLayout.VISIBLE);
    }

    private void dismiss_progress() {
        progressBar.setVisibility(RelativeLayout.INVISIBLE);
    }
}
