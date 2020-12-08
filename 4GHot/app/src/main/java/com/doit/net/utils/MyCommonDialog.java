package com.doit.net.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.TextView;

import com.doit.net.ucsi.R;

import java.lang.reflect.Field;

/**
 * Created by Zxc on 2019/12/26.
 */

public class MyCommonDialog {
    private Context context;
   // AlertDialog dialog;
    AlertDialog.Builder dialogBuiler;
    private int defaultTextColor;
    private int defaultButonTextColor;

    private String title = "未定义";
    private String content = "未定义";
    private CharSequence[] items;
    DialogInterface.OnClickListener itemsListener;

    private DialogInterface.OnClickListener negativeListener = null;
    private String negativeText;

    private DialogInterface.OnClickListener postiveListener = null;
    private String postiveText;

    public MyCommonDialog(Context context) {
        this.context = context;
        dialogBuiler = new AlertDialog.Builder(context, R.style.MyDialogBkg);//背景颜色固定
        defaultButonTextColor = context.getResources().getColor(R.color.darkorange);
        defaultTextColor = context.getResources().getColor(R.color.white);
    }

    public void setItems(CharSequence[] items, DialogInterface.OnClickListener listener){
        //dialog.setItems(items, null);
        this.items = items;
        this.itemsListener = listener;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setContent(String content){
        this.content = content;
    }

    public void showDialog(){
        if (items.length != 0){
            dialogBuiler.setItems(items, null);
        }else{
            dialogBuiler.setMessage(content);
        }

        dialogBuiler.setTitle(title);
        dialogBuiler.setNegativeButton(negativeText, negativeListener);
        dialogBuiler.setPositiveButton(postiveText, postiveListener);
        AlertDialog dialog = dialogBuiler.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(defaultButonTextColor);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(defaultButonTextColor);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(defaultButonTextColor);

        //设置标题颜色
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object alertController = mAlert.get(dialog);

            Field mTitleView = alertController.getClass().getDeclaredField("mTitleView");
            mTitleView.setAccessible(true);

            TextView title = (TextView) mTitleView.get(alertController);
            title.setTextColor(defaultTextColor);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        //设置内容文字颜色
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(dialog);
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(defaultTextColor);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void setNegativeButton(String buttonText, DialogInterface.OnClickListener listener) {
        negativeListener = listener;
        negativeText = buttonText;
    }

    public void setPostiveButton(String buttonText, DialogInterface.OnClickListener listener) {
        postiveListener = listener;
        postiveText = buttonText;
    }
}
