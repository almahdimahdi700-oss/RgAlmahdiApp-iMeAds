package com.rgalmahdi.app;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private StartAppAd startAppAd;
    private int actionCount = 0;
    public static final String SITE_URL = "https://rgalmahdi.blogspot.com/";
    public static final String IMEADS_PUB = "pub_X_V2BG-WWEDsbgkBc8qHng";
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StartAppSDK.init(this, "209205885", false);
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
                view.evaluateJavascript(
                    "(function(){var s=document.createElement('script');s.src='https://www.imeads.com/ads.js';s.setAttribute('data-pub','" + IMEADS_PUB + "');s.setAttribute('data-no-close','true');document.body.appendChild(s);})();"
                , null);
            }
        });

        webView.loadUrl(SITE_URL);
        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        findViewById(R.id.btnShare).setOnClickListener(v -> {
            actionCount++;
            if (actionCount % 2 == 0) startAppAd.showAd();
        });
        
        findViewById(R.id.btnEye).setOnClickListener(v -> {
            actionCount++;
            if (actionCount % 3 == 0) startAppAd.showAd();
        });

        // === إضافة القارئ 🔊 ===
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("ar"));
            }
        });

        findViewById(R.id.btnTTS).setOnClickListener(v -> {
            webView.evaluateJavascript("document.body.innerText", value -> {
                String text = value.replaceAll("\"", "");
                if (text.length() > 4000) text = text.substring(0, 4000);
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                Toast.makeText(this, "جاري القراءة 🔊", Toast.LENGTH_SHORT).show();
            });
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

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
