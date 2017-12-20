package cordova.plugin.elo.machine;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.Toast;

import com.elo.device.enums.Alignment;
import com.elo.device.enums.Language;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import com.starmicronics.starioextension.ICommandBuilder;
import com.starmicronics.starioextension.IConnectionCallback;
import com.starmicronics.starioextension.StarIoExt;
import com.starmicronics.starioextension.StarIoExtManager;
import com.starmicronics.starioextension.StarIoExtManagerListener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import static com.starmicronics.starioextension.StarIoExt.Emulation;
import static com.starmicronics.starioextension.ICommandBuilder.CutPaperAction;
import static com.starmicronics.starioextension.ICommandBuilder.SoundChannel;



public class StarPrinterHelper {

    public static final String TAG = "StarPrinterHelper";

    public static final int PAPER_SIZE_TWO_INCH = 384;
    public static final int PAPER_SIZE_THREE_INCH = 576;
    public static final int PAPER_SIZE_FOUR_INCH = 832;

    // Printer info
    public static class printer_info {
        public static Emulation emulation    = Emulation.StarGraphic;
        public static String PortName     = "USB:";
        public static String ModelName    = "Star TSP143IIIU";
        public static String DeviceName   = "USB:1-10";
        public static String PortSettings = "";
    }

    public static class printer_settings {
        public static int         font_size_w   = 10;      // TODO: put calculated values
        public static int         font_size_h   = 10;      // TODO: put calculated values
        public static Boolean     bold          = false;
        public static Language    language      = Language.ENGLISH;
        public static Alignment   alignment     = Alignment.LEFT;
    }

    public enum Result {
        Success,
        ErrorUnknown,
        ErrorOpenPort,
        ErrorBeginCheckedBlock,
        ErrorEndCheckedBlock,
        ErrorWritePort,
        ErrorReadPort,
    }

    interface SendCallback {
        void onStatus(boolean result, Result communicateResult);
    }

    public static byte[] createOpenDrawerData() {

        ICommandBuilder builder = StarIoExt.createCommandBuilder(printer_info.emulation);

        builder.beginDocument();
        builder.appendPeripheral(ICommandBuilder.PeripheralChannel.No1);
        builder.endDocument();

        return builder.getCommands();
    }

    public static byte[] commands_to_feed_lines(int lines) {
        ICommandBuilder builder = StarIoExt.createCommandBuilder(printer_info.emulation);

        int units_per_line = 64;

        builder.beginDocument();
        builder.appendUnitFeed(units_per_line * lines);
        builder.endDocument();

        return builder.getCommands();
    }

    static public Bitmap createBitmapFromText(String printText, int textSize, int printWidth, Typeface typeface) {
        Paint paint = new Paint();
        Bitmap bitmap;
        Canvas canvas;

        paint.setTextSize(textSize);
        paint.setTypeface(typeface);

        paint.getTextBounds(printText, 0, printText.length(), new Rect());

        TextPaint textPaint = new TextPaint(paint);
        StaticLayout staticLayout = new StaticLayout(printText, textPaint, printWidth, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);

        // Create bitmap
        bitmap = Bitmap.createBitmap(staticLayout.getWidth(), staticLayout.getHeight(), Bitmap.Config.ARGB_8888);

        // Create canvas
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.translate(0, 0);
        staticLayout.draw(canvas);

        return bitmap;
    }

    public static Bitmap create3inchRasterReceiptImage() {
        String textToPrint =
                "        Elo Clothing Boutique\n" +
                        "             123 Elo Road\n" +
                        "           City, State 12345\n" +
                        "\n" +
                        "Date:MM/DD/YYYY          Time:HH:MM PM\n" +
                        "--------------------------------------\n" +
                        "SALE\n" +
                        "SKU            Description       Total\n" +
                        "300678566      PLAIN T-SHIRT     10.99\n" +
                        "300692003      BLACK DENIM       29.99\n" +
                        "300651148      BLUE DENIM        29.99\n" +
                        "300642980      STRIPED DRESS     49.99\n" +
                        "30063847       BLACK BOOTS       35.99\n" +
                        "\n" +
                        "Subtotal                        156.95\n" +
                        "Tax                               0.00\n" +
                        "--------------------------------------\n" +
                        "Total                          $156.95\n" +
                        "--------------------------------------\n" +
                        "\n" +
                        "Charge\n" +
                        "156.95\n" +
                        "Visa XXXX-XXXX-XXXX-0123\n" +
                        "Refunds and Exchanges\n" +
                        "Within 30 days with receipt\n" +
                        "And tags attached\n";

        int      textSize = 25;
        Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);

