package co.ke.tracom.limited.services.sdktool_sumni.device;

import android.os.RemoteException;

import ke.co.tracom.libsunmi.SunmiSDK;

public class DeviceInformation {
    //trigger the get device information (serial number)
    public static String serialNumber() {

        String serialNumber = "";
        try {
            serialNumber = SunmiSDK.app.basicOptV2.getSysParam("SN");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return serialNumber;
    }
}