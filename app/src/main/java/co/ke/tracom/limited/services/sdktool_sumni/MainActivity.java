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

import java.io.IOException;

import co.ke.tracom.limited.services.sdktool_sumni.device.DeviceInformation;
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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OnCreateTrigger(this);
    }

    // this will trigger all functions that are required to be called in the onCreate

    private void OnCreateTrigger(Context c) {
        Intent intent = new Intent("apos.sdktool");
        Gson gson = new Gson();
        String action = "";
        String payload = "";

        if ("apos.sdktool".equals(intent.getAction())) {
            action = getIntent().getStringExtra("action");
            payload = getIntent().getStringExtra("payload");
            if (action != null) {
                switch (action) {
                    // add cases here
                    case "getDeviceNumber":
                        getSerialNumber(intent);
                        break;
                    case "doEmv":
                        onDoEmv();
                        break;
                }
            }
        }
    }

    private void onDoEmv() {
        EMVAction emvAction = new EMVAction(this);
        EmvConfig config=new EmvConfig();
        config.setOtherAmount("10");
        config.setTransactionType(TransactionType.SALE);
        config.setTid("11111111");
        config.setMid("111111111111111");
        config.setCardNumber("4762858001190538");
        config.setExpDate("771");
        config.setCvv("2224");
        config.setIp("41.215.130.247");
        config.setPort(8365);
        config.setProfileId("110a8c6f-e96d-4003-96e1-b45646387c3b");
        Context that = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                while (true) {
                    if (SunmiSDK.app.emvOptV2 == null) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    break;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println(config);
                            Intent intent=new Intent();
                            emvAction.start(that, new EMVListener() {
                                @Override
                                public void onEmvResult(EmvResult result) {
                                    Log.e(TAG, "onEmvResult----------");
                                    intent.putExtra("resp", result.toString());
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            }, new CardStateEmitter() {
                                @Override
                                public void onInsertCard() {
                                    Log.e(TAG, "onInsertCard----------");
                                }

                                @Override
                                public void onCardInserted(CardType cardType) {
                                    Log.e(TAG, "onCardInserted----------");
                                }

                                @Override
                                public void onProcessingEmv() {
                                    Log.e(TAG, "onProcessingEmv----------");
                                }
                            },config);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
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
}