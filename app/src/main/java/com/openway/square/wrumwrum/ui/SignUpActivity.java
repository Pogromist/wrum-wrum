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
import com.openway.square.wrumwrum.data.remote.APIService;
import com.openway.square.wrumwrum.data.remote.ApiUtils;
import com.openway.square.wrumwrum.utils.SharedPrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.input_email)
    EditText emailText;
    @BindView(R.id.input_top_up_card_number)
    EditText passwordText;
    @BindView(R.id.input_reEnterPassword)
    EditText reEnterPasswordText;
    @BindView(R.id.btn_signup)
    Button signUpButton;
    @BindView(R.id.link_login)
    TextView logInLink;

    private ProgressDialog progressDialog;
    private APIService apiService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        apiService = ApiUtils.getAPIService();

        SpannableString content = new SpannableString("Already a member? Login");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        logInLink.setText(content);

        signUpButton.setOnClickListener(v -> signUp());
        logInLink.setOnClickListener(v -> {
            setResult(1);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    public void signUp() {
        final String email = emailText.getText().toString();
        final String password = passwordText.getText().toString();
        final String passwordConfirmation = reEnterPasswordText.getText().toString();

        if (!validateRegistrationData(email, password, passwordConfirmation)) {
            onFailureSignUp("Incorrect format of personal data");
            return;
        }

        signUpButton.setEnabled(false);
        progressDialog = new ProgressDialog(SignUpActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        processRegistration(email, password);
    }

    public boolean validateRegistrationData(final String email, final String password, final String passwordConfirmation) {
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
        if (passwordConfirmation.isEmpty()
                || passwordConfirmation.length() < 8
                || passwordConfirmation.length() > 18
                || !(passwordConfirmation.equals(password))) {
            reEnterPasswordText.setError("Passwords do not match! Please type the same password");
            result = false;
        } else {
            reEnterPasswordText.setError(null);
        }
        return result;
    }

    private void onFailureSignUp(final String message) {
        Toast.makeText(getApplicationContext(), "SignUp failed: " + message, Toast.LENGTH_LONG).show();
        signUpButton.setEnabled(true);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void onSuccessSignUp(final String token) {
        signUpButton.setEnabled(true);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        SharedPrefUtils.saveString(getApplicationContext(), "token", "Token " + token);
        Intent intent = new Intent(SignUpActivity.this, ActivationActivity.class);
        intent.putExtra("token", "Token " + token);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void processRegistration(final String username, final String password) {
        apiService.registerTenant(username, password).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<String>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailureSignUp(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<String> responseWrapper) {
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            onFailureSignUp(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessSignUp(responseWrapper.getRawData());
                        }
                    }
                });
    }
}
