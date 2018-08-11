package com.example.arturo.security;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.ContentValues.TAG;

public class StatsService extends Service {

    private static final int NOTIFICATION = 1;
    Thread t;
    private boolean running;
    private SQLiteDatabase db;
    private WifiManager wmgr;
    private BluetoothManager bm;
    private BluetoothAdapter ba;
    private ConnectivityManager cm;
    private String tag = "StatsService";
    public int intentos = 3;

    public static String forsent_db;

    public static boolean dbusing = false;


    public static String db_names[];
    public static int db_index = 0;
    public static int cont = 0;
    public static int upload = 1;
    public static String IMEINumber;
    public static String IMEINumberReduce;
    public static String current_db;

    public Object dbs[];


    SimpleDateFormat sdf;
    Calendar d;


    public boolean logSent = false;

    String packageNameByUsageStats = "";
    String classByUsageStats = "";
    private NotificationManager mNM;

    public StatsService() {
        IMEINumber = null;
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.

    }

    private Notification showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification

        PendingIntent contentIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)

                .setTicker("Security")  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Stats")  // the label of the entry
                .setContentText("Stats")  // the contents of the entry
                .setContentIntent(contentIntent)

                .build();

        // Send the notification.
        //mNM.notify(NOTIFICATION, notification);
        return notification;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        init();


        this.startForeground(2, showNotification());
