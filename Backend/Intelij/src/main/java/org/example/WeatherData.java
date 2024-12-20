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
    private String locationCor;
    private String locationName;
    private String API_Key;

    public void weatherbylocation(Context ctx, String location, String locationCor, String API_Key) {
        System.out.println("WeatherData 21");
        // Kontrollera om plats eller koordinater är null
        if (locationCor == null || locationCor.isEmpty()) {
            ctx.status(400).result("Inga koordinater sparade för att hämta väder.");
            return;
        }

        // Skapa API URL
        String apiUrl = "https://api.tomorrow.io/v4/weather/realtime?location=" +
                locationCor + "&apikey=" + API_Key;

        System.out.println("Calling Weather API: " + apiUrl);

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
            responseData.addProperty("locationName", location != null ? location : "Okänd plats");
            responseData.addProperty("time", time != null ? time : "Okänd tid");
            responseData.addProperty("temp", temp != null ? temp : "Okänd temperatur");
            responseData.addProperty("weatherCode", weatherCode != null ? weatherCode : "Okänt väder");

            // Skicka JSON till frontend
            ctx.json(responseData);


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
        double temp1 = Double.parseDouble(temp);
        return temp1;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getWeatherCode() {
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

    @Override
    public String toString() {
        return "WeatherData{" +
                "time='" + time + '\'' +
                ", temp='" + temp + '\'' +
                ", weatherCode='" + weatherCode + '\'' +
                ", locationCor='" + locationCor + '\'' +
                ", locationName='" + locationName + '\'' +
                '}';
    }
    /*
    Få in allt, paketera upp, skicka weather code till analyzer.

    Spara svaret från analyzer så att Spotify kan hämta det. Tolka temperaturen till Spotify.

    Skicka vald information till Response + svaret från Weather Analyzer + Namnet på staden.
     */

    /*
    if (runner.location == null) {
                ctx.status(400).result("Ingen plats har sparats ännu.");
                return;
            }

            // Använd den sparade platsen i väder-API-anrop
            String apiUrl = "https://api.tomorrow.io/v4/weather/realtime?location=" +
                    runner.location + "&apikey=" + runner.weatherAPI_Key;

            System.out.println(apiUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("accept", "application/json")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());


            // Skicka vädersvar till klienten
            ctx.json(response.body());

            // Använd Parser
            try {
                weatherData.setLocationName( runner.locationName);
                System.out.println(weatherAnalyzer.analyzeWeather(weatherData.getWeatherCode(),weatherData.getTemp()));

                ctx.json(weatherData);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error parsing weather data");
            }
     */
}
