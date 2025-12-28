#!/usr/bin/env pwsh
# Comprehensive migration script for Anno1800 project restructuring
# This script moves files and updates package declarations in one atomic operation

$ErrorActionPreference = "Stop"
$basePath = "e:\Anno1800\src\main\java\com\anno1800"

# Define the complete migration map: old_path -> new_path
$migrations = @(
    # Game core packages
    @{
        old = "actions"
        new = "game\actions"
        package = "game.actions"
    },
    @{
        old = "board"
        new = "game\board"
        package = "game.board"
    },
    @{
        old = "cards"
        new = "game\cards"
        package = "game.cards"
    },
    @{
        old = "engine"
        new = "game\engine"
        package = "game.engine"
    },
    @{
        old = "FactoryMethods"
        new = "game\factories"
        package = "game.factories"
    },
    @{
        old = "player"
        new = "game\player"
        package = "game.player"
    },
    @{
        old = "residents"
        new = "game\residents"
        package = "game.residents"
    },
    @{
        old = "Rewards"
        new = "game\rewards"
        package = "game.rewards"
    },
    @{
        old = "Boardtiles"
        new = "game\tiles"
        package = "game.tiles"
    },
    
    # Data packages
    @{
        old = "config"
        new = "data\config"
        package = "data.config"
    },
    @{
        old = "data"
        new = "data\gamedata"
        package = "data.gamedata"
    },
    
    # UI packages - These must be extracted from engine folder
    @{
        old = "engine"
        new = "ui\examples"
        package = "ui.examples"
        filter = @("ActionExample.java", "ActionGeneratorExample.java", "InitialGameStateExample.java")
    },
    @{
        old = "engine"
        new = "ui\output"
        package = "ui.output"
        filter = @("GameStatePrinter.java")
    },
    @{
        old = "engine"
        new = "game\state"
        package = "game.state"
        filter = @("GameState.java")
    }
)

