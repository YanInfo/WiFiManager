package com.kongqw.wifilibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.kongqw.wifilibrary.listener.OnWifiConnectListener;
import com.kongqw.wifilibrary.listener.OnWifiEnabledListener;
import com.kongqw.wifilibrary.listener.OnWifiScanResultsListener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


/**
 * /**
 *
 * @Author: zhangyan
 * @Date: 2019/4/10 9:32
 * WifiManager : https://developer.android.com/reference/android/net/wifi/WifiManager.html
 * WifiConfiguration : https://developer.android.com/reference/android/net/wifi/WifiConfiguration.html
 * ScanResult : https://developer.android.com/reference/android/net/wifi/ScanResult.html
 * WifiInfo : https://developer.android.com/reference/android/net/wifi/WifiInfo.html
 * Wifi管理
 */
public class WiFiManager extends BaseWiFiManager {

    private static final String TAG = "WiFiManager";
    private static WiFiManager mWiFiManager;
    private static CallBackHandler mCallBackHandler = new CallBackHandler();
    private static final int WIFI_STATE_ENABLED = 0;
    private static final int WIFI_STATE_DISABLED = 1;
    private static final int SCAN_RESULTS_UPDATED = 3;
    private static final int WIFI_CONNECT_LOG = 4;
    private static final int WIFI_CONNECT_SUCCESS = 5;
    private static final int WIFI_CONNECT_FAILURE = 6;
    private static final String DEFAULT_AP_PASSWORD = "12345678";

    private WiFiManager(Context context) {
        super(context);
    }

    public static WiFiManager getInstance(Context context) {
        if (null == mWiFiManager) {
            synchronized (WiFiManager.class) {
                if (null == mWiFiManager) {
                    mWiFiManager = new WiFiManager(context);
                }
            }
        }
        return mWiFiManager;
    }

