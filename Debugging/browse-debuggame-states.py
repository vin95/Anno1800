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

GOOD_ICON_NAMES: dict[str, str] = {
    "beer": "beer.png",
    "big berta": "big_berta.png",
    "brass": "brass.png",
    "bread": "bread.png",
    "bricks": "bricks.png",
    "cacao": "cacao.png",
    "canned meat": "canned_meat.png",
    "cannons": "cannons.png",
    "cars": "cars.png",
    "champagne": "champagne.png",
    "chocolate": "chocolate.png",
    "cigars": "cigars.png",
    "coal": "coal.png",
    "coats": "coats.png",
    "coffee": "coffee.png",
    "coffee beans": "coffee_beans.png",
    "cotton": "cotton.png",
    "cotton fabric": "cotton_fabric.png",
    "dynamite": "dynamite.png",
    "glass": "glass.png",
    "glasses": "glasses.png",
    "gold": "gold.png",
    "goods": "goods.png",
    "grain": "grain.png",
    "gramophones": "gramophones.png",
    "highbikes": "highbikes.png",
    "light bulbs": "light_bulbs.png",
    "pigs": "pigs.png",
    "planks": "planks.png",
    "pocketwatches": "pocketwatches.png",
    "potatoes": "potatoes.png",
    "rubber": "rubber.png",
    "rum": "rum.png",
    "sails": "sails.png",
    "sausages": "sausages.png",
    "sewing machines": "sewing_machines.png",
    "snaps": "snaps.png",
    "soap": "soap.png",
    "steam gears": "steam_gears.png",
    "steel bars": "steel_bars.png",
    "sugarcane": "sugarcane.png",
    "tobacco": "tobacco.png",
    "windows": "windows.png",
    "wool": "wool.png",
    "work clothes": "work_clothes.png",
}

RESOURCE_ICON_NAMES: dict[str, str] = {
    "gold": "gold.png",
    "tradechips": "tradechip.png",
    "explorerchips": "explorerchip.png",
    "residentcards": "residentcard_lv_2.png",
}

WORKFORCE_ICON_NAMES: dict[str, str] = {
    "level1": "workforce_level_1.png",
    "level2": "workforce_level_2.png",
    "level3": "workforce_level_3.png",
    "level4": "workforce_level_4.png",
    "level5": "workforce_level_5.png",
}

RESIDENT_CARD_ICON_NAMES: dict[int, str] = {
    1: "residentcard_lv_1.png",
    2: "residentcard_lv_2.png",
    3: "residentcard_lv_3.png",
    4: "residentcard_lv_4.png",
    5: "residentcard_lv_5.png",
}

WORKFORCE_LABEL_ICON_NAMES: dict[str, str] = {
    "farmer": WORKFORCE_ICON_NAMES["level1"],
    "workers": WORKFORCE_ICON_NAMES["level2"],
    "worker": WORKFORCE_ICON_NAMES["level2"],
    "artisans": WORKFORCE_ICON_NAMES["level3"],
    "artisan": WORKFORCE_ICON_NAMES["level3"],
    "engineers": WORKFORCE_ICON_NAMES["level4"],
    "engineer": WORKFORCE_ICON_NAMES["level4"],
    "investors": WORKFORCE_ICON_NAMES["level5"],
    "investor": WORKFORCE_ICON_NAMES["level5"],
}

FACTORY_COLOR_SQUARE_NAMES: dict[str, str] = {
    "red": "red_square",
    "blue": "blue_square",
    "green": "green_square",
    "yellow": "yellow_square",
    "orange": "orange_square",
    "purple": "purple_square",
    "black": "black_square",
    "white": "white_square",
}

COUNTED_GOOD_PATTERN = re.compile(
    rf"(\b\d+x\s+)({'|'.join(sorted((re.escape(name) for name in GOOD_ICON_NAMES), key=len, reverse=True))})\b",
    flags=re.IGNORECASE,
)

GOOD_ICON_TOKEN_PREFIX = "goodicon_"
GOOD_ICON_TOKENS: dict[str, str] = {
    f"{GOOD_ICON_TOKEN_PREFIX}{key.replace(' ', '_')}": icon_name for key, icon_name in GOOD_ICON_NAMES.items()
}


def icon_name_for_good(value: str) -> str:
    key = value.strip().lower()
    return GOOD_ICON_NAMES.get(key, f"{key.replace(' ', '_')}.png")

def resident_card_icon_name(level: Any) -> str:
    if isinstance(level, int):
        return RESIDENT_CARD_ICON_NAMES.get(level, "residentcard_lv_2.png")
    return "residentcard_lv_2.png"


def format_factory_name(value: str) -> str:
    text_value = value.strip()
    if not text_value:
        return text_value

    parts = text_value.rsplit(" ", 1)
    if len(parts) == 2:
        base_name, color_name = parts[0].strip(), parts[1].strip().lower()
        if base_name and color_name in FACTORY_COLOR_SQUARE_NAMES:
            return f"{base_name} {FACTORY_COLOR_SQUARE_NAMES[color_name]}"

    return text_value


def replace_text_tokens_with_icons(text: str, mapping: dict[str, str]) -> str:
    if not text or not mapping:
        return text

    pattern = re.compile(
        "|".join(sorted((re.escape(name) for name in mapping), key=len, reverse=True)),
        flags=re.IGNORECASE,
    )

    def replacer(match: re.Match[str]) -> str:
        token = match.group(0).lower()
        return mapping.get(token, match.group(0))

    return pattern.sub(replacer, text)


def replace_counted_goods_with_icons(text: str) -> str:
    if not text:
        return text

    def replacer(match: re.Match[str]) -> str:
        prefix = match.group(1)
        good_name = match.group(2)
        token_name = f"{GOOD_ICON_TOKEN_PREFIX}{good_name.strip().lower().replace(' ', '_')}"
        return f"{prefix}{token_name}"

    return COUNTED_GOOD_PATTERN.sub(replacer, text)


