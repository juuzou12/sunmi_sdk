package co.ke.tracom.limited.services.sdktool_sumni;

import android.app.Application;
import android.graphics.SumPathEffect;

import ke.co.tracom.libsunmi.SunmiSDK;

public class EntryClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SunmiSDK.app = new SunmiSDK();

        SunmiSDK.app.init(getApplicationContext());

    }
}
