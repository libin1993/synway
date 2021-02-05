package com.doit.net.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.doit.net.bean.LteChannelCfg;
import com.doit.net.protocol.ProtocolManager;
import com.doit.net.model.CacheManager;
import com.doit.net.utils.FormatUtils;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.ucsi.R;
import com.doit.net.utils.ToastUtils;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wiker on 2016/4/29.
 */
public class SystemSetupDialog extends Dialog {
    private View mView;

    private List<String> bandwidthList = new ArrayList<>();
    private ArrayAdapter bandwidthAdapter = null;

    private List<String> SyncWayList = new ArrayList<>();
    private ArrayAdapter syncWayAdapter = null;

    private List<String> listSyncChannel = new ArrayList<>();
    private ArrayAdapter SyncChannelAdapter = null;

    private List<String> listSyncCarrier = new ArrayList<>();
    private ArrayAdapter SyncCarrierAdapter = null;

    private final String SYNC_AUTO = "智能同步";
    private final String SYNC_GPS = "GPS同步";
    private final String SYNC_AIR = "空口同步";
    private final String NO_SYNC = "自由同步";

    private final int SYNC_AUTO_IDX = 0;
    private final int SYNC_GPS_IDX = 1;
    private final int SYNC_AIR_IDX = 2;
    private final int NO_SYNC_IDX = 3;
    private Context context;


