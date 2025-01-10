package org.example;

import io.javalin.http.Context;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WeatherData {
    private String time;
    private String temp;
    private String weatherCode;
    private String weatherDescription;
    private String locationCor;
    private String locationName;
    private WeatherAnalyzer weatherAnalyzer;


    public WeatherData() {
         weatherAnalyzer = new WeatherAnalyzer();
    }

    public void weatherbylocation(Context ctx, String location, String locationCor, String API_Key) {

        locationName = location;
        // Kontrollera om plats eller koordinater är null
        if (locationCor == null || locationCor.isEmpty()) {
            ctx.status(400).result("Inga koordinater sparade för att hämta väder.");
            return;
        }

        // Skapa API URL
        String apiUrl = "https://api.tomorrow.io/v4/weather/realtime?location=" +
                locationCor + "&apikey=" + API_Key;



        // Skapa HTTP-begäran
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            // Skicka HTTP-begäran
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            // Kontrollera om API-svaret är giltigt
            if (response.statusCode() != 200) {
                ctx.status(response.statusCode()).result("Fel från väder-API: " + response.body());
                return;
            }

            // Hämta och bearbeta väderdata
            String jsonResponse = response.body();
            getWeather(jsonResponse);

            // Skapa JSON-svar till frontend
            JsonObject responseData = new JsonObject();

            responseData.addProperty("locationName", locationName != null ? locationName : "Okänd plats");
            responseData.addProperty("time", time != null ? time : "Okänd tid");
            responseData.addProperty("temp", temp != null ? temp : "Okänd temperatur");
            responseData.addProperty("weatherCode", weatherCode != null ? weatherCode : "Okänt väder");
            weatherDescription = weatherAnalyzer.getWeatherDescription(getWeatherCode());
            responseData.addProperty("weatherDescription", weatherDescription != null ? weatherDescription : "weathercode saknas");


            // Skicka JSON till frontend
            ctx.json(responseData.toString());



        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fel vid hämtning av väderdata.");
        }
    }


    private void getWeather(String jsonResponse){
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Extract required fields with null checks
        JsonObject data = jsonObject.getAsJsonObject("data");
        time = data != null && data.has("time") ? data.get("time").getAsString() : "Unknown time";


        JsonObject values = data != null ? data.getAsJsonObject("values") : null;
        temp = values != null && values.has("temperature") ? values.get("temperature").getAsString() : "Unknown temperature";
        weatherCode = values != null && values.has("weatherCode") ? values.get("weatherCode").getAsString() : "Unknown weather code";

    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getTemp() {
        if(temp == null || temp.isEmpty()) {
            return 10000.0;
        }
        double temp1 = Double.parseDouble(temp);
        return temp1;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getWeatherCode() {
        if(weatherCode == null || weatherCode.isEmpty()) {
            return "Unknown weather code";
        }
        return weatherCode;
    }

    public void setWeatherCode(String weatherCode) {
        this.weatherCode = weatherCode;
    }

    public String getLocationCor() {
        return locationCor;
    }

    public void setLocationCor(String locationCor) {
        this.locationCor = locationCor;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }


}