Write-Host "=== Anno1800 Project Restructuring ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Create all necessary directories first
Write-Host "Step 1: Creating directory structure..." -ForegroundColor Yellow
$uniqueNewPaths = $migrations | ForEach-Object { Join-Path $basePath $_.new } | Select-Object -Unique
foreach ($dir in $uniqueNewPaths) {
    if (!(Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "  Created: $dir" -ForegroundColor Green
    }
}
Write-Host ""

# Step 2: Copy and transform files
Write-Host "Step 2: Migrating files..." -ForegroundColor Yellow
$fileMigrations = @()

foreach ($migration in $migrations) {
    $oldPath = Join-Path $basePath $migration.old
    $newPath = Join-Path $basePath $migration.new
    
    if (!(Test-Path $oldPath)) {
        Write-Host "  Warning: Source path does not exist: $oldPath" -ForegroundColor Yellow
        continue
    }
    
    # Get files to migrate
    $files = Get-ChildItem -Path $oldPath -Filter "*.java" -File
    
    # Apply filter if specified
    if ($migration.filter) {
        $files = $files | Where-Object { $migration.filter -contains $_.Name }
    }
    
    foreach ($file in $files) {
        $destFile = Join-Path $newPath $file.Name
        
        # Skip if destination already exists (from previous migration in same batch)
        if ((Test-Path $destFile) -and ($migration.filter)) {
            Write-Host "  Skipping $($file.Name) (already moved)" -ForegroundColor Gray
            continue
        }
        
        # Read file content
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        
        # Update package declaration
        $newPackage = "com.anno1800.$($migration.package)"
        $content = $content -replace '^package\s+com\.anno1800\.[^;]+;', "package $newPackage;"
        
        # Write to new location
        Set-Content -Path $destFile -Value $content -Encoding UTF8 -NoNewline
        
        # Track for deletion later
        $fileMigrations += @{
            source = $file.FullName
            dest = $destFile
            oldPackage = $migration.old
        }
        
        Write-Host "  Migrated: $($file.Name) -> $($migration.new)" -ForegroundColor Green
    }
}
Write-Host ""

# Step 3: Update all import statements across the entire project
Write-Host "Step 3: Updating import statements..." -ForegroundColor Yellow

# Build import replacement map
$importReplacements = @{
    "com.anno1800.actions" = "com.anno1800.game.actions"
    "com.anno1800.board" = "com.anno1800.game.board"
    "com.anno1800.cards" = "com.anno1800.game.cards"
    "com.anno1800.engine" = "com.anno1800.game.engine"
    "com.anno1800.FactoryMethods" = "com.anno1800.game.factories"
    "com.anno1800.player" = "com.anno1800.game.player"
    "com.anno1800.residents" = "com.anno1800.game.residents"
    "com.anno1800.Rewards" = "com.anno1800.game.rewards"
    "com.anno1800.Boardtiles" = "com.anno1800.game.tiles"
    "com.anno1800.config" = "com.anno1800.data.config"
    "com.anno1800.data.Goods" = "com.anno1800.data.gamedata.Goods"
    "com.anno1800.data.Factories" = "com.anno1800.data.gamedata.Factories"
    "com.anno1800.data.FactoryData" = "com.anno1800.data.gamedata.FactoryData"
    "com.anno1800.data.ResidentCardData" = "com.anno1800.data.gamedata.ResidentCardData"
    "com.anno1800.data.ExpeditionCardData" = "com.anno1800.data.gamedata.ExpeditionCardData"
    "com.anno1800.data.OldWorldIslandData" = "com.anno1800.data.gamedata.OldWorldIslandData"
    "com.anno1800.data.NewWorldIslandsData" = "com.anno1800.data.gamedata.NewWorldIslandsData"
    "com.anno1800.data.Plantations" = "com.anno1800.data.gamedata.Plantations"
    "com.anno1800.data.StartFactories" = "com.anno1800.data.gamedata.StartFactories"
}

# Add UI imports
$importReplacements["com.anno1800.engine.GameStatePrinter"] = "com.anno1800.ui.output.GameStatePrinter"
$importReplacements["com.anno1800.engine.GameState"] = "com.anno1800.game.state.GameState"

# Update imports in all Java files
$allJavaFiles = Get-ChildItem -Path "$basePath" -Filter "*.java" -Recurse
$updatedFiles = 0

foreach ($javaFile in $allJavaFiles) {
    $content = Get-Content $javaFile.FullName -Raw -Encoding UTF8
    $originalContent = $content
    
    # Replace each import
    foreach ($old in $importReplacements.Keys) {
        $new = $importReplacements[$old]
        $content = $content -replace [regex]::Escape($old), $new
    }
    
    # Write back if changed
    if ($content -ne $originalContent) {
        Set-Content -Path $javaFile.FullName -Value $content -Encoding UTF8 -NoNewline
        $updatedFiles++
    }
}

Write-Host "  Updated imports in $updatedFiles files" -ForegroundColor Green

# Also update test files
$testPath = "e:\Anno1800\src\test\java\com\anno1800"
if (Test-Path $testPath) {
    $testFiles = Get-ChildItem -Path $testPath -Filter "*.java" -Recurse
    $updatedTestFiles = 0
    
    foreach ($testFile in $testFiles) {
        $content = Get-Content $testFile.FullName -Raw -Encoding UTF8
        $originalContent = $content
        
        foreach ($old in $importReplacements.Keys) {
            $new = $importReplacements[$old]
            $content = $content -replace [regex]::Escape($old), $new
        }
        
        if ($content -ne $originalContent) {
            Set-Content -Path $testFile.FullName -Value $content -Encoding UTF8 -NoNewline
            $updatedTestFiles++
        }
    }
    
    Write-Host "  Updated imports in $updatedTestFiles test files" -ForegroundColor Green
}

# Update App.java
$appFile = Join-Path $basePath "App.java"
if (Test-Path $appFile) {
    $content = Get-Content $appFile -Raw -Encoding UTF8
    $originalContent = $content
    
    foreach ($old in $importReplacements.Keys) {
        $new = $importReplacements[$old]
        $content = $content -replace [regex]::Escape($old), $new
    }
    
    if ($content -ne $originalContent) {
        Set-Content -Path $appFile -Value $content -Encoding UTF8 -NoNewline
        Write-Host "  Updated App.java imports" -ForegroundColor Green
    }
}

Write-Host ""

# Step 4: Delete old directories (only if they still exist and are now empty or only contain moved files)
Write-Host "Step 4: Cleaning up old directories..." -ForegroundColor Yellow

# Get unique old directories to delete (exclude ones that are also sources for filtered migrations)
$dirsToDelete = @("actions", "board", "cards", "Boardtiles", "FactoryMethods", 
                  "player", "residents", "Rewards", "config", "data")

foreach ($dir in $dirsToDelete) {
    $dirPath = Join-Path $basePath $dir
    if (Test-Path $dirPath) {
        # Only delete if directory is empty or only contains files we've moved
        $remainingFiles = Get-ChildItem -Path $dirPath -Recurse -File
        $shouldDelete = $true
        
        foreach ($file in $remainingFiles) {
            # Check if this file was migrated
            $wasMigrated = $fileMigrations | Where-Object { $_.source -eq $file.FullName }
            if (-not $wasMigrated) {
                $shouldDelete = $false
                Write-Host "  Warning: Directory $dir still contains unmigrated file: $($file.Name)" -ForegroundColor Yellow
            }
        }
        
        if ($shouldDelete) {
            Remove-Item -Path $dirPath -Recurse -Force
            Write-Host "  Removed: $dir" -ForegroundColor Green
        }
    }
}

# Special handling for engine directory - only delete specific files we moved
$enginePath = Join-Path $basePath "engine"
if (Test-Path $enginePath) {
    $filesToDelete = @("ActionExample.java", "ActionGeneratorExample.java", 
                      "InitialGameStateExample.java", "GameStatePrinter.java", "GameState.java")
    
    foreach ($file in $filesToDelete) {
        $filePath = Join-Path $enginePath $file
        if (Test-Path $filePath) {
            Remove-Item -Path $filePath -Force
            Write-Host "  Removed: engine\$file" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "=== Migration Complete ===" -ForegroundColor Cyan
Write-Host "Summary:" -ForegroundColor White
Write-Host "  Files migrated: $($fileMigrations.Count)" -ForegroundColor White
Write-Host "  Imports updated: $updatedFiles production files, $updatedTestFiles test files" -ForegroundColor White
Write-Host ""
Write-Host "New structure:" -ForegroundColor White
Write-Host "  game/     - Core game logic (actions, board, cards, engine, factories, player, residents, rewards, state, tiles)" -ForegroundColor Gray
Write-Host "  data/     - Game data (config, gamedata)" -ForegroundColor Gray
Write-Host "  ui/       - Output and examples (examples, output)" -ForegroundColor Gray
Write-Host "  agents/   - AI agents (unchanged)" -ForegroundColor Gray
Write-Host "  simulation/ - Match simulation (unchanged)" -ForegroundColor Gray
Write-Host "  utils/    - Utilities (unchanged)" -ForegroundColor Gray
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Run: .\gradlew.bat build --no-daemon" -ForegroundColor White
Write-Host "  2. Run: .\gradlew.bat test" -ForegroundColor White
Write-Host "  3. Verify all tests pass" -ForegroundColor White
