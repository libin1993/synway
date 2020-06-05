package com.doit.net.Model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.doit.net.Event.EventAdapter;
import com.doit.net.Event.UIEventManager;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.Utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.SocketFactory;

import okhttp3.Dns;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by Zxc on 2019/6/26.
 */
//imsi转手机号
public class ImsiMsisdnConvert {
    private static final String TEST_USERNAME = "TEST0001";
    private static final String TEST_PASSWORD = "12345678";
    public static final String DEFAULT_SERVER_ADDRESS = "http://101.201.213.210:8081";
    public static String serverAddress = DEFAULT_SERVER_ADDRESS;
    private static final String GET_TOKEN_PATH = "/api/login";
    private static final String REQUEST_CONVERT_PATH = "/api/postImsiOrMobile";
    private static final String QUERY_RESULT_PATH = "/api/getImsiOrMobile";
    private static final String KEY_IMSI = "imsi";
    private static final String KEY_MSISDN = "msisdn";
    private static final String KEY_CMDTYPE = "cmdtype";
    private static final String KEY_MSTYPE = "mstype";

    private static String currentUsername = "";
    private static String currentpassword = "";

    private static int restConvertTimes = -1;

    public static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");


    private static HashMap<String, Integer> mapConvertTimes = new HashMap<>();
    private static String token = "";

    /**************************** 获取令牌 *****************************/
    private static String getToken(OkHttpClient client, String server, String username, String password) {
        String result = "1111";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
            RequestBody requestBody = RequestBody.create(JSON,jsonObject.toString());
            Request request = new Request.Builder()
                    .url(serverAddress+GET_TOKEN_PATH)
                    .post(requestBody)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                String body = response.body().string();
                LogUtils.log("response.code()==" + response.code() + ", response.body().string()==" + body);
                JSONObject jsonRecv = new JSONObject(body);
                int resultCode = jsonRecv.getInt("code");
                if (resultCode == 1) {
                    token = parseToken(body);
                    currentUsername = username;
                    currentpassword = password;
                    serverAddress = server;
                    LogUtils.log("获取到token:" + token);
                    result = "认证成功，可以进行号码翻译";
                }else{
                    LogUtils.log("获取token失败:" + jsonRecv.getString("info"));
                    result = "认证失败，原因："+jsonRecv.getString("info");
                }
            }else{
                result = "认证失败：网络故障，请确保网络畅通！";
                LogUtils.log("获取数据失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            result = "认证失败：网络故障，请确保网络畅通！";
            e.printStackTrace();
        }

        return result;
    }

    private static boolean isGetTokenSuccess(String json) {
        boolean res = false;
        try {
            JSONObject jsonObj = new JSONObject(json);
            if (jsonObj.getInt("code") == 1)
                res = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static String getToken(){
        return token;
    }

    /**************************** IMSI转手机号 *****************************/
    private static String imsiToMsisdn(final Context context, OkHttpClient client, final String imsi){
        String result = "";
        //OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        JSONObject jsonToPost = new JSONObject();
        try {
            jsonToPost.put(KEY_IMSI, imsi);
            jsonToPost.put(KEY_MSISDN,"");
            jsonToPost.put(KEY_CMDTYPE,"1");
            jsonToPost.put(KEY_MSTYPE,"1");
            RequestBody requestBody = RequestBody.create(JSON,jsonToPost.toString());
            Request request = new Request.Builder()
                    .url(serverAddress +REQUEST_CONVERT_PATH)
                    .post(requestBody)
                    .addHeader("token", token)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                String body = response.body().string();
                LogUtils.log("response.code()=="+response.code()+", response.body().string()=="+body);
                int restTimes;
                String expireDate;
                JSONObject jsonRecv = new JSONObject(body);
                int resultCode = jsonRecv.getInt("code");
                if (resultCode == 200) {
                    JSONObject resultJsonObject = jsonRecv.getJSONObject("user");
                    restTimes = resultJsonObject.getInt("times");
                    expireDate = resultJsonObject.getString("expireDate");
                    result = "翻译请求已成功发出，请等待翻译结果";
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            queryImsiConvertMsisdnRes(context,imsi);
                        }
                    },3000);
                    restConvertTimes = restTimes;
                    LogUtils.log("剩余次数："+restTimes+"，到期时间："+expireDate);  //将到期时间和次数存入pref
                } else {
                    if (resultCode == 77){
                        restConvertTimes = 0;
                    }

                    result = "请求翻译IMSI失败，原因："+jsonRecv.getString("info");
                    LogUtils.log("请求转换IMSI失败，原因："+jsonRecv.getString("info"));
                }
            }else{
                result = "翻译请求失败：网络故障，请确保网络畅通！";
                LogUtils.log("获取数据失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            result = "翻译请求失败：网络故障，请确保网络畅通！";
            e.printStackTrace();
        }

        return result;
    }

