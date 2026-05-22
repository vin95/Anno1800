param(
    [string]$GameStateDir = "",
    [string]$OutFile = "",
    [switch]$NoFirefox
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$ScriptDir = $PSScriptRoot

$pythonArgs = @(
    (Join-Path $ScriptDir 'browse-debuggame-states.py')
    '--web'
)

if (-not $NoFirefox) {
    $pythonArgs += '--firefox'
}

if ($GameStateDir) {
    $pythonArgs += '--dir'
    $pythonArgs += $GameStateDir
}

if ($OutFile) {
    $pythonArgs += '--out'
    $pythonArgs += $OutFile
}

& 'C:/Users/vinoc/anaconda3/Scripts/conda.exe' run -p 'C:\Users\vinoc\anaconda3' --no-capture-output python 'c:\Users\vinoc\.vscode\extensions\ms-python.python-2026.4.0-win32-x64\python_files\get_output_via_markers.py' @pythonArgs
