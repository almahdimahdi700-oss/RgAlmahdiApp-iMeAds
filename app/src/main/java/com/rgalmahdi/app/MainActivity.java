
package com.rgalmahdi.app;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

// Start.io imports
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private FloatingActionButton fabShare, fabNightMode, fabTTS;
    private String currentUrl = "https://rgalmahdi.blogspot.com";
    private TextToSpeech textToSpeech;
    private SharedPreferences prefs;
    private boolean doubleBackToExitPressedOnce = false;
    private int pageViewCount = 0;
    
    // iMeAds
    private final String IME_PUBLISHER_KEY = "pub_X_V2BG-WWEDsbgkBc8qHng";
    
    // Start.io - غير هذا الـ ID بـ App ID حقك من Start.io
    private final String STARTIO_APP_ID = "204445944"; // مثلا: 123456789
    private StartAppAd startAppAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // تهيئة Start.io
        StartAppSDK.init(this, STARTIO_APP_ID, false);
        StartAppSDK.enableReturnAds(false); // نتحكم نحن بوقت الإعلان
        startAppAd = new StartAppAd(this);
        
        prefs = getSharedPreferences("RgAlmahdiPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("night_mode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        initViews();
        setupWebView();
        setupFABs();
        showWelcomeDialog();
    }

    private void initViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        fabShare = findViewById(R.id.fabShare);
        fabNightMode = findViewById(R.id.fabNightMode);
        fabTTS = findViewById(R.id.fabTTS);
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                    prefs.edit().putString("last_url", view.getUrl()).apply();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
                currentUrl = url;
                pageViewCount++;
                injectImeAdsUnlimited(view, pageViewCount);
            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                String errorHtml = "<html><body style='background-color:#000; color:#FFD700; text-align:center; padding-top:100px; font-family:sans-serif;'><h1>رق المهدي برو ماكس</h1><h3>أنت أوفلاين</h3></body></html>";
                view.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null);
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimetype);
            String cookies = CookieManager.getInstance().getCookie(url);
            request.addRequestHeader("cookie", cookies);
            request.addRequestHeader("User-Agent", userAgent);
            request.setDescription("جاري تحميل الملف...");
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(getApplicationContext(), "بدأ التحميل...", Toast.LENGTH_LONG).show();
        });

        webView.loadUrl(currentUrl);
        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());
    }

    private void injectImeAdsUnlimited(WebView view, int count) {
        String jsCode = "javascript:(function() {" +
                "var oldPixel = document.getElementById('ime-publisher-pixel');" +
                "if(oldPixel){ oldPixel.remove(); }" +
                "var s=document.createElement('script');" +
                "s.id='ime-publisher-pixel';" +
                "s.src='https://ad.api.imem.me/static/pixel-v2.min.js?t=' + new Date().getTime();" +
                "s.setAttribute('data-pk','" + IME_PUBLISHER_KEY + "');" +
                "s.setAttribute('data-views','" + count + "');" +
                "document.head.appendChild(s);" +
                "setTimeout(function(){ if(window.iMeAds){ window.iMeAds.refresh(); } }, 500);" +
                "setTimeout(function(){ var s2=document.createElement('script'); s2.src='https://ad.api.imem.me/static/pixel-v2.min.js?t=' + new Date().getTime(); s2.setAttribute('data-pk','" + IME_PUBLISHER_KEY + "'); document.body.appendChild(s2); }, 15000);" +
                "})()";
        view.evaluateJavascript(jsCode, null);
    }

    private void setupFABs() {
        // 1. زر المشاركة + إعلان Start.io بعد كل ضغطة
        fabShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "حمل تطبيق رق المهدي برو ماكس: " + currentUrl);
            startActivity(Intent.createChooser(shareIntent, "شارك التطبيق"));
            
            // إعلان Start.io بعد المشاركة
            new Handler().postDelayed(() -> {
                StartAppAd.showAd(MainActivity.this);
            }, 1000); // بعد ثانية من المشاركة
        });

        // 2. زر العين (الوضع الليلي) + إعلان Start.io بعد كل ضغطة
        fabNightMode.setOnClickListener(v -> {
            boolean isNight = prefs.getBoolean("night_mode", false);
            if (isNight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                prefs.edit().putBoolean("night_mode", false).apply();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                prefs.edit().putBoolean("night_mode", true).apply();
            }
            // إعلان Start.io بعد ضغط زر العين
            new Handler().postDelayed(() -> {
                StartAppAd.showAd(MainActivity.this);
            }, 500);
        });

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("ar"));
            }
        });
        fabTTS.setOnClickListener(v -> {
            webView.evaluateJavascript("document.body.innerText", value -> {
                String text = value.replaceAll("\"", "");
                if (text.length() > 4000) text = text.substring(0, 4000);
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                Toast.makeText(this, "جاري القراءة...", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showWelcomeDialog() {
        if (!prefs.getBoolean("first_run", false)) {
            new AlertDialog.Builder(this)
                .setTitle("رق المهدي برو ماكس - iMe + Start.io")
                .setMessage("نسخة مفتوحة الأرباح:\n\n✓ iMeAds كل صفحة = إعلان\n✓ Start.io بعد المشاركة\n✓ Start.io بعد زر العين\n✓ Start.io قبل الخروج")
                .setPositiveButton("ابدأ الآن", null)
                .setCancelable(false)
                .show();
            prefs.edit().putBoolean("first_run", true).apply();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            pageViewCount++;
            new Handler().postDelayed(() -> injectImeAdsUnlimited(webView, pageViewCount), 300);
        } else {
            if (doubleBackToExitPressedOnce) {
                // 3. قبل الخروج من التطبيق = إعلان Start.io
                StartAppAd.showAd(this, new com.startapp.sdk.adsbase.adlisteners.AdDisplayListener() {
                    @Override
                    public void adHidden(com.startapp.sdk.adsbase.adlisteners.Ad ad) {
                        finish();
                    }
                    @Override
                    public void adDisplayed(com.startapp.sdk.adsbase.adlisteners.Ad ad) {}
                    @Override
                    public void adClicked(com.startapp.sdk.adsbase.adlisteners.Ad ad) {}
                    @Override
                    public void adNotDisplayed(com.startapp.sdk.adsbase.adlisteners.Ad ad) {
                        finish();
                    }
                });
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "اضغط مرة أخرى للخروج - سيظهر إعلان", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) { textToSpeech.stop(); textToSpeech.shutdown(); }
        super.onDestroy();
    }
}
