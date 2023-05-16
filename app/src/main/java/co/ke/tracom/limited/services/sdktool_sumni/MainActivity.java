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

import co.ke.tracom.limited.services.sdktool_sumni.device.DeviceInformation;
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
            if (action != null) {
                switch (action) {
                    // add cases here
                    case "getDeviceNumber":
                        getSerialNumber(intent);
                        break;
                    case "doEmv":
                        onDoEmv(payload);
                        break;
                    case "doPrint":
                        doPrint(payload);
                        break;
                    case "doBalanceEnquiry":
                        getBalance(payload);
                        break;
                    case "pre-auth":
                        doPreAuth(payload);
                        break;
                }
            }
        }
    }

    //get the balance of the card
    public  void getBalance(String payload){
        Context that = this;
        Intent intent=new Intent();
        Gson gson = new Gson();
        try {
            emvAction.start(that, new EMVListener() {
                @Override
                public void onEmvResult(EmvResult result) {
                    //return to the ui application using intents
                    intent.putExtra("resp", gson.toJson(result));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }, new CardStateEmitter() {
                @Override
                public void onInsertCard() {
                    //introduce the lottie file for inserting / swapping / tapping cards
                }

                @Override
                public void onCardInserted(CardType cardType) {
                    //once the card is inserted
                }

                @Override
                public void onProcessingEmv() {
                    // whenever the card is processing the emv
                }
            },getEmvConfig(TransactionType.BALANCE_ENQUIRY,payload));
        } catch (RemoteException | ISOException | IOException e) {
            e.printStackTrace();
        }
    }

    //do emv processing
    private void onDoEmv(String payload) {
        Context that = this;
        Gson gson = new Gson();
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
                            Intent intent=new Intent();
                            emvAction.start(that, new EMVListener() {
                                @Override
                                public void onEmvResult(EmvResult result) {
                                    Log.e(TAG, "onEmvResult----------");
                                    intent.putExtra("resp", gson.toJson(result));
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
                            },getEmvConfig(TransactionType.SALE,payload));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }

    //do pre-auth
    private void doPreAuth(String payload){
        Context that = this;
        Gson gson = new Gson();
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
                            Intent intent=new Intent();
                            emvAction.start(that, new EMVListener() {
                                @Override
                                public void onEmvResult(EmvResult result) {
                                    Log.e(TAG, "onEmvResult----------");
                                    intent.putExtra("resp", gson.toJson(result));
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
                            },getEmvConfig(TransactionType.PREAUTHORIZATION,payload));
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

    //this function will return EmvConfig
    static EmvConfig getEmvConfig(TransactionType transactionType,String payload){
        EmvConfig config=new EmvConfig();
        try {
            config.setOtherAmount(payload != null ? new JSONObject(payload).getString("amount") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            config.setTid(payload != null ? new JSONObject(payload).getString("tid") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            config.setMid(payload != null ? new JSONObject(payload).getString("mid") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            config.setCardNumber(payload != null ? new JSONObject(payload).getString("cardNo") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            config.setExpDate(payload != null ? new JSONObject(payload).getString("expDate") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            config.setCvv(payload != null ? new JSONObject(payload).getString("cvv") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            config.setIp(payload != null ? new JSONObject(payload).getString("ip") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            config.setProfileId(payload != null ? new JSONObject(payload).getString("ip") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            config.setProfileId(payload != null ? new JSONObject(payload).getString("profileID") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        config.setTransactionType(transactionType);
        return  config;
    }
}