package org.example;


public class WeatherAnalyzer {

    /**
     * Analyzes the weather based on a weather code and temperature and returns name of Spotify Playlist.
     *
     * @param weatherCode the code representing the weather condition (e.g., sunshine, rain, snow).
     * @param temperature the current temperature in degrees Celsius.
     * @return a string representing a playlist based on the weather and season.
     */
    public String[] analyzeWeather(String weatherCode, double temperature) {
        String season = getSeason(temperature);
        String weatherDescription = getWeatherDescription(weatherCode);

        // Return Spotify playlist and image filename
        String playlistId = "";
        String imageFilename = getImageFilename(weatherCode);

        if (season.equals("Summer") && weatherDescription.equals("Sunshine")) {
            playlistId = "6s2WRJKFow7wGTj4Ogsgwv";
        } else if (season.equals("SA") && weatherDescription.equals("Sunshine")) {
            playlistId = "0tdypn7twCk5zLq2tbClJn";
        } else if (season.equals("Winter") && weatherDescription.equals("Sunshine")) {
            playlistId = "1CD4pZITYUfEtfuXc2VRa2";
        } else if (season.equals("Summer") && weatherDescription.equals("Cloudy")) {
            playlistId = "7jO7rEKc7j1CUjpB9QcbCX";
        } else if (season.equals("SA") && weatherDescription.equals("Cloudy")) {
            playlistId = "1KcOgxwD7Lj44lfb2KbO3f";
        } else if (season.equals("Winter") && weatherDescription.equals("Cloudy")) {
            playlistId = "3zhLD3MnatY98mrKI4O33G";
        } else if (season.equals("Summer") && weatherDescription.equals("Rain")) {
            playlistId = "4p7lCzu4FNwPlyUtfm3mXY";
        } else if (season.equals("SA") && weatherDescription.equals("Rain")) {
            playlistId = "0nEchTvkwkbzJ54p5aTT7X";
        } else if (season.equals("Winter") && weatherDescription.equals("Rain")) {
            playlistId = "2yIAa15P5OsuxOCBbQ98aP";
        } else if (season.equals("Summer") && weatherDescription.equals("Thunderstorm")) {
            playlistId = "1XZNnIESn9lDcLY7WzItO4";
        } else if ((season.equals("SA") || season.equals("Winter")) && weatherDescription.equals("Thunderstorm")) {
            playlistId = "1XZNnIESn9lDcLY7WzItO4";
        } else if ((season.equals("SA") || season.equals("Winter")) && weatherDescription.equals("Snow")) {
            playlistId = "2o0fV3Wo2MtDfi0dLKJqv2";
        } else if ((season.equals("SA") || season.equals("Winter")) && weatherDescription.equals("Cold")) {
            playlistId = "6UUCrjB00LEiGvhCWtxduy";
        } else if (season.equals("Tropical") && weatherDescription.equals("Rain")) {
            playlistId = "4p7lCzu4FNwPlyUtfm3mXY";
        } else if (season.equals("Tropical") && weatherDescription.equals("Sunshine")) {
            playlistId = "3fVXnGdrsw7bKUmUd3OxFv";
        } else if (season.equals("Tropical") && weatherDescription.equals("Cloudy")) {
            playlistId = "6E0RTtaSREqhZJQgAWEFqt";
        } else {
            playlistId = "6i2Qd6OpeRBAzxfscNXeWp";
        }

        return new String[]{playlistId, imageFilename};
    }


    /**
     * Returns a description of the weather condition based on the provided weather code.
     *
     * @param weatherCode the code representing the weather condition.
     * @return a string describing the weather condition (e.g., Sunshine, Rain, Snow).
     */
    public String getWeatherDescription(String weatherCode) {
        switch (weatherCode) {
            case "1000": // "Clear, Sunny"
            case "1100": // "Mostly Clear"
                return "Sunshine";
            case "1101": // "Partly Cloudy"
            case "1102": // "Mostly Cloudy"
            case "1001": // "Cloudy"
            case "2000": // "Fog"
            case "2100": // "Light Fog"
                return "Cloudy"; // Mostly Clear, Partly Cloudy
            case "4000": // "Drizzle"
            case "4200": // "Rain"
            case "4001": // "Light Rain"
            case "4201": // "Heavy Rain"
                return "Rain";
            case "8000": // "Thunderstorm"
                return "Thunderstorm";
            case "5000": // "Snow"
            case "5100": // "Flurries"
            case "5101": // "Light Snow"
            case "5001": // "Heavy Snow"
                return "Snow";
            case "6000": // "Freezing Drizzle"
            case "6001": // "Freezing Rain"
            case "6200": // "Light Freezing Rain"
            case "6201": // "Heavy Freezing Rain"
            case "7000": // "Ice Pellets"
            case "7101": // "Heavy Ice Pellets"
            case "7102": // "Light Ice Pellets"
                return "Cold";
            case "0": // Unknown
                return "Unknown";
            default:
                return "Deafult";
        }
    }


    /**
     * Determines the season based on the temperature.
     *
     * @param temperature the current temperature in degrees Celsius.
     * @return a string representing the season (e.g., Summer, SA (Spring/Autumn), Winter).
     */
    private String getSeason(double temperature) {
        if (temperature >30.0){
            return "Tropical"; //Tropiskt klimat om temperaturen är över 30 °C
        }
        else
         if (temperature > 19.0) {

            return "Summer";  // Sommar om temperaturen är över 15°C
        } else if (temperature >= 5.0) {
            return "SA";     // Vår/Höst om temperaturen är mellan 5°C och 15°C
        } else {
            return "Winter";  // Vinter om temperaturen är under 5°C
        }
    }

    private String getImageFilename(String weatherCode) {
        return weatherCode + ".png";
    }

}

