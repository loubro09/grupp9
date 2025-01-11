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
        List<String> trackUris = getPlaylistTracks(playlistId, accessToken);

        //kollar om musiken är pausad
        if (isPlaybackPaused(accessToken)) {
            //kollar om den pausade musiken är en del av den givna spellistan
            if (isPausedTrackInPlaylist(trackUris, accessToken)) {
                resumePlayback(accessToken); //om musiken tillhör spellistan fortsätter den spela bara
            } else {
                playPlaylist(playlistId, accessToken); //om musiken är en annan låt så startas spellistan
            }
        } else {
            playPlaylist(playlistId, accessToken); //om musiken inte är pausad så startar spellistan från början
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
        //hämtar alla låtar från spellistan
        List<String> trackUris = getPlaylistTracks(playlistId, accessToken);

        //konverterar spellistan till json-struktur för att kunna skicka till Spotify API
        JsonObject jsonBody = new JsonObject(); //jsonobjekt = objekt som består av nyckel-värde par
        JsonArray uris = new JsonArray(); //array av json-värden (sträng i detta fall)
        for (String uri : trackUris) { //lägger till alla låtar från låtlistan i array
            uris.add(uri);
        }
        jsonBody.add("uris", uris); //lägger till arrayn som värdet ("uris" är nyckeln)

        //skickar förfrågan och sparar svar
        HttpResponse<String> response = sendRequest(apiUrl, "PUT", accessToken, jsonBody.toString());

        //om anropet inte var framgångsrikt
        handleApiError(response);
    }

    /**
     * Återupptar uppspelningen om musiken har pausats
     * @param accessToken
     * @throws Exception
     */
    private void resumePlayback(String accessToken) throws Exception {
        String apiUrl = baseUrl + "play";

        //skickar förfrågan och sparar svar
        HttpResponse<String> response = sendRequest(apiUrl, "PUT", accessToken, null);

        //om anropet inte var framgångsrikt
        handleApiError(response);
    }

    /**
     * Pausar musiken
     * @param accessToken
     * @throws Exception
     */
    public void pauseMusic(String accessToken) throws Exception {
        String apiUrl = baseUrl + "pause";

        //skickar förfrågan och sparar svaret
        HttpResponse<String> response = sendRequest(apiUrl, "PUT", accessToken, "");

        //om anropet inte var framgångsrikt
        handleApiError(response);
    }

    /**
     * Spelar nästa låt i listan
     * @param accessToken
     * @throws Exception
     */
    public void nextTrack(String accessToken) throws Exception {
        String apiUrl = baseUrl + "next";

        //skickar förfrågan och sparar svaret
        HttpResponse<String> response = sendRequest(apiUrl, "POST", accessToken, "");

        //om anropet inte var framgångsrikt
        handleApiError(response);
    }

    /**
     * Spelar föregående låt i listan
     * @param accessToken
     * @throws Exception
     */
    public void previousTrack(String accessToken) throws Exception {
        String apiUrl = baseUrl + "previous";

        //skickar förfrågan och sparar svaret
        HttpResponse<String> response = sendRequest(apiUrl, "POST", accessToken, "");

        //om anropet inte var framgångsrikt
        handleApiError(response);
    }

    /**
     * Kontrollerar om musiken är pausad
     * @param accessToken
     * @return sant om musiken har pausats
     * @throws Exception
     */
    private boolean isPlaybackPaused(String accessToken) throws Exception {
        //skickar förfrågan och sparar svaret
        HttpResponse<String> response = sendRequest(baseUrl, "GET", accessToken, null);

        //om anropet var framgångsrikt
        if (response.statusCode() == 200) {
            //omvandlar svaret till jsonobjekt
            JsonObject playbackState = JsonParser.parseString(response.body()).getAsJsonObject();
            //returnerar true om Spotify spelar musik med den är pausad
            return playbackState.has("is_playing") && !playbackState.get("is_playing").getAsBoolean();
        } else {
            //om anropet inte var framgångsrikt
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

        //skickar förfrågan och sparar svar
        HttpResponse<String> response = sendRequest(apiUrl, "GET", accessToken, null);

        //om anropet var framgångsrikt
        if (response.statusCode() == 200) {
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            //hämtar uri för den pausade låten
            if (jsonResponse.has("item")) {
                String pausedTrackUri = jsonResponse.getAsJsonObject("item").get("uri").getAsString();
                //kollar om den pausade låtens uri finns i spellistan
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
    public boolean isActiveDevice(String accessToken) throws Exception {
        String apiUrl = baseUrl + "devices";

        //skickar förfrågan och sparar svaret
        HttpResponse<String> response = sendRequest(apiUrl, "GET", accessToken, null);

        //om anropet var framgångsrikt
        if (response.statusCode() == 200) {
            //omvandlar svaret till jsonobjekt
            JsonObject jsonResponse = new Gson().fromJson(response.body(), JsonObject.class);
            //om svaret innehåller nyckeln "devices" med ett värde som är en lista med enheter
            if (jsonResponse.has("devices")) {
                //varje enhet i listas omvandlas till jsonobjekt
                for (var device : jsonResponse.getAsJsonArray("devices")) {
                    JsonObject deviceObj = device.getAsJsonObject();
                    //om objektet har en aktiv enhet returneras true
                    if (deviceObj.has("is_active") && deviceObj.get("is_active").getAsBoolean()) {
                        return true;
                    }
                }
            }
        }
        else {
            //om anropet inte var framgångsrikt
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

        //skickar förfrågan och sparar svaret
        HttpResponse<String> response = sendRequest(apiUrl, "GET", accessToken, null);

        //om anropet var framgångsrikt
        if (response.statusCode() == 200) {
            //omvandlar svaret till jsonobjekt
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            //sparar alla låtar (items) i objektet i en array
            JsonArray tracks = jsonResponse.getAsJsonArray("items");

            List<String> trackUris = new ArrayList<>();
            for (JsonElement track : tracks) {
                //hämtar uri för varje låt i listan och sparar som sträng
                String trackUri = track.getAsJsonObject().getAsJsonObject("track").get("uri").getAsString();
                trackUris.add(trackUri);
            }

            return trackUris;
        } else {
            //om anropet inte var framgångsrikt
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
