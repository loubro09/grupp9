package org.example;

import io.javalin.http.Context;

import java.net.http.HttpRequest;

public class WeatherData {
    private String time;
    private String temp;
    private String weatherCode;
    private String locationCor;
    private String locationName;
    private String API_Key;

    public void weatherbylocation(Context ctx,String location, String API_Key){
        String apiUrl = "https://api.tomorrow.io/v4/weather/realtime?location=" +
                location + "&apikey=" + API_Key;

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
            WeatherData weatherData = WeatherParser.parseWeatherData(response.body());
            weatherData.setLocationName( runner.locationName);
            WeatherAnalyzer weatherAnalyzer = new WeatherAnalyzer();
            System.out.println(weatherAnalyzer.analyzeWeather(weatherData.getWeatherCode(),weatherData.getTemp()));

            ctx.json(weatherData);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error parsing weather data");
        }
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
                WeatherData weatherData = WeatherParser.parseWeatherData(response.body());
                weatherData.setLocationName( runner.locationName);
                WeatherAnalyzer weatherAnalyzer = new WeatherAnalyzer();
                System.out.println(weatherAnalyzer.analyzeWeather(weatherData.getWeatherCode(),weatherData.getTemp()));

                ctx.json(weatherData);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error parsing weather data");
            }
     */
}
