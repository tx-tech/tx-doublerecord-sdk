package com.txt.sl.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by JustinWjq
 *
 * @date 2020-03-16.
 * description：
 */
public class EditTextUtils {
    /**
     *  设置edittext只能输入小数点后两位
     */
    public static void afterDotTwo(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 限制最多能输入9位整数
                LogUtils.i("et_prices--afterDotTwo-----afterTextChanged" +s.toString());
                if (s.toString().contains(".")) {
                    if (s.toString().indexOf(".") > 9) {
                        s = s.toString().subSequence(0,9) + s.toString().substring(s.toString().indexOf("."));
                        editText.setText(s);
                        editText.setSelection(9);
                    }else{
                        if (s.toString().length()==3) {
                            editText.setText("");

                        }
                    }
                }else {
                    if (s.toString().length() > 9){
                        s = s.toString().subSequence(0,9);
                        editText.setText(s+".00");
                        editText.setSelection(9+3);
                    }else{
                        if (!s.toString().isEmpty()){
                            editText.setText(s+".00");
                            editText.setSelection(s.length());
                        }

                    }
                }
                // 判断小数点后只能输入两位
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 3);
                        editText.setText(s);
                        editText.setSelection(s.length());
                    }
                }
                //如果第一个数字为0，第二个不为点，就不允许输入
                if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        editText.setText(s.subSequence(0, 1));
                        editText.setSelection(1);
                        return;
                    }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {
//                if (editText.getText().toString().trim() != null && !editText.getText().toString().trim().equals("")) {
//                    if (editText.getText().toString().trim().substring(0, 1).equals(".")) {
//                        editText.setText("0" + editText.getText().toString().trim());
//                        editText.setSelection(1);
//                    }
//                }
            }
        });
    }

}