        return createBitmapFromText(textToPrint, textSize, PAPER_SIZE_THREE_INCH, typeface);
    }


    public static byte[] createScaleRasterReceiptData(Context context, int width, boolean bothScale) {
        byte[] data;

        ICommandBuilder builder = StarIoExt.createCommandBuilder(printer_info.emulation);

        builder.beginDocument();

        // add text info
        Bitmap image = create3inchRasterReceiptImage();
        builder.appendBitmap(image, false, width, bothScale);

        // add Barcode

        String barcode = "1234567890";
        data = barcode.getBytes();
        builder.appendBarcode(data, ICommandBuilder.BarcodeSymbology.Code93,ICommandBuilder.BarcodeWidth.Mode1, 100, false);

        Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
        builder.appendBitmap(createBitmapFromText(barcode, 25, PAPER_SIZE_THREE_INCH, typeface), false, width, bothScale);

        builder.appendCutPaper(CutPaperAction.PartialCutWithFeed);
        builder.endDocument();

        return builder.getCommands();
    }


    public static Bitmap create3inchRasterReceiptImage(String textToPrint)
    {
        int      textSize = 25;
        Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);

        return createBitmapFromText(textToPrint, textSize, PAPER_SIZE_THREE_INCH, typeface);
    }

    public static byte[] commands_to_print_plain_text(String text_to_print) {

        ICommandBuilder builder = StarIoExt.createCommandBuilder(printer_info.emulation);

        Bitmap image = create3inchRasterReceiptImage(text_to_print);

        builder.beginDocument();
        builder.appendBitmap(image, false, PAPER_SIZE_THREE_INCH, true);
        builder.appendUnitFeed(64);
        builder.appendCutPaper(CutPaperAction.PartialCutWithFeed);
        builder.appendUnitFeed(64);
        builder.endDocument();

        return builder.getCommands();
    }


    public static Boolean get_printer_paper_status(StarIoExtManager h_sdkman) {
        StarIoExtManager.PrinterPaperStatus paper_status = h_sdkman.getPrinterPaperReadyStatus();

        switch(paper_status) {
            case Ready:
            case NearEmpty:                return true;

            default:                       return false;
        }
    }

    public static void sendCommands(Object lock, byte[] commands, String portName, String portSettings, int timeout, Context context, SendCallback callback) {
        SendCommandThread thread = new SendCommandThread(lock, commands, portName, portSettings, timeout, context, callback);
        thread.start();
    }

    public static void print(String printText,StarIoExtManager starIoExtManager_obj , Context ctx, SendCallback cb)
    {
        byte[] cmd;

    //    String Text = "\n\n\nYour Elo Touch Solutions\nPayPoint receipt printer is\nworking properly.";
       cmd = commands_to_print_plain_text(printText);

        // cmd = createScaleRasterReceiptData(ctx, PAPER_SIZE_THREE_INCH, true);
        sendCommands(starIoExtManager_obj, cmd, printer_info.PortName, printer_info.PortSettings, 10000, ctx, cb);     // 10000mS!!!
    }

    public static void open_drawer(StarIoExtManager starIoExtManager_obj , Context ctx, SendCallback cb)
    {
        byte[] cmd;
        cmd = createOpenDrawerData();
        sendCommands(starIoExtManager_obj, cmd, printer_info.PortName, printer_info.PortSettings, 10000, ctx, cb);     // 10000mS!!!
    }


    public static class SendCommandThread extends Thread {
        private final Object mLock;
        private SendCallback mCallback;
        private byte[] mCommands;

        private StarIOPort mPort;

        private String  mPortName = null;
        private String  mPortSettings;
        private int     mTimeout;
        private Context mContext;

        SendCommandThread(Object lock, byte[] commands, StarIOPort port, SendCallback callback) {
            mCommands = commands;
            mPort     = port;
            mCallback = callback;
            mLock     = lock;
        }

        SendCommandThread(Object lock, byte[] commands, String portName, String portSettings, int timeout, Context context, SendCallback callback) {
            mCommands     = commands;
            mPortName     = portName;
            mPortSettings = portSettings;
            mTimeout      = timeout;
            mContext      = context;
            mCallback     = callback;
            mLock         = lock;
        }

        @Override
        public void run() {
            Result communicateResult = Result.ErrorOpenPort;
            boolean result = false;

            synchronized (mLock) {
                try {
                    if (mPort == null) {

                        if (mPortName == null) {
                            resultSendCallback(false, communicateResult, mCallback);
                            return;
                        } else {
                            mPort = StarIOPort.getPort(mPortName, mPortSettings, mTimeout, mContext);
                        }
                    }
                    if (mPort == null) {
                        communicateResult = Result.ErrorOpenPort;
                        resultSendCallback(false, communicateResult, mCallback);
                        return;
                    }

//          // When using USB interface with mPOP(F/W Ver 1.0.1), you need to send the following data.
//          byte[] dummy = {0x00};
//          port.writePort(dummy, 0, dummy.length);

                    StarPrinterStatus status;

                    communicateResult = Result.ErrorBeginCheckedBlock;

                    status = mPort.beginCheckedBlock();

                    if (status.offline) {
                        throw new StarIOPortException("A printer is offline");
                    }

                    communicateResult = Result.ErrorWritePort;

                    mPort.writePort(mCommands, 0, mCommands.length);

                    communicateResult = Result.ErrorEndCheckedBlock;

                    mPort.setEndCheckedBlockTimeoutMillis(30000);     // 30000mS!!!

                    status = mPort.endCheckedBlock();

                    if (status.coverOpen) {
                        throw new StarIOPortException("Printer cover is open");
                    } else if (status.receiptPaperEmpty) {
                        throw new StarIOPortException("Receipt paper is empty");
                    } else if (status.offline) {
                        throw new StarIOPortException("Printer is offline");
                    }

                    result = true;
                    communicateResult = Result.Success;
                } catch (StarIOPortException e) {
                    Log.e(TAG, "Exception while sending command", e);
                }

                if (mPort != null && mPortName != null) {
                    try {
                        StarIOPort.releasePort(mPort);
                    } catch (StarIOPortException e) {
                        // Nothing
                        Log.e(TAG, "Exception while releasing port");
                    }
                    mPort = null;
                } else {
                    Log.e(TAG, "Port or PortName is null");
                }

                resultSendCallback(result, communicateResult, mCallback);
            }
        }

        private void resultSendCallback(final boolean result, final Result communicateResult, final SendCallback callback) {
            if (callback != null) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onStatus(result, communicateResult);
                    }
                });
            }
        }
    }




}
