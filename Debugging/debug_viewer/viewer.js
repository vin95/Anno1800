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
            banner.classList.add('top-banner');
            banner.textContent = 'JS ERROR: ' + msg + ' | at ' + src + ':' + line + ':' + col + (err && err.stack ? ' | ' + err.stack : '');
            document.body.appendChild(banner);
            return false;
        };

        // (Removed temporary alias mappings for coffee image misspellings)

        // Small helper: return meaningful text or a fallback string
        function text(value, fallback = "(nicht im JSON vorhanden)") {
            if (value === null || value === undefined || value === "") return fallback;
            return String(value);
        }

        /**
         * Hilfsfunktion: Gibt einen Text-Wert zurück oder einen Fallback
         * @param {any} value - Wert zum Anzeigen
         * @param {string} fallback - Fallback wenn value leer/null/undefined
                    } else {
                        // Use precomputed playerOldSummaries / playerNewSummaries from above
                        try {
                            let rendered = false;
                            if (playerOldSummaries.length > 0) {
                                const icons = [];
                                for (let i = 0; i < Math.min(2, playerOldSummaries.length); i += 1) {
                                    const summary = playerOldSummaries[i];
                                    const manual = lookupIslandIconsFromSummary(summary || '');
                                    if (manual && manual.length > 0) { icons.push(manual[0]); continue; }
                                    const textSummary = String(summary || '').toLowerCase();
                                    let matched = null;

                                    // If the summary explicitly lists factories, prefer mapping those to island icons.
                                    try {
                                        const m = textSummary.match(/factories=([^\]]+)/i);
                                        if (m && m[1]) {
                                            const factories = m[1].split('|').map(x => String(x || '').trim()).filter(Boolean);
                                            for (const fRaw of factories) {
                                                const f = fRaw.toLowerCase();
                                                // direct ISLAND_ICON_MAP hit
                                                if (HARDCODED_OLDWORLD_MAP[f]) { const cand = resolveIslandIcon(HARDCODED_OLDWORLD_MAP[f]); if (cand) { icons.push(cand); matched = true; break; } }
                                                    // try base token (strip color suffix)
                                                    const base = f.split('_')[0];
                                                    if (HARDCODED_OLDWORLD_MAP[base]) { const cand2 = resolveIslandIcon(HARDCODED_OLDWORLD_MAP[base]); if (cand2) { icons.push(cand2); matched = true; break; } }
                                            }
                                            if (matched) continue;
                                        }
                                    } catch (e) {
                                        // fall through to generic matching
                                    }

                                    for (const candidate of orderedIconFileNames || []) {
                                        const name = candidate.toLowerCase();
                                        if (!name.includes('oldworldisland')) continue;
                                        if (textSummary.includes(name) || textSummary.includes(name.replace(/[^a-z0-9]/g, '')) || (textSummary.includes('reward=') && matchesRewardCandidate(name, textSummary))) { matched = candidate; break; }
                                    }
                                    if (!matched) {
                                        try { console.debug('No old-world icon matched (left small):', { summary: textSummary, manual: manual, candidates: (orderedIconFileNames||[]).filter(n=>n.toLowerCase().includes('oldworldisland')).slice(0,20) }); } catch(e){}
                                    }
                                    if (matched) icons.push(matched);
                                }

                                for (const u of icons) {
                                    const img = document.createElement('img');
                                    img.src = imageSrcFor(u) || (iconBaseUri + '/' + u);
                                    img.alt = u;
                                    img.classList.add('img-fit','img-margin-right');
                                    rightTile.appendChild(img);
                                }
                                rendered = true;
                            }

                            if (!rendered && playerNewSummaries.length > 0) {
                                const img = document.createElement('img');
                                const src = imageSrcFor('newWorldIsland_base.png') || (iconBaseUri + '/newWorldIsland_base.png');
                                img.src = src;
                                img.alt = 'newWorldIsland_base.png';
                                img.classList.add('img-fit');
                                rightTile.appendChild(img);
                                rendered = true;
                            }

                            if (!rendered) {
                                rightTile.textContent = 'Neue Insel (R)';
                            }
                        } catch (e) {
                            rightTile.textContent = 'Neue Insel (R)';
                        }
                    }

            // friendly: display name (from FACTORY_LAYOUT.name) -> count
            const friendly = {};
            for (const row of FACTORY_LAYOUT) {
                for (const f of row.factories) {
                    const layoutId = f.id || f.name || "";
                    const display = f.name || layoutId;
                    friendly[display] = byId[layoutId] ?? 0;
                }
            }

            return {
                friendly: friendly,
                byId: byId,
                nameLookup: nameLookup,
                normalizedById: normalizedById,
            };
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
                .replace(/\\\\/g, "/")
                .replace(/^file:\/+/i, "")
                .replace(/^.*?:\/\//, "")
                .replace(/^[^\/]*/i, (m) => m) ;
        }

        function resolveImagePath(value) {
            const normalized = normalizeImageName(value);
            if (!normalized) return null;

            if (iconPathByName[normalized]) {
                // clean accidental double-extensions in mapped values
                return String(iconPathByName[normalized]).replace(/\.jpe?g(?=\.png$)/i, "");
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
         
        
            // remove duplicate/stacked extensions like `.jpg.png` -> `.png`
            let cleaned = normalized.replace(/\.(?:jpg|jpeg|png|gif|svg)(?=\.(?:jpg|jpeg|png|gif|svg)\b)/g, "");

            // try direct filename lookup (last path segment)
            const fileName = cleaned.split("/").pop();
            if (fileName && iconPathByName[fileName]) {
                return String(iconPathByName[fileName]).replace(/\.jpe?g(?=\.png$)/i, "");
            }

            // try common factory/pictures folder prefixes
            const candidates = [cleaned, `factories/${fileName}`, `pictures/${fileName}`, `icons/${fileName}`, fileName];
            for (const c of candidates) {
                if (!c) continue;
                if (iconPathByName[c]) return String(iconPathByName[c]).replace(/\.jpe?g(?=\.png$)/i, "");
            }

            // tolerant scan: try to match by normalized key among all known icons
            const targetNorm = cleaned.replace(/^[^a-z0-9]+|[^a-z0-9]+$/g, "");
            for (const k of Object.keys(iconPathByName)) {
                const kn = String(k || "").toLowerCase().replace(/\\\\/g, "/").replace(/\.(?:jpg|jpeg|png|gif|svg)(?=\.(?:jpg|jpeg|png|gif|svg)\b)/g, "");
                const knFile = kn.split("/").pop();
                if (kn === cleaned || knFile === fileName || knFile === cleaned || kn === targetNorm || knFile.replace(/[^a-z0-9]/g, "") === targetNorm) {
                    return String(iconPathByName[k]).replace(/\.jpe?g(?=\.png$)/i, "");
                }
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

        // Manual island -> icon mapping (NEW-WORLD & generic hints only).
        // Old-World icon mappings have been removed and must be provided
        // explicitly via `HARDCODED_OLDWORLD_MAP` below.
        // Keys are matched case-insensitively by substring; values are arrays
        // of icon filenames (relative to the pictures folder).
        const ISLAND_ICON_MAP = {
            // New World plantation hints
            'new[plantations=cotton': ['islands/newWorldIsland_cotton.png'],
            'new[plantations=coffee': ['islands/newWorldIsland_coffee.png'],
            'new[plantations=cacao': ['islands/newWorldIsland_cacao.png'],

            // generic fallbacks by token found in summary
            'new[plantations=': ['islands/newWorldIsland_back.png']
        };

        // Explicit hardcoded mapping for Old-World island tokens -> icon filenames.
        // Edit this object to control exactly which `islands/oldWorldIsland_*.png` is used
        // for a given factory/token name. Keys should be lowercase tokens found in the
        // `factories=` or summary strings (e.g. 'brick_factory', 'coal_mine').
        const HARDCODED_OLDWORLD_MAP = {
            // example mappings (adjust as desired):
            // 'brick_factory': 'islands/oldWorldIsland_brick_factory_blue.png',
            // 'coal_mine': 'islands/oldWorldIsland_coal_mine_blue.png',
            // 'warehouse': 'islands/oldWorldIsland_warehouse_blue.png',
        };

        // Resolve an island icon candidate to a usable filename (or null).
        function resolveIslandIcon(candidate) {
            if (!candidate) return null;
            try { if (imageSrcFor(candidate)) return candidate; } catch (e) {}
            const candLower = String(candidate).toLowerCase();
            try { if ((orderedIconFileNames || []).map(n=>n.toLowerCase()).includes(candLower)) return candidate; } catch (e) {}
            try { if (iconPathByName && iconPathByName[candLower]) return candidate; } catch (e) {}
            const file = candLower.split('/').pop();
            try { if ((orderedIconFileNames || []).map(n=>n.toLowerCase()).includes(file)) return file; } catch (e) {}
            try { if (iconPathByName && iconPathByName[file]) return file; } catch (e) {}
            return null;
        }

        // Narrow helper: when an action detail contains a generic `reward=` token,
        // only match island candidates that include a likely reward keyword.
        function matchesRewardCandidate(name, hay) {
            if (!name || !hay) return false;
            const kws = ['farmer','worker','artisan','warehouse','coal','brick','steel','trade','explorer','shipyard','sail','expeditioncards','gold'];
            const lname = String(name || '').toLowerCase();
            const lhay = String(hay || '').toLowerCase();
            for (const kw of kws) {
                if (lhay.includes(kw) && lname.includes(kw)) return true;
            }
            return false;
        }

        // Lookup island icons by analyzing an island summary string.
        // Returns an array of candidate filenames (may be empty).
        function lookupIslandIconsFromSummary(summary) {
            const found = [];
            if (!summary) return found;
            const text = String(summary || '').toLowerCase();

            // 1) ISLAND_ICON_MAP substring matches (new-world & generic hints)
            try {
                for (const key of Object.keys(ISLAND_ICON_MAP || {})) {
                    if (!key) continue;
                    if (text.includes(key)) {
                        const arr = ISLAND_ICON_MAP[key] || [];
                        if (arr.length > 0) {
                            const cand = resolveIslandIcon(arr[0]);
                            if (cand && !found.includes(cand)) found.push(cand);
                        }
                    }
                }
            } catch (e) {}

            // 2) HARDCODED_OLDWORLD_MAP via factories= tokens
            try {
                const m = text.match(/factories=([^\]]+)/i);
                if (m && m[1]) {
                    const factories = m[1].split('|').map(x => String(x || '').trim()).filter(Boolean);
                    for (const fRaw of factories) {
                        const f = fRaw.toLowerCase();
                        if (HARDCODED_OLDWORLD_MAP[f]) {
                            const cand = resolveIslandIcon(HARDCODED_OLDWORLD_MAP[f]);
                            if (cand && !found.includes(cand)) found.push(cand);
                        }
                        const base = f.split('_')[0];
                        if (HARDCODED_OLDWORLD_MAP[base]) {
                            const candb = resolveIslandIcon(HARDCODED_OLDWORLD_MAP[base]);
                            if (candb && !found.includes(candb)) found.push(candb);
                        }
                    }
                }
            } catch (e) {}

            // 2b) Precise reward mapping: NewResidents(amount=1, populationLevel=3) -> artisan icon
            try {
                const rw = text.match(/newresidents\s*\[\s*amount\s*=\s*(\d+)\s*,\s*populationlevel\s*=\s*(\d+)\s*\]/i);
                if (rw) {
                    const amt = Number(rw[1] || 0);
                    const lvl = Number(rw[2] || 0);
                    if (amt === 1 && lvl === 3) {
                        const cand = resolveIslandIcon('islands/oldWorldIsland_artisan.png');
                        if (cand && !found.includes(cand)) found.push(cand);
                    }
                }
            } catch (e) {}

            // 2c) Ship/explorer specific mappings
            try {
                // Specific old[] pattern that should map to shipyard level 1
                if (/old\[\s*land\s*=\s*2\s*,\s*coast\s*=\s*1\s*,\s*sea\s*=\s*2\s*,\s*reward\s*=\s*none\s*\]/i.test(text)) {
                    const cand = resolveIslandIcon('islands/oldWorldIsland_shipyard_lv1.png');
                    if (cand && !found.includes(cand)) found.push(cand);
                }

                // ExplorerShip presence => explorerShip icon
                if (/explorer(ship)?/i.test(text) || text.includes('explorership')) {
                    const cand = resolveIslandIcon('islands/oldWorldIsland_explorerShip.png');
                    if (cand && !found.includes(cand)) found.push(cand);
                }

                // Shipyard presence => shipyard icon
                if (/shipyard/i.test(text) || text.includes('shipyard_lv1')) {
                    const cand = resolveIslandIcon('islands/oldWorldIsland_shipyard_lv1.png');
                    if (cand && !found.includes(cand)) found.push(cand);
                }
            } catch (e) {}

            // 3) Direct mentions of known icon filenames
            try {
                for (const candidate of (orderedIconFileNames || [])) {
                    const name = String(candidate || '').toLowerCase();
                    if (!name) continue;
                    if (text.includes(name) || text.includes(name.replace(/[^a-z0-9]/g, ''))) {
                        if (!found.includes(candidate)) found.push(candidate);
                    }
                }
            } catch (e) {}

            // 4) reward-based fallback
            if (found.length === 0 && text.includes('reward=')) {
                try {
                    const rewardCandidates = (orderedIconFileNames || []).filter(n => String(n || '').toLowerCase().includes('oldworldisland'));
                    for (const candidate of rewardCandidates) {
                        if (matchesRewardCandidate(candidate, text) && !found.includes(candidate)) found.push(candidate);
                    }
                } catch (e) {}
            }

            return found;
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
                residentTitle.className = "action-title resident-title";
                residentTitle.textContent = "ResidentCards je Spieler";
                cardOverviewContainerEl.appendChild(residentTitle);

                for (const player of players) {
                    const playerName = String(player?.name || "Spieler");
                    const header = document.createElement("p");
                    header.className = "action-title action-header";
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

        function createBlueprintIcon(iconCandidates, fallbackLabel) {
            const icon = document.createElement("span");
            icon.className = "pool-blueprint-icon";

            for (const candidate of iconCandidates) {
                const src = imageSrcFor(candidate);
                if (!src) {
                    continue;
                }
                const img = document.createElement("img");
                img.src = src;
                img.alt = fallbackLabel;
                img.title = fallbackLabel;
                icon.appendChild(img);
                return icon;
            }

            icon.classList.add("text-fallback");
            icon.textContent = fallbackLabel;
            return icon;
        }

        function appendBlueprintRow(container, label, count, iconCandidates) {
            const row = document.createElement("div");
            row.className = "pool-blueprint-row";

            const rowLabel = document.createElement("span");
            rowLabel.className = "pool-blueprint-label";
            rowLabel.textContent = label;
            row.appendChild(rowLabel);

            const icons = document.createElement("div");
            icons.className = "pool-blueprint-icons";

            const safeCount = Math.max(0, Number(count) || 0);
            if (safeCount === 0) {
                const empty = document.createElement("span");
                empty.className = "pool-blueprint-empty";
                empty.textContent = "keine";
                icons.appendChild(empty);
            } else {
                for (let i = 0; i < safeCount; i += 1) {
                    icons.appendChild(createBlueprintIcon(iconCandidates, label));
                }
            }

            row.appendChild(icons);
            container.appendChild(row);
        }

        function workerStoneForLevel(levelKey) {
            const map = {
                level1: "residents/farmer_stone.png",
                level2: "residents/worker_stone.png",
                level3: "residents/artisan_stone.png",
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

            // ships-row removed: ships are no longer shown in a separate section

            const tiles = player.tiles || {};
            const islandSection = document.createElement("div");
            islandSection.className = "layer-section";

            const islandTitle = document.createElement('strong');
            islandTitle.textContent = 'Inselbereich';
            islandSection.appendChild(islandTitle);

            const layout = document.createElement('div');
            layout.className = 'island-layout';

            // Gather discovered islands for this player so both left and right
            // "Neue Insel" areas can render them consistently. We merge multiple
            // possible JSON shapes and also scan historical entries up to current
            // index so discoveries persist across subsequent states.
            let playerOldSummaries = [];
            let playerNewSummaries = [];
            try {
                const oldFromPlayer = (player && Array.isArray(player.discoveredOldWorldIslands)) ? player.discoveredOldWorldIslands : [];
                const newFromPlayer = (player && Array.isArray(player.discoveredNewWorldIslands)) ? player.discoveredNewWorldIslands : [];
                const discoveredIslandsObj = (player && player.discoveredIslands) ? player.discoveredIslands : null;
                const oldFromObj = (discoveredIslandsObj && Array.isArray(discoveredIslandsObj.oldWorld)) ? discoveredIslandsObj.oldWorld : [];
                const newFromObj = (discoveredIslandsObj && Array.isArray(discoveredIslandsObj.newWorld)) ? discoveredIslandsObj.newWorld : [];

                const historicalOld = [];
                for (let i = 0; i <= index; i += 1) {
                    const e = entries[i];
                    if (!e) continue;
                    const act = String(e.executedAction || '').toLowerCase();
                    if (act.includes('discoveroldworldisland') || act.includes('discoveroldworld')) {
                        const executedBy = String(e.executedByPlayer || '').trim();
                        if (executedBy && player && String(player.name || '') === executedBy) {
                            const detailsText = String(e.executedActionDetails || e.actionDetails || '') + ' ' + ((e.actionDetailsBlocks || []).map(b => (b.items || []).join(' ')).join(' '));
                            if (detailsText && !historicalOld.includes(detailsText)) historicalOld.push(detailsText);
                        }
                    }
                }

                // merge sources (preserve order: explicit player arrays first)
                for (const s of oldFromPlayer) if (s && !playerOldSummaries.includes(s)) playerOldSummaries.push(s);
                for (const s of oldFromObj) if (s && !playerOldSummaries.includes(s)) playerOldSummaries.push(s);
                for (const s of historicalOld) if (s && !playerOldSummaries.includes(s)) playerOldSummaries.push(s);

                for (const s of newFromPlayer) if (s && !playerNewSummaries.includes(s)) playerNewSummaries.push(s);
                for (const s of newFromObj) if (s && !playerNewSummaries.includes(s)) playerNewSummaries.push(s);
            } catch (e) {
                // ignore
            }

            // left small tile container: split into top (new world) and bottom (old world)
            const leftTile = document.createElement('div');
            leftTile.className = 'island-small-tile';
            const leftTop = document.createElement('div');
            leftTop.className = 'island-sub-tile island-sub-new';
            const leftBottom = document.createElement('div');
            leftBottom.className = 'island-sub-tile island-sub-old';

            // Top: render older discovered new-world islands (3rd and 4th)
            if (playerNewSummaries.length > 2) {
                const icons = [];
                for (let i = 2; i < Math.min(4, playerNewSummaries.length); i += 1) {
                    const summary = playerNewSummaries[i];
                    const manual = lookupIslandIconsFromSummary(summary || '');
                    if (manual && manual.length > 0) { icons.push(manual[0]); continue; }
                    const textSummary = String(summary || '').toLowerCase();
                    let matched = null;
                    for (const candidate of orderedIconFileNames || []) {
                        const name = candidate.toLowerCase();
                        if (!name.includes('newworldisland')) continue;
                        if (textSummary.includes(name) || textSummary.includes(name.replace(/[^a-z0-9]/g, ''))) { matched = candidate; break; }
                    }
                    icons.push(matched || 'newWorldIsland_base.png');
                }
                leftTop.innerHTML = '';
                for (const u of icons) {
                    const img = document.createElement('img');
                    img.src = imageSrcFor(u) || (iconBaseUri + '/' + u);
                    img.alt = u;
                    img.classList.add('img-fit');
                    leftTop.appendChild(img);
                }
            } else {
                leftTop.textContent = 'Neue Insel (L) - NewWorld';
            }

            // Bottom: render older discovered old-world islands (3rd and 4th)
            if (playerOldSummaries.length > 2) {
                const icons = [];
                for (let i = 2; i < Math.min(4, playerOldSummaries.length); i += 1) {
                    const summary = playerOldSummaries[i];
                    const manual = lookupIslandIconsFromSummary(summary || '');
                    if (manual && manual.length > 0) { icons.push(manual[0]); continue; }
                    const textSummary = String(summary || '').toLowerCase();
                    let matched = null;
                    try {
                        const m = textSummary.match(/factories=([^\]]+)/i);
                        if (m && m[1]) {
                            const factories = m[1].split('|').map(x => String(x || '').trim()).filter(Boolean);
                                for (const fRaw of factories) {
                                const f = fRaw.toLowerCase();
                                if (HARDCODED_OLDWORLD_MAP[f]) { const cand = resolveIslandIcon(HARDCODED_OLDWORLD_MAP[f]); if (cand) { icons.push(cand); matched = true; break; } }
                                const base = f.split('_')[0];
                                if (HARDCODED_OLDWORLD_MAP[base]) { const candb = resolveIslandIcon(HARDCODED_OLDWORLD_MAP[base]); if (candb) { icons.push(candb); matched = true; break; } }
                            }
                            if (matched) continue;
                        }
                    } catch (e) {}
                    for (const candidate of orderedIconFileNames || []) {
                        const name = candidate.toLowerCase();
                        if (!name.includes('oldworldisland')) continue;
                        if (textSummary.includes(name) || textSummary.includes(name.replace(/[^a-z0-9]/g, '')) || (textSummary.includes('reward=') && matchesRewardCandidate(name, textSummary))) { matched = candidate; break; }
                    }
                    if (!matched) {
                        try { console.debug('No old-world icon matched (left bottom factory scan):', { summary: textSummary, candidates: (orderedIconFileNames||[]).filter(n=>n.toLowerCase().includes('oldworldisland')).slice(0,20) }); } catch(e){}
                    }
                    if (matched) icons.push(matched);
                }
                leftBottom.innerHTML = '';
                for (const u of icons) {
                    const img = document.createElement('img');
                    img.src = imageSrcFor(u) || (iconBaseUri + '/' + u);
                    img.alt = u;
                    img.classList.add('img-fit');
                    leftBottom.appendChild(img);
                }
            } else {
                leftBottom.textContent = 'Neue Insel (L) - OldWorld';
            }

            leftTile.appendChild(leftTop);
            leftTile.appendChild(leftBottom);
            layout.appendChild(leftTile);

            // main tile: show player's island image full-area if available
            const mainTile = document.createElement('div');
            mainTile.className = 'island-main-tile';

            // Try to resolve a suitable player island image from known candidates
            (function() {
                const candidates = ['player_island.png', 'player_island.jpg', 'player_island'];
                let imgSrc = null;
                for (const c of candidates) {
                    const resolved = imageSrcFor(c);
                    if (resolved) { imgSrc = resolved; break; }
                }
                if (imgSrc) {
                    const img = document.createElement('img');
                    img.className = 'island-main-image';
                    img.src = imgSrc; // imageSrcFor already returns full URL
                    img.alt = 'player_island';
                    // When image loads, adjust container aspect-ratio to match image
                    img.addEventListener('load', function () {
                        try {
                            const w = img.naturalWidth || img.width;
                            const h = img.naturalHeight || img.height;
                            if (w && h) {
                                // Use numeric aspect ratio (width/height)
                                // set as inline style to override default 3/2
                                mainTile.style.aspectRatio = `${w} / ${h}`;
                            }
                        } catch (e) {
                            // ignore
                        }
                    });
                    mainTile.appendChild(img);
                    return;
                }

                // Fallback text when no image found
                mainTile.textContent = 'Hauptinsel';
            })();
            layout.appendChild(mainTile);

            // Create a 5x5 overlay grid on top of the main tile. Each cell may contain
            // a factory/ship/shipyard image or remain empty but still take up space.
            (function createIslandGrid() {
                try {
                    const overlay = document.createElement('div');
                    overlay.className = 'island-grid-overlay';

                    // Allow data to come from several possible sources:
                    // - player.islandGrid (array of 25 values, e.g. image names)
                    // - currentEntry.state.boardState.islandGrid
                    const currentEntry = (typeof entries !== 'undefined' && entries[index]) ? entries[index] : null;
                    const srcArray = (player && Array.isArray(player.islandGrid) && player.islandGrid.length === 25)
                        ? player.islandGrid
                        : (currentEntry && currentEntry.state && Array.isArray(currentEntry.state.boardState?.islandGrid) && currentEntry.state.boardState.islandGrid.length === 25)
                            ? currentEntry.state.boardState.islandGrid
                            : null;

                    // default images for row 1 (top row, cells 0..4)
                    const forcedRow1 = [
                        'farmer_house.png',
                        'worker_house.png',
                        'artisan_house.png',
                        'engineer_house.png',
                        'investor_house.png'
                    ];

                    // Build rows from GameState tiles (player.tiles)
                    // First row remains hardcoded (houses) above.
                    const landTiles = Array.isArray(player.landtiles || player.tiles?.landtiles)
                        ? (player.tiles?.landtiles || player.landtiles || [])
                        : [];
                    const coastTiles = Array.isArray(player.coasttiles || player.tiles?.coasttiles)
                        ? (player.tiles?.coasttiles || player.coasttiles || [])
                        : [];
                    const seaTiles = Array.isArray(player.seatiles || player.tiles?.seatiles)
                        ? (player.tiles?.seatiles || player.seatiles || [])
                        : [];

                    // Ensure arrays have expected lengths (land: up to 10, coast: up to 5, sea: up to 5)
                    const forcedRow2 = [];
                    const forcedRow3 = [];
                    for (let i = 0; i < 5; i++) {
                        forcedRow2.push((landTiles[i] && landTiles[i] !== 'empty') ? `${landTiles[i]}.png` : null);
                    }
                    for (let i = 0; i < 5; i++) {
                        forcedRow3.push((landTiles[5 + i] && landTiles[5 + i] !== 'empty') ? `${landTiles[5 + i]}.png` : null);
                    }

                    const forcedRow4 = [];
                    for (let i = 0; i < 5; i++) {
                        const val = coastTiles[i];
                        forcedRow4.push((val && val !== 'empty') ? `${val}.png` : null);
                    }

                    const forcedRow5 = [];
                    for (let i = 0; i < 5; i++) {
                        const val = seaTiles[i];
                        forcedRow5.push((val && val !== 'empty') ? `${val}.png` : null);
                    }

                    for (let i = 0; i < 25; i += 1) {
                        const cell = document.createElement('div');
                        cell.className = 'island-grid-cell';
                        cell.dataset.cellIndex = String(i);

                        let filled = false;
                        // First, if this is in the top row (i 0..4), force the specified house images
                        if (i >= 0 && i < 5) {
                            const token = forcedRow1[i];
                            const resolved = imageSrcFor(token) || imageSrcFor(token + '.png') || imageSrcFor(token + '.jpg');
                            if (resolved) {
                                const ci = document.createElement('img');
                                ci.className = 'island-grid-cell-image';
                                ci.src = resolved;
                                ci.alt = token;
                                cell.appendChild(ci);
                                filled = true;
                            } else {
                                const lbl = document.createElement('div');
                                lbl.className = 'island-grid-cell-label';
                                lbl.textContent = token;
                                cell.appendChild(lbl);
                                filled = true;
                            }
                        }

                        // If this is in the second row (i 5..9), use land tiles from GameState
                        if (!filled && i >= 5 && i < 10) {
                            const token = forcedRow2[i - 5];
                            if (token) {
                                const resolved = imageSrcFor(token) || imageSrcFor(token.replace(/\.png$/i, ''));
                                if (resolved) {
                                    const ci = document.createElement('img');
                                    ci.className = 'island-grid-cell-image';
                                    ci.src = resolved;
                                    ci.alt = token;
                                    cell.appendChild(ci);
                                    filled = true;
                                }
                            }
                        }

                        // If this is in the third row (i 10..14), use land tiles (second half)
                        if (!filled && i >= 10 && i < 15) {
                            const token = forcedRow3[i - 10];
                            if (token) {
                                const resolved = imageSrcFor(token) || imageSrcFor(token.replace(/\.png$/i, ''));
                                if (resolved) {
                                    const ci = document.createElement('img');
                                    ci.className = 'island-grid-cell-image';
                                    ci.src = resolved;
                                    ci.alt = token;
                                    cell.appendChild(ci);
                                    filled = true;
                                }
                            }
                        }

                        // If this is in the fourth row (i 15..19), use coast tiles
                        if (!filled && i >= 15 && i < 20) {
                            const token = forcedRow4[i - 15];
                            if (token) {
                                const resolved = imageSrcFor(token) || imageSrcFor(token.replace(/\.png$/i, ''));
                                if (resolved) {
                                    const ci = document.createElement('img');
                                    ci.className = 'island-grid-cell-image';
                                    ci.src = resolved;
                                    ci.alt = token;
                                    cell.appendChild(ci);
                                    filled = true;
                                }
                            }
                        }

                        // If this is in the fifth row (i 20..24), use sea tiles (ships)
                        if (!filled && i >= 20 && i < 25) {
                            const token = forcedRow5[i - 20];
                            if (token) {
                                const resolved = imageSrcFor(token) || imageSrcFor(token.replace(/\.png$/i, ''));
                                if (resolved) {
                                    const ci = document.createElement('img');
                                    ci.className = 'island-grid-cell-image';
                                    ci.src = resolved;
                                    ci.alt = token;
                                    cell.appendChild(ci);
                                    filled = true;
                                }
                            }
                        }

                        // Next, try to fill from provided data if not already filled
                        if (!filled && srcArray && srcArray[i]) {
                            const token = String(srcArray[i] || '').trim();
                            if (token) {
                                // try to resolve an image path
                                const resolved = imageSrcFor(token) || imageSrcFor(token + '.png') || imageSrcFor(token + '.jpg');
                                if (resolved) {
                                    const ci = document.createElement('img');
                                    ci.className = 'island-grid-cell-image';
                                    ci.src = resolved;
                                    ci.alt = token;
                                    cell.appendChild(ci);
                                    filled = true;
                                } else {
                                    const lbl = document.createElement('div');
                                    lbl.className = 'island-grid-cell-label';
                                    lbl.textContent = token;
                                    cell.appendChild(lbl);
                                    filled = true;
                                }
                            }
                        }

                        if (!filled) {
                            cell.classList.add('empty');
                        }

                        overlay.appendChild(cell);
                    }

                    // append overlay onto the main tile so it follows the tile's aspect ratio
                    mainTile.appendChild(overlay);
                } catch (e) {
                    // don't break rendering if overlay fails
                    console.warn('Failed to create island grid overlay', e);
                }
            })();

            // right small tile container: split into top (new world) and bottom (old world)
            const rightTile = document.createElement('div');
            rightTile.className = 'island-small-tile';
            const rightTop = document.createElement('div');
            rightTop.className = 'island-sub-tile island-sub-new';
            const rightBottom = document.createElement('div');
            rightBottom.className = 'island-sub-tile island-sub-old';

            // determine current entry and whether this player discovered an island (new or old)
            try {
                const currentEntry = (typeof entries !== 'undefined' && entries[index]) ? entries[index] : null;

                if (currentEntry && String(currentEntry.executedAction || '').toLowerCase().includes('discovernewworldisland')) {
                    // New World: try to pick a matching island image from details or fallback
                    const executedBy = String(currentEntry.executedByPlayer || '').trim();
                        if (executedBy && player && String(player.name || '') === executedBy) {
                            const details = currentEntry.executedActionDetails || '';
                            let discoveredImg = 'newWorldIsland_base.png';
                            const img = document.createElement('img');
                            const src = imageSrcFor(discoveredImg) || (iconBaseUri + '/' + discoveredImg);
                            img.src = src;
                            img.alt = discoveredImg;
                            img.classList.add('img-fit');
                            rightTop.appendChild(img);
                        } else {
                            rightTop.textContent = 'Neue Insel (R) - NewWorld';
                        }

                } else if (currentEntry && String(currentEntry.executedAction || '').toLowerCase().includes('discoveroldworldisland')) {
                    // Old World: there may be 0..2 old-world tiles referenced in action details/blocks
                    const executedBy = String(currentEntry.executedByPlayer || '').trim();
                        if (executedBy && player && String(player.name || '') === executedBy) {
                        const detailsText = String(currentEntry.actionDetails || currentEntry.executedActionDetails || '') + ' ' +
                            ((currentEntry.actionDetailsBlocks || []).map(b => (b.items || []).join(' ')).join(' '));
                        const hay = detailsText.toLowerCase();
                        const found = [];

                        // Try manual mapping first
                        const manual = lookupIslandIconsFromSummary(detailsText);
                        if (manual && manual.length > 0) {
                            for (const m of manual) {
                                found.push(m);
                                if (found.length >= 2) break;
                            }
                        }

                        // If still none, prefer factory-driven mapping when action details mention factories=
                        if (found.length === 0) {
                            try {
                                const fm = detailsText.toLowerCase().match(/factories=([^\]]+)/i);
                                if (fm && fm[1]) {
                                    const fl = fm[1].split('|').map(x => String(x||'').trim()).filter(Boolean);
                                        for (const fRaw of fl) {
                                        const f = fRaw.toLowerCase();
                                        const base = f.split('_')[0];
                                        if (HARDCODED_OLDWORLD_MAP[f]) { const cand = resolveIslandIcon(HARDCODED_OLDWORLD_MAP[f]); if (cand) found.push(cand); }
                                        else if (HARDCODED_OLDWORLD_MAP[base]) { const candb = resolveIslandIcon(HARDCODED_OLDWORLD_MAP[base]); if (candb) found.push(candb); }
                                        if (found.length >= 2) break;
                                    }
                                }
                            } catch (e) {}
                        }

                        // If none from manual mapping, try direct mention match among known icons
                        if (found.length === 0) {
                            for (const candidate of orderedIconFileNames || []) {
                                const name = candidate.toLowerCase();
                                if (!name.includes('oldworldisland')) continue;
                                if (hay.includes(name)) {
                                    found.push(candidate);
                                    if (found.length >= 2) break;
                                }
                            }
                        }

                        // If still none found by direct mention, fall back to a generic mapping based on reward keywords
                        if (found.length === 0) {
                            const rewardText = String(currentEntry.actionDetails || '').toLowerCase();
                            const rewardCandidates = orderedIconFileNames.filter(n => n.toLowerCase().includes('oldworldisland'));
                            // try to match common reward tokens inside rewardText
                            for (const candidate of rewardCandidates) {
                                const name = candidate.toLowerCase();
                                if (rewardText.includes('farmer') && name.includes('farmer')) found.push(candidate);
                                if (rewardText.includes('worker') && name.includes('worker')) found.push(candidate);
                                if (rewardText.includes('warehouse') && name.includes('warehouse')) found.push(candidate);
                                if (rewardText.includes('coal') && name.includes('coal')) found.push(candidate);
                                if (rewardText.includes('brick') && name.includes('brick')) found.push(candidate);
                                if (rewardText.includes('steel') && name.includes('steel')) found.push(candidate);
                                if (rewardText.includes('trade') && name.includes('trade')) found.push(candidate);
                                if (rewardText.includes('explorer') && name.includes('explorer')) found.push(candidate);
                                if (rewardText.includes('shipyard') && name.includes('shipyard')) found.push(candidate);
                                if (rewardText.includes('sail') && name.includes('sail')) found.push(candidate);
                                if (found.length >= 2) break;
                            }
                        }

                        // dedupe and limit to 2
                        const unique = [...new Set(found)].slice(0, 2);
                        if (unique.length > 0) {
                            for (const u of unique) {
                                const img = document.createElement('img');
                                const src = imageSrcFor(u) || (iconBaseUri + '/' + u);
                                img.src = src;
                                img.alt = u;
                                img.classList.add('img-fit');
                                rightBottom.appendChild(img);
                            }
                        } else {
                            try { console.debug('No old-world icons found for discovery action (fallback removed):', { detailsText: detailsText, manual: manual, foundCandidates: found.slice(0,10), rewardSample: String(currentEntry.actionDetails||'').toLowerCase() }); } catch(e){}
                            rightBottom.textContent = 'Neue Insel (R) - OldWorld';
                        }
                    } else {
                        rightBottom.textContent = 'Neue Insel (R) - OldWorld';
                    }

                } else {
                        // If the current entry is not a discovery action, still show any
                        // islands that are stored in the player's discovered lists (persisted in GameState)
                        // or were discovered in any previous entry. This ensures old-world
                        // islands remain visible in subsequent states even if the immediate
                        // entry is not a discovery action.
                        try {
                            // collect discovered summaries from multiple possible shapes in the JSON
                            const oldDiscoveredFromPlayer = (player && Array.isArray(player.discoveredOldWorldIslands)) ? player.discoveredOldWorldIslands : [];
                            const newDiscoveredFromPlayer = (player && Array.isArray(player.discoveredNewWorldIslands)) ? player.discoveredNewWorldIslands : [];
                            const discoveredIslandsObj = (player && player.discoveredIslands) ? player.discoveredIslands : null;
                            const oldDiscoveredFromObj = (discoveredIslandsObj && Array.isArray(discoveredIslandsObj.oldWorld)) ? discoveredIslandsObj.oldWorld : [];
                            const newDiscoveredFromObj = (discoveredIslandsObj && Array.isArray(discoveredIslandsObj.newWorld)) ? discoveredIslandsObj.newWorld : [];

                            // scan previous entries up to current index for any discoveroldworldisland actions
                            const historicalOld = [];
                            for (let i = 0; i <= index; i += 1) {
                                const e = entries[i];
                                if (!e) continue;
                                const act = String(e.executedAction || '').toLowerCase();
                                if (act.includes('discoveroldworldisland') || act.includes('discoveroldworld')) {
                                    const executedBy = String(e.executedByPlayer || '').trim();
                                    if (executedBy && player && String(player.name || '') === executedBy) {
                                        // collect textual details if present
                                        const detailsText = String(e.executedActionDetails || e.actionDetails || '') + ' ' + ((e.actionDetailsBlocks || []).map(b => (b.items || []).join(' ')).join(' '));
                                        if (detailsText && !historicalOld.includes(detailsText)) historicalOld.push(detailsText);
                                    }
                                }
                            }

                            // merge sources, prefer explicit player arrays, then discoveredIslands obj, then historical detections
                            const oldSummaries = [];
                            for (const s of oldDiscoveredFromPlayer) if (s && !oldSummaries.includes(s)) oldSummaries.push(s);
                            for (const s of oldDiscoveredFromObj) if (s && !oldSummaries.includes(s)) oldSummaries.push(s);
                            for (const s of historicalOld) if (s && !oldSummaries.includes(s)) oldSummaries.push(s);

                            const newSummaries = [];
                            for (const s of newDiscoveredFromPlayer) if (s && !newSummaries.includes(s)) newSummaries.push(s);
                            for (const s of newDiscoveredFromObj) if (s && !newSummaries.includes(s)) newSummaries.push(s);

                            let rendered = false;
                            // Prefer explicit old-world discovered icons (limit to first two)
                            if (oldSummaries.length > 0) {
                                const foundIcons = [];
                                for (const summary of oldSummaries) {
                                    // try manual mapping first
                                    const manual = lookupIslandIconsFromSummary(summary);
                                    if (manual && manual.length > 0) {
                                        for (const m of manual) {
                                            if (!foundIcons.includes(m)) foundIcons.push(m);
                                        }
                                        if (foundIcons.length >= 2) break;
                                        continue;
                                    }

                                    const textSummary = String(summary || '').toLowerCase();
                                    for (const candidate of orderedIconFileNames || []) {
                                        const name = candidate.toLowerCase();
                                        if (!name.includes('oldworldisland')) continue;
                                        if (textSummary.includes(name) || textSummary.includes(name.replace(/[^a-z0-9]/g, '')) || (textSummary.includes('reward=') && matchesRewardCandidate(name, textSummary))) {
                                            if (!foundIcons.includes(candidate)) foundIcons.push(candidate);
                                            break;
                                        }
                                    }
                                    // Try factory-driven mapping as a last attempt for this summary
                                    if (foundIcons.length === 0) {
                                        try {
                                            const fm = String(summary || '').toLowerCase().match(/factories=([^\]]+)/i);
                                            if (fm && fm[1]) {
                                                const fl = fm[1].split('|').map(x=>String(x||'').trim()).filter(Boolean);
                                                    for (const fRaw of fl) {
                                                    const f = fRaw.toLowerCase();
                                                    const base = f.split('_')[0];
                                                    if (HARDCODED_OLDWORLD_MAP[f]) {
                                                        const cand = resolveIslandIcon(HARDCODED_OLDWORLD_MAP[f]);
                                                        if (cand && !foundIcons.includes(cand)) foundIcons.push(cand);
                                                    }
                                                    else if (HARDCODED_OLDWORLD_MAP[base]) {
                                                        const candb = resolveIslandIcon(HARDCODED_OLDWORLD_MAP[base]);
                                                        if (candb && !foundIcons.includes(candb)) foundIcons.push(candb);
                                                    }
                                                    if (foundIcons.length >= 2) break;
                                                }
                                            }
                                        } catch (e) {}
                                    }
                                    if (foundIcons.length >= 2) break;
                                }
                                const unique = [...new Set(foundIcons)].slice(0,2);
                                if (unique.length > 0) {
                                    for (const u of unique) {
                                        const img = document.createElement('img');
                                        img.src = imageSrcFor(u) || (iconBaseUri + '/' + u);
                                        img.alt = u;
                                        img.classList.add('img-fit');
                                        // img.style.marginRight = '6px';
                                        rightBottom.appendChild(img);
                                    }
                                    rendered = true;
                                }
                            }

                            // If none from old world, and there are new world discoveries,
                            // always render the base new world island image (plantations will be overlaid later).
                            if (!rendered && newSummaries.length > 0) {
                                const img = document.createElement('img');
                                const src = imageSrcFor('newWorldIsland_base.png') || (iconBaseUri + '/newWorldIsland_base.png');
                                img.src = src;
                                img.alt = 'newWorldIsland_base.png';
                                img.classList.add('img-fit');
                                rightTop.appendChild(img);
                                rendered = true;
                            }

                            if (!rendered) {
                                rightTop.textContent = 'Neue Insel (R) - NewWorld';
                                rightBottom.textContent = 'Neue Insel (R) - OldWorld';
                            }
                        } catch (e) {
                            rightTile.textContent = 'Neue Insel (R)';
                        }
                }
            } catch (e) {
                rightTile.textContent = 'Neue Insel (R)';
            }

            // append both sub-tiles into rightTile container
            rightTile.appendChild(rightTop);
            rightTile.appendChild(rightBottom);
            layout.appendChild(rightTile);

            islandSection.appendChild(layout);
            const resourceRow = document.createElement('div');
            resourceRow.className = 'resource-row resource-row--spaced';
            resourceRow.appendChild(createTokenWithIcon('token', `Land frei: ${tiles.freeLand ?? 0}`));
            // instead of using token image, reuse token-pill markup
            const pillLand = document.createElement('span'); pillLand.className = 'token-pill'; pillLand.textContent = `Land frei: ${tiles.freeLand ?? 0}`;
            const pillCoast = document.createElement('span'); pillCoast.className = 'token-pill'; pillCoast.textContent = `Küste frei: ${tiles.freeCoast ?? 0}`;
            const pillSea = document.createElement('span'); pillSea.className = 'token-pill'; pillSea.textContent = `See frei: ${tiles.freeSea ?? 0}`;
            resourceRow.innerHTML = '';
            resourceRow.appendChild(pillLand);
            resourceRow.appendChild(pillCoast);
            resourceRow.appendChild(pillSea);
            islandSection.appendChild(resourceRow);

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
            // Render all resident cards the player has (not limited to 5/6)
            for (const card of cards) {
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
                // hover preview: show a rotated resident card (placeholder residentcard_lv2.png)
                const preview = document.createElement('div');
                preview.className = 'resident-card-preview';
                const previewImg = document.createElement('img');
                const previewSrc = imageSrcFor('residentcard_lv_2.png') || src || (iconBaseUri + '/' + iconName);
                previewImg.src = previewSrc;
                preview.appendChild(previewImg);
                chip.appendChild(preview);

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

        function renderFactoryOverlay(state, boardCenterEl, entry) {
            const availableByType = calculateAvailableFactories(state, entry);
            const board = state?.boardState || {};
            const overlay = document.createElement("div");
            overlay.className = "board-factory-overlay";
            for (const row of FACTORY_LAYOUT) {
                const rowEl = document.createElement("div");
                rowEl.className = "board-factory-row";
                for (const f of row.factories) {
                    const fid = f.id || f.name || "";

                    // Debugging: log availableByType lookups for problematic factories
                    try {
                        const sampleKeys = availableByType && availableByType.byId ? Object.keys(availableByType.byId).slice(0,50) : [];
                        console.log('Factory lookup', { fid: fid, name: f.name, byIdValue: availableByType && availableByType.byId ? availableByType.byId[fid] : undefined, normalizedMatch: availableByType && availableByType.normalizedById ? availableByType.normalizedById[normalizeKey(fid || f.name || '')] : undefined, friendly: availableByType && availableByType.friendly ? availableByType.friendly[f.name] : undefined, nameLookup: availableByType && availableByType.nameLookup ? availableByType.nameLookup[normalizeKey(fid || f.name || '')] : undefined, sampleByIdKeys: sampleKeys });
                    } catch (e) {
                        console.warn('Failed to log factory lookup', e);
                    }

                    // Prefer authoritative boardState counters when present (they reflect current remaining pool)
                    let boardCount = undefined;
                    if (fid) {
                        const lvMatch = fid.match(/_lv(\d+)/i);
                        const levelKey = lvMatch ? `level${lvMatch[1]}` : null;
                        if (/shipyard/i.test(fid) && levelKey && board.shipyards && typeof board.shipyards[levelKey] !== 'undefined') {
                            boardCount = Number(board.shipyards[levelKey]);
                        } else if (/tradeship/i.test(fid) && levelKey && board.ships && board.ships.tradeShips && typeof board.ships.tradeShips[levelKey] !== 'undefined') {
                            boardCount = Number(board.ships.tradeShips[levelKey]);
                        } else if (/explorership/i.test(fid) && levelKey && board.ships && board.ships.explorerShips && typeof board.ships.explorerShips[levelKey] !== 'undefined') {
                            boardCount = Number(board.ships.explorerShips[levelKey]);
                        }
                    }

                    let count = 0;
                    if (typeof boardCount === 'number') {
                        count = boardCount;
                    } else {
                        if (availableByType) {
                            // direct lookup by layout id
                            if (availableByType.byId && typeof availableByType.byId[fid] !== 'undefined') {
                                count = Number(availableByType.byId[fid] || 0);
                            }
                            // fallback: normalized layout id lookup (handles id/name typos)
                            if (!count && availableByType.normalizedById) {
                                const normFid = normalizeKey(fid || f.name || "");
                                if (normFid && typeof availableByType.normalizedById[normFid] !== 'undefined') {
                                    count = Number(availableByType.normalizedById[normFid] || 0);
                                }
                            }
                            // fallback: friendly name
                            if (!count && availableByType.friendly) {
                                count = Number(availableByType.friendly[f.name] || 0);
                            }
                            // final fallback: try nameLookup mapping from normalized json key -> layoutId
                            if (!count && availableByType.nameLookup) {
                                const norm = normalizeKey(fid || f.name || "");
                                const mapped = availableByType.nameLookup[norm];
                                if (mapped && typeof availableByType.byId[mapped] !== 'undefined') {
                                    count = Number(availableByType.byId[mapped] || 0);
                                }
                            }
                        }
                    }

                    // Always render a slot placeholder to keep board layout stable
                    const slot = document.createElement("div");
                    slot.className = "board-factory-slot" + (count === 0 ? " empty" : "");

                    const img = document.createElement("img");
                    img.className = "board-factory-img";
                    img.src = iconBaseUri + "/" + (f.path || "");
                    img.title = f.name || "";
                    slot.appendChild(img);

                    const badge = document.createElement("div");
                    badge.className = "board-slot-count stack-count" + (count === 0 ? " zero" : "");
                    badge.textContent = count;
                    slot.appendChild(badge);

                    rowEl.appendChild(slot);
                }

                if (rowEl.childElementCount > 0) {
                    overlay.appendChild(rowEl);
                }
            }
            boardCenterEl.appendChild(overlay);
        }

        /**
         * Rendert den Expeditionskarten-Stack unten rechts auf dem Mainboard.
         * Liest die Anzahl aus state.cards.expeditionCards (Fallback: 22).
         * Verwendet `residentcard_lv2.png` als Platzhalterbild.
         *
         * @param {Object} state - Game State
         * @param {HTMLElement} boardCenterEl - Mainboard-Container-Element
         */
        function renderExpeditionStack(state, boardCenterEl) {
            const bs = state?.boardState ?? {};
            const cards = bs.cards ?? {};
            const count = Number(cards.expeditionCards ?? 22);

            const overlay = document.createElement("div");
            overlay.className = "expedition-overlay";

            const stackEl = document.createElement("div");
            stackEl.className = "expedition-stack";

            const img = document.createElement("img");
            const imgPath = resolveImagePath("residentcard_lv2.png") || "residentcard_lv2.png";
            img.src = iconBaseUri + "/" + imgPath;
            img.title = `Expeditionskarten`;
            stackEl.appendChild(img);

            const countEl = document.createElement("div");
            countEl.className = "expedition-count stack-count" + (count === 0 ? " zero" : "");
            countEl.textContent = count;
            stackEl.appendChild(countEl);

            overlay.appendChild(stackEl);
            boardCenterEl.appendChild(overlay);
        }

        /**
         * Rendert rechts vom Mainboard zwei Insel-Stapel (New World oben, Old World unten)
         * Liest die Counts aus state.boardState.islands.newWorldIslands / oldWorldIslands
         * und verwendet die Platzhalter-Bilder `newWorldIsland_back.png` und `oldWorldIsland_back.png`.
         * @param {Object} state
         */
        function renderIslandStacks(state) {
            const wrapper = document.querySelector('.mainboard-wrapper');
            if (!wrapper) return;
            // remove existing to avoid duplicates
            const existing = wrapper.querySelector('.island-stacks-container');
            if (existing) existing.remove();

            const bs = state?.boardState ?? {};
            const islands = bs.islands ?? {};
            const newCount = Number(islands.newWorldIslands ?? 0);
            const oldCount = Number(islands.oldWorldIslands ?? 0);

            const container = document.createElement('div');
            container.className = 'island-stacks-container';

            function makeStack(imgName, title, count, type) {
                const stack = document.createElement('div');
                stack.className = 'island-stack';
                if (type === 'new') {
                    stack.classList.add('island-stack-new-world');
                } else if (type === 'old') {
                    stack.classList.add('island-stack-old-world');
                }

                const img = document.createElement('img');
                const imgPath = resolveImagePath(imgName) || imgName;
                img.src = iconBaseUri + '/' + imgPath;
                img.title = title;
                stack.appendChild(img);

                const countEl = document.createElement('div');
                countEl.className = 'island-count stack-count' + (count === 0 ? ' zero' : '');
                countEl.textContent = count;
                stack.appendChild(countEl);

                return stack;
            }

            const newStack = makeStack('newWorldIsland_back.png', 'New World Islands', newCount, 'new');
            const oldStack = makeStack('oldWorldIsland_back.png', 'Old World Islands', oldCount, 'old');

            container.appendChild(newStack);
            container.appendChild(oldStack);

            wrapper.appendChild(container);
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
                countEl.className = "resident-card-count stack-count" + (stack.count === 0 ? " zero" : "");
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
                { type: "artisans", count: pool.artisans ?? 0, img: "residents/artisan_stone.png", label: "Artisans" },
                { type: "engineers", count: pool.engineers ?? 0, img: "residents/engineer_stone.png", label: "Engineers" },
                { type: "investors", count: pool.investors ?? 0, img: "residents/investor_stone.png", label: "Investors" }
            ];

            container.innerHTML = "";

            for (const resident of residents) {
                const group = document.createElement("div");
                group.className = "resident-stone-group";

                    const stackContainer = document.createElement("div");
                    stackContainer.className = "resident-stone-stack";

                    // Split stones into chunks of up to 5. Display two chunks per row (left/right).
                    const total = Number(resident.count || 0);
                    const chunks = [];
                    let rem = total;
                    while (rem > 0) {
                        chunks.push(Math.min(5, rem));
                        rem -= Math.min(5, rem);
                    }
                    if (chunks.length === 0) chunks.push(0);

                    for (let i = 0; i < chunks.length; i += 2) {
                        const row = document.createElement("div");
                        row.className = "resident-stone-row";

                        const leftCount = chunks[i] || 0;
                        const rightCount = (i + 1 < chunks.length) ? chunks[i + 1] : 0;

                        const leftCell = document.createElement("div");
                        leftCell.className = "resident-stone-cell";
                        for (let j = 0; j < leftCount; j++) {
                            const img = document.createElement("img");
                            img.className = "resident-stone-img";
                            const imgPath = resolveImagePath(resident.img) || resident.img;
                            img.src = iconBaseUri + "/" + imgPath;
                            img.title = resident.label;
                            leftCell.appendChild(img);
                        }

                        const rightCell = document.createElement("div");
                        rightCell.className = "resident-stone-cell";
                        for (let j = 0; j < rightCount; j++) {
                            const img = document.createElement("img");
                            img.className = "resident-stone-img";
                            const imgPath = resolveImagePath(resident.img) || resident.img;
                            img.src = iconBaseUri + "/" + imgPath;
                            img.title = resident.label;
                            rightCell.appendChild(img);
                        }

                        row.appendChild(leftCell);
                        row.appendChild(rightCell);
                        stackContainer.appendChild(row);
                    }

                const countEl = document.createElement("div");
                countEl.className = "resident-stone-count stack-count" + (resident.count === 0 ? " zero" : "");
                countEl.textContent = `${resident.count}`;

                // place count above the stone grid
                group.appendChild(countEl);
                group.appendChild(stackContainer);
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
            renderFactoryOverlay(state, boardCenter, entry);
            renderResidentCardsOverlay(state, boardCenter);
            renderExpeditionStack(state, boardCenter);
            renderIslandStacks(state);
            mainBoardEl.appendChild(boardCenter);

            // Pools under the mainboard were removed per user request.
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
            // plus buttons disabled only when at the last state
            const plus5El = document.getElementById("plus5Btn");
            const plus10El = document.getElementById("plus10Btn");
            if (plus5El) plus5El.disabled = index >= entries.length - 1;
            if (plus10El) plus10El.disabled = index >= entries.length - 1;
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

        function goNextBy(delta) {
            if (!Number.isFinite(delta) || delta === 0) return;
            const target = Math.min(entries.length - 1, Math.max(0, index + delta));
            if (target !== index) {
                index = target;
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

        // Toggle stack counts (show/hide) via body class `stack-count-hidden`
        const toggleControlBarBtn = document.getElementById('toggleControlBarBtn');
        if (toggleControlBarBtn) {
            toggleControlBarBtn.addEventListener('click', () => {
                const body = document.body;
                body.classList.toggle('stack-count-hidden');
                const hidden = body.classList.contains('stack-count-hidden');
                // Button shows the next action (turn counts on/off)
                toggleControlBarBtn.textContent = hidden ? 'Zahlen an' : 'Zahlen aus';
            });
        }

        // Toggle panel borders (show/hide) via body class `panel-borders-hidden`
        const togglePanelBordersBtn = document.getElementById('togglePanelBordersBtn');
        if (togglePanelBordersBtn) {
            togglePanelBordersBtn.addEventListener('click', () => {
                const body = document.body;
                body.classList.toggle('panel-borders-hidden');
                const hidden = body.classList.contains('panel-borders-hidden');
                togglePanelBordersBtn.textContent = hidden ? 'Rahmen an' : 'Rahmen aus';
            });
        }

        prevBtn.addEventListener("click", goPrevious);
        nextBtn.addEventListener("click", goNext);
        // Jump to the last available state and render immediately
        const lastBtn = document.getElementById("lastBtn");
        if (lastBtn) {
            lastBtn.addEventListener("click", () => {
                if (!Array.isArray(entries) || entries.length === 0) return;
                index = Math.max(0, entries.length - 1);
                render();
            });
        }
        // plus buttons: advance multiple states
        const plus5Btn = document.getElementById("plus5Btn");
        const plus10Btn = document.getElementById("plus10Btn");
        if (plus5Btn) plus5Btn.addEventListener("click", () => goNextBy(5));
        if (plus10Btn) plus10Btn.addEventListener("click", () => goNextBy(10));
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
            banner.classList.add('top-banner');
            banner.textContent = 'render() ERROR: ' + e.message + ' | ' + (e.stack || '');
            document.body.appendChild(banner);
        }

        // Berechnet verfügbare Fabriken/Schiffe aus dem Board-State.
        // Liefert ein Objekt mit folgenden Feldern:
        // - friendly: displayName -> count (für FACTORY_LAYOUT Anzeige)
        // - byId: id -> count (keys wie in FACTORY_LAYOUT f.id)
        // - nameLookup: displayName -> id
        // - normalizedById: normalizedId -> count (lowercased, stripped)
        function calculateAvailableFactories(state, entry) {
            const board = state?.boardState || {};
            const byId = {};
            const normalizedById = {};
            const nameLookup = {};

            // factories (generic named factories)
            if (board.factories && typeof board.factories === 'object') {
                for (const [k, v] of Object.entries(board.factories)) {
                    const cnt = Number(v || 0);
                    byId[k] = cnt;
                    normalizedById[String(k).toLowerCase().replace(/[^a-z0-9_]/g, '')] = cnt;
                }
            }

            // shipyards (level keys like level1 -> produce id shipyard_lv1)
            if (board.shipyards && typeof board.shipyards === 'object') {
                for (const [levelKey, v] of Object.entries(board.shipyards)) {
                    const m = String(levelKey).match(/level(\d+)/i);
                    const lv = m ? m[1] : levelKey.replace(/[^0-9]/g, '') || '';
                    const id = `shipyard_lv${lv}`;
                    const cnt = Number(v || 0);
                    byId[id] = cnt;
                    normalizedById[id.toLowerCase()] = cnt;
                }
            }

            // ships (tradeShips / explorerShips)
            if (board.ships && typeof board.ships === 'object') {
                if (board.ships.tradeShips && typeof board.ships.tradeShips === 'object') {
                    for (const [levelKey, v] of Object.entries(board.ships.tradeShips)) {
                        const m = String(levelKey).match(/level(\d+)/i);
                        const lv = m ? m[1] : levelKey.replace(/[^0-9]/g, '') || '';
                        const id = `tradeShip_lv${lv}`;
                        const cnt = Number(v || 0);
                        byId[id] = cnt;
                        normalizedById[id.toLowerCase()] = cnt;
                    }
                }
                if (board.ships.explorerShips && typeof board.ships.explorerShips === 'object') {
                    for (const [levelKey, v] of Object.entries(board.ships.explorerShips)) {
                        const m = String(levelKey).match(/level(\d+)/i);
                        const lv = m ? m[1] : levelKey.replace(/[^0-9]/g, '') || '';
                        const id = `explorerShip_lv${lv}`;
                        const cnt = Number(v || 0);
                        byId[id] = cnt;
                        normalizedById[id.toLowerCase()] = cnt;
                    }
                }
            }

            // Build friendly/nameLookup from FACTORY_LAYOUT when available
            const friendly = {};
            try {
                if (Array.isArray(FACTORY_LAYOUT)) {
                    for (const row of FACTORY_LAYOUT) {
                        for (const f of row.factories || []) {
                            const layoutId = f.id || f.name || "";
                            const display = f.name || layoutId;
                            nameLookup[display] = layoutId;
                            // try direct key, but also aggregate variants (color suffixes)
                            let sum = Number(byId[layoutId] || 0);
                            for (const k of Object.keys(byId)) {
                                if (k && layoutId && k.toLowerCase().startsWith((layoutId + "_").toLowerCase())) {
                                    sum += Number(byId[k] || 0);
                                }
                            }
                            friendly[display] = sum;
                        }
                    }
                }
            } catch (e) {
                // ignore
            }

            return {
                friendly: friendly,
                byId: byId,
                nameLookup: nameLookup,
                normalizedById: normalizedById,
            };
        }

        // Normalisiert einen beliebigen Key (id/name) für Lookup-Vergleiche.
        // Entfernt Nicht-Alphanumerische Zeichen und wandelt in lowercase.
        function normalizeKey(value) {
            const s = String(value ?? "").toLowerCase().trim();
            return s.replace(/[^a-z0-9_]/g, '');
        }