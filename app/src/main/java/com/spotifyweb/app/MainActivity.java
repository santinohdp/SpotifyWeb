package com.spotifyweb.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.Activity;

public class MainActivity extends Activity {

    private static final String START_URL = "https://open.spotify.com/";
    // User agent de escritorio: Spotify bloquea/redirige el reproductor web en móviles
    private static final String DESKTOP_UA =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/124.0.0.0 Safari/537.36";

    private WebView web;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setUserAgentString(DESKTOP_UA);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                String host = r.getUrl().getHost();
                if (host != null && (host.endsWith("spotify.com") || host.endsWith("scdn.co")
                        || host.endsWith("google.com") || host.endsWith("facebook.com")
                        || host.endsWith("apple.com"))) {
                    return false; // se queda dentro de la app (login, etc.)
                }
                startActivity(new Intent(Intent.ACTION_VIEW, r.getUrl()));
                return true;
            }
        });

        // Permite DRM (Widevine) para que el audio protegido pueda reproducirse
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl(START_URL);
        }

        startForegroundService(new Intent(this, KeepAliveService.class));
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else moveTaskToBack(true); // minimiza y la música sigue sonando
    }

    @Override
    protected void onDestroy() {
        if (isFinishing()) stopService(new Intent(this, KeepAliveService.class));
        super.onDestroy();
    }
}
