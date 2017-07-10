package com.example.kabs_.radiov2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.webkit.WebViewClient;

//Twitter
public class Main3Activity extends AppCompatActivity {
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        WebView webView = (WebView) findViewById(R.id.mWebView);//aqui deberia aparecer webView wn vez de nav_view
        //pero no se le da la gana de aparercer
        webView.loadUrl("https://mobile.twitter.com/notidelllano");
        webView.getSettings().getJavaScriptEnabled();
        webView.setWebViewClient(new WebViewClient());
    }
}
