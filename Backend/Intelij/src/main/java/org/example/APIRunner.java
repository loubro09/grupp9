package org.example;

import io.javalin.Javalin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class APIRunner {
    String weatherAPI_Key;
    private String location;
    private String locationName;
    private static Location locationController;
    private static WeatherData weatherData;

    public APIRunner() {
        loadConfig();
        locationController = new Location();
        weatherData = new WeatherData();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        APIRunner runner = new APIRunner();
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start(5008);

        //anrop för att få koordinaterna till nuvarande plats
        app.get("/", ctx -> ctx.result(Files.readString(Paths.get("weather.html"))));

        //anrop för att få namnet på en plats
        app.post("/location", ctx -> runner.locationController.locationByCoordinates(ctx));

        //anrop för att få koordinaterna till en plats
        app.get("/locationByName", ctx -> runner.locationController.locationByName(ctx));

        runner.location = locationController.getLocationCoordinates();
        runner.locationName = locationController.getPlaceName();


        // Hämta väderdata

        app.post("/weatherLocation", ctx -> {runner.weatherData.weatherbylocation(ctx, runner.locationName,runner.weatherAPI_Key);});


    }


    public void loadConfig() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            weatherAPI_Key = props.getProperty("db.weatherApi");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("WeatherAPI_Key : loadConfiguration : File not found exception");
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(":WeatherAPI_Key  loadConfiguration : IO exception");
            throw new RuntimeException(e);
        }
    }
}