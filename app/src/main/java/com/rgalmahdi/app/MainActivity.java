package com.rgalmahdi.app;

import android.app.AlertDialog;
import android.graphics.Color;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private StartAppAd startAppAd;
    private TextToSpeech textToSpeech;
    
    public static final String SITE_URL = "https://rgalmahdi.blogspot.com/";
    public static final String IMEADS_PUB = "pub_X_V2BG-WWEDsbgkBc8qHng";
    public static final String STARTAPP_ID = "209205885"; // ID حقك

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // === StartApp إعلانات ===
        StartAppSDK.init(this, STARTAPP_ID, false);
        StartAppSDK.setTestAdsEnabled(false);
        StartAppAd.disableSplash();
        startAppAd = new StartAppAd(this);
        startAppAd.loadAd(); // حمل إعلان من البداية

        webView = findViewById(R.id.webview);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        FloatingActionButton btnShare = findViewById(R.id.btnShare);
        FloatingActionButton btnEye = findViewById(R.id.btnEye);
        FloatingActionButton btnTTS = findViewById(R.id.btnTTS);

        // === تلوين الأيقونات برو ماكس تحت بعض ===
        // مشاركة = أخضر رق المهدي
        btnShare.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32")));
        btnShare.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        // العين = أزرق سماوي
        btnEye.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#0288D1")));
        btnEye.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        // القارئ = ذهبي فاخر
        btnTTS.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFB300")));
        btnTTS.setImageTintList(android.content.res.ColorStateList.valueOf(Color.BLACK));

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
        
        // تحديث بالسحب - برو ماكس
        swipeRefreshLayout.setColorSchemeColors(
            Color.parseColor("#2E7D32"),
            Color.parseColor("#0288D1"),
            Color.parseColor("#FFB300")
        );
        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        // === 1. بعد فتح كل مشاركة - إعلان ===
        btnShare.setOnClickListener(v -> {
            showAdWithAction(() -> {
                String shareText = "حمل تطبيق رق المهدي - رقيتك الشرعية: " + webView.getUrl();
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
                startActivity(android.content.Intent.createChooser(intent, "مشاركة عبر"));
            });
        });
        
        // === 2. بعد الضغط على العين - إعلان ===
        btnEye.setOnClickListener(v -> {
            showAdWithAction(() -> {
                webView.evaluateJavascript("document.body.style.filter = document.body.style.filter == 'brightness(1.5)' ? 'brightness(1)' : 'brightness(1.5)';", null);
                Toast.makeText(this, "وضع حماية العين 👁️", Toast.LENGTH_SHORT).show();
            });
        });

        // === القارئ الصوتي المحسن ===
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("ar"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "اللغة العربية غير مدعومة للقارئ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnTTS.setOnClickListener(v -> {
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
                Toast.makeText(this, "تم إيقاف القراءة", Toast.LENGTH_SHORT).show();
            } else {
                webView.evaluateJavascript("(function(){return document.body.innerText.substring(0,4000);})();", value -> {
                    String text = value.replaceAll("^\"|\"$", "").replace("\\n", " ").trim();
                    if (text.isEmpty() || text.equals("null")) {
                        Toast.makeText(this, "لا يوجد نص للقراءة", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "رق_المهدي");
                    Toast.makeText(this, "جاري القراءة بصوت الراقي 🔊", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // دالة سحرية - تعرض إعلان ثم تنفذ الحركة
    private void showAdWithAction(Runnable actionAfterAd) {
        startAppAd.showAd(new AdDisplayListener() {
            @Override
            public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                actionAfterAd.run();
                startAppAd.loadAd(); // حمل إعلان جديد
            }
            @Override
            public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {}
            @Override
            public void adClicked(com.startapp.sdk.adsbase.Ad ad) {}
            @Override
            public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                actionAfterAd.run();
                startAppAd.loadAd();
            }
        });
    }

    // === 3. قبل الخروج - إعلان + رسالة تأكيد ===
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setTitle("رق المهدي برو ماكس")
                .setMessage("هل تريد الخروج فعلاً؟\nلا تنسى ذكر الله 🙏")
                .setIcon(R.mipmap.ic_launcher) // أيقونتك الذهبية
                .setPositiveButton("نعم، خروج", (dialog, which) -> {
                    // إعلان قبل الخروج
                    startAppAd.showAd(new AdDisplayListener() {
                        @Override
                        public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                            finishAffinity();
                        }
                        @Override
                        public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {}
                        @Override
                        public void adClicked(com.startapp.sdk.adsbase.Ad ad) {}
                        @Override
                        public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                            finishAffinity();
                        }
                    });
                })
                .setNegativeButton("البقاء", null)
                .show();
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
