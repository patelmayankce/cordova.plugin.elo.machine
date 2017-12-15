package cordova.plugin.elo.machine;

import android.util.Log;

import com.starmicronics.starioextension.IConnectionCallback;
import com.starmicronics.starioextension.StarIoExtManagerListener;

/**
 * Created by anand on 8/1/17.
 */

public class StarPrinterCallback extends StarIoExtManagerListener implements IConnectionCallback {
    String TAG = "StarPrinterCallback";

    String get_method_name() {
        return new Exception().getStackTrace()[1].getMethodName();
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onConnected(ConnectResult connectResult) {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onPrinterImpossible() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onPrinterOnline() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onPrinterOffline() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onPrinterPaperReady() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onPrinterPaperNearEmpty() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onPrinterPaperEmpty() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onPrinterCoverOpen() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onPrinterCoverClose() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onCashDrawerOpen() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onCashDrawerClose() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onBarcodeReaderImpossible() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onBarcodeReaderConnect() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onBarcodeReaderDisconnect() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onBarcodeDataReceive(byte[] var1) {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onAccessoryConnectSuccess() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onAccessoryConnectFailure() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onAccessoryDisconnect() {
        Log.d(TAG, "Callback received " + get_method_name());
    }

    @Override
    public void onStatusUpdate(String var1) {
        Log.d(TAG, "Callback received " + get_method_name());
    }

}
