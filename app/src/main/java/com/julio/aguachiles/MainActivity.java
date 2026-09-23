package com.julio.aguachiles;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.EditText;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new Dialogs());
        web.addJavascriptInterface(new Bridge(), "Android");
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }

    // Sin WebChromeClient, confirm() y prompt() devuelven false/null sin mostrar nada
    class Dialogs extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView v, String url, String message, final JsResult result) {
            new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Aceptar", (d, w) -> result.confirm())
                .setOnCancelListener(d -> result.confirm())
                .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView v, String url, String message, final JsResult result) {
            new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Sí", (d, w) -> result.confirm())
                .setNegativeButton("Cancelar", (d, w) -> result.cancel())
                .setOnCancelListener(d -> result.cancel())
                .show();
            return true;
        }

        @Override
        public boolean onJsPrompt(WebView v, String url, String message, String defaultValue, final JsPromptResult result) {
            final EditText input = new EditText(MainActivity.this);
            if (defaultValue != null) input.setText(defaultValue);
            new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setView(input)
                .setPositiveButton("Aceptar", (d, w) -> result.confirm(input.getText().toString()))
                .setNegativeButton("Cancelar", (d, w) -> result.cancel())
                .setOnCancelListener(d -> result.cancel())
                .show();
            return true;
        }
    }

    class Bridge {
        @JavascriptInterface
        public void share(final String title, final String text) {
            runOnUiThread(() -> {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, title);
                i.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(i, title));
            });
        }
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("window.goBack && window.goBack()", v -> {
            if (!"true".equals(v)) MainActivity.super.onBackPressed();
        });
    }
}
