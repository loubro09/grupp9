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

    public APIRunner() {
        loadConfig();
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

        //hämtar nuvarande plats
        app.get("/", ctx -> ctx.result(Files.readString(Paths.get("weather.html"))));

        app.post("/location", ctx -> {
            String body = ctx.body();
            System.out.println("Mottagna koordinater: " + body);

            // Spara plats i instansvariabel
            runner.location = body;

            // Bekräfta mottagning till klient
            ctx.json("{\"message\": \"Platsen sparad\", \"data\": " + body + "}");
        });

        // Hämta väderdata
        app.get("/weather", ctx -> {
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

            System.out.println(response.body());
        });
    }


    public void loadConfig() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            weatherAPI_Key = props.getProperty("db.weatherApi");
            System.out.println(weatherAPI_Key);
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