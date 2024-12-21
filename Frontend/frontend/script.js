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
