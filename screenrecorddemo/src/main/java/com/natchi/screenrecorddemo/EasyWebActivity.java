package com.natchi.screenrecorddemo;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.just.agentweb.WebChromeClient;


/**
 * Created by cenxiaozhong on 2017/7/22.
 * <p>
 */
public class EasyWebActivity extends BaseAgentWebActivity {

    private TextView mTitleTextView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

//        LinearLayout mLinearLayout = (LinearLayout) this.findViewById(R.id.container);
//        Toolbar mToolbar = (Toolbar) this.findViewById(R.id.toolbar);
//        mToolbar.setTitleTextColor(Color.WHITE);
//        mToolbar.setTitle("");
//        mTitleTextView = (TextView) this.findViewById(R.id.toolbar_title);
//        this.setSupportActionBar(mToolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
//        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EasyWebActivity.this.finish();
//            }
//        });
    }


    @NonNull
    @Override
    protected ViewGroup getAgentWebParent() {
        return (ViewGroup) this.findViewById(R.id.container);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAgentWeb != null && mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected int getIndicatorColor() {
        return Color.parseColor("#ff0000");
    }

    @Override
    protected void setTitle(WebView view, String title) {
        super.setTitle(view, title);
//        if (!TextUtils.isEmpty(title)) {
//            if (title.length() > 10) {
//                title = title.substring(0, 10).concat("...");
//            }
//        }
//        mTitleTextView.setText(title);
    }

    @Override
    protected int getIndicatorHeight() {
        return 3;
    }

    @Nullable
    @Override
    protected String getUrl() {
        return "https://sync-fileview.cloud-ins.cn/onlinePreview?syncid=112-sltest6250157f9d017200183b3c4b1650859951878-1&synctoken=006880b027964924e6ca254b77531c2eaf3IACvOR8MdnHCibuLODmtLIffJ_lk7euVZWPByyqfV2IIxyEs42AAAAAAEACjXG2OMHFnYgEA6AMwcWdi&sync=test&dr=true&url=aHR0cHM6Ly9nZHJiLWRpbmdzdW4tdGVzdC0xMjU1MzgzODA2LmNvcy5hcC1zaGFuZ2hhaS5teXFjbG91ZC5jb20vJUUzJTgwJTkwJUU1JUE0JTg3JUU2JUExJTg4JUU3JTg5JTg4JUUzJTgwJTkxJUU3JTg4JUIxJUU1JUJGJTgzJUU0JUJBJUJBJUU1JUFGJUJGJUU5JTk5JTg0JUU1JThBJUEwJUU2JThBJTk1JUU0JUJGJTlEJUU0JUJBJUJBJUU4JUIxJTgxJUU1JTg1JThEMjAyMSVFNiU5RCVBMSVFNiVBQyVCRS5wZGY%3D";
    }


//    public void initWebSetting() {
//        {
//            // 允许跨域访问cookie信息
//            CookieManager cookieManager = CookieManager.getInstance();
//            cookieManager.setAcceptCookie(true);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                cookieManager.setAcceptThirdPartyCookies(mAgentWeb.getWebCreator().getWebView(), true);
//            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                WebView.setWebContentsDebuggingEnabled(debugging);
//            }
//            webSettings = mAgentWeb.getAgentWebSettings().getWebSettings();
//            webSettings.setJavaScriptEnabled(true);
//            webSettings.setDomStorageEnabled(true);
//            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//            webSettings.setBuiltInZoomControls(true);
//            webSettings.setDisplayZoomControls(false);
//            webSettings.setPluginState(WebSettings.PluginState.ON);
//            webSettings.setLoadWithOverviewMode(true);
//            webSettings.setUseWideViewPort(true);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//            }
//            String baseUserAgent = webSettings.getUserAgentString();
//            webSettings.setUserAgentString(baseUserAgent + " " + userAgent);
//            setFontSize(fontSize + 75);
//
//            mAgentWeb.getWebCreator().getWebView().requestFocus();
//            mAgentWeb.getWebCreator().getWebView().requestFocusFromTouch();
//            mAgentWeb.getWebCreator().getWebView().setVerticalScrollBarEnabled(true);
//            mAgentWeb.getWebCreator().getWebView().setHorizontalScrollBarEnabled(true);
//        }
//    }
}
