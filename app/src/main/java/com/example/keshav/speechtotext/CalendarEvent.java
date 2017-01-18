package com.example.keshav.speechtotext;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;



import org.json.JSONException;
import org.json.JSONObject;

public class CalendarEvent extends AppCompatActivity {
        String date,time,title,error_code,error_message,original_input;
        JSONObject json_data;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   setContentView(R.layout.activity_calendar_event);


        Intent i = getIntent();
        Bundle extras = i.getExtras();
        try {
            json_data = new JSONObject(getIntent().getStringExtra("json"));
            error_code = json_data.getString("error_code");

            if (error_code.equals("0")) {
                date = json_data.getString("date");
                time = json_data.getString("time");
                title = json_data.getString("desc");
                calEntry(date, time, title);

            }
            else{
                error_message = json_data.getString("desc");
                original_input = json_data.getString("original_input");
                Intent resultData = new Intent();
                resultData.putExtra("message", error_message);
                resultData.putExtra("error-code",error_code);
                resultData.putExtra("original_input",original_input);
                setResult(Activity.RESULT_OK, resultData);
                finish();

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }






    public void calEntry(String date,String time,String title){

        String eventdate;
        eventdate=date+" "+time;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        final Calendar cal = Calendar.getInstance();
        try {

            cal.setTime(formatter.parse(eventdate));
            eventdate = cal.get(Calendar.YEAR)+"/"+cal.get(Calendar.MONTH)+"/"+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE);
            //Log.e("Event date ", eventdate);
        } catch (Exception e) {
            Log.e("Catch ", "",e);
        }
        int eid;
        eid=Integer.parseInt(cal.get(Calendar.YEAR)+""+cal.get(Calendar.MONTH)+""+cal.get(Calendar.DAY_OF_MONTH));
        // System.out.println(eid);
        ContentValues event = new ContentValues();
        event.put("calendar_id", 3);
        event.put("_id", eid);
        event.put("title", title);
        event.put("description", "Set by Jarvis");

        event.put("eventTimezone", TimeZone.getDefault().getID());
        event.put("dtstart", cal.getTimeInMillis());
        event.put("dtend", cal.getTimeInMillis()+120*60*1000);
        event.put("hasAlarm", 1);



        Uri eventUri = getApplicationContext()
                .getContentResolver()
                .insert(Uri.parse("content://com.android.calendar/events"), event);
        String message = "I will remind you about \""+title+"\" on "+date+" at "+time;
        Intent resultData = new Intent();
        resultData.putExtra("message", message);
        resultData.putExtra("error-code",error_code);
        setResult(Activity.RESULT_OK, resultData);
        finish();
    }



}
