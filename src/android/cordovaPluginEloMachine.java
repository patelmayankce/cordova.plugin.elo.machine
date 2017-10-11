package cordova.plugin.elo.machine;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;

import com.elo.device.DeviceManager;
import com.elo.device.ProductInfo;
import com.elo.device.enums.Status;
import com.elo.device.peripherals.CFD;

/**
 * This class echoes a string called from JavaScript.
 */
public class cordovaPluginEloMachine extends CordovaPlugin {

    public DeviceManager deviceManager;
    public ProductInfo productInfo;
    private Context context;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        context = this.cordova.getActivity().getApplicationContext();
        productInfo = DeviceManager.getInstance(context).getProductInfo();
        if (productInfo != null) {
            deviceManager = DeviceManager.getInstance(context);
            if (action.equals("openCashDrawer")) {
                this.openCashDrawer(callbackContext);
                return true;
            }
            return false;
        } else {
            callbackContext.error("SDK not configured properly");
            return false;
        }
    }

    private void openCashDrawer(CallbackContext callbackContext) {
        try {
            deviceManager.getCashDrawer().open();
            callbackContext.success("success");
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }
}
