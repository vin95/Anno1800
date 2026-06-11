/*
============================================================================
Anno 1800 Debug Game State Viewer - JavaScript
============================================================================
Hauptlogik für die interaktive Visualisierung von Debug-Game-States.

Hauptfunktionen:
- Rendering von Spielzuständen (Mainboard, Player Layers, Ressourcen)
- Icon-basierte Text-Darstellung (Güter, Arbeiter, Karten)
- Navigation zwischen States (vorheriger/nächster)
- Berechnung und Anzeige von verfügbaren Fabriken
- Diff-Anzeige zwischen States
- Resident Stones Visualisierung
- Keyboard-Shortcuts für Navigation

Wichtige Render-Funktionen:
- renderMainBoard(): Rendert das zentrale Spielfeld
- renderPlayerLayer(): Rendert Spieler-Informationen
- renderFactoryOverlay(): Zeigt Fabriken mit Verfügbarkeits-Badges
- renderResidentStones(): Zeigt Bewohner-Steine im Pool
- renderDiffs(): Zeigt Änderungen zum vorherigen State
============================================================================
*/

        // Globaler Error-Handler für besseres Debugging
        window.onerror = function(msg, src, line, col, err) {
            const banner = document.createElement('div');
            banner.style.cssText = 'position:fixed;top:0;left:0;right:0;z-index:9999;background:#b91c1c;color:#fff;padding:16px 20px;font-size:14px;font-family:monospace;white-space:pre-wrap;';
            banner.textContent = 'JS ERROR: ' + msg + ' | at ' + src + ':' + line + ':' + col + (err && err.stack ? ' | ' + err.stack : '');
            document.body.appendChild(banner);
            return false;
        };

        /**
         * Hilfsfunktion: Gibt einen Text-Wert zurück oder einen Fallback
         * @param {any} value - Wert zum Anzeigen
         * @param {string} fallback - Fallback wenn value leer/null/undefined
         * @returns {string} Anzuzeigender Text
         */
        function text(value, fallback = "(nicht im JSON vorhanden)") {
            if (value === null || value === undefined || value === "") return fallback;
            return String(value);
        }

        /**
         * Berechnet die verfügbaren Fabriken basierend auf dem aktuellen State.
         * Subtrahiert bereits gebaute Fabriken von den initialen Stapeln.
         * 
         * @param {Object} state - Aktueller Game State
         * @returns {Object} Dictionary mit verfügbaren Fabriken pro Typ
         */
        function calculateAvailableFactories(state) {
            const factoriesPerStack = numPlayers <= 2 ? 1 : 2;
            const initialCounts = {};
            
            for (const row of FACTORY_LAYOUT) {
                for (const f of row.factories) {
                    if (f.initial > 0) {
                        initialCounts[f.name] = f.initial;
                    } else {
                        initialCounts[f.name] = factoriesPerStack;
                    }
                }
            }

            const built = {};
            const players = Array.isArray(state?.players) ? state.players : [];
            for (const player of players) {
                const tiles = player?.tiles?.islandTiles || [];
                for (const tile of tiles) {
                    const name = tile?.name || "";
                    if (name) {
                        built[name] = (built[name] || 0) + 1;
                    }
                }
            }

            const available = {};
            for (const [name, initial] of Object.entries(initialCounts)) {
                available[name] = Math.max(0, initial - (built[name] || 0));
            }

            console.log("Factory calculation:", {
                numPlayers,
                factoriesPerStack,
                builtCount: Object.keys(built).length,
                sampleBuilt: Object.entries(built).slice(0, 5),
                sampleAvailable: Object.entries(available).slice(0, 5)
            });

            return available;
        }

        /**
         * Konvertiert einen Güter-Namen in den entsprechenden Icon-Dateinamen.
         * 
         * @param {string} value - Güter-Name
         * @returns {string} Icon-Dateiname (z.B. "beer.png")
         */
        function goodToIconName(value) {
            const normalized = String(value ?? "").trim().toLowerCase();
            if (normalized === "exploration chip" || normalized === "explorer chip") {
                return "explorerchip.png";
            }
        /**
         * Normalisiert einen Bildnamen (lowercase, forward slashes).
         * 
         * @param {string} value - Zu normalisierender Bildname
         * @returns {string} Normalisierter Name
         */
            if (normalized === "trade chip") {
                return "tradechip.png";
            }
            return goodIconsByName[normalized] || normalized;
        }

        function normalizeImageName(value) {
        /**
         * Löst einen Bildnamen zu einem vollständigen Pfad auf.
         * Sucht zuerst exakt, dann nach Dateiname, dann mit Fallbacks.
         * 
         * @param {string} value - Bildname oder Pfad
         * @returns {string|null} Aufgelöster Pfad oder null
         */
            return String(value || "")
                .trim()
                .toLowerCase()
                .replace(/\\\\/g, "/");
        }

        function resolveImagePath(value) {
            const normalized = normalizeImageName(value);
            if (!normalized) return null;

            if (iconPathByName[normalized]) {
                return iconPathByName[normalized];
            }
/**
         * Generiert eine vollständige Bild-URL aus einem Bildnamen.
         * 
         * @param {string} value - Bildname
         * @returns {string|null} Vollständige URL oder null
        /**
         * Konvertiert Reward-Text in eine icon-basierte Darstellung.
         * Erkennt verschiedene Reward-Typen (Gold, Trade Points, Exploration Points, etc.)
         * und wandelt sie in lesbare Strings mit Icon-Token um.
         * 
         * @param {string} rewardValue - Reward-Text aus dem Game State
         * @returns {string} Icon-basierter Reward-Text
         */
         
        
            const fileName = normalized.split("/").pop();
            if (fileName && iconPathByName[fileName]) {
                return iconPathByName[fileName];
            }

            if (normalized.startsWith("residentcard_lv_")) {
                return iconPathByName["residentcard_lv_2.png"] || null;
            }

            return null;
        }

        function imageSrcFor(value) {
            const resolved = resolveImagePath(value);
            return resolved ? `${iconBaseUri}/${resolved}` : null;
        }

        function rewardToIconText(rewardValue) {
            const reward = String(rewardValue ?? "-").trim();
            if (!reward || reward === "-") return "-";
/**
         * Generiert eine Textzeile für eine ResidentCard mit Icons.
         * Format: "residentcard_lv_X.png need1.png need2.png => reward"
         * 
         * @param {Object} card - ResidentCard-Objekt aus dem State
         * @returns {string} Formatierte Karten-Beschreibung
         */
        
            let match = reward.match(/^(\\d+)\\s+Gold$/i);
            if (match) return `${match[1]}x gold.png`;

            match = reward.match(/^(\\d+)\\s+Trade\\s+Points$/i);
            if (match) return `${match[1]}x tradechip.png`;

            match = reward.match(/^(\\d+)\\s+Exploration\\s+Points$/i);
            if (match) return `${match[1]}x explorerchip.png`;

            match = reward.match(/^(\\d+)\\s+Expedition\\s+Cards?$/i);
            if (match) return `${match[1]}x Expeditioncards`;

            match = reward.match(/^(\\d+)x\\s+neuer\\s+Bewohner\\s+Stufe\\s+(\\d)$/i);
            if (match) return `${match[1]}x workforce_level_${match[2]}.png`;

        /**
         * Findet ein Player-Objekt im State anhand des Namens.
         * 
         * @param {Object} state - Game State
         * @param {string} playerName - Spielername (z.B. "Player 1")
         * @returns {Object|null} Player-Objekt oder null
         */
            match = reward.match(/^(\\d+)x\\s+Upgrade\\s+Stufe\\s+(\\d)\\s*->\\s*(\\d)$/i);
            if (match) return `${match[1]}x workforce_level_${match[2]}.png -> workforce_level_${match[3]}.png`;

            match = reward.match(/^Discard\\s+(\\d+)\\s+ResidentCard\\(s\\)$/i);
            if (match) return `${match[1]}x residentcard_lv_2.png abwerfen`;

            if (/^Extra\\s+Action$/i.test(reward)) return "Extra Action";

            return reward;
        }

        function cardLineText(card) {
            if (!card || typeof card !== "object") {
                return "residentcard_lv_2.png => -";
            }

        /**
         * Prüft ob nach einem Token ein Fabrik-Suffix folgt (z.B. "coal mine").
         * Verhindert, dass "coal" in "coal mine" als Güter-Icon erkannt wird.
         * 
         * @param {string} value - Zu prüfender Text
        /**
         * Rendert Text mit eingebetteten Icons.
         * Erkennt Icon-Token (z.B. "beer.png", "goodicon_coal") und ersetzt sie durch <img>-Tags.
         * Unterstützt auch Farb-Quadrate für Fabriken.
         * 
         * @param {HTMLElement} container - Container für die gerenderten Elemente
         * @param {string} value - Zu rendernder Text mit Icon-Token
         *
         * @param {number} cursor - Aktuelle Position
         * @param {number} tokenLength - Länge des Tokens
         * @returns {boolean} true wenn Fabrik-Suffix folgt
         */
            const level = Number(card.populationLevel || 2);
            const cardIcon = `residentcard_lv_${Number.isFinite(level) ? level : 2}.png`;
            const needs = Array.isArray(card.needs) ? card.needs : [];
            const needIcons = needs.map((good) => {
                const icon = goodToIconName(good);
                return icon.endsWith(".png") ? icon : `${icon}.png`;
            });
            const needsPart = needIcons.length ? needIcons.join(" , ") : "-";
            const rewardPart = rewardToIconText(card.reward);
            return `${cardIcon} ${needsPart} => ${rewardPart}`;
        }

        function findPlayerByName(state, playerName) {
            if (!state || !Array.isArray(state.players)) return null;
            const target = String(playerName || "").trim();
            if (!target) return null;

            for (const player of state.players) {
                if (player && String(player.name || "") === target) {
                    return player;
                }
            }

            if (target.startsWith("Spieler ")) {
                const normalized = target.replace("Spieler ", "Player ");
                for (const player of state.players) {
                    if (player && String(player.name || "") === normalized) {
                        return player;
                    }
                }
            }

            return null;
        }

        function startsWithFactorySuffix(value, cursor, tokenLength) {
            const suffix = value.slice(cursor + tokenLength);
            return /^(?:\\s+)(?:mine|factory|workshop|mill|yard|works|plant|plantation|refinery|foundry)\\b/i.test(suffix);
        }

        function renderTextWithIcons(container, value) {
            const textValue = text(value, "");
            if (!textValue) {
                return;
            }

            let bufferStart = 0;
            let cursor = 0;

            while (cursor < textValue.length) {
                const goodToken = orderedGoodIconTokens.find((candidate) => textValue.startsWith(candidate, cursor));
                if (goodToken) {
                    if (startsWithFactorySuffix(textValue, cursor, goodToken.length)) {
                        cursor += 1;
                        continue;
                    }

                    if (cursor > bufferStart) {
                        container.appendChild(document.createTextNode(textValue.slice(bufferStart, cursor)));
                    }

                    const wrapper = document.createElement("span");
                    wrapper.className = "icon-inline";
                    const img = document.createElement("img");
                    const resolvedGoodPath = resolveImagePath(goodIconTokens[goodToken]) || goodIconTokens[goodToken];
                    img.src = iconBaseUri + "/" + resolvedGoodPath;
                    img.alt = goodIconTokens[goodToken];
                    img.title = goodIconTokens[goodToken];
                    if (smallResourceIconNames.has(goodIconTokens[goodToken])) {
                        img.classList.add("resource-small");
                    }
                    wrapper.appendChild(img);
                    container.appendChild(wrapper);
                    container.appendChild(document.createTextNode(" "));

                    cursor += goodToken.length;
                    if (textValue.startsWith(".png", cursor)) {
                        cursor += 4;
                    }
                    bufferStart = cursor;
                    continue;
                }

                const squareToken = orderedColorSquareTokens.find((candidate) => textValue.startsWith(candidate, cursor));
                if (squareToken) {
                    if (cursor > bufferStart) {
                        container.appendChild(document.createTextNode(textValue.slice(bufferStart, cursor)));
                    }

                    const square = document.createElement("span");
                    square.className = "factory-color-square " + colorSquareTokens[squareToken];
                    square.title = "rotes Quadrat";
                    square.setAttribute("aria-label", "rotes Quadrat");
                    container.appendChild(square);
                    container.appendChild(document.createTextNode(" "));

                    cursor += squareToken.length;
                    bufferStart = cursor;
                    continue;
                }

                const iconName = orderedIconFileNames.find((candidate) => textValue.startsWith(candidate, cursor));
                if (!iconName) {
                    cursor += 1;
                    continue;
                }

                if (goodIconTokens["goodicon_" + iconName.replace(/\\.png$/i, "")] && startsWithFactorySuffix(textValue, cursor, iconName.length)) {
                    cursor += 1;
                    continue;
                }

                if (cursor > bufferStart) {
                    container.appendChild(document.createTextNode(textValue.slice(bufferStart, cursor)));
                }

                const wrapper = document.createElement("span");
                wrapper.className = "icon-inline";
                const img = document.createElement("img");
                const resolvedIconPath = resolveImagePath(iconName) || iconName;
                img.src = iconBaseUri + "/" + resolvedIconPath;
                img.alt = iconName;
                img.title = iconName;
                if (smallResourceIconNames.has(iconName)) {
                    img.classList.add("resource-small");
                } else if (iconName.startsWith("residentcard_")) {
                    img.classList.add("residentcard-large");
                }
                wrapper.appendChild(img);
                container.appendChild(wrapper);
                container.appendChild(document.createTextNode(" "));

                cursor += iconName.length;
                if (textValue.startsWith(".png", cursor)) {
                    cursor += 4;
                }
                bufferStart = cursor;
            }

            if (bufferStart < textValue.length) {
                container.appendChild(document.createTextNode(textValue.slice(bufferStart)));
            }
        }

        /**
         * Rendert die Änderungen (Diffs) zwischen aktuellem und vorherigem State.
         * 
         * @param {Object} entry - State-Eintrag mit diffs-Array
         */
        function renderDiffs(entry) {
            diffContainerEl.innerHTML = "";
            if (entry.isInitial) {
                const line = document.createElement("p");
                line.className = "diff-empty";
                line.textContent = "Dies ist der Initial-State.";
                diffContainerEl.appendChild(line);
                return;
            }

            if (!entry.diffs || entry.diffs.length === 0) {
                const line = document.createElement("p");
                line.className = "diff-empty";
                line.textContent = "Keine Änderungen erkannt.";
                diffContainerEl.appendChild(line);
                return;
            }

            const list = document.createElement("ul");
            list.className = "diff-list";
            for (const diff of entry.diffs) {
                const item = document.createElement("li");
                renderTextWithIcons(item, diff);
                list.appendChild(item);
            }
            diffContainerEl.appendChild(list);
        }

        /**
         * Rendert die Details einer ausgeführten Aktion.
         * Zeigt Blöcke mit Aktions-Informationen (z.B. produzierte Güter, verbrauchte Ressourcen).
         * 
         * @param {Object} entry - State-Eintrag mit actionDetailsBlocks
         */
        function renderActionDetails(entry) {
            actionDetailsContainerEl.innerHTML = "";

            const blocks = entry.actionDetailsBlocks || [];
            if (!blocks.length) {
                const line = document.createElement("p");
                line.className = "diff-empty";
                line.textContent = "Keine Aktionsdetails vorhanden.";
                actionDetailsContainerEl.appendChild(line);
                return;
            }

            for (const block of blocks) {
                const wrapper = document.createElement("div");
                wrapper.className = "action-block";

                const title = document.createElement("p");
                title.className = "action-title";
                title.textContent = block.title || "Details";
                wrapper.appendChild(title);

                const list = document.createElement("div");
                list.className = "action-list";
                for (const itemText of (block.items || [])) {
                    const paragraph = document.createElement("p");
                    paragraph.className = "action-paragraph";
                    renderTextWithIcons(paragraph, itemText);
                    list.appendChild(paragraph);
        /**
         * Rendert die Kartenübersicht (Objective Cards und ResidentCards).
         * Zeigt im Initial-State alle Objective Cards und für jeden Spieler die ResidentCards.
         * 
         * @param {Object} entry - State-Eintrag
         */
                }
                wrapper.appendChild(list);
                actionDetailsContainerEl.appendChild(wrapper);
            }
        }

        function renderCardOverview(entry) {
            cardOverviewContainerEl.innerHTML = "";

            if (entry.isInitial) {
                const state = entry.state || {};
                const objectives = Array.isArray(state.objectiveCards) ? state.objectiveCards : []

                const objectiveTitle = document.createElement("p");
                objectiveTitle.className = "action-title";
                objectiveTitle.textContent = "Objective Cards im Spiel";
                cardOverviewContainerEl.appendChild(objectiveTitle);

                if (!objectives.length) {
                    const none = document.createElement("p");
                    none.className = "diff-empty";
                    none.textContent = "Keine Objective Cards im JSON vorhanden.";
                    cardOverviewContainerEl.appendChild(none);
                } else {
                    const list = document.createElement("div");
                    list.className = "action-list";
                    for (const objective of objectives) {
                        const paragraph = document.createElement("p");
                        paragraph.className = "action-paragraph";
                        const title = String(objective?.title || "Unbekannte Objective Card");
                        const description = String(objective?.description || "");
                        renderTextWithIcons(paragraph, description ? `${title}: ${description}` : title);
                        list.appendChild(paragraph);
                    }
                    cardOverviewContainerEl.appendChild(list);
                }

                const players = Array.isArray(state.players) ? state.players : [];
                const residentTitle = document.createElement("p");
                residentTitle.className = "action-title";
                residentTitle.style.marginTop = "12px";
                residentTitle.textContent = "ResidentCards je Spieler";
                cardOverviewContainerEl.appendChild(residentTitle);

                for (const player of players) {
                    const playerName = String(player?.name || "Spieler");
                    const header = document.createElement("p");
                    header.className = "action-title";
                    header.style.margin = "8px 0 4px";
                    header.textContent = playerName.replace("Player ", "Spieler ");
                    cardOverviewContainerEl.appendChild(header);

                    const cards = Array.isArray(player?.cards?.residentCardDetails)
                        ? player.cards.residentCardDetails
                        : [];
                    if (!cards.length) {
                        const none = document.createElement("p");
                        none.className = "muted";
                        none.textContent = "Keine ResidentCards im JSON vorhanden.";
                        cardOverviewContainerEl.appendChild(none);
                        continue;
                    }

                    const cardList = document.createElement("div");
                    cardList.className = "action-list handcards-list";
                    for (const card of cards) {
                        const paragraph = document.createElement("p");
                        paragraph.className = "action-paragraph";
                        renderTextWithIcons(paragraph, cardLineText(card));
                        cardList.appendChild(paragraph);
                    }
                    cardOverviewContainerEl.appendChild(cardList);
                }
            }

            const state = entry.state || {};
            const executedBy = entry.executedByPlayer || state.currentPlayer;
            const player = findPlayerByName(state, executedBy);
            const playerName = (player?.name || String(executedBy || "Unbekannt")).replace("Player ", "Spieler ");
            const cards = Array.isArray(player?.cards?.residentCardDetails)
                ? player.cards.residentCardDetails
                : [];

            const details = document.createElement("details");
            details.className = "collapsible";

            const summary = document.createElement("summary");
            summary.textContent = `Handkarten von ${playerName} (ein-/ausklappen)`;
            details.appendChild(summary);

            const list = document.createElement("div");
            list.className = "action-list handcards-list";
            if (!cards.length) {
                const none = document.createElement("p");
                none.className = "muted";
                none.textContent = "Keine Handkarten im JSON vorhanden.";
                list.appendChild(none);
            } else {
        /**
         * Rendert die Agent-Scores (KI-Entscheidungs-Scores für Aktionen).
         * Zeigt für jede mögliche Aktion den Score und ob sie gewählt wurde.
         * 
         * @param {Object} entry - State-Eintrag mit agentMainActionScores
         */
                for (const card of cards) {
                    const paragraph = document.createElement("p");
                    paragraph.className = "action-paragraph";
                    renderTextWithIcons(paragraph, cardLineText(card));
                    list.appendChild(paragraph);
                }
            }

            details.appendChild(list);
            cardOverviewContainerEl.appendChild(details);
        }

        function renderAgentScores(entry) {
            agentScoresContainerEl.innerHTML = "";
            const strategyName = text(entry.agentStrategyName, "unbekannt");
            agentScoresTitleEl.textContent = `Agent-Auswahl (${strategyName})`;
            const scores = entry.agentMainActionScores || [];

            if (!scores.length) {
                const line = document.createElement("p");
                line.className = "diff-empty";
                line.textContent = "Keine Agent-Scores im JSON vorhanden.";
                agentScoresContainerEl.appendChild(line);
                return;
            }

            const details = document.createElement("details");
            details.className = "collapsible";

            const summary = document.createElement("summary");
            summary.textContent = "Ein-/Ausklappen";
            details.appendChild(summary);

            const list = document.createElement("div");
            list.className = "agent-scores";

            for (const score of scores) {
                const row = document.createElement("div");
                row.className = "agent-score-row";

                const main = document.createElement("div");
                main.className = "agent-score-main";
                renderTextWithIcons(main, String(score.mainAction || "(unbekannt)"));
                row.appendChild(main);

                const meta = document.createElement("div");
                const scoreValue = typeof score.score === "number"
                    ? score.score.toFixed(4)
                    : String(score.score ?? "-");
                const selectedText = score.selected ? "Gewählt" : "Nicht gewählt";
                meta.className = "agent-score-meta";
                renderTextWithIcons(meta, `Score: ${scoreValue} | ${selectedText}`);
                if (score.selected) {
        /**
         * Erstellt ein Token-Element mit Icon und Label.
         * Wird für Ressourcen, Schiffe, etc. verwendet.
         * 
         * @param {string} iconValue - Icon-Name oder Pfad
         * @param {string} labelText - Anzuzeigender Text
         * @returns {HTMLElement} Span-Element mit Icon und Text
         */
                    meta.classList.add("agent-score-selected");
                }
                row.appendChild(meta);

                if (score.bestActionVariant) {
                    const variant = document.createElement("div");
                    variant.className = "agent-score-meta";
                    renderTextWithIcons(variant, `Beste Variante: ${score.bestActionVariant}`);
                    row.appendChild(variant);
                }

                list.appendChild(row);
            }

        /**
         * Gibt den Pfad zum Arbeiter-Stein-Icon für ein bestimmtes Level zurück.
         * 
         * @param {string} levelKey - Level-Key (z.B. "level1", "level2")
         * @returns {string|null} Pfad zum Stein-Icon
         */
            details.appendChild(list);
            agentScoresContainerEl.appendChild(details);
        }

        function createTokenWithIcon(iconValue, labelText) {
        /**
         * Rendert einen Player-Layer mit allen Spieler-Informationen.
         * Zeigt Ressourcen, Schiffe, Insel-Layout, Arbeiter und Karten.
         * 
         * @param {HTMLElement} container - Container-Element für den Layer
         * @param {Object} player - Spieler-Objekt aus dem State
         * @param {string} fallbackLabel - Label falls kein Spieler vorhanden
         */
            const item = document.createElement("span");
            item.className = "token-pill";
            const src = imageSrcFor(iconValue);
            if (src) {
                const img = document.createElement("img");
                img.src = src;
                img.alt = iconValue;
                img.title = iconValue;
                item.appendChild(img);
                item.appendChild(document.createTextNode(" "));
            }
            item.appendChild(document.createTextNode(labelText));
            return item;
        }

        function workerStoneForLevel(levelKey) {
            const map = {
                level1: "residents/farmer_stone.png",
                level2: "residents/worker_stone.png",
                level3: "residents/artesian_stone.png",
                level4: "residents/engineer_stone.png",
                level5: "residents/investor_stone.png",
            };
            return map[levelKey] || null;
        }

        function renderPlayerLayer(container, player, fallbackLabel) {
            container.innerHTML = "";
            if (!player || typeof player !== "object") {
                const empty = document.createElement("p");
                empty.className = "muted";
                empty.textContent = `${fallbackLabel}: kein Spieler zugewiesen`;
                container.appendChild(empty);
                return;
            }

            const title = document.createElement("div");
            title.className = "main-board-title";
            title.textContent = String(player.name || fallbackLabel).replace("Player ", "Spieler ");
            container.appendChild(title);

            const resources = player.resources || {};
            const resourceSection = document.createElement("div");
            resourceSection.className = "layer-section resource-row";
            resourceSection.appendChild(createTokenWithIcon("goods/gold.png", `Gold: ${resources.gold ?? 0}`));
            resourceSection.appendChild(createTokenWithIcon("goods/tradechip.png", `Tradechips: ${resources.tradeChips ?? 0}`));
            resourceSection.appendChild(createTokenWithIcon("goods/explorerchip.png", `Explorerchips: ${resources.explorerChips ?? 0}`));
            container.appendChild(resourceSection);

            const ships = player.ships || {};
            const shipsSection = document.createElement("div");
            shipsSection.className = "layer-section ships-row";
            shipsSection.appendChild(createTokenWithIcon("ships/tradeship_lv1.png", `Trade-Schiffe: ${ships.tradeShips ?? 0}`));
            shipsSection.appendChild(createTokenWithIcon("ships/explorership_lv1.png", `Explorer-Schiffe: ${ships.explorerShips ?? 0}`));
            container.appendChild(shipsSection);

            const tiles = player.tiles || {};
            const islandSection = document.createElement("div");
            islandSection.className = "layer-section";
            islandSection.innerHTML = `
                <strong>Inselbereich</strong>
                <div class=\"island-layout\">
                    <div class=\"island-main-tile\">Hauptinsel</div>
                    <div class=\"island-lower-row\">
                        <div class=\"island-small-tile\">Neue Insel (L)</div>
                        <div class=\"island-small-tile\">Neue Insel (R)</div>
                    </div>
                </div>
                <div class=\"resource-row\" style=\"margin-top: 7px;\"><span class=\"token-pill\">Land frei: ${tiles.freeLand ?? 0}</span><span class=\"token-pill\">Küste frei: ${tiles.freeCoast ?? 0}</span><span class=\"token-pill\">See frei: ${tiles.freeSea ?? 0}</span></div>
            `;
            container.appendChild(islandSection);

            const workersSection = document.createElement("div");
            workersSection.className = "layer-section";
            const workersTitle = document.createElement("strong");
            workersTitle.textContent = "Verfügbare Arbeiter (Steine)";
            workersSection.appendChild(workersTitle);
            const workersRow = document.createElement("div");
            workersRow.className = "workers-row";
            const fitByLevel = (((player.residents || {}).byStatusByLevel || {}).fit || {});
            for (const levelKey of ["level1", "level2", "level3", "level4", "level5"]) {
                const count = Number(fitByLevel[levelKey] || 0);
                const chip = document.createElement("span");
                chip.className = "worker-chip";
                const stone = workerStoneForLevel(levelKey);
                const src = stone ? imageSrcFor(stone) : null;
                if (src) {
                    const img = document.createElement("img");
                    img.src = src;
                    img.alt = levelKey;
                    chip.appendChild(img);
                }
                chip.appendChild(document.createTextNode(String(count)));
                workersRow.appendChild(chip);
            }
            workersSection.appendChild(workersRow);
            container.appendChild(workersSection);

            const cardsSection = document.createElement("div");
            cardsSection.className = "layer-section";
            const cardsTitle = document.createElement("strong");
            cardsTitle.textContent = "Resident Cards";
            cardsSection.appendChild(cardsTitle);
        /**
         * Rendert die Objective Cards inline auf dem Mainboard.
         * 
         * @param {Object} state - Game State
         * @param {HTMLElement} containerEl - Container-Element
         */
            const cardsRow = document.createElement("div");
            cardsRow.className = "residentcards-row";
            const cards = Array.isArray(((player.cards || {}).residentCardDetails)) ? player.cards.residentCardDetails : [];
            for (const card of cards.slice(0, 6)) {
                const chip = document.createElement("span");
                chip.className = "resident-chip";
                const level = Number(card?.populationLevel || 2);
                const iconName = `residentcard_lv_${Number.isFinite(level) ? level : 2}.png`;
                const src = imageSrcFor(iconName);
                if (src) {
                    const img = document.createElement("img");
                    img.src = src;
                    img.alt = iconName;
                    chip.appendChild(img);
                }
                chip.appendChild(document.createTextNode(`Lv${Number.isFinite(level) ? level : 2}`));
                cardsRow.appendChild(chip);
            }
            if (!cards.length) {
                const none = document.createElement("span");
                none.className = "muted";
                none.textContent = "Keine Karten";
                cardsRow.appendChild(none);
        /**
         * Rendert das Fabrik-Overlay auf dem Mainboard.
         * Zeigt alle verfügbaren Fabriken mit Verfügbarkeits-Badges.
         * 
         * @param {Object} state - Game State
         * @param {HTMLElement} boardCenterEl - Mainboard-Container-Element
         */
            }
            cardsSection.appendChild(cardsRow);
            container.appendChild(cardsSection);
        }

        function renderObjectiveCards(state, containerEl) {
            containerEl.innerHTML = "";
            const objectives = Array.isArray(state?.objectiveCards) ? state.objectiveCards : [];
            if (!objectives.length) {
                const none = document.createElement("p");
                none.className = "muted";
                none.textContent = "Keine Objective Cards im State";
                containerEl.appendChild(none);
                return;
            }

            const title = document.createElement("div");
            title.className = "objective-inline-title";
            title.textContent = "Objective Cards";
            containerEl.appendChild(title);

            for (const objective of objectives) {
                const card = document.createElement("div");
                card.className = "objective-card";
                const title = document.createElement("strong");
        /**
         * Rendert das ResidentCards-Overlay auf dem Mainboard.
         * Zeigt die drei ResidentCard-Stapel mit Anzahl.
         * 
         * @param {Object} state - Game State
         * @param {HTMLElement} boardCenterEl - Mainboard-Container-Element
         */
                title.textContent = String(objective?.title || "Objective Card");
                card.appendChild(title);
                const textNode = document.createElement("div");
                textNode.textContent = String(objective?.description || "");
                card.appendChild(textNode);
                containerEl.appendChild(card);
            }
        }

        function renderFactoryOverlay(state, boardCenterEl) {
            const availableByType = calculateAvailableFactories(state);
            const overlay = document.createElement("div");
            overlay.className = "board-factory-overlay";
            for (const row of FACTORY_LAYOUT) {
                const rowEl = document.createElement("div");
                rowEl.className = "board-factory-row";
                for (const f of row.factories) {
                    const slot = document.createElement("div");
                    slot.className = "board-factory-slot";

                    const img = document.createElement("img");
                    img.className = "board-factory-img";
                    img.src = iconBaseUri + "/" + (f.path || "");
                    img.title = f.name;
                    slot.appendChild(img);

                    const count = availableByType[f.name];
                    if (count !== undefined) {
                        const badge = document.createElement("div");
                        badge.className = "board-slot-count" + (count === 0 ? " zero" : "");
                        badge.textContent = count;
                        slot.appendChild(badge);
        /**
         * Rendert die Bewohner-Steine (Resident Stones) aus dem Population Pool.
         * Zeigt gestapelte Steine für jedes Level (Farmers, Workers, Artisans, Engineers, Investors).
         * 
         * @param {Object} state - Game State mit boardState.populationPool
         */
                    }

                    rowEl.appendChild(slot);
                }
                overlay.appendChild(rowEl);
            }
            boardCenterEl.appendChild(overlay);
        }

        function renderResidentCardsOverlay(state, boardCenterEl) {
            const bs = state?.boardState ?? {};
            const cards = bs.cards ?? {};
            const overlay = document.createElement("div");
            overlay.className = "resident-cards-overlay";

            const stacks = [
                { level: 2, count: cards.residentStack1 ?? 0, img: "residents/residentcard_lv2.png" },
                { level: 5, count: cards.residentStack2 ?? 0, img: "residents/residentcard_lv5.png" },
                { level: 7, count: cards.residentStack3 ?? 0, img: "residents/residentcard_lv7.png" }
            ];

            for (const stack of stacks) {
                const stackEl = document.createElement("div");
                stackEl.className = "resident-card-stack";

                const img = document.createElement("img");
                const imgPath = resolveImagePath(stack.img) || stack.img;
                img.src = iconBaseUri + "/" + imgPath;
                img.title = `Resident Cards Level ${stack.level}`;
                stackEl.appendChild(img);

                const countEl = document.createElement("div");
                countEl.className = "resident-card-count" + (stack.count === 0 ? " zero" : "");
                countEl.textContent = stack.count;
                stackEl.appendChild(countEl);

                overlay.appendChild(stackEl);
            }

            boardCenterEl.appendChild(overlay);
        }
