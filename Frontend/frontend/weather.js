document.addEventListener("DOMContentLoaded", () => {
    const output = document.getElementById("output");
    const weather = document.getElementById("weather");

    // Hämta plats baserat på användarens koordinater
    document.getElementById("getLocation").addEventListener("click", () => {
        if (!("geolocation" in navigator)) {
            output.textContent = "Geolocation stöds inte.";
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                fetchLocationFromCoordinates(latitude, longitude);
            },
            (error) => {
                output.textContent = `Fel: ${error.message}`;
            }
        );
    });

    // Hämta plats från manuellt inmatad text
    document.getElementById("fetchCoordinates").addEventListener("click", () => {
        const locationInput = document.getElementById("manualLocation").value.trim();
        if (!locationInput) {
            output.textContent = "Skriv in en giltig plats.";
            return;
        }
        fetchLocationFromName(locationInput);
    });

    // Hämta väderdata från servern
    document.getElementById("getWeather").addEventListener("click", () => {
        fetch("http://localhost:5008/weatherLocation")
            .then(handleResponse)
            .then((data) => {
                weather.textContent = `
                    Plats: ${data.locationName || "Okänd plats"},
                    Tid: ${data.time || "Okänd tid"},
                    Väderprognos: ${data.weatherDescription || "Okänt väder"},
                    Temperatur: ${data.temp || "Okänd temperatur"} °C
                `;
            })
            .catch(handleError);
    });

    // Funktion för att hämta plats från koordinater
    const fetchLocationFromCoordinates = (latitude, longitude) => {
        fetch("http://localhost:5008/location", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ latitude, longitude }),
        })
            .then(handleResponse)
            .then((data) => {
                output.textContent = `Plats: ${data.place}`;
            })
            .catch(handleError);
    };

    // Funktion för att hämta plats från namn
    const fetchLocationFromName = (place) => {
        fetch(`http://localhost:5008/locationByName?place=${encodeURIComponent(place)}`)
            .then(handleResponse)
            .then((data) => {
                output.textContent = `Plats: ${data.place}`;
            })
            .catch(handleError);
    };

    // Hantera API-svar
    const handleResponse = (response) => {
        if (!response.ok) {
            throw new Error(`API-fel: ${response.status}`);
        }
        return response.json();
    };

    // Hantera fel
    const handleError = (error) => {
        console.error("Fel:", error);
        output.textContent = "Ett fel uppstod. Försök igen senare.";
    };
});
