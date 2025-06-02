package org.example;

import io.javalin.Javalin;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
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
        app.get("/coordinates", ctx -> {
            runner.locationController.locationByName(ctx);
        });



        //anrop för att få vädret hos en plats
        app.get("/weather", ctx -> {
            if (locationController.getLocationCoordinates() == null) {
                ctx.status(404).result("Platsen hittades inte.");
                return;
            }
            runner.weatherData.weatherbylocation(ctx, locationController.getPlaceName(),
                    locationController.getLocationCoordinates(), runner.weatherAPI_Key);
        });

        //anrop för att logga in
        app.get("/loginURL", ctx -> {
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

        //anrop för att hämta väderdata
       /** app.put("/player", ctx -> {
            String accessToken = loginController.getAccessToken();
            String state = ctx.bodyAsClass(Map.class).get("state").toString();

            if ("playing".equals(state)) {
                String playlistId = weatherAnalyzer.analyzeWeather(weatherData.getWeatherCode(), weatherData.getTemp());

                if (!musicController.isActiveDevice(accessToken)) {
                    ctx.status(409).result("Ingen aktiv enhet är tillgänglig för uppspelning.");
                    return;
                }

                musicController.playOrResumeMusic(playlistId, accessToken);
                musicData.fetchPlaylistData(ctx, playlistId, accessToken);
            } else if ("paused".equals(state)) {
                musicController.pauseMusic(accessToken);
            } else {
                ctx.status(400).result("Invalid state");
            }
        });
        **/
        app.put("/player", ctx -> {
            String accessToken = loginController.getAccessToken();
            String action = ctx.queryParam("action");

            if (accessToken == null || accessToken.isEmpty()) {
                ctx.status(401).result("Access token saknas eller är ogiltig.");
                return;
            }

            if ("next".equals(action)) {
                musicController.nextTrack(accessToken);
                ctx.status(204);
            } else if ("previous".equals(action)) {
                musicController.previousTrack(accessToken);
                ctx.status(204);
            } else if ("play".equals(action)) {
                String playlistId = weatherAnalyzer.analyzeWeather(weatherData.getWeatherCode(), weatherData.getTemp());
                
                if (!musicController.isActiveDevice(accessToken)) {
                    ctx.status(409).result("Ingen aktiv enhet är tillgänglig för uppspelning.");
                    return;
                }

                musicController.playOrResumeMusic(playlistId, accessToken);
                musicData.fetchPlaylistData(ctx, playlistId, accessToken);
            } else if ("pause".equals(action)) {
                musicController.pauseMusic(accessToken);
                ctx.status(204);
            } else {
                ctx.status(422).result("Ogiltig åtgärd: " + action);
            }
        });



        //TODO Put/Player? action=next, istället för kombinera next och previous så blir det mer RESTFUL.

        /**app.post("/player/actions", ctx -> {
            String accessToken = loginController.getAccessToken();
            String action = ctx.bodyAsClass(Map.class).get("action").toString();

            if ("next".equals(action)) {
                musicController.nextTrack(accessToken);
            } else if ("previous".equals(action)) {
                musicController.previousTrack(accessToken);
            } else {
                ctx.status(422).result("Ogiltig åtgärd.");
            }
        });
         **/


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
