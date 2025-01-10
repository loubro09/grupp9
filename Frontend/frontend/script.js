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


document.getElementById("play-button").addEventListener("click", async () => {
    try {
        // Step 1: Call the backend to play the playlist
        const response = await fetch("/play-playlist", { method: "PUT" });

        if (!response.ok) {
            throw new Error(`API error while playing playlist: ${response.status}`);
        }

        const playlistData = await response.json();
        console.log("Playing Playlist:", playlistData);

        // Step 2: Optionally display playlist info in the UI
        const playlistOutput = `
            Playlist Name: ${playlistData.playlistName || "Unknown Playlist"}
            Playlist Image: ${playlistData.playlistImage || "Unknown Playlist Image"}
        `;
        document.getElementById("output").textContent = playlistOutput;
    } catch (error) {
        console.error("Error playing music:", error);
        document.getElementById("output").textContent = "Failed to play music.";
    }
});

async function fetchCurrentlyPlaying() {
    try {
        const response = await fetch("http://localhost:5009/currently-playing");

        if (!response.ok) {
            if (response.status === 204) {
                console.log("No song is currently playing.");
                document.getElementById("output").textContent = "No song is currently playing.";
                return;
            }
            throw new Error(`API error: ${response.status}`);
        }

        const data = await response.json();
        const currentlyPlayingOutput = `
            Song: ${data.songName || "Unknown Song"},
            Artist: ${data.artist || "Unknown Artist"}
        `;
        document.getElementById("output").textContent = currentlyPlayingOutput;
    } catch (error) {
        console.error("Error fetching currently playing song:", error);
        document.getElementById("output").textContent = "Failed to fetch currently playing song.";
    }
}

// Poll the currently playing song every 5 seconds
setInterval(fetchCurrentlyPlaying, 5000);

// Initial fetch when the page loads
fetchCurrentlyPlaying();



