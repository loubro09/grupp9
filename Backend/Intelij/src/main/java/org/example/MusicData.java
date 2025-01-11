package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Context;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


/**
 * Denna klass hanterar musikdataoperationer som att hämta spellisteinformation och
 * hämta den aktuellt spelande låten från Spotify API.
 */
public class MusicData {

    private String playlistName;
    private String playlistImage;
    private String songName;
    private String artist;
    private String songImage;


    /**
     * Hämtar detaljer om en spellista från Spotify API och skickar tillbaka informationen till klienten.
     *
     * @param ctx         HTTP-kontext från Javalin, används för att svara till klienten.
     * @param playlistId  ID för spellistan som ska hämtas.
     * @param accessToken Spotify API access-token för autentisering.
     */
    public void fetchPlaylistData(Context ctx, String playlistId, String accessToken) {
        // Första anropet: Hämta spellistans namn och låtdata
        String apiUrl = "https://api.spotify.com/v1/playlists/" + playlistId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                ctx.status(response.statusCode()).result("Error fetching playlist: " + response.body());
                return;
            }

            // Bearbeta spellistans data
            String jsonResponse = response.body();
            processPlaylistData(jsonResponse);

            // Hämta spellistans bild
            String playlistImage = fetchPlaylistImage(playlistId, accessToken);

            // Skicka svar med spellistans namn och bild
            JsonObject responseData = new JsonObject();
            responseData.addProperty("playlistName", playlistName);
            responseData.addProperty("playlistImage", playlistImage);
            ctx.json(responseData.toString());

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error processing playlist data.");
        }
    }

    /**
     * Hämtar spellistans bild från Spotify API.
     *
     * @param playlistId Spellistans ID.
     * @param accessToken Åtkomsttoken för Spotify API.
     * @return Bildens URL.
     */
    private String fetchPlaylistImage(String playlistId, String accessToken) {
        String apiUrl = "https://api.spotify.com/v1/playlists/" + playlistId + "/images";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "No Image URL"; // Returnera default om det finns ett fel
            }

            // Parsar JSON-svaret för att få bildens URL
            JsonArray images = JsonParser.parseString(response.body()).getAsJsonArray();
            if (images.size() > 0) {
                JsonObject firstImage = images.get(0).getAsJsonObject();
                return firstImage.has("url") ? firstImage.get("url").getAsString() : "No Image URL";
            } else {
                return "No Image URL"; // Returnera default om ingen bild finns
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "No Image URL"; // Returnera default om något går fel
        }
    }

    /**
     * Bearbetar JSON-svaret från Spotify API för att extrahera information om spellistan.
     *
     * @param jsonResponse JSON-svaret från Spotify API.
     */
    private void processPlaylistData(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Hämta spellistans namn
        playlistName = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "Unknown Playlist";

        // Hämta spårinformation som tidigare
        JsonArray tracks = jsonObject.getAsJsonObject("tracks").getAsJsonArray("items");
        if (tracks.size() > 0) {
            JsonObject trackObject = tracks.get(0).getAsJsonObject().getAsJsonObject("track");
            songName = trackObject.has("name") ? trackObject.get("name").getAsString() : "Unknown Song";
            artist = trackObject.getAsJsonArray("artists").size() > 0
                    ? trackObject.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString()
                    : "Unknown Artist";
        } else {
            songName = "No Songs";
            artist = "Unknown Artist";
            songImage = "No Image";
        }
    }


    /**
     * Hämtar information om den aktuellt spelande låten från Spotify API och skickar tillbaka informationen till klienten.
     *
     * @param ctx         HTTP-kontext från Javalin, används för att svara till klienten.
     * @param accessToken Spotify API access-token för autentisering.
     */
    public void fetchCurrentlyPlaying(Context ctx, String accessToken) {
        String apiUrl = "https://api.spotify.com/v1/me/player/currently-playing";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String jsonResponse = response.body();
                processCurrentlyPlaying(jsonResponse);

                JsonObject responseData = new JsonObject();
                responseData.addProperty("songName", songName);
                responseData.addProperty("artist", artist);
                responseData.addProperty("songImage", songImage);

                ctx.json(responseData.toString());
            } else if (response.statusCode() == 204) { // Ingen låt spelas
                ctx.status(204).result("No song is currently playing.");
            } else {
                ctx.status(response.statusCode()).result("Error fetching currently playing: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error fetching currently playing song.");
        }
    }


    /**
     * Bearbetar JSON-svaret från Spotify API för att extrahera information om den aktuellt spelande låten.
     *
     * @param jsonResponse JSON-svaret från Spotify API.
     */
    private void processCurrentlyPlaying(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Extrahera låtens namn och artist
        if (jsonObject.has("item")) {
            JsonObject track = jsonObject.getAsJsonObject("item");
            songName = track.has("name") ? track.get("name").getAsString() : "Unknown Song";
            artist = track.getAsJsonArray("artists").size() > 0
                    ? track.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString()
                    : "Unknown Artist";

            // Extrahera bild för låten
            JsonArray images = track.getAsJsonObject("album").getAsJsonArray("images");
            songImage = images.size() > 0
                    ? images.get(0).getAsJsonObject().get("url").getAsString()
                    : "No Image";
        } else {
            songName = "No Song";
            artist = "Unknown Artist";
            songImage = "No Image";
        }
    }

}
