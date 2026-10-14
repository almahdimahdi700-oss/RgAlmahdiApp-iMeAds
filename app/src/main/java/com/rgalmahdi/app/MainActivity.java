package com.rgalmahdi.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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

        // === 1. StartApp برو ماكس - يشتغل حتى لو النت ضعيف ===
        StartAppSDK.init(this, STARTAPP_ID, false);
        StartAppSDK.setTestAdsEnabled(false);
        StartAppAd.disableSplash();
        startAppAd = new StartAppAd(this);
        startAppAd.loadAd();

        startAppBanner = findViewById(R.id.startAppBanner);
        if (startAppBanner != null) {
            startAppBanner.loadAd();
            startAppBanner.postDelayed(new Runnable() {
                @Override public void run() {
                    try { startAppBanner.loadAd(); } catch (Exception e) {}
                    startAppBanner.postDelayed(this, 45000);
                }
            }, 45000);
        }

        // === 2. WebView الذهبي + iMe الجديد اللي يحسب ===
        webView = findViewById(R.id.webview);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipeRefreshLayout.setRefreshing(false);
                
                // === حقن iMe الجديد الذهبي - هذا اللي يحسب في صورتك ===
                String imeNew = "(function(){try{" +
                        "if(window.iMeAdsLayer==undefined){" +
                        "window.iMeAdsLayer=[];window.imeads=function(){iMeAdsLayer.push(arguments);};" +
                        "var s=document.createElement('script');s.async=true;" +
                        "s.src='https://ad.api.imem.me/static/pixel-v2.min.js';" +
                        "s.setAttribute('data-pk','" + IMEADS_PUB + "');" +
                        "document.head.appendChild(s);" +
                        "setTimeout(function(){try{imeads('page_view');imeads('new_user');}catch(e){}},2000);" +
                        "}else{try{imeads('page_view');}catch(e){}}" +
                        "}catch(e){}})();";
                view.evaluateJavascript(imeNew, null);
                
                try { if (startAppBanner != null) startAppBanner.loadAd(); } catch (Exception e) {}
            }
        });
        webView.loadUrl(SITE_URL);
        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        // === 3. زر المشاركة الذهبي الإبداعي ===
        findViewById(R.id.btnShare).setOnClickListener(v -> showGoldAd(() -> {
            try {
                String u = webView.getUrl(); if (u == null) u = SITE_URL;
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, "✨ رق المهدي ✨\n" +
                        "📖 نافذتك إلى عالم الفن والأفلام والمسلسلات\n" +
                        "🔗 " + u + "\n\n#رق_المهدي");
                startActivity(Intent.createChooser(i, "مشاركة ذهبية 📖"));
                Toast.makeText(this, "💎 شكراً لمشاركتك", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {}
        }));

        // === 4. زر راحة العين الذهبي الإبداعي ===
        findViewById(R.id.btnEye).setOnClickListener(v -> showGoldAd(() -> {
            String js = "javascript:try{" +
                    "var b=document.body;" +
                    "var f=b.style.filter;" +
                    "if(f.includes('brightness')){b.style.filter='';b.style.backgroundColor='';}" +
                    "else{b.style.filter='brightness(1.5) sepia(0.15) contrast(0.95)';b.style.backgroundColor='#FFF8E1';}" +
                    "}catch(e){}";
            webView.evaluateJavascript(js, null);
            Toast.makeText(this, "👁️ وضع راحة العين الذهبي", Toast.LENGTH_SHORT).show();
        }));

        // === 5. زر القارئ الصوتي الذهبي الخارق ===
        findViewById(R.id.btnTTS).setOnClickListener(v -> showGoldAd(() -> {
            try {
                if (textToSpeech != null && textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                    Toast.makeText(this, "⏹️ تم إيقاف القراءة الذهبية", Toast.LENGTH_SHORT).show();
                    return;
                }
                webView.evaluateJavascript("(function(){try{return document.body.innerText.substring(0,4000);}catch(e){return '';}})();", value -> {
                    String t = value.replaceAll("^\"|\"$", "").replace("\\n", " ").replace("\\", " ").trim();
                    if (t.length() > 15) {
                        textToSpeech.speak(t, TextToSpeech.QUEUE_FLUSH, null, "GOLD_TTS");
                        Toast.makeText(this, "🔊 يقرأ الآن بالصوت الذهبي... اضغط ثانية للإيقاف", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "📭 لا يوجد نص للقراءة", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {}
        }));

        textToSpeech = new TextToSpeech(this, s -> {
            if (s == TextToSpeech.SUCCESS) {
                try {
                    textToSpeech.setLanguage(new Locale("ar"));
                    textToSpeech.setSpeechRate(0.88f);
                    textToSpeech.setPitch(1.0f);
                } catch (Exception e) {}
            }
        });
    }

    // === دالة الإعلان الذهبي الخارق - حتى لو النت ضعيف ينفذ الزر ===
    private void showGoldAd(Runnable action) {
        try {
            if (startAppAd != null) {
                startAppAd.showAd(new AdDisplayListener() {
                    @Override public void adHidden(Ad ad) { action.run(); try { startAppAd.loadAd(); } catch (Exception e) {} }
                    @Override public void adDisplayed(Ad ad) {}
                    @Override public void adClicked(Ad ad) {}
                    @Override public void adNotDisplayed(Ad ad) { action.run(); try { startAppAd.loadAd(); } catch (Exception e) {} }
                });
            } else action.run();
        } catch (Exception e) { action.run(); }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) { webView.goBack(); return; }
        new AlertDialog.Builder(this)
                .setTitle("💎 رق المهدي برو ماكس الذهبي")
                .setMessage("هل تريد الخروج؟\n\n💛 لا تنسى الأذكار")
                .setPositiveButton("خروج 🚪", (d, w) -> showGoldAd(() -> finishAffinity()))
                .setNegativeButton("البقاء 📖💎", (d, w) -> d.dismiss())
                .setCancelable(false)
                .show();
    }

    @Override protected void onResume() { super.onResume(); try { if (startAppBanner != null) startAppBanner.loadAd(); } catch (Exception e) {} }
    @Override protected void onDestroy() { try { if (textToSpeech != null) { textToSpeech.stop(); textToSpeech.shutdown(); } } catch (Exception e) {} super.onDestroy(); }
}
