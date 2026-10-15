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
    private boolean isTtsReady = false;

    public static final String SITE_URL = "https://rgalmahdi.blogspot.com/";
    public static final String IMEADS_PUB = "pub_X_V2BG-WWEDsbgkBc8qHng";
    public static final String STARTAPP_ID = "209205885";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StartAppSDK.init(this, STARTAPP_ID, false);
        StartAppSDK.setTestAdsEnabled(false);
        StartAppAd.disableSplash();
        startAppAd = new StartAppAd(this);
        startAppAd.loadAd();

        startAppBanner = findViewById(R.id.startAppBanner);
        if (startAppBanner!= null) {
            startAppBanner.loadAd();
            startAppBanner.postDelayed(new Runnable() {
                @Override public void run() {
                    try { if (startAppBanner!= null) startAppBanner.loadAd(); } catch (Exception e) {}
                    startAppBanner.postDelayed(this, 45000);
                }
            }, 45000);
        }

        webView = findViewById(R.id.webview);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipeRefreshLayout.setRefreshing(false);
                String imeGold = "(function(){try{if(!window.iMeAdsLayer){window.iMeAdsLayer=[];window.imeads=function(){iMeAdsLayer.push(arguments);};var s=document.createElement('script');s.async=true;s.src='https://ad.api.imem.me/static/pixel-v2.min.js';s.setAttribute('data-pk','"+IMEADS_PUB+"');document.head.appendChild(s);setTimeout(function(){try{imeads('page_view');imeads('new_user');}catch(e){}},2000);}else{try{imeads('page_view');}catch(e){}}}catch(e){}})();";
                view.evaluateJavascript(imeGold, null);
                try { if (startAppBanner!= null) startAppBanner.loadAd(); } catch (Exception e) {}
            }
        });
        webView.loadUrl(SITE_URL);
        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        // قارئ TTS ماسي خارق - يقرأ ويوقف فوري
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                try {
                    int result = textToSpeech.setLanguage(new Locale("ar"));
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeech.setLanguage(Locale.getDefault());
                    }
                    textToSpeech.setSpeechRate(0.85f);
                    textToSpeech.setPitch(1.0f);
                    isTtsReady = true;
                } catch (Exception e) { isTtsReady = true; }
            }
        });

        findViewById(R.id.btnShare).setOnClickListener(v -> showDiamondAd(() -> {
            try {
                String u = webView.getUrl(); if (u == null) u = SITE_URL;
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, "💎✨ رق المهدي برو ماكس الماسي ✨💎\n📖 رق المهدي\n🔗 " + u);
                startActivity(Intent.createChooser(i, "مشاركة ماسية 📖💎"));
            } catch (Exception e) {}
        }));

        findViewById(R.id.btnEye).setOnClickListener(v -> showDiamondAd(() -> {
            String js = "javascript:try{var b=document.body;var f=b.style.filter||'';if(f.includes('brightness')){b.style.filter='';b.style.backgroundColor='';document.documentElement.style.backgroundColor='';}else{b.style.filter='brightness(1.6) sepia(0.12) contrast(0.96)';b.style.backgroundColor='#FFF8E1';document.documentElement.style.backgroundColor='#FFF8E1';}}catch(e){}";
            webView.evaluateJavascript(js, null);
            Toast.makeText(this, "👁️💎 راحة عين ماسية", Toast.LENGTH_SHORT).show();
        }));

        findViewById(R.id.btnTTS).setOnClickListener(v -> showDiamondAd(() -> {
            try {
                if (textToSpeech!= null && textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                    Toast.makeText(this, "⏹️ تم إيقاف القراءة الماسية", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isTtsReady) {
                    Toast.makeText(this, "⏳ جاري تجهيز القارئ الماسي...", Toast.LENGTH_SHORT).show();
                    return;
                }
                webView.evaluateJavascript("(function(){try{var t='';var ps=document.querySelectorAll('p, h1, h2, h3,.post-body,.entry-content');if(ps.length>0){for(var i=0;i<Math.min(ps.length,5);i++){t+=ps[i].innerText+' ';}}else{t=document.body.innerText;}return t.substring(0,4000);}catch(e){return '';}})();", value -> {
                    try {
                        if (value == null || value.length() < 5) {
                            Toast.makeText(this, "📭 لا يوجد نص للقراءة", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String clean = value;
                        if (clean.startsWith("\"") && clean.endsWith("\"")) clean = clean.substring(1, clean.length()-1);
                        clean = clean.replace("\\n", " ").replace("\\r", " ").replace("\\\"", "\"").replace("\\t", " ").replace("\\u003C", "<").trim();
                        if (clean.length() > 20) {
                            textToSpeech.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "DIAMOND_TTS");
                            Toast.makeText(this, "🔊💎 يقرأ الآن... اضغط ثانية للإيقاف", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "📭 النص قصير جداً", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "❌ خطأ في القراءة", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) { Toast.makeText(this, "❌ خطأ", Toast.LENGTH_SHORT).show(); }
        }));
    }

    private void showDiamondAd(Runnable action) {
        try {
            if (startAppAd!= null && startAppAd.isReady()) {
                startAppAd.showAd(new AdDisplayListener() {
                    @Override public void adHidden(Ad ad) { action.run(); try { startAppAd.loadAd(); } catch (Exception e) {} }
                    @Override public void adDisplayed(Ad ad) {}
                    @Override public void adClicked(Ad ad) {}
                    @Override public void adNotDisplayed(Ad ad) { action.run(); try { startAppAd.loadAd(); } catch (Exception e) {} }
                });
            } else { action.run(); try { if (startAppAd!= null) startAppAd.loadAd(); } catch (Exception e) {} }
        } catch (Exception e) { action.run(); }
    }

    @Override public void onBackPressed() {
        if (webView!= null && webView.canGoBack()) { webView.goBack(); return; }
        new AlertDialog.Builder(this).setTitle("💎 رق المهدي برو ماكس الماسي").setMessage("هل تريد الخروج؟\n\لاتنسى الأذكار").setPositiveButton("خروج 🚪💎", (d, w) -> showDiamondAd(() -> finishAffinity())).setNegativeButton("البقاء 📖💎", (d, w) -> d.dismiss()).setCancelable(false).show();
    }
    @Override protected void onResume() { super.onResume(); try { if (startAppBanner!= null) startAppBanner.loadAd(); } catch (Exception e) {} }
    @Override protected void onDestroy() { try { if (textToSpeech!= null) { textToSpeech.stop(); textToSpeech.shutdown(); } } catch (Exception e) {} super.onDestroy(); }
}
