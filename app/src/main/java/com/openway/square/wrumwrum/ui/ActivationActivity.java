package com.openway.square.wrumwrum.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.openway.square.wrumwrum.R;
import com.openway.square.wrumwrum.data.model.ResponseWrapper;
import com.openway.square.wrumwrum.data.model.Tenant;
import com.openway.square.wrumwrum.data.remote.APIService;
import com.openway.square.wrumwrum.data.remote.ApiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ActivationActivity extends AppCompatActivity {

    @BindView(R.id.input_top_up_card_number)
    EditText activationCode;
    @BindView(R.id.btn_activation)
    Button activationButton;

    private ProgressDialog progressDialog;
    private APIService apiService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);
        ButterKnife.bind(this);
        apiService = ApiUtils.getAPIService();

        final String token = getIntent().getStringExtra("token");
        activationButton.setOnClickListener(v -> processActivation(token));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
    }

    private void processActivation(final String token) {
        final String code = activationCode.getText().toString();

        activationButton.setEnabled(false);
        progressDialog = new ProgressDialog(ActivationActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Activation...");
        progressDialog.show();

        processActivation(code, token);
    }

    public void onFailureActivation(final String message) {
        Toast.makeText(getApplicationContext(), "Activation failed: " + message, Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
        activationButton.setEnabled(true);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void onSuccessActivation(final Tenant tenant) {
        Toast.makeText(getApplicationContext(), "Account activated", Toast.LENGTH_SHORT).show();
        activationButton.setEnabled(true);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Intent startupIntent = new Intent(ActivationActivity.this, BottomNavigationActivity.class);
        startupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startupIntent.putExtra("tenant", tenant);
        startActivity(startupIntent);
        finish();
        overridePendingTransition(R.anim.fadeout, R.anim.fadein);
    }

    private void processActivation(final String code, final String token) {
        apiService.activateTenant(code, token).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<Tenant>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailureActivation(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<Tenant> responseWrapper) {
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            onFailureActivation(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessActivation(responseWrapper.getRawData());
                        }
                    }
                });
    }
}
