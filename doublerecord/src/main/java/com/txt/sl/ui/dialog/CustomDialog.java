package com.txt.sl.ui.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.common.widget.dialog.core.CenterPopupView;
import com.common.widget.dialog.util.XPopupUtils;
import com.txt.sl.R;

/**
 * Created by JustinWjq
 *
 * @date 2020/8/11.
 * description：
 */
public class CustomDialog extends CenterPopupView {
    public CustomDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        EditText et_ip = findViewById(R.id.et_ip);
        EditText et_port = findViewById(R.id.et_port);
        TextView tv_confirm = findViewById(R.id.tv_confirm);
        TextView tv_current = findViewById(R.id.tv_current);

        tv_confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onConfirmClick(et_ip.getText().toString(), et_port.getText().toString());
                }

            }
        });
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.tx_dialog_control;
    }

    @Override
    protected int getMaxHeight() {      //最大高度
        return (int) (XPopupUtils.getWindowHeight(getContext()) * .5f);
    }


    public interface OnConfirmClickListener {
        /**
         * @param ip
         * @param port
         */
        void onConfirmClick(String ip, String port);
    }

    OnConfirmClickListener mOnItemClickListener;

    public void setOnConfirmClickListener(OnConfirmClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


}
