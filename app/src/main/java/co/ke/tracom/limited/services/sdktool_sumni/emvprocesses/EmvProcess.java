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
import ke.co.tracom.libsunmi.api.transactionData.Adjust;
import ke.co.tracom.libsunmi.api.transactionData.BalanceInquiry;
import ke.co.tracom.libsunmi.api.transactionData.Cash;
import ke.co.tracom.libsunmi.api.transactionData.OtherData;
import ke.co.tracom.libsunmi.api.transactionData.PreAuthAdjust;
import ke.co.tracom.libsunmi.api.transactionData.PreAuthCompletionCancellation;
import ke.co.tracom.libsunmi.api.transactionData.PreAuthData;
import ke.co.tracom.libsunmi.api.transactionData.SaleData;
import ke.co.tracom.libsunmi.api.transactionData.SaleWithCashbackData;
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
                new SunmiReports(that,payload,emv).statementReports();
                break;
            case "pre-auth-after-invoice":
                preAuthAfterInvoice();
                break;
            case "cash-advance":
                doCashAdvanced();
                break;
            case "Sale with cashback":
                saleWithCashBack();
                break;
            case "Mini-statement":
                new SunmiReports(that,payload,emv).miniStatement();
                break;
            case "adjust-after-invoice":
                finishAdjust();
                break;
            case "Pre-authorize completion after":
                preAuthorizeCompletionAfterInvoice();
                break;
            case "totalReport":
                new SunmiReports(that,payload,emv).getTotalReports();
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
                            PreAuthAdjust preAuth=new PreAuthAdjust();
                            preAuth.setAmount(new JSONObject(payload).getString("amount"));
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
                            }, getEmvConfig(TransactionType.PREAUTHORIZATION_ADJUSTMENT,preAuth));
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

    //do cashAdvanced
    public void doCashAdvanced() {
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
                            }, getEmvConfig(TransactionType.CASH,new Cash()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }

    //do sale with cash back
    public void saleWithCashBack() {
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
                try {
                    Intent intent = new Intent();

                    SaleWithCashbackData saleWithCashbackData=new SaleWithCashbackData();
                    saleWithCashbackData.setCashBackAmount(new JSONObject(payload).getString("amount"));
                    saleWithCashbackData.setInitialAmount(new JSONObject(payload).getString("otherAmount"));

                    if(new JSONObject(payload).getString("type").equals("Sale with tip")){
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
                        }, getEmvConfig(TransactionType.CASHWITHTIP,saleWithCashbackData));
                        return;
                    }
                    if(new JSONObject(payload).getString("type").equals("Sale with cashback")){
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
                        }, getEmvConfig(TransactionType.CASHBACK,saleWithCashbackData));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }).start();
    }

    //do adjust of transaction
    public void doAdjust() {
        OtherData otherData = new OtherData(TransactionType.ADJUST);
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

    //after you get the invoice details u can finish the transaction
    public void finishAdjust(){
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
                            Adjust adjust=new Adjust();
                            adjust.setAmount(new JSONObject(payload).getString("amount"));
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
                            }, getEmvConfig(TransactionType.ADJUST,adjust));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }

    //Pre-authorize completion
    public void preAuthorizeCompletion() {
        OtherData otherData = new OtherData(TransactionType.PREAUTHORIZATION_COMPLETION_INQUIRY);
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

    //after the invoice is returned to the ui
    public void preAuthorizeCompletionAfterInvoice(){
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
                            PreAuthAdjust preAuth=new PreAuthAdjust();
                            preAuth.setAmount(new JSONObject(payload).getString("amount"));
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
                            }, getEmvConfig(TransactionType.PREAUTHORIZATION_COMPLETION,preAuth));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }

    //Pre-authorize complete cancellation
    public void preAuthorizeCompleteCancellation(){
        OtherData otherData = new OtherData(TransactionType.PREAUTHORIZATION_COMPLETION_CANCELLETION);
        try {
            otherData.setRrn(new JSONObject(payload).getString("invoiceNo") );
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

    //Pre-authorize complete cancellation after rrn is valid
    private void preAuthorizeCompleteCancellationAfter(){
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
                            PreAuthCompletionCancellation preAuthCompletionCancellation=new PreAuthCompletionCancellation();
                            preAuthCompletionCancellation.setAmount(new JSONObject(payload).getString("amount"));
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
                            }, getEmvConfig(TransactionType.PREAUTHORIZATION_COMPLETION_CANCELLETION,preAuthCompletionCancellation));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }
}
