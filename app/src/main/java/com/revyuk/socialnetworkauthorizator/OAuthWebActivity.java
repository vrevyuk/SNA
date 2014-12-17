package com.revyuk.socialnetworkauthorizator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class OAuthWebActivity extends ActionBarActivity {
    WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_web);
        Intent getIntent = getIntent();
        if(getIntent.getStringExtra("requestToken")==null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        String requestTokenUrl = getIntent.getStringExtra("requestToken");
        Log.d("XXX", "Login page:"+requestTokenUrl);
        web = (WebView) findViewById(R.id.twitterWebView);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setLoadsImagesAutomatically(true);
        web.getSettings().setSupportMultipleWindows(true);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                final Uri uri = Uri.parse(url);
                if(uri.getHost().contains("www.revyuk.com")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            intent.putExtra("oauth_verifier", uri.getQueryParameter("oauth_verifier"));
                            intent.putExtra("code", uri.getQueryParameter("code"));
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                }
                return false;
            }
        });
        web.loadUrl(requestTokenUrl);
    }

}
