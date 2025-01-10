package org.example;

import com.google.gson.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

/**
 * Interagerar med Spotify Web API för att kontrollera musiken
 */
public class MusicController {

    private static final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl = "https://api.spotify.com/v1/me/player/";

    /**
     * Kollar om musiken är pausad eller inte
     * @param playlistId
     * @param accessToken
     * @throws Exception
     */
    public void playOrResumeMusic(String playlistId, String accessToken) throws Exception {
        // Kollar om Spotify körs någonstans (behövs för att interagera med det)
        if (!isActiveDevice(accessToken)) {
            System.out.println("Ingen aktiv enhet hittades. Se till att du har Spotify öppet på någon enhet.");
            return;
        }

        List<String> trackUris = getPlaylistTracks(playlistId, accessToken);

        // Kollar om musiken är pausad
        if (isPlaybackPaused(accessToken)) {
            // Kollar om den pausade musiken är en del av den givna spellistan
            if (isPausedTrackInPlaylist(trackUris, accessToken)) {
                resumePlayback(accessToken); // Om musiken tillhör spellistan fortsätter den spela bara
            } else {
                playPlaylist(playlistId, accessToken); // Om musiken är en annan låt så startas spellistan
            }
        } else {
            playPlaylist(playlistId, accessToken); // Om musiken inte är pausad så startar spellistan från början
        }
    }

    /**
     * Spelar en spellista
     * @param playlistId
     * @param accessToken
     * @throws Exception
     */
    public void playPlaylist(String playlistId, String accessToken) throws Exception {
        String apiUrl = baseUrl + "play";
        // Hämtar alla låtar från spellistan
        List<String> trackUris = getPlaylistTracks(playlistId, accessToken);

        // Konverterar spellistan till json-struktur för att kunna skicka till Spotify API
        JsonObject jsonBody = new JsonObject(); // jsonobjekt = objekt som består av nyckel-värde par
        JsonArray uris = new JsonArray(); // array av json-värden (sträng i detta fall)
        for (String uri : trackUris) { // lägger till alla låtar från låtlistan i array
            uris.add(uri);
        }
        jsonBody.add("uris", uris); // lägger till arrayn som värdet ("uris" är nyckeln)

        // Skickar förfrågan och sparar svar
        HttpResponse<String> response = sendRequest(apiUrl, "PUT", accessToken, jsonBody.toString());

        // Om anropet inte var framgångsrikt
        handleApiError(response);
    }

    /**
     * Återupptar uppspelningen om musiken har pausats
     * @param accessToken
     * @throws Exception
     */
    private void resumePlayback(String accessToken) throws Exception {
        String apiUrl = baseUrl + "play";

        // Skickar förfrågan och sparar svar
        HttpResponse<String> response = sendRequest(apiUrl, "PUT", accessToken, null);

        // Om anropet inte var framgångsrikt
        handleApiError(response);
    }

    /**
     * Pausar musiken
     * @param accessToken
     * @throws Exception
     */
    public void pauseMusic(String accessToken) throws Exception {
        String apiUrl = baseUrl + "pause";

        // Skickar förfrågan och sparar svaret
        HttpResponse<String> response = sendRequest(apiUrl, "PUT", accessToken, "");

        // Om anropet inte var framgångsrikt
        handleApiError(response);
    }

    /**
     * Spelar nästa låt i listan
     * @param accessToken
     * @throws Exception
     */
    public void nextTrack(String accessToken) throws Exception {
        String apiUrl = baseUrl + "next";

        // Skickar förfrågan och sparar svaret
        HttpResponse<String> response = sendRequest(apiUrl, "POST", accessToken, "");

        // Om anropet inte var framgångsrikt
        handleApiError(response);
    }

    /**
     * Spelar föregående låt i listan
     * @param accessToken
     * @throws Exception
     */
    public void previousTrack(String accessToken) throws Exception {
        String apiUrl = baseUrl + "previous";

        // Skickar förfrågan och sparar svaret
        HttpResponse<String> response = sendRequest(apiUrl, "POST", accessToken, "");

        // Om anropet inte var framgångsrikt
        handleApiError(response);
    }

    /**
     * Hämtar aktuell spellista eller låt
     * @param accessToken
     * @return URL till coverbilden
     * @throws Exception
     */
    public String getCurrentTrackCover(String accessToken) throws Exception {
        String apiUrl = "https://api.spotify.com/v1/me/player/currently-playing";

        HttpResponse<String> response = sendRequest(apiUrl, "GET", accessToken, null);

        if (response.statusCode() == 200) {
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            if (jsonResponse.has("item")) {
                JsonObject track = jsonResponse.getAsJsonObject("item");
                JsonObject album = track.getAsJsonObject("album");
                JsonArray images = album.getAsJsonArray("images");
                if (images.size() > 0) {
                    return images.get(0).getAsJsonObject().get("url").getAsString();
                }
            }
        } else {
            handleApiError(response);
        }
        return "/images/spotify.png"; // Fallback-bild
    }

