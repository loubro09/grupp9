package org.example;

public class WeatherData {
    private String time;
    private String temp;
    private String weatherCode;
    private String locationCor;
    private String locationName;

    public WeatherData(String time, String temp, String weatherCode, String locationCor, String locationName) {
        this.time = time;
        this.temp = temp;
        this.weatherCode = weatherCode;
        this.locationCor = locationCor;
        this.locationName = locationName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getTemp() {
        double temp1 = Double.parseDouble(temp);
        return temp1;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(String weatherCode) {
        this.weatherCode = weatherCode;
    }

    public String getLocationCor() {
        return locationCor;
    }

    public void setLocationCor(String locationCor) {
        this.locationCor = locationCor;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "time='" + time + '\'' +
                ", temp='" + temp + '\'' +
                ", weatherCode='" + weatherCode + '\'' +
                ", locationCor='" + locationCor + '\'' +
                ", locationName='" + locationName + '\'' +
                '}';
    }
    /*
    Få in allt, paketera upp, skicka weather code till analyzer.

    Spara svaret från analyzer så att Spotify kan hämta det. Tolka temperaturen till Spotify.

    Skicka vald information till Response + svaret från Weather Analyzer + Namnet på staden.
     */

}
