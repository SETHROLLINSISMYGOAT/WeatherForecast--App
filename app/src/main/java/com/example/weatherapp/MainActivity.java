package com.example.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView cityNameText, temperatureText, humidityText, desciptionText, windText;
    private ImageView weatherIcon;
    private Button refreshButton;
    private EditText cityNameInput;
    private static final String API_KEY = "d759cbf3fa197877aa7d865d70206aa9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        cityNameText = findViewById(R.id.cityNameText);
        temperatureText = findViewById(R.id.TemperatureText);
        humidityText = findViewById(R.id.humidityText);
        desciptionText = findViewById(R.id.descriptionText);
        windText = findViewById(R.id.windText);
        weatherIcon = findViewById(R.id.weatherIcon);
        refreshButton = findViewById(R.id.fetchWeatherButton);
        cityNameInput = findViewById(R.id.cityNameInput);


        FetchWeatherData("Mumbai");

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cityName = cityNameInput.getText().toString();
                if (!cityName.isEmpty()) {
                    FetchWeatherData(cityName);
                } else {
                    cityNameInput.setError("Enter City Name");
                }
            }
        });
    }

    private void FetchWeatherData(String cityName) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + API_KEY + "&units=metric";
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                runOnUiThread(() -> updateUI(result));
            } catch (IOException e) {
                e.printStackTrace();

                runOnUiThread(() -> {

                    cityNameText.setText("Error fetching weather data");
                    temperatureText.setText("");
                    humidityText.setText("");
                    desciptionText.setText("");
                    windText.setText("");
                    weatherIcon.setImageResource(0);
                });
            }
        });
    }

    private void updateUI(String result) {
        if (result != null) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");
                double temperature = main.getDouble("temp");
                double humidity = main.getDouble("humidity");
                double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");
                String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                String iconCode = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
                String resouceName = "ic_" + iconCode;
                int resourceId = getResources().getIdentifier(resouceName, "drawable", getPackageName());
                weatherIcon.setImageResource(resourceId);
                cityNameText.setText(jsonObject.getString("name"));
                temperatureText.setText(String.format("%.0f°", temperature));
                humidityText.setText(String.format("%.0f%%", humidity));
                windText.setText(String.format("%.0f km/h", windSpeed));
                desciptionText.setText(description);

            } catch (JSONException e) {
                e.printStackTrace();

                runOnUiThread(() -> {

                    cityNameText.setText("Error parsing weather data");
                    temperatureText.setText("");
                    humidityText.setText("");
                    desciptionText.setText("");
                    windText.setText("");
                    weatherIcon.setImageResource(0);
                });
            }
        }
    }
}