    public SystemSetupDialog(Context context) {
        super(context,R.style.Theme_dialog);
        this.context = context;
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        x.view().inject(this,mView);

        if(!CacheManager.isDeviceOk()){
            return;
        }

        gps.setText(CacheManager.getCellConfig().getGpsOffset());
        pci.setText(CacheManager.getCellConfig().getPci());
        etTacUpdatePeriod.setText(CacheManager.getCellConfig().getTacTimer());

        //同步方式
        SyncWayList.add(SYNC_AUTO);
        SyncWayList.add(SYNC_GPS);
        SyncWayList.add(SYNC_AIR);
        SyncWayList.add(NO_SYNC);
        syncWayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, SyncWayList);
        syncWayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSyncWay.setAdapter(syncWayAdapter);
        spSyncWay.setSelection(getSyncWayposition(),true);
        spSyncWay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equals(SYNC_AIR)){
                    if (CacheManager.getCellConfig().getSync().contains("air") && !CacheManager.getCellConfig().getSync().contains("auto")) {
                        showAirSyncOption(CacheManager.getCellConfig().getSync().split(",")[1],
                                CacheManager.getCellConfig().getSync().split(",")[2],
                                CacheManager.getCellConfig().getSync().split(",")[3]);
                    }else{
                        showAirSyncOption("", "", "");
                    }
                }else{
                    layoutAirSyncPCI.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (CacheManager.getCellConfig().getSync().contains("air") && !CacheManager.getCellConfig().getSync().contains("auto")){
            showAirSyncOption(CacheManager.getCellConfig().getSync().split(",")[1],
                    CacheManager.getCellConfig().getSync().split(",")[2],
                    CacheManager.getCellConfig().getSync().split(",")[3]);
        }else{
            layoutAirSyncPCI.setVisibility(View.GONE);
        }
    }

    private void showAirSyncOption(String channelIdx, String pci, String carrierIdx) {
        listSyncChannel.clear();
        for (LteChannelCfg channel : CacheManager.getChannels()) {
            listSyncChannel.add(channel.getIdx());
        }
        SyncChannelAdapter= new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, listSyncChannel);
        SyncChannelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSyncChannel.setAdapter(SyncChannelAdapter);
        spSyncChannel.setSelection(getSyncChannelPosition(channelIdx),true);

        listSyncCarrier.clear();
        for (int i = 1; i < 4; i++) {
            listSyncCarrier.add(String.valueOf(i));
        }
        SyncCarrierAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, listSyncCarrier);
        SyncCarrierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSyncCarrier.setAdapter(SyncCarrierAdapter);
        spSyncCarrier.setSelection(getSynCarrierPosition(carrierIdx),true);

        layoutAirSyncPCI.setVisibility(View.VISIBLE);

        etSyncPCI.setText(pci);
    }

    private int getSyncChannelPosition(String syncChannel) {
        if ("".equals(syncChannel))
            return 0;

        for (int i = 0; i < CacheManager.getChannels().size(); i++) {
            if (CacheManager.getChannels().get(i).getIdx().equals(syncChannel)){
                return i;
            }
        }

        return 0;
    }

    private int getSyncWayposition() {
        if (CacheManager.getCellConfig().getSync().contains("air") && CacheManager.getCellConfig().getSync().contains("auto")) {
            return SYNC_AUTO_IDX;
        }else if (CacheManager.getCellConfig().getSync().contains("gps")){
            return SYNC_GPS_IDX;
        }else if (CacheManager.getCellConfig().getSync().contains("air") && !CacheManager.getCellConfig().getSync().contains("auto")){
            return SYNC_AIR_IDX;
        }if (CacheManager.getCellConfig().getSync().contains("no")){
            return NO_SYNC_IDX;
        }

        return 0;
    }

    private int getSynCarrierPosition(String carrierIdx) {
        if ("".equals(carrierIdx))
            return 0;
        else
            return Integer.parseInt(carrierIdx);
    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.doit_layout_system_setup, null);
    }

    @Event(value = R.id.button_cancel)
    private void cancelClick(View v){
        dismiss();
    }

    @Event(value = R.id.button_save)
    private void saveClick(View v){
        try {
            if(!CacheManager.checkDevice(getContext())){
                return;
            }

            String tddPci = pci.getText().toString();
            String gpsOffset = gps.getText().toString();
            String tacPeriod = etTacUpdatePeriod.getText().toString();


            if (!TextUtils.isEmpty(gpsOffset)){
                if (!FormatUtils.getInstance().gpsRange(gpsOffset)){
                    ToastUtils.showMessage("GPS偏移格式有误");
                    return;
                }

            }

            if (!TextUtils.isEmpty(tddPci)){
                if (!FormatUtils.getInstance().pciRange(tddPci)){
                    ToastUtils.showMessage("PCI格式有误");
                    return;
                }
            }



            new MySweetAlertDialog(getContext(), MySweetAlertDialog.WARNING_TYPE)
                    .setTitleText("小区配置")
                    .setContentText("修改后设备将会重启，是否要修改？")
                    .setCancelText(getContext().getString(R.string.cancel))
                    .setConfirmText(getContext().getString(R.string.sure))
                    .showCancelButton(true)
                    .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {

                        @Override
                        public void onClick(MySweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();


                            String sync = spSyncWay.getSelectedItem().toString();
                            if (sync.equals(SYNC_AUTO)){
                                sync = "air,auto";
                            }else if (sync.equals(SYNC_AIR)){
                                if ("".equals(etSyncPCI.getText().toString())){
                                    new MySweetAlertDialog(getContext(), MySweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("参数错误")
                                            .setContentText("未输入锁定的PCI")
                                            .show();
                                    return;
                                }

                                sync = "air";
                                sync += ",";
                                sync += spSyncChannel.getSelectedItem().toString();
                                sync += ",";
                                sync += etSyncPCI.getText().toString();
                                sync += ",";
                                sync += String.valueOf(Integer.parseInt(spSyncCarrier.getSelectedItem().toString())-1);
                            }else if(sync.equals(SYNC_GPS)){
                                sync = "gps";
                            }else if (sync.equals(NO_SYNC)){
                                sync = "no";
                            }

                            ProtocolManager.setCellConfig(gpsOffset.equals(CacheManager.getCellConfig().getGpsOffset())?"":gpsOffset,
                                    tddPci.equals(CacheManager.getCellConfig().getPci())?"":tddPci,
                                    tacPeriod.equals(CacheManager.getCellConfig().getTacTimer())?"":tacPeriod,
                                    sync.equals(CacheManager.getCellConfig().getSync())?"":sync);

                            CacheManager.getCellConfig().setGpsOffset(gpsOffset);
                            CacheManager.getCellConfig().setPci(tddPci);
                            CacheManager.getCellConfig().setTacTimer(tacPeriod);
                            CacheManager.getCellConfig().setSync(sync);

                            dismiss();

                        }
                    })
                    .show();
        } catch (NumberFormatException e) {
            new MySweetAlertDialog(getContext(), MySweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getContext().getString(R.string.tip_16))
                    .show();
        } catch (Exception e){
//            new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
//                    .setTitleText(e.getMessage())
//                    .show();
        }
    }

    @ViewInject(value = R.id.id_pci_tdd)
    private EditText pci;

    @ViewInject(value = R.id.id_gps)
    private EditText gps;

    @ViewInject(value = R.id.etTacUpdatePeriod)
    private EditText etTacUpdatePeriod;

    @ViewInject(value = R.id.button_cancel)
    private Button button_cancel;

    @ViewInject(value = R.id.button_save)
    private Button button_save;

    @ViewInject(value = R.id.spSyncWay)
    private Spinner spSyncWay;

    @ViewInject(value = R.id.layoutAirSyncPCI)
    private LinearLayout layoutAirSyncPCI;

    @ViewInject(value = R.id.etSyncPCI)
    private EditText etSyncPCI;

    @ViewInject(value = R.id.spSyncChannel)
    private Spinner spSyncChannel;

    @ViewInject(value = R.id.spSyncCarrier)
    private Spinner spSyncCarrier;
}
