package com.example.beaconconfigapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.beaconconfigapp.R;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private EditText macAddressInput;
    private Spinner macAddressSpinner;
    private EditText integerInput;
    private Button addMacButton;
    private Button getMacButton;
    private Button removeMacButton;
    private Button updateRssiButton;
    private TextView httpResponseText;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        macAddressInput = findViewById(R.id.mac_address_input);
        macAddressSpinner = findViewById(R.id.mac_address_spinner);
        integerInput = findViewById(R.id.integer_input);
        addMacButton = findViewById(R.id.button1);
        getMacButton = findViewById(R.id.button2);
        removeMacButton = findViewById(R.id.button3);
        updateRssiButton = findViewById(R.id.button4);
        httpResponseText = findViewById(R.id.http_response_text);

        addMacButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject json = new JSONObject();
                try {
                    json.put("mac", macAddressInput.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendPostRequest("http://10.34.82.169:80/addMac", json);
            }

            private void sendPostRequest(String url, JSONObject json) {
                RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        httpResponseText.setText(response.body().string());
                    }
                });
            }
        });

        getMacButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGetRequest("http://10.34.82.169:80/getMacs");
            }

            private void sendGetRequest(String url) {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        httpResponseText.setText(response.body().string());
                    }
                });
            }
        });

        removeMacButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject json = new JSONObject();
                try {
                    json.put("mac", macAddressSpinner.getSelectedItem().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendPostRequest("http://10.34.82.169:80/removeMac", json);
            }

            private void sendPostRequest(String url, JSONObject json) {
                RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        httpResponseText.setText(response.body().string());
                    }
                });

            }
        });
    }
}

