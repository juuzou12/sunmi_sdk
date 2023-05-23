package co.ke.tracom.limited.services.sdktool_sumni.reports;

import android.content.Context;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ke.co.tracom.libsunmi.db.SaveDate;
import ke.co.tracom.libsunmi.reports.ReportsData;
import ke.co.tracom.libsunmi.reports.TotalReportData;

public class SunmiReports {
    Context that;

    public SunmiReports(Context that) {
        this.that = that;
    }

    //get the total reports from the database
    public void statementReports(String payload) {
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
    public static void getStatementsReport(String payload) {

    }

}
