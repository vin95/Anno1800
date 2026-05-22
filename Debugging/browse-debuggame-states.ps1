param(
    [string]$GameStateDir = "game-states/Debuggame-01"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-StateFiles {
    param([string]$Dir)

    if (-not (Test-Path $Dir)) {
        throw "Verzeichnis nicht gefunden: $Dir"
    }

    Get-ChildItem -Path $Dir -Filter '*.json' |
        Sort-Object -Property @{ Expression = {
            if ($_.Name -match '^initial_') { return [int]::MinValue }
            if ($_.Name -match '^action_(\d+)_') { return [int]$Matches[1] }
            return [int]::MaxValue
        } }, Name
}

function Get-ActionLabel {
    param([string]$Name)

    if ($Name -match '^initial_') {
        return 'initial'
    }

    if ($Name -match '^action_(\d+)_') {
        return "action_$($Matches[1])"
    }

    return $Name
}

function Get-NumericValue {
    param($Value)

    if ($null -eq $Value) {
        return $null
    }

    if ($Value -is [bool]) {
        return [int]$Value
    }

    if ($Value -is [int] -or $Value -is [long] -or $Value -is [double] -or $Value -is [decimal]) {
        return [double]$Value
    }

    return $null
}

function Get-DiffLines {
    param(
        [object]$Current,
        [object]$Previous,
        [string]$Prefix = ''
    )

    $lines = New-Object System.Collections.Generic.List[string]

    if ($null -eq $Current -and $null -eq $Previous) {
        return $lines
    }

    if ($null -eq $Previous) {
        if ($Prefix) {
            $lines.Add($Prefix + ': hinzugefügt')
        }
        return $lines
    }

    if ($null -eq $Current) {
        if ($Prefix) {
            $lines.Add($Prefix + ': entfernt')
        }
        return $lines
    }

    $currentIsObject = $Current -is [System.Management.Automation.PSCustomObject]
    $previousIsObject = $Previous -is [System.Management.Automation.PSCustomObject]
    $currentIsArray = $Current -is [System.Collections.IEnumerable] -and $Current -isnot [string] -and -not $currentIsObject
    $previousIsArray = $Previous -is [System.Collections.IEnumerable] -and $Previous -isnot [string] -and -not $previousIsObject

    if ($currentIsArray -and $previousIsArray) {
        $currentItems = @($Current)
        $previousItems = @($Previous)
        $maxCount = [Math]::Max($currentItems.Count, $previousItems.Count)

        if ($currentItems.Count -ne $previousItems.Count) {
            $path = if ($Prefix) { $Prefix } else { 'array' }
            $lines.Add($path + ': Count ' + $previousItems.Count + ' -> ' + $currentItems.Count)
        }

        for ($i = 0; $i -lt $maxCount; $i++) {
            $itemPrefix = if ($Prefix) { $Prefix + '[' + $i + ']' } else { '[' + $i + ']' }
            $currentItem = if ($i -lt $currentItems.Count) { $currentItems[$i] } else { $null }
            $previousItem = if ($i -lt $previousItems.Count) { $previousItems[$i] } else { $null }
            $nested = Get-DiffLines -Current $currentItem -Previous $previousItem -Prefix $itemPrefix
            foreach ($line in $nested) { $lines.Add($line) }
        }

        return $lines
    }

    if ($currentIsObject -and $previousIsObject) {
        $currentProps = $Current.PSObject.Properties.Name
        $previousProps = $Previous.PSObject.Properties.Name
        $allProps = ($currentProps + $previousProps) | Sort-Object -Unique

        foreach ($prop in $allProps) {
            $currentValue = $Current.$prop
            $previousValue = $Previous.$prop
            $path = if ($Prefix) { $Prefix + '.' + $prop } else { $prop }

            $nested = Get-DiffLines -Current $currentValue -Previous $previousValue -Prefix $path
            foreach ($line in $nested) { $lines.Add($line) }
        }

        return $lines
    }

    $currentNumeric = Get-NumericValue $Current
    $previousNumeric = Get-NumericValue $Previous
    if ($null -ne $currentNumeric -and $null -ne $previousNumeric) {
        if ($currentNumeric -ne $previousNumeric) {
            $delta = $currentNumeric - $previousNumeric
            $path = if ($Prefix) { $Prefix } else { 'value' }
            $lines.Add($path + ': ' + $previousNumeric + ' -> ' + $currentNumeric + ' (Δ ' + $delta + ')')
        }
        return $lines
    }

    if ($Current -ne $Previous) {
        $path = if ($Prefix) { $Prefix } else { 'value' }
        $lines.Add($path + ": '" + $Previous + "' -> '" + $Current + "'")
    }

    return $lines
}

$files = Get-StateFiles -Dir $GameStateDir
if (-not $files -or $files.Count -eq 0) {
    throw "Keine JSON-Dateien in $GameStateDir gefunden."
}

Write-Host ""
Write-Host "Debuggame-States: $GameStateDir"
Write-Host "Taste drücken: beliebige Taste = nächster State, Q = Ende"
Write-Host ""

$previousState = $null

for ($index = 0; $index -lt $files.Count; $index++) {
    $file = $files[$index]
    $state = Get-Content -Raw -Path $file.FullName | ConvertFrom-Json

    Clear-Host
    Write-Host "State $($index + 1)/$($files.Count)"
    Write-Host "Datei: $($file.Name)"
    Write-Host "Aktion: $(Get-ActionLabel -Name $file.Name)"
    Write-Host "Runde: $($state.round)"
    Write-Host "Aktueller Spieler: $($state.currentPlayer)"
    if ($state.PSObject.Properties.Name -contains 'label') {
        Write-Host "Label: $($state.label)"
    }
    Write-Host ""

    if ($null -eq $previousState) {
        Write-Host "Dies ist der Initial-State."
    } else {
        Write-Host "Änderungen seit dem vorherigen State:"
        $diffLines = Get-DiffLines -Current $state -Previous $previousState
        if ($diffLines.Count -eq 0) {
            Write-Host "  Keine Änderungen erkannt."
        } else {
            foreach ($line in $diffLines) {
                Write-Host "  $line"
            }
        }
    }

    Write-Host ""
    Write-Host "Beliebige Taste = weiter | Q = beenden"

    $key = [Console]::ReadKey($true)
    if ($key.Key -eq 'Q') {
        break
    }

    $previousState = $state
}

Write-Host ""
Write-Host "Fertig."
