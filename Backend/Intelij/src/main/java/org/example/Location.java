package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Context;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Location {
    private String locationCoordinates;
    private String placeName;

    /**
     *Returnerar addressen till platsen från koordinaterna
     * @param ctx
     */
    public void locationByCoordinates(Context ctx) {
        String body = ctx.body().replace("\"", ""); //tar bort citattecken från sträng

        this.locationCoordinates = body;
        placeName = getPlaceNameFromCoordinates(body); //hämtar addressen

        //skickar addressen till platsen till frontend
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("place", placeName);
        ctx.json(jsonResponse.toString());
    }

    /**
     * Returnerar addressen till platsen från namnet
     * @param ctx
     * @throws UnsupportedEncodingException
     */
    public void locationByName(Context ctx) throws UnsupportedEncodingException {
        String place = ctx.queryParam("place"); //hämtar namnet på platsen från frågan
        locationCoordinates = getCoordinatesFromPlaceName(place); //hämtar koordinaterna till platsen
        placeName = getPlaceNameFromCoordinates(locationCoordinates); //hämtar addressen till platsen

        //skickar addressen till platsen till frontend
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("place", placeName);
        ctx.json(jsonResponse.toString());
    }

    /**
     * Returnerar koordinater baserat på ett platsnamn
     * @param place
     * @return sträng med lat lon
     * @throws UnsupportedEncodingException
     */
    private String getCoordinatesFromPlaceName(String place) throws UnsupportedEncodingException {
        //skriver om platsen ifall det finns mellanslag i
        String encodedPlace = URLEncoder.encode(place, "UTF-8");
        //skapar URL för API-anrop
        String apiUrl = String.format("https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1", encodedPlace);

        //skapa HTTP-förfrågan
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", "grupp9/1.0")
                .build();

        try {
            //skicka HTTP-förfrågan och spara svar
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) { //om anrop var framgångsrikt
                return getCoordinates(response.body()); //hämtar koordinater från svar
            } else {
                System.err.println("Fel vid anrop till Nominatim API. Statuskod: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returnerar ett namn baserat på koordinaterna
     * @param coordinates
     * @return namn på plats
     */
    private String getPlaceNameFromCoordinates(String coordinates) {
        if (coordinates == null) {
            return "You must enter a valid city name."; // Hanterar null innan split()
        }
        try {
            String[] parts = coordinates.split(",");
            if (parts.length < 2) {
                return "Invalid coordinates received.";
            }
        //delar upp koordinater
        double latitude = Double.parseDouble(parts[0]);
        double longitude = Double.parseDouble(parts[1]);

        //skapar URL för API-anrop
        String apiUrl = String.format("https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&format=json&addressdetails=1&zoom=18&accept-language=en", latitude, longitude);
        apiUrl = apiUrl.replace(",", ".");

        //skapa HTTP-förfrågan
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", "grupp9/1.0")
                .build();


            //skicka HTTP-förfrågan och spara svar
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) { //om anrop var framgångsrikt
                return getAddress(response.body()); //hämtar address från svar
            } else {
                return "Fel vid anrop till Nominatim API. Statuskod: " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Fel vid hämtning av platsnamn: " + e.getMessage();
        }
    }

    /**
     * Returnerar stad och land från svaret
     * @param jsonResponse
     * @return sträng med stad, län och land
     */
    private String getAddress(String jsonResponse) {
        try {
            //omvandlar svaret till json objekt
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            //hämtar addressinformationen from objektet
            JsonObject address = jsonObject.getAsJsonObject("address");
            String city = address.has("city") ? address.get("city").getAsString() : null;
            String province =address.has("province") ? address.get("province").getAsString() : null;
            String country = address.has("country") ? address.get("country").getAsString() : null;

            //stad eller provins måste ha skrivits in
            if (city == null || city.isEmpty()) {
                if (province == null || province.isEmpty()) {
                    //return "You must enter a city name.";
                }
            }

            StringBuilder addressString = new StringBuilder();

            //stad eller provins sparas i sträng
            if (city != null && !city.isEmpty()) {
                addressString.append(city);
            }
            else if (province != null && !province.isEmpty()) {
                addressString.append(province);
            }

            //land sparas i sträng
            addressString.append(", ").append(country);

            return addressString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returnerar koordinaterna
     * @param jsonResponse
     * @return koordinaterna som sträng
     */
    private String getCoordinates(String jsonResponse) {
        try {
            if (JsonParser.parseString(jsonResponse).getAsJsonArray().size() == 0) {
                return null; //inga resultat hittades
            }
            //gör om strängen till objekt
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonArray().get(0).getAsJsonObject();

            //hämtar latitud och longitud från objekt
            String lat = jsonObject.get("lat").getAsString();
            String lon = jsonObject.get("lon").getAsString();

            return String.format("%s,%s", lat, lon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getLocationCoordinates() {
        return locationCoordinates;
    }

    public String getPlaceName() {
        return placeName;
    }
}
