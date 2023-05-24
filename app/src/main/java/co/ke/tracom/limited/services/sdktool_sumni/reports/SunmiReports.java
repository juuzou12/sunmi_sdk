package co.ke.tracom.limited.services.sdktool_sumni.reports;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Statement;
import java.util.List;

import ke.co.tracom.libsunmi.SunmiSDK;
import ke.co.tracom.libsunmi.api.EmvConfig;
import ke.co.tracom.libsunmi.api.TransactionType;
import ke.co.tracom.libsunmi.api.transactionData.SaleData;
import ke.co.tracom.libsunmi.api.transactionData.Settlement;
import ke.co.tracom.libsunmi.card.EmvResult;
import ke.co.tracom.libsunmi.db.SaveDate;
import ke.co.tracom.libsunmi.emv.EMVAction;
import ke.co.tracom.libsunmi.enums.CardType;
import ke.co.tracom.libsunmi.interfaces.CardStateEmitter;
import ke.co.tracom.libsunmi.interfaces.EMVListener;
import ke.co.tracom.libsunmi.interfaces.TransactionData;
import ke.co.tracom.libsunmi.reports.ReportsData;
import ke.co.tracom.libsunmi.reports.TotalReportData;

public class SunmiReports {
    Context that;
    String payload;
    public SunmiReports(Context that,String payload) {
        this.that = that;
        this.payload = payload;
    }

    //get the total reports from the database
    public void statementReports() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    List<ReportsData> reports = null;
                    try {
                        reports = SaveDate.statementDetails(new JSONObject(payload).getString("profileID") , that.getApplicationContext());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    System.out.println(reports);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //getting the statements report from the database
    public void miniStatement(EMVAction emv) {
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
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Intent intent = new Intent();
                            Settlement settlement=new Settlement();
                            settlement.setAmount(new JSONObject(payload).getString("amount") );
                            emv.start(that, new EMVListener() {
                                @Override
                                public void onEmvResult(EmvResult result) {
                                    Log.e(TAG, "onEmvResult----------");
                                    intent.putExtra("resp", gson.toJson(result));
                                    ((Activity) that).setResult(RESULT_OK, intent);
                                    ((Activity) that).finish();
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
                            }, getEmvConfig(TransactionType.MINI_STATEMENT,settlement));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }

    //this function will return EmvConfig
    public EmvConfig getEmvConfig(TransactionType transactionType, TransactionData transactionData){
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
            config.setProfileId(payload != null ? new JSONObject(payload).getString("profileID") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        config.setTransactionType(transactionType);
        config.setTransactionData(transactionData);
        return  config;
    }
}
