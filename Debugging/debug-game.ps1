# =============================================================================
# Anno 1800 - Debug Game Starter
# =============================================================================
# Hier den Seed, die Spieleranzahl und die maximalen Runden eintragen.
# Gleicher Seed = identisch gemischte Kartenstapel und identischer Startspieler.

$SEED       = 42       # Seed fuer reproduzierbares Spiel (beliebige Ganzzahl)
$PLAYERS    = 3        # Anzahl Spieler (1-4)
$MAX_ROUNDS = 100      # Maximale Rundenzahl bevor das Spiel abbricht

# =============================================================================
# Ab hier nichts aendern
# =============================================================================

$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.11'
$ProjectRoot = Split-Path -Parent $PSScriptRoot

Write-Host ""
Write-Host "========================================================================"
Write-Host "  Anno 1800 - Debug Game"
Write-Host "  Seed: $SEED  |  Spieler: $PLAYERS  |  Max. Runden: $MAX_ROUNDS"
Write-Host "  Game-States werden in game-states/ gespeichert"
Write-Host "========================================================================"
Write-Host ""

& "$ProjectRoot\gradlew.bat" -p "$ProjectRoot" debugGame "-PgameArgs=$SEED $PLAYERS $MAX_ROUNDS"
