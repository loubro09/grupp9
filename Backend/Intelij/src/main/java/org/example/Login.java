package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Hanterar autentisering via Spotify API med OAuth 2.0
 */
public class Login {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String accessToken; //används för att göra auktoriserade API-anrop till Spotify

    public Login(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = "http://localhost:5009/callback"; //anropar get/callback efter inloggning
    }

    /**
     * Hanterar callback efter inloggning och hämtar access token
     * @param code
     * @throws IOException
     */
    public void handleCallback(String code) throws IOException {
        String credentials = clientId + ":" + clientSecret;
        //omvandlar binär data till ASCII-tecken för Basic Authentication (krävs av Spotify)
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        //formdatan som behövs av Spotify
        String formData = "grant_type=authorization_code&code=" + code + "&redirect_uri=" + redirectUri;

        //skapa Http-förfrågan (för att byta ut code till access token)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + encodedCredentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        try {
            //skickar förfrågan och sparar svar
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) { //om anrop var framgångsrikt
                //hämta och spara access token från svaret
                JsonObject jsonResponse = new Gson().fromJson(response.body(), JsonObject.class);
                accessToken = jsonResponse.get("access_token").getAsString();

            } else {
                System.err.println("Failed to fetch access token. HTTP Status: " + response.statusCode());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Skapar en URL som omdirigerar användaren till Spotify inloggninssida
     * @return url
     */
    public String getSpotifyLoginUrl() {
        //scopes definierar de åtkomstbehörigheter som användaren behöver i programmet (spela musik osv)
        String scopes = "user-read-playback-state user-modify-playback-state streaming user-read-currently-playing user-read-playback-position";

        return "https://accounts.spotify.com/authorize" +
                "?client_id=" + clientId +
                "&response_type=code" + //får tillbaka authorization code som byts ut till access token
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8);
    }

    public String getAccessToken() {
        return accessToken;
    }
}
