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


def render_web_view(state_dir: Path, entries: list[dict[str, Any]]) -> str:
    payload = json.dumps(entries, ensure_ascii=False)
    icon_base_uri = (PROJECT_ROOT / "src" / "pictures").resolve().as_uri()
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
            --bg-1: #1b2639;
            --panel: #162136dd;
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
                radial-gradient(1200px 700px at 10% -10%, #27344d 0%, transparent 60%),
                radial-gradient(900px 600px at 100% 0%, #1d5f73 0%, transparent 60%),
                linear-gradient(180deg, var(--bg-0), var(--bg-1));
            font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
            padding: 10px;
        }}

        .container {{
            width: 100%;
            max-width: none;
            margin: 0;
            display: grid;
            gap: 10px;
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

        .controls .toggle-details {{
            border-color: #6b7280;
            background: linear-gradient(180deg, #e2e8f0, #cbd5e1);
            color: #0f172a;
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

        .handcards-list .icon-inline img {{
            width: 2.38em;
            height: 2.38em;
        }}

        .action-paragraph {{
            margin: 0;
            line-height: 1.5;
        }}

        .icon-inline {{
            display: inline-flex;
            align-items: center;
            gap: 4px;
            vertical-align: middle;
            line-height: 1;
            white-space: nowrap;
        }}

        .icon-inline img {{
            width: 1.98em;
            height: 1.98em;
            object-fit: contain;
            vertical-align: middle;
            image-rendering: auto;
        }}

        .icon-inline img.resource-small {{
            width: 1.58em;
            height: 1.58em;
        }}

        .icon-inline img.residentcard-large {{
            width: 2.38em;
            height: 2.38em;
        }}

        .factory-color-square {{
            display: inline-block;
            width: 0.99em;
            height: 0.99em;
            border-radius: 0.25em;
            vertical-align: middle;
            border: 1px solid rgba(255, 255, 255, 0.35);
            box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.18);
        }}

        .factory-color-square.red {{ background: #d94848; }}
        .factory-color-square.blue {{ background: #4a89dc; }}
        .factory-color-square.green {{ background: #47b36b; }}
        .factory-color-square.yellow {{ background: #d4b13a; }}
        .factory-color-square.orange {{ background: #dd8a36; }}
        .factory-color-square.purple {{ background: #9b63d6; }}
        .factory-color-square.black {{ background: #4b5563; }}
        .factory-color-square.white {{ background: #f3f4f6; }}

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

        .visual-layer {{
            display: grid;
            gap: 10px;
        }}

        .game-layout {{
            display: grid;
            gap: 10px;
            grid-template-columns: 1fr 1fr;
            grid-template-rows: minmax(380px, auto) minmax(560px, auto) minmax(380px, auto);
            grid-template-areas:
                "ul ur"
                "center center"
                "ll lr";
        }}

        .board-slot {{
            border: 1px solid #334155;
            border-radius: 12px;
            background: rgba(7, 12, 24, 0.82);
            padding: 12px;
            display: grid;
            gap: 8px;
            align-content: start;
        }}

        .board-slot h3 {{
            margin: 0;
            font-size: 0.95rem;
            letter-spacing: 0.02em;
            color: #cbd5e1;
        }}

        .slot-ul {{ grid-area: ul; }}
        .slot-ur {{ grid-area: ur; }}
        .slot-ll {{ grid-area: ll; }}
        .slot-lr {{ grid-area: lr; }}
        .slot-center {{ grid-area: center; }}

        .slot-ul, .slot-ur, .slot-ll, .slot-lr {{
            min-height: 360px;
        }}

        .main-board {{
            width: 100%;
            min-height: 560px;
            aspect-ratio: 16 / 9;
            border: 2px dashed #14b8a677;
            border-radius: 14px;
            background:
                radial-gradient(700px 260px at 50% -20%, rgba(45, 212, 191, 0.15), transparent 70%),
                linear-gradient(180deg, rgba(15, 23, 42, 0.8), rgba(2, 6, 23, 0.92));
            padding: 12px;
            display: grid;
            grid-template-rows: auto auto 1fr auto;
            gap: 10px;
        }}

        .main-board-title {{
            font-weight: 700;
            font-size: 1.05rem;
        }}

        .main-board-meta {{
            color: #94a3b8;
            font-size: 0.9rem;
        }}

        .main-board-pools {{
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 8px;
        }}

        .pool-chip {{
            border: 1px solid #334155;
            border-radius: 10px;
            padding: 8px;
            background: rgba(15, 23, 42, 0.7);
            font-size: 0.88rem;
        }}

        .island-layout {{
            border: 1px dashed #32465f;
            border-radius: 10px;
            padding: 8px;
            display: grid;
            grid-template-columns: 1fr;
            gap: 8px;
            background: rgba(15, 23, 42, 0.52);
        }}

        .island-main-tile {{
            width: 100%;
            aspect-ratio: 1 / 1;
            border: 1px solid #3b4f6a;
            border-radius: 12px;
            background: linear-gradient(160deg, rgba(30, 64, 83, 0.5), rgba(15, 23, 42, 0.8));
            display: grid;
            place-items: center;
            color: #cbd5e1;
            font-size: 0.85rem;
        }}

        .island-lower-row {{
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 8px;
        }}

        .island-small-tile {{
            aspect-ratio: 1 / 1;
            border: 1px solid #3b4f6a;
            border-radius: 10px;
            background: linear-gradient(160deg, rgba(30, 41, 59, 0.6), rgba(15, 23, 42, 0.9));
            display: grid;
            place-items: center;
            color: #94a3b8;
            font-size: 0.75rem;
        }}

        .layer-section {{
            border: 1px solid #243246;
            border-radius: 10px;
            padding: 8px;
            background: rgba(15, 23, 42, 0.6);
        }}

        .resource-row, .workers-row, .ships-row, .residentcards-row {{
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
            align-items: center;
        }}

        .token-pill {{
            border: 1px solid #334155;
            border-radius: 999px;
            padding: 3px 8px;
            background: rgba(30, 41, 59, 0.8);
            font-size: 0.78rem;
        }}

        .worker-chip, .resident-chip {{
            display: inline-flex;
            align-items: center;
            gap: 4px;
            border: 1px solid #334155;
            border-radius: 999px;
            padding: 2px 7px;
            background: rgba(30, 41, 59, 0.7);
            font-size: 0.76rem;
        }}

        .worker-chip img, .resident-chip img, .resource-row img {{
            width: 1.1rem;
            height: 1.1rem;
            object-fit: contain;
        }}

        .objective-card {{
            border: 1px solid #334155;
            border-radius: 10px;
            padding: 8px;
            background: #ffffff;
            color: #0f172a;
            min-height: 110px;
            font-size: 0.82rem;
            line-height: 1.35;
        }}

        .objective-card strong {{
            display: block;
            margin-bottom: 4px;
            color: #0f172a;
        }}

        .objective-inline-strip {{
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            background: #f8fafc;
            padding: 10px;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
            gap: 8px;
        }}

        .objective-inline-title {{
            grid-column: 1 / -1;
            font-weight: 700;
            color: #0f172a;
        }}

        .hidden {{
            display: none !important;
        }}

        @media (max-width: 1200px) {{
            .game-layout {{
                grid-template-columns: 1fr;
                grid-template-rows: auto;
                grid-template-areas:
                    "center"
                    "ul"
                    "ur"
                    "ll"
                    "lr";
                min-height: 0;
            }}
            .main-board-pools {{
                grid-template-columns: 1fr;
            }}
        }}
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
            <strong>Visuelle Spielansicht</strong>
            <div id=\"visualLayer\" class=\"visual-layer\">
                <div class=\"game-layout\">
                    <section class="board-slot slot-ul">
                        <h3>Layer Oben Links</h3>
                        <div id="layerUpperLeft"></div>
                    </section>
                    <section class="board-slot slot-ur">
                        <h3>Layer Oben Rechts</h3>
                        <div id="layerUpperRight"></div>
                    </section>
                    <section class=\"board-slot slot-center\">
                        <div id=\"mainBoard\" class=\"main-board\"></div>
                    </section>
                    <section class="board-slot slot-ll">
                        <h3>Layer Unten Links</h3>
                        <div id="layerLowerLeft"></div>
                    </section>
                    <section class="board-slot slot-lr">
                        <h3>Layer Unten Rechts</h3>
                        <div id="layerLowerRight"></div>
                    </section>
                </div>
            </div>
        </div>

        <div id=\"detailsLayer\" class=\"hidden\">
        <div class=\"panel\">
            <strong>Aktionsdetails</strong>
            <div id=\"actionDetailsContainer\"></div>
        </div>

        <div class="panel">

                    <div class="panel">
                        <strong>Kartenübersicht</strong>
                        <div id="cardOverviewContainer"></div>
                    </div>
            <div class="header">
                <strong id="stateIndicator">Zustand</strong>
                <span class="hint muted">Tasten: &lt;- vorheriger, -&gt; nächster</span>
            </div>
            <div class="grid" style="margin-top: 10px">
                <div class="kv"><div class="k">Datei</div><div class="v" id="fileName"></div></div>
                <div class="kv"><div class="k">Aktion</div><div class="v" id="action"></div></div>
                <div class="kv"><div class="k">Ausgeführte Aktion</div><div class="v" id="executedAction"></div></div>
                <div class="kv"><div class="k">Ausgeführt von</div><div class="v" id="executedBy"></div></div>
                <div class="kv"><div class="k">Runde</div><div class="v" id="round"></div></div>
                <div class="kv"><div class="k">Aktueller Spieler</div><div class="v" id="currentPlayer"></div></div>
            </div>
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
    </div>

    <script>
        const stateDir = {json.dumps(str(state_dir), ensure_ascii=False)};
        const entries = {payload};
        const iconBaseUri = {json.dumps(icon_base_uri, ensure_ascii=False)};
        const iconPathByName = {json.dumps(icon_lookup, ensure_ascii=False)};
        const allPicturePaths = {json.dumps(all_picture_paths, ensure_ascii=False)};
        const iconFileNames = {json.dumps(icon_file_names, ensure_ascii=False)};
        const orderedIconFileNames = [...iconFileNames].sort((left, right) => right.length - left.length);
        const goodIconTokens = {json.dumps(GOOD_ICON_TOKENS, ensure_ascii=False)};
        const orderedGoodIconTokens = Object.keys(goodIconTokens).sort((left, right) => right.length - left.length);
        const goodIconsByName = {json.dumps(GOOD_ICON_NAMES, ensure_ascii=False)};
        const actionImageCandidates = {json.dumps(action_image_candidates, ensure_ascii=False)};
        const smallResourceIconNames = new Set(["gold.png", "tradechip.png", "explorerchip.png"]);
        const colorSquareTokens = {{
            "red_square": "red",
            "blue_square": "blue",
            "green_square": "green",
            "yellow_square": "yellow",
            "orange_square": "orange",
            "purple_square": "purple",
            "black_square": "black",
            "white_square": "white",
        }};
        const orderedColorSquareTokens = Object.keys(colorSquareTokens).sort((left, right) => right.length - left.length);
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
        const cardOverviewContainerEl = document.getElementById("cardOverviewContainer");
        const rawJsonEl = document.getElementById("rawJson");
        const prevBtn = document.getElementById("prevBtn");
        const nextBtn = document.getElementById("nextBtn");
        const mainBoardEl = document.getElementById("mainBoard");
        const layerUpperLeftEl = document.getElementById("layerUpperLeft");
        const layerUpperRightEl = document.getElementById("layerUpperRight");
        const layerLowerLeftEl = document.getElementById("layerLowerLeft");
        const layerLowerRightEl = document.getElementById("layerLowerRight");

        function text(value, fallback = "(nicht im JSON vorhanden)") {{
            if (value === null || value === undefined || value === "") return fallback;
            return String(value);
        }}

        function goodToIconName(value) {{
            const normalized = String(value ?? "").trim().toLowerCase();
            if (normalized === "exploration chip" || normalized === "explorer chip") {{
                return "explorerchip.png";
            }}
            if (normalized === "trade chip") {{
                return "tradechip.png";
            }}
            return goodIconsByName[normalized] || normalized;
        }}

        function normalizeImageName(value) {{
            return String(value || "")
                .trim()
                .toLowerCase()
                .replace(/\\\\/g, "/");
        }}

        function resolveImagePath(value) {{
            const normalized = normalizeImageName(value);
            if (!normalized) return null;

            if (iconPathByName[normalized]) {{
                return iconPathByName[normalized];
            }}

            const fileName = normalized.split("/").pop();
            if (fileName && iconPathByName[fileName]) {{
                return iconPathByName[fileName];
            }}

            if (normalized.startsWith("residentcard_lv_")) {{
                return iconPathByName["residentcard_lv_2.png"] || null;
            }}

            return null;
        }}

        function imageSrcFor(value) {{
            const resolved = resolveImagePath(value);
            return resolved ? `${{iconBaseUri}}/${{resolved}}` : null;
        }}

        function rewardToIconText(rewardValue) {{
            const reward = String(rewardValue ?? "-").trim();
            if (!reward || reward === "-") return "-";

            let match = reward.match(/^(\\d+)\\s+Gold$/i);
            if (match) return `${{match[1]}}x gold.png`;

            match = reward.match(/^(\\d+)\\s+Trade\\s+Points$/i);
            if (match) return `${{match[1]}}x tradechip.png`;

            match = reward.match(/^(\\d+)\\s+Exploration\\s+Points$/i);
            if (match) return `${{match[1]}}x explorerchip.png`;

            match = reward.match(/^(\\d+)\\s+Expedition\\s+Cards?$/i);
            if (match) return `${{match[1]}}x Expeditioncards`;

            match = reward.match(/^(\\d+)x\\s+neuer\\s+Bewohner\\s+Stufe\\s+(\\d)$/i);
            if (match) return `${{match[1]}}x workforce_level_${{match[2]}}.png`;

            match = reward.match(/^(\\d+)x\\s+Upgrade\\s+Stufe\\s+(\\d)\\s*->\\s*(\\d)$/i);
            if (match) return `${{match[1]}}x workforce_level_${{match[2]}}.png -> workforce_level_${{match[3]}}.png`;

            match = reward.match(/^Discard\\s+(\\d+)\\s+ResidentCard\\(s\\)$/i);
            if (match) return `${{match[1]}}x residentcard_lv_2.png abwerfen`;

            if (/^Extra\\s+Action$/i.test(reward)) return "Extra Action";

            return reward;
        }}

        function cardLineText(card) {{
            if (!card || typeof card !== "object") {{
                return "residentcard_lv_2.png => -";
            }}

            const level = Number(card.populationLevel || 2);
            const cardIcon = `residentcard_lv_${{Number.isFinite(level) ? level : 2}}.png`;
            const needs = Array.isArray(card.needs) ? card.needs : [];
            const needIcons = needs.map((good) => {{
                const icon = goodToIconName(good);
                return icon.endsWith(".png") ? icon : `${{icon}}.png`;
            }});
            const needsPart = needIcons.length ? needIcons.join(" , ") : "-";
            const rewardPart = rewardToIconText(card.reward);
            return `${{cardIcon}} ${{needsPart}} => ${{rewardPart}}`;
        }}

        function findPlayerByName(state, playerName) {{
            if (!state || !Array.isArray(state.players)) return null;
            const target = String(playerName || "").trim();
            if (!target) return null;

            for (const player of state.players) {{
                if (player && String(player.name || "") === target) {{
                    return player;
                }}
            }}

            if (target.startsWith("Spieler ")) {{
                const normalized = target.replace("Spieler ", "Player ");
                for (const player of state.players) {{
                    if (player && String(player.name || "") === normalized) {{
                        return player;
                    }}
                }}
            }}

            return null;
        }}

        function startsWithFactorySuffix(value, cursor, tokenLength) {{
            const suffix = value.slice(cursor + tokenLength);
            return /^(?:\\s+)(?:mine|factory|workshop|mill|yard|works|plant|plantation|refinery|foundry)\\b/i.test(suffix);
        }}

        function renderTextWithIcons(container, value) {{
            const textValue = text(value, "");
            if (!textValue) {{
                return;
            }}

            let bufferStart = 0;
            let cursor = 0;

            while (cursor < textValue.length) {{
                const goodToken = orderedGoodIconTokens.find((candidate) => textValue.startsWith(candidate, cursor));
                if (goodToken) {{
                    if (startsWithFactorySuffix(textValue, cursor, goodToken.length)) {{
                        cursor += 1;
                        continue;
                    }}

                    if (cursor > bufferStart) {{
                        container.appendChild(document.createTextNode(textValue.slice(bufferStart, cursor)));
                    }}

                    const wrapper = document.createElement("span");
                    wrapper.className = "icon-inline";
                    const img = document.createElement("img");
                    const resolvedGoodPath = resolveImagePath(goodIconTokens[goodToken]) || goodIconTokens[goodToken];
                    img.src = iconBaseUri + "/" + resolvedGoodPath;
                    img.alt = goodIconTokens[goodToken];
                    img.title = goodIconTokens[goodToken];
                    if (smallResourceIconNames.has(goodIconTokens[goodToken])) {{
                        img.classList.add("resource-small");
                    }}
                    wrapper.appendChild(img);
                    container.appendChild(wrapper);
                    container.appendChild(document.createTextNode(" "));

                    cursor += goodToken.length;
                    if (textValue.startsWith(".png", cursor)) {{
                        cursor += 4;
                    }}
                    bufferStart = cursor;
                    continue;
                }}

                const squareToken = orderedColorSquareTokens.find((candidate) => textValue.startsWith(candidate, cursor));
                if (squareToken) {{
                    if (cursor > bufferStart) {{
                        container.appendChild(document.createTextNode(textValue.slice(bufferStart, cursor)));
                    }}

                    const square = document.createElement("span");
                    square.className = "factory-color-square " + colorSquareTokens[squareToken];
                    square.title = "rotes Quadrat";
                    square.setAttribute("aria-label", "rotes Quadrat");
                    container.appendChild(square);
                    container.appendChild(document.createTextNode(" "));

                    cursor += squareToken.length;
                    bufferStart = cursor;
                    continue;
                }}

                const iconName = orderedIconFileNames.find((candidate) => textValue.startsWith(candidate, cursor));
                if (!iconName) {{
                    cursor += 1;
                    continue;
                }}

                if (goodIconTokens["goodicon_" + iconName.replace(/\\.png$/i, "")] && startsWithFactorySuffix(textValue, cursor, iconName.length)) {{
                    cursor += 1;
                    continue;
                }}

                if (cursor > bufferStart) {{
                    container.appendChild(document.createTextNode(textValue.slice(bufferStart, cursor)));
                }}

                const wrapper = document.createElement("span");
                wrapper.className = "icon-inline";
                const img = document.createElement("img");
                const resolvedIconPath = resolveImagePath(iconName) || iconName;
                img.src = iconBaseUri + "/" + resolvedIconPath;
                img.alt = iconName;
                img.title = iconName;
                if (smallResourceIconNames.has(iconName)) {{
                    img.classList.add("resource-small");
                }} else if (iconName.startsWith("residentcard_")) {{
                    img.classList.add("residentcard-large");
                }}
                wrapper.appendChild(img);
                container.appendChild(wrapper);
                container.appendChild(document.createTextNode(" "));

                cursor += iconName.length;
                if (textValue.startsWith(".png", cursor)) {{
                    cursor += 4;
                }}
                bufferStart = cursor;
            }}

            if (bufferStart < textValue.length) {{
                container.appendChild(document.createTextNode(textValue.slice(bufferStart)));
            }}
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
                renderTextWithIcons(item, diff);
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
                    renderTextWithIcons(paragraph, itemText);
                    list.appendChild(paragraph);
                }}
                wrapper.appendChild(list);
                actionDetailsContainerEl.appendChild(wrapper);
            }}
        }}

        function renderCardOverview(entry) {{
            cardOverviewContainerEl.innerHTML = "";

            if (entry.isInitial) {{
                const state = entry.state || {{}};
                const objectives = Array.isArray(state.objectiveCards) ? state.objectiveCards : [];

                const objectiveTitle = document.createElement("p");
                objectiveTitle.className = "action-title";
                objectiveTitle.textContent = "Objective Cards im Spiel";
                cardOverviewContainerEl.appendChild(objectiveTitle);

                if (!objectives.length) {{
                    const none = document.createElement("p");
                    none.className = "diff-empty";
                    none.textContent = "Keine Objective Cards im JSON vorhanden.";
                    cardOverviewContainerEl.appendChild(none);
                }} else {{
                    const list = document.createElement("div");
                    list.className = "action-list";
                    for (const objective of objectives) {{
                        const paragraph = document.createElement("p");
                        paragraph.className = "action-paragraph";
                        const title = String(objective?.title || "Unbekannte Objective Card");
                        const description = String(objective?.description || "");
                        renderTextWithIcons(paragraph, description ? `${{title}}: ${{description}}` : title);
                        list.appendChild(paragraph);
                    }}
                    cardOverviewContainerEl.appendChild(list);
                }}

                const players = Array.isArray(state.players) ? state.players : [];
                const residentTitle = document.createElement("p");
                residentTitle.className = "action-title";
                residentTitle.style.marginTop = "12px";
                residentTitle.textContent = "ResidentCards je Spieler";
                cardOverviewContainerEl.appendChild(residentTitle);

                for (const player of players) {{
                    const playerName = String(player?.name || "Spieler");
                    const header = document.createElement("p");
                    header.className = "action-title";
                    header.style.margin = "8px 0 4px";
                    header.textContent = playerName.replace("Player ", "Spieler ");
                    cardOverviewContainerEl.appendChild(header);

                    const cards = Array.isArray(player?.cards?.residentCardDetails)
                        ? player.cards.residentCardDetails
                        : [];
                    if (!cards.length) {{
                        const none = document.createElement("p");
                        none.className = "muted";
                        none.textContent = "Keine ResidentCards im JSON vorhanden.";
                        cardOverviewContainerEl.appendChild(none);
                        continue;
                    }}

                    const cardList = document.createElement("div");
                    cardList.className = "action-list handcards-list";
                    for (const card of cards) {{
                        const paragraph = document.createElement("p");
                        paragraph.className = "action-paragraph";
                        renderTextWithIcons(paragraph, cardLineText(card));
                        cardList.appendChild(paragraph);
                    }}
                    cardOverviewContainerEl.appendChild(cardList);
                }}
            }}

            const state = entry.state || {{}};
            const executedBy = entry.executedByPlayer || state.currentPlayer;
            const player = findPlayerByName(state, executedBy);
            const playerName = (player?.name || String(executedBy || "Unbekannt")).replace("Player ", "Spieler ");
            const cards = Array.isArray(player?.cards?.residentCardDetails)
                ? player.cards.residentCardDetails
                : [];

            const details = document.createElement("details");
            details.className = "collapsible";

            const summary = document.createElement("summary");
            summary.textContent = `Handkarten von ${{playerName}} (ein-/ausklappen)`;
            details.appendChild(summary);

            const list = document.createElement("div");
            list.className = "action-list handcards-list";
            if (!cards.length) {{
                const none = document.createElement("p");
                none.className = "muted";
                none.textContent = "Keine Handkarten im JSON vorhanden.";
                list.appendChild(none);
            }} else {{
                for (const card of cards) {{
                    const paragraph = document.createElement("p");
                    paragraph.className = "action-paragraph";
                    renderTextWithIcons(paragraph, cardLineText(card));
                    list.appendChild(paragraph);
                }}
            }}

            details.appendChild(list);
            cardOverviewContainerEl.appendChild(details);
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
                renderTextWithIcons(main, String(score.mainAction || "(unbekannt)"));
                row.appendChild(main);

                const meta = document.createElement("div");
                const scoreValue = typeof score.score === "number"
                    ? score.score.toFixed(4)
                    : String(score.score ?? "-");
                const selectedText = score.selected ? "Gewählt" : "Nicht gewählt";
                meta.className = "agent-score-meta";
                renderTextWithIcons(meta, `Score: ${{scoreValue}} | ${{selectedText}}`);
                if (score.selected) {{
                    meta.classList.add("agent-score-selected");
                }}
                row.appendChild(meta);

                if (score.bestActionVariant) {{
                    const variant = document.createElement("div");
                    variant.className = "agent-score-meta";
                    renderTextWithIcons(variant, `Beste Variante: ${{score.bestActionVariant}}`);
                    row.appendChild(variant);
                }}

                list.appendChild(row);
            }}

            details.appendChild(list);
            agentScoresContainerEl.appendChild(details);
        }}

        function createTokenWithIcon(iconValue, labelText) {{
            const item = document.createElement("span");
            item.className = "token-pill";
            const src = imageSrcFor(iconValue);
            if (src) {{
                const img = document.createElement("img");
                img.src = src;
                img.alt = iconValue;
                img.title = iconValue;
                item.appendChild(img);
                item.appendChild(document.createTextNode(" "));
            }}
            item.appendChild(document.createTextNode(labelText));
            return item;
        }}

        function workerStoneForLevel(levelKey) {{
            const map = {{
                level1: "residents/farmer_stone.png",
                level2: "residents/worker_stone.png",
                level3: "residents/artesian_stone.png",
                level4: "residents/engineer_stone.png",
                level5: "residents/investor_stone.png",
            }};
            return map[levelKey] || null;
        }}

        function renderPlayerLayer(container, player, fallbackLabel) {{
            container.innerHTML = "";
            if (!player || typeof player !== "object") {{
                const empty = document.createElement("p");
                empty.className = "muted";
                empty.textContent = `${{fallbackLabel}}: kein Spieler zugewiesen`;
                container.appendChild(empty);
                return;
            }}

            const title = document.createElement("div");
            title.className = "main-board-title";
            title.textContent = String(player.name || fallbackLabel).replace("Player ", "Spieler ");
            container.appendChild(title);

            const resources = player.resources || {{}};
            const resourceSection = document.createElement("div");
            resourceSection.className = "layer-section resource-row";
            resourceSection.appendChild(createTokenWithIcon("goods/gold.png", `Gold: ${{resources.gold ?? 0}}`));
            resourceSection.appendChild(createTokenWithIcon("goods/tradechip.png", `Tradechips: ${{resources.tradeChips ?? 0}}`));
            resourceSection.appendChild(createTokenWithIcon("goods/explorerchip.png", `Explorerchips: ${{resources.explorerChips ?? 0}}`));
            container.appendChild(resourceSection);

            const ships = player.ships || {{}};
            const shipsSection = document.createElement("div");
            shipsSection.className = "layer-section ships-row";
            shipsSection.appendChild(createTokenWithIcon("ships/tradeship_lv1.png", `Trade-Schiffe: ${{ships.tradeShips ?? 0}}`));
            shipsSection.appendChild(createTokenWithIcon("ships/explorership_lv1.png", `Explorer-Schiffe: ${{ships.explorerShips ?? 0}}`));
            container.appendChild(shipsSection);

            const tiles = player.tiles || {{}};
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
                <div class=\"resource-row\" style=\"margin-top: 7px;\"><span class=\"token-pill\">Land frei: ${{tiles.freeLand ?? 0}}</span><span class=\"token-pill\">Küste frei: ${{tiles.freeCoast ?? 0}}</span><span class=\"token-pill\">See frei: ${{tiles.freeSea ?? 0}}</span></div>
            `;
            container.appendChild(islandSection);

            const workersSection = document.createElement("div");
            workersSection.className = "layer-section";
            const workersTitle = document.createElement("strong");
            workersTitle.textContent = "Verfügbare Arbeiter (Steine)";
            workersSection.appendChild(workersTitle);
            const workersRow = document.createElement("div");
            workersRow.className = "workers-row";
            const fitByLevel = (((player.residents || {{}}).byStatusByLevel || {{}}).fit || {{}});
            for (const levelKey of ["level1", "level2", "level3", "level4", "level5"]) {{
                const count = Number(fitByLevel[levelKey] || 0);
                const chip = document.createElement("span");
                chip.className = "worker-chip";
                const stone = workerStoneForLevel(levelKey);
                const src = stone ? imageSrcFor(stone) : null;
                if (src) {{
                    const img = document.createElement("img");
                    img.src = src;
                    img.alt = levelKey;
                    chip.appendChild(img);
                }}
                chip.appendChild(document.createTextNode(String(count)));
                workersRow.appendChild(chip);
            }}
            workersSection.appendChild(workersRow);
            container.appendChild(workersSection);

            const cardsSection = document.createElement("div");
            cardsSection.className = "layer-section";
            const cardsTitle = document.createElement("strong");
            cardsTitle.textContent = "Resident Cards";
            cardsSection.appendChild(cardsTitle);
            const cardsRow = document.createElement("div");
            cardsRow.className = "residentcards-row";
            const cards = Array.isArray(((player.cards || {{}}).residentCardDetails)) ? player.cards.residentCardDetails : [];
            for (const card of cards.slice(0, 6)) {{
                const chip = document.createElement("span");
                chip.className = "resident-chip";
                const level = Number(card?.populationLevel || 2);
                const iconName = `residentcard_lv_${{Number.isFinite(level) ? level : 2}}.png`;
                const src = imageSrcFor(iconName);
                if (src) {{
                    const img = document.createElement("img");
                    img.src = src;
                    img.alt = iconName;
                    chip.appendChild(img);
                }}
                chip.appendChild(document.createTextNode(`Lv${{Number.isFinite(level) ? level : 2}}`));
                cardsRow.appendChild(chip);
            }}
            if (!cards.length) {{
                const none = document.createElement("span");
                none.className = "muted";
                none.textContent = "Keine Karten";
                cardsRow.appendChild(none);
            }}
            cardsSection.appendChild(cardsRow);
            container.appendChild(cardsSection);
        }}

        function renderObjectiveCards(state, containerEl) {{
            containerEl.innerHTML = "";
            const objectives = Array.isArray(state?.objectiveCards) ? state.objectiveCards : [];
            if (!objectives.length) {{
                const none = document.createElement("p");
                none.className = "muted";
                none.textContent = "Keine Objective Cards im State";
                containerEl.appendChild(none);
                return;
            }}

            const title = document.createElement("div");
            title.className = "objective-inline-title";
            title.textContent = "Objective Cards";
            containerEl.appendChild(title);

            for (const objective of objectives) {{
                const card = document.createElement("div");
                card.className = "objective-card";
                const title = document.createElement("strong");
                title.textContent = String(objective?.title || "Objective Card");
                card.appendChild(title);
                const textNode = document.createElement("div");
                textNode.textContent = String(objective?.description || "");
                card.appendChild(textNode);
                containerEl.appendChild(card);
            }}
        }}

        function renderMainBoard(entry) {{
            const state = entry.state || {{}};
            const board = state.boardState || {{}};
            const resources = board.resources || {{}};
            const islands = board.islands || {{}};
            const ships = board.ships || {{}};

            mainBoardEl.innerHTML = "";
            const title = document.createElement("div");
            title.className = "main-board-title";
            title.textContent = `Main Board - ${{text(entry.actionLabel, "State")}}`;
            mainBoardEl.appendChild(title);

            const meta = document.createElement("div");
            meta.className = "main-board-meta";
            meta.textContent = `Runde ${{text(entry.round, "-")}} | Aktueller Spieler: ${{text(entry.currentPlayer, "-")}}`;
            mainBoardEl.appendChild(meta);

            const objectiveStrip = document.createElement("div");
            objectiveStrip.className = "objective-inline-strip";
            renderObjectiveCards(state, objectiveStrip);
            mainBoardEl.appendChild(objectiveStrip);

            const pools = document.createElement("div");
            pools.className = "main-board-pools";
            pools.innerHTML = `
                <div class=\"pool-chip\">Old World Inseln frei: ${{islands.oldWorldIslands ?? 0}}<br>New World Inseln frei: ${{islands.newWorldIslands ?? 0}}</div>
                <div class=\"pool-chip\">Gold-Pool: ${{resources.goldPool ?? 0}}<br>Tradechips-Pool: ${{resources.tradeChips ?? 0}}<br>Explorerchips-Pool: ${{resources.explorerChips ?? 0}}</div>
                <div class=\"pool-chip\">Trade Ships Board: L1 ${{ships.tradeShips?.level1 ?? 0}} / L2 ${{ships.tradeShips?.level2 ?? 0}} / L3 ${{ships.tradeShips?.level3 ?? 0}}<br>Explorer Ships Board: L1 ${{ships.explorerShips?.level1 ?? 0}} / L2 ${{ships.explorerShips?.level2 ?? 0}} / L3 ${{ships.explorerShips?.level3 ?? 0}}</div>
            `;
            mainBoardEl.appendChild(pools);

            const boardSpace = document.createElement("div");
            boardSpace.className = "layer-section";
            boardSpace.innerHTML = "<strong>Zentrale Spielfläche</strong><div class=\"muted\">Hier folgt als nächstes die genaue grafische Platzierung von Inseln, Fabriken, Schiffen und Markern.</div>";
            mainBoardEl.appendChild(boardSpace);
        }}

        function renderGameBoard(entry) {{
            const state = entry.state || {{}};
            const players = Array.isArray(state.players) ? state.players : [];
            renderMainBoard(entry);
            renderPlayerLayer(layerUpperLeftEl, players[0], "Layer Oben Links");
            renderPlayerLayer(layerUpperRightEl, players[1], "Layer Oben Rechts");
            renderPlayerLayer(layerLowerLeftEl, players[2], "Layer Unten Links");
            renderPlayerLayer(layerLowerRightEl, players[3], "Layer Unten Rechts");
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
            renderGameBoard(entry);

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

    if args.migrate_json:
        migration_root = Path(args.migrate_dir) if args.migrate_dir else DEFAULT_GAME_STATES_DIR
        changed_files, changed_tokens = migrate_debug_json_image_paths(migration_root)
        print(f"JSON-Migration abgeschlossen: {changed_files} Dateien, {changed_tokens} ersetzte Bildpfade")
        return 0

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
        raw_action_details = state.get("executedActionDetails") or build_action_details(
            executed_action,
            state,
            previous_state,
        )
        action_details = iconize_output_text(with_umlauts(str(raw_action_details)) if raw_action_details else None)
        action_detail_blocks = build_action_details_blocks(
            executed_action,
            state,
            previous_state,
            raw_action_details if raw_action_details else action_details,
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
            diffs = [iconize_output_text(with_umlauts(line) or line) or line for line in diff_values(state, previous_state, action_details=action_details)]
            diffs = enrich_working_diffs(diffs, action_details, state, previous_state)
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

