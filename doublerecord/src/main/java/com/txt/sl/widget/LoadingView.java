package com.txt.sl.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txt.sl.R;


public class LoadingView extends Dialog {

    private String mMessage;
    private BallFadeLoadingView mLoadView;
    private Context mContext;

    private LinearLayout mShowUploadView;
    private TextView mUploadNumber;
    private TextView mLoadTextView;

    public final static int SHOWLOADING = 0;
    public final static int SHOWUPLOAD = 1;

    public int mType = SHOWLOADING;

    public LoadingView(Context context, int theme) {
        super(context, theme);
    }

    public LoadingView(Context context, String message, int type) {
        this(context, R.style.tx_DialogStyle);
        this.mMessage = message;
        this.mContext = context;
        this.mType = type;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.tx_dialog_loading, null);
        setContentView(view);
        mLoadTextView = (TextView) view.findViewById(R.id.tvLoadingText);

        mUploadNumber = (TextView) findViewById(R.id.uploadnumber);
        mShowUploadView = (LinearLayout) findViewById(R.id.showuploadnumber);
        mLoadView = (BallFadeLoadingView) view.findViewById(R.id.ballView);
        initShowType();
        if (!TextUtils.isEmpty(mMessage)) {
            mLoadTextView.setVisibility(View.VISIBLE);
            mLoadTextView.setText(mMessage);
        } else {
            mLoadTextView.setVisibility(View.GONE);
        }
        setCanceledOnTouchOutside(false);
    }

    public void setmUploadNumber(int uploadnumber, int total) {
        String number = uploadnumber + "/" + total;
        mUploadNumber.setText(number);
    }

    public void setEmptyText() {
        mUploadNumber.setText("");
    }
    public void setCustomText(String CustomText) {
        mUploadNumber.setText(CustomText);
    }


    //设置显示的type
    public void initShowType() {
        if (mType == SHOWUPLOAD) {
            mShowUploadView.setVisibility(View.VISIBLE);

        } else if (mType == SHOWLOADING) {
            mShowUploadView.setVisibility(View.GONE);

        }

    }

    @Override
    public void show() {
        super.show();


        if (mLoadView != null)
            mLoadView.startAnimators();


    }

    @Override
    public void dismiss() {

        if (mLoadView != null) {
            mLoadView.stopAnimators();
            setEmptyText();
        }
        super.dismiss();
    }
}
