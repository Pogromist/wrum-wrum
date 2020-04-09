package com.openway.square.wrumwrum.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.openway.square.wrumwrum.R;
import com.openway.square.wrumwrum.data.model.ResponseWrapper;
import com.openway.square.wrumwrum.data.model.Scooter;
import com.openway.square.wrumwrum.data.model.Tariff;
import com.openway.square.wrumwrum.data.model.Tenant;
import com.openway.square.wrumwrum.data.remote.APIService;
import com.openway.square.wrumwrum.data.remote.ApiUtils;
import com.openway.square.wrumwrum.ui.active_rental.ActiveRentalFragment;
import com.openway.square.wrumwrum.ui.active_rental.ActiveRentalFragmentListener;
import com.openway.square.wrumwrum.ui.history.HistoryFragment;
import com.openway.square.wrumwrum.ui.maps.MapsFragment;
import com.openway.square.wrumwrum.ui.maps.MapsFragmentListener;
import com.openway.square.wrumwrum.ui.profile.ProfileFragment;
import com.openway.square.wrumwrum.ui.rating.RatingFragment;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BottomNavigationActivity extends AppCompatActivity
        implements MapsFragmentListener, ActiveRentalFragmentListener {

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private final Fragment fragmentProfile = new ProfileFragment();
    private final Fragment fragmentMaps = new MapsFragment();
    private final Fragment fragmentHistory = new HistoryFragment();
    private final Fragment fragmentRating = new RatingFragment();
    private final Fragment fragmentActiveRental = new ActiveRentalFragment();
    private Fragment activeFragment = fragmentMaps;

    @BindView(R.id.bottom_sheet_layout)
    LinearLayout layoutBottomSheet;
    @BindView(R.id.tv_scooter_number)
    TextView textViewScooterNumber;
    @BindView(R.id.tv_scooter_fuel)
    TextView textViewScooterFuel;
    @BindView(R.id.tv_scooter_coordinates)
    TextView textViewScooterCoordinates;
    @BindView(R.id.rgTariffs)
    RadioGroup radioGroupTariffs;
    @BindView(R.id.fab_rent)
    FloatingActionButton fabRent;

    public BottomSheetBehavior sheetTariffs;
    private APIService apiService;

    private TreeMap<Integer, Tariff> tariffs = new TreeMap<>();
    private Scooter currentScooter;
    private Tenant tenant;
    private Tariff currentTariff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_bottom);
        ButterKnife.bind(this);
        apiService = ApiUtils.getAPIService();

        tenant = (Tenant) getIntent().getSerializableExtra("tenant");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.getMenu().getItem(1).setChecked(true);

        Bundle bundle = new Bundle();
        bundle.putSerializable("tenant", tenant);
        fragmentProfile.setArguments(bundle);
        fragmentActiveRental.setArguments(bundle);
        fragmentMaps.setArguments(bundle);
        fragmentHistory.setArguments(bundle);
        fragmentRating.setArguments(bundle);

