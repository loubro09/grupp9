
window.onload = function () {
    // Open Spotify Web Player in a new tab when the page loads
    window.open("https://open.spotify.com/?flow_ctx=e9948630-d52d-48b6-ad66-c1b385b1a927%3A1736272483", "_blank", "width=800,height=600");

    // Fetch the currently playing song when the page loads
    fetchCurrentlyPlaying();

    // Periodically refresh the currently playing info every 30 seconds
    setInterval(fetchCurrentlyPlaying, 30000);
};
// --- Plats- och Väderfunktionalitet ---

// Funktion för att hämta plats och väder vid sidladdning eller via knappar
async function fetchLocationAndWeather() {
    if ("geolocation" in navigator) {
        navigator.geolocation.getCurrentPosition(
            async (position) => {
                const latitude = position.coords.latitude;
                const longitude = position.coords.longitude;

                 const locationData = `${latitude},${longitude}`;

                try {
                    // Skicka platskoordinater till servern
                    const locationResponse = await fetch("http://localhost:5009/location", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                        },
                        body: JSON.stringify(locationData),
                    });

                    if (!locationResponse.ok) {
                        throw new Error(`API-fel vid plats: ${locationResponse.status}`);
                    }

                    const locationDataResponse = await locationResponse.json();
                    const place = locationDataResponse.place || "Okänd plats";
                    document.getElementById("output").textContent = `Plats: ${place}`;

                    // Hämta väderdata baserat på platsen
                    fetchWeather();
                } catch (error) {
                    console.error("Fel vid hämtning av plats:", error);
                    document.getElementById("output").textContent = "Kunde inte hämta plats.";
                }
            },
            (error) => {
                document.getElementById("output").textContent = "Fel vid geolocation: " + error.message;
            }
        );
    } else {
        document.getElementById("output").textContent = "Geolocation stöds inte.";
    }
}

// Funktion för att hämta väderdata
async function fetchWeather() {
    try {
        const response = await fetch("http://localhost:5009/weatherLocation");

        if (!response.ok) {
            throw new Error(`API-fel vid väder: ${response.status}`);
        }

        const data = await response.json();
        const weatherOutput = `
            Plats: ${data.locationName || "Okänd plats"},
            Tid: ${data.time || "Okänd tid"},
            Väderprognos: ${data.weatherDescription || "Okänt väder"},
            Temperatur: ${data.temp || "Okänd temperatur"} °C
        `;
        document.getElementById("weather").textContent = weatherOutput;

        // Uppdatera väderbilden baserat på väderdata
        updateWeatherImage(data.weatherCode, data.weatherDescription);
    } catch (error) {
        console.error("Fel vid hämtning av väderdata:", error);
        document.getElementById("weather").textContent = "Kunde inte hämta väderdata.";
    }
}

// Eventlyssnare för manuellt sök
document.getElementById("fetchCoordinates").addEventListener("click", async () => {
    const locationInput = document.getElementById("manualLocation").value.trim();
    if (locationInput === "") {
        document.getElementById("output").textContent = "Skriv in en giltig plats.";
        return;
    }

    try {
        const response = await fetch(`http://localhost:5009/locationByName?place=${encodeURIComponent(locationInput)}`);
        if (!response.ok) {
            throw new Error(`API-fel: ${response.status}`);
        }

        const data = await response.json();
        const place = data.place || "Okänd plats";
        document.getElementById("output").textContent = `Plats: ${place}`;

        // Hämta väderdata baserat på den sökta platsen
        fetchWeather();
    } catch (error) {
        console.error("Fel vid hämtning av koordinater:", error);
        document.getElementById("output").textContent = "Kunde inte hitta plats.";
    }
});

// Eventlyssnare för att hämta användarens plats via geolokalisering
document.getElementById("getLocation").addEventListener("click", fetchLocationAndWeather);

