document.addEventListener('DOMContentLoaded', () => {
    const clientId = '60d1a794bb57451f847d0b566f0c7d4f';
    const redirectUri = 'http://127.0.0.1:5500/Frontend/index.html';
    const scopes = [
        'streaming',
        'user-read-email',
        'user-read-private',
        'user-modify-playback-state',
        'user-read-playback-state',
        'playlist-read-private',
    ];

    // Kontrollera vilken sida som laddas
    if (document.getElementById('login-button')) {
        // Logik för login.html
        console.log("Login-sidan laddad.");
        document.getElementById('login-button').addEventListener('click', () => {
            const authUrl = `https://accounts.spotify.com/authorize?response_type=token&client_id=${clientId}&scope=${encodeURIComponent(scopes.join(' '))}&redirect_uri=${encodeURIComponent(redirectUri)}`;
            console.log("Auth URL:", authUrl);
            window.location.href = authUrl; // Skicka användaren till Spotify
        });
    }

    if (document.getElementById('player')) {
        // Logik för index.html (huvudsidan)
        console.log("Huvudsidan laddad.");

        // Hämta access token från URL-hashen efter Spotify-login
        const hashParams = new URLSearchParams(window.location.hash.substring(1));
        const accessToken = hashParams.get('access_token');

        if (accessToken) {
            console.log('Access Token mottagen:', accessToken);
            localStorage.setItem('spotify_access_token', accessToken); // Spara token i localStorage
            window.location.hash = ''; // Rensa URL-hashen
        }

        // Hämta token från localStorage
        const token = accessToken || localStorage.getItem('spotify_access_token');

        if (token) {
            console.log('Token hittad, startar Spotify Player...');

            // Web Playback SDK – Initiera Spotify Player
            window.onSpotifyWebPlaybackSDKReady = () => {
                const player = new Spotify.Player({
                    name: 'Väder-Spotify Player',
                    getOAuthToken: cb => { cb(token); },
                    volume: 0.5,
                });

                // Koppla spelaren
                player.connect().then(success => {
                    if (success) {
                        console.log('Spotify Player connected!');
                    } else {
                        console.error('Spotify Player kunde inte kopplas.');
                    }
                });

                // Event Listeners för kontroller
                document.getElementById('play-button').addEventListener('click', () => player.togglePlay());
                document.getElementById('prev-button').addEventListener('click', () => player.previousTrack());
                document.getElementById('next-button').addEventListener('click', () => player.nextTrack());
                document.getElementById('volume-control').addEventListener('input', (e) => {
                    player.setVolume(parseFloat(e.target.value)).then(() => {
                        console.log('Volume updated to:', e.target.value);
                    });
                });
            };
        } else {
            console.log("Ingen access token funnen, klicka på 'Logga in med Spotify'.");
        }
    }
});
