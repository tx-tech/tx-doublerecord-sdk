package com.txt.sl.system;

import android.media.MediaPlayer;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.txt.sl.config.TXManagerImpl;
import com.txt.sl.TXSdk;
import com.txt.sl.entity.bean.UploadOcrPic;
import com.txt.sl.entity.bean.UploadSignPic;
import com.txt.sl.entity.bean.UploadShotPic;
import com.txt.sl.entity.bean.RequestOrderBean;
import com.txt.sl.utils.LogUtils;
import com.txt.sl.BuildConfig;
import com.txt.sl.http.https.HttpRequestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SystemHttpRequest {

    private String port = "";

    private String TAG = SystemHttpRequest.class.getSimpleName();

    private static volatile SystemHttpRequest singleton = null;

    private SystemHttpRequest() {
    }

    public static SystemHttpRequest getInstance() {
        if (singleton == null) {
            synchronized (SystemHttpRequest.class) {
                if (singleton == null) {
                    singleton = new SystemHttpRequest();
                }
            }
        }
        return singleton;
    }

    String mCommonIp = "";
    String mDoubleRecordIP = "";

    public void changeIP(TXSdk.Environment environment) {
        switch (environment) {
            case DEV:
                mDoubleRecordIP = "https://dev1.ikandy.cn:60427";
                break;
            case RELEASE:
                mDoubleRecordIP = "https://sl-zrhj.cloud-ins.cn";
                break;
            case POC:
                mDoubleRecordIP = "https://doublerecord.cloud-ins.cn";
                break;
            default:
                mDoubleRecordIP = "https://new-2record.ikandy.cn";
        }
        switch (environment) {
            case DEV:
                mCommonIp = "https://dev1.ikandy.cn:60428";
                break;
            case RELEASE:
                mCommonIp = "https://service-support-prod.ikandy.cn";
                break;
            case POC:
                mCommonIp = "https://common.cloud-ins.cn";
                break;
            default:
                mCommonIp = "https://service-support-test.ikandy.cn";
        }
    }


    public void changeIP(String ip, String port) {
        if (port.isEmpty()) {
            mDoubleRecordIP = "https://" + ip;
        } else {
            mDoubleRecordIP = "https://" + ip + ":" + port;
        }

    }

    public void customIp(String commonIp,String doublerecordIp) {
        mCommonIp = "";
        mDoubleRecordIP = "";
    }

    public void uploadFile(String filePath, String preTime, String serviceId, String author,
                           HttpRequestClient.RequestHttpCallBack callback, HttpRequestClient.UploadProgressListener back) {
        StringBuilder builder = new StringBuilder(mDoubleRecordIP + "/api/serviceRoom/uploadVideo");

        LogUtils.i("filePath", filePath);
        LogUtils.i("preTime", preTime);
        LogUtils.i("serviceId", serviceId);
        if (TextUtils.isEmpty(filePath)) {
            callback.onFail("upload file is null", -2);
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            callback.onFail("upload file is null", -2);
            return;
        }
        Map<String, String> stringStringMap = new HashMap<>();
        stringStringMap.put("serviceId", serviceId);
        stringStringMap.put("preTime", preTime);
        HttpRequestClient.getIntance().postFile(builder.toString(), stringStringMap, author, file, callback, back);
    }

    public void cancelClient() {
        HttpRequestClient.getIntance().cancelClient();
    }


    public void getVideoSizeAndDuration(String fileName, onFileCallBack onFileCallBack) {

        LogUtils.i(TAG, "fileName: " + fileName);

        File upFile = null;
        try {
            upFile = new File(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            long length = upFile.length();
            mediaPlayer.setDataSource(upFile.getAbsolutePath());
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            onFileCallBack.onFile(length, duration);
            mediaPlayer.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //上传log日志
    public void uploadLogFile(String fileName, String preTime, String serviceId, onFileCallBack onFileCallBack, final onRequestCallBack callBack, HttpRequestClient.UploadProgressListener back) {


        LogUtils.d(TAG, "fileName: " + fileName);
        if (fileName == null || fileName.equals("")) {
            LogUtils.d(TAG, "uploadLogFile: file is null");
            if (callBack != null) {
                callBack.onFail("file is empty");
            }
            return;
        }
        File file = new File(fileName);
        if (!file.exists()) {
            if (callBack != null) {
                callBack.onFail("文件不存在");
            }
            return;
        }
//        String[] split = fileName.split("\\.mp4");
//        String zipFileName = split[0] + ".zip";
        File upFile = null;
        try {
//            ZipUtil.zipFolder(fileName, zipFileName);
            upFile = new File(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (upFile == null && upFile.exists()) {
            upFile = file;
        }


        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            long length = upFile.length();
            mediaPlayer.setDataSource(upFile.getAbsolutePath());
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            onFileCallBack.onFile(length, duration);
            mediaPlayer.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        getSystem(SystemLogHelper.class).deleteFile(fileName); //删除文件
        LogUtils.d(TAG, "uploadLogFile: " + upFile.getAbsolutePath());
//        cutFile(upFile.getAbsolutePath(),);
        uploadFile(upFile.getAbsolutePath(), preTime, serviceId, TXManagerImpl.getInstance().getToken(), new HttpRequestClient.RequestHttpCallBack() {
            @Override
            public void onSuccess(String json) {


                if (callBack != null) {
                    callBack.onSuccess();
                }
            }

            @Override
            public void onFail(String err, int code) {
                LogUtils.d(TAG, "onFail: err:" + err + "  code:" + code);

                if (callBack != null) {
                    callBack.onFail(err);
                }

            }
        }, back);
    }

    public void getRecordInstitutionList(String tenantId, HttpRequestClient.RequestHttpCallBack callback) {


        HttpRequestClient.getIntance().get(mDoubleRecordIP + "/api/record/institutions?tenantId=" + tenantId, TXManagerImpl.getInstance().getToken(), callback);
    }

    // /api/serviceRoom/getCosStsToken
    public void getCosStsToken(HttpRequestClient.RequestHttpCallBack callback) {


        HttpRequestClient.getIntance().get(mDoubleRecordIP + "/api/serviceRoom/getCosStsToken", TXManagerImpl.getInstance().getToken(), callback);
    }

    public void pushMessage(String json, HttpRequestClient.RequestHttpCallBack callback) {


        HttpRequestClient.getIntance().post(mDoubleRecordIP + "/api/serviceRoom/pushMessage", json, TXManagerImpl.getInstance().getToken(), callback);
    }


    public void startRecord(String jsonObject, HttpRequestClient.RequestHttpCallBack callback) {


        HttpRequestClient.getIntance().post(mDoubleRecordIP + "/api/serviceRoom/startRecord", jsonObject.toString(), TXManagerImpl.getInstance().getToken(), callback);
    }

    public void setServiceRoomStatus(String serviceId, String userId, String status, HttpRequestClient.RequestHttpCallBack callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serviceId", serviceId);
            jsonObject.put("userId", userId);
            jsonObject.put("status", status);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequestClient.getIntance().post(mDoubleRecordIP + "/api/serviceRoom/setServiceRoomStatus", jsonObject.toString(), TXManagerImpl.getInstance().getToken(), callback);
    }

    public void uploadServiceVideoNew(String serviceId, String preTime, String url, HttpRequestClient.RequestHttpCallBack callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serviceId", serviceId);
            jsonObject.put("preTime", preTime);
            jsonObject.put("url", url);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequestClient.getIntance().post(mDoubleRecordIP + "/api/serviceRoom/uploadServiceVideoNew", jsonObject.toString(), TXManagerImpl.getInstance().getToken(), callback);
    }


    public void nextStep(String jsonObject, HttpRequestClient.RequestHttpCallBack callback) {


        HttpRequestClient.getIntance().post(mDoubleRecordIP + "/api/serviceRoom/nextStep", jsonObject.toString(), TXManagerImpl.getInstance().getToken(), callback);
    }


    public void endRecord(String jsonObject, HttpRequestClient.RequestHttpCallBack callback) {


        HttpRequestClient.getIntance().post(mDoubleRecordIP + "/api/serviceRoom/endRecord", jsonObject.toString(), TXManagerImpl.getInstance().getToken(), callback);
    }


    public void getRoomInfo(String serviceId, HttpRequestClient.RequestHttpCallBack callback) {

        HttpRequestClient.getIntance().get(mDoubleRecordIP + "/api/serviceRoom/roomInfo/" + serviceId, TXManagerImpl.getInstance().getToken(), callback);
    }


    public void startAgent(String flowId,
                           boolean isRemote,
                           ArrayList<String> roleArray,
                           String recordType,
                           HttpRequestClient.RequestHttpCallBack callback) {
        JSONArray jsonArray = new JSONArray();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("flowId", flowId);
            jsonObject.put("isRemote", isRemote);
            for (int i = 0; i < roleArray.size(); i++) {
                jsonArray.put(roleArray.get(i));
            }
            jsonObject.put("role", jsonArray);
            if (isRemote) {
                jsonObject.put("recordType", recordType);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequestClient.getIntance().post(mDoubleRecordIP + "/api/serviceRoom/startAgent", jsonObject.toString(), TXManagerImpl.getInstance().getToken(), callback);
    }


    public void getAllSteps(String serviceId, HttpRequestClient.RequestHttpCallBack callback) {

        HttpRequestClient.getIntance().get(mDoubleRecordIP + "/api/serviceRoom/getStepsAll?serviceId=" + serviceId, TXManagerImpl.getInstance().getToken(), callback);
    }

    public void getInsuranceData(String agentId, HttpRequestClient.RequestHttpCallBack callback) {

        HttpRequestClient.getIntance().get(mCommonIp + "/api/insurance/agent?id=" + agentId, TXManagerImpl.getInstance().getToken(), callback);
    }

    public void getProductData(String agentId, HttpRequestClient.RequestHttpCallBack callback) {

        HttpRequestClient.getIntance().get(mDoubleRecordIP + "/api/insurances", TXManagerImpl.getInstance().getToken(), callback);
    }


    public void getRecordTaskProcessDisabled(String flowId, HttpRequestClient.RequestHttpCallBack callback) {

        HttpRequestClient.getIntance().get(mDoubleRecordIP + "/api/record/task/process/disabled?flowId=" + flowId, TXManagerImpl.getInstance().getToken(), callback);
    }

    public void getRecordTask(String flowId, HttpRequestClient.RequestHttpCallBack callback) {

        HttpRequestClient.getIntance().get(mDoubleRecordIP + "/api/record/task?flowId=" + flowId + "&isApp=true", TXManagerImpl.getInstance().getToken(), callback);
    }


    public void login(String loginName, String password, HttpRequestClient.RequestHttpCallBack callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("loginName", loginName);
            jsonObject.put("password", password);
            jsonObject.put("logOnToWay", "app");
        } catch (Exception e) {

        }

        HttpRequestClient.getIntance().post(mCommonIp + "/api/login", jsonObject.toString(), "", callback);
    }

    public void agentIdCard(UploadShotPic flowId, HttpRequestClient.RequestHttpCallBack callback) {

        Gson gson = new Gson();
        String s = gson.toJson(flowId);

        HttpRequestClient.getIntance().post(mCommonIp + "/api/idCompareRealTime/common", s, TXManagerImpl.getInstance().getToken(), callback);
    }

    public void ocr(UploadOcrPic uploadOcrPic, HttpRequestClient.RequestHttpCallBack callback) {
//
        Gson gson = new Gson();
        String s = gson.toJson(uploadOcrPic);

        HttpRequestClient.getIntance().post(mCommonIp + "/api/ocr/agentIdCard", s, TXManagerImpl.getInstance().getToken(), callback);

    }

    public void signPic(UploadSignPic signPicBean, HttpRequestClient.RequestHttpCallBack callback) {

        Gson gson = new Gson();
        String s = gson.toJson(signPicBean);

        HttpRequestClient.getIntance().post(mCommonIp + "/api/ocr/signPic", s, TXManagerImpl.getInstance().getToken(), callback);
    }

    public void faceDetection(JSONObject jsonObject, HttpRequestClient.RequestHttpCallBack callback) {

        HttpRequestClient.getIntance().post(mDoubleRecordIP + "/api/serviceRoom/faceDetection", jsonObject.toString(), TXManagerImpl.getInstance().getToken(), callback);
    }


    public void update(String code, String agentId, RequestOrderBean bean, HttpRequestClient.RequestHttpCallBack callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", code);
            jsonObject.put("agentId", agentId);

            Gson gson = new Gson();
            String s = gson.toJson(bean);
            JSONObject fieldsJsonObject = new JSONObject(s);

            //insurancePrompt: '投保提示书',
            fieldsJsonObject.put("insurancePrompt","https://gdrb-dingsun-test-1255383806.cos.ap-shanghai.myqcloud.com/%E3%80%90%E5%A4%87%E6%A1%88%E7%89%88%E3%80%91%E7%88%B1%E5%BF%83%E4%BA%BA%E5%AF%BF%E9%99%84%E5%8A%A0%E6%8A%95%E4%BF%9D%E4%BA%BA%E8%B1%81%E5%85%8D2021%E6%9D%A1%E6%AC%BE.pdf");
            //   productsClause: '产品条款',
            //   writtenDocument: '免除保险人责任条款的书面说明',
            //   insurancePolicy: '投保单',
            //   productSpecification: '产品说明书'
            //   insuranceClause:‘保险条款’
            //   bankTransferAdvice:'银行转账通知'
            fieldsJsonObject.put("productsClause","https://gdrb-dingsun-test-1255383806.cos.ap-shanghai.myqcloud.com/%E7%88%B1%E5%BF%83%E4%BA%BA%E5%AF%BF%E5%AE%88%E6%8A%A4%E7%A5%9E2.0%E7%BB%88%E8%BA%AB%E5%AF%BF%E9%99%A9--%E6%9D%A1%E6%AC%BE.pdf");
            fieldsJsonObject.put("writtenDocument","https://gdrb-dingsun-test-1255383806.cos.ap-shanghai.myqcloud.com/%E7%88%B1%E5%BF%83%E4%BA%BA%E5%AF%BF%E5%AE%88%E6%8A%A4%E7%A5%9E2.0%E7%BB%88%E8%BA%AB%E5%AF%BF%E9%99%A9-%E5%85%B3%E4%BA%8E%E5%85%8D%E9%99%A4%E4%BF%9D%E9%99%A9%E4%BA%BA%E8%B4%A3%E4%BB%BB%E6%9D%A1%E6%AC%BE%E7%9A%84%E4%B9%A6%E9%9D%A2%E8%AF%B4%E6%98%8E.pdf");
            fieldsJsonObject.put("insurancePolicy","https://csms-uat.ebaocloud.com.cn/csms2/mobile/policy-fxbdx/signature/4268?delegated=ZRHJ.zr-longkou&ticket=CSMD%20WlJISjoyMDY2NjEwOTA2MWIwMGJjOTQ0MWY2NTg4N2VlM2Y4ZToxNjQ4NTY5NjAwMDAw&token=nedKneC0kf1PtJTpFMWc&parcel=company-zrhj%2Cproduct-FXBDXXYLQ%2Cagent-zr-longkou&rootUrlFrom=https%3A%2F%2Fappuat.zrbxyun.com%2F%23!%2Fapp%2Ftab%2Fhome&actionType=read&pageStep=sign-signature");
            fieldsJsonObject.put("productSpecification","https://csms-uat.ebaocloud.com.cn/csms2/mobile/policy-fxbdx/signature/4268?delegated=ZRHJ.zr-longkou&ticket=CSMD%20WlJISjoyMDY2NjEwOTA2MWIwMGJjOTQ0MWY2NTg4N2VlM2Y4ZToxNjQ4NTY5NjAwMDAw&token=nedKneC0kf1PtJTpFMWc&parcel=company-zrhj%2Cproduct-FXBDXXYLQ%2Cagent-zr-longkou&rootUrlFrom=https%3A%2F%2Fappuat.zrbxyun.com%2F%23!%2Fapp%2Ftab%2Fhome&actionType=read&pageStep=sign-signature");
           //            fieldsJsonObject.put("insuranceClause","https://csms-uat.ebaocloud.com.cn/csms2/mobile/policy-fxbdx/signature/4268?delegated=ZRHJ.zr-longkou&ticket=CSMD%20WlJISjoyMDY2NjEwOTA2MWIwMGJjOTQ0MWY2NTg4N2VlM2Y4ZToxNjQ4NTY5NjAwMDAw&token=nedKneC0kf1PtJTpFMWc&parcel=company-zrhj%2Cproduct-FXBDXXYLQ%2Cagent-zr-longkou&rootUrlFrom=https%3A%2F%2Fappuat.zrbxyun.com%2F%23!%2Fapp%2Ftab%2Fhome&actionType=read&pageStep=sign-signature");
//            fieldsJsonObject.put("bankTransferAdvice","https://csms-uat.ebaocloud.com.cn/csms2/mobile/policy-fxbdx/signature/4268?delegated=ZRHJ.zr-longkou&ticket=CSMD%20WlJISjoyMDY2NjEwOTA2MWIwMGJjOTQ0MWY2NTg4N2VlM2Y4ZToxNjQ4NTY5NjAwMDAw&token=nedKneC0kf1PtJTpFMWc&parcel=company-zrhj%2Cproduct-FXBDXXYLQ%2Cagent-zr-longkou&rootUrlFrom=https%3A%2F%2Fappuat.zrbxyun.com%2F%23!%2Fapp%2Ftab%2Fhome&actionType=read&pageStep=sign-signature");

            fieldsJsonObject.put("insuranceCompanyLevel2","623c365bf70d00000f0007e8");
            fieldsJsonObject.put("insuranceCompanyNew","623aaaf92f740000aa0023e8");
            //
           // 当签字对象为投保人时，取客户推送保单信息接口中的policyHolderSignPage的url，跟同屏SDK交互，获取两个链接

           // 当签字对象为被保人时，取客户推送保单信息接口中的insuredSignPage的url，跟同屏SDK交互，获取两个链接
            fieldsJsonObject.put("policyHolderSignPage","https://csms-uat.ebaocloud.com.cn/csms2/mobile/policy-fxbdx/signature/4268?delegated=ZRHJ.zr-longkou&ticket=CSMD%20WlJISjoyMDY2NjEwOTA2MWIwMGJjOTQ0MWY2NTg4N2VlM2Y4ZToxNjQ4NTY5NjAwMDAw&token=nedKneC0kf1PtJTpFMWc&parcel=company-zrhj%2Cproduct-FXBDXXYLQ%2Cagent-zr-longkou&rootUrlFrom=https%3A%2F%2Fappuat.zrbxyun.com%2F%23!%2Fapp%2Ftab%2Fhome&actionType=read&pageStep=sign-signature");
            fieldsJsonObject.put("insuredSignPage","https://csms-uat.ebaocloud.com.cn/csms2/mobile/policy-fxbdx/signature/4268?delegated=ZRHJ.zr-longkou&ticket=CSMD%20WlJISjoyMDY2NjEwOTA2MWIwMGJjOTQ0MWY2NTg4N2VlM2Y4ZToxNjQ4NTY5NjAwMDAw&token=nedKneC0kf1PtJTpFMWc&parcel=company-zrhj%2Cproduct-FXBDXXYLQ%2Cagent-zr-longkou&rootUrlFrom=https%3A%2F%2Fappuat.zrbxyun.com%2F%23!%2Fapp%2Ftab%2Fhome&actionType=read&pageStep=sign-signature");

//          "insuranceCompanyLevel2": "623c365bf70d00000f0007e8",
//          "insuranceCompanyNew": "623aaaf92f740000aa0023e8",
            jsonObject.put("fields", fieldsJsonObject);
        } catch (Exception e) {

        }


        HttpRequestClient.getIntance().post(mCommonIp + "/api/report/CommonRecord/update", jsonObject.toString(), TXManagerImpl.getInstance().getToken(), callback);
    }

    public void list(String state, HttpRequestClient.RequestHttpCallBack callback) {
        list(state, "", callback);
    }

    public void list(String state, String policyholderName, HttpRequestClient.RequestHttpCallBack callback) {

        StringBuffer stringBuffer = new StringBuffer(mDoubleRecordIP + "/api/record/tasks/app?");
        stringBuffer.append("pageSize=").append("100");
        stringBuffer.append("&pageIndex=").append("1");
        if (!state.isEmpty()) {
            stringBuffer.append("&status=").append(state);
        }

        if (!policyholderName.isEmpty()) {
            stringBuffer.append("&policyholderName=").append(policyholderName);
        }

        HttpRequestClient.getIntance().get(stringBuffer.toString(), TXManagerImpl.getInstance().getToken(), callback);
    }

    public void getFlowDetails(String flowid, String agentId, HttpRequestClient.RequestHttpCallBack callback) {

        HttpRequestClient.getIntance().get(mCommonIp + "/api/report/detail?flowId=" + flowid + "&agentId=" + agentId, TXManagerImpl.getInstance().getToken(), callback);
    }

    public void getFlowDetails(String flowid, HttpRequestClient.RequestHttpCallBack callback) {

        HttpRequestClient.getIntance().get(mDoubleRecordIP + "/api/record/tasks/app/getReportInfo?flowId=" + flowid, TXManagerImpl.getInstance().getToken(), callback);
    }

    public void getFlowDetailsByTaskid(String taskid, HttpRequestClient.RequestHttpCallBack callback) {

        HttpRequestClient.getIntance().get(mDoubleRecordIP + "/api/record/tasks/app/getReportInfo?taskId=" + taskid, TXManagerImpl.getInstance().getToken(), callback);
    }

    public void passwordFreeLogin(String orgCode, String sign, String loginName, String fullName, HttpRequestClient.RequestHttpCallBack callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("orgCode", orgCode);
            jsonObject.put("sign", sign);
            jsonObject.put("loginName", loginName);
            jsonObject.put("fullName", fullName);
            jsonObject.put("roleCode", "doubleRecordAgent");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequestClient.getIntance().post(mCommonIp + "/api/auth/passwordFreeLogin", jsonObject.toString(), "", callback);
    }


    public static void cutFile(String src, String endsrc, int num) {
        FileInputStream fis = null;
        File file = null;
        try {
            fis = new FileInputStream(src);
            file = new File(src);
            //创建规定大小的byte数组
            byte[] b = new byte[num];
            int len = 0;
            //name为以后的小文件命名做准备
            int name = 1;
            //遍历将大文件读入byte数组中，当byte数组读满后写入对应的小文件中
            while ((len = fis.read(b)) != -1) {
                //分别找到原大文件的文件名和文件类型，为下面的小文件命名做准备
                String name2 = file.getName();
                int lastIndexOf = name2.lastIndexOf(".");
                String substring = name2.substring(0, lastIndexOf);
                String substring2 = name2.substring(lastIndexOf, name2.length());
                FileOutputStream fos = new FileOutputStream(endsrc + substring + "_" + name + substring2);
                //将byte数组写入对应的小文件中
                fos.write(b, 0, len);
                //结束资源
                fos.close();
                name++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    //结束资源
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface onRequestCallBack {
        public void onSuccess();

        public void onFail(String msg);
    }

    public interface onFileCallBack {
        public void onFile(long size, int time);


    }
}