    /**
     * 打开Wifi
     */
    public void openWiFi() {
        if (!isWifiEnabled() && null != mWifiManager) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭Wifi
     */
    public void closeWiFi() {
        if (isWifiEnabled() && null != mWifiManager) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 创建热点，这里只能是7.0或7.0之前的版本
     * 手动设置热点名和密码
     */
    public void createWifiHotspot7() {

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "性感荷官在线发脾气";
        config.preSharedKey = "123456789";
        config.hiddenSSID = true;
        config.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);//开放系统认证
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        //设置加密的方式，这个对应小米手机MIUI
        int indexOfWPA2_PSK = 4;
        //从WifiConfiguration.KeyMgmt数组中查找WPA2_PSK的值
        for (int i = 0; i < WifiConfiguration.KeyMgmt.strings.length; i++) {
            if (WifiConfiguration.KeyMgmt.strings[i].equals("WPA2_PSK")) {
                indexOfWPA2_PSK = i;
                break;
            }
        }
        config.allowedKeyManagement.set(indexOfWPA2_PSK);

        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;

        //通过反射调用设置热点
        try {
            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            boolean enable = (Boolean) method.invoke(mWifiManager, config, true);
            if (enable) {
                Log.i(TAG, "热点已经开启##################################");
            } else {
                Log.i(TAG, "热点开启失败##################################");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 8.0 开启热点方法
     * 这里使用WPA2加密
     *
     * @param ssid
     * @param password
     */
    public void createWifiHotspot8(String ssid, String password) {

        WifiConfiguration wifiConfig = new WifiConfiguration();
        Log.i(TAG, "热点准备开启##################################");

        // 清理配置
        wifiConfig.SSID = new String(ssid);
        wifiConfig.allowedAuthAlgorithms.clear();
        wifiConfig.allowedGroupCiphers.clear();
        wifiConfig.allowedKeyManagement.clear();
        wifiConfig.allowedPairwiseCiphers.clear();
        wifiConfig.allowedProtocols.clear();

        // 如果密码为空或者小于8位数，则使用默认的密码
        if (null != password && password.length() >= 8) {
            wifiConfig.preSharedKey = password;
        } else {
            wifiConfig.preSharedKey = DEFAULT_AP_PASSWORD;
        }
        wifiConfig.hiddenSSID = false;
        wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wifiConfig.allowedKeyManagement.set(4);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfig.status = WifiConfiguration.Status.ENABLED;

    }

    /**
     * 关闭热点
     */
    public void closeWifiHotspot8() {
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(mWifiManager, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 连接到开放网络
     *
     * @param ssid 热点名
     * @return 配置是否成功
     */
    public boolean connectOpenNetwork(@NonNull String ssid) {
        // 获取networkId
        int networkId = setOpenNetwork(ssid);
        if (-1 != networkId) {
            // 保存配置
            boolean isSave = saveConfiguration();
            // 连接网络
            boolean isEnable = enableNetwork(networkId);

            return isSave && isEnable;
        }
        return false;

    }

    /**
     * 连接到WEP网络
     *
     * @param ssid     热点名
     * @param password 密码
     * @return 配置是否成功
     */
    public boolean connectWEPNetwork(@NonNull String ssid, @NonNull String password) {
        // 获取networkId
        int networkId = setWEPNetwork(ssid, password);
        if (-1 != networkId) {
            // 保存配置
            boolean isSave = saveConfiguration();
            // 连接网络
            boolean isEnable = enableNetwork(networkId);

            return isSave && isEnable;
        }
        return false;
    }

    /**
     * 连接到WPA2网络
     *
     * @param ssid     热点名
     * @param password 密码
     * @return 配置是否成功
     */
    public boolean connectWPA2Network(@NonNull String ssid, @NonNull String password) {
        // 获取networkId
        int networkId = setWPA2Network(ssid, password);
        if (-1 != networkId) {
            // 保存配置
            boolean isSave = saveConfiguration();
            // 连接网络
            boolean isEnable = enableNetwork(networkId);

            return isSave && isEnable;
        }
        return false;
    }

    /* *******************************************************************************************/


    /**
     * 广播接收者
     */
    public static class NetworkBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获得WifiManager的实例对象
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            switch (intent.getAction()) {
                //WIFI状态发生变化
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
                        case WifiManager.WIFI_STATE_ENABLING:
                            Log.i(TAG, "onReceive: 正在打开 WIFI...");
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            Log.i(TAG, "onReceive: WIFI 已打开");
                            mCallBackHandler.sendEmptyMessage(WIFI_STATE_ENABLED);
                            break;
                        case WifiManager.WIFI_STATE_DISABLING:
                            Log.i(TAG, "onReceive: 正在关闭 WIFI...");
                            break;
                        case WifiManager.WIFI_STATE_DISABLED:
                            Log.i(TAG, "onReceive: WIFI 已关闭");
                            mCallBackHandler.sendEmptyMessage(WIFI_STATE_DISABLED);
                            break;
                        case WifiManager.WIFI_STATE_UNKNOWN:
                        default:
                            Log.i(TAG, "onReceive: WIFI 状态未知!");
                            break;
                    }
                    break;
                // WIFI扫描完成
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        boolean isUpdated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                        Log.i(TAG, "onReceive: WIFI扫描  " + (isUpdated ? "完成" : "未完成"));
                    } else {
                        Log.i(TAG, "onReceive: WIFI扫描完成");
                    }

                    Message scanResultsMessage = Message.obtain();
                    scanResultsMessage.what = SCAN_RESULTS_UPDATED;
                    scanResultsMessage.obj = wifiManager.getScanResults();
                    mCallBackHandler.sendMessage(scanResultsMessage);
                    break;
                //WIFI网络状态变化通知
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    if (null != wifiInfo && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                        String ssid = wifiInfo.getSSID();
                        Log.i(TAG, "onReceive: 网络连接成功 ssid = " + ssid);
                    }
                    break;
                //WIFI连接状态变化的时候
                case WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION:
                    boolean isConnected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
                    Log.i(TAG, "onReceive: SUPPLICANT_CONNECTION_CHANGE_ACTION  isConnected = " + isConnected);
                    break;
                //wifi连接结果通知   WIFI连接请求状态发生改变
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    // 获取连接状态
                    SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

                    Message logMessage = Message.obtain();
                    logMessage.what = WIFI_CONNECT_LOG;
                    logMessage.obj = supplicantState.toString();
                    logMessage.obj = supplicantState.toString();
                    mCallBackHandler.sendMessage(logMessage);

                    switch (supplicantState) {
                        case INTERFACE_DISABLED: // 接口禁用
                            Log.i(TAG, "onReceive: INTERFACE_DISABLED 接口禁用");
                            break;
                        case DISCONNECTED:// 断开连接
//                            Log.i(TAG, "onReceive: DISCONNECTED:// 断开连接");
//                            break;
                        case INACTIVE: // 不活跃的
                            WifiInfo connectFailureInfo = wifiManager.getConnectionInfo();
                            Log.i(TAG, "onReceive: INACTIVE 不活跃的  connectFailureInfo = " + connectFailureInfo);
                            if (null != connectFailureInfo) {
                                Message wifiConnectFailureMessage = Message.obtain();
                                wifiConnectFailureMessage.what = WIFI_CONNECT_FAILURE;
                                wifiConnectFailureMessage.obj = connectFailureInfo.getSSID();
                                mCallBackHandler.sendMessage(wifiConnectFailureMessage);
                                // 断开连接
                                int networkId = connectFailureInfo.getNetworkId();
                                boolean isDisable = wifiManager.disableNetwork(networkId);
                                boolean isDisconnect = wifiManager.disconnect();
                                Log.i(TAG, "onReceive: 断开连接  =  " + (isDisable && isDisconnect));
                            }
                            break;
                        case SCANNING: // 正在扫描
                            Log.i(TAG, "onReceive: SCANNING 正在扫描");
                            break;
                        case AUTHENTICATING: // 正在验证
                            Log.i(TAG, "onReceive: AUTHENTICATING: // 正在验证");
                            break;
                        case ASSOCIATING: // 正在关联
                            Log.i(TAG, "onReceive: ASSOCIATING: // 正在关联");
                            break;
                        case ASSOCIATED: // 已经关联
                            Log.i(TAG, "onReceive: ASSOCIATED: // 已经关联");
                            break;
                        case FOUR_WAY_HANDSHAKE: // 四次握手
                            Log.i(TAG, "onReceive: FOUR_WAY_HANDSHAKE:");
                            break;
                        case GROUP_HANDSHAKE:  // 组握手
                            Log.i(TAG, "onReceive: GROUP_HANDSHAKE:");
                            break;
                        case COMPLETED: // 完成
                            Log.i(TAG, "onReceive: WIFI_CONNECT_SUCCESS: // 完成");
                            WifiInfo connectSuccessInfo = wifiManager.getConnectionInfo();
                            if (null != connectSuccessInfo) {
                                Message wifiConnectSuccessMessage = Message.obtain();
                                wifiConnectSuccessMessage.what = WIFI_CONNECT_SUCCESS;
                                wifiConnectSuccessMessage.obj = connectSuccessInfo.getSSID();
                                mCallBackHandler.sendMessage(wifiConnectSuccessMessage);
                            }
                            break;
                        case DORMANT: // 休眠
                            Log.i(TAG, "onReceive: DORMANT:");
                            break;
                        case UNINITIALIZED: // 未初始化
                            Log.i(TAG, "onReceive: UNINITIALIZED: // 未初始化");
                            break;
                        case INVALID: // 无效的
                            Log.i(TAG, "onReceive: INVALID: // 无效的");
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }


    private static class CallBackHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WIFI_STATE_ENABLED: // WIFI已经打开
                    if (null != mOnWifiEnabledListener) {
                        mOnWifiEnabledListener.onWifiEnabled(true);
                    }
                    break;
                case WIFI_STATE_DISABLED: // WIFI已经关闭
                    if (null != mOnWifiEnabledListener) {
                        mOnWifiEnabledListener.onWifiEnabled(false);
                    }
                    break;
                case SCAN_RESULTS_UPDATED: // WIFI扫描完成
                    if (null != mOnWifiScanResultsListener) {
                        @SuppressWarnings("unchecked")
                        List<ScanResult> scanResults = (List<ScanResult>) msg.obj;
                        mOnWifiScanResultsListener.onScanResults(scanResults);
                    }
                    break;
                case WIFI_CONNECT_LOG: // WIFI连接完成
                    if (null != mOnWifiConnectListener) {
                        String log = (String) msg.obj;
                        mOnWifiConnectListener.onWiFiConnectLog(log);
                    }
                    break;
                case WIFI_CONNECT_SUCCESS: // WIFI连接完成
                    if (null != mOnWifiConnectListener) {
                        String ssid = (String) msg.obj;
                        mOnWifiConnectListener.onWiFiConnectSuccess(ssid);
                    }
                    break;
                case WIFI_CONNECT_FAILURE: // WIFI连接完成
                    if (null != mOnWifiConnectListener) {
                        String ssid = (String) msg.obj;
                        mOnWifiConnectListener.onWiFiConnectFailure(ssid);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static OnWifiEnabledListener mOnWifiEnabledListener;

    private static OnWifiScanResultsListener mOnWifiScanResultsListener;

    private static OnWifiConnectListener mOnWifiConnectListener;

    public void setOnWifiEnabledListener(OnWifiEnabledListener listener) {
        mOnWifiEnabledListener = listener;
    }

    public void removeOnWifiEnabledListener() {
        mOnWifiEnabledListener = null;
    }

    public void setOnWifiScanResultsListener(OnWifiScanResultsListener listener) {
        mOnWifiScanResultsListener = listener;
    }

    public void removeOnWifiScanResultsListener() {
        mOnWifiScanResultsListener = null;
    }

    public void setOnWifiConnectListener(OnWifiConnectListener listener) {
        mOnWifiConnectListener = listener;
    }

    public void removeOnWifiConnectListener() {
        mOnWifiConnectListener = null;
    }
}
