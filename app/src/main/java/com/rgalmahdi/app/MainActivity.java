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

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextToSpeech textToSpeech;
    private Object startAppAd = null; // Object عشان لو المكتبة مش موجودة ما ينهار
    
    public static final String SITE_URL = "https://rgalmahdi.blogspot.com/";
    public static final String IMEADS_PUB = "pub_X_V2BG-WWEDsbgkBc8qHng";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // حماية قصوى - لو أي شي فشل ما ينهار
        try {
            setContentView(R.layout.activity_main);
        } catch (Exception e) {
            // لو التصميم فشل، أنشئ WebView فقط
            WebView fallback = new WebView(this);
            setContentView(fallback);
            fallback.loadUrl(SITE_URL);
            return;
        }

        // === StartApp مع حماية كاملة - لو فشل ما يأثر ===
        try {
            Class<?> sdkClass = Class.forName("com.startapp.sdk.adsbase.StartAppSDK");
            sdkClass.getMethod("init", android.content.Context.class, String.class, boolean.class).invoke(null, this, "209205885", false);
            Class<?> adClass = Class.forName("com.startapp.sdk.adsbase.StartAppAd");
            Object splash = adClass.getMethod("disableSplash").invoke(null);
            startAppAd = adClass.getConstructor(android.content.Context.class).newInstance(this);
            adClass.getMethod("loadAd").invoke(startAppAd);
        } catch (Exception e) {
            startAppAd = null; // لو مافي مكتبة StartApp، كمل عادي
        }

        try {
            webView = findViewById(R.id.webview);
            swipeRefreshLayout = findViewById(R.id.swipeRefresh);

            if (webView == null) {
                Toast.makeText(this, "جاري التحميل...", Toast.LENGTH_SHORT).show();
                return;
            }

            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    try {
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                        view.evaluateJavascript("(function(){try{var s=document.createElement('script');s.src='https://www.imeads.com/ads.js';s.setAttribute('data-pub','" + IMEADS_PUB + "');s.setAttribute('data-no-close','true');document.body.appendChild(s);}catch(e){}})();", null);
                    } catch (Exception ex) {}
                }
            });
            
            webView.loadUrl(SITE_URL);
            
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setOnRefreshListener(() -> {
                    try { if (webView != null) webView.reload(); } catch (Exception e) {}
                });
            }

            // === أزرار برو ماكس مع حماية ===
            if (findViewById(R.id.btnShare) != null) {
                findViewById(R.id.btnShare).setOnClickListener(v -> {
                    tryShowAdThen(() -> {
                        try {
                            String url = webView.getUrl() != null ? webView.getUrl() : SITE_URL;
                            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(android.content.Intent.EXTRA_TEXT, "✨ رق المهدي ✨\n" + url);
                            startActivity(android.content.Intent.createChooser(intent, "مشاركة"));
                        } catch (Exception e) { Toast.makeText(this, "تم", Toast.LENGTH_SHORT).show(); }
                    });
                });
            }

            if (findViewById(R.id.btnEye) != null) {
                findViewById(R.id.btnEye).setOnClickListener(v -> {
                    tryShowAdThen(() -> {
                        try {
                            webView.evaluateJavascript("javascript:try{var f=document.body.style.filter;document.body.style.filter=f.includes('1.6')?'':'brightness(1.6) sepia(0.2)'}catch(e){}", null);
                            Toast.makeText(this, "👁️ راحة العين", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {}
                    });
                });
            }

            if (findViewById(R.id.btnTTS) != null) {
                findViewById(R.id.btnTTS).setOnClickListener(v -> {
                    try {
                        if (textToSpeech != null && textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                            Toast.makeText(this, "⏹️ توقف", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        webView.evaluateJavascript("(function(){try{return document.body.innerText.substring(0,3000)}catch(e){return ''}})();", value -> {
                            try {
                                String text = value.replaceAll("^\"|\"$", "").trim();
                                if (text.length() > 5 && textToSpeech != null) {
                                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                                    Toast.makeText(this, "🔊 يقرأ...", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception ex) {}
                        });
                    } catch (Exception e) {}
                });
            }

            textToSpeech = new TextToSpeech(this, status -> {
                try { if (status == TextToSpeech.SUCCESS) textToSpeech.setLanguage(new Locale("ar")); } catch (Exception e) {}
            });

        } catch (Exception e) {
            try { Toast.makeText(this, "تم التحميل ✅", Toast.LENGTH_SHORT).show(); } catch (Exception ex) {}
        }
    }

    private void tryShowAdThen(Runnable action) {
        try {
            if (startAppAd != null) {
                Class<?> adClass = startAppAd.getClass();
                Class<?> listenerClass = Class.forName("com.startapp.sdk.adsbase.adlisteners.AdDisplayListener");
                Object listener = java.lang.reflect.Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class[]{listenerClass}, (proxy, method, args) -> {
                    if (method.getName().equals("adHidden") || method.getName().equals("adNotDisplayed")) {
                        action.run();
                        try { adClass.getMethod("loadAd").invoke(startAppAd); } catch (Exception e) {}
                    }
                    return null;
                });
                adClass.getMethod("showAd", listenerClass).invoke(startAppAd, listener);
            } else {
                action.run();
            }
        } catch (Exception e) {
            action.run(); // لو الإعلان فشل، نفذ الحركة مباشرة
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (webView != null && webView.canGoBack()) { webView.goBack(); return; }
            
            new AlertDialog.Builder(this)
                .setTitle("رق المهدي برو ماكس")
                .setMessage("هل تريد الخروج فعلاً؟\nاذكر الله 🙏")
                .setPositiveButton("خروج", (d, w) -> {
                    tryShowAdThen(() -> finishAffinity());
                })
                .setNegativeButton("البقاء", null)
                .show();
        } catch (Exception e) {
            try { super.onBackPressed(); } catch (Exception ex) { finishAffinity(); }
        }
    }

    @Override
    protected void onDestroy() {
        try { if (textToSpeech != null) { textToSpeech.stop(); textToSpeech.shutdown(); } } catch (Exception e) {}
        super.onDestroy();
    }
}
