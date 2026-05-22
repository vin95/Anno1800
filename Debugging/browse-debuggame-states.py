from __future__ import annotations

import argparse
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import webbrowser
from pathlib import Path
from typing import Any


ACTION_RE = re.compile(r"^action_(\d+)_")
INITIAL_RE = re.compile(r"^initial_")
SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parent if SCRIPT_DIR.name.lower() == "debugging" else SCRIPT_DIR
DEFAULT_GAME_STATES_DIR = PROJECT_ROOT / "game-states"
ACTION_MAPPING_FILE = SCRIPT_DIR / "action-mapping.json"


def default_action_mappings() -> dict[str, Any]:
    return {
        "exact": {
            "Carneval[]": "Karneval",
            "Expedition[]": "Expedition starten",
            "DiscoverOldWorldIsland[]": "Alte-Welt-Insel entdecken",
            "DiscoverNewWorldIsland[]": "Neue-Welt-Insel entdecken",
        },
        "prefix": {
            "UpgradeResident[": "Bewohner aufwerten",
            "SettleResident[": "Bewohner ansiedeln",
            "BuildShipyard[": "Werft bauen",
            "BuildFactory[": "Fabrik bauen",
            "BuildShips[": "Schiffe bauen",
            "FulfillNeeds[": "Beduerfnisse erfuellen",
            "TradeGoods[": "Gueter handeln",
            "ProduceGoods[": "Gueter produzieren",
            "DrawResidentCard[": "Bewohnerkarte ziehen",
            "InvestorGoldAction[": "Investoren-Goldaktion",
            "DiscardResidentCardAction[": "Bewohnerkarte abwerfen",
        },
        "regex": [
            {
                "pattern": r"^SettleResident\[level=(?P<level>\d+)\]$",
                "template": "Bewohner ansiedeln (Stufe {level})",
            },
            {
                "pattern": r"^BuildShipyard\[level=(?P<level>\d+)\]$",
                "template": "Werft bauen (Stufe {level})",
            },
            {
                "pattern": r"^BuildShips\[shipType=(?P<shipType>[^,\]]+), level=(?P<level>\d+), amount=(?P<amount>\d+)\]$",
                "template": "Schiffe bauen ({shipType}, Stufe {level}, Anzahl {amount})",
            },
            {
                "pattern": r"^TradeGoods\[good=(?P<good>[^,\]]+), player=(?P<player>\d+)\]$",
                "template": "Gueter handeln ({good}, Spielerindex {player})",
            },
        ],
    }


def load_action_mappings(path: Path = ACTION_MAPPING_FILE) -> dict[str, Any]:
    defaults = default_action_mappings()
    if not path.exists():
        return defaults

    try:
        with path.open("r", encoding="utf-8") as handle:
            loaded = json.load(handle)
    except (OSError, json.JSONDecodeError):
        return defaults

    if not isinstance(loaded, dict):
        return defaults

    result: dict[str, Any] = {
        "exact": dict(defaults.get("exact", {})),
        "prefix": dict(defaults.get("prefix", {})),
        "regex": list(defaults.get("regex", [])),
    }

    if isinstance(loaded.get("exact"), dict):
        result["exact"].update({str(k): str(v) for k, v in loaded["exact"].items()})

    if isinstance(loaded.get("prefix"), dict):
        result["prefix"].update({str(k): str(v) for k, v in loaded["prefix"].items()})

    if isinstance(loaded.get("regex"), list):
        for item in loaded["regex"]:
            if not isinstance(item, dict):
                continue
            pattern = item.get("pattern")
            template = item.get("template")
            if isinstance(pattern, str) and isinstance(template, str):
                result["regex"].append({"pattern": pattern, "template": template})

    return result


ACTION_MAPPINGS = load_action_mappings()


def humanize_action(raw_action: Any) -> str | None:
    if raw_action is None:
        return None

    action = str(raw_action)
    exact = ACTION_MAPPINGS.get("exact", {})
    if action in exact:
        return exact[action]

    for rule in ACTION_MAPPINGS.get("regex", []):
        pattern = rule.get("pattern")
        template = rule.get("template")
        if not isinstance(pattern, str) or not isinstance(template, str):
            continue
        match = re.match(pattern, action)
        if match:
            groups = {key: value for key, value in match.groupdict().items() if value is not None}
            try:
                return template.format(**groups)
            except KeyError:
                return template

    for prefix, label in ACTION_MAPPINGS.get("prefix", {}).items():
        if action.startswith(prefix):
            return str(label)

    plain = action.split("[", 1)[0]
    spaced = re.sub(r"(?<!^)([A-Z])", r" \\1", plain).strip()
    return spaced if spaced else action


def extract_action_amount(raw_action: Any) -> int | None:
    if raw_action is None:
        return None

    action = str(raw_action)
    match = re.search(r"(?:^|[\[,\s])amount=(\d+)(?:[,\]])", action)
    if match:
        return int(match.group(1))

    return None


def find_player_state(state: dict[str, Any], player_name: str) -> dict[str, Any] | None:
    players = state.get("players")
    if not isinstance(players, list):
        return None

    for player in players:
        if isinstance(player, dict) and player.get("name") == player_name:
            return player

    return None


