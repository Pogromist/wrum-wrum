package com.openway.square.wrumwrum.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.braintreepayments.cardform.view.CardForm;
import com.openway.square.wrumwrum.R;
import com.openway.square.wrumwrum.data.model.ResponseWrapper;
import com.openway.square.wrumwrum.data.model.Tenant;
import com.openway.square.wrumwrum.data.remote.APIService;
import com.openway.square.wrumwrum.data.remote.ApiUtils;
import com.openway.square.wrumwrum.ui.LoginActivity;
import com.openway.square.wrumwrum.utils.SharedPrefUtils;

import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ProfileFragment extends Fragment {

    private APIService apiService;
    Unbinder unbinder;

    @BindView(R.id.top_up)
    Button startTopUpButton;
    @BindView(R.id.cv_top_up_form)
    CardView topUpForm;
    @BindView(R.id.input_topup_amount)
    EditText amountTopUpText;
    @BindView(R.id.top_up_card_form)
    CardForm topUpCardForm;
    @BindView(R.id.btn_top_up)
    Button topUpButton;
    @BindView(R.id.withdrawal)
    Button startWithdrawalButton;
    @BindView(R.id.cv_withdrawal_form)
    CardView withdrawalForm;
    @BindView(R.id.input_withdrawal_amount)
    EditText amountWithdrawalText;
    @BindView(R.id.withdrawal_card_form)
    CardForm withdrawalCardForm;
    @BindView(R.id.btn_withdrawal)
    Button withdrawButton;
    @BindView(R.id.tv_balance)
    TextView textViewBalance;
    @BindView(R.id.layout_progress)
    RelativeLayout progressBar;
    @BindView(R.id.tv_tenant_username)
    TextView textViewTenantUsername;
    @BindView(R.id.ib_log_out)
    ImageButton imageButtonLogout;
    @BindView(R.id.ib_balance_update)
    ImageButton imageButtonBalanceUpdate;

    Tenant tenant;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder = ButterKnife.bind(this, inflate);
        apiService = ApiUtils.getAPIService();
        if (getArguments() == null) {
            return inflate;
        }

        tenant = (Tenant) getArguments().getSerializable("tenant");
        getBalance(tenant.getFormattedToken());
        textViewTenantUsername.setText(tenant.getUsername());

        final Activity activity = getActivity();
        topUpCardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .setup(activity);
        topUpCardForm.getCvvEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        withdrawalCardForm.cardRequired(true).setup(activity);

        imageButtonLogout.setOnClickListener(v -> {
            SharedPrefUtils.remove(getContext().getApplicationContext(), "token");
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        imageButtonBalanceUpdate.setOnClickListener(v -> getBalance(tenant.getFormattedToken()));

        startTopUpButton.setOnClickListener(v -> onStartTopUpClick());
        startWithdrawalButton.setOnClickListener(v -> onStartWithdrawalClick());

        topUpButton.setOnClickListener(v -> {
            if (amountTopUpText.getText().length() == 0) {
                Toast.makeText(getContext().getApplicationContext(), "To withdraw money fields should not be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            final Float amount = Float.parseFloat(amountTopUpText.getText().toString());
            final String cardNumber = topUpCardForm.getCardNumber();
            final String dueDate = topUpCardForm.getExpirationDateEditText().getText().toString();
            final String cvv = topUpCardForm.getCvv();
            topUpWallet(amount, cardNumber, dueDate, cvv, tenant.getFormattedToken());
        });

        withdrawButton.setOnClickListener(v -> {
            if (amountWithdrawalText.getText().length() == 0) {
                Toast.makeText(getContext().getApplicationContext(), "To withdraw money fields should not be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            final Float amount = Float.parseFloat(amountWithdrawalText.getText().toString());
            final String cardNumber = withdrawalCardForm.getCardNumber();
            withdrawalMoney(amount, cardNumber, tenant.getFormattedToken());
        });
        return inflate;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textViewBalance = getView().findViewById(R.id.tv_balance);
    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Min balance info!")
                .setMessage("You should have at least 200.00 USD in your balance to start the trip")
                .setIcon(R.drawable.logo)
                .setCancelable(false)
                .setNegativeButton("OK", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void onStartTopUpClick() {
        if (topUpForm.getVisibility() == CardView.INVISIBLE) {
            startTopUpButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            startWithdrawalButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            topUpForm.setVisibility(CardView.VISIBLE);
            withdrawalForm.setVisibility(CardView.INVISIBLE);
        } else {
            startTopUpButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            topUpForm.setVisibility(CardView.INVISIBLE);
            withdrawalForm.setVisibility(CardView.INVISIBLE);
        }
    }

    private void onStartWithdrawalClick() {
        if (withdrawalForm.getVisibility() == CardView.INVISIBLE) {
            startWithdrawalButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            startTopUpButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            topUpForm.setVisibility(CardView.INVISIBLE);
            withdrawalForm.setVisibility(CardView.VISIBLE);
        } else {
            startWithdrawalButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            topUpForm.setVisibility(CardView.INVISIBLE);
            withdrawalForm.setVisibility(CardView.INVISIBLE);
        }
    }

    private void onFailureBalance(final String message) {
        Toast.makeText(getContext().getApplicationContext(), "Getting balance failed: " + message, Toast.LENGTH_LONG).show();
    }

    private void onSuccessBalance(final Float balance) {
        if (textViewBalance != null) {
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
            final String currency = format.format(balance);
            textViewBalance.setText(currency);
        }
        if (balance < 200) {
            showAlert();
        }
    }

    private void getBalance(final String token) {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        apiService.getBalance(token).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<Float>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        onFailureBalance(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<Float> responseWrapper) {
                        progressBar.setVisibility(RelativeLayout.INVISIBLE);
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            onFailureBalance(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessBalance(responseWrapper.getRawData());
                        }
                    }
                });
    }

    private void onFailureTopUp(final String message) {
        Toast.makeText(getContext().getApplicationContext(), "Top up failed: " + message, Toast.LENGTH_LONG).show();
    }

    private void onSuccessTopUp() {
        Toast.makeText(getContext().getApplicationContext(), "Successful top up", Toast.LENGTH_SHORT).show();
        getBalance(tenant.getFormattedToken());
    }

    private void topUpWallet(final Float amount,
                             final String cardNumber,
                             final String dueDate,
                             final String cvv,
                             final String token) {
        progressBar.setVisibility(RelativeLayout.VISIBLE);
        apiService.topUpWallet(amount, cardNumber, dueDate, cvv, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<String>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(RelativeLayout.INVISIBLE);
                        onFailureTopUp(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<String> responseWrapper) {
                        progressBar.setVisibility(RelativeLayout.INVISIBLE);
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            onFailureTopUp(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessTopUp();
                        }
                    }
                });
    }

    private void onFailureWithdraw(final String message) {
        Toast.makeText(getContext().getApplicationContext(), "Withdrawal failed: " + message, Toast.LENGTH_LONG).show();
    }

    private void onSuccessWithdraw() {
        Toast.makeText(getContext().getApplicationContext(), "Successful withdrawal", Toast.LENGTH_SHORT).show();
        getBalance(tenant.getFormattedToken());
    }

    private void withdrawalMoney(final Float amount,
                                 final String cardNumber,
                                 final String token) {
        progressBar.setVisibility(RelativeLayout.VISIBLE);
        apiService.withdrawalOfMoney(amount, cardNumber, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseWrapper<String>>() {
                    @Override
                    public void onCompleted() {
                        // ignore
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(RelativeLayout.INVISIBLE);
                        onFailureWithdraw("ERROR: " + e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(ResponseWrapper<String> responseWrapper) {
                        progressBar.setVisibility(RelativeLayout.INVISIBLE);
                        if (responseWrapper.getData().getStatus().equals("error")) {
                            onFailureWithdraw(responseWrapper.getData().getMessage());
                        } else {
                            onSuccessWithdraw();
                        }
                    }
                });
    }
}
