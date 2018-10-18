package com.omninos.raile.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.firebase.iid.FirebaseInstanceId;
import com.omninos.raile.Constants.AppConstants;
import com.omninos.raile.Model.UserRegisterModel;
import com.omninos.raile.R;
import com.omninos.raile.Retrofit.ApiService;
import com.omninos.raile.Retrofit.RetrofitApi;
import com.omninos.raile.Util.CommonUtil;
import com.omninos.raile.Util.ShowSnackBar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {

    private Button submit;
    private Activity activity = RegistrationActivity.this;
    private EditText name, phone, countrycode;
    public static int APP_REQUEST_CODE = 99;
    private LinearLayout registration_parent;
    String reg_id, sname, number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        initviews();
        //Clicklistners
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateInfo(v);
//                phoneLogin(v);
            }
        });

    }

    private void initviews() {
        name = findViewById(R.id.registration_fullname);
        // phone = findViewById(R.id.registration_phonenumber);
        //countrycode = findViewById(R.id.registration_countrycode);
        registration_parent = findViewById(R.id.registration_parent);
        submit = findViewById(R.id.submit);

    }

    private void ValidateInfo(View view) {

        sname = name.getText().toString();
        reg_id = FirebaseInstanceId.getInstance().getToken();

        if (sname.isEmpty()) {
            name.setError(AppConstants.mandatoryField);
            ShowSnackBar.longSnackBar(registration_parent, activity, AppConstants.mandatoryField);
        } else {
            phoneLogin(view);
        }
    }


    public void phoneLogin(final View view) {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
                //showErrorActivity(loginResult.getError());
            } else if (loginResult.wasCancelled()) {
                toastMessage = "Login Cancelled";
            } else {
                if (loginResult.getAccessToken() != null) {
                    toastMessage = "Success:" + loginResult.getAccessToken().getAccountId();

                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            number = account.getPhoneNumber().toString();
                            Log.d("onSuccess: ", number);
                            RegisterApi();

                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {

                        }
                    });

                } else {
                    toastMessage = String.format(
                            "Success:%s...",
                            loginResult.getAuthorizationCode().substring(0, 10));
                }


            }

            // Surface the result to your user in an appropriate way.
            Toast.makeText(
                    this,
                    toastMessage,
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void RegisterApi() {
        if (CommonUtil.isNetworkConnected(RegistrationActivity.this)) {
            ApiService apiService = RetrofitApi.getApi().create(ApiService.class);
            CommonUtil.showProgress(RegistrationActivity.this, "Please wait...");
            Call<UserRegisterModel> call = apiService.userregister(sname, number, "normal", "android", reg_id);

            call.enqueue(new Callback<UserRegisterModel>() {
                @Override
                public void onResponse(Call<UserRegisterModel> call, Response<UserRegisterModel> response) {
                    if (response.body().getSuccess().equalsIgnoreCase("1")) {
                        CommonUtil.dismissProgress();
                        CommonUtil.setID(response.body().getDetails().getId(),RegistrationActivity.this);
                        CommonUtil.SetPhone(RegistrationActivity.this,response.body().getDetails().getPhone());

                        Toast.makeText(RegistrationActivity.this, response.body().getMessage() + " "+response.body().getDetails().getId(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegistrationActivity.this, NewHomeActivity.class));
                    } else {
                        Toast.makeText(RegistrationActivity.this, response.body().getMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserRegisterModel> call, Throwable t) {
                    CommonUtil.dismissProgress();
                    Toast.makeText(RegistrationActivity.this, t + "", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(RegistrationActivity.this, "Network Issue", Toast.LENGTH_SHORT).show();

        }
    }
    }
