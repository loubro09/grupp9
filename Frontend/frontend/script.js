window.onload = function () {
    //default-bakgrund för locationCard och playlistCard
    const defaultBg = "/images/cloudy.jpg";

    const locationCard = document.getElementById("locationCard");
    const playlistCard = document.getElementById("playlistCard");

    if (locationCard) {
        locationCard.style.backgroundImage = `linear-gradient(to bottom, rgba(0, 0, 0, 0) 0%, rgba(17, 17, 17, 0.6) 100%), url('${defaultBg}')`;
    }

    if (playlistCard) {
        playlistCard.style.backgroundImage = `linear-gradient(to bottom, rgba(0, 0, 0, 0) 0%, rgba(17, 17, 17, 0.9) 100%), url('${defaultBg}')`;
    }

    //hämta nuvarande låt info
    fetchCurrentlyPlaying();
    //uppdatera låtinfo kontinuerligt
    setInterval(fetchCurrentlyPlaying, 1000);
};
//--- Plats- och Väderfunktionalitet ---

//funktion för att hämta plats och väder vid sidladdning eller via knappar
async function fetchLocationAndWeather() {
    if ("geolocation" in navigator) {
        navigator.geolocation.getCurrentPosition(
            async (position) => {
                const latitude = position.coords.latitude;
                const longitude = position.coords.longitude;

                 const locationData = `${latitude},${longitude}`;

                try {
                    //skicka platskoordinater till servern
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

                    //hämta väderdata baserat på platsen
                    fetchWeather();
                    fetchPlaylist()
                } catch (error) {
                   // console.error("Fel vid hämtning av plats:", error);
                }
            },
        );
    }
}

//funktion för att hämta väderdata
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

        //uppdatera väderbilden baserat på väderdata
        updateWeatherImage(data.weatherCode, data.weatherDescription);

        fetchPlaylist(data.weatherCode, data.temp);
    } catch (error) {
        //console.error("Fel vid hämtning av väderdata:", error);
        document.getElementById("weather").textContent = "Could not get weather information.";
    }
}

//funktion för att hämta koordinater baserat på platsnamn
document.getElementById("fetchCoordinates").addEventListener("click", async () => {
    const locationInput = document.getElementById("manualLocation").value.trim();

    if (locationInput === "") {
        return;
    }

    try {
        const response = await fetch(`http://localhost:5009/coordinates?place=${encodeURIComponent(locationInput)}`);

        if (!response.ok) {
            throw new Error(`API-fel: ${response.status}`);
        }

        const data = await response.json();
        const place = data.place || "Okänd plats";

        //skriv ut platsnamn
        document.getElementById("place").textContent = place;

        //hämta väderdata baserat på den sökta platsen
        fetchWeather();
    } catch (error) {
       // console.error("Fel vid hämtning av koordinater:", error);
    }
});

//eventlyssnare för att hämta användarens plats via geolokalisering
document.getElementById("getLocation").addEventListener("click", fetchLocationAndWeather);

//funktion för att uppdatera väderbilden och servicesbilderna
function updateWeatherImage(weatherCode, weatherDescription) {
    // Uppdatera huvudväderbilden och weatherIcon
    const weatherImage = document.getElementById("weatherImage");
    const weatherInfo = document.getElementById("weatherInfo");
    const weatherIcon = document.getElementById("weather-icon");
        //hämta weather icon
        if (weatherIcon) {
                weatherIcon.src = `icons/${weatherCode}.png`;
                weatherIcon.alt = `Weather Icon - ${weatherDescription}`;
                }

    let imgSrc = "/images/default.png"; //default bild

    //mappa väderkoder till bilder
    switch (weatherCode) {
        case "1000": //"Mostly clear Sky"
        case "1100": //"Clear sky"
            imgSrc = "/images/Sunny.jpg";
            break;
        case "1101": //"Partly Cloudy"
        case "1102": //"Mostly Cloudy"
        case "1001": //"Cloudy"
        case "2000": //"Fog"
        case "2100": //"Light Fog"
            imgSrc = "/images/cloudy.jpg";
            break;
        case "4000": //"Drizzle"
        case "4200": //"Rain"
        case "4001": //"Light Rain"
        case "4201": //"Heavy Rain"
        case "8000": //"Thunderstorm"
            imgSrc = "/images/rain.jpg";
            break;
        case "5000": //"Snow"
        case "5100": //"Flurries"
        case "5101": //"Light Snow"
        case "5001": //"Heavy Snow"
        case "6000": //"Freezing Drizzle"
        case "6001": //"Freezing Rain"
        case "6200": //"Light Freezing Rain"
        case "6201": //"Heavy Freezing Rain"
        case "7000": //"Ice Pellets"
        case "7101": //"Heavy Ice Pellets"
        case "7102": //"Light Ice Pellets"
            imgSrc= "/images/winter.jpg";
        default:
            imgSrc = "/images/default.jpg";
            break;
    }

    //uppdaterar väderbildens src om elementet finns
    if (weatherImage) {
        weatherImage.src = imgSrc;
    }

    //visar väderbeskrivningen  om elementet finns
    if (weatherInfo) {
        weatherInfo.textContent = weatherDescription;
    }
        //uppdaterar bakgrundsbilderna baserat på väder
        updateServicesBackgrounds(weatherCode, weatherDescription);
}

