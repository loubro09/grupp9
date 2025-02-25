# grupp9

--- Användarmanual ---

-- Installationsinstruktioner --

Installera en IDE:
För att köra koden behöver du en utvecklingsmiljö (IDE). Vi rekommenderar att du använder antingen IntelliJ IDEA eller Visual Studio Code (VS Code).

-- Körinstruktioner för Weatherly --
1. Starta applikationen:
   Kör klassen APIRunner i din IDE eller kommandotolk för att starta Javelin-servern.

2. Öppna applikationen i webbläsaren:
   Öppna en webbläsare på samma dator där servern körs och navigera till:
     http://localhost:5009/

  Om det inte fungerar:
    Kontrollera att port 5009 är öppen på din dator.								
    
    För macOS: Öppna Terminal och kör följande kommando för att öppna porten:
     sudo ipfw add allow tcp from any to any 5009  - äldre macOS.
					
    För Windows:
      Öppna "Windows Defender Firewall".
      Klicka på "Avancerade inställningar".
      Välj "Inbound Rules" och skapa en ny regel.
      Välj "Port", ange port 5009 och tillåt anslutning.					
      
3. Använd Weatherly:
   När sidan öppnas, logga in med ditt Spotify Premium-konto. Detta är nödvändigt för att kunna generera personliga spellistor baserat på väderförhållanden.

4. Spela musik: För att spela musik måste du ha en aktiv enhet kopplad till ditt Spotify-konto där musik kan spelas upp.
   Om ingen enhet är aktiv visas en popup vid knapptryckning på play med en länk till Spotifys webbspelare. Öppna länken och starta en låt för att aktivera enheten.
   
5. När allt är uppsatt kan du söka efter väder på olika platser och lyssna på den musik som passar bäst för väderförhållandena!
