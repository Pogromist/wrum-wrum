package com.openway.square.wrumwrum.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.openway.square.wrumwrum.R;
import com.openway.square.wrumwrum.data.model.ResponseWrapper;
import com.openway.square.wrumwrum.data.model.Tenant;
import com.openway.square.wrumwrum.data.remote.APIService;
import com.openway.square.wrumwrum.data.remote.ApiUtils;
import com.openway.square.wrumwrum.utils.SharedPrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.input_email)
    EditText emailText;
    @BindView(R.id.input_top_up_card_number)
    EditText passwordText;
    @BindView(R.id.btn_login)
    Button loginButton;
    @BindView(R.id.link_signup)
    TextView signUpLink;

    private ProgressDialog progressDialog;
    private APIService apiService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        apiService = ApiUtils.getAPIService();

        SpannableString content = new SpannableString("No account yet? Create one");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        signUpLink.setText(content);

        loginButton.setOnClickListener(v -> processLogIn());
        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });
    }

    public void processLogIn() {
        final String email = emailText.getText().toString();
        final String password = passwordText.getText().toString();

        if (!validateLogInData(email, password)) {
            onFailureLogIn("Invalid data");
            return;
        }

        loginButton.setEnabled(false);
        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        processAuthorization(email, password);
    }

    public boolean validateLogInData(final String email, final String password) {
        boolean result = true;
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Enter a valid email address");
            result = false;
        } else {
            emailText.setError(null);
        }
        if (password.isEmpty() || password.length() < 8 || password.length() > 18) {
            passwordText.setError("Between 8 and 18 alphanumeric characters");
            result = false;
        } else {
            passwordText.setError(null);
        }
        return result;
    }

    public void onFailureLogIn(final String message) {
        Toast.makeText(getApplicationContext(), "Authorization failed: " + message, Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void onSuccessLogIn(Tenant tenant) {
        Toast.makeText(getApplicationContext(), "Authorization success", Toast.LENGTH_SHORT).show();
        SharedPrefUtils.saveString(getApplicationContext(), "token", "Token " + tenant.getToken());
        loginButton.setEnabled(true);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (tenant.getActivated()) {
            Intent startupIntent = new Intent(LoginActivity.this, BottomNavigationActivity.class);
            startupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startupIntent.putExtra("tenant", tenant);
            startActivity(startupIntent);
            finish();
            overridePendingTransition(R.anim.fadeout, R.anim.fadein);
        } else {
            Intent intent = new Intent(LoginActivity.this, ActivationActivity.class);
            intent.putExtra("token", "Token " + tenant.getToken());
            startActivity(intent);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    }

    private void processAuthorization(final String username, final String password) {
        apiService.logInTenant(username, password)
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
