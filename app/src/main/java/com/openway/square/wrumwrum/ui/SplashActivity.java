package com.openway.square.wrumwrum.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.openway.square.wrumwrum.R;
import com.openway.square.wrumwrum.data.model.ResponseWrapper;
import com.openway.square.wrumwrum.data.model.Tenant;
import com.openway.square.wrumwrum.data.remote.APIService;
import com.openway.square.wrumwrum.data.remote.ApiUtils;
import com.openway.square.wrumwrum.utils.SharedPrefUtils;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.openway.square.wrumwrum.utils.SharedPrefUtils.SHARED_PREFERENCES_KEY;

public class SplashActivity extends Activity {

    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (isLoggedIn()) {
            apiService = ApiUtils.getAPIService();
            final String token = SharedPrefUtils.getString(getApplicationContext(), "token");
            processAuthorization(token);
        } else {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
                overridePendingTransition(R.anim.fadeout, R.anim.fadein);
            }, 500);
        }
        super.onCreate(savedInstanceState);
    }

    private boolean isLoggedIn() {
        return getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE).contains("token");
    }

    private void onFailureLogIn(final String message) {
        Toast.makeText(getApplicationContext(), "Authorization failed: " + message, Toast.LENGTH_LONG).show();
    }

    private void onSuccessLogIn(final Tenant tenant) {
        Toast.makeText(getApplicationContext(), "Authorization success", Toast.LENGTH_SHORT).show();
        Intent startupIntent = new Intent(SplashActivity.this, BottomNavigationActivity.class);
        startupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startupIntent.putExtra("tenant", tenant);
        startActivity(startupIntent);
        finish();
        overridePendingTransition(R.anim.fadeout, R.anim.fadein);
    }

    private void processAuthorization(final String token) {
        apiService.logInTenant(token)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<Tenant>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailureLogIn(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<Tenant> responseWrapper) {
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            onFailureLogIn(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessLogIn(responseWrapper.getRawData());
                        }
                    }
                });
    }
}
