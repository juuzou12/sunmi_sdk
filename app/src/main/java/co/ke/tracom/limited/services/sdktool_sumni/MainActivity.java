package co.ke.tracom.limited.services.sdktool_sumni;

import static android.content.ContentValues.TAG;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;

import org.jpos.iso.ISOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.ke.tracom.limited.services.sdktool_sumni.device.DeviceInformation;
import co.ke.tracom.limited.services.sdktool_sumni.emvprocesses.EmvProcess;
import co.ke.tracom.limited.services.sdktool_sumni.reports.SunmiReports;
import ke.co.tracom.libsunmi.SunmiFunctions;
import ke.co.tracom.libsunmi.SunmiSDK;
import ke.co.tracom.libsunmi.api.EmvConfig;
import ke.co.tracom.libsunmi.api.TransactionType;
import ke.co.tracom.libsunmi.card.EmvResult;
import ke.co.tracom.libsunmi.emv.EMVAction;
import ke.co.tracom.libsunmi.enums.CardType;
import ke.co.tracom.libsunmi.enums.Transactions;
import ke.co.tracom.libsunmi.interfaces.CardStateEmitter;
import ke.co.tracom.libsunmi.interfaces.EMVListener;
import ke.co.tracom.libsunmi.interfaces.TransactionData;
import ke.co.tracom.libsunmi.printer.SunmiPrinter;
import ke.co.tracom.libsunmi.reports.ReportsData;
import ke.co.tracom.libsunmi.reports.TotalReportData;

public class MainActivity extends AppCompatActivity {
    EMVAction emvAction = new EMVAction(this);
    String action = "";
    String payload = "";
    Intent intent = new Intent("apos.sdktool");
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        OnCreateTrigger(this);
    }

    // this will trigger all functions that are required to be called in the onCreate
    private void OnCreateTrigger(Context c) {
        if ("apos.sdktool".equals(intent.getAction())) {
            action = getIntent().getStringExtra("action");
            payload = getIntent().getStringExtra("payload");
            System.out.println(payload);
            if (action != null) {
                switch (action) {
                    // add cases here
                    case "getDeviceNumber":
                        getSerialNumber(intent);
                        break;
                    case "doEmv":
                        try {
                            new EmvProcess(this,emvAction,payload).emvMainTrigger();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "doPrint":
                        doPrint(payload);
                        break;
                    case "doBalanceEnquiry":
                        new EmvProcess(this,emvAction,payload).getBalance();
                        break;
                    case "pre-auth-adjust":
                        new EmvProcess(this,emvAction,payload).doPreAuthAdjust();
                        break;
                    case "doTwoAmount":
                        new EmvProcess(this,emvAction,payload).saleWithCashBack();
                        break;
                    case "Adjust":
                        new EmvProcess(this,emvAction,payload).doAdjust();
                        break;
                    case "pre-auth-completion":
                        new EmvProcess(this,emvAction,payload).preAuthorizeCompletion();
                        break;
                }
            }
        }
    }



    //get the device serial number
    public void getSerialNumber(Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                while (true) {
                    if (SunmiSDK.app.basicOptV2 == null) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    break;
                }

                String serial = DeviceInformation.serialNumber();

                intent.putExtra("resp", serial);
                setResult(RESULT_OK, intent);
                finish();

                Log.e("device number", serial);
                Looper.loop();
            }
        }).start();
    }

    //do print
    public  void doPrint(String payload){
        Intent intent=new Intent();
        SunmiPrinter.printerFunction(payload,this,R.drawable.awash);
        new  Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                intent.putExtra("resp", "successfully done print");
                setResult(RESULT_OK, intent);
                finish();
            }
        }).start();

    }

}