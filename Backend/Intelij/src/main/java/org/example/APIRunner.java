package org.example;

import io.javalin.Javalin;
import java.io.*;
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
    private static MusicData musicData;
    private String clientId;
    private String clientSecret;

    public APIRunner() {
        loadConfig();
        locationController = new Location();
        weatherData = new WeatherData();
        loginController = new Login(clientId, clientSecret);
        musicController = new MusicController();
        weatherAnalyzer = new WeatherAnalyzer();
        musicData = new MusicData();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        APIRunner runner = new APIRunner();

        // Create an instance of Javalin
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("frontend/frontend", io.javalin.http.staticfiles.Location.EXTERNAL);
            config.fileRenderer((filePath, ctx, layoutPath) -> {
                try {
                    return Files.readString(Paths.get("frontend/frontend/" + filePath));
                } catch (Exception e) {
                    throw new RuntimeException("Could not read file: " + filePath, e);
                }
            });

            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start(5009);

        app.get("/", ctx -> {
            ctx.render("login.html");
        });

        app.post("/location", ctx -> {
            runner.locationController.locationByCoordinates(ctx);
        });

        app.get("/locationByName", ctx -> {
            runner.locationController.locationByName(ctx);
            runner.location = locationController.getLocationCoordinates();
            runner.locationName = locationController.getPlaceName();
        });

        app.get("/weatherLocation", ctx -> {
            if (locationController.getLocationCoordinates() == null) {
                ctx.status(400).result("Ingen plats har sparats ännu.");
                return;
            }
            runner.weatherData.weatherbylocation(ctx, locationController.getPlaceName(),
                    locationController.getLocationCoordinates(), runner.weatherAPI_Key);
        });

        app.get("/login", ctx -> {
            String loginUrl = loginController.getSpotifyLoginUrl();
            ctx.redirect(loginUrl);
        });

        app.get("/callback", ctx -> {
            String code = ctx.queryParam("code");
            if (code != null) {
                loginController.handleCallback(code);
                ctx.render("music-control.html");
            } else {
                ctx.result("Login failed");
            }
        });

        app.put("/pause", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicController.pauseMusic(accessToken);
            ctx.status(204);
        });

        app.post("/next", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicController.nextTrack(accessToken);
            ctx.status(204);
        });

        app.post("/previous", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicController.previousTrack(accessToken);
            ctx.status(204);
        });

        app.put("/play-playlist", ctx -> {
            String accessToken = loginController.getAccessToken();
<<<<<<< HEAD
            String playlistId = weatherAnalyzer.analyzeWeather(weatherData.getWeatherCode(), weatherData.getTemp());
=======
            // String playlistId = "1pYJQgF8EmVcSlGbskZXfA"; // Temporär spellista hårdkodad
            String playlistId = weatherAnalyzer.analyzeWeather(weatherData.getWeatherCode(), weatherData.getTemp());
            if (!musicController.isActiveDevice(accessToken)) {
                ctx.status(400); // Bad Request
                return;
            }

>>>>>>> forntend--location-BACKUP
            musicController.playOrResumeMusic(playlistId, accessToken);
            musicData.fetchPlaylistData(ctx, playlistId, accessToken);
        });

<<<<<<< HEAD
        app.get("/currently-playing", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicData.fetchCurrentlyPlaying(ctx, accessToken);
        });

=======
        // *** NYA ENDPOINTS FÖR MUSIKSPELAREN ***

        // Endpoint för att hämta aktuell spellistas coverbild
>>>>>>> forntend--location-BACKUP
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