def infer_upgrade_resident_amount(
    current_state: dict[str, Any],
    previous_state: dict[str, Any] | None,
    executed_by_player: Any,
) -> int | None:
    if previous_state is None or not executed_by_player:
        return None

    player_name = str(executed_by_player)
    current_player = find_player_state(current_state, player_name)
    previous_player = find_player_state(previous_state, player_name)
    if current_player is None or previous_player is None:
        return None

    current_levels = (((current_player.get("residents") or {}).get("byLevel")) or {})
    previous_levels = (((previous_player.get("residents") or {}).get("byLevel")) or {})
    if not isinstance(current_levels, dict) or not isinstance(previous_levels, dict):
        return None

    levels = ("level1", "level2", "level3", "level4", "level5")
    deltas: dict[str, int] = {}
    for level in levels:
        cur_val = current_levels.get(level)
        prev_val = previous_levels.get(level)
        if not isinstance(cur_val, int) or not isinstance(prev_val, int):
            return None
        deltas[level] = cur_val - prev_val

    # Reconstruct the number of residents upgraded from each level i to i+1.
    # For upgrade-only transitions:
    #   delta(level1) = -x1
    #   delta(level2) = x1 - x2
    #   delta(level3) = x2 - x3
    #   delta(level4) = x3 - x4
    #   delta(level5) = x4
    x1 = -deltas["level1"]
    x2 = x1 - deltas["level2"]
    x3 = x2 - deltas["level3"]
    x4 = x3 - deltas["level4"]

    if min(x1, x2, x3, x4) < 0:
        return None

    if x4 != deltas["level5"]:
        return None

    upgraded = x1 + x2 + x3 + x4
    return upgraded if upgraded > 0 else None


def action_with_amount(
    raw_action: Any,
    current_state: dict[str, Any],
    previous_state: dict[str, Any] | None,
) -> str | None:
    readable = humanize_action(raw_action)
    if readable is None:
        return None

    amount = extract_action_amount(raw_action)
    if amount is None:
        action_name = str(raw_action).split("[", 1)[0] if raw_action is not None else ""
        if action_name == "UpgradeResident":
            amount = infer_upgrade_resident_amount(
                current_state=current_state,
                previous_state=previous_state,
                executed_by_player=current_state.get("executedByPlayer"),
            )

    if isinstance(amount, int) and amount > 1:
        return f"{readable} (Anzahl: {amount})"

    return readable


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Browse Anno 1800 debug game states one keypress at a time."
    )
    parser.add_argument(
        "--dir",
        default=None,
        help="Path to the game state directory",
    )
    parser.add_argument(
        "--web",
        action="store_true",
        help="Render states in a browser with previous/next controls",
    )
    parser.add_argument(
        "--firefox",
        action="store_true",
        help="Open the generated web view in Firefox (falls back to default browser)",
    )
    parser.add_argument(
        "--out",
        default=None,
        help="Optional output HTML file path for --web mode",
    )
    return parser.parse_args()


def find_latest_debuggame_dir(base_dir: Path = DEFAULT_GAME_STATES_DIR) -> Path | None:
    if not base_dir.exists():
        return None

    candidates = [
        path
        for path in base_dir.iterdir()
        if path.is_dir() and re.match(r"^Debuggame-\d+$", path.name)
    ]
    if not candidates:
        return None

    def debuggame_number(path: Path) -> int:
        return int(path.name.split("-")[-1])

    return max(candidates, key=debuggame_number)


def sort_key(path: Path) -> tuple[int, int | str]:
    name = path.name
    if INITIAL_RE.match(name):
        return (0, 0)
    match = ACTION_RE.match(name)
    if match:
        return (1, int(match.group(1)))
    return (2, name)


def action_label(path: Path) -> str:
    name = path.name
    if INITIAL_RE.match(name):
        return "initial"
    match = ACTION_RE.match(name)
    if match:
        return f"action_{match.group(1)}"
    return name


