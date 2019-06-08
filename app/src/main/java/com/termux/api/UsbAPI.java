package com.termux.api;

import android.annotation.SuppressLint;
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
    static void onReceiveUsbInfo(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.ResultJsonWriter() {
            @Override
            public void writeJson(JsonWriter out) throws Exception {
                UsbManager manager = (UsbManager) context.getApplicationContext().getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
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
            }
        });
    }

    static void onReceiveUsbOpen(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.ResultJsonWriter() {
            @Override
            public void writeJson(JsonWriter out) throws Exception {
                UsbManager manager = (UsbManager) context.getApplicationContext().getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                UsbDevice device;
                UsbDeviceConnection connection = null;
                out.beginObject();
                while (deviceIterator.hasNext()) {
                    device = deviceIterator.next();
                    out.name("vendor_id").value(device.getVendorId());
                    if (device.getVendorId() == 5008) {
                        connection = manager.openDevice(device);
                        break;
                    }
                }

                //manager.RequestPermission(device, mPermissionIntent);
                //bool hasPermision = manager.HasPermission(device);
                //UsbInterface intf = device.getInterface(0);
                //UsbEndpoint endpoint = intf.getEndpoint(0);
                //permissionIntent = PendingIntent.getBroadcast(0, new Intent(ACTION_USB_PERMISSION), 0);
                //usbManager.requestPermission(device, permissionIntent);

                if (connection == null) {
                    out.name("connection failed");
                } else {
                    out.name("connection succeeded");
                }
                out.endObject();
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
