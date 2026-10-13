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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // إعلانات StartApp
            StartAppSDK.init(this, "209205885", false);
            StartAppSDK.setTestAdsEnabled(false);
            StartAppAd.disableSplash();
            startAppAd = new StartAppAd(this);
            startAppAd.loadAd();
        } catch (Exception e) {}

        try {
            webView = findViewById(R.id.webview);
            swipeRefreshLayout = findViewById(R.id.swipeRefresh);

            if (webView != null) {
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                        try {
                            view.evaluateJavascript(
                                "(function(){var s=document.createElement('script');s.src='https://www.imeads.com/ads.js';s.setAttribute('data-pub','" + IMEADS_PUB + "');s.setAttribute('data-no-close','true');document.body.appendChild(s);})();"
                            , null);
                        } catch (Exception ex) {}
                    }
                });
                webView.loadUrl(SITE_URL);
            }

            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setOnRefreshListener(() -> {
                    if (webView != null) webView.reload();
                });
            }

            // أزرار - مع حماية ما يقفل
            setupButtons();

            // القارئ
            textToSpeech = new TextToSpeech(this, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(new Locale("ar"));
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "تم التحميل", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtons() {
        try {
            // مشاركة
            if (findViewById(R.id.btnShare) != null) {
                findViewById(R.id.btnShare).setOnClickListener(v -> {
                    showAdWithAction(() -> {
                        try {
                            String shareText = "حمل تطبيق رق المهدي: " + (webView != null ? webView.getUrl() : SITE_URL);
                            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
                            startActivity(android.content.Intent.createChooser(intent, "مشاركة"));
                        } catch (Exception e) {}
                    });
                });
            }
            // عين
            if (findViewById(R.id.btnEye) != null) {
                findViewById(R.id.btnEye).setOnClickListener(v -> {
                    showAdWithAction(() -> {
                        try {
                            if (webView != null) webView.evaluateJavascript("document.body.style.filter = document.body.style.filter == 'brightness(1.5)' ? 'brightness(1)' : 'brightness(1.5)';", null);
                            Toast.makeText(this, "وضع العين 👁️", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {}
                    });
                });
            }
            // قارئ
            if (findViewById(R.id.btnTTS) != null) {
                findViewById(R.id.btnTTS).setOnClickListener(v -> {
                    try {
                        if (textToSpeech != null && textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                            return;
                        }
                        if (webView != null) {
                            webView.evaluateJavascript("(function(){return document.body.innerText.substring(0,3000);})();", value -> {
                                try {
                                    String text = value.replaceAll("^\"|\"$", "").trim();
                                    if (text.length() > 5) {
                                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                                        Toast.makeText(this, "جاري القراءة 🔊", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception ex) {}
                            });
                        }
                    } catch (Exception e) {}
                });
            }
        } catch (Exception e) {}
    }

    private void showAdWithAction(Runnable action) {
        try {
            if (startAppAd != null) {
                startAppAd.showAd(new AdDisplayListener() {
                    @Override public void adHidden(com.startapp.sdk.adsbase.Ad ad) { action.run(); startAppAd.loadAd(); }
                    @Override public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {}
                    @Override public void adClicked(com.startapp.sdk.adsbase.Ad ad) {}
                    @Override public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) { action.run(); startAppAd.loadAd(); }
                });
            } else {
                action.run();
            }
        } catch (Exception e) {
            action.run();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
                return;
            }
            new AlertDialog.Builder(this)
                .setTitle("رق المهدي")
                .setMessage("هل تريد الخروج فعلاً؟")
                .setPositiveButton("خروج", (d, w) -> {
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
                .setNegativeButton("البقاء", null)
                .show();
        } catch (Exception e) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (textToSpeech != null) { textToSpeech.stop(); textToSpeech.shutdown(); }
        } catch (Exception e) {}
        super.onDestroy();
    }
}
