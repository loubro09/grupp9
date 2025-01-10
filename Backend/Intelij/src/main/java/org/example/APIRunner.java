package org.example;

import io.javalin.Javalin;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import com.google.gson.*;

public class APIRunner {
    private String weatherAPI_Key;
    private String location;
    private String locationName;
    private static Location locationController;
    private static WeatherData weatherData;
    private static WeatherAnalyzer weatherAnalyzer;
    private static Login loginController;
    private static MusicController musicController;
    private String clientId;
    private String clientSecret;

    public APIRunner() {
        loadConfig();
        locationController = new Location();
        weatherData = new WeatherData();
        loginController = new Login(clientId, clientSecret);
        musicController = new MusicController();
        weatherAnalyzer = new WeatherAnalyzer();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        APIRunner runner = new APIRunner();

        // Skapar instans av Javalin
        Javalin app = Javalin.create(config -> {
            // Konfigurerar servern att hämta statiska filer (css, js) från frontend katalogen
            config.staticFiles.add("frontend/frontend", io.javalin.http.staticfiles.Location.EXTERNAL);
            // Anger hur servern ska läsa och returnera en fil (html) när den efterfrågas
            config.fileRenderer((filePath, ctx, layoutPath) -> {
                try {
                    return Files.readString(Paths.get("frontend/frontend/" + filePath));
                } catch (Exception e) {
                    throw new RuntimeException("Could not read file: " + filePath, e);
                }
            });

            // Aktiverar CORS-plugin
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();  // Tillåter alla domäner skicka begäranden till servern
                });
            });
            /** OBS!!!
             * Startar server på port 5009
             */
        }).start(5009);

        // Anrop för att få koordinaterna till nuvarande plats
        // app.get("/", ctx -> ctx.result(Files.readString(Paths.get("weather.html"))));

        // Anrop för att få första sidan (kan tas bort om index.html används som inlogg-sida)
        /** TO DO:
         * // LÄGG TILL NYA HTML
         */
        app.get("/", ctx -> { ctx.render("login.html"); });
        // app.get("/", ctx -> {ctx.render("weather.html"); });

        // Anrop för att få namnet på en plats
        app.post("/location", ctx -> {
            runner.locationController.locationByCoordinates(ctx);
        });

        // Anrop för att få koordinaterna till en plats
        app.get("/locationByName", ctx -> {
            runner.locationController.locationByName(ctx);

            runner.location = locationController.getLocationCoordinates();
            runner.locationName = locationController.getPlaceName();
        });

        // Hämta väderdata
        app.get("/weatherLocation", ctx -> {
            if (locationController.getLocationCoordinates() == null) {
                ctx.status(400).result("Ingen plats har sparats ännu.");
                return;
            }
            runner.weatherData.weatherbylocation(ctx, locationController.getPlaceName(),
                    locationController.getLocationCoordinates(), runner.weatherAPI_Key);

        });

        // Anrop för att logga in
        app.get("/login", ctx -> {
            String loginUrl = loginController.getSpotifyLoginUrl();
            ctx.redirect(loginUrl); // Omdirigering till Spotify OAuth 2.0 inloggningssida
        });

        // Anrop för att få access token från Spotify
        app.get("/callback", ctx -> { // Efter inlogg omdirigeras användaren till /callback
            String code = ctx.queryParam("code"); // Hämtar code från callback-URLen
            if (code != null) {
                loginController.handleCallback(code);
                ctx.render("music-control.html");
            } else {
                ctx.result("Login failed");
            }
        });

        // Anrop för att pausa musik
        app.put("/pause", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicController.pauseMusic(accessToken);
            ctx.status(204);
        });

        // Anrop för att spela nästa låt
        app.post("/next", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicController.nextTrack(accessToken);
            ctx.status(204);
        });

        // Anrop för att spela föregående låt
        app.post("/previous", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicController.previousTrack(accessToken);
            ctx.status(204);
        });

        // Anrop för att starta musik
        app.put("/play-playlist", ctx -> {
            String accessToken = loginController.getAccessToken();
            // String playlistId = "1pYJQgF8EmVcSlGbskZXfA"; // Temporär spellista hårdkodad
            String playlistId = weatherAnalyzer.analyzeWeather("1000", 16.0);
            // String playlistId = "37i9dQZF1EIfS0ZRAzGri5";
            musicController.playOrResumeMusic(playlistId, accessToken);
            ctx.status(204);
        });

        // *** NYA ENDPOINTS FÖR MUSIKSPELAREN ***

        // Endpoint för att hämta aktuell spellistas coverbild
        app.get("/current-track-cover", ctx -> {
            String accessToken = loginController.getAccessToken();
            if (accessToken != null && !accessToken.isEmpty()) {
                try {
                    String coverUrl = musicController.getCurrentTrackCover(accessToken);
                    JsonObject responseJson = new JsonObject();
                    responseJson.addProperty("coverUrl", coverUrl);
                    ctx.json(responseJson);
                } catch (Exception e) {
                    e.printStackTrace();
                    ctx.status(500).result("Serverfel");
                }
            } else {
                ctx.status(401).result("Obehörig");
            }
        });

        // Endpoint för att hantera seek-förfrågningar
        app.put("/seek", ctx -> {
            String accessToken = loginController.getAccessToken();
            if (accessToken != null && !accessToken.isEmpty()) {
                String positionMs = ctx.queryParam("position_ms");
                if (positionMs != null && !positionMs.isEmpty()) {
                    try {
                        String apiUrl = "https://api.spotify.com/v1/me/player/seek?position_ms=" + positionMs;
                        HttpResponse<String> response = musicController.sendRequest(apiUrl, "PUT", accessToken, null);
                        if (response.statusCode() == 204) {
                            ctx.status(204);
                        } else {
                            musicController.handleApiError(response);
                            ctx.status(500).result("Serverfel vid seek.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ctx.status(500).result("Serverfel vid seek.");
                    }
                } else {
                    ctx.status(400).result("Bad Request: position_ms saknas.");
                }
            } else {
                ctx.status(401).result("Obehörig");
            }
        });
    }

    public void loadConfig() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            weatherAPI_Key = props.getProperty("db.weatherApi");
            clientId = props.getProperty("db.clientId");
            clientSecret = props.getProperty("db.clientSecret");
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
