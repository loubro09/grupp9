package org.example;

import io.javalin.http.Context;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Sparar information om vädret för analys
 */
public class WeatherData {
    private String temp;
    private String weatherCode;
    private String weatherDescription;
    private String locationName;
    private WeatherAnalyzer weatherAnalyzer;


    public WeatherData() {
         weatherAnalyzer = new WeatherAnalyzer();
    }

    public void weatherbylocation(Context ctx, String location, String locationCor, String API_Key) {

        locationName = location;

        if (locationCor == null || locationCor.isEmpty()) {
            ctx.status(400).result("Inga koordinater sparade för att hämta väder.");
            return;
        }

        String apiUrl = "https://api.tomorrow.io/v4/weather/realtime?location=" +
                locationCor + "&apikey=" + API_Key;

        //skapa HTTP-förfrågan
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            //skicka HTTP-begäran och spara svaret
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            //kontrollera om API-svaret är giltigt
            if (response.statusCode() != 200) {
                ctx.status(response.statusCode()).result("Fel från väder-API: " + response.body());
                return;
            }

            //hämta och bearbeta väderdata
            String jsonResponse = response.body();
            getWeather(jsonResponse);

            //skapa JSON-svar till frontend
            JsonObject responseData = new JsonObject();

            responseData.addProperty("locationName", locationName != null ? locationName : "Okänd plats");
            responseData.addProperty("temp", temp != null ? temp : "Okänd temperatur");
            responseData.addProperty("weatherCode", weatherCode != null ? weatherCode : "Okänt väder");
            weatherDescription = weatherAnalyzer.getWeatherDescription(getWeatherCode());
            responseData.addProperty("weatherDescription", weatherDescription != null ? weatherDescription : "weathercode saknas");

            //skicka JSON till frontend
            ctx.json(responseData.toString());

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fel vid hämtning av väderdata.");
        }
    }

    private void getWeather(String jsonResponse){
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        //extrahera temperatur och väderkod från Json svar
        JsonObject data = jsonObject.getAsJsonObject("data");

        JsonObject values = data != null ? data.getAsJsonObject("values") : null;
        temp = values != null && values.has("temperature") ? values.get("temperature").getAsString() : "Unknown temperature";
        weatherCode = values != null && values.has("weatherCode") ? values.get("weatherCode").getAsString() : "Unknown weather code";
    }

    public double getTemp() {
        if(temp == null || temp.isEmpty()) {
            return 10000.0;
        }
        double temp1 = Double.parseDouble(temp);
        return temp1;
    }

    public String getWeatherCode() {
        if(weatherCode == null || weatherCode.isEmpty()) {
            return "Unknown weather code";
        }
        return weatherCode;
    }
}
