package com.rgalmahdi.app;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private StartAppAd startAppAd;
    private int actionCount = 0;
    public static final String SITE_URL = "https://rg-almahdi.ahmed-mahdi.com/";
    public static final String IMEADS_PUB = "pub_X_V2BG-WWEDsbgkBc8qHng";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start.io - 204445944
        StartAppSDK.init(this, "204445944", false);
        StartAppSDK.setTestAdsEnabled(false);
        StartAppAd.disableSplash();
        startAppAd = new StartAppAd(this);

        webView = findViewById(R.id.webview);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipeRefreshLayout.setRefreshing(false);
                // iMeAds pub_X_V2BG-WWEDsbgkBc8qHng يظل مفتوح ولا يسكر ابدا
                view.evaluateJavascript(
                    "(function(){var s=document.createElement('script');s.src='https://www.imeads.com/ads.js';s.setAttribute('data-pub','" + IMEADS_PUB + "');s.setAttribute('data-no-close','true');document.body.appendChild(s);})();"
                , null);
            }
        });

        webView.loadUrl(SITE_URL);

        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        // زر المشاركة و زر العين يزود العداد
        findViewById(R.id.btnShare).setOnClickListener(v -> {
            actionCount++;
            if (actionCount % 2 == 0) startAppAd.showAd();
        });
        
        findViewById(R.id.btnEye).setOnClickListener(v -> {
            actionCount++;
            if (actionCount % 3 == 0) startAppAd.showAd();
        });
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else {
            startAppAd.showAd();
            super.onBackPressed();
        }
    }
}
