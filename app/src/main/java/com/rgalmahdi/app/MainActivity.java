package com.rgalmahdi.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.Locale;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.adsbase.Ad;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextToSpeech textToSpeech;
    private StartAppAd startAppAd;
    private Banner startAppBanner;

    public static final String SITE_URL = "https://rgalmahdi.blogspot.com/";
    public static final String IMEADS_PUB = "pub_X_V2BG-WWEDsbgkBc8qHng";
    public static final String STARTAPP_ID = "209205885";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // تهيئة StartApp - تشتغل حتى لو النت ضعيف
        StartAppSDK.init(this, STARTAPP_ID, false);
        StartAppSDK.setTestAdsEnabled(false);
        StartAppAd.disableSplash();
        startAppAd = new StartAppAd(this);
        startAppAd.loadAd();

        // البانر الثابت الذهبي فوق - يظهر دائماً
        startAppBanner = findViewById(R.id.startAppBanner);
        if (startAppBanner != null) {
            startAppBanner.loadAd();
            startAppBanner.postDelayed(new Runnable() {
                @Override public void run() {
                    try { if (startAppBanner != null) startAppBanner.loadAd(); } catch (Exception e) {}
                    if (startAppBanner != null) startAppBanner.postDelayed(this, 45000);
                }
            }, 45000);
        }

        // WebView + iMeAds الذهبي
        webView = findViewById(R.id.webview);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipeRefreshLayout.setRefreshing(false);
                String js = "(function(){try{var s=document.createElement('script');s.src='https://www.imeads.com/ads.js';s.setAttribute('data-pub','" + IMEADS_PUB + "');s.setAttribute('data-no-close','true');document.body.appendChild(s);}catch(e){}})();";
                view.evaluateJavascript(js, null);
                try { if (startAppBanner != null) startAppBanner.loadAd(); } catch (Exception e) {}
            }
        });
        webView.loadUrl(SITE_URL);
        swipeRefreshLayout.setOnRefreshListener(() -> { if (webView != null) webView.reload(); });

        // زر المشاركة الذهبي + إعلان بيني
        findViewById(R.id.btnShare).setOnClickListener(v -> {
            showInterstitialThen(() -> {
                try {
                    String shareUrl = webView.getUrl(); if (shareUrl == null) shareUrl = SITE_URL;
                    Intent intent = new Intent(Intent.ACTION_SEND); intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "✨ رق المهدي ✨\n" + shareUrl);
                    startActivity(Intent.createChooser(intent, "مشاركة رق المهدي 📖"));
                } catch (Exception e) {}
            });
        });

        // زر راحة العين الذهبي + إعلان بيني
        findViewById(R.id.btnEye).setOnClickListener(v -> {
            showInterstitialThen(() -> {
                try {
                    String eyeJs = "javascript:try{var f=document.body.style.filter;if(f.includes('brightness')){document.body.style.filter='';}else{document.body.style.filter='brightness(1.6) sepia(0.2) contrast(0.9)';}}catch(e){}";
                    webView.evaluateJavascript(eyeJs, null);
                    Toast.makeText(this, "👁️ تم تغيير وضع راحة العين", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {}
            });
        });

        // زر القارئ الصوتي الذهبي + إعلان بيني
        findViewById(R.id.btnTTS).setOnClickListener(v -> {
            showInterstitialThen(() -> {
                try {
                    if (textToSpeech != null && textToSpeech.isSpeaking()) { textToSpeech.stop(); Toast.makeText(this, "⏹️ تم إيقاف القراءة", Toast.LENGTH_SHORT).show(); return; }
                    webView.evaluateJavascript("(function(){try{return document.body.innerText.substring(0,3800);}catch(e){return '';}})();", value -> {
                        try {
                            String text = value.replaceAll("^\"|\"$", "").replace("\\n", " ").trim().replace("\\", "").trim();
                            if (text.length() > 10) { if (textToSpeech != null) { textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "RqAlMahdi_GOLD"); Toast.makeText(this, "🔊 يقرأ الآن...", Toast.LENGTH_LONG).show(); } }
                        } catch (Exception e) {}
                    });
                } catch (Exception e) {}
            });
        });

        textToSpeech = new TextToSpeech(this, status -> { if (status == TextToSpeech.SUCCESS) { try { textToSpeech.setLanguage(new Locale("ar")); textToSpeech.setSpeechRate(0.9f); } catch (Exception e) {} } });
    }

    private void showInterstitialThen(Runnable action) {
        try {
            if (startAppAd != null) {
                startAppAd.showAd(new AdDisplayListener() {
                    @Override public void adHidden(Ad ad) { action.run(); try { startAppAd.loadAd(); } catch (Exception e) {} }
                    @Override public void adDisplayed(Ad ad) {}
                    @Override public void adClicked(Ad ad) {}
                    @Override public void adNotDisplayed(Ad ad) { action.run(); try { startAppAd.loadAd(); } catch (Exception e) {} }
                });
            } else action.run();
        } catch (Exception e) { action.run(); try { if (startAppAd != null) startAppAd.loadAd(); } catch (Exception ex) {} }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) { webView.goBack(); return; }
        new AlertDialog.Builder(this).setTitle("💎 رق المهدي برو ماكس الذهبي").setMessage("هل تريد الخروج فعلاً؟\n\nلا تنسى الأذكار :\nرق المهدي").setPositiveButton("خروج 🚪", (d, w) -> showInterstitialThen(() -> finishAffinity())).setNegativeButton("البقاء 📖", (d, w) -> d.dismiss()).setCancelable(false).show();
    }
    @Override protected void onResume() { super.onResume(); try { if (startAppBanner != null) startAppBanner.loadAd(); } catch (Exception e) {} }
    @Override protected void onDestroy() { try { if (textToSpeech != null) { textToSpeech.stop(); textToSpeech.shutdown(); } } catch (Exception e) {} super.onDestroy(); }
}
