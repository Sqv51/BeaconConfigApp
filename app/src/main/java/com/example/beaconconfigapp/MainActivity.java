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

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
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

        // Assuming macAddressInput is your EditText or similar input field
        macAddressInput.setFilters(new InputFilter[]{
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest, int dstart, int dend) {
                        StringBuilder builder = new StringBuilder(dest.toString());
                        builder.replace(dstart, dend, source.subSequence(start, end).toString());
                        if (!builder.toString().matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})?$")) {
                            return "";
                        }
                        return null;
                    }
                }

        });




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
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        updateRssiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rssi = Integer.parseInt(integerInput.getText().toString());
                try {
                    sendRequest(updateRssi(rssi));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private Request updateRssi(int rssi) {
        JSONObject json = new JSONObject();
        try {
            json.put("rssi", -rssi);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        return new Request.Builder()
                .url("http://10.34.82.169/updateRssi")
                .post(body)
                .build();


    }

    private Request removeMac(String macAddress) {
        JSONObject json = new JSONObject();
        try {
            json.put("macAddress", macAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        return new Request.Builder()
                .url("http://10.34.82.169/removeMac")
                .post(body)
                .build();

    }

    private Request getMac() {
        return new Request.Builder()
                .url("http://10.34.82.169/getMacs")
                .get()
                .build();

    }

    private Request addMac(String macAddress) {
        JSONObject json = new JSONObject();
        try {
            json.put("macAddress", macAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        return new Request.Builder()
                .url("http://10.34.82.169/addMac")
                .post(body)
                .build();


    }

    private String sendRequest(Request request) throws IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> responseDataRef = new AtomicReference<>();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    final String responseData = responseBody.string();
                    httpResponseText.setText(responseData);
                    responseDataRef.set(responseData);
                    latch.countDown();
                }
            }
        });

        try {
            latch.await(); // Wait until response is received or onFailure is called
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return responseDataRef.get();
    }

}
