package org.example;

import io.javalin.Javalin;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class APIRunner {
    private String weatherAPI_Key;
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

        //skapar instans av Javalin
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
                    it.anyHost(); //tillåter alla domäner skicka begäranden till servern
                });
            });
        }).start(5009);

        //anrop för att hämta första sidan
        app.get("/", ctx -> {
            ctx.render("login.html");
        });

        //anrop för att få namnet på en plats
        app.post("/location", ctx -> {
            runner.locationController.locationByCoordinates(ctx);
        });

        //anrop för att få koordinaterna till en plats
        app.get("/coordinates", ctx -> {
            runner.locationController.locationByName(ctx);
        });

        //anrop för att få vädret hos en plats
        app.get("/weather", ctx -> {
            if (locationController.getLocationCoordinates() == null) {
                ctx.status(400).result("Ingen plats har sparats ännu.");
                return;
            }
            runner.weatherData.weatherbylocation(ctx, locationController.getPlaceName(),
                    locationController.getLocationCoordinates(), runner.weatherAPI_Key);
        });

        //anrop för att logga in
        app.get("/loginURL", ctx -> {
            String loginUrl = loginController.getSpotifyLoginUrl();
            ctx.redirect(loginUrl);
        });

        //anrop för att få access token från Spotify
        app.get("/callback", ctx -> {
            String code = ctx.queryParam("code");
            if (code != null) {
                loginController.handleCallback(code);
                ctx.render("music-control.html");
            } else {
                ctx.result("Login misslyckades.");
            }
        });

        // Hantera uppspelning (play/pause)
        app.put("/player/state", ctx -> {
            String accessToken = loginController.getAccessToken();
            String state = ctx.formParam("state"); // Hämtar state från request-body

            if ("play".equals(state)) {
                String playlistId = weatherAnalyzer.analyzeWeather(weatherData.getWeatherCode(), weatherData.getTemp());

                if (!musicController.isActiveDevice(accessToken)) {
                    ctx.status(400).result("Ingen aktiv enhet.");
                    return;
                }

                musicController.playOrResumeMusic(playlistId, accessToken);
                musicData.fetchPlaylistData(ctx, playlistId, accessToken);
            } else if ("pause".equals(state)) {
                musicController.pauseMusic(accessToken);
            } else {
                ctx.status(400).result("Ogiltigt statusvärde.");
            }
        });

// Byta låt
        app.post("/player/next", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicController.nextTrack(accessToken);
        });

        app.post("/player/previous", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicController.previousTrack(accessToken);
        });



        //anrop för att spela föregående låt
        app.post("/player/previous", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicController.previousTrack(accessToken);
        });

        //anrop för att hämta låten som spelas just nu
        app.get("/current-song", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicData.fetchCurrentlyPlaying(ctx, accessToken);
        });
    }

    //laddar config filen med api-nycklar
    public void loadConfig() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            weatherAPI_Key = props.getProperty("db.weatherApi");
            clientId = props.getProperty("db.clientId");
            clientSecret = props.getProperty("db.clientSecret");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