//funktion för att uppdatera bakgrundsbilderna
function updateServicesBackgrounds(weatherCode, weatherDescription) {
    const locationCard = document.getElementById("locationCard");
    const playlistCard = document.getElementById("playlistCard");

    let locationBg = "/images/cloudy.jpg"; //default bild väder
    let playlistBg = "/images/cloudy.jpg"; //default bild spellista

    //anpassar bilder baserat på väderkoden
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
            locationBg = "/images/default.jpg";
            playlistBg = "/images/default.jpg";
            break;
    }
    //uppdaterar bakgrundsbilderna med bild om elementen finns
    if (locationCard) {
        locationCard.style.backgroundImage = `linear-gradient(to bottom, rgba(0, 0, 0, 0) 0%, rgba(17, 17, 17, 0.6) 100%), url('${locationBg}')`;
    }

    if (playlistCard) {
        playlistCard.style.backgroundImage = `linear-gradient(to bottom, rgba(0, 0, 0, 0) 0%, rgba(17, 17, 17, 0.9) 100%), url('${playlistBg}')`;
    }
}

//--- Music Player ---
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
       // console.error("Error fetching currently playing song:", error);
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
        showPopup();
    } else {
        document.getElementById("play-notification").style.display = "none"; //ta bort meddelande när musik börjat spelas
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

        //rensar innehållet innan vi lägger till nya element
        playlistCard.innerHTML = "";

        //skapar ruta för spellista
        if (playlistImage) {
            const imageContainer = document.createElement("div");
            imageContainer.style.display = "flex";
            imageContainer.style.justifyContent = "center";
            imageContainer.style.alignItems = "center";
            imageContainer.style.marginBottom = "10px";

            //skapar bild-elementet till spellistan
            const imgElement = document.createElement("img");
            imgElement.src = playlistImage;
            imgElement.alt = "Playlist Image";
            imgElement.classList.add("playlist__image");

            //storleksanpassning
            imgElement.style.maxWidth = "100%";
            imgElement.style.maxHeight = "300px";
            imgElement.style.objectFit = "cover";

            //lägger till bilden i rutan
            imageContainer.appendChild(imgElement);

            //lägger till rutan i playlistCard
            playlistCard.appendChild(imageContainer);
        }

        //rubriken under bilden
        const titleElement = document.createElement("h2");
        titleElement.textContent = playlistName;
        titleElement.style.textAlign = "center";
        titleElement.style.marginTop = "10px";
        playlistCard.appendChild(titleElement);
    } catch (error) {
       // console.error("Fel vid hämtning av spellista:", error);
    }
}

//funktion för att visa popup för Spotify
function showPopup() {
    const popup = document.createElement("div");
    popup.classList.add("popup");
    popup.innerHTML = `
            <div class="popup-content">
                <p>Ingen aktiv enhet hittades.</p>
                <p>Du måste först aktivera Spotify på en enhet genom att starta en låt. Klicka på play knappen för att börja lyssna! </p>
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

//funktion för att stänga popup
function closePopup() {
    const popup = document.querySelector(".popup");
    if (popup) {
        popup.remove();
    }
}