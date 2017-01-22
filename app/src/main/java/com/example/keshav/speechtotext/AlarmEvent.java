package com.example.keshav.speechtotext;

import android.app.Activity;
import android.content.Intent;
import android.provider.AlarmClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class AlarmEvent extends AppCompatActivity {

    JSONObject json_data;
    String error_code,hours,minutes,minutes_only,message,error_message,original_input;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_alarm_event);
        Intent i=getIntent();
        Bundle extra= i.getExtras();
        try{
            json_data= new JSONObject(getIntent().getStringExtra("json"));
            error_code= json_data.getString("error_code");
            if(error_code.equals("0")){
                message = json_data.getString("message");
                hours = json_data.getString("hours");
                minutes = json_data.getString("minutes");
                minutes_only = json_data.getString("minutes_only");
                setAlarmClock(hours,minutes,minutes_only);
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
        }
        catch (JSONException e){}
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
        Intent resultData = new Intent();
        message="Your alarm has been set";
        resultData.putExtra("message", message);
        resultData.putExtra("error-code",error_code);
        setResult(Activity.RESULT_OK, resultData);
        finish();


    }

}
