package co.ke.tracom.limited.services.sdktool_sumni.reports;

import android.content.Intent;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ke.co.tracom.libsunmi.db.SaveDate;
import ke.co.tracom.libsunmi.reports.TotalReportData;

public class SunmiReports {
    //get the total reports from the database
    public static void reportTotalReports(String payload){
        List<TotalReportData> reports = new ArrayList<>();
        reports= SaveDate.totalsReport(payload);
        Gson gson = new Gson();
        //run on a thread to delay for 1000mill then finish
        List<TotalReportData> finalReports = reports;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    Intent in=new Intent();
                    in.putExtra("resp",gson.toJson(finalReports.toString()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //getting the statements report from the database
    public static void getStatementsReport(String payload){

    }

}