//        Intent intent2 = new Intent(this,UpLoadDbService.class);
//        //intent2.putExtra("IMEINumber",new String[]{IMEINumber});
//        this.startService(intent2);


        wmgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        cm = (ConnectivityManager) getApplicationContext().getSystemService
                (Context.CONNECTIVITY_SERVICE);
        bm = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        ba = bm.getAdapter();


        sdf = new SimpleDateFormat("dd-MM-yyyy;HH:mm:ss");


        // prepareDb();


        running = true;
        Runnable r = new Runnable() {


            @Override
            public void run() {


                boolean yes = true;


                while (running) {


                    if (yes) {

                        if (cont > 50) {



                            if (lastReg() >  2000 ) {
                                prepareToSent();

                                db_index++;
                                current_db = db_names[db_index];

                            }
                            cont = 0;
                        }

                        if (upload % 200 == 0) {
                            sent2();
                            upload = 1;
                        }


                        String app[] = appInForeground();

                        String pkg = app[0];

                        String clas = app[1];


                        String mem = memUsage();


                        String cpu = cpuUsage();


                        String rxtx[] = getRxTx();
                        String rx = rxtx[0];
                        String tx = rxtx[1];

                        d = Calendar.getInstance();

                        String date;
                        date = sdf.format(d.getTime());

                        String networkInfo[] = getNetworkInfo();


                        insert(pkg,
                                clas,
                                mem,
                                cpu,
                                tx,
                                rx,
                                networkInfo,
                                date);

                        cont++;
                        upload++;

                        String ni = "";


                        for (int i = 0; i < networkInfo.length; i++)
                            ni += networkInfo[i] + ";";


                        Log.i("ROW", ":\n  " + pkg + ";" + clas + ";" + mem + ";" + cpu + ";" + tx + ";" + rx + ";" + ni + ";" + date);


                    } else {


                        SystemClock.sleep(1500);

                    }

                }
                stopSelf();

            }
        };

        t = new Thread(r);

        t.start();


        return START_STICKY;
    }


    public void init() {

        db_names = new String[15];

        IMEINumber = getUniqueIMEIId(this);
        IMEINumberReduce = IMEINumber.substring(0, 5);
        db_names[0] = IMEINumberReduce + "-1.db";
        db_names[1] = IMEINumberReduce + "-2.db";
        db_names[2] = IMEINumberReduce + "-3.db";
        db_names[3] = IMEINumberReduce + "-4.db";
        db_names[4] = IMEINumberReduce + "-5.db";
        db_names[5] = IMEINumberReduce + "-6.db";
        db_names[6] = IMEINumberReduce + "-7.db";
        db_names[7] = IMEINumberReduce + "-8.db";
        db_names[8] = IMEINumberReduce + "-9.db";
        db_names[9] = IMEINumberReduce + "-10.db";
        db_names[10] = IMEINumberReduce + "-11.db";
        db_names[11] = IMEINumberReduce + "-12.db";
        db_names[12] = IMEINumberReduce + "-13.db";
        db_names[13] = IMEINumberReduce + "-14.db";
        db_names[14] = IMEINumberReduce + "-15.db";
        Log.i("SERVICE", "IMEINumber : " + IMEINumberReduce);
        for (int i = 0; i < 15; i++)
            prepareStatsDb(db_names[i]);

        prepareUploadsDb();

        current_db = getCurrentDb();
        forsent_db = getDbForSent();


    }

    private void prepareStatsDb(String db_name) {


        db = openOrCreateDatabase(db_name, SQLiteDatabase.OPEN_READWRITE, null);

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
                    //+ "BLUETOOTH_PAIRED_DEVICE TEXT,"
                    + "BLUETOOTH_STATUS TEXT,"
                    + "DATE TEXT);";
            db.execSQL(CREATE_TABLE_LOG);
            Log.i("DBUTILS", " TABLE CREATED ON " + db_name);


        } catch (Exception e) {

            Log.i("DBUTILS", " TABLE NOT CREATED ON " + db_name);

        } finally {
            db.close();
        }

    }


    private void prepareUploadsDb() {


        db = openOrCreateDatabase("uploads", SQLiteDatabase.OPEN_READWRITE, null);

        try {


            final String CREATE_TABLE_UPLOADS = "CREATE TABLE IF NOT EXISTS uploads ("
                    + "ID INTEGER primary key AUTOINCREMENT,"
                    + "DB_NAME TEXT, "
                    + "COUNT INTEGER, "
                    + "SENT INTEGER, "
                    + "FOR_SENT INTEGER, "
                    + "CURRENT INTEGER, "
                    + "TIME TEXT, "
                    + "DATE TEXT);";
            db.execSQL(CREATE_TABLE_UPLOADS);
            Log.i("DBUTILS", " TABLE CREATED ON UPLOADS");


        } catch (Exception e) {

            Log.i("DBUTILS", " TABLE NOT CREATED ON UPLOADS ");

        } finally {
            db.close();
        }


        if (!isUploadsTablePrepared()) {


            int j = 0;
            for (int i = 0; i < 7; i++) {

                if (i == 0)
                    j = 1;

                else
                    j = 0;

                String insertQ =
                        "INSERT into UPLOADS " +
                                "('DB_NAME', " +
                                "'COUNT'," +
                                " 'SENT'," +
                                " 'FOR_SENT'," +
                                "'CURRENT'," +
                                " 'TIME'," +
                                " 'DATE') " +
                                "values ('" + db_names[i] +
                                "', '0'" +
                                ",'0'" +
                                ",'0'" +
                                ", '" + j + "'" +
                                ",'" + String.valueOf(Calendar.getInstance().getTimeInMillis()) + "'" +
                                ",'" + Calendar.getInstance().getTime().toString() +
                                "');";

                db = openOrCreateDatabase("uploads", SQLiteDatabase.OPEN_READWRITE, null);

                try {

                    db.execSQL(insertQ);
                    Log.i("DBUTILS", " INSERT INTO UPLOADS OK  ");


                } catch (Exception e) {

                    Log.i("DBUTILS", "INSERT INTO UPLOADS FAIL  ");

                } finally {
                    db.close();
                }


            }
        }

    }

    public boolean isUploadsTablePrepared() {


        String selectQuery = "SELECT  *  FROM uploads ;";
        db = openOrCreateDatabase("uploads", SQLiteDatabase.OPEN_READWRITE, null);
        if (db.isOpen()) {

            Cursor cursor = db.rawQuery(selectQuery, null);
            try {
                if (cursor.getCount() > 1) {


                    Log.i("St", "isUploadsTablePrepared: " + cursor.getCount());
                    db.close();
                    return true;


                }
                Log.i("DBUTILS", " INSERT INTO UPLOADS OK  ");


            } catch (Exception e) {

                Log.i("DBUTILS", "INSERT INTO UPLOADS FAIL  ");

            } finally {
                db.close();
            }

        }
        return false;


    }


    public String getCurrentDb() {


        String selectQuery = "SELECT   ID, DB_NAME  FROM uploads WHERE CURRENT = 1 AND FOR_SENT = 0";

        db = openOrCreateDatabase("uploads", SQLiteDatabase.OPEN_READWRITE, null);
        while (db.inTransaction()) ;
        Cursor cursor = db.rawQuery(selectQuery, null);
        String current = "";


        try {

            if (cursor.moveToFirst()) {

                db_index = cursor.getInt(0);
                db_index--;
                current = cursor.getString(1);
                Log.i(TAG, "getCurrentDb: " + current);


            }


        } catch (Exception e) {
            Log.i(TAG, "getCurrentDb: ¡¡¡¡ SELECT FAILED !!!!");
        } finally {
            db.close();
        }

        return current;

    }

    public String getDbForSent() {


        String selectQuery = "SELECT   DB_NAME  FROM uploads WHERE FOR_SENT = 1 AND SENT = 0; ";

        db = openOrCreateDatabase("uploads", SQLiteDatabase.OPEN_READWRITE, null);
        while (db.inTransaction()) ;
        Cursor cursor = db.rawQuery(selectQuery, null);
        String for_sent = null;


        try {

            if (cursor.moveToFirst()) {


                for_sent = cursor.getString(0);
                Log.i(TAG, "getDbForSend: " + for_sent);


            }


        } catch (Exception e) {
            Log.i(TAG, "getDbForSend: ¡¡¡¡ SELECT FAILED !!!!");
        } finally {
            db.close();
        }

        return for_sent;

    }

    private void insert(
            String pkg,
            String clas,
            String mem,
            String cpu,
            String rx,
            String tx,
            String networkInfo[],
            String date

    ) {
        db = openOrCreateDatabase(current_db, SQLiteDatabase.OPEN_READWRITE, null);

        if (db.isOpen()) {
            String insertQ = "INSERT into log " +
                    "('PACKG_NAME', " +
                    "'CLASS_NAME'," +
                    " 'MEM'," +
                    "'CPU'," +
                    " 'RX'," +
                    "'TX'," +
                    "'WIFI_ADAPTER_STATUS'," +
                    "'WIFI_AP_NAME'," +
                    "'WIFI_STATUS'," +
                    "'MOBILE_ADAPTER_STATUS'," +
                    "'MOBILE_OPERATOR_NAME'," +
                    "'MOBILE_SIGNAL_NAME'," +
                    "'MOBILE_STATUS'," +
                    "'BLUETOOTH_ADAPTER_STATUS'," +
                    //"'BLUETOOTH_PAIRED_DEVICE'," +
                    "'BLUETOOTH_STATUS'," +

                    " 'DATE') " +
                    "values ('" + pkg +
                    "' ,'" + clas +
                    "' ,'" + mem +
                    "' ,'" + cpu +
                    "' ,'" + rx +
                    "' ,'" + tx +
                    "' ,'" + networkInfo[0] +
                    "' ,'" + networkInfo[1] +
                    //"' ,'" +networkInfo[2]+
                    "' ,'" + networkInfo[3] +
                    "' ,'" + networkInfo[4] +
                    "' ,'" + networkInfo[5] +
                    "' ,'" + networkInfo[6] +
                    "' ,'" + networkInfo[7] +
                    "' ,'" + networkInfo[8] +
                    // "' ,'" +networkInfo[9]+
                    // "' ,'" +networkInfo[10]+
                    "' ,'" + networkInfo[11] +
                    "' ,'" + date +
                    "');";

            try {


                db.execSQL(insertQ);
                Log.i("StatsService", " INSERT OK  ");


            } catch (Exception e) {

                Log.i("StatsService", " INSERT FAIL  ");

            } finally {
                db.close();

            }
        } else
            Log.i("StatsService", " DATABASE NOT OPEN  ");


    }

    private void prepareToSent(


    ) {


        db = openOrCreateDatabase("uploads", SQLiteDatabase.OPEN_READWRITE, null);

        if (db.isOpen()) {
            String insertQ = "UPDATE  uploads SET" +


                    " FOR_SENT = '1' " +


                    " WHERE DB_NAME = '" + current_db + "';";

            while (db.inTransaction()) ;
            try {

                db.execSQL(insertQ);
                Log.i("StatsService", " prepareToSend():  INSERT OK ON UPLOADS CURRENT_DB:"+ current_db);


            } catch (Exception e) {

                Log.i("StatsService", " prepareToSend():  INSERT FAIL  ");

            } finally {
                db.close();

            }


        } else
            Log.i("StatsService", "  prepareToSend(): DATABASE NOT OPEN  ");


    }

    public int lastReg() {
        String selectQuery = "SELECT  ID  FROM log order by ID desc limit 1 ; ";
        String row = "";
        int last = 0;
        db = openOrCreateDatabase(current_db, SQLiteDatabase.OPEN_READWRITE, null);
        while (db.inTransaction()) ;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        try {
            if (cursor.moveToFirst() && cursor.getCount() > 0) {


                last = cursor.getInt(0);




            }
        } catch (Exception e) {
            Log.i("StatsService", "¡¡¡¡ SELECT FAILED !!!!");
        } finally {
            db.close();
        }

        return last;

    }

    public String[] appInForeground() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.i(TAG, "ENTER");

            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            final long INTERVAL = 10000;
            final long end = System.currentTimeMillis();
            final long begin = end - INTERVAL;

            final UsageEvents usageEvents = mUsageStatsManager.queryEvents(begin, end);

            if (usageEvents.hasNextEvent()) {
                while (usageEvents.hasNextEvent()) {
                    UsageEvents.Event event = new UsageEvents.Event();
                    usageEvents.getNextEvent(event);

                    if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {

                        packageNameByUsageStats = event.getPackageName();
                        classByUsageStats = event.getClassName();


                    }
                }


            }

        }
        return new String[]{packageNameByUsageStats, classByUsageStats};
    }

    public String[] getRxTx() {


        long mStartRX1 = 0;
        long mStartTX1 = 0;
        long mStartRX2 = 0;
        long mStartTX2 = 0;

        mStartRX1 = TrafficStats.getTotalRxBytes() / 1024;
        mStartTX1 = TrafficStats.getTotalTxBytes() / 1024;


        long lastTimeStamp = System.currentTimeMillis();


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long nowTimeStamp = System.currentTimeMillis();

        mStartRX2 = TrafficStats.getTotalRxBytes() / 1024;
        mStartTX2 = TrafficStats.getTotalTxBytes() / 1024;

        String speedRx = "" + String.valueOf(
                ((mStartRX2 - mStartRX1) * 1000) / (nowTimeStamp - lastTimeStamp));
        String speedTx = "" + String.valueOf(
                ((mStartTX2 - mStartTX1) * 1000) / (nowTimeStamp - lastTimeStamp));


        String speedTotal = "Rx: " + speedRx + " Kb/s " + "   Tx:" + speedTx + " Kb/s ";

        return new String[]{speedRx, speedTx};

    }

    public String[] getNetworkInfo() {

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService
                (Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni[] = new NetworkInfo[3];
        ni[0] = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        ni[1] = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        ni[2] = cm.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);

        String info[] = new String[12];


        String out = "";
        for (NetworkInfo nix : ni) {


            if (nix != null) {
                switch (nix.getType()) {

                    case ConnectivityManager.TYPE_WIFI:

                        WifiManager wmgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (wmgr.isWifiEnabled())
                            info[0] = "WIFI_IS_ON";
                        else
                            info[0] = "WIFI_IS_OFF";

                        info[1] = nix.getExtraInfo();
                        info[2] = nix.getSubtypeName();
                        info[3] = nix.getDetailedState().name();

                        break;

                    case ConnectivityManager.TYPE_MOBILE:

                        TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                        if (nix.getSubtypeName() != null)
                            info[4] = "MOBILE_IS_ON";
                        else
                            info[4] = "MOBILE_IS_OFF";

                        info[5] = nix.getExtraInfo();
                        info[6] = nix.getSubtypeName();
                        info[7] = nix.getDetailedState().name();
                        break;

                    case ConnectivityManager.TYPE_BLUETOOTH:

                        BluetoothManager bm = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
                        BluetoothAdapter ba = bm.getAdapter();
                        if (ba.isEnabled())
                            info[8] = "BLUETOOTH_IS_ON";
                        else
                            info[8] = "BLUETOOTH_IS_OFF";

                        info[9] = nix.getExtraInfo();
                        info[10] = nix.getSubtypeName();
                        info[11] = nix.getDetailedState().name();
                        break;


                }



            }
        }

        return info;

    }


    private String memUsage() {

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double percentAvail = 100.0 - (mi.availMem / (double) mi.totalMem * 100.0);

        return String.format("%.2f", percentAvail);
    }

    private String cpuUsage() {

        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" +");  // Split on one or more spaces
            /*String l ="";

            for (int i = 0; i<5; i++)
                reader.readLine();
            Log.i("TOP: ", reader.readLine());
            Log.i("TOP: ", reader.readLine());
            Log.i("TOP: ", reader.readLine());*/


            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }

            reader.seek(0);
            load = reader.readLine();

            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            float cpuUsage = 100.0f * (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

            String cpuUsageString = String.format("%.2f", cpuUsage);


            return cpuUsageString;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return "";
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
        Intent broadcastIntent = new Intent("Restart");
        sendBroadcast(broadcastIntent);

        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    public void sendLog() {

        try {
            if (cm != null) {
                if (cm.getActiveNetworkInfo().isConnected()) {
                    Log.i("FTP", "sendLog");
                    FtpAsyncTask ftp = new FtpAsyncTask(StatsService.forsent_db);
                    Object o[] = new Object[1];
                    o[0] = this.getApplicationContext();
                    int count = getDbCount();

                    if (count < intentos) {
                        updateUploads(count, 0, 1);
                        ftp.execute(o);
                    } else {
                        updateUploads(count, 1, 0);


                    }
                }
            }
        }
        catch (Exception e){

        }

    }


        public int getDbCount () {


            String selectQuery = "SELECT  COUNT  FROM uploads WHERE DB_NAME = '" + StatsService.forsent_db + "' ; ";

            db = this.getApplicationContext().openOrCreateDatabase("uploads", SQLiteDatabase.OPEN_READWRITE, null);

            Cursor cursor = db.rawQuery(selectQuery, null);
            int count = 0;


            try {

                if (cursor.moveToFirst()) {


                    count = cursor.getInt(0);
                    Log.i(TAG, "count: " + count);


                }


            } catch (Exception e) {
                Log.i("UpLoadService", "¡¡¡¡ SELECT FAILED !!!!");
            } finally {
                db.close();
            }

            return count;

        }


        public void sent2 () {


            String selectQuery = "SELECT   *  FROM uploads WHERE FOR_SENT = 1 AND SENT = 0; ";

            db = this.getApplicationContext().openOrCreateDatabase("uploads", SQLiteDatabase.OPEN_READWRITE, null);
            while (db.inTransaction()) ;
            Cursor cursor = db.rawQuery(selectQuery, null);

            long date2l = Calendar.getInstance().getTimeInMillis();

            try {
                if (cursor.getCount() != 0) {

                    if (cursor.moveToFirst()) {
                        String row = "";
                        for (int i = 0; i < cursor.getColumnCount(); i++)
                            row += cursor.getString(i) + "|";


                        Log.i("UpLoadService", "UPLOADS ROW: " + row);
                        long time = 20 * 60 * 1000;
                        String date1s = cursor.getString(5);
                        Log.i(TAG, "date string: " + date1s);
                        long date1l = Long.parseLong(date1s);
                        Log.i(TAG, "date to long: " + date1l);

                        Log.i(TAG, "date2l - date1l: " + String.valueOf(date2l - date1l));
                        if ((date2l - date1l) > time) {

                            StatsService.forsent_db = cursor.getString(1);
                            sendLog();


                        }


                    }
                }

            } catch (Exception e) {
                Log.i("UpLoadService", "¡¡¡¡ SELECT FAILED !!!!");
            } finally {
                db.close();
            }


        }
        private void updateUploads ( int count, int sent, int for_sent ){

            count++;
            String time = String.valueOf(Calendar.getInstance().getTimeInMillis());
            String date = Calendar.getInstance().getTime().toString();

            db = openOrCreateDatabase("uploads", SQLiteDatabase.OPEN_READWRITE, null);

            if (db.isOpen()) {
                String insertQ = "UPDATE  uploads SET" +

                        " TIME = " + "'" + time + "'" + "," +
                        " COUNT = '" + count + "'," +
                        " SENT = '" + sent + "'," +
                        " FOR_SENT = '" + for_sent + "'," +
                        " DATE = " + "'" + date + "'" +

                        " WHERE DB_NAME = '" + StatsService.forsent_db + "';";

                Log.i(TAG, " QUERY -->  " + insertQ);
                while (dbusing) ;
                try {
                    dbusing = true;

                    db.execSQL(insertQ);
                    Log.i(TAG, " UPDATE OK  ");


                } catch (Exception e) {

                    Log.i("UpLoadService", " UPDATE FAIL  ");

                } finally {
                    db.close();
                    dbusing = false;
                }
            } else
                Log.i("UpLoadService", " DATABASE NOT OPEN  ");


        }


    }


