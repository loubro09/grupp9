package org.example;

import io.javalin.Javalin;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

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

        //skapar instans av Javalin
        Javalin app = Javalin.create(config -> {
            //konfigurerar servern att hämta statiska filer (css, js) från frontend katalogen
            config.staticFiles.add("frontend", io.javalin.http.staticfiles.Location.EXTERNAL);
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
                    it.anyHost();  //tillåter alla domäner skicka begäranden till servern
                });
            });
            /** OBSSSS!!!
             * startar server på port 5008 FÖR weather
             */
        }).start(5009);

        //anrop för att få koordinaterna till nuvarande plats
        //app.get("/", ctx -> ctx.result(Files.readString(Paths.get("weather.html"))));

        //anrop för att hämta första sidan (kan tas bort om index.html används som inlogg-sida)
        /** TO DO:
         * // LÄGG TILL NYA HTML
         */
        app.get("/", ctx -> {ctx.render("login.html");});
        //app.get("/", ctx -> {ctx.render("weather.html");});


        //anrop för att få namnet på en plats
        app.post("/location", ctx -> {
            runner.locationController.locationByCoordinates(ctx);
        });

        //anrop för att få koordinaterna till en plats
        app.get("/locationByName", ctx -> {
            runner.locationController.locationByName(ctx);

            runner.location = locationController.getLocationCoordinates();
            runner.locationName = locationController.getPlaceName();
        });

        // Hämta väderdata

            // Hämta väderdata
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
                ctx.result("Login failed");
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
            // String playlistId = "1pYJQgF8EmVcSlGbskZXfA"; //temporär spellista hårdkodad
            String playlistId = (weatherAnalyzer.analyzeWeather("1000",16.0));
            //String playlistId = "37i9dQZF1EIfS0ZRAzGri5";
            musicController.playOrResumeMusic(playlistId, accessToken);

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