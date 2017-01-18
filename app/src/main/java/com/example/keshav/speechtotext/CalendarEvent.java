package com.example.keshav.speechtotext;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class CalendarEvent extends AppCompatActivity {
        String date,time,title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   setContentView(R.layout.activity_calendar_event);

        Intent i=getIntent();
        Bundle extras = i.getExtras();
        if (extras == null) {
        } else {
            date = extras.getString("date");
            time = extras.getString("time");
            title = extras.getString("title");
        }

        calEntry(date,time,title);


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
         Toast.makeText(this,"Event set",Toast.LENGTH_LONG).show();
        finish();
    }



}
