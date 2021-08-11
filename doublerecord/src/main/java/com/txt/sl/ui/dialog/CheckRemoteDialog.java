package com.txt.sl.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.txt.sl.R;
import com.txt.sl.entity.bean.WorkItemBean;
import com.txt.sl.utils.RoomVideoUiUtils;

import java.util.ArrayList;

/**
 * Created by JustinWjq
 *
 * @date 2020/8/11.
 * description： 选择双录方式
 * ["agent","policyholder","insured"]
 */
public class CheckRemoteDialog extends Dialog implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    Context mContext;
    ArrayList<String> mMemberArray;
    String recordType = "2";

    public CheckRemoteDialog(@NonNull Context context) {
        super(context, R.style.tx_MyDialog);
        mContext = context;
        mMemberArray = new ArrayList();
    }

    TextView tv_local;
    TextView tv_confirm;
    TextView tv_remote;
    TextView  tv_text1;
    RadioGroup ll_person;
    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tx_dialog_changeremote);
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = RoomVideoUiUtils.getWindowWidth(mContext);
        window.setGravity(Gravity.BOTTOM);

        setCanceledOnTouchOutside(true);
        initView();

    }

    private void initView() {
        tv_local = findViewById(R.id.tv_local);
        tv_confirm = findViewById(R.id.tv_confirm);
        tv_local.setOnClickListener(this);
        tv_remote = findViewById(R.id.tv_remote);
//        tv_person1 = findViewById(R.id.tv_person1);
//        tv_person2 = findViewById(R.id.tv_person2);
        tv_text1 = findViewById(R.id.tv_text1);
        ll_person = findViewById(R.id.ll_person);
        group = findViewById(R.id.group);
        tv_remote.setOnClickListener(this);
//        tv_person1.setOnClickListener(this);
//        tv_person2.setOnClickListener(this);
        findViewById(R.id.tv_confirm).setOnClickListener(this);

        ll_person.setOnCheckedChangeListener(this);
    }

    @Override
    public void show() {
        super.show();
        showLocalAndRemoteView();
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
                mOnItemClickListener.onConfirmClick(isRemote,
                        recordType,
                        workItemBean
                        );
                dismiss();


            }
        }
//        else if (v.getId() == R.id.tv_person1) {
//            changeView1(tv_person1, "policyholder");
//        } else if (v.getId() == R.id.tv_person2) {
//            changeView1(tv_person2, "insured");
//        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    boolean isRemote = false;
//recordingMethod '0'现场'1'远程'2'不限 =======>双录方式管控
    private void showLocalAndRemoteView(){
        if (null != workItemBean) {
            String recordingMethod = workItemBean.getRecordingMethod();
            if ("0".equals(recordingMethod)) {
                tv_remote.setVisibility(View.GONE);
                tv_local.setVisibility(View.VISIBLE);

            }else if ("1".equals(recordingMethod)){
                tv_remote.setVisibility(View.VISIBLE);
                tv_local.setVisibility(View.GONE);
            }else{
                if (isSelfInsurance){
                    tv_remote.setVisibility(View.GONE);
                }else{
                    tv_remote.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private void changeView(boolean isRemoteBo) {

        isRemote = isRemoteBo;



        if (!isRemoteBo) {
            ll_person.clearCheck();
            tv_confirm.setEnabled(true);
            tv_local.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_bg_item_blue_20));

            tv_remote.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_button_gray_all_20));
            group.setVisibility(View.GONE);
        } else {

            resetMembers();
            if (isSelf) {
                mMemberArray.add("policyholder");
                group.setVisibility(View.GONE);
                tv_confirm.setEnabled(true);
            }else{
                group.setVisibility(View.VISIBLE);
                tv_confirm.setEnabled(false);
            }


            tv_local.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_button_gray_all_20));

            tv_remote.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_bg_item_blue_20));

        }

    }

    private void resetMembers() {
//        tv_person1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_button_gray_all_20));
//        tv_person2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tx_button_gray_all_20));
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
//            if ("policyholder".equals(members)) {
//                tv_person1.setVisibility(VISIBLE);
//            } else if ("insured".equals(members)) {
//                tv_person2.setVisibility(VISIBLE);
//            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.minpro_test) {
            mMemberArray.add("policyholder");
            recordType ="0";
            updateNextBt();
        } else if (checkedId == R.id.minpro_dev) {
            mMemberArray.add("policyholder");
            mMemberArray.add("insured");
            recordType ="1";
            updateNextBt();
        }
    }


    public interface OnRemoteClickListener {

        void onConfirmClick(boolean isRemote,
                            String recordType,
                            WorkItemBean workItemBean);

    }

    private ArrayList<String> membersArray;
    private boolean isSelfInsurance;
    private boolean isSelf;
    private WorkItemBean workItemBean;

    public void setData(WorkItemBean workItemBean) {

        this.membersArray = (ArrayList<String>) workItemBean.getMembersArray();
        this.isSelfInsurance = workItemBean.isSelfInsurance();
        this.isSelf = workItemBean.getRelationship().equals("Self");
        this.workItemBean = workItemBean;
        mMemberArray.clear();
    }

    OnRemoteClickListener mOnItemClickListener;

    public void setOnRemoteClickListener(OnRemoteClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


}
