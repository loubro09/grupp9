package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Location {
    public String getPlaceNameFromCoordinates(String coordinates) {
        String[] parts = coordinates.split(",");
        double latitude = Double.parseDouble(parts[0]);
        double longitude = Double.parseDouble(parts[1]);

        String apiUrl = String.format("https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&format=json&addressdetails=1&zoom=18", latitude, longitude);
        apiUrl = apiUrl.replace(",", ".");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", "grupp9/1.0")
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Kontrollera om vi får ett korrekt JSON-svar
                String jsonResponse = response.body();

                // För att extrahera adressnamnet från JSON-svaret
                String placeName = extractPlaceName(jsonResponse);
                return placeName != null ? placeName : "Platsnamn kunde inte hittas.";
            } else {
                return "Fel vid anrop till Nominatim API. Statuskod: " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Fel vid hämtning av platsnamn: " + e.getMessage();
        }
    }

    private String extractPlaceName(String jsonResponse) {
        // För att hitta en lämplig plats i JSON-responsen
        try {
            // Parsar JSON-strängen till ett JsonObject
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Extrahera "address"-objektet från jsonObject
            JsonObject address = jsonObject.getAsJsonObject("address");

            // Exempel: Hämta gatan, stad och land från address-objektet
            String county = address.has("county") ? address.get("county").getAsString() : "Okänt län";
            String city = address.has("city") ? address.get("city").getAsString() : "Okänd stad";
            String country = address.has("country") ? address.get("country").getAsString() : "Okänt land";

            // Sätt ihop och returnera en lämplig plats
            return String.format("%s, %s, %s", city, county, country);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
