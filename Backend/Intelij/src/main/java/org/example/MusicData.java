package org.example;

import com.google.gson.JsonObject;

public class MusicData {

    private String playlistName;
    private String playlistImage;
    private String songName;
    private String artist;
    private String songImage;

    // Getter och Setter
    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getPlaylistImage() {
        return playlistImage;
    }

    public void setPlaylistImage(String playlistImage) {
        this.playlistImage = playlistImage;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSongImage() {
        return songImage;
    }

    public void setSongImage(String songImage) {
        this.songImage = songImage;
    }

    /**
     * Skapar ett JSON-objekt av musikdata
     * @return JSON-objekt med musikdata
     */
    public JsonObject toJson() {
        JsonObject responseData = new JsonObject();
        responseData.addProperty("playlistName", playlistName != null ? playlistName : "Unknown Playlist");
        responseData.addProperty("playlistImage", playlistImage != null ? playlistImage : "No Image");
        responseData.addProperty("songName", songName != null ? songName : "Unknown Song");
        responseData.addProperty("artist", artist != null ? artist : "Unknown Artist");
        responseData.addProperty("songImage", songImage != null ? songImage : "No Image");
        return responseData;
    }

    /**
     * Exempel: Laddar musikdata från JSON och skapar ett MusicData-objekt
     * @param jsonBody JSON-objekt från API
     */
    public void getMusic(JsonObject jsonBody) {
        if (jsonBody != null) {
            if (jsonBody.has("playlistName")) {
                this.playlistName = jsonBody.get("playlistName").getAsString();
            }
            if (jsonBody.has("playlistImage")) {
                this.playlistImage = jsonBody.get("playlistImage").getAsString(); // Förutsatt att Base64 används
            }
            if (jsonBody.has("songName")) {
                this.songName = jsonBody.get("songName").getAsString();
            }
            if (jsonBody.has("artist")) {
                this.artist = jsonBody.get("artist").getAsString();
            }
            if (jsonBody.has("songImage")) {
                this.songImage = jsonBody.get("songImage").getAsString(); // Förutsatt att Base64 används
            }
        }
    }

    @Override
    public String toString() {
        return "MusicData{" +
                "playlistName='" + playlistName + '\'' +
                ", playlistImage='" + playlistImage + '\'' +
                ", songName='" + songName + '\'' +
                ", artist='" + artist + '\'' +
                ", songImage='" + songImage + '\'' +
                '}';
    }
}
