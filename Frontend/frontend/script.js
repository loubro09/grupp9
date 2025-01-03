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

document.getElementById("fetchCoordinates").addEventListener("click", () => {
    const locationInput = document.getElementById("manualLocation").value;
    if (locationInput.trim() === "") {
        document.getElementById("output").textContent = "Skriv in en giltig plats.";
        return;
    }

    // Skicka platsnamn till servern som GET-parameter
    fetch(`http://localhost:5009/locationByName?place=${encodeURIComponent(locationInput)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`API-fel: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            const place = data.place; // Här hanteras platsnamnet
            document.getElementById("output").textContent = `Plats: ${place}`;
        })
        .catch(error => console.error("Fel vid hämtning av koordinater:", error));
});

document.getElementById("getWeather").addEventListener("click", () => {
    fetch("http://localhost:5009/weatherLocation")
        .then(response => {
            if (!response.ok) {
                throw new Error(`API-fel: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            // Visa data på frontend
            const weatherOutput = `
                Place: ${data.locationName || "Okänd plats"},
                Time: ${data.time || "Okänd tid"},
                Weather forecast: ${data.weatherDescription || "Okänt väder"},
                Temperature: ${data.temp || "Okänd temperatur"} °C
            `;
            document.getElementById("weather").textContent = weatherOutput;
        })
        .catch(error => {
            console.error("Fel vid hämtning av väderdata:", error);
            document.getElementById("weather").textContent = "Kunde inte hämta väderdata.";
        });
});



document.getElementById("getLocation").addEventListener("click", () => {
    if ("geolocation" in navigator) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const latitude = position.coords.latitude;
                const longitude = position.coords.longitude;

                // Skapa en sträng med latitud och longitud separerade med komma
                const locationData = `${latitude},${longitude}`;

                // Skicka koordinater som en enkel sträng till servern
                fetch("http://localhost:5009/location", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(locationData), // Skicka som JSON med key 'location'
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error(`API-fel: ${response.status}`);
                        }
                        return response.json(); // Hantera JSON-svaret
                    })
                    .then(data => {
                        const place = data.place; // Hämta platsnamnet
                        document.getElementById("output").textContent = `Plats: ${place}`;
                    })
                    .catch(error => {
                        console.error("Fel vid sparande av plats:", error);
                        document.getElementById("output").textContent = "Fel vid hämtning av plats.";
                    });
            },
            (error) => {
                document.getElementById("output").textContent = "Fel: " + error.message;
            }
        );
    } else {
        document.getElementById("output").textContent = "Geolocation stöds inte.";
    }
});