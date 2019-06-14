package com.termux.api;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.JsonWriter;

import com.termux.api.util.ResultReturner;

import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

public class UsbAPI {

    private static final String ACTION_USB_PERMISSION = "com.termux.api.usb.USB_PERMISSION";

    static void onReceiveUsbInfo(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.ResultJsonWriter() {
            @Override
            public void writeJson(JsonWriter out) throws Exception {
                UsbManager manager = (UsbManager) context.getApplicationContext().getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                out.beginArray();
                out.beginObject();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    out.name("device_id").value(device.getDeviceId());
                    out.name("device_name").value(device.getDeviceName());
                    out.name("device_class").value(device.getDeviceClass()+" - "+translateDeviceClass(device.getDeviceClass()));
                    out.name("device_sub_class").value(device.getDeviceSubclass());
                    out.name("manufacturer_name").value(device.getManufacturerName());
                    out.name("vendor_id").value("0x"+Integer.toHexString(device.getVendorId()));
                    out.name("product_id").value("0x"+Integer.toHexString(device.getProductId()));
                }
                out.endObject();
                out.endArray();
            }
        });
    }

    static void onReceiveUsbOpen(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
	int vendorId = intent.getIntExtra("vendorId", 0);
	int productId = intent.getIntExtra("productId", 0);
        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.ResultJsonWriter() {
            @Override
            public void writeJson(JsonWriter out) throws Exception {
                UsbManager manager = (UsbManager) context.getApplicationContext().getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                UsbDevice device;
                UsbDeviceConnection connection = null;
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                out.beginArray();
                out.beginObject();
                out.name("vendorId").value(vendorId);
                out.name("productId").value(productId);
                out.endObject();

                while (deviceIterator.hasNext()) {
                    device = deviceIterator.next();
                    //out.beginObject().name("vendor_id").value(device.getVendorId()).endObject();

                    if ((vendorId == 0 || device.getVendorId() == vendorId) && (productId == 0 || device.getProductId() == productId)) {
                        if (! manager.hasPermission(device)) {
                            manager.requestPermission(device, mPermissionIntent);
                        }
                        connection = manager.openDevice(device);
                        out.beginObject().name("fd").value(connection.getFileDescriptor()).endObject();
                        break;
                    }
                }

                if (connection == null) {
                    out.beginObject().name("Connection").value("failed").endObject();
                } else {
                    out.beginObject().name("Connection").value("succeeded").endObject();
                }
                //manager.RequestPermission(device, mPermissionIntent);
                //bool hasPermision = manager.HasPermission(device);
                //UsbInterface intf = device.getInterface(0);
                //UsbEndpoint endpoint = intf.getEndpoint(0);
                //permissionIntent = PendingIntent.getBroadcast(0, new Intent(ACTION_USB_PERMISSION), 0);
                //usbManager.requestPermission(device, permissionIntent);
                out.endArray();
            }
        });
    }

    static String translateDeviceClass(int usbClass){
        switch(usbClass){
        case UsbConstants.USB_CLASS_APP_SPEC:
            return "App specific USB class";
        case UsbConstants.USB_CLASS_AUDIO:
            return "Audio device";
        case UsbConstants.USB_CLASS_CDC_DATA:
            return "CDC device (communications device class)";
        case UsbConstants.USB_CLASS_COMM:
            return "Communication device";
        case UsbConstants.USB_CLASS_CONTENT_SEC:
            return "Content security device";
        case UsbConstants.USB_CLASS_CSCID:
            return "Content smart card device";
        case UsbConstants.USB_CLASS_HID:
            return "Human interface device (for example a keyboard)";
        case UsbConstants.USB_CLASS_HUB:
            return "USB hub";
        case UsbConstants.USB_CLASS_MASS_STORAGE:
            return "Mass storage device";
        case UsbConstants.USB_CLASS_MISC:
            return "Wireless miscellaneous devices";
        case UsbConstants.USB_CLASS_PER_INTERFACE:
            return "Usb class is determined on a per-interface basis";
        case UsbConstants.USB_CLASS_PHYSICA:
            return "Physical device";
        case UsbConstants.USB_CLASS_PRINTER:
            return "Printer";
        case UsbConstants.USB_CLASS_STILL_IMAGE:
            return "Still image devices (digital cameras)";
        case UsbConstants.USB_CLASS_VENDOR_SPEC:
            return "Vendor specific USB class";
        case UsbConstants.USB_CLASS_VIDEO:
            return "Video device";
        case UsbConstants.USB_CLASS_WIRELESS_CONTROLLER:
            return "Wireless controller device";
        default: return "Unknown USB class!";
        }
    }
}
