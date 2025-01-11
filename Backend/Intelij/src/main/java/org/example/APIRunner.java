package org.example;

import io.javalin.Javalin;
import java.io.*;
import java.net.http.HttpResponse;
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
            //konfigurerar servern att hämta statiska filer (css, js) från frontend katalogen
            config.staticFiles.add("frontend/frontend", io.javalin.http.staticfiles.Location.EXTERNAL);
            //anger hur servern ska läsa och returnera en fil (html) när den efterfrågas
            config.fileRenderer((filePath, ctx, layoutPath) -> {
                try {
                    return Files.readString(Paths.get("frontend/frontend/" + filePath));
                } catch (Exception e) {
                    throw new RuntimeException("Could not read file: " + filePath, e);
                }
            });

            //aktiverar CORS-plugin
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost(); //tillåter alla domäner skicka begäranden till servern
                });
            });
        }).start(5009); //startar server på port 5009

        //anrop för att hämta första sidan
        app.get("/", ctx -> {
            ctx.render("login.html");
        });

        //anrop för att få namnet på en plats
        app.post("/location", ctx -> {
            runner.locationController.locationByCoordinates(ctx);
        });

        //anrop för att få koordinaterna till en plats
        app.get("/locationByName", ctx -> {
            runner.locationController.locationByName(ctx);
        });

        //anrop för att få vädret hos en plats
        app.get("/weatherLocation", ctx -> {
            if (locationController.getLocationCoordinates() == null) {
                ctx.status(400).result("Ingen plats har sparats ännu.");
                return;
            }
            runner.weatherData.weatherbylocation(ctx, locationController.getPlaceName(),
                    locationController.getLocationCoordinates(), runner.weatherAPI_Key);
        });

        //anrop för att logga in
        app.get("/login", ctx -> {
            String loginUrl = loginController.getSpotifyLoginUrl();
            ctx.redirect(loginUrl); //omdirigering till Spotify OAuth 2.0 inloggningssida
        });

        //anrop för att få access token från Spotify
        app.get("/callback", ctx -> { //efter inlogg omdirigeras användaren till /callback
            String code = ctx.queryParam("code"); //hämtar code från callback-URLen
            if (code != null) {
                loginController.handleCallback(code);
                ctx.render("music-control.html");
            } else {
                ctx.result("Login misslyckades.");
            }
        });

        //anrop för att pausa musik
        app.put("/pause", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicController.pauseMusic(accessToken);
        });

        //anrop för att spela nästa låt
        app.post("/next", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicController.nextTrack(accessToken);
        });

        //anrop för att spela föregående låt
        app.post("/previous", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicController.previousTrack(accessToken);
        });

        //anrop för att starta musik
        app.put("/play-playlist", ctx -> {
            String accessToken = loginController.getAccessToken();
            String playlistId = weatherAnalyzer.analyzeWeather(weatherData.getWeatherCode(), weatherData.getTemp());

            if (!musicController.isActiveDevice(accessToken)) {
                ctx.status(400); //ingen aktiv enhet, skapar popup på webbsidan
                return;
            }
            
            musicController.playOrResumeMusic(playlistId, accessToken);
            musicData.fetchPlaylistData(ctx, playlistId, accessToken); //hämtar data om spellista för att visa på webbsidan
        });

        //anrop för att hämta låten som spelas just nu
        app.get("/currently-playing", ctx -> {
            String accessToken = loginController.getAccessToken();
            musicData.fetchCurrentlyPlaying(ctx, accessToken);
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
