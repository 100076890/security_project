package com.example.arturo.security;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DataUiService extends Service {

    Thread t;
    private boolean running;

    public String IMEINumber;

    public boolean logSent = true;
    public String db_name = "uploads", IMEINumberReduce;
    String[] db_names;
    private SQLiteDatabase db;
    private final Handler handler = new Handler();
    Intent intent;
    public static final String BROADCAST_ACTION = "com.arturo.example.security.DataUiService";
    private ConnectivityManager cm;
    int db_index = 0;
    public String TAG = "DataUiService";



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        cm = (ConnectivityManager) getApplicationContext().getSystemService
                (Context.CONNECTIVITY_SERVICE);

         db_names = new String[7];





        db_index = 0;


        //prepareTable2();

        SystemClock.sleep(200*10);

        running = true;
        Runnable r = new Runnable() {


            @Override
            public void run() {




                while (running) {

                   /* Date currentTime = Calendar.getInstance().getTime();
                    String day1 = currentTime.toString().split(" +")[2];

                    SystemClock.sleep(200*10);

                     currentTime = Calendar.getInstance().getTime();
                    String day2 = currentTime.toString().split(" +")[2];


                    Log.i("SERVICE 2 ", "day1: "+day1 +"   day2: "+day2+"   compare: "+day1.compareTo(day2));
                    if(day1.compareTo(day2) != 0){



                        }
                        */





                    SystemClock.sleep(200*10);



                    // if (num>44000) running = false;
                    //Log.i("\nFTP SERVICE ","last reg : "+num );

                    sendInfo();




                }
                stopSelf();

            }
        };

        t = new Thread(r);

        t.start();


        return START_STICKY;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public void sendInfo(){

        boolean myService = isMyServiceRunning(StatsService.class);
        boolean dataUiService = isMyServiceRunning(DataUiService.class);
        //boolean uploadService = isMyServiceRunning(UpLoadDbService.class);

        intent = new Intent(BROADCAST_ACTION);
        String num = numRegs();

        Log.i(TAG,"last reg : "+num );
        if(myService)
            intent.putExtra("service1", "Ok");
        else
            intent.putExtra("service1", "BAD!");
        /*if(uploadService)
            intent.putExtra("service2", "Ok");
        else
            intent.putExtra("service2", "BAD!");
        */if(dataUiService)
            intent.putExtra("service3", "Ok");
        else
            intent.putExtra("service3", "BAD!");

        intent.putExtra("last_int", Integer.valueOf(num));
        intent.putExtra("last_string",num);

        String row = showLastReg();
        if (row == null){
            intent.putExtra("row", "ERROR");
        }
        else if(row.length() < 2)
            intent.putExtra("row", "WARNING");
        else
            intent.putExtra("row", "OK");

        if(StatsService.current_db != null)
            intent.putExtra("current_db", StatsService.current_db);

        sendBroadcast(intent);
    }

    public String getUniqueIMEIId(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            @SuppressLint("MissingPermission") String imei = telephonyManager.getDeviceId();
            Log.e("imei", "=" + imei);
            if (imei != null && !imei.isEmpty()) {
                return imei;
            } else {
                return Build.SERIAL;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "not_found";
    }

    public String numRegs(){

        String numreg = "0";

        String selectQuery = "SELECT ID FROM log order by ID desc limit 1 ; ";
        //Log.i("UPLOADSERVICE", selectQuery);

        db = this.getApplicationContext().openOrCreateDatabase(StatsService.current_db, SQLiteDatabase.OPEN_READWRITE, null);

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        try {


            if (cursor.moveToFirst() && cursor.getCount() > 0) {


                numreg =  cursor.getString(0);

                Log.i(TAG, cursor.getString(0));





            }
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            Log.i("DataUiService", "¡¡¡¡ SELECT FAILED !!!!");
        }
        finally {
            db.close();
        }
        // close db connection



        return numreg;


    }
    public String showLastReg() {
        String selectQuery = "SELECT  *  FROM log order by ID desc limit 1 ; ";
        String row = "";
        db = openOrCreateDatabase(StatsService.current_db, SQLiteDatabase.OPEN_READWRITE, null);
        while(db.inTransaction());
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            try {
                if (cursor.moveToFirst() && cursor.getCount() > 0 ) {

                    //String  names[] = cursor.getColumnNames();
                    //Log.i("SQLITE", "ROW SIZE: "+cursor.getColumnCount()+": " + names[0]+"\t"+names[1]+"\t"+names[2]+"\t"+names[3]);


                    /*row += cursor.getString(0) + ", ";*/
                    row += cursor.getString(1) + ", ";
                   /* row += cursor.getString(2) + ", ";
                    row += cursor.getString(3) + ", ";
                    row += cursor.getString(4) + ", ";
                    row += cursor.getString(5) + ", ";
                    row += cursor.getString(6) + ", ";
                    row += cursor.getString(7) + ", ";
                    row += cursor.getString(8) + ", ";
                    row += cursor.getString(9) + ", ";
                    row += cursor.getString(10) + ", ";
                    row += cursor.getString(11) + ", ";
                    row += cursor.getString(12) + ", ";
                    row += cursor.getString(13) + ", ";
                    row += cursor.getString(14) + ", ";
                    row += cursor.getString(15) + ", ";
                    row += cursor.getString(16) + ", ";
                    row += cursor.getString(17);*/
               /* row += cursor.getString(8) + ", ";
                row += cursor.getString(9) + ", ";
                row += cursor.getString(10) + ", ";
                row += cursor.getString(11);*/
                    //Log.i("SQLITE", "ROW: " + row);
                    //Toast.makeText(this, "Last row: " + row, Toast.LENGTH_SHORT).show();


                }
            } catch (Exception e) {
                Log.i("DataUiService", "¡¡¡¡ SELECT FAILED !!!!");
            }
            finally {
                db.close();
            }

        return row;

    }



    @Override
    public void onDestroy() {
        //shoRegs();
        running = false;
        try {
            //shoRegs();
            //writer.close();
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();



    }


}
