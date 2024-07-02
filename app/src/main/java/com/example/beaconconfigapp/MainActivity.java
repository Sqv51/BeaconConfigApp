package com.example.beaconconfigapp;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private EditText macAddressInput;
    private Spinner macAddressSpinner;
    private EditText integerInput;
    private Button addMacButton;
    private Button getMacButton;
    private Button removeMacButton;
    private Button updateRssiButton;
    private TextView httpResponseText;

    private String tempString = "temp";
    private OkHttpClient client = new OkHttpClient();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        macAddressInput = findViewById(R.id.macAddressInput);
        macAddressSpinner = findViewById(R.id.macAddressSpinner);
        integerInput = findViewById(R.id.integerInput);
        addMacButton = findViewById(R.id.addMacButton);
        getMacButton = findViewById(R.id.getMacButton);
        removeMacButton = findViewById(R.id.removeMacButton);
        updateRssiButton = findViewById(R.id.updateRssiButton);
        httpResponseText = findViewById(R.id.httpResponseText);






        addMacButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (macAddressInput.getText().toString().length() != 17) {
                    httpResponseText.setText("Invalid MAC Address");
                    return;
                }
                String macAddress = macAddressInput.getText().toString();
                try {
                    sendRequest(addMac(macAddress));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        getMacButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    tempString = sendRequest(getMac());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //get the data between the square brackets
                Pattern p = Pattern.compile("\\[(.*?)\\]");
                Matcher m = p.matcher(tempString);
                while(m.find()) {
                    tempString = m.group(1);
                }
                //split the string into an array
                String[] macAddresses = tempString.split(",");
                //remove the quotes from the strings
                for (int i = 0; i < macAddresses.length; i++) {
                    macAddresses[i] = macAddresses[i].replace("\"", "");
                }
                //sort the array
                Arrays.sort(macAddresses);
                //create an adapter for the spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, macAddresses);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                macAddressSpinner.setAdapter(adapter);


            }
        });

        removeMacButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String macAddress = macAddressSpinner.getSelectedItem().toString();

                try {
                    sendRequest(removeMac(macAddress));
                } catch (IOException | JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        updateRssiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(integerInput.getText().toString().isEmpty()) {
                    //set rss to 70 if no input is given
                    integerInput.setText("70");
                }
                int rssi = Integer.parseInt(integerInput.getText().toString());
                try {
                    sendRequest(updateRssi(rssi));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static Request updateRssi(int rssi) {
        //directly build reqest as a string
        String json = "{\"rssiThreshold\":" + -rssi + "}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        return new Request.Builder()
                .url("http://10.34.82.169/updateRssi")
                .post(body)
                .build();
    }

    private Request removeMac(String macAddress) throws JSONException {
        JSONArray macAddresses = new JSONArray();
        macAddresses.put(macAddress);
        JSONObject json = new JSONObject();
        json.put("macAddresses", macAddresses);
        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("http://10.34.82.169/removeMac")
                .post(body)
                .build();
        System.out.println(request);
        return request;
    }

    private Request getMac() {
        return new Request.Builder()
                .url("http://10.34.82.169/getMacs")
                .get()
                .build();

    }

    private Request addMac(String macAddress) {
    JSONObject json = new JSONObject();
    JSONArray macAddresses = new JSONArray();
    try {
        macAddresses.put(macAddress);
        json.put("macAddresses", macAddresses);
    } catch (JSONException e) {
        e.printStackTrace();
    }
    RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
    return new Request.Builder()
            .url("http://10.34.82.169/addMac")
            .post(body)
            .build();
}




    private String sendRequest(Request request) throws IOException {


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    final String responseData = responseBody.string();
                    //set tempString to the response data
                    tempString = responseData;

                }
            }
        });

        return tempString;


    }

}
