package com.txt.sl.ui.invite;

import com.tencent.cos.xml.transfer.TransferState;

/**
 * author ：Justin
 * time ：2021/9/9.
 * des ：
 */
public interface OnUploadListener {
    void onProgress(long complete, long target);

    void onStateChanged(TransferState var1, String uploadId);

    void onFail();

    void onSuccess();
}
