package com.txt.sl.ui.dialog;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.widget.dialog.core.BottomPopupView;
import com.common.widget.dialog.util.PermissionConstants;
import com.txt.sl.R;
import com.txt.sl.utils.TxPermissionConstants;
import com.txt.sl.utils.TxPermissionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JustinWjq
 *
 * @date 2020/8/11.
 * description： 选择双录方式
 * ["agent","policyholder","insured"]
 */
public class CheckRemoteDialog extends BottomPopupView implements View.OnClickListener {
    Context mContext;
    ArrayList<String> mMemberArray;

    public CheckRemoteDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        mMemberArray = new ArrayList();
    }

    TextView tv_local;
    TextView tv_confirm;
    TextView tv_remote;
    TextView tv_person1, tv_person2, tv_text1;
    LinearLayout ll_person;
    Group group;

    @Override
    protected void onCreate() {
        super.onCreate();

        tv_local = findViewById(R.id.tv_local);
        tv_confirm = findViewById(R.id.tv_confirm);
        tv_local.setOnClickListener(this);
        tv_remote = findViewById(R.id.tv_remote);
        tv_person1 = findViewById(R.id.tv_person1);
        tv_person2 = findViewById(R.id.tv_person2);
        tv_text1 = findViewById(R.id.tv_text1);
        ll_person = findViewById(R.id.ll_person);
        group = findViewById(R.id.group);
        tv_remote.setOnClickListener(this);
        tv_person1.setOnClickListener(this);
        tv_person2.setOnClickListener(this);
        findViewById(R.id.tv_confirm).setOnClickListener(this);


    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.tx_dialog_changeremote;
    }

    @Override
    protected void onShow() {
        super.onShow();
        changeView(false);
        changeMembersView(membersArray);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_local) {
            changeView(false);
        } else if (v.getId() == R.id.tv_remote) {
            changeView(true);
        } else if (v.getId() == R.id.tv_confirm) {
            if (null != mOnItemClickListener) {
                mMemberArray.add("agent");
                mOnItemClickListener.onConfirmClick(isRemote, mFlowId, phone, taskId, mMemberArray);
                dismiss();


            }
        } else if (v.getId() == R.id.tv_person1) {
            changeView1(tv_person1, "policyholder");
        } else if (v.getId() == R.id.tv_person2) {
            changeView1(tv_person2, "insured");
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    boolean isRemote = false;

    private void changeView(boolean isRemoteBo) {
        isRemote = isRemoteBo;
        if (!isRemoteBo) {
            tv_confirm.setEnabled(true);
            tv_local.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_bg_item_blue_20));

            tv_remote.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_button_gray_all_20));
//            group.setVisibility(INVISIBLE);
            tv_text1.setVisibility(INVISIBLE);
            ll_person.setVisibility(INVISIBLE);
        } else {

            tv_confirm.setEnabled(false);
            tv_local.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_button_gray_all_20));

            tv_remote.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_bg_item_blue_20));
            tv_text1.setVisibility(VISIBLE);
            ll_person.setVisibility(VISIBLE);
            resetMembers();
        }

    }

    private void resetMembers() {
        tv_person1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_button_gray_all_20));
        tv_person2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_button_gray_all_20));
        mMemberArray.clear();
    }

    private void changeView1(View view, String role) {
        int index = mMemberArray.indexOf(role);
        if (-1 != index) {
            view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_button_gray_all_20));
            mMemberArray.remove(role);
        } else {
            view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_bg_item_blue_20));
            mMemberArray.add(role);
        }
        updateNextBt();
    }

    private void updateNextBt() {
        if (mMemberArray.size() == 0) {
            tv_confirm.setEnabled(false);
        } else {
            tv_confirm.setEnabled(true);
        }
    }

    private void changeMembersView(ArrayList<String> membersArray) {
        for (int i = 0; i < membersArray.size(); i++) {
            String members = membersArray.get(i);
            if ("policyholder".equals(members)) {
                tv_person1.setVisibility(VISIBLE);
            } else if ("insured".equals(members)) {
                tv_person2.setVisibility(VISIBLE);
            }
        }
    }


    public interface OnRemoteClickListener {

        void onConfirmClick(boolean isRemote, String flowId, String phone, String taskId, ArrayList<String> membersArray);

    }

    private String mFlowId;
    private String phone;
    private String taskId;
    private ArrayList<String> membersArray;

    public void setFlowId(String flowId, String phone, String taskId, ArrayList<String> membersArray) {
        this.mFlowId = flowId;
        this.phone = phone;
        this.taskId = taskId;
        this.membersArray = membersArray;
        mMemberArray.clear();
    }

    OnRemoteClickListener mOnItemClickListener;

    public void setOnRemoteClickListener(OnRemoteClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


}