def load_state(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def is_primitive(value: Any) -> bool:
    return isinstance(value, (str, int, float, bool)) or value is None


def should_skip_diff(prefix: str) -> bool:
    # Noise reduction: population pool changes are intentionally hidden.
    return prefix.startswith("boardState.populationPool.") or prefix == "timestamp"


def format_diff_label(prefix: str) -> str:
    label = prefix
    level_labels = {
        "level1": "Farmer",
        "level2": "Worker",
        "level3": "Artisan",
        "level4": "Engineer",
        "level5": "Investor",
    }
    for level_key, level_label in level_labels.items():
        marker = f".residents.byLevel.{level_key}"
        if marker in label:
            label = label.replace(marker, f".{level_label}")

    label = re.sub(r"players\[(\d+)\]", lambda m: f"Spieler {int(m.group(1)) + 1}", label)
    return label


def format_player_reference(value: Any) -> Any:
    if isinstance(value, int):
        return f"Spieler {value + 1}"

    if isinstance(value, str):
        stripped = value.strip()
        if stripped.isdigit():
            return f"Spieler {int(stripped) + 1}"
        match = re.match(r"^Player\s+(\d+)$", stripped)
        if match:
            return f"Spieler {int(match.group(1))}"
        return value.replace("Player ", "Spieler ")

    return value


def player_index_from_name(state: dict[str, Any], player_name: Any) -> int | None:
    if not player_name:
        return None
    players = state.get("players")
    if not isinstance(players, list):
        return None
    player_name_str = str(player_name)
    for index, player in enumerate(players):
        if isinstance(player, dict) and str(player.get("name")) == player_name_str:
            return index
    return None


def summarize_goods(values: list[str]) -> str:
    if not values:
        return ""
    counts: dict[str, int] = {}
    for value in values:
        counts[value] = counts.get(value, 0) + 1
    return ", ".join(f"{count}x {good}" for good, count in sorted(counts.items()))


def summarize_factories(values: list[str]) -> str:
    if not values:
        return ""

    counts: dict[str, int] = {}
    order: list[str] = []
    for value in values:
        if value not in counts:
            order.append(value)
        counts[value] = counts.get(value, 0) + 1

    parts: list[str] = []
    for value in order:
        count = counts[value]
        parts.append(f"{count}x {value}" if count > 1 else value)

    return ", ".join(parts)


def with_umlauts(text: str | None) -> str | None:
    if text is None:
        return None

    normalized = text
    replacements = {
        "Aenderungen": "Änderungen",
        "aenderungen": "änderungen",
        "Ausgefuehrte": "Ausgeführte",
        "ausgefuehrte": "ausgeführte",
        "fuer": "für",
        "ueber": "über",
        "Beduerfnisse": "Bedürfnisse",
        "Gueter": "Güter",
        "gueter": "güter",
        "oe": "ö",
        "Oe": "Ö",
        "ae": "ä",
        "Ae": "Ä",
        "ue": "ü",
        "Ue": "Ü",
    }
    for old, new in replacements.items():
        normalized = normalized.replace(old, new)
    return normalized


def parse_action_details_entries(action_details: str) -> tuple[list[str], str | None]:
    parts = [part.strip() for part in action_details.split(";") if part.strip()]
    goods_entries: list[str] = []
    chip_line: str | None = None

    for part in parts:
        if part.startswith("Chips verwendet:"):
            chip_line = part
        else:
            goods_entries.append(part)

    return goods_entries, chip_line


def clean_goods_detail_entry(entry: str) -> str:
    cleaned = entry.strip()
    for prefix in ("Verbrauch fuer Aktion:", "Verbrauch für Aktion:"):
        if cleaned.startswith(prefix):
            cleaned = cleaned[len(prefix) :].strip()
            break
    return cleaned


def is_build_action(action_name: str) -> bool:
    return action_name.startswith("Build")


def normalize_chip_line(chip_line: str | None, action_name: str) -> str | None:
    if not chip_line:
        return None

    text = chip_line.strip()
    if not text.startswith("Chips verwendet:"):
        return text

    payload = text[len("Chips verwendet:") :].strip()
    if not payload:
        return None

    parts = [part.strip() for part in payload.split(",") if part.strip()]
    trade_part: str | None = None
    explorer_part: str | None = None

    for part in parts:
        if part.startswith("Tradechips="):
            trade_part = part
        elif part.startswith("Explorerchips="):
            value_raw = part.split("=", 1)[1].strip() if "=" in part else ""
            explorer_value: int | None = None
            if value_raw.isdigit():
                explorer_value = int(value_raw)
            if is_build_action(action_name) and isinstance(explorer_value, int) and explorer_value > 0:
                explorer_part = part

    selected: list[str] = []
    if trade_part:
        selected.append(trade_part)
    if explorer_part:
        selected.append(explorer_part)

    if not selected:
        return None

    return f"Chips verwendet: {', '.join(selected)}"


def calculate_chip_usage_from_items(items: list[str]) -> tuple[int, int]:
    trade_chips = 0
    explorer_chips = 0

    for item in items:
        traded_match = re.search(r"\[gehandelt\s*\([^\)]*Kosten\s*(\d+)\s*Chip", item)
        if traded_match:
            trade_chips += int(traded_match.group(1))

        imported_match = re.search(r"\[importiert\s*\(Explorerchips\s*(\d+)\)", item)
        if imported_match:
            explorer_chips += int(imported_match.group(1))

        traded_explorer_match = re.search(r"\[gehandelt\s*\([^\)]*Explorerchip[s]?\s*(\d+)", item)
        if traded_explorer_match:
            explorer_chips += int(traded_explorer_match.group(1))

    return trade_chips, explorer_chips


def build_chip_line_for_items(items: list[str], action_name: str) -> str | None:
    trade_chips, explorer_chips = calculate_chip_usage_from_items(items)
    if trade_chips <= 0 and explorer_chips <= 0:
        return None

    segments: list[str] = []
    if trade_chips > 0:
        segments.append(f"Tradechips={trade_chips}")

    if is_build_action(action_name) and explorer_chips > 0:
        segments.append(f"Explorerchips={explorer_chips}")

    if not segments:
        return None

    return f"Chips verwendet: {', '.join(segments)}"


def extract_production_factories(action_details: str | None) -> str | None:
    if not action_details:
        return None

    factories: list[str] = []
    pattern = re.compile(r"\[produziert\s*\(([^,\]]+),\s*Bewohnerstufe\s*\d+\)\]")
    for match in pattern.finditer(action_details):
        factory_name = match.group(1).strip()
        if factory_name:
            factories.append(factory_name)

    if not factories:
        return None

    return summarize_factories(factories)


def split_entries_evenly(entries: list[str], bucket_count: int) -> list[list[str]]:
    if bucket_count <= 1:
        return [entries]

    buckets: list[list[str]] = [[] for _ in range(bucket_count)]
    if not entries:
        return buckets

    # Keep original order and create contiguous chunks.
    base_size = len(entries) // bucket_count
    remainder = len(entries) % bucket_count
    start = 0
    for i in range(bucket_count):
        extra = 1 if i < remainder else 0
        size = base_size + extra
        if size > 0:
            buckets[i] = entries[start : start + size]
            start += size
    return buckets


def build_action_details_blocks(
    raw_action: Any,
    current_state: dict[str, Any],
    previous_state: dict[str, Any] | None,
    action_details: str | None,
) -> list[dict[str, Any]]:
    if not action_details:
        return []

    details_text = str(action_details)
    goods_entries, chip_line = parse_action_details_entries(details_text)
    if not goods_entries and not chip_line:
        return []

    action_name = str(raw_action).split("[", 1)[0] if raw_action is not None else ""
    amount = extract_action_amount(raw_action)
    if amount is None and action_name == "UpgradeResident":
        amount = infer_upgrade_resident_amount(
            current_state=current_state,
            previous_state=previous_state,
            executed_by_player=current_state.get("executedByPlayer"),
        )

    block_count = amount if isinstance(amount, int) and amount > 1 else 1
    cleaned_goods_entries = [clean_goods_detail_entry(entry) for entry in goods_entries]
    if cleaned_goods_entries:
        block_count = min(block_count, len(cleaned_goods_entries))
    grouped_entries = split_entries_evenly(cleaned_goods_entries, block_count)
    fallback_chip_line = normalize_chip_line(chip_line, action_name)

    if action_name == "UpgradeResident":
        prefix = "ResidentUpdate"
    elif action_name == "BuildShips":
        prefix = "Shipbuild"
    else:
        prefix = "Aktionsschritt"

    blocks: list[dict[str, Any]] = []
    for i in range(block_count):
        items = grouped_entries[i] if i < len(grouped_entries) else []
        calculated_chip_line = build_chip_line_for_items(items, action_name)
        final_chip_line = calculated_chip_line
        if final_chip_line is None and i == 0 and block_count == 1:
            final_chip_line = fallback_chip_line
        if final_chip_line:
            items = items + [final_chip_line]
        title = f"{prefix}{i + 1}" if block_count > 1 else "Details"
        blocks.append({"title": title, "items": [with_umlauts(item) or item for item in items]})

    return blocks


def build_action_details(
    raw_action: Any,
    current_state: dict[str, Any],
    previous_state: dict[str, Any] | None,
) -> str | None:
    if previous_state is None or raw_action is None:
        return None

    action = str(raw_action)
    executed_by = current_state.get("executedByPlayer")
    player_index = player_index_from_name(current_state, executed_by)

    if action.startswith("TradeGoods["):
        match = re.search(r"TradeGoods\[good=([^,\]]+),\s*player=(\d+)\]", action)
        if match:
            good = match.group(1)
            partner = int(match.group(2)) + 1
            chips_text = "unbekannt"
            if player_index is not None:
                cur_players = current_state.get("players")
                prev_players = previous_state.get("players")
                if isinstance(cur_players, list) and isinstance(prev_players, list) and player_index < len(cur_players) and player_index < len(prev_players):
                    cur_res = ((cur_players[player_index] or {}).get("resources") or {})
                    prev_res = ((prev_players[player_index] or {}).get("resources") or {})
                    cur_chips = cur_res.get("tradeChips") if isinstance(cur_res, dict) else None
                    prev_chips = prev_res.get("tradeChips") if isinstance(prev_res, dict) else None
                    if isinstance(cur_chips, int) and isinstance(prev_chips, int):
                        used = max(0, prev_chips - cur_chips)
                        chips_text = str(used)
            return f"Handel: 1x {good} (Tradechips: {chips_text}, mit Spieler {partner})"

    if player_index is None:
        return None

    cur_players = current_state.get("players")
    prev_players = previous_state.get("players")
    if not isinstance(cur_players, list) or not isinstance(prev_players, list):
        return None
    if player_index >= len(cur_players) or player_index >= len(prev_players):
        return None

    cur_player = cur_players[player_index] if isinstance(cur_players[player_index], dict) else {}
    prev_player = prev_players[player_index] if isinstance(prev_players[player_index], dict) else {}

    cur_residents = ((cur_player.get("residents") or {}).get("byLevel") or {})
    prev_residents = ((prev_player.get("residents") or {}).get("byLevel") or {})
    traded_for_levels: list[str] = []
    if isinstance(cur_residents, dict) and isinstance(prev_residents, dict):
        level_goods = {
            "level2": "Worker",
            "level3": "Artisan",
            "level4": "Engineer",
            "level5": "Investor",
        }
        for level, label in level_goods.items():
            cur_val = cur_residents.get(level)
            prev_val = prev_residents.get(level)
            if isinstance(cur_val, int) and isinstance(prev_val, int) and cur_val > prev_val:
                traded_for_levels.extend([label] * (cur_val - prev_val))

    cur_resources = (cur_player.get("resources") or {}) if isinstance(cur_player, dict) else {}
    prev_resources = (prev_player.get("resources") or {}) if isinstance(prev_player, dict) else {}
    chips_used = None
    if isinstance(cur_resources, dict) and isinstance(prev_resources, dict):
        cur_chips = cur_resources.get("tradeChips")
        prev_chips = prev_resources.get("tradeChips")
        if isinstance(cur_chips, int) and isinstance(prev_chips, int):
            chips_used = max(0, prev_chips - cur_chips)

    if traded_for_levels and chips_used and chips_used > 0:
        goods_text = summarize_goods(traded_for_levels)
        return f"Verbrauch fuer Aktion: {goods_text} (ueber Handel, Tradechips: {chips_used}, Spieler: {format_player_reference(executed_by)})"

    if chips_used and chips_used > 0 and (action.startswith("Build") or action.startswith("UpgradeResident") or action.startswith("SettleResident")):
        return f"Verbrauch fuer Aktion: Tradechips {chips_used}"

    return None


def diff_values(
    current: Any,
    previous: Any,
    prefix: str = "",
    action_details: str | None = None,
) -> list[str]:
    lines: list[str] = []
    display_prefix = format_diff_label(prefix) if prefix else ""

    if prefix and should_skip_diff(prefix):
        return lines

    if current is None and previous is None:
        return lines

    if previous is None:
        if prefix:
            if prefix in {"executedAction", "executedByPlayer"}:
                if prefix == "executedAction":
                    readable = humanize_action(current)
                    amount = extract_action_amount(current)
                    if readable is not None and isinstance(amount, int) and amount > 1:
                        lines.append(f"{display_prefix}: {readable} (Anzahl:{amount})")
                    else:
                        lines.append(f"{display_prefix}: {readable}")
                else:
                    lines.append(f"{display_prefix}: {format_player_reference(current)}")
            else:
                lines.append(f"{display_prefix}: hinzugefuegt")
        return lines

    if current is None:
        if prefix:
            lines.append(f"{display_prefix}: entfernt")
        return lines

    if isinstance(current, dict) and isinstance(previous, dict):
        keys = sorted(set(current.keys()) | set(previous.keys()))
        for key in keys:
            next_prefix = f"{prefix}.{key}" if prefix else key
            lines.extend(diff_values(current.get(key), previous.get(key), next_prefix, action_details))
        return lines

    if isinstance(current, list) and isinstance(previous, list):
        if len(current) != len(previous):
            label = display_prefix or "liste"
            lines.append(f"{label}: Anzahl {len(previous)} -> {len(current)}")

        max_len = max(len(current), len(previous))
        for index in range(max_len):
            next_prefix = f"{prefix}[{index}]" if prefix else f"[{index}]"
            cur_item = current[index] if index < len(current) else None
            prev_item = previous[index] if index < len(previous) else None
            lines.extend(diff_values(cur_item, prev_item, next_prefix, action_details))
        return lines

    if is_primitive(current) and is_primitive(previous):
        if current != previous:
            label = display_prefix or "wert"
            if label == "executedAction":
                prev_action = humanize_action(previous)
                cur_action = humanize_action(current)
                lines.append(f"{label}: '{prev_action}' -> '{cur_action}'")
                return lines
            if label == "executedActionDetails":
                prev_details = str(previous).replace("; ", ";\n")
                cur_details = str(current).replace("; ", ";\n")
                lines.append(f"{label}: '{prev_details}' -> '{cur_details}'")
                return lines
            if label == "currentPlayer":
                prev_player = format_player_reference(previous)
                cur_player = format_player_reference(current)
                lines.append(f"{label}: '{prev_player}' -> '{cur_player}'")
                return lines
            if isinstance(current, (int, float)) and isinstance(previous, (int, float)):
                lines.append(f"{label}: {previous} -> {current}")
            else:
                lines.append(f"{label}: '{previous}' -> '{current}'")
        return lines

    if current != previous:
        label = display_prefix or "wert"
        lines.append(f"{label}: '{previous}' -> '{current}'")

    return lines


def enrich_working_diffs(diffs: list[str], action_details: str | None) -> list[str]:
    factory_summary = extract_production_factories(action_details)
    if not factory_summary:
        return diffs

    enriched: list[str] = []
    working_pattern = re.compile(r"\.residents\.byStatus\.working:\s*\d+\s*->\s*\d+")

    for line in diffs:
        enriched.append(line)
        if working_pattern.search(line):
            enriched.append(f"  Zugewiesene Fabriken: {factory_summary}")

    return enriched


def read_key() -> str:
    if os.name == "nt":
        import msvcrt

        return msvcrt.getwch()

    return sys.stdin.read(1)


def clear_screen() -> None:
    if os.name == "nt":
        os.system("cls")
    else:
        os.system("clear")


def state_for_display(state: dict[str, Any]) -> dict[str, Any]:
    filtered = dict(state)
    filtered.pop("timestamp", None)
    return filtered


def build_state_entries(files: list[Path]) -> list[dict[str, Any]]:
    previous_state: dict[str, Any] | None = None
    entries: list[dict[str, Any]] = []

    for index, path in enumerate(files, start=1):
        state = load_state(path)
        action_details = state.get("executedActionDetails") or build_action_details(
            state.get("executedAction"),
            state,
            previous_state,
        )
        action_details = with_umlauts(str(action_details)) if action_details else None
        diffs = [] if previous_state is None else [
            with_umlauts(line) or line for line in diff_values(state, previous_state, action_details=action_details)
        ]
        diffs = enrich_working_diffs(diffs, action_details)

        entries.append(
            {
                "index": index,
                "fileName": path.name,
                "actionLabel": with_umlauts(action_label(path)),
                "executedAction": state.get("executedAction"),
                "executedActionReadable": with_umlauts(
                    action_with_amount(state.get("executedAction"), state, previous_state)
                ),
                "executedByPlayer": state.get("executedByPlayer"),
                "actionDetails": action_details,
                "actionDetailsBlocks": build_action_details_blocks(
                    state.get("executedAction"),
                    state,
                    previous_state,
                    action_details,
                ),
                "agentMainActionScores": state.get("agentMainActionScores")
                if isinstance(state.get("agentMainActionScores"), list)
                else [],
                "agentStrategyName": state.get("agentStrategyName"),
                "round": state.get("round"),
                "currentPlayer": format_player_reference(state.get("currentPlayer")),
                "isInitial": previous_state is None,
                "diffs": diffs,
                "state": state_for_display(state),
            }
        )

        previous_state = state

    return entries


def render_web_view(state_dir: Path, entries: list[dict[str, Any]]) -> str:
    payload = json.dumps(entries, ensure_ascii=False)
    title = f"Anno 1800 Debuggame States - {state_dir}"

    return f"""<!doctype html>
<html lang=\"de\">
<head>
    <meta charset=\"utf-8\">
    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">
    <title>{title}</title>
    <style>
        :root {{
            --bg-0: #0f172a;
            --bg-1: #111827;
            --panel: #111827dd;
            --text: #f3f4f6;
            --muted: #9ca3af;
            --accent: #14b8a6;
            --accent-strong: #0f766e;
            --border: #334155;
            --ok: #10b981;
        }}

        * {{ box-sizing: border-box; }}
        body {{
            margin: 0;
            min-height: 100vh;
            color: var(--text);
            background:
                radial-gradient(1200px 700px at 10% -10%, #1e293b 0%, transparent 60%),
                radial-gradient(900px 600px at 100% 0%, #164e63 0%, transparent 60%),
                linear-gradient(180deg, var(--bg-0), var(--bg-1));
            font-family: \"Segoe UI\", Tahoma, Geneva, Verdana, sans-serif;
            padding: 16px;
        }}

        .container {{
            max-width: 1100px;
            margin: 0 auto;
            display: grid;
            gap: 14px;
        }}

        .panel {{
            background: var(--panel);
            border: 1px solid var(--border);
            border-radius: 14px;
            padding: 14px;
            backdrop-filter: blur(4px);
        }}

        .header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 10px;
            flex-wrap: wrap;
        }}

        h1 {{
            margin: 0;
            font-size: clamp(1.1rem, 2vw, 1.5rem);
            letter-spacing: 0.01em;
        }}

        .muted {{ color: var(--muted); }}

        .controls {{
            display: flex;
            align-items: center;
            gap: 8px;
            flex-wrap: wrap;
        }}

        button {{
            border: 1px solid var(--accent-strong);
            background: linear-gradient(180deg, var(--accent), #0d9488);
            color: #042f2e;
            font-weight: 700;
            border-radius: 10px;
            padding: 8px 12px;
            cursor: pointer;
            min-width: 90px;
        }}

        button:disabled {{
            opacity: 0.45;
            cursor: not-allowed;
        }}

        .grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 8px 14px;
        }}

        .kv {{
            background: #0b1220a6;
            border: 1px solid #1f2937;
            border-radius: 10px;
            padding: 10px;
        }}

        .kv .k {{
            color: var(--muted);
            font-size: 0.85rem;
            margin-bottom: 3px;
        }}

        .kv .v {{
            font-weight: 600;
            word-break: break-word;
        }}

        .diff-list {{
            margin: 8px 0 0;
            padding-left: 20px;
            line-height: 1.35;
        }}

        .diff-list li {{
            white-space: pre-wrap;
        }}

        .action-block {{
            margin-top: 10px;
            padding: 10px;
            border: 1px solid #1f2937;
            border-radius: 10px;
            background: #0b1220a6;
        }}

        .action-title {{
            font-weight: 700;
            margin: 0 0 6px;
        }}

        .action-list {{
            display: grid;
            gap: 8px;
        }}

        .action-paragraph {{
            margin: 0;
            line-height: 1.5;
        }}

        details.collapsible {{
            margin-top: 8px;
            border: 1px solid #1f2937;
            border-radius: 10px;
            background: #0b1220a6;
            padding: 8px 10px;
        }}

        details.collapsible > summary {{
            cursor: pointer;
            font-weight: 700;
            outline: none;
        }}

        .agent-scores {{
            margin: 10px 0 0;
            display: grid;
            gap: 8px;
        }}

        .agent-score-row {{
            border: 1px solid #1f2937;
            border-radius: 8px;
            padding: 8px;
            background: #0b1220d8;
        }}

        .agent-score-main {{
            font-weight: 700;
        }}

        .agent-score-meta {{
            color: var(--muted);
            margin-top: 2px;
            font-size: 0.92rem;
            word-break: break-word;
        }}

        .agent-score-selected {{
            color: var(--ok);
        }}

        .diff-empty {{ color: var(--ok); font-weight: 600; }}

        pre {{
            margin: 0;
            white-space: pre-wrap;
            word-break: break-word;
            max-height: 48vh;
            overflow: auto;
            background: #0b1220d8;
            border: 1px solid #1f2937;
            border-radius: 10px;
            padding: 12px;
        }}

        .hint {{ font-size: 0.9rem; }}
    </style>
</head>
<body>
    <div class=\"container\">
        <div class=\"panel header\">
            <div>
                <h1>Debuggame-Zustände</h1>
                <div class=\"muted\" id=\"stateDir\"></div>
            </div>
            <div class=\"controls\">
                <button id=\"prevBtn\" type=\"button\">Vorheriger</button>
                <button id=\"nextBtn\" type=\"button\">Nächster</button>
            </div>
        </div>

        <div class=\"panel\">
            <div class=\"header\">
                <strong id=\"stateIndicator\">Zustand</strong>
                <span class=\"hint muted\">Tasten: &lt;- vorheriger, -&gt; nächster</span>
            </div>
            <div class=\"grid\" style=\"margin-top: 10px\">
                <div class=\"kv\"><div class=\"k\">Datei</div><div class=\"v\" id=\"fileName\"></div></div>
                <div class=\"kv\"><div class=\"k\">Aktion</div><div class=\"v\" id=\"action\"></div></div>
                <div class=\"kv\"><div class=\"k\">Ausgeführte Aktion</div><div class=\"v\" id=\"executedAction\"></div></div>
                <div class=\"kv\"><div class=\"k\">Ausgeführt von</div><div class=\"v\" id=\"executedBy\"></div></div>
                <div class=\"kv\"><div class=\"k\">Runde</div><div class=\"v\" id=\"round\"></div></div>
                <div class=\"kv\"><div class=\"k\">Aktueller Spieler</div><div class=\"v\" id=\"currentPlayer\"></div></div>
            </div>
        </div>

        <div class=\"panel\">
            <strong>Aktionsdetails</strong>
            <div id=\"actionDetailsContainer\"></div>
        </div>

        <div class=\"panel\">
            <strong id=\"agentScoresTitle\">Agent-Auswahl</strong>
            <div id=\"agentScoresContainer\"></div>
        </div>

        <div class=\"panel\">
            <strong>Änderungen seit dem vorherigen State</strong>
            <div id=\"diffContainer\"></div>
        </div>

        <div class=\"panel\">
            <strong>Roh-JSON</strong>
            <pre id=\"rawJson\"></pre>
        </div>
    </div>

    <script>
        const stateDir = {json.dumps(str(state_dir), ensure_ascii=False)};
        const entries = {payload};
        let index = 0;

        const stateDirEl = document.getElementById("stateDir");
        const stateIndicatorEl = document.getElementById("stateIndicator");
        const fileNameEl = document.getElementById("fileName");
        const actionEl = document.getElementById("action");
        const executedActionEl = document.getElementById("executedAction");
        const actionDetailsContainerEl = document.getElementById("actionDetailsContainer");
        const agentScoresContainerEl = document.getElementById("agentScoresContainer");
        const agentScoresTitleEl = document.getElementById("agentScoresTitle");
        const executedByEl = document.getElementById("executedBy");
        const roundEl = document.getElementById("round");
        const currentPlayerEl = document.getElementById("currentPlayer");
        const diffContainerEl = document.getElementById("diffContainer");
        const rawJsonEl = document.getElementById("rawJson");
        const prevBtn = document.getElementById("prevBtn");
        const nextBtn = document.getElementById("nextBtn");

        function text(value, fallback = "(nicht im JSON vorhanden)") {{
            if (value === null || value === undefined || value === "") return fallback;
            return String(value);
        }}

        function renderDiffs(entry) {{
            diffContainerEl.innerHTML = "";
            if (entry.isInitial) {{
                const line = document.createElement("p");
                line.className = "diff-empty";
                line.textContent = "Dies ist der Initial-State.";
                diffContainerEl.appendChild(line);
                return;
            }}

            if (!entry.diffs || entry.diffs.length === 0) {{
                const line = document.createElement("p");
                line.className = "diff-empty";
                line.textContent = "Keine Änderungen erkannt.";
                diffContainerEl.appendChild(line);
                return;
            }}

            const list = document.createElement("ul");
            list.className = "diff-list";
            for (const diff of entry.diffs) {{
                const item = document.createElement("li");
                item.textContent = diff;
                list.appendChild(item);
            }}
            diffContainerEl.appendChild(list);
        }}

        function renderActionDetails(entry) {{
            actionDetailsContainerEl.innerHTML = "";

            const blocks = entry.actionDetailsBlocks || [];
            if (!blocks.length) {{
                const line = document.createElement("p");
                line.className = "diff-empty";
                line.textContent = "Keine Aktionsdetails vorhanden.";
                actionDetailsContainerEl.appendChild(line);
                return;
            }}

            for (const block of blocks) {{
                const wrapper = document.createElement("div");
                wrapper.className = "action-block";

                const title = document.createElement("p");
                title.className = "action-title";
                title.textContent = block.title || "Details";
                wrapper.appendChild(title);

                const list = document.createElement("div");
                list.className = "action-list";
                for (const itemText of (block.items || [])) {{
                    const paragraph = document.createElement("p");
                    paragraph.className = "action-paragraph";
                    paragraph.textContent = itemText;
                    list.appendChild(paragraph);
                }}
                wrapper.appendChild(list);
                actionDetailsContainerEl.appendChild(wrapper);
            }}
        }}

        function renderAgentScores(entry) {{
            agentScoresContainerEl.innerHTML = "";
            const strategyName = text(entry.agentStrategyName, "unbekannt");
            agentScoresTitleEl.textContent = `Agent-Auswahl (${{strategyName}})`;
            const scores = entry.agentMainActionScores || [];

            if (!scores.length) {{
                const line = document.createElement("p");
                line.className = "diff-empty";
                line.textContent = "Keine Agent-Scores im JSON vorhanden.";
                agentScoresContainerEl.appendChild(line);
                return;
            }}

            const details = document.createElement("details");
            details.className = "collapsible";

            const summary = document.createElement("summary");
            summary.textContent = "Ein-/Ausklappen";
            details.appendChild(summary);

            const list = document.createElement("div");
            list.className = "agent-scores";

            for (const score of scores) {{
                const row = document.createElement("div");
                row.className = "agent-score-row";

                const main = document.createElement("div");
                main.className = "agent-score-main";
                main.textContent = String(score.mainAction || "(unbekannt)");
                row.appendChild(main);

                const meta = document.createElement("div");
                const scoreValue = typeof score.score === "number"
                    ? score.score.toFixed(4)
                    : String(score.score ?? "-");
                const selectedText = score.selected ? "Gewählt" : "Nicht gewählt";
                meta.className = "agent-score-meta";
                meta.textContent = `Score: ${{scoreValue}} | ${{selectedText}}`;
                if (score.selected) {{
                    meta.classList.add("agent-score-selected");
                }}
                row.appendChild(meta);

                if (score.bestActionVariant) {{
                    const variant = document.createElement("div");
                    variant.className = "agent-score-meta";
                    variant.textContent = `Beste Variante: ${{score.bestActionVariant}}`;
                    row.appendChild(variant);
                }}

                list.appendChild(row);
            }}

            details.appendChild(list);
            agentScoresContainerEl.appendChild(details);
        }}

        function render() {{
            if (!entries.length) return;

            const entry = entries[index];
            stateDirEl.textContent = stateDir;
            stateIndicatorEl.textContent = `Zustand ${{entry.index}}/${{entries.length}}`;
            fileNameEl.textContent = text(entry.fileName, "-");
            actionEl.textContent = text(entry.actionLabel, "-");
            executedActionEl.textContent = text(entry.executedActionReadable);
            executedByEl.textContent = text(entry.executedByPlayer ? String(entry.executedByPlayer).replace("Player ", "Spieler ") : entry.executedByPlayer);
            roundEl.textContent = text(entry.round, "-");
            currentPlayerEl.textContent = text(entry.currentPlayer, "-");
            rawJsonEl.textContent = JSON.stringify(entry.state, null, 2);
            renderActionDetails(entry);
            renderAgentScores(entry);
            renderDiffs(entry);

            prevBtn.disabled = index <= 0;
            nextBtn.disabled = index >= entries.length - 1;
        }}

        function goPrevious() {{
            if (index > 0) {{
                index -= 1;
                render();
            }}
        }}

        function goNext() {{
            if (index < entries.length - 1) {{
                index += 1;
                render();
            }}
        }}

        prevBtn.addEventListener("click", goPrevious);
        nextBtn.addEventListener("click", goNext);
        document.addEventListener("keydown", (event) => {{
            if (event.key === "ArrowLeft") {{
                event.preventDefault();
                goPrevious();
            }}
            if (event.key === "ArrowRight") {{
                event.preventDefault();
                goNext();
            }}
        }});

        render();
    </script>
</body>
</html>
"""


def open_in_firefox(html_path: Path) -> bool:
    candidates = []
    if os.name == "nt":
        candidates.extend(
            [
                Path("C:/Program Files/Mozilla Firefox/firefox.exe"),
                Path("C:/Program Files (x86)/Mozilla Firefox/firefox.exe"),
            ]
        )

    firefox_from_path = shutil.which("firefox")
    if firefox_from_path:
        candidates.append(Path(firefox_from_path))

    for candidate in candidates:
        if candidate.exists():
            subprocess.Popen([str(candidate), str(html_path)], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            return True

    return False


def browse_in_web(state_dir: Path, files: list[Path], prefer_firefox: bool, output_path: str | None) -> int:
    entries = build_state_entries(files)
    html_output = (
        Path(output_path)
        if output_path
        else Path(tempfile.gettempdir()) / f"anno1800-debuggame-{state_dir.name}.html"
    )

    html_output.parent.mkdir(parents=True, exist_ok=True)
    html_output.write_text(render_web_view(state_dir, entries), encoding="utf-8")

    opened = False
    if prefer_firefox:
        opened = open_in_firefox(html_output)

    if not opened:
        opened = webbrowser.open(html_output.resolve().as_uri())

    print(f"Web-Ansicht erstellt: {html_output}")
    if prefer_firefox and not opened:
        print("Firefox konnte nicht automatisch gestartet werden. Bitte Datei manuell oeffnen.")

    return 0


def main() -> int:
    args = parse_args()
    state_dir = Path(args.dir) if args.dir else find_latest_debuggame_dir()

    if state_dir is None:
        print("Kein Debuggame-Ordner gefunden. Starte zuerst .\\Debugging\\debug-game.ps1", file=sys.stderr)
        return 1

    if not state_dir.exists():
        print(f"Verzeichnis nicht gefunden: {state_dir}", file=sys.stderr)
        return 1

    files = sorted(state_dir.glob("*.json"), key=sort_key)
    if not files:
        print(f"Keine JSON-Dateien in {state_dir} gefunden.", file=sys.stderr)
        return 1

    if args.web:
        return browse_in_web(
            state_dir=state_dir,
            files=files,
            prefer_firefox=args.firefox,
            output_path=args.out,
        )

    previous_state: dict[str, Any] | None = None

    print()
    print(f"Debuggame-States: {state_dir}")
    print("Taste drücken: beliebige Taste = nächster State, Q = Ende")
    print()

    for index, path in enumerate(files, start=1):
        state = load_state(path)

        clear_screen()
        print(f"Zustand {index}/{len(files)}")
        print(f"Datei: {path.name}")
        print(f"Aktion: {with_umlauts(action_label(path))}")
        executed_action = state.get("executedAction")
        executed_action_readable = with_umlauts(
            action_with_amount(executed_action, state, previous_state)
        )
        executed_by_player = state.get("executedByPlayer")
        action_details = state.get("executedActionDetails") or build_action_details(
            executed_action,
            state,
            previous_state,
        )
        action_details = with_umlauts(str(action_details)) if action_details else None
        action_detail_blocks = build_action_details_blocks(
            executed_action,
            state,
            previous_state,
            action_details,
        )
        if executed_action:
            print(f"Ausgeführte Aktion: {executed_action_readable}")
        else:
            print("Ausgeführte Aktion: (nicht im JSON vorhanden)")

        print("Aktionsdetails:")
        if action_detail_blocks:
            for block in action_detail_blocks:
                print(f"  {block.get('title', 'Details')}")
                items = block.get("items", [])
                if items:
                    for item in items:
                        print(f"    {item}")
                    print()
                else:
                    print("    (keine Details)")
        elif action_details:
            print(f"  {action_details}")
        else:
            print("  (nicht im JSON vorhanden)")

        if executed_by_player:
            print(f"Ausgeführt von: {format_player_reference(executed_by_player)}")
        else:
            print("Ausgeführt von: (nicht im JSON vorhanden)")
        print(f"Runde: {state.get('round')}")
        print(f"Aktueller Spieler: {format_player_reference(state.get('currentPlayer'))}")
        print()

        if previous_state is None:
            print("Dies ist der Initial-State.")
        else:
            print("Änderungen seit dem vorherigen State:")
            diffs = [with_umlauts(line) or line for line in diff_values(state, previous_state, action_details=action_details)]
            diffs = enrich_working_diffs(diffs, action_details)
            if diffs:
                for line in diffs:
                    for part in str(line).splitlines() or [""]:
                        print(f"  {part}")
            else:
                print("  Keine Änderungen erkannt.")

        print()
        print("Beliebige Taste = weiter | Q = beenden")

        key = read_key()
        if key.lower() == "q":
            break

        previous_state = state

    print()
    print("Fertig.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

