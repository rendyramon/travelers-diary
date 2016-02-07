package com.travelersdiary.services;

import android.os.AsyncTask;
import com.travelersdiary.models.LocationPoint;
import com.travelersdiary.models.WeatherInfo;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Vladimir on 06.02.2016.
 */
public class DataWeather extends AsyncTask<Void, Void, WeatherInfo> {

    WeatherInfo weatherInfo = null;
    private String latitude;
    private String longitude;
    private double altitude;

    DataWeather(LocationPoint locationPoint) {
        latitude = Double.toString(locationPoint.getLatitude());
        longitude = Double.toString(locationPoint.getLongitude());
        altitude = locationPoint.getLongitude();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected WeatherInfo doInBackground(Void... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        URL url;

        try {
            url = new URL("http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=metric&appid=7ecc1ea3a34dd7e4677a252c9ddafa8e");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            resultJson = buffer.toString();


            JSONObject jsonObject = new JSONObject(resultJson);

             weatherInfo = new WeatherInfo();


            weatherInfo.setTemp(Float.valueOf(jsonObject.getJSONObject("main").getString("temp").toString()));
            weatherInfo.setPressure(Float.valueOf(jsonObject.getJSONObject("main").getString("pressure").toString()));
            weatherInfo.setHumidity(Integer.valueOf(jsonObject.getJSONObject("main").getString("humidity").toString()));
            weatherInfo.setWeatherIcon(jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon"));
            weatherInfo.setWeatherMain(jsonObject.getJSONArray("weather").getJSONObject(0).getString("main"));
            weatherInfo.setWeatherDescription(jsonObject.getJSONArray("weather").getJSONObject(0).getString("description"));
            weatherInfo.setWindSpeed(Float.valueOf(jsonObject.getJSONObject("wind").getString("speed").toString()));
            weatherInfo.setWindDeg(Float.valueOf(jsonObject.getJSONObject("wind").getString("deg").toString()));
            weatherInfo.setClouds(Integer.valueOf(jsonObject.getJSONObject("clouds").getString("all").toString()));
            weatherInfo.setSunrise(Long.valueOf(jsonObject.getJSONObject("sys").getString("sunrise").toString()));
            weatherInfo.setSunset(Long.valueOf(jsonObject.getJSONObject("sys").getString("sunset").toString()));

        } catch (Exception e) {
            e.printStackTrace();

        }
        return weatherInfo;
    }

}
