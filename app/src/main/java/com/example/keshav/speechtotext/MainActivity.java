package com.example.keshav.speechtotext;

import android.Manifest;
import android.app.ActionBar;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
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
import android.widget.ProgressBar;
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
    private final int CAL_EVENT_RECOGNITION_CODE=2;
    private TextView txtOutput;
    private Button btnMicrophone;
    private EditText txtFld;
    private Button txtButton;
    private EditText mInputMessageView;


    public static String internalPath; // internal storage path
    public static String fileName; // the file name

    static String trigger_sentence, message, date, time,hours,minutes,minutes_only,title;
    int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR=1;
    int intent_code;

    ProgressBar pbar;
    LinearLayout layout;
    Socket socket; // socket object
    ScrollView scrollView;
    int incomplete_flag = 0;
    String buffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            finish();
            return;
        }
        //Getting Calendar write permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CALENDAR},
                    MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
        }


        super.onCreate(savedInstanceState);

        SocketHandler.setSocket();
        socket = SocketHandler.getSocket();


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
                        removeLoadingIcon();
                        try {
                            JSONObject j = new JSONObject((String) args[0]);

                            JSONObject temp = j.getJSONObject("result");
                            intent_code = temp.getInt("intent_code");
                            switch(intent_code) {
                                case 1: {
                                    message = temp.getString("message");
                                    hours = temp.getString("hours");
                                    minutes = temp.getString("minutes");
                                    minutes_only = temp.getString("minutes_only");
                                    setAlarmClock(hours, minutes, minutes_only);
                                }break;
                                case 2: {
                                    //date = temp.getString("date");
                                    //time = temp.getString("time");
                                    //title = temp.getString("desc");
                                    Intent CE= new Intent(MainActivity.this,CalendarEvent.class);
                                    CE.putExtra("json",temp.toString());


                                    startActivityForResult(CE,CAL_EVENT_RECOGNITION_CODE);

                                }
                            }


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
                txtFld.setText("");
                if(incomplete_flag==0){
                    socket.emit("chat", txt);

                }else{
                    requestDayandTime(txt);
                }

                messageHandler(txt, 1);



            }
        });
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


    public void messageHandler(String message, int person) {

        TextView t = new TextView(MainActivity.this);

        if (person == 1) {
            t.setBackgroundResource(R.drawable.rounded_corner);
        } else {
            t.setBackgroundResource(R.drawable.rounded_corner1);
        }
        t.setText(message);
        t.setTextColor(Color.WHITE);

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
        if(person==1) {
            showLoadingIcon();
        }

        layout.invalidate();


    }

    public void showLoadingIcon(){
        pbar  = new ProgressBar(MainActivity.this,null,android.R.attr.progressBarStyleSmall);
        pbar.setLayoutParams(new LinearLayout.LayoutParams(
               LinearLayout.LayoutParams.WRAP_CONTENT,
              LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) pbar.getLayoutParams();
        layoutParams.gravity= Gravity.LEFT;
        layoutParams.setMargins(0, 50, 0, 0);
        layout.addView(pbar);






    }
    public void removeLoadingIcon(){
        layout.removeView(pbar);
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
                    if(incomplete_flag==0){
                        socket.emit("chat", text);

                    }else{
                        requestDayandTime(text);
                    }


                    messageHandler(text,1);

                }

        }
        else if(requestCode == CAL_EVENT_RECOGNITION_CODE){

            if(resultCode == RESULT_OK && null!= data) {
                if(data.getStringExtra("error-code").equals("0")){
                    String myValue = data.getStringExtra("message");
                    messageHandler(myValue,0);
                }
                else if(data.getStringExtra("error-code").equals("1")){
                    String orginal_input = data.getStringExtra("original_input");
                    String message = data.getStringExtra("message");
                    messageHandler(message,0);
                    incomplete_flag=1;
                    buffer = orginal_input;

                }


            }
        }
    }

    public void requestDayandTime(String input){

        String final_input  = buffer.concat(" "+input);
        socket.emit("chat", final_input);

        buffer="";
        incomplete_flag= 0;

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}

