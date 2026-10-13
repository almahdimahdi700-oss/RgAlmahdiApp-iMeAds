package com.rgalmahdi.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private StartAppAd startAppAd;
    private TextToSpeech textToSpeech;
    
    public static final String SITE_URL = "https://rgalmahdi.blogspot.com/";
    public static final String IMEADS_PUB = "pub_X_V2BG-WWEDsbgkBc8qHng";
    public static final String STARTAPP_ID = "209205885";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            StartAppSDK.init(this, STARTAPP_ID, false);
            StartAppSDK.setTestAdsEnabled(false);
            StartAppAd.disableSplash();
            startAppAd = new StartAppAd(this);
            startAppAd.loadAd();
        } catch (Exception e) {}

        webView = findViewById(R.id.webview);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
                try {
                    view.evaluateJavascript("(function(){var s=document.createElement('script');s.src='https://www.imeads.com/ads.js';s.setAttribute('data-pub','" + IMEADS_PUB + "');s.setAttribute('data-no-close','true');document.body.appendChild(s);})();", null);
                } catch (Exception ex) {}
            }
        });
        webView.loadUrl(SITE_URL);
        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        if (findViewById(R.id.btnShare) != null) {
            findViewById(R.id.btnShare).setOnClickListener(v -> {
                showAdThen(() -> {
                    try {
                        String url = webView.getUrl() != null ? webView.getUrl() : SITE_URL;
                        String shareText = "✨ رق المهدي - رقيتك الشرعية ✨\n" + url;
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
                        startActivity(android.content.Intent.createChooser(intent, "مشاركة"));
                    } catch (Exception e) {}
                });
            });
        }

        if (findViewById(R.id.btnEye) != null) {
            findViewById(R.id.btnEye).setOnClickListener(v -> {
                showAdThen(() -> {
                    try {
                        webView.evaluateJavascript("javascript: (function(){ var f=document.body.style.filter; document.body.style.filter = f.includes('brightness(1.6)') ? 'brightness(1)' : 'brightness(1.6) sepia(0.2)'; })();", null);
                        Toast.makeText(this, "👁️ وضع راحة العين", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {}
                });
            });
        }

        if (findViewById(R.id.btnTTS) != null) {
            findViewById(R.id.btnTTS).setOnClickListener(v -> {
                try {
                    if (textToSpeech != null && textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                        Toast.makeText(this, "⏹️ تم الإيقاف", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    webView.evaluateJavascript("(function(){return document.body.innerText.substring(0,3500);})();", value -> {
                        try {
                            String text = value.replaceAll("^\"|\"$", "").replace("\\n", " ").trim();
                            if (text.length() > 5 && textToSpeech != null) {
                                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "RqAlMahdi");
                                Toast.makeText(this, "🔊 يقرأ الآن...", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {}
                    });
                } catch (Exception e) {}
            });
        }

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                try { textToSpeech.setLanguage(new Locale("ar")); } catch (Exception e) {}
            }
        });
    }

    private void showAdThen(Runnable action) {
        try {
            if (startAppAd != null) {
                startAppAd.showAd(new AdDisplayListener() {
                    @Override public void adHidden(com.startapp.sdk.adsbase.Ad ad) { action.run(); startAppAd.loadAd(); }
                    @Override public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {}
                    @Override public void adClicked(com.startapp.sdk.adsbase.Ad ad) {}
                    @Override public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) { action.run(); startAppAd.loadAd(); }
                });
            } else action.run();
        } catch (Exception e) { action.run(); }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) { webView.goBack(); return; }
        new AlertDialog.Builder(this)
            .setTitle("💎 رق المهدي برو ماكس")
            .setMessage("هل تريد الخروج فعلاً؟\nلا تنسى ذكر الله ﷺ")
            .setIcon(R.mipmap.ic_launcher)
            .setPositiveButton("نعم، خروج 🚪", (d, w) -> {
                try {
                    if (startAppAd != null) {
                        startAppAd.showAd(new AdDisplayListener() {
                            @Override public void adHidden(com.startapp.sdk.adsbase.Ad ad) { finishAffinity(); }
                            @Override public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {}
                            @Override public void adClicked(com.startapp.sdk.adsbase.Ad ad) {}
                            @Override public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) { finishAffinity(); }
                        });
                    } else finishAffinity();
                } catch (Exception e) { finishAffinity(); }
            })
            .setNegativeButton("البقاء 📖", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        try { if (textToSpeech != null) { textToSpeech.stop(); textToSpeech.shutdown(); } } catch (Exception e) {}
        super.onDestroy();
    }
}
