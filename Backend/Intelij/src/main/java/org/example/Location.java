package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Location {
    /**
     * Returnerar ett namn baserat på koordinaterna
     * @param coordinates
     * @return namn på plats
     */
    public String getPlaceNameFromCoordinates(String coordinates) {
        //delar upp koordinater
        String[] parts = coordinates.split(",");
        double latitude = Double.parseDouble(parts[0]);
        double longitude = Double.parseDouble(parts[1]);

        //skapar URL för API-anrop
        String apiUrl = String.format("https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&format=json&addressdetails=1&zoom=18", latitude, longitude);
        apiUrl = apiUrl.replace(",", ".");

        //skapa HTTP-förfrågan
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", "grupp9/1.0")
                .build();

        try {
            //skicka HTTP-förfrågan och spara svar
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) { //om anrop var framgångsrikt
                return getAddress(response.body()); //hämtar stad, län och land från svar
            } else {
                return "Fel vid anrop till Nominatim API. Statuskod: " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Fel vid hämtning av platsnamn: " + e.getMessage();
        }
    }

    /**
     * Returnerar stad, län och land från svaret
     * @param jsonResponse
     * @return sträng med stad, län och land
     */
    private String getAddress(String jsonResponse) {
        try {
            //omvandlar svaret till json objekt
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            //hämtar addressinformationen from objektet
            JsonObject address = jsonObject.getAsJsonObject("address");
            String city = address.get("city").getAsString();
            String county = address.get("county").getAsString();
            String country = address.get("country").getAsString();

            return String.format("%s, %s, %s", city, county, country);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
