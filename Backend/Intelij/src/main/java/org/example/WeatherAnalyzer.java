package org.example;


public class WeatherAnalyzer {

    /**
     * Analyzes the weather based on a weather code and temperature and returns name of Spotify Playlist.
     *
     * @param weatherCode the code representing the weather condition (e.g., sunshine, rain, snow).
     * @param temperature the current temperature in degrees Celsius.
     * @return a string representing a playlist based on the weather and season.
     */
    public String analyzeWeather(String weatherCode, double temperature) {
        String season = getSeason(temperature);
        String weatherDescription = getWeatherDescription(weatherCode);

        if (season.equals("Summer") && weatherDescription.equals("Sunshine")) {
            return "Summer Mix";

        } else if (season.equals("SA") && weatherDescription.equals("Sunshine")) {
            return "Sunny Day";

        } else if (season.equals("Winter") && weatherDescription.equals("Sunshine")) {
            return "Sunshine Winter Mix";

        } else if (season.equals("Summer") && weatherDescription.equals("Cloudy")) {
            return "Chill Moody Mix";

        } else if (season.equals("SA") && weatherDescription.equals("Cloudy")) {
            return "Moody Sad Mix";

        } else if (season.equals("Winter") && weatherDescription.equals("Cloudy")) {
            return "Dark Moody Mix";

        } else if (season.equals("Summer") && weatherDescription.equals("Rain")) {
            return "Rainy Day Mix";

        } else if (season.equals("SA") && weatherDescription.equals("Rain")) {
            return "Rainy Day Mix";

        } else if (season.equals("Winter") && weatherDescription.equals("Rain")) {
            return "Rainy Day Mix";
        } else if (season.equals("Summer") && weatherDescription.equals("ThunderStorm")) {
            return "Beast Mode";

        } else if (season.equals("SA") || season.equals("Winter") && weatherDescription.equals("ThunderStorm")) {
            return "Beast Mode";

        } else if ((season.equals("SA") || season.equals("Winter")) && weatherDescription.equals("Snow")) {
            return "Chill Winter Mix";

        } else if ((season.equals("SA") || season.equals("Winter")) && weatherDescription.equals("Cold")) {
            return "Morning Winter Mix";
        } else {
            return "Today's Top Hits";
        }
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
        if (temperature > 19.0) {
            return "Summer";  // Sommar om temperaturen är över 15°C
        } else if (temperature >= 5.0) {
            return "SA";     // Vår/Höst om temperaturen är mellan 5°C och 15°C
        } else {
            return "Winter";  // Vinter om temperaturen är under 5°C
        }
    }
}

