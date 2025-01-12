
window.onload = function () {
// Sätt default-bakgrund för locationCard och playlistCard
    const defaultBg = "/images/cloudy.jpg"; // Eller vilken default-bild du vill använda

    const locationCard = document.getElementById("locationCard");
    const playlistCard = document.getElementById("playlistCard");

    if (locationCard) {
        locationCard.style.backgroundImage = `linear-gradient(to bottom, rgba(0, 0, 0, 0) 0%, rgba(17, 17, 17, 0.6) 100%), url('${defaultBg}')`;
    }

    if (playlistCard) {
        playlistCard.style.backgroundImage = `linear-gradient(to bottom, rgba(0, 0, 0, 0) 0%, rgba(17, 17, 17, 0.9) 100%), url('${defaultBg}')`;
    }

    // Fetch the currently playing song when the page loads
    fetchCurrentlyPlaying();
    // Periodically refresh the currently playing info every 30 seconds
    setInterval(fetchCurrentlyPlaying, 1000);
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
                    document.getElementById("place").textContent = place;

                    // Hämta väderdata baserat på platsen
                    fetchWeather();
                    fetchPlaylist()
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
        const response = await fetch("http://localhost:5009/weather");

        if (!response.ok) {
            throw new Error(`API-fel vid väder: ${response.status}`);
        }

        const data = await response.json();
        const weatherOutput = data.weatherDescription || "Unknown weather";
        document.getElementById("weather").textContent = weatherOutput;

        const temperatureOutput = `${Math.round(data.temp)} °C` || "Unknown temperature";
               document.getElementById("temperature").textContent = temperatureOutput;

        // Uppdatera väderbilden baserat på väderdata
        updateWeatherImage(data.weatherCode, data.weatherDescription);

        fetchPlaylist(data.weatherCode, data.temp);
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
        const response = await fetch(`http://localhost:5009/coordinates?place=${encodeURIComponent(locationInput)}`);

        if (!response.ok) {
            throw new Error(`API-fel: ${response.status}`);
        }

        const data = await response.json();
        const place = data.place || "Okänd plats";
       document.getElementById("place").textContent = place;

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
    // Uppdatera huvudväderbilden och weatherIcon
    const weatherImage = document.getElementById("weatherImage");
    const weatherInfo = document.getElementById("weatherInfo");
    const weatherIcon = document.getElementById("weather-icon");
        // Hämta rätt weather icon
        if (weatherIcon) {
                weatherIcon.src = `icons/${weatherCode}.png`;
                weatherIcon.alt = `Weather Icon - ${weatherDescription}`;
            }

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


        const response = await fetch("http://localhost:5009/current-song");

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

    }
}
document.getElementById("pause-button").addEventListener("click", async () => {
    await fetch("/playback/pause", { method: "PUT" });
});

document.getElementById("prev-button").addEventListener("click", async () => {
    await fetch("/playback/previous", { method: "POST" });
});

document.getElementById("next-button").addEventListener("click", async () => {
    await fetch("/playback/next", { method: "POST" });
});


document.getElementById("play-button").addEventListener("click", async () => {
    const response = await fetch("/playback/play", { method: "PUT" });
    if (response.status === 400) {
        showPopup();  // Visa popup med felmeddelandet
    }
});

async function fetchPlaylist(weatherCode, temp) {
    try {
    const response = await fetch("/playback/play", { method: "PUT" });

        if (!response.ok) {
            throw new Error(`API-fel vid spellista: ${response.status}`);
        }

        const playlistData = await response.json();
        const playlistName = playlistData.playlistName || "Okänd spellista";
        const playlistImage = playlistData.playlistImage;

        const playlistCard = document.getElementById("playlistCard");

        // Rensa innehållet innan vi lägger till nya element
        playlistCard.innerHTML = "";

        // Skapa en behållare för att centrera bilden
        if (playlistImage) {
            const imageContainer = document.createElement("div");
            imageContainer.style.display = "flex";
            imageContainer.style.justifyContent = "center";
            imageContainer.style.alignItems = "center";
            imageContainer.style.marginBottom = "10px"; // Utrymme mellan bild och rubrik

            // Skapa bild-elementet
            const imgElement = document.createElement("img");
            imgElement.src = playlistImage;
            imgElement.alt = "Playlist Image";
            imgElement.classList.add("playlist__image");

            // Begränsa storlek
            imgElement.style.maxWidth = "100%";
            imgElement.style.maxHeight = "300px";
            imgElement.style.objectFit = "cover";

            // Lägg till bilden i behållaren
            imageContainer.appendChild(imgElement);

            // Lägg till behållaren i playlistCard
            playlistCard.appendChild(imageContainer);
        }

        // Skapa och lägg till rubriken under bilden
        const titleElement = document.createElement("h2");
        titleElement.textContent = playlistName;
        titleElement.style.textAlign = "center"; // Centrera rubriken
        titleElement.style.marginTop = "10px"; // Utrymme mellan rubriken och bilden
        playlistCard.appendChild(titleElement);
    } catch (error) {
        console.error("Fel vid hämtning av spellista:", error);
    }
}


// Funktion för att visa popup
function showPopup() {
    const popup = document.createElement("div");
    popup.classList.add("popup");
    popup.innerHTML = `
            <div class="popup-content">
                <p>Ingen aktiv enhet hittades.</p>
                <p>Du måste först aktivera Spotify på en enhet genom att starta en låt.</p>
                <div class="popup-buttons">
                    <button onclick="closePopup()">OK</button>
                    <a href="https://open.spotify.com/" target="_blank">
                        <button onclick="closePopup()">Öppna Spotify på webben</button>
                    </a>
                </div>
            </div>
        `;
    document.body.appendChild(popup);
}

// Funktion för att stänga popup
function closePopup() {
    const popup = document.querySelector(".popup");
    if (popup) {
        popup.remove();
    }
}