/**
         * Rendert das zentrale Mainboard mit allen Overlays.
         * Kombiniert Fabrik-Overlay, ResidentCards-Overlay, Objective Cards und Meta-Informationen.
         * 
         * @param {Object} entry - State-Eintrag mit state-Objekt
         */
        
        function renderResidentStones(state) {
            const container = document.getElementById("residentStones");
            if (!container) return;
            
            const bs = state?.boardState ?? {};
            const pool = bs.populationPool ?? {};
            
            const residents = [
                { type: "farmers", count: pool.farmers ?? 0, img: "residents/farmer_stone.png", label: "Farmers" },
                { type: "workers", count: pool.workers ?? 0, img: "residents/worker_stone.png", label: "Workers" },
                { type: "artisans", count: pool.artisans ?? 0, img: "residents/artesian_stone.png", label: "Artisans" },
                { type: "engineers", count: pool.engineers ?? 0, img: "residents/engineer_stone.png", label: "Engineers" },
                { type: "investors", count: pool.investors ?? 0, img: "residents/investor_stone.png", label: "Investors" }
            ];

            container.innerHTML = "";

            for (const resident of residents) {
                const group = document.createElement("div");
                group.className = "resident-stone-group";

                const stackContainer = document.createElement("div");
                stackContainer.className = "resident-stone-stack";

                const maxVisible = Math.min(resident.count, 50);
                for (let i = 0; i < maxVisible; i++) {
                    const img = document.createElement("img");
                    img.className = "resident-stone-img";
                    const imgPath = resolveImagePath(resident.img) || resident.img;
                    img.src = iconBaseUri + "/" + imgPath;
                    img.title = resident.label;
                    img.style.left = `${i * 8}px`;
                    img.style.bottom = `${i * 3}px`;
                    img.style.zIndex = i;
                    stackContainer.appendChild(img);
                }

                const countEl = document.createElement("div");
                countEl.className = "resident-stone-count";
                countEl.textContent = `${resident.count}`;
                
                group.appendChild(stackContainer);
                group.appendChild(countEl);
                container.appendChild(group);
            }
        }

        function renderMainBoard(entry) {
            const state = entry.state || {};
            const board = state.boardState || {};
            const resources = board.resources || {};
            const islands = board.islands || {};
            const ships = board.ships || {};

            renderResidentStones(state);

            mainBoardEl.innerHTML = "";
            const title = document.createElement("div");
            title.className = "main-board-title";
            title.textContent = `Main Board - ${text(entry.actionLabel, "State")}`;
            mainBoardEl.appendChild(title);

            const meta = document.createElement("div");
            meta.className = "main-board-meta";
            meta.textContent = `Runde ${text(entry.round, "-")} | Aktueller Spieler: ${text(entry.currentPlayer, "-")}`;
            mainBoardEl.appendChild(meta);

            const objectiveStrip = document.createElement("div");
            objectiveStrip.className = "objective-inline-strip";
            renderObjectiveCards(state, objectiveStrip);
            mainBoardEl.appendChild(objectiveStrip);

            const boardCenter = document.createElement("div");
            boardCenter.className = "main-board-center";
            if (mainBoardImageUri) {
                const boardImage = document.createElement("img");
                boardImage.className = "main-board-image";
                boardImage.src = mainBoardImageUri;
                boardImage.alt = "Main Board";
                boardImage.addEventListener("error", () => {
                    boardImage.remove();
                    const errorNote = document.createElement("div");
                    errorNote.className = "main-board-image-fallback";
                    errorNote.textContent = `Mainboard konnte nicht geladen werden: ${mainBoardImageUri}`;
                    boardCenter.appendChild(errorNote);
                });
                boardCenter.appendChild(boardImage);
            } else {
                const fallback = document.createElement("div");
                fallback.className = "main-board-image-fallback";
                fallback.textContent = "mainboard.png nicht gefunden";
                boardCenter.appendChild(fallback);
            }
            renderFactoryOverlay(state, boardCenter);
            renderResidentCardsOverlay(state, boardCenter);
            mainBoardEl.appendChild(boardCenter);

            const pools = document.createElement("div");
            pools.className = "main-board-pools";
            pools.innerHTML = `
                <div class=\"pool-chip\">Old World Inseln frei: ${islands.oldWorldIslands ?? 0}<br>New World Inseln frei: ${islands.newWorldIslands ?? 0}</div>
                <div class=\"pool-chip\">Gold-Pool: ${resources.goldPool ?? 0}<br>Tradechips-Pool: ${resources.tradeChips ?? 0}<br>Explorerchips-Pool: ${resources.explorerChips ?? 0}</div>
                <div class=\"pool-chip\">Trade Ships Board: L1 ${ships.tradeShips?.level1 ?? 0} / L2 ${ships.tradeShips?.level2 ?? 0} / L3 ${ships.tradeShips?.level3 ?? 0}<br>Explorer Ships Board: L1 ${ships.explorerShips?.level1 ?? 0} / L2 ${ships.explorerShips?.level2 ?? 0} / L3 ${ships.explorerShips?.level3 ?? 0}</div>
            `;
            mainBoardEl.appendChild(pools);
        }

        function renderGameBoard(entry) {
            const state = entry.state || {};
            const players = Array.isArray(state.players) ? state.players : [];
            renderMainBoard(entry);
            renderPlayerLayer(layerUpperLeftEl, players[0], "Layer Oben Links");
            renderPlayerLayer(layerUpperRightEl, players[1], "Layer Oben Rechts");
            renderPlayerLayer(layerLowerLeftEl, players[2], "Layer Unten Links");
            renderPlayerLayer(layerLowerRightEl, players[3], "Layer Unten Rechts");
        }

        function render() {
            if (!entries.length) return;

            const entry = entries[index];
            renderDiffs(entry);
            renderActionDetails(entry);
            renderCardOverview(entry);
            renderAgentScores(entry);
            stateDirEl.textContent = stateDir;
            stateIndicatorEl.textContent = `Zustand ${entry.index}/${entries.length}`;
            fileNameEl.textContent = text(entry.fileName, "-");
            actionEl.textContent = text(entry.actionLabel, "-");
            executedActionEl.textContent = text(entry.executedActionReadable);
            executedByEl.textContent = text(entry.executedByPlayer ? String(entry.executedByPlayer).replace("Player ", "Spieler ") : entry.executedByPlayer);
            roundEl.textContent = text(entry.round, "-");
            currentPlayerEl.textContent = text(entry.currentPlayer, "-");
            rawJsonEl.textContent = JSON.stringify(entry.state, null, 2);
            renderGameBoard(entry);

            prevBtn.disabled = index <= 0;
            nextBtn.disabled = index >= entries.length - 1;
        }

        function goPrevious() {
            if (index > 0) {
                index -= 1;
                render();
            }
        }

        function goNext() {
            if (index < entries.length - 1) {
                index += 1;
                render();
            }
        }

        toggleDetailsBtn.addEventListener("click", () => {
            if (detailsLayer.classList.contains("hidden")) {
                detailsLayer.classList.remove("hidden");
            } else {
                detailsLayer.classList.add("hidden");
            }
        });

        prevBtn.addEventListener("click", goPrevious);
        nextBtn.addEventListener("click", goNext);
        document.addEventListener("keydown", (event) => {
            if (event.key === "ArrowLeft") {
                event.preventDefault();
                goPrevious();
            }
            if (event.key === "ArrowRight") {
                event.preventDefault();
                goNext();
            }
        });

        try {
            if (entries.length > 0) {
                const firstState = entries[0]?.state || {};
                const players = Array.isArray(firstState.players) ? firstState.players : [];
                numPlayers = players.length || 2;
            }
            render();
        } catch (e) {
            const banner = document.createElement('div');
            banner.style.cssText = 'position:fixed;top:0;left:0;right:0;z-index:9999;background:#b91c1c;color:#fff;padding:16px 20px;font-size:14px;font-family:monospace;white-space:pre-wrap;';
            banner.textContent = 'render() ERROR: ' + e.message + ' | ' + (e.stack || '');
            document.body.appendChild(banner);
        }