//        ViewGroup.LayoutParams params = layoutBottomSheet.getLayoutParams();
//        params.width = bottomNavigationView.getMeasuredWidth();
//        layoutBottomSheet.setLayoutParams(params);
        sheetTariffs = BottomSheetBehavior.from(layoutBottomSheet);
        sheetTariffs.setFitToContents(true);

        fragmentManager.beginTransaction().add(R.id.main_container, fragmentProfile, "1").hide(fragmentProfile).commit();
        fragmentManager.beginTransaction().add(R.id.main_container, fragmentHistory, "3").hide(fragmentHistory).commit();
        fragmentManager.beginTransaction().add(R.id.main_container, fragmentRating, "4").hide(fragmentRating).commit();
        if (tenant.getActiveRent() == null) {
            fragmentManager.beginTransaction().add(R.id.main_container, fragmentActiveRental, "5").hide(fragmentActiveRental).commit();
            fragmentManager.beginTransaction().add(R.id.main_container, fragmentMaps, "2").commit();
            activeFragment = fragmentMaps;
            sheetTariffs.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showFab();
        } else {
            fragmentManager.beginTransaction().add(R.id.main_container, fragmentMaps, "2").hide(fragmentMaps).commit();
            fragmentManager.beginTransaction().add(R.id.main_container, fragmentActiveRental, "5").commit();
            activeFragment = fragmentActiveRental;
            sheetTariffs.setState(BottomSheetBehavior.STATE_HIDDEN);
            hideFab();
        }
        getTariffs(tenant.getFormattedToken());
        fabRent.setOnClickListener(fabOnClickListener);
    }

    @Override
    public void onBackPressed() {
        if (sheetTariffs.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetTariffs.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    private FloatingActionButton.OnClickListener
            fabOnClickListener = fab -> {
        if (currentScooter == null) {
            Toast.makeText(getApplicationContext(), "Choose scooter", Toast.LENGTH_LONG).show();
            return;
        }
        if (tariffs.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Tariffs not loaded", Toast.LENGTH_LONG).show();
            return;
        }
        final RadioGroup rg = findViewById(R.id.rgTariffs);
        currentTariff = tariffs.get(rg.getCheckedRadioButtonId());
        rentScooter(tenant.getFormattedToken(), currentTariff.getId(), currentScooter.getId());
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = menuItem -> {
        switch (menuItem.getItemId()) {
            case R.id.action_profile:
                fragmentManager.beginTransaction().hide(activeFragment).show(fragmentProfile).commit();
                activeFragment = fragmentProfile;
                sheetTariffs.setState(BottomSheetBehavior.STATE_HIDDEN);
                hideFab();
                break;
            case R.id.action_maps:
                if (tenant.getActiveRent() == null) {
                    fragmentManager.beginTransaction().hide(activeFragment).show(fragmentMaps).commit();
                    activeFragment = fragmentMaps;
                    sheetTariffs.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showFab();
                } else {
                    fragmentManager.beginTransaction().hide(activeFragment).show(fragmentActiveRental).commit();
                    activeFragment = fragmentActiveRental;
                    sheetTariffs.setState(BottomSheetBehavior.STATE_HIDDEN);
                    hideFab();
                }
                break;
            case R.id.action_history:
                fragmentManager.beginTransaction().hide(activeFragment).show(fragmentHistory).commit();
                activeFragment = fragmentHistory;
                sheetTariffs.setState(BottomSheetBehavior.STATE_HIDDEN);
                hideFab();
                break;
            case R.id.action_rating:
                fragmentManager.beginTransaction().hide(activeFragment).show(fragmentRating).commit();
                activeFragment = fragmentRating;
                sheetTariffs.setState(BottomSheetBehavior.STATE_HIDDEN);
                hideFab();
                break;
            default:
                activeFragment = fragmentMaps;
                sheetTariffs.setState(BottomSheetBehavior.STATE_COLLAPSED);
                hideFab();
        }
        return true;
    };

    @SuppressLint("SetTextI18n")
    @Override
    public void scooterClicked(Scooter scooter) {
        currentScooter = scooter;
        sheetTariffs.setState(BottomSheetBehavior.STATE_EXPANDED);
        textViewScooterNumber.setText("Scooter #" + scooter.getNumber().toString());
        textViewScooterFuel.setText("Fuel " + scooter.getFuel().toString() + "%");
        textViewScooterCoordinates.setText(scooter.getLat().toString() + ", " + scooter.getLng().toString());
    }

    @Override
    public void onFinishRental() {
        tenant.setActiveRent(null);
        MapsFragment mapsFragment = (MapsFragment) fragmentMaps;
        mapsFragment.getScooters(tenant.getFormattedToken());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out);
        ft.hide(activeFragment).show(fragmentMaps).commit();
        activeFragment = fragmentMaps;
        sheetTariffs.setState(BottomSheetBehavior.STATE_COLLAPSED);
        showFab();
    }

    private void changeFragment() {

    }

    private void onFailureLoadTariffs(final String message) {
        Toast.makeText(getApplicationContext(), "Load tariffs failed: " + message, Toast.LENGTH_LONG).show();
    }

    private void onSuccessLoadTariffs(List<Tariff> loadedTariffs) {
        for (Tariff t : loadedTariffs) {
            tariffs.put(t.getId(), t);
        }
        if (tenant.getActiveRent() != null) {
            ActiveRentalFragment activeRentalFragment = (ActiveRentalFragment) fragmentActiveRental;
            activeRentalFragment.showTariffInfo(tariffs.get(tenant.getActiveRent().getTariffId()));
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        int i = 0;
        for (Tariff t : tariffs.values()) {
            RadioButton rb = new RadioButton(this);
            final String price = format.format(t.getPrice());
            String time;
            if (t.getDays() != 0) {
                time = t.getDays().toString() + " d.";
            } else if (t.getHours() != 0) {
                time = t.getHours().toString() + " h.";
            } else {
                time = t.getMinutes().toString() + " min.";
            }
            rb.setText(t.getTitle() + ": " + price + "/" + time + " (" + (t.getPost() ? "Postpayment" : "Prepayment") + ")");
            rb.setId(t.getId());
            if (i++ == 0) {
                rb.setChecked(true);
            }
            rb.setTextSize(18);
            radioGroupTariffs.addView(rb);
        }
    }

    public void getTariffs(final String token) {
        apiService.getTariffs(token).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<List<Tariff>>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        onFailureLoadTariffs(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<List<Tariff>> responseWrapper) {
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            onFailureLoadTariffs(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessLoadTariffs(responseWrapper.getRawData());
                        }
                    }
                });
    }

    private void onFailureRental(final String message) {
        Toast.makeText(getApplicationContext(), "Rental failed: " + message, Toast.LENGTH_LONG).show();
    }

    private void onSuccessRental(final Tenant tenant) {
        this.tenant = tenant;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
        ft.hide(activeFragment).show(fragmentActiveRental).commit();
        activeFragment = fragmentActiveRental;
        sheetTariffs.setState(BottomSheetBehavior.STATE_HIDDEN);
        hideFab();
        ActiveRentalFragment activeRentalFragment = (ActiveRentalFragment) fragmentActiveRental;
        activeRentalFragment.showTimerInfo(this.tenant.getActiveRent());
        activeRentalFragment.showTariffInfo(tariffs.get(tenant.getActiveRent().getTariffId()));
    }

    public void rentScooter(final String token, final Integer tariffId, final Integer scooterId) {
        apiService.chooseScooter(token, tariffId, scooterId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<Tenant>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailureRental(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<Tenant> responseWrapper) {
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            if (responseWrapper.getData().getMessage().equals("Scooter is already taken")) {
                                MapsFragment mapsFragment = (MapsFragment) fragmentMaps;
                                mapsFragment.getScooters(tenant.getFormattedToken());
                            }
                            onFailureRental(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessRental(responseWrapper.getRawData());
                        }
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    private void hideFab() {
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fabRent.getLayoutParams();
        p.setBehavior(null);
        p.setAnchorId(View.NO_ID);
        fabRent.setLayoutParams(p);
        fabRent.setVisibility(View.GONE);
    }

    @SuppressLint("RestrictedApi")
    private void showFab() {
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fabRent.getLayoutParams();
        p.setBehavior(new FloatingActionButton.Behavior());
        p.setAnchorId(R.id.bottom_sheet_layout);
        fabRent.setLayoutParams(p);
        fabRent.setVisibility(View.VISIBLE);
    }
}
