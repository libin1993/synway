package com.doit.net.fragment;

import com.doit.net.View.ClearHistoryTimeDialog;
import com.doit.net.activity.CustomFcnActivity;
import com.doit.net.activity.DeviceParamActivity;
import com.doit.net.activity.HistoryListActivity;
import com.doit.net.activity.JustForTest;
import com.doit.net.View.LicenceDialog;
import com.doit.net.activity.SystemSettingActivity;
import com.doit.net.activity.UserManageActivity;
import com.doit.net.activity.WhitelistManagerActivity;
import com.doit.net.activity.BlackBoxActivity;
import com.doit.net.Model.VersionManage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.doit.net.base.BaseFragment;
import com.doit.net.Event.IHandlerFinish;
import com.doit.net.Protocol.ProtocolManager;
import com.doit.net.Event.UIEventManager;
import com.doit.net.Model.AccountManage;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.PrefManage;
import com.doit.net.Utils.LicenceUtils;
import com.doit.net.Utils.MySweetAlertDialog;
import com.doit.net.Utils.LogUtils;
import com.doit.net.Utils.StringUtils;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.Utils.LSettingItem;
import com.doit.net.ucsi.R;

import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AppFragment extends BaseFragment implements IHandlerFinish {

    private MySweetAlertDialog mProgressDialog;

    @ViewInject(R.id.tvLoginAccount)
    private TextView tvLoginAccount;

    @ViewInject(R.id.btClearUeid)
    private LSettingItem btClearUeid;

    @ViewInject(R.id.tvLocalImsi)
    private LSettingItem tvLocalImsi;

    @ViewInject(R.id.tvSupportVoice)
    private LSettingItem tvSupportVoice;

    @ViewInject(R.id.tvVersion)
    private LSettingItem tvVersion;

    @ViewInject(R.id.btSetWhiteList)
    private LSettingItem btSetWhiteList;

    @ViewInject(R.id.btUserManage)
    private LSettingItem btUserManage;

    @ViewInject(R.id.btBlackBox)
    private LSettingItem btBlackBox;

    @ViewInject(R.id.btn_history_view)
    private LSettingItem historyItem;

    @ViewInject(R.id.btWifiSetting)
    private LSettingItem btWifiSetting;

    @ViewInject(R.id.btDeviceParam)
    private LSettingItem btDeviceParam;


    @ViewInject(R.id.btDeviceFcn)
    private LSettingItem btDeviceFcn;

    @ViewInject(R.id.btDeviceInfoAndUpgrade)
    private LSettingItem btDeviceInfoAndUpgrade;

    @ViewInject(R.id.tvSystemSetting)
    private LSettingItem tvSystemSetting;

    @ViewInject(R.id.btAuthorizeCodeInfo)
    private LSettingItem btAuthorizeCodeInfo;

    @ViewInject(R.id.tvTest)
    private LSettingItem just4Test;

    private ListView lvPackageList;
    private ArrayAdapter upgradePackageAdapter;
    private LinearLayout layoutUpgradePackage;

    private View rootView;
    private String[] playTypes;

    //handler消息
    private final int EXPORT_SUCCESS = 0;
    private final int EXPORT_ERROR = -1;
    private final int UPGRADE_STATUS_RPT = 1;

    public AppFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        if (null != rootView) {
//            ViewGroup parent = (ViewGroup) rootView.getParent();
//            if (null != parent) {
//                parent.removeView(rootView);
//            }
//            return rootView;
//        }

        rootView = inflater.inflate(R.layout.doit_layout_app, container, false);

        UIEventManager.register(UIEventManager.RPT_UPGRADE_STATUS, this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playTypes = getResources().getStringArray(R.array.play_list);

        tvLoginAccount.setText(AccountManage.getCurrentLoginAccount());

        if (VersionManage.isPoliceVer()) {
            btSetWhiteList.setVisibility(View.GONE);
        } else if (VersionManage.isArmyVer()) {
            btSetWhiteList.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
                @Override
                public void click(LSettingItem item) {
                    startActivity(new Intent(getActivity(), WhitelistManagerActivity.class));
                }
            });
        }

        historyItem.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), HistoryListActivity.class));
            }
        });

        btClearUeid.setmOnLSettingItemClick(clearHistoryListener);

        btUserManage.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), UserManageActivity.class));
            }
        });

        if ((VersionManage.isPoliceVer())) {
            btBlackBox.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
                @Override
                public void click(LSettingItem item) {
                    startActivity(new Intent(getActivity(), BlackBoxActivity.class));
                }
            });
        }

        btWifiSetting.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
            }
        });

        tvSystemSetting.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), SystemSettingActivity.class));
            }
        });

        btDeviceParam.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), DeviceParamActivity.class));
            }
        });

        btDeviceFcn.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                if (!CacheManager.checkDevice(getContext()))
                    return;

                startActivity(new Intent(getActivity(), CustomFcnActivity.class));
            }
        });

        btDeviceInfoAndUpgrade.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                showDeviceInfoDialog();
            }
        });

        btAuthorizeCodeInfo.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                if (!CacheManager.checkDevice(getContext()))
                    return;

                if (!TextUtils.isEmpty(LicenceUtils.authorizeCode)) {
                    LicenceDialog licenceDialog = new LicenceDialog(getActivity());
                    licenceDialog.show();
                } else {
                    ToastUtils.showMessage(getActivity(), "获取机器码中，请稍等");
                }

            }
        });

        just4Test.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), JustForTest.class));
            }
        });

        String imsi = getImsi();
        tvLocalImsi.setRightText(imsi);
        tvLocalImsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("Label", imsi);
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
            }
        });

        if (PrefManage.supportPlay) {
            tvSupportVoice.setRightText("支持");
        } else {
            tvSupportVoice.setRightText("不支持");
        }

        tvVersion.setRightText(VersionManage.getVersionName(getContext()));
        initProgressDialog();

        if (AccountManage.getCurrentPerLevel() >= AccountManage.PERMISSION_LEVEL2) {
            btUserManage.setVisibility(View.VISIBLE);
            if (VersionManage.isPoliceVer()) {    //军队版本不使用黑匣子
                btBlackBox.setVisibility(View.VISIBLE);
            }
            btClearUeid.setVisibility(View.VISIBLE);
        }

        if (AccountManage.getCurrentPerLevel() >= AccountManage.PERMISSION_LEVEL3) {
            just4Test.setVisibility(View.VISIBLE);
            tvSystemSetting.setVisibility(View.VISIBLE);
        }
    }

    private void initProgressDialog() {
        mProgressDialog = new MySweetAlertDialog(getContext(), MySweetAlertDialog.PROGRESS_TYPE);
        mProgressDialog.setTitleText("升级包正在加载，请耐心等待...");
        mProgressDialog.setCancelable(false);
    }

    private void showDeviceInfoDialog() {
        if (!CacheManager.checkDevice(getContext()))
            return;

        final View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_device_info, null);
        TextView tvDeviceIP = (TextView) dialogView.findViewById(R.id.tvDeviceIP);
        tvDeviceIP.setText(CacheManager.DEVICE_IP);
        TextView tvHwVersion = (TextView) dialogView.findViewById(R.id.tvHwVersion);
        tvHwVersion.setText(CacheManager.getLteEquipConfig().getHw());
        TextView tvSwVersion = (TextView) dialogView.findViewById(R.id.tvSwVersion);
        tvSwVersion.setText(CacheManager.getLteEquipConfig().getSw());
        Button btDeviceUpgrade = (Button) dialogView.findViewById(R.id.btDeviceUpgrade);
        btDeviceUpgrade.setOnClickListener(upgradeListner);
        lvPackageList = (ListView) dialogView.findViewById(R.id.lvPackageList);
        layoutUpgradePackage = (LinearLayout) dialogView.findViewById(R.id.layoutUpgradePackage);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    @SuppressLint("MissingPermission")
    private String getImsi() {
        TelephonyManager telManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return StringUtils.defaultIfBlank(telManager.getSubscriberId(), getString(R.string.no_sim_card));
    }

    @SuppressLint("MissingPermission")
    private String getImei() {
        TelephonyManager telManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return StringUtils.defaultIfBlank(telManager.getDeviceId(), getString(R.string.no));
    }

    LSettingItem.OnLSettingItemClick clearHistoryListener = new LSettingItem.OnLSettingItemClick() {
        @Override
        public void click(LSettingItem item) {
            ClearHistoryTimeDialog clearHistoryTimeDialog = new ClearHistoryTimeDialog(getActivity());
            clearHistoryTimeDialog.show();
        }
    };


    private String getPackageMD5(String FilePath) {
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            File f = new File(FilePath);
            FileInputStream fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bi.toString(16);
    }

    View.OnClickListener upgradeListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String FTP_SERVER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/4GHotspot";
            final String UPGRADE_PACKAGE_PATH = "/upgrade/";

            File file = new File(FTP_SERVER_PATH + UPGRADE_PACKAGE_PATH);
            if (!file.exists()) {
                ToastUtils.showMessageLong(getContext(), "未找到升级包，请确认已将升级包放在\"手机存储/4GHotspot/upgrade\"目录下");
                return;
            }

            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                ToastUtils.showMessageLong(getContext(), "未找到升级包，请确认已将升级包放在\"手机存储/4GHotspot/upgrade\"目录下");
                return;
            }

            List<String> fileList = new ArrayList<>();
            String tmpFileName = "";
            for (int i = 0; i < files.length; i++) {
                tmpFileName = files[i].getName();
                //UtilBaseLog.printLog("获取升级包：" + tmpFileName);
                if (tmpFileName.endsWith(".tgz"))
                    fileList.add(tmpFileName);
            }
            if (fileList.size() == 0) {
                ToastUtils.showMessageLong(getContext(), "文件错误，升级包必须是以\".tgz\"为后缀的文件");
                return;
            }

            layoutUpgradePackage.setVisibility(View.VISIBLE);
            upgradePackageAdapter = new ArrayAdapter<String>(getContext(), R.layout.comman_listview_text, fileList);
            lvPackageList.setAdapter(upgradePackageAdapter);
            lvPackageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String choosePackage = fileList.get(position);
                    LogUtils.log("选择升级包：" + choosePackage);

                    new MySweetAlertDialog(getContext(), MySweetAlertDialog.WARNING_TYPE)
                            .setTitleText("提示")
                            .setContentText("选择的升级包为：" + choosePackage + ", 确定升级吗？")
                            .setCancelText(getContext().getString(R.string.cancel))
                            .setConfirmText(getContext().getString(R.string.sure))
                            .showCancelButton(true)
                            .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(MySweetAlertDialog sweetAlertDialog) {
                                    String md5 = getPackageMD5(FTP_SERVER_PATH + UPGRADE_PACKAGE_PATH + choosePackage);
                                    if ("".equals(md5)) {
                                        ToastUtils.showMessage(getContext(), "文件校验失败，升级取消！");
                                        sweetAlertDialog.dismiss();
                                    } else {
                                        LogUtils.log("MD5：" + md5);
                                        String command = UPGRADE_PACKAGE_PATH + choosePackage + "#" + md5;
                                        LogUtils.log(command);
                                        ProtocolManager.systemUpgrade(command);
                                        mProgressDialog.show();
                                        sweetAlertDialog.dismiss();
                                    }
                                }
                            }).show();

                }
            });
        }
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == EXPORT_SUCCESS) {
                new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("导出成功")
                        .setContentText("文件导出在：" + msg.obj)
                        .show();
            } else if (msg.what == EXPORT_ERROR) {
                new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("导出失败")
                        .setContentText("失败原因：" + msg.obj)
                        .show();
            } else if (msg.what == UPGRADE_STATUS_RPT) {
                if (mProgressDialog != null)
                    mProgressDialog.dismiss();
            }
        }
    };

    private void createExportError(String obj) {
        Message msg = new Message();
        msg.what = UPGRADE_STATUS_RPT;
        msg.obj = obj;
        mHandler.sendMessage(msg);
    }

    @Override
    public void handlerFinish(String key) {
        if (key.equals(UIEventManager.RPT_UPGRADE_STATUS)) {
            Message msg = new Message();
            msg.what = UPGRADE_STATUS_RPT;
            mHandler.sendMessage(msg);
        }
    }
}