    /**
     * Uppdaterar access_token med refresh_token
     * @param refreshToken
     * @return Nytt access_token
     * @throws Exception
     */
    public String refreshAccessToken(String refreshToken) throws Exception {
        String apiUrl = "https://accounts.spotify.com/api/token";
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret = System.getenv("CLIENT_SECRET");

        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        String requestBody = "grant_type=refresh_token&refresh_token=" + refreshToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Basic " + encodedCredentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            return jsonResponse.get("access_token").getAsString();
        } else {
            handleApiError(response);
            throw new RuntimeException("Misslyckades att uppdatera access_token: " + response.body());
        }
    }

    /**
     * Kontrollerar om musiken är pausad
     * @param accessToken
     * @return sant om musiken har pausats
     * @throws Exception
     */
    private boolean isPlaybackPaused(String accessToken) throws Exception {
        // Skickar förfrågan och sparar svaret
        HttpResponse<String> response = sendRequest(baseUrl, "GET", accessToken, null);

        // Om anropet var framgångsrikt
        if (response.statusCode() == 200) {
            // Omvandlar svaret till jsonobjekt
            JsonObject playbackState = JsonParser.parseString(response.body()).getAsJsonObject();
            // Returnerar true om Spotify spelar musik men är pausad
            return playbackState.has("is_playing") && !playbackState.get("is_playing").getAsBoolean();
        } else {
            // Om anropet inte var framgångsrikt
            handleApiError(response);
            return false;
        }
    }

    /**
     * Kollar om den pausade låten finns i spellistan
     * @param trackUris
     * @param accessToken
     * @return true om den pausade låten finns i spellistan, annars false
     */
    private boolean isPausedTrackInPlaylist(List<String> trackUris, String accessToken) throws Exception {
        String apiUrl = baseUrl + "currently-playing";

        // Skickar förfrågan och sparar svar
        HttpResponse<String> response = sendRequest(apiUrl, "GET", accessToken, null);

        // Om anropet var framgångsrikt
        if (response.statusCode() == 200) {
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            // Hämtar uri för den pausade låten
            if (jsonResponse.has("item")) {
                String pausedTrackUri = jsonResponse.getAsJsonObject("item").get("uri").getAsString();
                // Kollar om den pausade låtens uri finns i spellistan
                return trackUris.contains(pausedTrackUri);
            }
        } else {
            handleApiError(response);
        }
        return false;
    }

    /**
     * Kontrollerar om det finns en aktiv enhet som spelar musik
     * @param accessToken
     * @return sant om det finns en aktiv enhet
     * @throws Exception
     */
    private boolean isActiveDevice(String accessToken) throws Exception {
        String apiUrl = baseUrl + "devices";

        // Skickar förfrågan och sparar svaret
        HttpResponse<String> response = sendRequest(apiUrl, "GET", accessToken, null);

        // Om anropet var framgångsrikt
        if (response.statusCode() == 200) {
            // Omvandlar svaret till jsonobjekt
            JsonObject jsonResponse = new Gson().fromJson(response.body(), JsonObject.class);
            // Om svaret innehåller nyckeln "devices" med ett värde som är en lista med enheter
            if (jsonResponse.has("devices")) {
                // Varje enhet i listan omvandlas till jsonobjekt
                for (var device : jsonResponse.getAsJsonArray("devices")) {
                    JsonObject deviceObj = device.getAsJsonObject();
                    // Om objektet har en aktiv enhet returneras true
                    if (deviceObj.has("is_active") && deviceObj.get("is_active").getAsBoolean()) {
                        return true;
                    }
                }
            }
        } else {
            // Om anropet inte var framgångsrikt
            handleApiError(response);
        }
        return false;
    }

    /**
     * Hämtar låtar från spellista
     * @param playlistId
     * @param accessToken
     * @return lista på låtar
     * @throws Exception
     */
    public List<String> getPlaylistTracks(String playlistId, String accessToken) throws Exception {
        String apiUrl = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";

        // Skickar förfrågan och sparar svaret
        HttpResponse<String> response = sendRequest(apiUrl, "GET", accessToken, null);

        // Om anropet var framgångsrikt
        if (response.statusCode() == 200) {
            // Omvandlar svaret till jsonobjekt
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            // Sparar alla låtar (items) i objektet i en array
            JsonArray tracks = jsonResponse.getAsJsonArray("items");

            List<String> trackUris = new ArrayList<>();
            for (JsonElement track : tracks) {
                // Hämtar uri för varje låt i listan och sparar som sträng
                String trackUri = track.getAsJsonObject().getAsJsonObject("track").get("uri").getAsString();
                trackUris.add(trackUri);
            }

            return trackUris;
        } else {
            // Om anropet inte var framgångsrikt
            handleApiError(response);
            throw new RuntimeException("Misslyckades att hämta spellistas spår: " + response.body());
        }
    }

    /**
     * Skickar Http-förfrågan och returnerar svar
     * @param uri
     * @param method
     * @param accessToken
     * @param body
     * @return http-svar
     * @throws Exception
     */
    public HttpResponse<String> sendRequest(String uri, String method, String accessToken, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .method(method, body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody())
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Hanterar API-anrop misslyckanden
     * @param response
     */
    public void handleApiError(HttpResponse<String> response) {
        if (response.statusCode() != 200 && response.statusCode() != 204) {
            System.err.println("API-anrop misslyckades med statuskod: " + response.statusCode());
            System.err.println("Response Body: " + response.body());
        }
    }
}
