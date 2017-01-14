package com.example.keshav.speechtotext;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;


import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final int SPEECH_RECOGNITION_CODE = 1;
    private TextView txtOutput;
    private ImageButton btnMicrophone;
    private EditText txtFld;
    private Button txtButton;
    private EditText mInputMessageView;

    public static String internalPath; // internal storage path
    public static String fileName; // the file name
    private Socket socket; // socket object
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try
        {
            socket = IO.socket("http://127.0.0.1:3001");
            socket.connect();  // initiate connection to socket server
            socket.emit("chat message",  "From Android to server: 1st outgoing message");
            socket.disconnect();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        txtOutput = (TextView)findViewById(R.id.txt_output);
        btnMicrophone = (ImageButton)findViewById(R.id.btn_mic);
        txtFld = (EditText)findViewById(R.id.TxtField);
        txtButton = (Button)findViewById(R.id.txtButton);
        btnMicrophone.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                speechtotext();
            }
        });
        txtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = txtFld.getText().toString();
                txtOutput.setText(txt);


            }
        });
    }


    private void speechtotext(){

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
                    txtOutput.setText(text);
                }

        }
    }
}

