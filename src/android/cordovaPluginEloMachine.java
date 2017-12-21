package cordova.plugin.elo.machine;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;

import java.util.ArrayList;

import com.elo.device.DeviceManager;
import com.elo.device.ProductInfo;
import com.elo.device.enums.Status;
import com.elo.device.peripherals.CFD;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import com.starmicronics.starioextension.ICommandBuilder;
import com.starmicronics.starioextension.IConnectionCallback;
import com.starmicronics.starioextension.StarIoExt;
import com.starmicronics.starioextension.StarIoExtManager;
import com.starmicronics.starioextension.StarIoExtManagerListener;

import static com.starmicronics.starioextension.StarIoExt.Emulation;
import static com.starmicronics.starioextension.ICommandBuilder.CutPaperAction;
import static com.starmicronics.starioextension.ICommandBuilder.SoundChannel;

/**
 * This class echoes a string called from JavaScript.
 */
public class cordovaPluginEloMachine extends CordovaPlugin {

    public DeviceManager deviceManager;
    public ProductInfo productInfo;
    private Context context;

    public StarIoExtManager m_StarIoExtMan;
    public StarPrinterCallback m_StarPrinter_cb;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        context = this.cordova.getActivity().getApplicationContext();
        productInfo = DeviceManager.getInstance(context).getProductInfo();
        if (productInfo != null) {
            deviceManager = DeviceManager.getInstance(context);
            if (is_device_payoint_2_0()) {
                init_paypoint_2_0_peripherals();
            }
            if (action.equals("openCashDrawer")) {
                this.openCashDrawer(callbackContext);
                return true;
            } else if (action.equals("print")) {
                this.print(args.getString(0), callbackContext);
                return true;
            }
            return false;
        } else {
            callbackContext.error("SDK not configured properly");
            return false;
        }
    }

    private StarPrinterHelper.SendCallback mCallback = new StarPrinterHelper.SendCallback() {
        @Override
        public void onStatus(boolean result, StarPrinterHelper.Result communicateResult) {

            String msg;

            switch (communicateResult) {
            case Success:
                msg = "Success!";
                break;
            case ErrorOpenPort:
                msg = "Fail to openPort";
                break;
            case ErrorBeginCheckedBlock:
                msg = "Printer is offline (beginCheckedBlock)";
                break;
            case ErrorEndCheckedBlock:
                msg = "Printer is offline (endCheckedBlock)";
                break;
            case ErrorReadPort:
                msg = "Read port error (readPort)";
                break;
            case ErrorWritePort:
                msg = "Write port error (writePort)";
                break;
            default:
                msg = "Unknown error";
                break;
            }

        }
    };

    boolean is_device_payoint_2_0() {
        if (productInfo != null)
            if (productInfo.name.equals("PAYPOINT-2.0"))
                return true;

        return false;
    }

    void init_paypoint_2_0_peripherals() {
        if (is_device_payoint_2_0()) {
            m_StarIoExtMan = new StarIoExtManager(StarIoExtManager.Type.Standard,
                    StarPrinterHelper.printer_info.PortName, StarPrinterHelper.printer_info.PortSettings, 10000,
                    context); // 10000mS!!!

            m_StarIoExtMan.setCashDrawerOpenActiveHigh(true);
            m_StarPrinter_cb = new StarPrinterCallback();
            m_StarIoExtMan.setListener(m_StarPrinter_cb);
            m_StarIoExtMan.connect(m_StarPrinter_cb);
        }
    }

    private void openCashDrawer(CallbackContext callbackContext) {
        try {
            if (is_device_payoint_2_0()) {
                StarPrinterHelper.open_drawer(m_StarIoExtMan, context, mCallback);
            } else {
                deviceManager.getCashDrawer().open();
            }
            callbackContext.success("success");
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    private void print(String printText, CallbackContext callbackContext) {
        try {
            if (is_device_payoint_2_0()) {
                StarPrinterHelper.print(printText, m_StarIoExtMan, context, mCallback);
            } else {
                deviceManager.getPrinter().print(getPrintData(printText));
            }
            callbackContext.success("success");
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    private String getPrintData(String printText) {

        ArrayList<Byte> dataArray = new ArrayList<Byte>();
        // dataArray.addAll(AlignCenter());
        dataArray.addAll(toByteArray(printText)); //Prints the Text

        String response = dataArray.toString();

        String[] byteValues = response.substring(1, response.length() - 1).split(",");
        byte[] bytes = new byte[byteValues.length];

        for (int i = 0, len = bytes.length; i < len; i++) {
            bytes[i] = Byte.parseByte(byteValues[i].trim());
        }
        String Result = new String(bytes);
        return Result;
    }

    private ArrayList<Byte> toByteArray(String string) {
        ArrayList<Byte> list = new ArrayList<Byte>();
        for (byte byt : string.getBytes()) {
            list.add(Byte.valueOf(byt));
        }
        // line feed
        list.add(Byte.valueOf((byte) 0x0A));
        list.add(Byte.valueOf((byte) 0x0D));
        return list;
    }
}
