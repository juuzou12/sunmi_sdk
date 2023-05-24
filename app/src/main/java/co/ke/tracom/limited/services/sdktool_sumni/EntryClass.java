package co.ke.tracom.limited.services.sdktool_sumni;

import static android.icu.util.MeasureUnit.EM;

import android.app.Application;
import android.graphics.SumPathEffect;

import ke.co.tracom.libsunmi.SunmiSDK;
import ke.co.tracom.libsunmi.emv.EMVAction;

public class EntryClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SunmiSDK.app = new SunmiSDK();

        SunmiSDK.app.init(getApplicationContext());

        EMVAction emvAction= new EMVAction(getApplicationContext());
        emvAction.initData();

    }
}
