// script.js

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

// --- Musikspelare Funktionalitet ---

// Funktion för att få access token från backend
function getAccessToken() {
    // Här antar vi att accessToken är lagrad i sessionStorage eller någon annan säker plats
    return sessionStorage.getItem('access_token') || '';
}

// Funktion för att uppdatera access_token
async function refreshAccessToken() {
    try {
        const response = await fetch("/refresh-token", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            }
            // Inga body behövs eftersom backend använder refreshToken från LoginController
        });

        if (response.ok) {
            const data = await response.json();
            if (data.access_token) {
                sessionStorage.setItem('access_token', data.access_token);
                console.log('Access token uppdaterat');
                // Uppdatera token expiry tid, anta 3600 sekunder
                const expiryTime = new Date().getTime() + (3600 * 1000);
                sessionStorage.setItem('token_expiry', expiryTime);
            }
        } else {
            console.error('Fel vid token refresh:', response.status);
        }
    } catch (error) {
        console.error('Fel vid token refresh:', error);
    }
}

// Kontrollera om access_token är nära att löpa ut och uppdatera den
setInterval(() => {
    const tokenExpiry = sessionStorage.getItem('token_expiry');
    if (tokenExpiry) {
        const currentTime = new Date().getTime();
        if (currentTime > tokenExpiry - 60000) { // Om token löper ut inom 1 minut
            refreshAccessToken();
        }
    }
}, 60000); // Kontrollera varje minut

// Hämta musikspelare-element
const playButton = document.getElementById("play-button");
const pauseButton = document.getElementById("pause-button");
const prevButton = document.getElementById("prev-button");
const nextButton = document.getElementById("next-button");
const trackImage = document.getElementById("track-image");
const trackTitle = document.getElementById("track-title");
const trackArtist = document.getElementById("track-artist");
const seekBar = document.getElementById("seek-bar");
const currentTimeEl = document.getElementById("current-time");
const durationEl = document.getElementById("duration");

let isPlaying = false;

// Funktion för att uppdatera spårinfo
function updateTrackInfo(track) {
    if (track.album && track.album.images && track.album.images.length > 0) {
        trackImage.src = track.album.images[0].url;
    } else {
        trackImage.src = "/images/default.png"; // Fallback
    }
    trackTitle.textContent = track.name;
    trackArtist.textContent = track.artists.map(artist => artist.name).join(", ");
}

// Funktion för att spela upp
async function playTrack() {
    try {
        const response = await fetch("/play-playlist", {
            method: "PUT",
            headers: {
                'Authorization': 'Bearer ' + getAccessToken(),
                'Content-Type': 'application/json'
            },
            // Skicka spellista-ID som JSON. Här kan du justera beroende på hur din backend hanterar playlistId.
            body: JSON.stringify("DIN_PLAYLIST_ID") // Ersätt med rätt spellista-ID eller dynamisk värde
        });

        if (response.status === 204) {
            isPlaying = true;
            playButton.style.display = 'none';
            pauseButton.style.display = 'inline-block';
            trackImage.classList.add('playing'); // Lägg till klassen för rotation
            fetchCurrentTrack();
        } else {
            console.error('Fel vid uppspelning:', response.status);
        }
    } catch (error) {
        console.error('Fel vid uppspelning:', error);
    }
}

// Funktion för att pausa
async function pauseTrack() {
    try {
        const response = await fetch("/pause", {
            method: "PUT",
            headers: {
                'Authorization': 'Bearer ' + getAccessToken()
            }
        });

        if (response.status === 204) {
            isPlaying = false;
            playButton.style.display = 'inline-block';
            pauseButton.style.display = 'none';
            trackImage.classList.remove('playing'); // Ta bort klassen för rotation
        } else {
            console.error('Fel vid paus:', response.status);
        }
    } catch (error) {
        console.error('Fel vid paus:', error);
    }
}

// Funktion för att hämta aktuell spår
async function fetchCurrentTrack() {
    try {
        const response = await fetch("/current-track-cover", { // Använd din backend endpoint
            headers: {
                'Authorization': 'Bearer ' + getAccessToken()
            }
        });

        if (response.ok) {
            const data = await response.json();
            if (data.coverUrl) {
                trackImage.src = data.coverUrl;
            }
            if (data.title) {
                trackTitle.textContent = data.title;
            }
            if (data.artist) {
                trackArtist.textContent = data.artist;
            }
        } else {
            console.error('Fel vid hämtning av aktuell spår:', response.status);
        }
    } catch (error) {
        console.error('Fel vid hämtning av aktuell spår:', error);
    }
}

