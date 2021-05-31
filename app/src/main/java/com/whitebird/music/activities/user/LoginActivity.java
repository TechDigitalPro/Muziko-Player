package com.whitebird.music.activities.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.whitebird.music.utils.StaticData.BASE_URL;
import static com.whitebird.music.utils.StaticData.LOGIN;

public class LoginActivity extends BaseActivity  implements ATEActivityThemeCustomizer, View.OnClickListener {

    private Button btn_register,btn_login;
    private EditText  et_mail, et_password;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpUi();
    }

    private void setUpUi() {
        btn_register=findViewById(R.id.btn_register);
        btn_login=findViewById(R.id.btn_login);
        btn_register.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        et_mail = findViewById(R.id.et_mail);
        et_password = findViewById(R.id.et_password);
        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v==btn_register){
            startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
        }else if (v==btn_login){
            if (et_mail.length() == 0) {
                et_mail.setError("Please fill!");
                return;
            }else if (et_password.length() == 0) {
                et_password.setError("Please fill!");
                return;
            }
            RequestParams params = new RequestParams();

            params.put("email", et_mail.getText().toString());
            params.put("password", et_password.getText().toString());


            postCall(LoginActivity.this, BASE_URL+LOGIN, params, "Logging...");

        }

    }

    @Override
    public int getActivityTheme() {
        return R.style.AppThemeNormalLight;
    }

    public void postCall(final Context c, String url, RequestParams params, String loadingMsg) {
        if (!MuzikoApp.isConnectingToInternet(c)) {
            Toast.makeText(c, "No internet connection !", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(loadingMsg))
            MuzikoApp.spinnerStart(c, loadingMsg);
        Log.e("URl:", url);
        Log.e("Request:", params.toString());
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);
        //    client.addHeader("Content-Type","application/x-www-form-urlencoded");
        client.post(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                MuzikoApp.spinnerStop();
                Log.d("Response:", response.toString());

                try {
                    if (response.getBoolean("status")) {
                        UserData userData=new UserData();
                        userData.Id=response.getJSONObject("data").getInt("id");
                        userData.Name=response.getJSONObject("data").getString("name");
                        userData.Email=response.getJSONObject("data").getString("email");
                        userData.Phone=response.getJSONObject("data").getString("phone");
                        userData.Pic=response.getJSONObject("data").getString("pic");
                        userData.Password = response.getJSONObject("data").getString("password");
                        userData.IsLogin=true;
                        MuzikoApp.writeUser(userData);
                        MuzikoApp.toast(LoginActivity.this, response.getString("message"));

                        finish();

                    } else {
                        MuzikoApp.toast(LoginActivity.this, response.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                MuzikoApp.spinnerStop();
                Log.d("error message:", throwable.getMessage());
                try {
                    MuzikoApp.toast(c, "Something went wrong ! \nPlease try again");
                } catch (Exception e) {
                    MuzikoApp.toast(c, "Slow internet connection ! \nPlease try again");
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                MuzikoApp.spinnerStop();
                Log.d("error message:", throwable.getMessage());
                try {
                    MuzikoApp.toast(c, "Something went wrong ! \nPlease try again");
                } catch (Exception e) {
                    MuzikoApp.toast(c, "Slow internet connection ! \nPlease try again");
                }
            }
        });
    }
}
