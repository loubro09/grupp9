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

    Spara svaret från analyzer, temperature och code så att Spotify kan hämta det.

    Skicka vald information till Response + svaret från Weather Analyzer + Namnet på staden.
     */

}
