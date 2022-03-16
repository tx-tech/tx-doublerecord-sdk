package com.tx.znsl;



import com.txt.sl.TXSdk;

import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import kotlin.jvm.JvmStatic;

/**
 * Created by JustinWjq
 * @date 2020/9/7.
 * description： 加密工具类
 */
public class SignUtils {
    //解密密钥
    //测试密钥key
    public static final String key = BuildConfig.key;
    //正式密钥key
    public static final String rekey = BuildConfig.rekey;
    //测试向量iv
    public static final String initVector =  BuildConfig.initVector;
    //正式向量iv
    public static final String reinitVector = BuildConfig.reinitVector;

    //加密
    @JvmStatic
    public static String Encrypt(String content) throws Exception {
        try {
            IvParameterSpec iv;
            switch (TXSdk.getInstance().getEnvironment()) {
                case DEV:
                case TEST:
                case RELEASE:
                    iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
                    break;
                default:
                    iv = new IvParameterSpec(reinitVector.getBytes("UTF-8"));
            }
            SecretKeySpec skeySpec;
            switch (TXSdk.getInstance().getEnvironment()) {
                case DEV:
                case TEST:
                case RELEASE:
                    skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
                    break;
                default:
                    skeySpec = new SecretKeySpec(rekey.getBytes("UTF-8"), "AES");
            }



            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(content.getBytes());

            return byte2HexStr(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @JvmStatic
    public static String decrypt(String encryptedStr) {
        try {
            IvParameterSpec ivParameterSpec;
            switch (TXSdk.getInstance().getEnvironment()) {
                case DEV:
                case TEST:
                    ivParameterSpec = new IvParameterSpec(initVector.getBytes("UTF-8"));
                    break;
                default:
                    ivParameterSpec = new IvParameterSpec(reinitVector.getBytes("UTF-8"));
            }
            SecretKeySpec skeySpec;
            switch (TXSdk.getInstance().getEnvironment()) {
                case DEV:
                case TEST:
                    skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
                    break;
                default:
                    skeySpec = new SecretKeySpec(rekey.getBytes("UTF-8"), "AES");
            }

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);

            byte[] bytes = hexStr2Bytes(encryptedStr);
            byte[] original = cipher.doFinal(bytes);

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static byte[] hexStr2Bytes(String hexStr) {
        hexStr = hexStr.trim().replace(" ", "").toUpperCase(Locale.US);
        int m = 0, n = 0;
        int iLen = hexStr.length() / 2;
        byte[] ret = new byte[iLen];

        for (int i = 0; i < iLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = (byte) (Integer.decode("0x" + hexStr.substring(i * 2, m) + hexStr.substring(m, n)) & 0xFF);
        }
        return ret;
    }


    public static String byte2HexStr(byte[] bytes) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < bytes.length; n++) {
            stmp = (Integer.toHexString(bytes[n] & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
        }
        return hs;
    }

}
