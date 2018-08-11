package com.example.arturo.security;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    String msg = "Android : ";
    private static final int INI = 100;
    private static final String[] RD = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private SQLiteDatabase db;
    private static final String[] WR = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String[] READ_PHONE_STATE = {Manifest.permission.READ_PHONE_STATE};
    private static final String[] USAGE_STATS = {Manifest.permission.PACKAGE_USAGE_STATS};
    String IMEINumber;
    String table;
    String tag = "SECURITY: ";
   // DBUtils u = new DBUtils(this, IMEINumber);
    private boolean dab_ready = false;




    public void showDbs(View view) {


        Context c = getApplicationContext();
        String dbs[] = c.databaseList();

        if( dbs.length > 0) {

            String out = "";

            for (int i = 0; i < dbs.length; i++) {

                out += "Database [ " + dbs[i] + " ]\n";
                Log.i(tag,  dbs[i]);

            }

            Toast.makeText(this, out, Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "No database for this app", Toast.LENGTH_SHORT).show();



    }
    /*private void prepareDb() {


        db = openOrCreateDatabase(IMEINumber, SQLiteDatabase.OPEN_READWRITE, null          );
        try {

            final String CREATE_TABLE_LOG = "CREATE TABLE IF NOT EXISTS log ("
                    + "ID INTEGER primary key AUTOINCREMENT,"
                    + "PACKG_NAME TEXT,"
                    + "CLASS_NAME TEXT,"
                    + "MEM TEXT,"
                    + "CPU TEXT,"
                    + "TX TEXT,"
                    + "RX TEXT,"
                    + "WIFI_ADAPTER_STATUS TEXT,"
                    + "WIFI_AP_NAME TEXT,"
                    + "WIFI_STATUS TEXT,"
                    + "MOBILE_ADAPTER_STATUS TEXT,"
                    + "MOBILE_OPERATOR_NAME TEXT,"
                    + "MOBILE_SIGNAL_NAME TEXT,"
                    + "MOBILE_STATUS TEXT,"
                    + "BLUETOOTH_ADAPTER_STATUS TEXT,"
                    + "BLUETOOTH_PAIRED_DEVICE TEXT,"
                    + "BLUETOOTH_STATUS TEXT,"
                    + "DATE TEXT);";
            db.execSQL(CREATE_TABLE_LOG);
            Log.i("SQLITE", " TABLE CREATED  " );
            final String CREATE_TABLE_UPLOADS = "CREATE TABLE IF NOT EXISTS uploads ("
                    + "ID INTEGER primary key AUTOINCREMENT,"
                    + "DATE TEXT);";
            db.execSQL(CREATE_TABLE_UPLOADS);
            final String CREATE_TABLE_UPLOADS2 = "CREATE TABLE IF NOT EXISTS uploads2 ("
                    + "ID INTEGER primary key AUTOINCREMENT,"
                    +"TIME TEXT, "
                    +"DATE TEXT);";
            db.execSQL(CREATE_TABLE_UPLOADS2);
            Log.i("UpLoadService", " TABLE CREATED  " );

            db.close();

        } catch (Exception e) {

            Log.i("SQLITE", " TABLE NOT CREATED  " );

        }

    }*/

    public void deleteDb(View view) {


       Context c = getApplicationContext();
       String dbs[] = c.databaseList();

       if( dbs.length > 0) {

           for (int i = 0; i < dbs.length; i++) {

               boolean deleted = c.deleteDatabase(dbs[i]);
               if (deleted) {
                   Toast.makeText(this, "Database [" + dbs[i] + "] deleted", Toast.LENGTH_SHORT).show();
                   Log.i(tag, "Database [" + dbs[i] + "] deleted");
               }
               else{
                   Log.i(tag, "Database [" + dbs[i] + "] can't deleted");
                   Toast.makeText(this, "Database [" + dbs[i] + "] can't deleted", Toast.LENGTH_SHORT).show();

               }
               }


       }
       else
           Toast.makeText(this, "No database for this app", Toast.LENGTH_SHORT).show();



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

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        requestPermission();





        servicesAreRunning();

        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);

        pb.setMax(2*60*24);
        pb.setProgress(0);





        table = "log";

        Log.d(msg, "The onCreate() event");

    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    public void servicesAreRunning(){

        boolean myService = isMyServiceRunning(StatsService.class);
        //boolean uploadService = isMyServiceRunning(UpLoadDbService.class);
        boolean dataUiService = isMyServiceRunning(DataUiService.class);
        Log.i(tag, "StatsService is running? :"+myService);
        //Log.i(tag, "UpLoadDbService is running? :"+uploadService);
        Log.i(tag, "dataUiService is running? :"+dataUiService);

        /*if ( myService && uploadService && dataUiService){//&& service3){
            Button b = (Button) findViewById(R.id.buttonStart);
            b.setEnabled(false);
           *//* Button d = (Button) findViewById(R.id.buttonStop);
            d.setEnabled(true);*//*

        }
        else if ( !myService || !uploadService || !dataUiService ){
            Button b = (Button) findViewById(R.id.buttonStart);
            b.setEnabled(true);
          *//*  Button d = (Button) findViewById(R.id.buttonStop);
            d.setEnabled(false);*//*
        }*/

        if ( myService  && dataUiService){//&& service3){
            Button b = (Button) findViewById(R.id.buttonStart);
            b.setEnabled(false);
           /* Button d = (Button) findViewById(R.id.buttonStop);
            d.setEnabled(true);*/

        }
        else if ( !myService  || !dataUiService ){
            Button b = (Button) findViewById(R.id.buttonStart);
            b.setEnabled(true);
          /*  Button d = (Button) findViewById(R.id.buttonStop);
            d.setEnabled(false);*/
        }

    }



    @Override
    public void onResume() {
        super.onResume();


        registerReceiver(broadcastReceiver, new IntentFilter(DataUiService.BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);

    }

    public void start(View view) {
        if(!hasPermission())
        requestPermission();


            Intent intent = new Intent(this,StatsService.class);
            //intent.putExtra("DBstats",dbs);
            this.startService(intent);


        /*    Intent intent2 = new Intent(this,UpLoadDbService.class);
            //intent2.putExtra("IMEINumber",new String[]{IMEINumber});
            this.startService(intent2);*/


            Intent intent3 = new Intent(this,DataUiService.class);
            //intent3.putExtra("IMEINumber",new String[]{IMEINumber});
            this.startService(intent3);



      


    }

    // Method to stop the service
    public void stop(View view) {



        this.stopService(new Intent(getBaseContext(), StatsService.class));
        //this.stopService(new Intent(getBaseContext(), UpLoadDbService.class));
        this.stopService(new Intent(getBaseContext(), DataUiService.class));

        servicesAreRunning();


    }

    public void closeApp(View view) {
        /*
        if (service1){
            stopService(new Intent(getBaseContext(), StatsService.class));
            service1 = false;

        }
        if (service2){

            stopService(new Intent(getBaseContext(), UpLoadDbService.class));
            service2 = false;
        }

        /*if (service3){

            stopService(new Intent(getBaseContext(), NetworkService.class));
            service3 = false;
        }*/
        /*
        if (!service1 && !service2) // && !service3)
            finish();
        //System.exit(0);*/
    }


    public void fillStats() {
        if (hasPermission()) {
            //getStats();
        } else {
            requestPermission();
        }
    }


    private void requestPermission() {

        if (!hasPermission()) {
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 4);

        }
        else {

            Log.i(tag, "permission USAGE STATS ya concedido");
        }

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)==
                    PackageManager.PERMISSION_GRANTED) {
                Log.i("READ_PHONE_STATE  ", "GRANTED");
                // do something cool
            }
            else{
                requestPermissions( READ_PHONE_STATE, 5);
                Log.i("READ_PHONE_STATE  ", "REQUEST");
            }

        }
        else{


            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)==
                PackageManager.PERMISSION_GRANTED) {
            }
            else{
                ActivityCompat.requestPermissions(this, READ_PHONE_STATE, 5);
            }

        }
        IMEINumber = getUniqueIMEIId(this)+".db";