def iconize_output_text(text: str | None) -> str | None:
    if text is None:
        return None

    normalized = str(text)

    normalized = replace_counted_goods_with_icons(normalized)

    for resource_name, icon_name in RESOURCE_ICON_NAMES.items():
        normalized = re.sub(
            rf"(?<![A-Za-z0-9_]){resource_name}(?![A-Za-z0-9_]|\.png)",
            f"{resource_name} {icon_name}",
            normalized,
            flags=re.IGNORECASE,
        )

    normalized = replace_text_tokens_with_icons(normalized, WORKFORCE_LABEL_ICON_NAMES)

    return normalized


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
    parser.add_argument(
        "--migrate-json",
        action="store_true",
        help="One-time migration: rewrite legacy image tokens in debug JSON files to folder-based paths",
    )
    parser.add_argument(
        "--migrate-dir",
        default=None,
        help="Root directory for --migrate-json (default: game-states)",
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
    return (
        prefix.startswith("boardState.populationPool.")
        or prefix == "timestamp"
        or prefix.startswith("agentMainActionScores")
        or prefix.startswith("agentStrategyName")
        or prefix.startswith("executedActionDetails")
        or ".residents.byStatus." in prefix
        or ".residents.byStatusByLevel." in prefix
    )


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
            label = label.replace(marker, f" {level_label}")

    for resource_name, icon_name in RESOURCE_ICON_NAMES.items():
        if resource_name in label:
            label = re.sub(
                rf"(?<![A-Za-z]){resource_name}(?![A-Za-z])",
                f"{resource_name} {icon_name}",
                label,
                flags=re.IGNORECASE,
            )

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
    return ", ".join(
        f"{count}x {icon_name_for_good(good)}" for good, count in sorted(counts.items())
    )


def summarize_factories(values: list[str]) -> str:
    if not values:
        return ""

    counts: dict[str, int] = {}
    order: list[str] = []
    for value in values:
        formatted_value = format_factory_name(value)
        if formatted_value not in counts:
            order.append(formatted_value)
        counts[formatted_value] = counts.get(formatted_value, 0) + 1

    parts: list[str] = []
    for value in order:
        count = counts[value]
        parts.append(f"{count}x {value}" if count > 1 else value)

    return ", ".join(parts)


def summarize_resource_chips(count: int, icon_name: str) -> str:
    return f"{count}x {icon_name}"


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
        "Schiffe baün": "Schiffe bauen",
        "schiffe baün": "schiffe bauen",
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

    # Render production factories as "FactoryName color_square" and remove resident level text.
    production_pattern = re.compile(r"\[produziert\s*\(([^,\]]+),\s*Bewohnerstufe\s*\d+\)\]")

    def production_replacer(match: re.Match[str]) -> str:
        factory_text = format_factory_name(match.group(1).strip())
        return f"(produziert in {factory_text})"

    trade_pattern = re.compile(r"\[gehandelt\s*\(\s*Spieler\s*(\d+)\s*,\s*Kosten\s*(\d+)\s*Chip(?:s)?\s*\)\]")

    def trade_replacer(match: re.Match[str]) -> str:
        player = match.group(1)
        cost = match.group(2)
        return f"gehandelt mit Spieler {player}, Kosten {cost} tradechip.png, {cost}x Gold => Spieler {player}"

    cleaned = production_pattern.sub(production_replacer, cleaned)
    cleaned = trade_pattern.sub(trade_replacer, cleaned)
    return cleaned


def summarize_status_by_level(status_map: dict[str, Any], status_key: str) -> str | None:
    by_level = status_map.get(status_key)
    if not isinstance(by_level, dict):
        return None

    parts: list[str] = []
    for level_key in ("level1", "level2", "level3", "level4", "level5"):
        value = by_level.get(level_key)
        if isinstance(value, int) and value > 0:
            icon_name = WORKFORCE_ICON_NAMES[level_key]
            parts.append(f"{value}x {icon_name}")

    if not parts:
        return None

    return f"Residents.{status_key}: {', '.join(parts)}"


def build_resident_status_level_diffs(
    current_state: dict[str, Any],
    previous_state: dict[str, Any] | None,
) -> list[str]:
    if previous_state is None:
        return []

    cur_players = current_state.get("players")
    prev_players = previous_state.get("players")
    if not isinstance(cur_players, list) or not isinstance(prev_players, list):
        return []

    lines: list[str] = []
    for index, cur_player in enumerate(cur_players):
        if not isinstance(cur_player, dict):
            continue
        if index >= len(prev_players) or not isinstance(prev_players[index], dict):
            continue

        prev_player = prev_players[index]
        cur_residents = cur_player.get("residents") if isinstance(cur_player.get("residents"), dict) else {}
        prev_residents = prev_player.get("residents") if isinstance(prev_player.get("residents"), dict) else {}
        cur_status_map = cur_residents.get("byStatusByLevel") if isinstance(cur_residents.get("byStatusByLevel"), dict) else {}
        prev_status_map = prev_residents.get("byStatusByLevel") if isinstance(prev_residents.get("byStatusByLevel"), dict) else {}

        if not cur_status_map or not prev_status_map:
            continue

        player_label = f"Spieler {index + 1}"
        for status_key in ("fit", "working", "exhausted"):
            cur_summary = summarize_status_by_level(cur_status_map, status_key)
            prev_summary = summarize_status_by_level(prev_status_map, status_key)
            if cur_summary != prev_summary and cur_summary is not None:
                lines.append(f"{player_label} {cur_summary}")

    return lines


def get_status_by_level_for_player(player: dict[str, Any]) -> dict[str, dict[str, int]]:
    residents = player.get("residents") if isinstance(player, dict) else None
    if not isinstance(residents, dict):
        return {}

    by_status_by_level = residents.get("byStatusByLevel")
    if not isinstance(by_status_by_level, dict):
        return {}

    extracted: dict[str, dict[str, int]] = {}
    for status_key in ("fit", "working", "exhausted"):
        per_level = by_status_by_level.get(status_key)
        if not isinstance(per_level, dict):
            continue

        level_map: dict[str, int] = {}
        for level_key in ("level1", "level2", "level3", "level4", "level5"):
            value = per_level.get(level_key)
            if isinstance(value, int):
                level_map[level_key] = value

        if level_map:
            extracted[status_key] = level_map

    return extracted


def calculate_status_level_deltas_for_player(
    current_state: dict[str, Any],
    previous_state: dict[str, Any] | None,
    player_index: int | None,
) -> dict[str, dict[str, int]]:
    if previous_state is None or player_index is None:
        return {}

    cur_players = current_state.get("players")
    prev_players = previous_state.get("players")
    if not isinstance(cur_players, list) or not isinstance(prev_players, list):
        return {}
    if player_index < 0 or player_index >= len(cur_players) or player_index >= len(prev_players):
        return {}
    if not isinstance(cur_players[player_index], dict) or not isinstance(prev_players[player_index], dict):
        return {}

    current_map = get_status_by_level_for_player(cur_players[player_index])
    previous_map = get_status_by_level_for_player(prev_players[player_index])
    if not current_map or not previous_map:
        return {}

    deltas: dict[str, dict[str, int]] = {}
    for status_key in ("fit", "working", "exhausted"):
        current_levels = current_map.get(status_key, {})
        previous_levels = previous_map.get(status_key, {})
        status_deltas: dict[str, int] = {}

        for level_key in ("level1", "level2", "level3", "level4", "level5"):
            cur_value = current_levels.get(level_key)
            prev_value = previous_levels.get(level_key)
            if isinstance(cur_value, int) and isinstance(prev_value, int):
                delta = cur_value - prev_value
                if delta != 0:
                    status_deltas[level_key] = delta

        if status_deltas:
            deltas[status_key] = status_deltas

    return deltas


def distribute_delta_across_buckets(delta: int, bucket_count: int) -> list[int]:
    if bucket_count <= 1:
        return [delta]
    if delta == 0:
        return [0 for _ in range(bucket_count)]

    sign = 1 if delta > 0 else -1
    magnitude = abs(delta)
    base = magnitude // bucket_count
    remainder = magnitude % bucket_count
    distributed = [sign * base for _ in range(bucket_count)]
    for i in range(remainder):
        distributed[i] += sign
    return distributed


def distribute_status_level_deltas(
    deltas: dict[str, dict[str, int]],
    bucket_count: int,
) -> list[dict[str, dict[str, int]]]:
    if bucket_count <= 0:
        return []

    buckets: list[dict[str, dict[str, int]]] = [
        {"fit": {}, "working": {}, "exhausted": {}} for _ in range(bucket_count)
    ]

    for status_key, levels in deltas.items():
        for level_key, delta in levels.items():
            distributed = distribute_delta_across_buckets(delta, bucket_count)
            for i, value in enumerate(distributed):
                if value != 0:
                    buckets[i][status_key][level_key] = value

    return buckets


def format_status_delta_lines(status_deltas: dict[str, dict[str, int]]) -> list[str]:
    lines: list[str] = []

    for status_key in ("fit", "working", "exhausted"):
        level_deltas = status_deltas.get(status_key, {})
        if not isinstance(level_deltas, dict) or not level_deltas:
            continue

        parts: list[str] = []
        for level_key in ("level1", "level2", "level3", "level4", "level5"):
            delta = level_deltas.get(level_key)
            if isinstance(delta, int) and delta != 0:
                sign = "+" if delta > 0 else ""
                parts.append(f"{sign}{delta}x {WORKFORCE_ICON_NAMES[level_key]}")

        if parts:
            lines.append(f"Residents.{status_key}: {', '.join(parts)}")

    return lines


def workforce_label_for_level(level: int) -> str:
    labels = {
        1: "Farmer",
        2: "Worker",
        3: "Artisan",
        4: "Engineer",
        5: "Investor",
    }
    return labels.get(level, f"Level {level}")


def extract_working_changes_from_raw_items(raw_items: list[str]) -> list[str]:
    counts: dict[int, int] = {}
    for item in raw_items:
        match = re.search(r"Bewohnerstufe\s*(\d+)", item)
        if not match:
            continue
        level = int(match.group(1))
        if 1 <= level <= 5:
            counts[level] = counts.get(level, 0) + 1

    lines: list[str] = []
    for level in (1, 2, 3, 4, 5):
        count = counts.get(level, 0)
        if count <= 0:
            continue
        label = workforce_label_for_level(level)
        if count == 1:
            lines.append(f"1x {label} is Working now")
        else:
            lines.append(f"{count}x {label} are Working now")

    return lines


def get_by_level_for_player(player: dict[str, Any]) -> dict[int, int]:
    residents = player.get("residents") if isinstance(player, dict) else None
    if not isinstance(residents, dict):
        return {}

    by_level = residents.get("byLevel")
    if not isinstance(by_level, dict):
        return {}

    extracted: dict[int, int] = {}
    for level in (1, 2, 3, 4, 5):
        value = by_level.get(f"level{level}")
        if isinstance(value, int):
            extracted[level] = value
    return extracted


def calculate_by_level_deltas_for_player(
    current_state: dict[str, Any],
    previous_state: dict[str, Any] | None,
    player_index: int | None,
) -> dict[int, int]:
    if previous_state is None or player_index is None:
        return {}

    cur_players = current_state.get("players")
    prev_players = previous_state.get("players")
    if not isinstance(cur_players, list) or not isinstance(prev_players, list):
        return {}
    if player_index < 0 or player_index >= len(cur_players) or player_index >= len(prev_players):
        return {}
    if not isinstance(cur_players[player_index], dict) or not isinstance(prev_players[player_index], dict):
        return {}

    cur_levels = get_by_level_for_player(cur_players[player_index])
    prev_levels = get_by_level_for_player(prev_players[player_index])
    if not cur_levels or not prev_levels:
        return {}

    deltas: dict[int, int] = {}
    for level in (1, 2, 3, 4, 5):
        delta = cur_levels.get(level, 0) - prev_levels.get(level, 0)
        if delta != 0:
            deltas[level] = delta
    return deltas


def build_action_result_change_blocks(
    current_state: dict[str, Any],
    previous_state: dict[str, Any] | None,
    player_index: int | None,
    block_count: int,
) -> list[list[str]]:
    if block_count <= 0:
        return []

    blocks: list[list[str]] = [[] for _ in range(block_count)]
    deltas = calculate_by_level_deltas_for_player(current_state, previous_state, player_index)
    if not deltas:
        return blocks

    remaining = {level: deltas.get(level, 0) for level in (1, 2, 3, 4, 5)}

    # Convert paired level deltas to explicit upgrade transitions.
    for from_level in (1, 2, 3, 4):
        to_level = from_level + 1
        moved = min(max(0, -remaining.get(from_level, 0)), max(0, remaining.get(to_level, 0)))
        if moved <= 0:
            continue

        distribution = distribute_delta_across_buckets(moved, block_count)
        for i, count in enumerate(distribution):
            if count <= 0:
                continue
            from_label = workforce_label_for_level(from_level)
            to_label = workforce_label_for_level(to_level)
            blocks[i].append(f"{count}x {from_label} => {count}x {to_label}")

        remaining[from_level] += moved
        remaining[to_level] -= moved

    # Positive remainder means newly settled residents.
    for level in (1, 2, 3, 4, 5):
        settled_count = remaining.get(level, 0)
        if settled_count <= 0:
            continue

        distribution = distribute_delta_across_buckets(settled_count, block_count)
        for i, count in enumerate(distribution):
            if count <= 0:
                continue
            label = workforce_label_for_level(level)
            if count == 1:
                blocks[i].append(f"1 new {label} has settled")
            else:
                blocks[i].append(f"{count} new {label}s have settled")

    return blocks


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
        segments.append(summarize_resource_chips(trade_chips, RESOURCE_ICON_NAMES["tradechips"]))

    if is_build_action(action_name) and explorer_chips > 0:
        segments.append(summarize_resource_chips(explorer_chips, RESOURCE_ICON_NAMES["explorerchips"]))

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
    if raw_action is None and not action_details:
        return []

    action_name = str(raw_action).split("[", 1)[0] if raw_action is not None else ""
    details_text = str(action_details) if action_details else ""
    goods_entries, chip_line = parse_action_details_entries(details_text)
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
    grouped_raw_entries = split_entries_evenly(goods_entries, block_count)
    if action_name == "UpgradeResident":
        prefix = "ResidentUpdate"
    elif action_name == "BuildShips":
        prefix = "Shipbuild"
    else:
        prefix = "Aktionsschritt"

    player_index = player_index_from_name(current_state, current_state.get("executedByPlayer"))
    action_result_change_blocks = build_action_result_change_blocks(
        current_state=current_state,
        previous_state=previous_state,
        player_index=player_index,
        block_count=block_count,
    )

    blocks: list[dict[str, Any]] = []
    action_label = action_with_amount(raw_action, current_state, previous_state)
    for i in range(block_count):
        items = grouped_entries[i] if i < len(grouped_entries) else []
        if action_label:
            items = [f"Aktion: {action_label}", *items]
        production_status_lines = extract_working_changes_from_raw_items(
            grouped_raw_entries[i] if i < len(grouped_raw_entries) else []
        )
        action_result_lines = action_result_change_blocks[i] if i < len(action_result_change_blocks) else []

        if production_status_lines:
            items = [*items, "Statusaenderungen Rohstoffproduktion:", *production_status_lines]
        if action_result_lines:
            items = [*items, "Statusaenderungen Aktionsergebnis:", *action_result_lines]
        title = f"{prefix}{i + 1}" if block_count > 1 else "Details"
        rendered_items: list[str] = []
        for item in items:
            display_item = with_umlauts(item) or item
            rendered_items.append(iconize_output_text(display_item) or display_item)

        blocks.append({"title": title, "items": rendered_items})

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
            good_icon = icon_name_for_good(good)
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
                        chips_text = summarize_resource_chips(used, RESOURCE_ICON_NAMES["tradechips"])
            return f"Handel: 1x {good_icon} (Tradechips: {chips_text}, mit Spieler {partner})"

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
        return f"Verbrauch fuer Aktion: {goods_text} (ueber Handel, Tradechips: {summarize_resource_chips(chips_used, RESOURCE_ICON_NAMES['tradechips'])}, Spieler: {format_player_reference(executed_by)})"

    if chips_used and chips_used > 0 and (action.startswith("Build") or action.startswith("UpgradeResident") or action.startswith("SettleResident")):
        return f"Verbrauch fuer Aktion: {summarize_resource_chips(chips_used, RESOURCE_ICON_NAMES['tradechips'])}"

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
                prev_details = iconize_output_text(str(previous).replace("; ", ";\n"))
                cur_details = iconize_output_text(str(current).replace("; ", ";\n"))
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


def enrich_working_diffs(
    diffs: list[str],
    action_details: str | None,
    current_state: dict[str, Any],
    previous_state: dict[str, Any] | None,
) -> list[str]:
    factory_summary = extract_production_factories(action_details)
    if not factory_summary:
        return diffs

    enriched: list[str] = []
    working_pattern = re.compile(r"\.residents\.byStatus\.working:\s*\d+\s*->\s*\d+")

    for line in diffs:
        enriched.append(line)
        if working_pattern.search(line):
            enriched.append(f"  Zugewiesene Fabriken: {factory_summary}")

    enriched.extend(build_resident_status_level_diffs(current_state, previous_state))
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


def build_picture_index() -> tuple[list[str], dict[str, str]]:
    picture_dir = (PROJECT_ROOT / "src" / "pictures").resolve()
    if not picture_dir.exists():
        return [], {}

    image_paths = sorted(
        [
            path
            for path in picture_dir.rglob("*")
            if path.is_file() and path.suffix.lower() in {".png", ".jpg", ".jpeg", ".webp"}
        ]
    )
    relative_paths = [path.relative_to(picture_dir).as_posix() for path in image_paths]

    preference_order = {"goods": 0, "residents": 1, "factories": 2, "ships": 3}
    by_name: dict[str, list[str]] = {}
    for rel_path in relative_paths:
        name = Path(rel_path).name.lower()
        by_name.setdefault(name, []).append(rel_path)

    resolved_by_name: dict[str, str] = {}
    for name, candidates in by_name.items():
        resolved = min(
            candidates,
            key=lambda value: (preference_order.get(Path(value).parts[0], 99), len(value), value),
        )
        resolved_by_name[name] = resolved

    alias_map = {
        "explorationchip.png": "explorerchip.png",
        "sausage.png": "sausages.png",
        "steelbars.png": "steel_bars.png",
        "gramophone.png": "gramophones.png",
        "gramphone.png": "gramophones.png",
        "highbike.png": "highbikes.png",
        "lightbulb.png": "light_bulbs.png",
        "lightbulbs.png": "light_bulbs.png",
        "sewingmachine.png": "sewing_machines.png",
        "pocketwatch.png": "pocketwatches.png",
        "coffeebeans.png": "coffee_beans.png",
        "workclothes.png": "work_clothes.png",
        "cottonfabric.png": "cotton_fabric.png",
        "bigberta.png": "big_berta.png",
    }

    icon_lookup: dict[str, str] = {}
    for rel_path in relative_paths:
        name = Path(rel_path).name.lower()
        icon_lookup[name] = resolved_by_name.get(name, rel_path)
        icon_lookup[rel_path.lower()] = rel_path

    for alias, target_name in alias_map.items():
        target_path = resolved_by_name.get(target_name)
        if target_path:
            icon_lookup[alias] = target_path

    default_resident_card = resolved_by_name.get("residentcard_lv_2.png")
    if default_resident_card:
        for level in (1, 3, 4, 6):
            icon_lookup[f"residentcard_lv_{level}.png"] = default_resident_card
            icon_lookup[f"residentcard_lv{level}.png"] = default_resident_card

    return relative_paths, icon_lookup


def migrate_image_tokens_in_text(raw_text: str, icon_lookup: dict[str, str]) -> tuple[str, int]:
    token_pattern = re.compile(r"(?<![A-Za-z0-9_])([A-Za-z0-9_\\/-]+\.(?:png|jpg|jpeg|webp))(?![A-Za-z0-9_])", flags=re.IGNORECASE)
    replacement_count = 0

    def replace_token(match: re.Match[str]) -> str:
        nonlocal replacement_count
        token = match.group(1)
        normalized = token.replace("\\", "/").lower()
        resolved = icon_lookup.get(normalized)
        if resolved is None:
            resolved = icon_lookup.get(Path(normalized).name)
        if not resolved or token == resolved:
            return token
        replacement_count += 1
        return resolved

    return token_pattern.sub(replace_token, raw_text), replacement_count


def migrate_debug_json_image_paths(state_root: Path) -> tuple[int, int]:
    if not state_root.exists():
        return 0, 0

    _, icon_lookup = build_picture_index()
    if not icon_lookup:
        return 0, 0

    changed_files = 0
    changed_tokens = 0
    for json_file in state_root.rglob("*.json"):
        try:
            original_text = json_file.read_text(encoding="utf-8")
        except OSError:
            continue

        migrated_text, replacements = migrate_image_tokens_in_text(original_text, icon_lookup)
        if replacements <= 0 or migrated_text == original_text:
            continue

        json_file.write_text(migrated_text, encoding="utf-8")
        changed_files += 1
        changed_tokens += replacements

    return changed_files, changed_tokens


def migrate_legacy_json_files(migrate_dir: str | None) -> None:
    state_root = Path(migrate_dir) if migrate_dir else DEFAULT_GAME_STATES_DIR
    changed_files, changed_tokens = migrate_debug_json_image_paths(state_root)
    print(f"Migration abgeschlossen: {changed_files} Dateien, {changed_tokens} ersetzte Bild-Tokens")


def run_terminal_interactively(sorted_paths: list[Path]) -> int:
    if not sorted_paths:
        print("Keine JSON-Dateien gefunden.", file=sys.stderr)
        return 1

    previous_state: dict[str, Any] | None = None
    total = len(sorted_paths)

    for index, path in enumerate(sorted_paths, start=1):
        state = load_state(path)
        clear_screen()
        print(f"State {index}/{total}")
        print(f"Datei: {path.name}")
        print(f"Aktion: {action_label(path)}")
        print(f"Runde: {state.get('round', '-')}")
        print(f"Aktueller Spieler: {format_player_reference(state.get('currentPlayer'))}")
        print()

        if previous_state is None:
            print("Dies ist der Initial-State.")
        else:
            print("Aenderungen seit dem vorherigen State:")
            diffs = diff_values(state, previous_state)
            if not diffs:
                print("  Keine Aenderungen erkannt.")
            else:
                for line in diffs:
                    print(f"  {line}")

        print()
        print("Beliebige Taste = weiter | Q = beenden")
        key = read_key()
        if key and key.lower() == "q":
            break

        previous_state = state

    return 0


def build_state_entries(files: list[Path]) -> list[dict[str, Any]]:
    previous_state: dict[str, Any] | None = None
    entries: list[dict[str, Any]] = []

    for index, path in enumerate(files, start=1):
        state = load_state(path)
        raw_action_details = state.get("executedActionDetails") or build_action_details(
            state.get("executedAction"),
            state,
            previous_state,
        )
        action_details = iconize_output_text(with_umlauts(str(raw_action_details)) if raw_action_details else None)
        diffs = [] if previous_state is None else [
            iconize_output_text(with_umlauts(line) or line) or line
            for line in diff_values(state, previous_state, action_details=action_details)
        ]
        diffs = enrich_working_diffs(diffs, action_details, state, previous_state)

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
                    raw_action_details if raw_action_details else action_details,
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


def render_web_view(state_dir: Any, entries: list[dict[str, Any]], html_output_path: Path | None = None) -> str:
    entries_json = json.dumps(entries, indent=None)

    payload = entries_json
    pictures_dir = (PROJECT_ROOT / "src" / "pictures").resolve()
    output_dir = (html_output_path.parent.resolve() if html_output_path else PROJECT_ROOT.resolve())
    try:
        icon_base_uri = pictures_dir.relative_to(output_dir).as_posix()
    except ValueError:
        icon_base_uri = pictures_dir.as_uri()
    try:
        main_board_image_uri = pictures_dir.relative_to(output_dir).as_posix() + "/mainboard.png"
    except ValueError:
        main_board_image_uri = (pictures_dir / "mainboard.png").as_uri()
    all_picture_paths, icon_lookup = build_picture_index()
    icon_file_names = sorted(
        {
            *GOOD_ICON_NAMES.values(),
            *RESOURCE_ICON_NAMES.values(),
            *WORKFORCE_ICON_NAMES.values(),
                    *RESIDENT_CARD_ICON_NAMES.values(),
            *(Path(path).name for path in all_picture_paths),
        }
    )
    action_image_candidates = {
        "BuildShipyard": ["ships/shipyard_lv1.png", "ships/shipyard_lv2.png", "ships/shipyard_lv3.png"],
        "BuildShips": ["ships/tradeship_lv1.png", "ships/explorership_lv1.png"],
        "SettleResident": ["residents/farmer_house.png", "residents/worker_house.png"],
        "UpgradeResident": ["residents/workforce_level_2.png", "residents/workforce_level_3.png"],
        "FulfillNeeds": ["goods/goods.png", "goods/tradechip.png"],
        "TradeGoods": ["goods/tradechip.png", "goods/gold.png"],
        "ProduceGoods": ["factories/warehouse_red.png", "goods/goods.png"],
        "DrawResidentCard": ["residents/residentcard_lv_2.png"],
        "DiscoverOldWorldIsland": ["ships/explorership_lv1.png"],
        "DiscoverNewWorldIsland": ["ships/explorership_lv1.png"],
        "Expedition": ["ships/explorership_lv2.png", "ships/explorership_lv1.png"],
        "Carneval": ["startplayer.png", "finishplayer.png"],
    }
    factory_layout = [
        {"label": "Reihe 1 — Basisfabriken + Werften", "factories": [
            {"id": "sawmill_blue",             "path": "factories/sawmill_blue_blueprint.png",             "name": "Sägewerk",              "initial": 0},
            {"id": "coal_mine_blue",           "path": "factories/coal_mine_blue_blueprint.png",           "name": "Kohlemine",             "initial": 0},
            {"id": "brick_factory_blue",       "path": "factories/brick_factory_blue_blueprint.png",       "name": "Ziegelei",              "initial": 0},
            {"id": "brewery_blue",             "path": "factories/brewery_blue_blueprint.png",             "name": "Brauerei",              "initial": 0},
            {"id": "bakery_blue",              "path": "factories/bakery_blue_blueprint.png",              "name": "Bäckerei",              "initial": 0},
            {"id": "brass_foundry",            "path": "factories/brass_foundry_red_blueprint.png",        "name": "Messinggießerei",       "initial": 0},
            {"id": "window_maker_red",         "path": "factories/window_maker_red_blueprint.png",         "name": "Fenstermacher",         "initial": 0},
            {"id": "champagne_cellar_red",     "path": "factories/champagne_cellar_red_blueprint.png",     "name": "Champagnerkeller",      "initial": 0},
            {"id": "shipyard_lv1",             "path": "ships/shipyard_lv1_blueprint.png",                 "name": "Werft L1",              "initial": 4},
            {"id": "shipyard_lv2",             "path": "ships/shipyard_lv2_blueprint.png",                 "name": "Werft L2",              "initial": 6},
            {"id": "shipyard_lv3",             "path": "ships/shipyard_lv3_blueprint.png",                 "name": "Werft L3",              "initial": 4}
        ]},
        {"label": "Reihe 2 — Verarbeitungsfabriken + Handelsschiffe", "factories": [
            {"id": "warehouse_blue",           "path": "factories/warehouse_blue_blueprint.png",           "name": "Lagerhaus",             "initial": 0},
            {"id": "steel_works_blue",         "path": "factories/steel_works_blue_blueprint.png",         "name": "Stahlwerk",             "initial": 0},
            {"id": "sailmakers_blue",          "path": "factories/sailmakers_blue_blueprint.png",          "name": "Segelmacher",           "initial": 0},
            {"id": "distillery_blue",          "path": "factories/distillery_blue_blueprint.png",          "name": "Destillerie",           "initial": 0},
            {"id": "glass_maker_blue",         "path": "factories/glass_maker_blue_blueprint.png",         "name": "Glasmacher",            "initial": 0},
            {"id": "spectacle_factory_red",    "path": "factories/spectacle_factory_red_blueprint.png",    "name": "Brillenfabrik",         "initial": 0},
            {"id": "clockmakers",              "path": "factories/clockmakers_red_blueprint.png",          "name": "Uhrmacher",             "initial": 0},
            {"id": "sewing_machine_factory_red",       "path": "factories/sewing_machine_red_blueprint.png",       "name": "Nähmaschine",           "initial": 0},
            {"id": "tradeship_lv1",            "path": "ships/tradeship_lv1_blueprint.png",               "name": "Handelsschiff L1",      "initial": 6},
            {"id": "tradeship_lv2",            "path": "ships/tradeship_lv2_blueprint.png",               "name": "Handelsschiff L2",      "initial": 6},
            {"id": "tradeship_lv3",            "path": "ships/tradeship_lv3_blueprint.png",               "name": "Handelsschiff L3",      "initial": 6},
        ]},
        {"label": "Reihe 3 — Fortgeschrittene Fabriken + Expeditionsschiffe", "factories": [
            {"id": "cotton_mill_red",          "path": "factories/cotton_mill_red_blueprint.png",          "name": "Baumwollmühle (rot)",   "initial": 0},
            {"id": "cotton_mill_blue",         "path": "factories/cotton_mill_blue_blue_blueprint.png",    "name": "Baumwollmühle (blau)",  "initial": 0},
            {"id": "coffee_roaster",         "path": "factories/coffee_roaster_red_blueprint.png",       "name": "Kaffeerösterei",        "initial": 0},
            {"id": "slaughterhouse_blue",      "path": "factories/slaughterhouse_blue_blueprint.png",      "name": "Schlachthaus",          "initial": 0},
            {"id": "soap_factory_blue",        "path": "factories/soap_factory_blue_blueprint.png",        "name": "Seifenfabrik",          "initial": 0},
            {"id": "fur_dealer_red",           "path": "factories/fur_dealer_red_blueprint.png",           "name": "Pelzhändler",           "initial": 0},
            {"id": "dynamite_factory_red",     "path": "factories/dynamite_factory_red_blueprint.png",     "name": "Dynamitfabrik",         "initial": 0},
            {"id": "cannons_factory_red",      "path": "factories/cannons_factory_red_blueprint.png",      "name": "Kanonenfabrik",         "initial": 0},
            {"id": "explorership_lv1",         "path": "ships/explorership_lv1_blueprint.png",             "name": "Expeditionsschiff L1",  "initial": 6},
            {"id": "explorership_lv2",         "path": "ships/explorership_lv2_blueprint.png",             "name": "Expeditionsschiff L2",  "initial": 6},
            {"id": "explorership_lv3",         "path": "ships/explorership_lv3_blueprint.png",             "name": "Expeditionsschiff L3",  "initial": 6},
        ]},
        {"label": "Reihe 4 — Luxusgüter & Neue Welt", "factories": [
            {"id": "rum_distillery_red",       "path": "factories/rum_distillery_red_blueprint.png",       "name": "Rum-Destillerie",       "initial": 0},
            {"id": "cigar_factory_red",        "path": "factories/cigar_factory_red_blueprint.png",        "name": "Zigarrenfabrik",        "initial": 0},
            {"id": "chocolate_factory_red",    "path": "factories/chocolate_factory_red_blueprint.png",    "name": "Schokoladenfabrik",     "initial": 0},
            {"id": "cannery_blue",             "path": "factories/cannery_blue_blueprint.png",             "name": "Konservenfabrik",       "initial": 0},
            {"id": "framework_knitters_blue",  "path": "factories/framework_knitters_blue_blueprint.png",  "name": "Strumpfwirker",         "initial": 0},
            {"id": "motor_assembly_purple",    "path": "factories/motor_assembly_purple_blueprint.png",    "name": "Motorenmontage",        "initial": 0},
            {"id": "car_factory_purple",       "path": "factories/car_factory_purple_blueprint.png",       "name": "Autofabrik",            "initial": 0},
            {"id": "bicycle_factory_purple",           "path": "factories/bicycle_purple_blueprint.png",           "name": "Fahrradfabrik",         "initial": 0},
            {"id": "light_bulb_factory_purple",         "path": "factories/lightbulb_purple_blueprint.png",         "name": "Glühbirnenfabrik",      "initial": 0},
            {"id": "gramophone_factory_purple",         "path": "factories/gramphone_purple_blueprint.png",         "name": "Grammophonfabrik",      "initial": 0},
            {"id": "heavy_weapons_factory_purple",         "path": "factories/heavy_weapons_purple_blueprint.png",         "name": "Schwerwaffenfabrik",    "initial": 0},
        ]},
    ]
    template_path = SCRIPT_DIR / "debug_viewer" / "viewer.html"
    assets_dir = SCRIPT_DIR / "debug_viewer"

    try:
        css_uri = (assets_dir / "viewer.css").resolve().relative_to(output_dir).as_posix()
    except ValueError:
        css_uri = (assets_dir / "viewer.css").resolve().as_uri()

    try:
        js_uri = (assets_dir / "viewer.js").resolve().relative_to(output_dir).as_posix()
    except ValueError:
        js_uri = (assets_dir / "viewer.js").resolve().as_uri()

    template_html = template_path.read_text(encoding="utf-8")
    template_html = template_html.replace("{{", "{").replace("}}", "}")

    init_script = "\n".join(
        [
            f"const stateDir = {json.dumps(str(state_dir), ensure_ascii=False)};",
            "const entries = JSON.parse(document.getElementById('gameStateData').textContent);",
            "const FACTORY_LAYOUT = JSON.parse(document.getElementById('factoryLayoutData').textContent);",
            f"const iconBaseUri = {json.dumps(icon_base_uri, ensure_ascii=False)};",
            f"const mainBoardImageUri = {json.dumps(main_board_image_uri, ensure_ascii=False)};",
            f"const iconPathByName = {json.dumps(icon_lookup, ensure_ascii=False)};",
            f"const allPicturePaths = {json.dumps(all_picture_paths, ensure_ascii=False)};",
            f"const iconFileNames = {json.dumps(icon_file_names, ensure_ascii=False)};",
            "const orderedIconFileNames = [...iconFileNames].sort((left, right) => right.length - left.length);",
            f"const goodIconTokens = {json.dumps(GOOD_ICON_TOKENS, ensure_ascii=False)};",
            "const orderedGoodIconTokens = Object.keys(goodIconTokens).sort((left, right) => right.length - left.length);",
            f"const goodIconsByName = {json.dumps(GOOD_ICON_NAMES, ensure_ascii=False)};",
            f"const actionImageCandidates = {json.dumps(action_image_candidates, ensure_ascii=False)};",
            "const smallResourceIconNames = new Set(['gold.png', 'tradechip.png', 'explorerchip.png']);",
            "const colorSquareTokens = {",
            "    red_square: 'red',",
            "    blue_square: 'blue',",
            "    green_square: 'green',",
            "    yellow_square: 'yellow',",
            "    orange_square: 'orange',",
            "    purple_square: 'purple',",
            "    black_square: 'black',",
            "    white_square: 'white',",
            "};",
            "const orderedColorSquareTokens = Object.keys(colorSquareTokens).sort((left, right) => right.length - left.length);",
            "let index = 0;",
            "let numPlayers = 2;",
            "const stateDirEl = document.getElementById('stateDir');",
            "const stateIndicatorEl = document.getElementById('stateIndicator');",
            "const fileNameEl = document.getElementById('fileName');",
            "const actionEl = document.getElementById('action');",
            "const executedActionEl = document.getElementById('executedAction');",
            "const actionDetailsContainerEl = document.getElementById('actionDetailsContainer');",
            "const agentScoresContainerEl = document.getElementById('agentScoresContainer');",
            "const agentScoresTitleEl = document.getElementById('agentScoresTitle');",
            "const executedByEl = document.getElementById('executedBy');",
            "const roundEl = document.getElementById('round');",
            "const currentPlayerEl = document.getElementById('currentPlayer');",
            "const diffContainerEl = document.getElementById('diffContainer');",
            "const cardOverviewContainerEl = document.getElementById('cardOverviewContainer');",
            "const rawJsonEl = document.getElementById('rawJson');",
            "const prevBtn = document.getElementById('prevBtn');",
            "const nextBtn = document.getElementById('nextBtn');",
            "const toggleDetailsBtn = document.getElementById('toggleDetailsBtn');",
            "const detailsLayer = document.getElementById('detailsLayer');",
            "const mainBoardEl = document.getElementById('mainBoard');",
            "const layerUpperLeftEl = document.getElementById('layerUpperLeft');",
            "const layerUpperRightEl = document.getElementById('layerUpperRight');",
            "const layerLowerLeftEl = document.getElementById('layerLowerLeft');",
            "const layerLowerRightEl = document.getElementById('layerLowerRight');",
        ]
    )

    template_html = re.sub(
        r"<title>.*?</title>",
        lambda _m: f"<title>Anno 1800 Debuggame States - {state_dir}</title>",
        template_html,
        flags=re.DOTALL,
    )
    template_html = re.sub(
        r'<link rel="stylesheet" href="[^"]*">',
        lambda _m: f'<link rel="stylesheet" href="{css_uri}">',
        template_html,
        count=1,
    )
    template_html = re.sub(
        r'<script type="application/json" id="gameStateData">.*?</script>',
        lambda _m: f'<script type="application/json" id="gameStateData">{payload}</script>',
        template_html,
        flags=re.DOTALL,
    )
    template_html = re.sub(
        r'<script type="application/json" id="factoryLayoutData">.*?</script>',
        lambda _m: f'<script type="application/json" id="factoryLayoutData">{json.dumps(factory_layout, ensure_ascii=False)}</script>',
        template_html,
        flags=re.DOTALL,
    )
    template_html = re.sub(
        r'<script type="text/x-python-template">.*?</script>',
        lambda _m: f"<script>\n{init_script}\n</script>",
        template_html,
        flags=re.DOTALL,
    )
    template_html = re.sub(
        r'<script src="[^"]*viewer\.js"></script>',
        lambda _m: f'<script src="{js_uri}"></script>',
        template_html,
        count=1,
    )

    return template_html


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
        else PROJECT_ROOT / f"anno1800-debuggame-{state_dir.name}.html"
    )

    html_output.parent.mkdir(parents=True, exist_ok=True)
    html_output.write_text(render_web_view(state_dir, entries, html_output_path=html_output), encoding="utf-8")

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

    if args.migrate_json:
        migrate_legacy_json_files(args.migrate_dir)
        return

    directory = args.dir
    if directory is None:
        latest_dir = find_latest_debuggame_dir()
        if latest_dir:
            directory = str(latest_dir)
        else:
            print("No game state directory found.", file=sys.stderr)
            sys.exit(1)

    state_dir = Path(directory)
    sorted_paths = sorted([p for p in state_dir.iterdir() if p.suffix == ".json"], key=sort_key)

    if args.web:
        entries = build_state_entries(sorted_paths)

        # Factory availability is calculated dynamically in JavaScript (calculateAvailableFactories)
        # based on player count and built factories from state.players[].tiles.islandTiles

        html_output = PROJECT_ROOT / f"anno1800-debuggame-{state_dir.name}.html"
        html_output.parent.mkdir(parents=True, exist_ok=True)
        html_output.write_text(render_web_view(state_dir, entries, html_output_path=html_output), encoding="utf-8")
        print(f"Web-Ansicht erstellt: {html_output}")

        opened = False
        if args.firefox:
            opened = open_in_firefox(html_output)
        if not opened:
            webbrowser.open(html_output.resolve().as_uri())
    else:
        run_terminal_interactively(sorted_paths)


if __name__ == "__main__":
    main()