// Funktion för att uppdatera väderbilden och servicesbilderna
function updateWeatherImage(weatherCode, weatherDescription) {
    // Uppdatera huvudväderbilden
    const weatherImage = document.getElementById("weatherImage");
    const weatherInfo = document.getElementById("weatherInfo");

    let imgSrc = "/images/default.png"; // Fallback-bild

    // Mappa väderkoder till bilder
    switch (weatherCode) {
        case "1000": // Klar himmel
        case "1100": // Mycket klart
            imgSrc = "/images/Sunny.jpg";
            break;
        case "1101": // "Partly Cloudy"
        case "1102": // "Mostly Cloudy"
        case "1001": // "Cloudy"
        case "2000": // "Fog"
        case "2100": // "Light Fog"
            imgSrc = "/images/cloudy.jpg";
            break;
        case "4000": // "Drizzle"
        case "4200": // "Rain"
        case "4001": // "Light Rain"
        case "4201": // "Heavy Rain"
        case "8000": // "Thunderstorm"
            imgSrc = "/images/rain.jpg";
            break;
        case "5000": // "Snow"
        case "5100": // "Flurries"
        case "5101": // "Light Snow"
        case "5001": // "Heavy Snow"
        case "6000": // "Freezing Drizzle"
        case "6001": // "Freezing Rain"
        case "6200": // "Light Freezing Rain"
        case "6201": // "Heavy Freezing Rain"
        case "7000": // "Ice Pellets"
        case "7101": // "Heavy Ice Pellets"
        case "7102": // "Light Ice Pellets"
            imgSrc= "/images/winter.jpg";
        default:
            imgSrc = "/images/default.jpg";
            break;
    }

    // Uppdatera huvudväderbildens src om elementet finns
    if (weatherImage) {
        weatherImage.src = imgSrc;
    }

    // Visa väderbeskrivningen (valfritt) om elementet finns
    if (weatherInfo) {
        weatherInfo.textContent = weatherDescription;
    }

    // Uppdatera bakgrundsbilderna i Services Section baserat på väder
    updateServicesBackgrounds(weatherCode, weatherDescription);
}

// Funktion för att uppdatera bakgrundsbilderna i Services Section
function updateServicesBackgrounds(weatherCode, weatherDescription) {
    const locationCard = document.getElementById("locationCard");
    const playlistCard = document.getElementById("playlistCard");

    let locationBg = "/images/cloudy.jpg"; // Default platsbild
    let playlistBg = "/images/cloudy.jpg"; // Default spellistabild

    // Anpassa bilder baserat på väderkoden
    switch (weatherCode) {
        case "1000": // Klar himmel
        case "1100": // Mycket klart
            locationBg = "/images/Sunny.jpg";
            playlistBg = "/images/Sunny.jpg";
            break;

        case "1101": // Delvis molnigt
        case "1102": // "Mostly Cloudy"
        case "1001": // "Cloudy"
        case "2000": // "Fog"
        case "2100": // "Light Fog"
            locationBg = "/images/cloudy.jpg";
            playlistBg = "/images/cloudy.jpg";
            break;
        case "4000": // "Drizzle"
        case "4200": // "Rain"
        case "4001": // "Light Rain"
        case "4201": // "Heavy Rain"
        case "8000": // "Thunderstorm"
            locationBg = "/images/rain.jpg";
            playlistBg = "/images/rain.jpg";
            break;
        case "5000": // "Snow"
        case "5100": // "Flurries"
        case "5101": // "Light Snow"
        case "5001": // "Heavy Snow"
        case "6000": // "Freezing Drizzle"
        case "6001": // "Freezing Rain"
        case "6200": // "Light Freezing Rain"
        case "6201": // "Heavy Freezing Rain"
        case "7000": // "Ice Pellets"
        case "7101": // "Heavy Ice Pellets"
        case "7102": // "Light Ice Pellets"
            locationBg = "/images/winter.jpg";
            playlistBg = "/images/winter.jpg";
            break;
        default:
            locationBg = "/images/default.jpg"; // Eller annan default bild
            playlistBg = "/images/default.jpg"; // Eller annan default bild
            break;
    }

    // Uppdatera bakgrundsbilderna med gradient och ny bild om elementen finns
    if (locationCard) {
        locationCard.style.backgroundImage = `linear-gradient(to bottom, rgba(0, 0, 0, 0) 0%, rgba(17, 17, 17, 0.6) 100%), url('${locationBg}')`;
    }

    if (playlistCard) {
        playlistCard.style.backgroundImage = `linear-gradient(to bottom, rgba(0, 0, 0, 0) 0%, rgba(17, 17, 17, 0.9) 100%), url('${playlistBg}')`;
    }
}

