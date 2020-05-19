package com.synway.authorization;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    EditText etCode;
    Button btnEncrypt;
    TextView tvEncrypt;
    Button btnCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etCode = findViewById(R.id.et_code);
        btnEncrypt = findViewById(R.id.btn_encrypt);
        tvEncrypt = findViewById(R.id.tv_encrypt);
        btnCopy = findViewById(R.id.btn_copy);
        initView();

    }

    private void initView() {
        CipherUtils.init("");
        btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String trim = etCode.getText().toString().trim();
                if (!TextUtils.isEmpty(trim)){
                    try {
                        tvEncrypt.setText(CipherUtils.encrypt(trim));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = tvEncrypt.getText().toString().trim();
                if (!TextUtils.isEmpty(text)){
                    //获取剪贴板管理器：
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", text);
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData);
                }
            }
        });
    }
}
