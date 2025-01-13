package org.example;

public class WeatherAnalyzer {

    /**
     * Analyserar vädret baserat på en väderkod från Tomorrow.io API och returnerar en spellista
     * @param weatherCode koden som representerar en vädertyp
     * @param temperature temperaturen i celsius
     * @return ID till en spellista på Spotify
     */
    public String analyzeWeather(String weatherCode, double temperature) {
        String season = getSeason(temperature); //hämtar säsongen bereoende på temperaturen
        //hämtar väderbeskrivning baserat på väderkod
        String weatherDescription = getWeatherDescription(weatherCode);

        //jämför väderbeskrivning och säsong för att få en spellista
        if (season.equals("Summer") && weatherDescription.equals("Sunshine")) {
            return "6s2WRJKFow7wGTj4Ogsgwv";

        } else if (season.equals("SA") && weatherDescription.equals("Sunshine")) {
            return "0tdypn7twCk5zLq2tbClJn";

        } else if (season.equals("Winter") && weatherDescription.equals("Sunshine")) {
            return "1CD4pZITYUfEtfuXc2VRa2";

        } else if (season.equals("Summer") && weatherDescription.equals("Cloudy")) {
            return "7jO7rEKc7j1CUjpB9QcbCX";

        } else if (season.equals("SA") && weatherDescription.equals("Cloudy")) {
            return "1KcOgxwD7Lj44lfb2KbO3f";

        } else if (season.equals("Winter") && weatherDescription.equals("Cloudy")) {
            return "3zhLD3MnatY98mrKI4O33G";

        } else if (season.equals("Summer") && weatherDescription.equals("Rain")) {
            return "4p7lCzu4FNwPlyUtfm3mXY";

        } else if (season.equals("SA") && weatherDescription.equals("Rain")) {
            return "0nEchTvkwkbzJ54p5aTT7X";

        } else if (season.equals("Winter") && weatherDescription.equals("Rain")) {
            return "2yIAa15P5OsuxOCBbQ98aP";

        } else if (season.equals("Summer") && weatherDescription.equals("ThunderStorm")) {
            return "1XZNnIESn9lDcLY7WzItO4";

        } else if (season.equals("SA") || season.equals("Winter") && weatherDescription.equals("ThunderStorm")) {
            return "1XZNnIESn9lDcLY7WzItO4";

        } else if ((season.equals("SA") || season.equals("Winter")) && weatherDescription.equals("Snow")) {
            return "2o0fV3Wo2MtDfi0dLKJqv2";

        } else if ((season.equals("SA") || season.equals("Winter")) && weatherDescription.equals("Cold")) {
            return "6UUCrjB00LEiGvhCWtxduy";

        } else if (season.equals("Tropical") && weatherDescription.equals("Rain")) {
            return "4p7lCzu4FNwPlyUtfm3mXY";

        } else if (season.equals("Tropical") && weatherDescription.equals("Sunshine")) {
            return "3fVXnGdrsw7bKUmUd3OxFv";

        } else if (season.equals("Tropical") && weatherDescription.equals("Cloudy")) {
            return "6E0RTtaSREqhZJQgAWEFqt";

        }else {
            return "6i2Qd6OpeRBAzxfscNXeWp";
        }
    }

    /**
     * Returnerar en väderbeskrivning baserat på väderkoden
     * @param weatherCode
     * @return väderbeskrivning
     */
    public String getWeatherDescription(String weatherCode) {
        switch (weatherCode) {
            case "1000": //"Clear, Sunny"
            case "1100": //"Mostly Clear"
                return "Sunshine";
            case "1101": //"Partly Cloudy"
            case "1102": //"Mostly Cloudy"
            case "1001": //"Cloudy"
            case "2000": //"Fog"
            case "2100": //"Light Fog"
                return "Cloudy";
            case "4000":// "Drizzle"
            case "4200":// "Rain"
            case "4001":// "Light Rain"
            case "4201":// "Heavy Rain"
                return "Rain";
            case "8000": //"Thunderstorm"
                return "Thunderstorm";
            case "5000": //"Snow"
            case "5100": //"Flurries"
            case "5101": //"Light Snow"
            case "5001": //"Heavy Snow"
                return "Snow";
            case "6000": //"Freezing Drizzle"
            case "6001": //"Freezing Rain"
            case "6200": //"Light Freezing Rain"
            case "6201": //"Heavy Freezing Rain"
            case "7000": //"Ice Pellets"
            case "7101": //"Heavy Ice Pellets"
            case "7102": //"Light Ice Pellets"
                return "Cold";
            case "0": //Unknown
                return "Unknown";
            default:
                return "Deafult";
        }
    }


    /**
     * Returnerar en säsong beroende på temperaturen
     * @param temperature
     * @return säsongen
     */
    private String getSeason(double temperature) {
        //om temp är null
        if(temperature == 10000.0){
            return "default";
        }

        if (temperature >30.0){
            return "Tropical"; //tropiskt klimat om temperaturen är över 30 °C
        }
        else if (temperature > 19.0) {
            return "Summer";  //sommar om temperaturen är över 15°C
        } else if (temperature >= 5.0) {
            return "SA";     //vår/höst om temperaturen är mellan 5°C och 15°C
        } else {
            return "Winter";  // vinter om temperaturen är under 5°C
        }
    }
}

