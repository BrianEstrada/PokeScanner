package com.pokescanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class GoogleLoginActivity extends AppCompatActivity {

    //
    // CODE BROUGHT TO YOU BY @langerhans
    //
    public static final String EXTRA_CODE = "extra_code";
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth?scope=openid%20email%20https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email&redirect_uri=http://127.0.0.1:9004&response_type=code&client_id=848232511240-73ri3t7plvk96pj4f85uj8otdat2alem.apps.googleusercontent.com";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_login);

        WebView view = (WebView) findViewById(R.id.webView);

        view.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://127.0.0.1")) {
                    Uri uri = Uri.parse(url);

                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_CODE, uri.getQueryParameter("code"));
                    setResult(RESULT_OK, intent);
                    finish();

                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        view.loadUrl(AUTH_URL);
    }
}