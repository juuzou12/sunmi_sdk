package co.ke.tracom.limited.services.sdktool_sumni.reports;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import static org.jpos.iso.packager.LogPackager.LOG_TAG;

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
import ke.co.tracom.libsunmi.api.transactionData.OtherData;
import ke.co.tracom.libsunmi.api.transactionData.SaleData;
import ke.co.tracom.libsunmi.api.transactionData.Settlement;
import ke.co.tracom.libsunmi.card.EmvResult;
import ke.co.tracom.libsunmi.cardless.Manual;
import ke.co.tracom.libsunmi.db.Reports;
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
    EMVAction emv;
    public SunmiReports(Context that,String payload,EMVAction emv) {
        this.that = that;
        this.payload = payload;
        this.emv = emv;
    }

    //get the total reports from the database
    public void statementReports() {
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

                            Reports.statementOfDay(new EMVListener() {
                                @Override
                                public void onEmvResult(EmvResult result) {
                                    Log.e(TAG, "onEmvResult----------");
                                    intent.putExtra("resp", gson.toJson(result));
                                    ((Activity) that).setResult(RESULT_OK, intent);
                                    ((Activity) that).finish();
                                }
                            },getEmvConfig(null,null),that);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }

    //getting the statements report from the database
    public void miniStatement() {
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

    //get the list of manual settlement

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

    //the total reports
    public void getTotalReports() {
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
                            TotalReportData totalReportData=new TotalReportData();
                            Reports.totalReport(new EMVListener() {
                                @Override
                                public void onEmvResult(EmvResult result) {
                                    Log.e(TAG, "onEmvResult----------");
                                    intent.putExtra("resp", gson.toJson(result));
                                    ((Activity) that).setResult(RESULT_OK, intent);
                                    ((Activity) that).finish();
                                }
                            },getEmvConfig(null,null),that);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }

    //manual Settlement
    public void manualSettlement(){
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
                            Manual manual = new Manual();
                            manual.doManualSettlement(that, new EMVListener() {
                                @Override
                                public void onEmvResult(EmvResult result) {
                                    Log.e(TAG, "onEmvResult----------");
                                    intent.putExtra("resp", gson.toJson(result));
                                    ((Activity) that).setResult(RESULT_OK, intent);
                                    ((Activity) that).finish();
                                }
                            },getEmvConfig(TransactionType.SETTLEMENT, settlement));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }


    //Receipt reprint
    public void printReceiptReprint() {
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
                            Reports.lastReceipt(new EMVListener() {
                                @Override
                                public void onEmvResult(EmvResult result) {
                                    Log.e(TAG, "onEmvResult----------");
                                    intent.putExtra("resp", gson.toJson(result));
                                    ((Activity) that).setResult(RESULT_OK, intent);
                                    ((Activity) that).finish();
                                }
                            },getEmvConfig(null,null),that);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }

    //duplicate receipt
    public void duplicateReceipt(){
        OtherData otherData = new OtherData();
        Intent intent = new Intent();
        Gson gson = new Gson();
        try {
            otherData.setInvoiceNumber(new JSONObject(payload).getString("invoiceNo") );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        emv.queryBasedOnInvoices(new EMVListener() {
            @Override
            public void onEmvResult(EmvResult result) {
                Log.d(LOG_TAG, result.toString());
                intent.putExtra("resp", gson.toJson(result));
                ((Activity) that).setResult(RESULT_OK, intent);
                ((Activity) that).finish();
            }
        }, otherData);
    }
}