// --- Music Player Functionality ---

async function fetchCurrentlyPlaying() {
    try {
        const trackTitleElement = document.getElementById("track-title");
        const trackArtistElement = document.getElementById("track-artist");
        const trackImageElement = document.getElementById("track-image");

        if (trackTitleElement) trackTitleElement.textContent = "Loading...";
        if (trackArtistElement) trackArtistElement.textContent = "Please wait...";
        if (trackImageElement) trackImageElement.src = "/images/loading.png";

        const response = await fetch("http://localhost:5009/currently-playing");

        if (!response.ok) {
            if (response.status === 204) {
                if (trackTitleElement) trackTitleElement.textContent = "No song is playing";
                if (trackArtistElement) trackArtistElement.textContent = "";
                if (trackImageElement) trackImageElement.src = "/images/default.png";
                return;
            }
            throw new Error(`API error: ${response.status}`);
        }

        const data = await response.json();

        if (trackTitleElement) trackTitleElement.textContent = data.songName || "Unknown Song";
        if (trackArtistElement) trackArtistElement.textContent = data.artist || "Unknown Artist";
        if (trackImageElement) trackImageElement.src = data.songImage || "/images/default.png";

    } catch (error) {
        console.error("Error fetching currently playing song:", error);
        const trackTitleElement = document.getElementById("track-title");
        const trackArtistElement = document.getElementById("track-artist");
        const trackImageElement = document.getElementById("track-image");

        if (trackTitleElement) trackTitleElement.textContent = "Error fetching song";
        if (trackArtistElement) trackArtistElement.textContent = "";
        if (trackImageElement) trackImageElement.src = "/images/default.png";
    }
}

// Event Listeners for Music Controls
document.addEventListener("DOMContentLoaded", () => {
    const playButton = document.getElementById("play-button");
    const pauseButton = document.getElementById("pause-button");
    const prevButton = document.getElementById("prev-button");
    const nextButton = document.getElementById("next-button");
    const startPlaylistButton = document.getElementById("start-playlist");

    if (playButton) {
        playButton.addEventListener("click", async () => {
            try {
                await fetch("/play-playlist", { method: "PUT" });
                fetchCurrentlyPlaying();
            } catch (error) {
                console.error("Error playing playlist:", error);
            }
        });
    }

    if (pauseButton) {
        pauseButton.addEventListener("click", async () => {
            try {
                await fetch("/pause", { method: "PUT" });
                fetchCurrentlyPlaying();
            } catch (error) {
                console.error("Error pausing music:", error);
            }
        });
    }

    if (prevButton) {
        prevButton.addEventListener("click", async () => {
            try {
                await fetch("/previous", { method: "POST" });
                fetchCurrentlyPlaying();
            } catch (error) {
                console.error("Error going to previous track:", error);
            }
        });
    }

    if (nextButton) {
        nextButton.addEventListener("click", async () => {
            try {
                await fetch("/next", { method: "POST" });
                fetchCurrentlyPlaying();
            } catch (error) {
                console.error("Error going to next track:", error);
            }
        });
    }

    if (startPlaylistButton) {
        startPlaylistButton.addEventListener("click", async () => {
            try {
                await fetch("/start-playlist", { method: "PUT" });
                fetchCurrentlyPlaying();
            } catch (error) {
                console.error("Error starting playlist:", error);
            }
        });
    }
});


