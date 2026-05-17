from __future__ import annotations

import argparse
import json
import os
import re
import sys
from pathlib import Path
from typing import Any


ACTION_RE = re.compile(r"^action_(\d+)_")
INITIAL_RE = re.compile(r"^initial_")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Browse Anno 1800 debug game states one keypress at a time."
    )
    parser.add_argument(
        "--dir",
        default=None,
        help="Path to the game state directory",
    )
    return parser.parse_args()


def find_latest_debuggame_dir(base_dir: Path = Path("game-states")) -> Path | None:
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
    return prefix.startswith("boardState.populationPool.")


def diff_values(current: Any, previous: Any, prefix: str = "") -> list[str]:
    lines: list[str] = []

    if prefix and should_skip_diff(prefix):
        return lines

    if current is None and previous is None:
        return lines

    if previous is None:
        if prefix:
            if prefix in {"executedAction", "executedByPlayer"}:
                lines.append(f"{prefix}: {current}")
            else:
                lines.append(f"{prefix}: hinzugefügt")
        return lines

    if current is None:
        if prefix:
            lines.append(f"{prefix}: entfernt")
        return lines

    if isinstance(current, dict) and isinstance(previous, dict):
        keys = sorted(set(current.keys()) | set(previous.keys()))
        for key in keys:
            next_prefix = f"{prefix}.{key}" if prefix else key
            lines.extend(diff_values(current.get(key), previous.get(key), next_prefix))
        return lines

    if isinstance(current, list) and isinstance(previous, list):
        if len(current) != len(previous):
            label = prefix or "array"
            lines.append(f"{label}: Count {len(previous)} -> {len(current)}")

        max_len = max(len(current), len(previous))
        for index in range(max_len):
            next_prefix = f"{prefix}[{index}]" if prefix else f"[{index}]"
            cur_item = current[index] if index < len(current) else None
            prev_item = previous[index] if index < len(previous) else None
            lines.extend(diff_values(cur_item, prev_item, next_prefix))
        return lines

    if is_primitive(current) and is_primitive(previous):
        if current != previous:
            label = prefix or "value"
            if isinstance(current, (int, float)) and isinstance(previous, (int, float)):
                delta = current - previous
                lines.append(f"{label}: {previous} -> {current} (Δ {delta})")
            else:
                lines.append(f"{label}: '{previous}' -> '{current}'")
        return lines

    if current != previous:
        label = prefix or "value"
        lines.append(f"{label}: '{previous}' -> '{current}'")

    return lines


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


def main() -> int:
    args = parse_args()
    state_dir = Path(args.dir) if args.dir else find_latest_debuggame_dir()

    if state_dir is None:
        print("Kein Debuggame-Ordner gefunden. Starte zuerst .\\debug-game.ps1", file=sys.stderr)
        return 1

    if not state_dir.exists():
        print(f"Verzeichnis nicht gefunden: {state_dir}", file=sys.stderr)
        return 1

    files = sorted(state_dir.glob("*.json"), key=sort_key)
    if not files:
        print(f"Keine JSON-Dateien in {state_dir} gefunden.", file=sys.stderr)
        return 1

    previous_state: dict[str, Any] | None = None

    print()
    print(f"Debuggame-States: {state_dir}")
    print("Taste drücken: beliebige Taste = nächster State, Q = Ende")
    print()

    for index, path in enumerate(files, start=1):
        state = load_state(path)

        clear_screen()
        print(f"State {index}/{len(files)}")
        print(f"Datei: {path.name}")
        print(f"Aktion: {action_label(path)}")
        executed_action = state.get("executedAction")
        executed_by_player = state.get("executedByPlayer")
        if executed_action:
            print(f"Ausgeführte Aktion: {executed_action}")
        else:
            print("Ausgeführte Aktion: (nicht im JSON vorhanden)")
        if executed_by_player:
            print(f"Ausgeführt von: {executed_by_player}")
        else:
            print("Ausgeführt von: (nicht im JSON vorhanden)")
        print(f"Runde: {state.get('round')}")
        print(f"Aktueller Spieler: {state.get('currentPlayer')}")
        if "label" in state:
            print(f"Label: {state.get('label')}")
        print()

        if previous_state is None:
            print("Dies ist der Initial-State.")
        else:
            print("Änderungen seit dem vorherigen State:")
            diffs = diff_values(state, previous_state)
            if diffs:
                for line in diffs:
                    print(f"  {line}")
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
