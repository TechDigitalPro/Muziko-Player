package com.whitebird.music.activities.user;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.hbb20.CountryCodePicker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.BaseActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;

import static com.whitebird.music.utils.StaticData.BASE_URL;
import static com.whitebird.music.utils.StaticData.REGISTER;

public class RegisterActivity extends BaseActivity implements ATEActivityThemeCustomizer, View.OnClickListener {

    private ImageView img_user;
    private File imageFile = null;
    private EditText et_name, etEmail, et_phone, et_password;
    private Button btn_register;
    private CountryCodePicker ccp_code;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setUpUi();
    }

    private void setUpUi() {
        img_user = findViewById(R.id.img_user);
        et_name = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.etEmail);
        et_phone = findViewById(R.id.et_phone);
        et_password = findViewById(R.id.et_password);
        btn_register = findViewById(R.id.btn_register);
        ccp_code = findViewById(R.id.ccp_code);
        img_user.setOnClickListener(this);
        btn_register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == img_user) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(RegisterActivity.this);
        } else if (v == btn_register) {
            if (et_name.length() == 0) {
                et_name.setError("Please fill!");
                return;
            } else if (etEmail.length() == 0) {
                etEmail.setError("Please fill!");
                return;
            } else if (et_phone.length() == 0) {
                et_phone.setError("Please fill!");
                return;
            } else if (et_password.length() == 0) {
                et_password.setError("Please fill!");
                return;
            } else if (imageFile == null) {
                MuzikoApp.toast(RegisterActivity.this, "Please select Image!");
                return;
            }
            RequestParams params = new RequestParams();
            params.put("name", et_name.getText().toString());
            params.put("email", etEmail.getText().toString());
            params.put("phone", ccp_code.getSelectedCountryCode() + et_phone.getText().toString());
            params.put("password", et_password.getText().toString());

            try {
                params.put("pic", imageFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            postCall(RegisterActivity.this, BASE_URL+REGISTER, params, "Registering...");
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                img_user.setImageURI(resultUri);
                imageFile = new File(resultUri.getPath());

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
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
                        UserData userData = new UserData();
                        userData.Id = response.getJSONObject("data").getInt("id");
                        userData.Name = response.getJSONObject("data").getString("name");
                        userData.Email = response.getJSONObject("data").getString("email");
                        userData.Phone = response.getJSONObject("data").getString("phone");
                        userData.Pic = response.getJSONObject("data").getString("pic");
                        userData.Password = response.getJSONObject("data").getString("password");
                        userData.IsLogin=true;
                        MuzikoApp.writeUser(userData);
                        MuzikoApp.toast(RegisterActivity.this, response.getString("message"));
                        finish();
                    } else {
                        MuzikoApp.toast(RegisterActivity.this, response.getString("message"));
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

                } catch (Exception e) {
                    Toast.makeText(c, "Slow internet connection ! \nPlease try again", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                MuzikoApp.spinnerStop();
                Log.d("error message:", throwable.getMessage());
                try {

                } catch (Exception e) {
                    Toast.makeText(c, "Slow internet connection ! \nPlease try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
