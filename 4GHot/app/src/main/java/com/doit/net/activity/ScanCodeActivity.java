package com.doit.net.activity;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.doit.net.base.BaseActivity;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Utils.LogUtils;
import com.doit.net.Utils.ScreenUtils;
import com.doit.net.ucsi.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bertsir.zbar.CameraConfiguration;
import cn.bertsir.zbar.CameraManager;
import cn.bertsir.zbar.CameraPreview;
import cn.bertsir.zbar.Qr.Symbol;
import cn.bertsir.zbar.QrConfig;
import cn.bertsir.zbar.ScanCallback;
import cn.bertsir.zbar.view.ScanView;


/**
 * Author：Libin on 2020/5/19 14:28
 * Email：1993911441@qq.com
 * Describe：扫码
 */
public class ScanCodeActivity extends BaseActivity implements ScanCallback {
    @BindView(R.id.cp_scan)
    CameraPreview cpScan;
    @BindView(R.id.scan_view)
    ScanView scanView;
    @BindView(R.id.iv_scan_back)
    ImageView ivScanBack;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Symbol.scanType = QrConfig.TYPE_CUSTOM;
        Symbol.doubleEngine = true;
        setContentView(R.layout.activity_scan_code);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cpScan != null) {
            cpScan.setScanCallback(this);
            cpScan.start();

            try {
                //CameraPreview设置宽高
                CameraManager cameraManager = cpScan.getmCameraManager();
                Camera camera = cameraManager.getmCamera();
                Camera.Parameters parameters = camera.getParameters();
                Point screenResolutionForCamera = new Point();
                screenResolutionForCamera.x =  ScreenUtils.getInstance().getScreenHeight(this);
                screenResolutionForCamera.y =  ScreenUtils.getInstance().getScreenWidth(this);
                Point bestPreviewSizeValue = CameraConfiguration.findBestPreviewSizeValue(parameters, screenResolutionForCamera);
                ViewGroup.LayoutParams layoutParams = cpScan.getLayoutParams();
                layoutParams.width = ScreenUtils.getInstance().getScreenWidth(this);
                layoutParams.height = ScreenUtils.getInstance().getScreenWidth(this) * bestPreviewSizeValue.x / bestPreviewSizeValue.y;
                LogUtils.log("屏幕："+layoutParams.width+","+layoutParams.height);
                cpScan.setLayoutParams(layoutParams);
            }catch (Exception ignored){
                LogUtils.log(ignored.getMessage());
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cpScan != null) {
            cpScan.stop();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (cpScan != null) {
            cpScan.setFlash(false);
            cpScan.stop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (cpScan != null) {
            cpScan.setFlash(false);
            cpScan.stop();
        }

        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
        vibrator.cancel();
    }



    @Override
    public void onScanResult(cn.bertsir.zbar.Qr.ScanResult result) {
        LogUtils.log("扫描结果"+result.getContent());
        EventAdapter.call(EventAdapter.SCAN_CODE,result.getContent());
        vibrate();
        finish();

    }



    @OnClick(R.id.iv_scan_back)
    public void onViewClicked() {
        finish();
    }
}