// Funktion för att hantera seek
async function seekTrack(position) {
    try {
        const response = await fetch(`/seek?position_ms=${position}`, {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + getAccessToken()
            }
        });

        if (!response.ok) {
            console.error('Fel vid seek:', response.status);
        }
    } catch (error) {
        console.error('Fel vid seek:', error);
    }
}

// Konvertera millisekunder till tidformat
function msToTime(duration) {
    let seconds = Math.floor((duration / 1000) % 60),
        minutes = Math.floor((duration / (1000 * 60)) % 60),
        hours = Math.floor((duration / (1000 * 60 * 60)) % 24);

    hours = (hours < 10) ? "0" + hours : hours;
    minutes = (minutes < 10) ? "0" + minutes : minutes;
    seconds = (seconds < 10) ? "0" + seconds : seconds;

    return (hours > 0 ? hours + ":" : "") + minutes + ":" + seconds;
}

// Uppdatera progress bar
async function updateProgressBar() {
    try {
        const response = await fetch("/current-track-status", { // Ny backend endpoint
            headers: {
                'Authorization': 'Bearer ' + getAccessToken()
            }
        });

        if (response.ok) {
            const data = await response.json();
            if (data && data.item) {
                const progress = data.progress_ms;
                const duration = data.item.duration_ms;

                seekBar.max = duration;
                seekBar.value = progress;

                currentTimeEl.textContent = msToTime(progress);
                durationEl.textContent = msToTime(duration);

                // Uppdatera progress bar gradient
                const percentage = (progress / duration) * 100;
                seekBar.style.background = `linear-gradient(to right, #1db954 0%, #1db954 ${percentage}%, #555 ${percentage}%, #555 100%)`;
            }
        } else {
            console.error('Fel vid uppdatering av progress bar:', response.status);
        }
    } catch (error) {
        console.error('Fel vid uppdatering av progress bar:', error);
    }
}

// Hantera seek-bar ändringar
seekBar.addEventListener("input", () => {
    const position = seekBar.value;
    seekTrack(position);
});

// Uppdatera progress bar regelbundet
setInterval(updateProgressBar, 1000);

// Event listeners för musikspelaren
playButton.addEventListener("click", playTrack);
pauseButton.addEventListener("click", pauseTrack);
prevButton.addEventListener("click", async () => {
    try {
        const response = await fetch("/previous", {
            method: "POST",
            headers: {
                'Authorization': 'Bearer ' + getAccessToken()
            }
        });

        if (response.status === 204) {
            fetchCurrentTrack();
        } else {
            console.error('Fel vid föregående spår:', response.status);
        }
    } catch (error) {
        console.error('Fel vid föregående spår:', error);
    }
});
nextButton.addEventListener("click", async () => {
    try {
        const response = await fetch("/next", {
            method: "POST",
            headers: {
                'Authorization': 'Bearer ' + getAccessToken()
            }
        });

        if (response.status === 204) {
            fetchCurrentTrack();
        } else {
            console.error('Fel vid nästa spår:', response.status);
        }
    } catch (error) {
        console.error('Fel vid nästa spår:', error);
    }
});

// Hantera tokens efter inloggning
window.addEventListener("load", () => {
    const params = getQueryParams();
    const accessToken = params['access_token'];
    const refreshToken = params['refresh_token'];
    const expiresIn = params['expires_in'];

    if (accessToken) {
        sessionStorage.setItem('access_token', accessToken);
        if (refreshToken) {
            sessionStorage.setItem('refresh_token', refreshToken);
        }
        // Spara token expiry tid
        const expiryTime = new Date().getTime() + (parseInt(expiresIn) * 1000);
        sessionStorage.setItem('token_expiry', expiryTime);
        // Rensa URL hash
        window.location.hash = '';
        // Hämta aktuell låt och uppdatera UI
        fetchCurrentTrack();
        updateProgressBar();
    }
});
