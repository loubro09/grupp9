package org.example;

import kong.unirest.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Location {
    public String getPlaceNameFromCoordinates(String coordinates) {
        String[] parts = coordinates.split(",");
        double latitude = Double.parseDouble(parts[0]);
        double longitude = Double.parseDouble(parts[1]);


        System.out.println(latitude);
        System.out.println(longitude);

        // Nominatim API URL med alla parametrar
        String apiUrl = String.format("https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&format=json&addressdetails=1&zoom=18", latitude, longitude);

        apiUrl = apiUrl.replace(",", ".");

        System.out.println(apiUrl);
        // Skapa HTTP-request med korrekt User-Agent header
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", "GeolocationApp/1.0")  // Skicka en tydlig User-Agent
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
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject address = jsonObject.getJSONObject("address");

            // Exempel: Hämta gatan, stad och land
            String road = address.optString("road", "Okänd väg");
            String city = address.optString("city", "Okänd stad");
            String country = address.optString("country", "Okänt land");

            // Sätt ihop och returnera en lämplig plats
            return String.format("%s, %s, %s", road, city, country);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