/*
        ActivityCompat.requestPermissions(this, US, 3);
        Toast.makeText(this, "US", Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(this, RD, 1);
        Toast.makeText(this, "RD", Toast.LENGTH_SHORT).show();*/

    }

    private boolean hasPermission() {

        boolean granted = false;
            try {
                PackageManager packageManager = getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
                AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                int mode = 0;
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                            applicationInfo.uid, applicationInfo.packageName);
                }
                return (mode == AppOpsManager.MODE_ALLOWED);

            } catch (PackageManager.NameNotFoundException e) {
                //Toast.makeText(this, "permission USAGE STATS "+granted, Toast.LENGTH_SHORT).show();
                Log.i(tag, "permission USAGE STATS :"+granted);
                return granted;
            }



    }

   /* public void showRegs(View view) {
        String selectQuery = "SELECT   *  FROM "+table+"  ; ";

        db = this.getApplicationContext().openOrCreateDatabase(IMEINumber, SQLiteDatabase.OPEN_READWRITE, null);
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        try {
            if (cursor.moveToFirst()) {

                String names[] = cursor.getColumnNames();
                Log.i("SQLITE", "ROW SIZE: " + cursor.getColumnCount() );

                do {
                    String row = "";
                    row += cursor.getString(0) + ", ";
                    row += cursor.getString(1) + ", ";
                    row += cursor.getString(2) + ", ";
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

                    row += cursor.getString(16) ;
               *//* row += cursor.getString(8) + ", ";
                row += cursor.getString(9) + ", ";
                row += cursor.getString(10) + ", ";
                row += cursor.getString(11);*//*
                    Log.i("SQLITE", "ROW: " + row);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.i("SQLITE", "¡¡¡¡ SELECT FAILED !!!!");
        }
        // close db connection
        db.close();

    }

    public void showLastReg() {
        String selectQuery = "SELECT  *  FROM log order by ID desc limit 1 ; ";

        db = openOrCreateDatabase(IMEINumber, SQLiteDatabase.OPEN_READWRITE, null);
        if(db.isOpen()) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            try {
                if (cursor.moveToFirst()) {

                    //String  names[] = cursor.getColumnNames();
                    //Log.i("SQLITE", "ROW SIZE: "+cursor.getColumnCount()+": " + names[0]+"\t"+names[1]+"\t"+names[2]+"\t"+names[3]);

                    String row = "";
                    row += cursor.getString(0) + ", ";
                    row += cursor.getString(1) + ", ";
                    row += cursor.getString(2) + ", ";
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

                    row += cursor.getString(16);
               *//* row += cursor.getString(8) + ", ";
                row += cursor.getString(9) + ", ";
                row += cursor.getString(10) + ", ";
                row += cursor.getString(11);*//*
                    //Log.i("SQLITE", "ROW: " + row);
                    //Toast.makeText(this, "Last row: " + row, Toast.LENGTH_SHORT).show();

                    TextView rowView = (TextView) findViewById(R.id.row);
                    if (!row.contains("WIFI_IS_ON")) {
                        TextView status = (TextView) findViewById(R.id.status_result);

                        status.setText("warning");
                        //status.setTextColor(123);
                    }
                    rowView.setText(row);
                }
            } catch (Exception e) {
                Log.i("SQLITE", "¡¡¡¡ SELECT FAILED !!!!");
            }
            // close db connection
            db.close();
        }

    }*/













    private void updateUI(Intent intent) {





       // String lastreg = intent.getStringExtra("lastreg");
       int last_pb = intent.getIntExtra("last_int",0);
       String numreg = intent.getStringExtra("last_string");
        String status = intent.getStringExtra("status");
        String s1 = intent.getStringExtra("service1");
       // String s2 = "ok"; //intent.getStringExtra("service2");
        String s3 = intent.getStringExtra("service3");

        String row = intent.getStringExtra("row");

        String current = intent.getStringExtra("current_db");
       // txtDateTime.setText(lastreg);

        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);

        try{
            pb.setProgress(last_pb);
        }
        catch (Exception e){

        }


        TextView stat= (TextView) findViewById(R.id.status_result);

        stat.setText(status);

        TextView last = (TextView) findViewById(R.id.last_result);

        last.setText(String.valueOf(numreg));



        TextView service1= (TextView) findViewById(R.id.service1_result);

        service1.setText(s1);

        TextView service2= (TextView) findViewById(R.id.service2_result);

        service2.setText(s3);

        /*TextView service3= (TextView) findViewById(R.id.service3_result);

        service3.setText(s3);*/


        TextView rowView = (TextView) findViewById(R.id.row_result);
        if (current != null)
            rowView.setText(current);
        if (row != null)
            stat.setText(row);


        servicesAreRunning();


    }

    public String getUniqueIMEIId(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.requestPermissions(new String[] {Manifest.permission.READ_PHONE_STATE}, 6);
                }
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "";
            }
            String imei = telephonyManager.getDeviceId();
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


    @Override
    protected void onDestroy() {
        this.stopService(new Intent(getBaseContext(), DataUiService.class));
        super.onDestroy();


    }


}
