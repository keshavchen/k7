package com.example.keshav.speechtotext;

import android.Manifest;
import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;


public class MainActivity extends AppCompatActivity {
    private final int SPEECH_RECOGNITION_CODE = 1;
    private TextView txtOutput;
    private Button btnMicrophone;
    private EditText txtFld;
    private Button txtButton;
    private EditText mInputMessageView;


    public static String internalPath; // internal storage path
    public static String fileName; // the file name

    static String trigger_sentence, message, date, time, intent_code,hours,minutes,minutes_only;


    LinearLayout layout;
    Socket socket; // socket object
    ScrollView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        try {
            socket = IO.socket("http://104.199.174.3:9000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    }
                });


            }

        }).on("reply", new Emitter.Listener() {

            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject j = new JSONObject((String) args[0]);

                            JSONObject temp = j.getJSONObject("result");
                            message = temp.getString("message");
                            hours = temp.getString("hours");
                            minutes = temp.getString("minutes");
                            minutes_only = temp.getString("minutes_only");

                            setAlarmClock(hours,minutes,minutes_only);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                });


            }

        });
        socket.connect();

        setContentView(R.layout.activity_main);
        btnMicrophone = (Button) findViewById(R.id.btn_mic);
        txtFld = (EditText) findViewById(R.id.TxtField);
        txtButton = (Button) findViewById(R.id.txtButton);
        layout = (LinearLayout) findViewById(R.id.LinearLayout);
        scrollView  = (ScrollView)findViewById(R.id.scrollView);


        btnMicrophone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                speechtotext();
            }
        });

        txtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = txtFld.getText().toString();
                socket.emit("chat", txt);
                Calpermission();
                messageHandler(txt, 1);



            }
        });
    }
        private void calEntry(){
            String eventdate;

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            final Calendar cal = Calendar.getInstance();
            try {

                cal.setTime(formatter.parse("2017/01/17 05:40"));
                eventdate = cal.get(Calendar.YEAR)+"/"+cal.get(Calendar.MONTH)+"/"+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE);
                //Log.e("Event date ", eventdate);
            } catch (Exception e) {
                Log.e("Catch ", "",e);
            }
            int eid;
            eid=((cal.get(Calendar.YEAR)*100)+(cal.get(Calendar.DAY_OF_MONTH)))*100+cal.get(Calendar.HOUR_OF_DAY);
            System.out.println(eid);

            ContentValues event = new ContentValues();
            event.put("calendar_id", 3);
            event.put("_id", eid);
            event.put("title", "mytitle2");
            event.put("description", "mydescription");
            event.put("eventLocation", "Event Location");
            event.put("eventTimezone", TimeZone.getDefault().getID());
            event.put("dtstart", cal.getTimeInMillis());
            event.put("dtend", cal.getTimeInMillis()+120*60*1000);
            event.put("hasAlarm", 0);



            Uri eventUri = getApplicationContext()
                    .getContentResolver()
                    .insert(Uri.parse("content://com.android.calendar/events"), event);
            Toast.makeText(this,"Event set",Toast.LENGTH_LONG).show();
        }
    private void Calpermission(){
        int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR=1;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CALENDAR},
                    MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
        }
        else calEntry();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    calEntry();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void setAlarmClock(String hour, String minute,String minutes_only) {



        int hours = Integer.parseInt(hour);

        int minutes = Integer.parseInt(minute);
        Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
        i.putExtra(AlarmClock.EXTRA_MESSAGE, "Set by Jarvis");
        i.putExtra(AlarmClock.EXTRA_HOUR, hours);
        i.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
        i.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        startActivity(i);

        messageHandler("Your Alarm has been set", 0);
    }


    private void messageHandler(String message, int person) {
        TextView t = new TextView(MainActivity.this);
        if (person == 1) {
            t.setBackgroundResource(R.drawable.rounded_corner);
        } else {
            t.setBackgroundResource(R.drawable.rounded_corner1);
        }
        t.setText(message);

        final float scale = getResources().getDisplayMetrics().density;
        int padding_5dp = (int) (5 * scale + 0.5f);
        int padding_20dp = (int) (15 * scale + 0.5f);
        int padding_50dp = (int) (50 * scale + 0.5f);
        t.setLayoutParams(new LinearLayout.LayoutParams(padding_50dp, padding_50dp));
        t.setPadding(padding_20dp, padding_20dp, padding_20dp, padding_20dp);

        t.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) t.getLayoutParams();
        if (person == 1) {
            layoutParams.gravity = Gravity.RIGHT;

        } else {
            layoutParams.gravity = Gravity.LEFT;

        }

        layoutParams.setMargins(0, 10, 0, 0);
        layout.addView(t);

        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        layout.invalidate();


    }


    private void speechtotext() {





        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak now..");

        try{
            startActivityForResult(intent,SPEECH_RECOGNITION_CODE);
        }
        catch(ActivityNotFoundException a){
            Toast.makeText(getApplicationContext(),"not supported",Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SPEECH_RECOGNITION_CODE) {
                if(resultCode == RESULT_OK && null!= data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = result.get(0);
                    socket.emit("chat",text);
                    messageHandler(text,1);

                }

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}

