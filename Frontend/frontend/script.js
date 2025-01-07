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

async function fetchWeather() {
    try {
        const response = await fetch("http://localhost:5009/weatherLocation");

        if (!response.ok) {
            throw new Error(`API-fel vid väder: ${response.status}`);
        }

        const data = await response.json();
        const weatherOutput = `
            Place: ${data.locationName || "Okänd plats"},
            Time: ${data.time || "Okänd tid"},
            Weather forecast: ${data.weatherDescription || "Okänt väder"},
            Temperature: ${data.temp || "Okänd temperatur"} °C
        `;
        document.getElementById("weather").textContent = weatherOutput;
    } catch (error) {
        console.error("Fel vid hämtning av väderdata:", error);
        document.getElementById("weather").textContent = "Kunde inte hämta väderdata.";
    }
}

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

// Kör plats- och väderhämtning vid inloggning (sidladdning)
window.addEventListener("load", fetchLocationAndWeather);
document.getElementById("getLocation").addEventListener("click", fetchLocationAndWeather);

document.getElementById("play-button").addEventListener("click", async () => {
    await fetch("/play-playlist", { method: "PUT" });
});

document.getElementById("pause-button").addEventListener("click", async () => {
    await fetch("/pause", { method: "PUT" });
});

document.getElementById("prev-button").addEventListener("click", async () => {
    await fetch("/previous", { method: "POST" });
});

document.getElementById("next-button").addEventListener("click", async () => {
    await fetch("/next", { method: "POST" });
});
// Progressindikator
function updateProgress() {
    fetch('/track-progress')
        .then(response => response.json())
        .then(progress => {
            const progressBar = document.getElementById('track-progress');
            const trackDuration = 300000; // exempel på total längd på låt i millisekunder
            const progressPercentage = (progress / trackDuration) * 100;
            progressBar.value = progressPercentage;
        });
}

// Funktion för att skicka 'seek' begäran till Spotify
function seekToPosition(position_ms) {
    const accessToken = 'din_access_token'; // Hämta access token från användarens session eller OAuth
    const deviceId = 'din_device_id'; // Hämta device id för användarens enhet

    // Skicka PUT-begäran till Spotify API för att ändra positionen
    fetch(`https://api.spotify.com/v1/me/player/seek?position_ms=${position_ms}&device_id=${deviceId}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${accessToken}`,
        },
    })
    .then(response => {
        if (response.ok) {
            console.log("Position uppdaterad");
        } else {
            console.error("Fel vid uppdatering av position");
        }
    });
}