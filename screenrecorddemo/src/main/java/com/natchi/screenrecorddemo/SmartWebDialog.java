package com.natchi.screenrecorddemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

//import com.just.agentweb.AgentWeb;
//import com.tencent.smtt.sdk.WebSettings;
//
//import com.tencent.smtt.sdk.WebChromeClient;
//import com.tencent.smtt.sdk.WebSettings;
//import com.tencent.smtt.sdk.WebView;
//import com.tencent.smtt.sdk.WebViewClient;


/**
 * Created by justin on 2017/8/25.
 */
public class SmartWebDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private boolean isShare = false;

    public boolean isShare() {
        return isShare;
    }

    public void setShare(boolean share) {
        isShare = share;
    }

    public SmartWebDialog(Context context) {
        super(context, R.style.tx_MyDialog);
        mContext = context;


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tx_dialog_smartweb);
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
//        attributes.height = Utils.getWindowHeight(mContext);
        attributes.height = ViewGroup.LayoutParams.MATCH_PARENT;
        attributes.width = getWindowWidth(mContext);
        window.setAttributes(attributes);
        window.setGravity(Gravity.BOTTOM);

        setCanceledOnTouchOutside(true);
        initView();
        initWebView();
    }

    WebView webView;
//    com.tencent.smtt.sdk.WebView webView;
//    FrameLayout webView;
    TextView iv_close;
    TextView tv_title;
//    protected com.tencent.smtt.sdk.WebView mWebView;
    protected void initWebView() {


//        webView = new WebView(getContext());
//        ViewGroup mContainer = findViewById(R.id.webView);
//        mContainer.addView(webView);
//
//        WebSettings webSetting = webView.getSettings();
//        webSetting.setJavaScriptEnabled(true);
//        webSetting.setAllowFileAccess(true);
//        webSetting.setSupportZoom(true);
//        webSetting.setDatabaseEnabled(true);
//        webSetting.setAllowFileAccess(true);
//        webSetting.setDomStorageEnabled(true);
//        webSettings = mAgentWeb.getAgentWebSettings().getWebSettings();
//        webSetting.setJavaScriptEnabled(true);
//        webSetting.setDomStorageEnabled(true);
//        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
//        webSetting.setBuiltInZoomControls(true);
//        webSetting.setDisplayZoomControls(false);
//        webSetting.setPluginState(WebSettings.PluginState.ON);
//        webSetting.setLoadWithOverviewMode(true);
//        webSetting.setUseWideViewPort(true);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//        }

//        initWebViewClient();
//        initWebChromeClient();
//        initJavaScriptInterface();
//
//        mWebView.loadUrl(mHomeUrl);
    }
    private void initView() {
//        initWebView();
        webView = findViewById(R.id.webView);
        iv_close = findViewById(R.id.iv_close);
        tv_title = findViewById(R.id.tv_title);
        iv_close.setOnClickListener(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    //加载完毕


                }

            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

    }

    public void request(String url, String title) {
        tv_title.setText(title);
        webView.loadUrl(url);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.iv_close) {

            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                dismiss();
            }

        }


    }



    public static int getWindowWidth(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }
}
