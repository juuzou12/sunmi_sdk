package co.ke.tracom.limited.services.sdktool_sumni.emvprocesses;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;


import static org.jpos.iso.packager.LogPackager.LOG_TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;

import org.jpos.iso.ISOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import co.ke.tracom.limited.services.sdktool_sumni.reports.SunmiReports;
import ke.co.tracom.libsunmi.SunmiSDK;
import ke.co.tracom.libsunmi.api.EmvConfig;
import ke.co.tracom.libsunmi.api.TransactionType;
import ke.co.tracom.libsunmi.api.transactionData.BalanceInquiry;
import ke.co.tracom.libsunmi.api.transactionData.OtherData;
import ke.co.tracom.libsunmi.api.transactionData.PreAuthAdjust;
import ke.co.tracom.libsunmi.api.transactionData.PreAuthData;
import ke.co.tracom.libsunmi.api.transactionData.SaleData;
import ke.co.tracom.libsunmi.card.EmvResult;
import ke.co.tracom.libsunmi.emv.EMVAction;
import ke.co.tracom.libsunmi.enums.CardType;
import ke.co.tracom.libsunmi.interfaces.CardStateEmitter;
import ke.co.tracom.libsunmi.interfaces.EMVListener;
import ke.co.tracom.libsunmi.interfaces.TransactionData;

public class EmvProcess {
    Context that;
    EMVAction emv;
    String payload;
    public EmvProcess(Context that,EMVAction emv,String payload) {
        this.that = that;
        this.emv = emv;
        this.payload = payload;

    }
    Gson gson = new Gson();
    Intent intent = new Intent();
    //this will be initialized as the emv process main function
    //it will contain a switch case that checks the transactions type then handle based on the parameters passed to it
    public void emvMainTrigger() throws JSONException {
        switch (new JSONObject(payload).getString("type")){
            case "sale":
                doSale();
                break;
            case "pre-auth":
                doPreAuth();
                break;
            case "statementOfDay":
                new SunmiReports(that).statementReports(payload);
                break;
            case "pre-auth-after-invoice":
                preAuthAfterInvoice();
                break;
            default:
                break;
        }
    }

    //do sale
    private void doSale() {
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
                            }, getEmvConfig(TransactionType.SALE,new SaleData()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }

    //get the balance of the card
    public  void getBalance(){
        Intent intent=new Intent();
        Gson gson = new Gson();
        try {
            emv.start(that, new EMVListener() {
                @Override
                public void onEmvResult(EmvResult result) {
                    //return to the ui application using intents
                    intent.putExtra("resp", gson.toJson(result));
                    ((Activity) that).setResult(RESULT_OK, intent);
                    ((Activity) that).finish();
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
            },getEmvConfig(TransactionType.BALANCE_ENQUIRY,new BalanceInquiry()));
        } catch (RemoteException | ISOException | IOException e) {
            e.printStackTrace();
        }
    }

    //do pre-auth-adjust
    public void doPreAuthAdjust() {
        OtherData otherData = new OtherData(TransactionType.PREAUTHORIZATION_ADJUSTMENT);
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

    //pre-auth-after-invoice
    //this is after the invoice for the preAuth transaction has returned to the ui with a successful result
    public void preAuthAfterInvoice(){
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
                            }, getEmvConfig(TransactionType.PREAUTHORIZATION_ADJUSTMENT,new PreAuthAdjust()));
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
    private void doPreAuth(){
        Gson gson = new Gson();
        EmvConfig config = getEmvConfig(TransactionType.PREAUTHORIZATION, new PreAuthData());
        System.out.println("+++++" + config.getProfileId());

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
                            Intent intent=new Intent();
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