    /**************************** 请求IMSI转手机 *****************************/
    public static boolean requestConvertImsiToMsisdn(final Context context, final String imsi){
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        final Object[] objs = new Object[1];  //用于匿名内部类值传出
        /* 1.需要android 系统5.0以上
           2.开发者选项里打开网络数据常开
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && connectivityManager != null) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
            NetworkRequest networkRequest = builder.build();
            connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onAvailable(final Network network) {
                    super.onAvailable(network);
                    //这个函数是异步的，所以暂时只能把操作都放在这了。
                    SocketFactory socketFactory = network.getSocketFactory();
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            //.proxy(Proxy.NO_PROXY)
                            .socketFactory(socketFactory)
                            .dns(new Dns() {
                                @Override
                                public List<InetAddress> lookup(@NonNull String hostname) {
                                    List<InetAddress> list = new ArrayList<>();
                                    try {
                                        list =  Arrays.asList(network.getAllByName(hostname));
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }

                                    return list;
                                }
                            }).build();

                    if (okHttpClient == null){
                        objs[0] = "网络故障，请检查网络!";
                    }else{
                        LogUtils.log("okHttpClient ok");
                        objs[0] = imsiToMsisdn(context, okHttpClient, imsi);
                    }

                    if (!objs[0].equals(""))
                        ToastUtils.showMessageLong(context, (String)objs[0]);
                }
            });

            return true;
        }else{
            ToastUtils.showMessageLong(context, "认证失败：获取网络失败");
        }

        return false;
    }

    /**************************** 手机号转IMSI *****************************/
    private static String msisdnToImsi(final Context context, OkHttpClient client, final String msisdn){
        String result = "";
        //OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        JSONObject jsonToPost = new JSONObject();
        try {
            jsonToPost.put(KEY_IMSI, "");
            jsonToPost.put(KEY_MSISDN,msisdn);
            jsonToPost.put(KEY_CMDTYPE,"0");
            jsonToPost.put(KEY_MSTYPE,"0");
            RequestBody requestBody = RequestBody.create(JSON,jsonToPost.toString());
            Request request = new Request.Builder()
                    .url(serverAddress +REQUEST_CONVERT_PATH)
                    .post(requestBody)
                    .addHeader("token", token)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                String body = response.body().string();
                LogUtils.log("response.code()=="+response.code()+", response.body().string()=="+body);
                int restTimes;
                String expireDate;
                JSONObject jsonRecv = new JSONObject(body);
                int resultCode = jsonRecv.getInt("code");
                if (resultCode == 200) {
                    JSONObject resultJsonObject = jsonRecv.getJSONObject("user");
                    restTimes = resultJsonObject.getInt("times");
                    expireDate = resultJsonObject.getString("expireDate");
                    result = "翻译请求已成功发出，请等待翻译结果";
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            queryMsisdnConvertImsiRes(context,msisdn);
                        }
                    },3000);
                    restConvertTimes = restTimes;
                    LogUtils.log("剩余次数："+restTimes+"，到期时间："+expireDate);
                } else {
                    if (resultCode == 77){
                        //删掉手机白名单
                        restConvertTimes = 0;
                    }

                    result = "请求翻译手机号失败，原因："+jsonRecv.getString("info");
                    LogUtils.log("请求转换手机号失败，原因："+jsonRecv.getString("info"));
                }

            }else{
                result = "翻译请求失败：网络故障，请确保网络畅通！";
                LogUtils.log("获取数据失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            result = "翻译请求失败：网络故障，请确保网络畅通！";
            e.printStackTrace();
        }

        return result;
    }

    /**************************** 请求转手机号转IMSI *****************************/
    public static boolean requestConvertMsisdnToImsi(final Context context, final String msisdn){
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        final Object[] objs = new Object[1];  //用于匿名内部类值传出
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && connectivityManager != null) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
            NetworkRequest networkRequest = builder.build();
            connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onAvailable(final Network network) {
                    super.onAvailable(network);
                    //这个函数是异步的，所以暂时只能把操作都放在这了。
                    SocketFactory socketFactory = network.getSocketFactory();
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            //.proxy(Proxy.NO_PROXY)
                            .socketFactory(socketFactory)
                            .dns(new Dns() {
                                @Override
                                public List<InetAddress> lookup(@NonNull String hostname) {
                                    List<InetAddress> list = new ArrayList<>();
                                    try {
                                        list =  Arrays.asList(network.getAllByName(hostname));
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }

                                    return list;
                                }
                            }).build();

                    if (okHttpClient == null){
                        objs[0] = "网络故障，请检查网络!";
                    }else{
                        LogUtils.log("okHttpClient ok");
                        objs[0] = msisdnToImsi(context, okHttpClient, msisdn);
                    }
                    ToastUtils.showMessageLong(context, (String)objs[0]);
                }
            });

            return true;
        }else{
            ToastUtils.showMessageLong(context, "认证失败：获取网络失败");
        }

        return false;
    }

    /**************************** 查询IMSI转手机号结果 *****************************/
    private static String queryImsiConvert(final Context context, OkHttpClient client, final String convertImsi){
        String result = "";
        //OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        JSONObject jsonToPost = new JSONObject();
        try {
            jsonToPost.put(KEY_IMSI,convertImsi);
            jsonToPost.put(KEY_MSISDN,"");
            jsonToPost.put(KEY_CMDTYPE,"1");
            jsonToPost.put(KEY_MSTYPE,"1");
            RequestBody requestBody = RequestBody.create(JSON,jsonToPost.toString());
            Request request = new Request.Builder()
                    .url(serverAddress +QUERY_RESULT_PATH)
                    .post(requestBody)
                    .addHeader("token", token)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                String body = response.body().string();
                LogUtils.log("response.code()=="+response.code()+",response.body().string()=="+body);

                String msisdn;
                String imsi;
                JSONObject jsonObject = new JSONObject(body);
                int resultCode = jsonObject.getInt("code");
                if (resultCode == 200) {
                    JSONObject resultJsonObject = jsonObject.getJSONObject("data");
                    msisdn = resultJsonObject.getString("msisdn");
                    imsi = resultJsonObject.getString("imsi");
                    result = "获取手机号成功："+msisdn+"/"+imsi;
                    LogUtils.log("获取手机号成功："+msisdn+"/"+imsi);
                    updateMsisdnToDB(imsi, msisdn);
                    UIEventManager.call(UIEventManager.KEY_REFRESH_REALTIME_UEID_LIST);
                    UIEventManager.call(UIEventManager.KEY_RESEARCH_HISTORY_LIST);
                }else if (resultCode == 404){
                    LogUtils.log("还在转换中，2秒后重试！");
                    if (mapConvertTimes.containsKey(convertImsi)){
                        int times = mapConvertTimes.get(convertImsi);
                        if (times >= 30){
                            LogUtils.log("尝试次数已到，终止翻译请求。");
                            mapConvertTimes.remove(convertImsi);
                            return "尝试次数已到，终止翻译请求";
                        }
                        mapConvertTimes.put(convertImsi, times+1);
                    }else{
                        mapConvertTimes.put(convertImsi, 1);
                    }

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            queryImsiConvertMsisdnRes(context, convertImsi);
                        }
                    },2000);
                } else {
                    result = "请求转换IMSI失败，原因："+jsonObject.getString("info");
                    LogUtils.log("请求转换IMSI失败，原因："+jsonObject.getString("info"));
                }

            }else{
                result = "网络故障，请确保网络畅通！";
                LogUtils.log("查询转换结果失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            result = "网络故障，请确保网络畅通！";
            e.printStackTrace();
        }

        return result;
    }

    /**************************** 请求查询IMSI转手机号结果 *****************************/
    public static boolean queryImsiConvertMsisdnRes(final Context context, final String convertImsi){
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        final Object[] objs = new Object[1];  //用于匿名内部类值传出
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && connectivityManager != null) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
            NetworkRequest networkRequest = builder.build();
            connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onAvailable(final Network network) {
                    super.onAvailable(network);
                    //这个函数是异步的，所以暂时只能把操作都放在这了。
                    SocketFactory socketFactory = network.getSocketFactory();
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            //.proxy(Proxy.NO_PROXY)
                            .socketFactory(socketFactory)
                            .dns(new Dns() {
                                @Override
                                public List<InetAddress> lookup(@NonNull String hostname) {
                                    List<InetAddress> list = new ArrayList<>();
                                    try {
                                        list =  Arrays.asList(network.getAllByName(hostname));
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }

                                    return list;
                                }
                            }).build();

                    if (okHttpClient == null){
                        objs[0] = "网络故障，请检查网络!";
                    }else{
                        LogUtils.log("okHttpClient ok");
                        objs[0] = queryImsiConvert(context, okHttpClient, convertImsi);
                    }
                    ToastUtils.showMessageLong(context, (String)objs[0]);
                }
            });

            return true;
        }else{
            ToastUtils.showMessageLong(context, "认证失败：获取网络失败");
        }

        return false;
    }

    /**************************** 查询手机号转IMSI结果 *****************************/
    private static String queryMsisdnConvert(final Context context, OkHttpClient client, final String convertMsisdn){
        String result = "";
        //OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        JSONObject jsonToPost = new JSONObject();
        try {
            jsonToPost.put(KEY_IMSI, "");
            jsonToPost.put(KEY_MSISDN,convertMsisdn);
            jsonToPost.put(KEY_CMDTYPE,"0");
            jsonToPost.put(KEY_MSTYPE,"0");
            RequestBody requestBody = RequestBody.create(JSON,jsonToPost.toString());
            Request request = new Request.Builder()
                    .url(serverAddress +QUERY_RESULT_PATH)
                    .post(requestBody)
                    .addHeader("token", token)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                String body = response.body().string();
                LogUtils.log("response.code()=="+response.code()+",response.body().string()=="+body);

                String msisdn;
                String imsi;
                JSONObject jsonObject = new JSONObject(body);
                int resultCode = jsonObject.getInt("code");
                if (resultCode == 200) {
                    JSONObject resultJsonObject = jsonObject.getJSONObject("data");
                    msisdn = resultJsonObject.getString("msisdn");
                    imsi = resultJsonObject.getString("imsi");
                    result = "获取IMSI成功："+msisdn+"/"+imsi;
                    LogUtils.log("获取IMSI成功："+msisdn+"/"+imsi);
                    updatWhitelistToDB(imsi, msisdn);
                    EventAdapter.call(EventAdapter.UPDATE_WHITELIST);
                }else if (resultCode == 404){
                    LogUtils.log("还在转换中，2秒后重试！");
                    if (mapConvertTimes.containsKey(convertMsisdn)){
                        int times = mapConvertTimes.get(convertMsisdn);
                        if (times >= 30){
                            LogUtils.log("尝试次数已到，终止翻译请求。");
                            mapConvertTimes.remove(convertMsisdn);
                            return "尝试次数已到，终止翻译请求";
                        }
                        mapConvertTimes.put(convertMsisdn, times+1);
                    }else{
                        mapConvertTimes.put(convertMsisdn, 1);
                    }

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            queryMsisdnConvertImsiRes(context, convertMsisdn);
                        }
                    },2000);
                } else {
                    result = "请求转换IMSI失败，原因："+jsonObject.getString("info");
                    LogUtils.log("请求转换IMSI失败，原因："+jsonObject.getString("info"));
                }

            }else{
                result = "网络故障，请确保网络畅通！";
                LogUtils.log("查询转换结果失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            result = "网络故障，请确保网络畅通！";
            e.printStackTrace();
        }

        return result;
    }

    /**************************** 请求查询手机号转IMSI结果 *****************************/
    public static boolean queryMsisdnConvertImsiRes(final Context context, final String Msisdn){
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        final Object[] objs = new Object[1];  //用于匿名内部类值传出
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && connectivityManager != null) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
            NetworkRequest networkRequest = builder.build();
            connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onAvailable(final Network network) {
                    super.onAvailable(network);
                    //这个函数是异步的，所以暂时只能把操作都放在这了。
                    SocketFactory socketFactory = network.getSocketFactory();
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            //.proxy(Proxy.NO_PROXY)
                            .socketFactory(socketFactory)
                            .dns(new Dns() {
                                @Override
                                public List<InetAddress> lookup(@NonNull String hostname) {
                                    List<InetAddress> list = new ArrayList<>();
                                    try {
                                        list =  Arrays.asList(network.getAllByName(hostname));
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }

                                    return list;
                                }
                            }).build();

                    if (okHttpClient == null){
                        objs[0] = "网络故障，请检查网络!";
                    }else{
                        LogUtils.log("okHttpClient ok");
                        objs[0] = queryMsisdnConvert(context, okHttpClient, Msisdn);
                    }
                    if (!objs[0].equals(""))
                        ToastUtils.showMessageLong(context, (String)objs[0]);
                }
            });

            return true;
        }else{
            ToastUtils.showMessageLong(context, "认证失败：获取网络失败");
        }

        return false;
    }

    private static void updateMsisdnToDB(String imsi, String msisdn) {
        DbManager db = UCSIDBManager.getDbManager();
        List<DBUeidInfo> listDBUeidInfo = new ArrayList<>();
        try {
            listDBUeidInfo = db.selector(DBUeidInfo.class)
                    .where("imsi", "=", imsi)
                    .findAll();

            if (listDBUeidInfo == null || listDBUeidInfo.size() == 0){return;}

            for (int i = 0; i < listDBUeidInfo.size(); i++) {
                listDBUeidInfo.get(i).setMsisdn(msisdn);
                db.update(listDBUeidInfo.get(i), "msisdn");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private static void updatWhitelistToDB(String imsi, String msisdn) {
        DbManager db = UCSIDBManager.getDbManager();
        List<WhiteListInfo> listDBUeidInfo = new ArrayList<>();
        try {
            listDBUeidInfo = db.selector(WhiteListInfo.class)
                    .where("msisdn", "=", msisdn)
                    .findAll();

            if (listDBUeidInfo == null || listDBUeidInfo.size() == 0){return;}

            for (int i = 0; i < listDBUeidInfo.size(); i++) {
                listDBUeidInfo.get(i).setImsi(imsi);
                db.update(listDBUeidInfo.get(i), "imsi");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    private static String parseToken(String json) {
        String res = "";
        try {
            JSONObject result = new JSONObject(json);
             res = result.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static boolean isAuthenticated(){
        return !"".equals(token);
    }

    public static String getMsisdnFromLocal(String imsi) {
        try {
            DBUeidInfo info = UCSIDBManager.getDbManager().selector(DBUeidInfo.class)
                    .where("imsi","=", imsi).findFirst();

            if (info != null){
                return info.getMsisdn();
            }else{
                return "未获取";
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        return "未获取";
    }

    public static int getRestConvertTimes(){
        return restConvertTimes;
    }

    public static String getCurrentUsername(){
        return currentUsername;
    }

    public static String getCurrentpassword(){
        return currentpassword;
    }


    public static boolean authenticate(final Context context, final String server, final String username, final String password){
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        final Object[] objs = new Object[1];  //用于匿名内部类值传出
        /* 1.需要android 系统5.0以上
           2.开发者选项里打开网络数据常开
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && connectivityManager != null) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
            NetworkRequest networkRequest = builder.build();
            connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onAvailable(final Network network) {
                    super.onAvailable(network);
                    //这个函数是异步的，所以暂时只能把操作都放在这了。
                    SocketFactory socketFactory = network.getSocketFactory();
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            //.proxy(Proxy.NO_PROXY)
                            .socketFactory(socketFactory)
                            .dns(new Dns() {
                                @Override
                                public List<InetAddress> lookup(@NonNull String hostname) {
                                    List<InetAddress> list = new ArrayList<>();
                                    try {
                                        list =  Arrays.asList(network.getAllByName(hostname));
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }

                                    return list;
                                }
                            }).build();

                    if (okHttpClient == null){
                        LogUtils.log("create okHttpClient error!");
                        objs[0] = "网络故障，请检查网络!";
                    }else{
                        LogUtils.log("okHttpClient ok");
                        objs[0] = getToken(okHttpClient, server, username,password);
                    }

                    if (!objs[0].equals(""))
                        ToastUtils.showMessageLong(context, (String)objs[0]);
                }
            });

            return true;
        }else{
            ToastUtils.showMessageLong(context, "认证失败：获取网络失败");
        }

        return false;
    }
}
