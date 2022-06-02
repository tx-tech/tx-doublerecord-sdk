package com.txt.sl.ui.adpter;


import android.text.TextUtils;
import android.view.View;

import com.common.widget.recyclerviewadapterhelper.base.BaseMultiItemQuickAdapter;
import com.common.widget.recyclerviewadapterhelper.base.TxBaseViewHolder;
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity;
import com.txt.sl.R;
import com.txt.sl.entity.bean.WorkItemBean;
import com.txt.sl.utils.DateUtils;


import java.text.ParseException;
import java.util.List;

/**
 * Created by JustinWjq
 *
 * @date 2020/5/25.
 * description：
 */
public class OrderListNodeAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, TxBaseViewHolder> {

    public static final int TYPE_UnRecorded = 1;
    public static final int TYPE_Refused = 2;
    public static final int TYPE_UnUploaded = 3;
    public static final int TYPE_UnChecked = 4;
    public static final int TYPE_Completed = 5;

    public OrderListNodeAdapter(List<MultiItemEntity> data) {
        super(data);
        addItemType(TYPE_UnRecorded, R.layout.tx_rv_item_worker_item_unrecorded);
        addItemType(TYPE_Refused, R.layout.tx_rv_item_worker_item_refused);
        addItemType(TYPE_UnUploaded, R.layout.tx_rv_item_worker_item_unuploaded);
        addItemType(TYPE_UnChecked, R.layout.tx_rv_item_worker_item_unchecked);
        addItemType(TYPE_Completed, R.layout.tx_rv_item_worker_item_completed);

    }


    @Override
    protected void convert(TxBaseViewHolder helper, MultiItemEntity item) {
        WorkerItemTypeBean itemTypeBean = (WorkerItemTypeBean) item;
        WorkItemBean workItemBean = itemTypeBean.workItemBean;
        View view = helper.getView(R.id.ll_remote);
        if (null != workItemBean) {
            helper.setText(R.id.tv_insurantName, workItemBean.getInsurantName());
            helper.setText(R.id.tv_insuranceName, workItemBean.getInsuranceName());
            String insurerQuotationNo = workItemBean.getInsurerQuotationNo();
            //业务单号 投保单号 如果没有投保单号 就用业务单号
            boolean empty = TextUtils.isEmpty(insurerQuotationNo);
            if (empty){
                helper.setText(R.id.tv_taskid_title,"业务单号" );
                helper.setText(R.id.tv_taskid, workItemBean.getTaskId());
            }else{
                helper.setText(R.id.tv_taskid_title,"投保单号" );
                helper.setText(R.id.tv_taskid, insurerQuotationNo);
            }



            String isremoteStr = "";
            if (workItemBean.isIsRemote()) {
                isremoteStr = "远程双录";
            } else {
                isremoteStr = "现场双录";
            }

            helper.setText(R.id.tv_remote, isremoteStr);



        }

        helper.addOnClickListener(R.id.ll_common);
        switch (itemTypeBean.getItemType()) {
            case TYPE_UnRecorded:
                helper.setText(R.id.tv_ctime_title, "创建时间");
                try {
                    String s = DateUtils.UTCToCST(workItemBean.getCtime());
                    helper.setText(R.id.tv_ctime, s);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                helper.addOnClickListener(R.id.tv_item1_sl);
                view.setVisibility(View.GONE);
                break;
            case TYPE_Refused:
                try {
                    String s = DateUtils.UTCToCST(workItemBean.getUtime());
                    helper.setText(R.id.tv_ctime, s);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                helper.setText(R.id.tv_ctime_title, "驳回时间");
                helper.addOnClickListener(R.id.tv_details, R.id.tv_item1_sl);
                view.setVisibility(View.VISIBLE);
                break;
            case TYPE_UnUploaded:
                try {
                    String s = DateUtils.UTCToCST(workItemBean.getUtime());
                    helper.setText(R.id.tv_ctime, s);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                helper.setText(R.id.tv_ctime_title, "录制完成时间");
                helper.addOnClickListener(R.id.tv_unupload_play, R.id.tv_replay, R.id.tv_item2_play);
                view.setVisibility(View.VISIBLE);
                break;
            case TYPE_UnChecked:
                try {
                    String s = DateUtils.UTCToCST(workItemBean.getUtime());
                    helper.setText(R.id.tv_ctime, s);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                helper.setText(R.id.tv_ctime_title, "上传时间");
                helper.addOnClickListener(R.id.tv_playremotevideo);
                view.setVisibility(View.VISIBLE);
                break;
            case TYPE_Completed:
                try {
                    String s = DateUtils.UTCToCST(workItemBean.getUtime());
                    helper.setText(R.id.tv_ctime, s);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                helper.setText(R.id.tv_ctime_title, "完成时间");
                helper.addOnClickListener(R.id.tv_item2_play);
                view.setVisibility(View.VISIBLE);
                break;

            default:
        }


    }


}
