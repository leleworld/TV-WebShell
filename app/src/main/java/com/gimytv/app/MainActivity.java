package com.gimytv.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * GimyTV Android TV - 主 Activity
 *
 * 核心功能：
 * 1. 全屏 WebView 加载可配置的网站地址
 * 2. 遥控器 D-pad 方向键 → 映射为网页焦点导航
 * 3. 确认键 → 点击当前焦点元素
 * 4. 返回键 → 网页后退 / 退出确认
 * 5. 短按菜单键 → 回首页 / 长按菜单键 → 打开设置
 */
public class MainActivity extends Activity {

    private static final long BACK_EXIT_INTERVAL = 2000;
    private static final int REQUEST_SETTINGS = 1001;

    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout loadingView;
    private LinearLayout errorView;
    private Button btnRetry;

    private String homeUrl;
    private long lastBackPressTime = 0;
    private boolean isPageError = false;

    // ======================== JS: 遥控器焦点导航脚本 ========================
    private static final String TV_REMOTE_JS = "(function() {\n" +
        "  if (window.__tvRemoteInit) return;\n" +
        "  window.__tvRemoteInit = true;\n" +
        "\n" +
        "  var style = document.createElement('style');\n" +
        "  style.textContent = '\\n" +
        "    .tv-focused {\\n" +
        "      outline: 3px solid #FFD700 !important;\\n" +
        "      outline-offset: 2px !important;\\n" +
        "      box-shadow: 0 0 12px rgba(255,215,0,0.6) !important;\\n" +
        "      position: relative;\\n" +
        "      z-index: 9999 !important;\\n" +
        "      transition: outline 0.15s, box-shadow 0.15s;\\n" +
        "    }\\n" +
        "  ';\n" +
        "  document.head.appendChild(style);\n" +
        "\n" +
        "  var currentIndex = -1;\n" +
        "\n" +
        "  function getFocusableElements() {\n" +
        "    var selectors = 'a[href], button, input, select, textarea, [tabindex]:not([tabindex=\"-1\"]), [onclick]';\n" +
        "    var all = document.querySelectorAll(selectors);\n" +
        "    var visible = [];\n" +
        "    for (var i = 0; i < all.length; i++) {\n" +
        "      var el = all[i];\n" +
        "      var rect = el.getBoundingClientRect();\n" +
        "      if (rect.width > 0 && rect.height > 0 &&\n" +
        "          rect.bottom > 0 && rect.top < window.innerHeight + 200 &&\n" +
        "          getComputedStyle(el).display !== 'none' &&\n" +
        "          getComputedStyle(el).visibility !== 'hidden') {\n" +
        "        visible.push(el);\n" +
        "      }\n" +
        "    }\n" +
        "    return visible;\n" +
        "  }\n" +
        "\n" +
        "  function clearFocus() {\n" +
        "    var focused = document.querySelectorAll('.tv-focused');\n" +
        "    for (var i = 0; i < focused.length; i++) {\n" +
        "      focused[i].classList.remove('tv-focused');\n" +
        "    }\n" +
        "  }\n" +
        "\n" +
        "  function setFocus(el) {\n" +
        "    clearFocus();\n" +
        "    if (!el) return;\n" +
        "    el.classList.add('tv-focused');\n" +
        "    el.scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'});\n" +
        "  }\n" +
        "\n" +
        "  function findNextElement(direction) {\n" +
        "    var elements = getFocusableElements();\n" +
        "    if (elements.length === 0) return null;\n" +
        "\n" +
        "    var current = document.querySelector('.tv-focused');\n" +
        "    if (!current) return elements[0];\n" +
        "\n" +
        "    var cRect = current.getBoundingClientRect();\n" +
        "    var cx = cRect.left + cRect.width / 2;\n" +
        "    var cy = cRect.top + cRect.height / 2;\n" +
        "\n" +
        "    var best = null;\n" +
        "    var bestScore = Infinity;\n" +
        "\n" +
        "    for (var i = 0; i < elements.length; i++) {\n" +
        "      var el = elements[i];\n" +
        "      if (el === current) continue;\n" +
        "\n" +
        "      var rect = el.getBoundingClientRect();\n" +
        "      var ex = rect.left + rect.width / 2;\n" +
        "      var ey = rect.top + rect.height / 2;\n" +
        "      var dx = ex - cx;\n" +
        "      var dy = ey - cy;\n" +
        "\n" +
        "      var valid = false;\n" +
        "      switch(direction) {\n" +
        "        case 'up':    valid = dy < -10; break;\n" +
        "        case 'down':  valid = dy > 10;  break;\n" +
        "        case 'left':  valid = dx < -10; break;\n" +
        "        case 'right': valid = dx > 10;  break;\n" +
        "      }\n" +
        "      if (!valid) continue;\n" +
        "\n" +
        "      var mainDist, crossDist;\n" +
        "      if (direction === 'up' || direction === 'down') {\n" +
        "        mainDist = Math.abs(dy);\n" +
        "        crossDist = Math.abs(dx);\n" +
        "      } else {\n" +
        "        mainDist = Math.abs(dx);\n" +
        "        crossDist = Math.abs(dy);\n" +
        "      }\n" +
        "      var score = mainDist + crossDist * 3;\n" +
        "\n" +
        "      if (score < bestScore) {\n" +
        "        bestScore = score;\n" +
        "        best = el;\n" +
        "      }\n" +
        "    }\n" +
        "    return best;\n" +
        "  }\n" +
        "\n" +
        "  function clickFocused() {\n" +
        "    var el = document.querySelector('.tv-focused');\n" +
        "    if (el) { el.click(); return true; }\n" +
        "    return false;\n" +
        "  }\n" +
        "\n" +
        "  window.tvNavigate = function(direction) {\n" +
        "    var next = findNextElement(direction);\n" +
        "    if (next) { setFocus(next); return true; }\n" +
        "    if (direction === 'down') window.scrollBy(0, 300);\n" +
        "    if (direction === 'up') window.scrollBy(0, -300);\n" +
        "    return false;\n" +
        "  };\n" +
        "\n" +
        "  window.tvClick = function() { return clickFocused(); };\n" +
        "\n" +
        "  window.tvScrollPage = function(dy) {\n" +
        "    window.scrollBy({top: dy, behavior: 'smooth'});\n" +
        "  };\n" +
        "\n" +
        "  setTimeout(function() {\n" +
        "    var elements = getFocusableElements();\n" +
        "    if (elements.length > 0) setFocus(elements[0]);\n" +
        "  }, 800);\n" +
        "\n" +
        "  console.log('[GimyTV] Remote control JS injected');\n" +
        "})();";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 全屏沉浸模式
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        loadingView = findViewById(R.id.loadingView);
        errorView = findViewById(R.id.errorView);
        btnRetry = findViewById(R.id.btnRetry);

