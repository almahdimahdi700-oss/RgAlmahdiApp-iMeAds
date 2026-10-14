package com.rgalmahdi.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.adsbase.model.AdPreferences;

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

        // ===== 1. تهيئة StartApp - ID حقك =====
        StartAppSDK.init(this, STARTAPP_ID, false);
        StartAppSDK.setTestAdsEnabled(false); // إعلانات حقيقية = فلوس حقيقية
        StartAppAd.disableSplash();
        
        // إعلان بيني
        startAppAd = new StartAppAd(this);
        startAppAd.loadAd(AdPreferences.TYPE.INTERSTITIAL);

        // ===== 2. بانر ثابت فوق - يظهر دائماً حتى لو المدونة ما اشتغلت =====
        startAppBanner = findViewById(R.id.startAppBanner);
        startAppBanner.setBannerListener(new BannerListener() {
            @Override public void onReceiveAd(com.startapp.sdk.adsbase.View view) {}
            @Override public void onFailedToReceiveAd(com.startapp.sdk.adsbase.View view) {
                // لو فشل، حاول مرة ثانية بعد 30 ثانية
                view.postDelayed(() -> startAppBanner.loadAd(), 30000);
            }
            @Override public void onImpression(com.startapp.sdk.adsbase.View view) {}
            @Override public void onClick(com.startapp.sdk.adsbase.View view) {}
        });
        startAppBanner.loadAd(); // حمل البانر فوراً
        
        // إعادة تحميل البانر كل 45 ثانية
        startAppBanner.postDelayed(new Runnable() {
            @Override public void run() {
                try { startAppBanner.loadAd(); } catch (Exception e) {}
                startAppBanner.postDelayed(this, 45000);
            }
        }, 45000);

        // ===== 3. WebView + iMeAds =====
        webView = findViewById(R.id.webview);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
                // حقن iMeAds - يحسب لك داخل الصفحة
                view.evaluateJavascript("(function(){try{var s=document.createElement('script');s.src='https://www.imeads.com/ads.js';s.setAttribute('data-pub','" + IMEADS_PUB + "');s.setAttribute('data-no-close','true');document.body.appendChild(s);}catch(e){}})();", null);
            }
        });
        webView.loadUrl(SITE_URL);
        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        // ===== 4. أزرار برو ماكس الذهبية =====
        findViewById(R.id.btnShare).setOnClickListener(v -> {
            showInterstitialThen(() -> {
                String url = webView.getUrl() != null ? webView.getUrl() : SITE_URL;
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, "✨ رق المهدي - رقيتك الشرعية ✨\n" + url + "\nحمل التطبيق الآن!");
                startActivity(android.content.Intent.createChooser(intent, "مشاركة رق المهدي"));
            });
        });

        findViewById(R.id.btnEye).setOnClickListener(v -> {
            showInterstitialThen(() -> {
                webView.evaluateJavascript("javascript:try{var f=document.body.style.filter;document.body.style.filter=f.includes('1.6')?'':'brightness(1.6) sepia(0.2)'}catch(e){}", null);
                Toast.makeText(this, "👁️ وضع راحة العين", Toast.LENGTH_SHORT).show();
            });
        });

        findViewById(R.id.btnTTS).setOnClickListener(v -> {
            try {
                if (textToSpeech != null && textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                    Toast.makeText(this, "⏹️ توقف", Toast.LENGTH_SHORT).show();
                    return;
                }
                webView.evaluateJavascript("(function(){try{return document.body.innerText.substring(0,3500)}catch(e){return ''}})();", value -> {
                    String text = value.replaceAll("^\"|\"$", "").replace("\\n", " ").trim();
                    if (text.length() > 5) {
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "RqAlMahdi");
                        Toast.makeText(this, "🔊 يقرأ الآن... اضغط مرة أخرى للإيقاف", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {}
        });

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) textToSpeech.setLanguage(new Locale("ar"));
        });
    }

    // دالة الإعلان البيني - تظهر الإعلان ثم تنفذ الحركة
    private void showInterstitialThen(Runnable action) {
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
            .setMessage("هل تريد الخروج فعلاً؟\nلا تنسى ذكر الله والصلاة على النبي ﷺ")
            .setPositiveButton("خروج 🚪", (d, w) -> showInterstitialThen(() -> finishAffinity()))
            .setNegativeButton("البقاء 📖", null)
            .setNeutralButton("مشاركة التطبيق", (d,w) -> {
                android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_TEXT, "حمل رق المهدي برو ماكس: https://rgalmahdi.blogspot.com/");
                startActivity(i);
            })
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try { if (startAppBanner != null) startAppBanner.loadAd(); } catch (Exception e) {}
    }

    @Override
    protected void onDestroy() {
        try { if (textToSpeech != null) { textToSpeech.stop(); textToSpeech.shutdown(); } } catch (Exception e) {}
        super.onDestroy();
    }
}