        btnRetry.setOnClickListener(v -> reloadPage());

        // 从设置中读取首页地址
        homeUrl = SettingsActivity.getHomeUrl(this);

        setupWebView();
        webView.loadUrl(homeUrl);
    }

    // ======================== WebView 配置 ========================
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAppCachePath(getCacheDir().getAbsolutePath());

        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        String ua = settings.getUserAgentString();
        settings.setUserAgentString(ua.replace("Mobile", "") + " GimyTV/1.0");

        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                isPageError = false;
                progressBar.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.VISIBLE);
                errorView.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                loadingView.setVisibility(View.GONE);
                if (!isPageError) {
                    errorView.setVisibility(View.GONE);
                }
                injectRemoteControlJS();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                if (request.isForMainFrame()) {
                    isPageError = true;
                    loadingView.setVisibility(View.GONE);
                    errorView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                           SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("gimytw") || url.contains("gimytv") ||
                    url.contains("gimy") || url.startsWith("https://") ||
                    url.startsWith("http://")) {
                    view.loadUrl(url);
                    return true;
                }
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress >= 90) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    // ======================== 注入遥控器 JS ========================
    private void injectRemoteControlJS() {
        webView.evaluateJavascript(TV_REMOTE_JS, null);
    }

    // ======================== 遥控器按键处理 ========================
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                webView.evaluateJavascript("window.tvNavigate('up')", null);
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                webView.evaluateJavascript("window.tvNavigate('down')", null);
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                webView.evaluateJavascript("window.tvNavigate('left')", null);
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                webView.evaluateJavascript("window.tvNavigate('right')", null);
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                webView.evaluateJavascript("window.tvClick()", null);
                return true;

            case KeyEvent.KEYCODE_BACK:
                return handleBackKey();

            case KeyEvent.KEYCODE_MENU:
                // 长按检测在 onKeyLongPress 中处理
                // 这里仅标记为待处理，不拦截 event
                event.startTracking();
                return true;

            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                webView.evaluateJavascript("window.tvScrollPage(800)", null);
                return true;

            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_CHANNEL_UP:
                webView.evaluateJavascript("window.tvScrollPage(-800)", null);
                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // 短按菜单键（非长按）→ 回首页
            if (!event.isCanceled() && (event.getFlags() & KeyEvent.FLAG_CANCELED_LONG_PRESS) == 0) {
                webView.loadUrl(homeUrl);
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // 长按菜单键 → 打开设置
            openSettings();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private boolean handleBackKey() {
        if (webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        long now = System.currentTimeMillis();
        if (now - lastBackPressTime < BACK_EXIT_INTERVAL) {
            finish();
        } else {
            lastBackPressTime = now;
            Toast.makeText(this, "再按一次返回退出", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    // ======================== 设置页面 ========================
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK) {
            // 设置页面保存了新 URL，重新加载
            String newUrl = SettingsActivity.getHomeUrl(this);
            if (!newUrl.equals(homeUrl)) {
                homeUrl = newUrl;
                webView.loadUrl(homeUrl);
                Toast.makeText(this, "已切换到: " + homeUrl, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ======================== 重新加载 ========================
    private void reloadPage() {
        errorView.setVisibility(View.GONE);
        webView.loadUrl(homeUrl);
    }

    // ======================== 生命周期 ========================
    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
