"""Apply and verify release metadata in the dswhale residence structure template.

This script uses only the Python standard library and is a development tool. It
is not needed at build time or at Minecraft runtime.
"""

from __future__ import annotations

import argparse
import gzip
import io
import struct
from collections import OrderedDict
from dataclasses import dataclass
from pathlib import Path
from typing import BinaryIO


TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12

EXPECTED_SIZE = [13, 9, 14]
CHEST_POSITION = [6, 5, 4]
HOME_ANCHOR = [7, 2, 7]
BED_HEAD_POSITION = [9, 2, 4]
ENTRANCE_POSITION = [4, 2, 9]
EXPECTED_BARREL_COUNT = 20
LOOT_TABLE = "funny_cat_stick:chests/dswhale_architecture"


@dataclass
class Tag:
    tag_type: int
    value: object
    list_type: int | None = None


def read_exact(stream: BinaryIO, length: int) -> bytes:
    value = stream.read(length)
    if len(value) != length:
        raise ValueError("Unexpected end of NBT data")
    return value


def read_number(stream: BinaryIO, fmt: str):
    return struct.unpack(">" + fmt, read_exact(stream, struct.calcsize(fmt)))[0]


def read_string(stream: BinaryIO) -> str:
    length = read_number(stream, "H")
    return read_exact(stream, length).decode("utf-8")


def read_payload(stream: BinaryIO, tag_type: int) -> Tag:
    if tag_type == TAG_BYTE:
        return Tag(tag_type, read_number(stream, "b"))
    if tag_type == TAG_SHORT:
        return Tag(tag_type, read_number(stream, "h"))
    if tag_type == TAG_INT:
        return Tag(tag_type, read_number(stream, "i"))
    if tag_type == TAG_LONG:
        return Tag(tag_type, read_number(stream, "q"))
    if tag_type == TAG_FLOAT:
        return Tag(tag_type, read_number(stream, "f"))
    if tag_type == TAG_DOUBLE:
        return Tag(tag_type, read_number(stream, "d"))
    if tag_type == TAG_BYTE_ARRAY:
        return Tag(tag_type, read_exact(stream, read_number(stream, "i")))
    if tag_type == TAG_STRING:
        return Tag(tag_type, read_string(stream))
    if tag_type == TAG_LIST:
        list_type = read_number(stream, "B")
        length = read_number(stream, "i")
        return Tag(tag_type, [read_payload(stream, list_type) for _ in range(length)], list_type)
    if tag_type == TAG_COMPOUND:
        values: OrderedDict[str, Tag] = OrderedDict()
        while True:
            child_type = read_number(stream, "B")
            if child_type == TAG_END:
                return Tag(tag_type, values)
            name = read_string(stream)
            values[name] = read_payload(stream, child_type)
    if tag_type == TAG_INT_ARRAY:
        return Tag(tag_type, [read_number(stream, "i") for _ in range(read_number(stream, "i"))])
    if tag_type == TAG_LONG_ARRAY:
        return Tag(tag_type, [read_number(stream, "q") for _ in range(read_number(stream, "i"))])
    raise ValueError(f"Unsupported NBT tag type: {tag_type}")


def write_number(stream: BinaryIO, fmt: str, value) -> None:
    stream.write(struct.pack(">" + fmt, value))


def write_string(stream: BinaryIO, value: str) -> None:
    encoded = value.encode("utf-8")
    write_number(stream, "H", len(encoded))
    stream.write(encoded)


def write_payload(stream: BinaryIO, tag: Tag) -> None:
    if tag.tag_type == TAG_BYTE:
        write_number(stream, "b", tag.value)
    elif tag.tag_type == TAG_SHORT:
        write_number(stream, "h", tag.value)
    elif tag.tag_type == TAG_INT:
        write_number(stream, "i", tag.value)
    elif tag.tag_type == TAG_LONG:
        write_number(stream, "q", tag.value)
    elif tag.tag_type == TAG_FLOAT:
        write_number(stream, "f", tag.value)
    elif tag.tag_type == TAG_DOUBLE:
        write_number(stream, "d", tag.value)
    elif tag.tag_type == TAG_BYTE_ARRAY:
        write_number(stream, "i", len(tag.value))
        stream.write(tag.value)
    elif tag.tag_type == TAG_STRING:
        write_string(stream, tag.value)
    elif tag.tag_type == TAG_LIST:
        write_number(stream, "B", tag.list_type)
        write_number(stream, "i", len(tag.value))
        for child in tag.value:
            if child.tag_type != tag.list_type:
                raise ValueError("NBT list contains a mismatched child tag")
            write_payload(stream, child)
    elif tag.tag_type == TAG_COMPOUND:
        for name, child in tag.value.items():
            write_number(stream, "B", child.tag_type)
            write_string(stream, name)
            write_payload(stream, child)
        write_number(stream, "B", TAG_END)
    elif tag.tag_type == TAG_INT_ARRAY:
        write_number(stream, "i", len(tag.value))
        for value in tag.value:
            write_number(stream, "i", value)
    elif tag.tag_type == TAG_LONG_ARRAY:
        write_number(stream, "i", len(tag.value))
        for value in tag.value:
            write_number(stream, "q", value)
    else:
        raise ValueError(f"Unsupported NBT tag type: {tag.tag_type}")


def read_nbt(path: Path) -> tuple[str, Tag]:
    with gzip.open(path, "rb") as compressed:
        stream = io.BytesIO(compressed.read())
    root_type = read_number(stream, "B")
    if root_type != TAG_COMPOUND:
        raise ValueError(f"Expected compound root tag, found {root_type}")
    root_name = read_string(stream)
    root = read_payload(stream, root_type)
    if stream.read(1):
        raise ValueError("Trailing bytes after NBT root tag")
    return root_name, root


def write_nbt(path: Path, root_name: str, root: Tag) -> None:
    uncompressed = io.BytesIO()
    write_number(uncompressed, "B", root.tag_type)
    write_string(uncompressed, root_name)
    write_payload(uncompressed, root)

    temporary_path = path.with_suffix(path.suffix + ".tmp")
    with temporary_path.open("wb") as output:
        with gzip.GzipFile(filename="", mode="wb", fileobj=output, compresslevel=9, mtime=0) as compressed:
            compressed.write(uncompressed.getvalue())
    temporary_path.replace(path)


def list_values(tag: Tag) -> list:
    if tag.tag_type != TAG_LIST:
        raise ValueError("Expected NBT list")
    return [child.value for child in tag.value]


def prepare_structure(root: Tag, write_changes: bool) -> None:
    values: OrderedDict[str, Tag] = root.value
    if list_values(values["size"]) != EXPECTED_SIZE:
        raise ValueError(f"Unexpected structure size: {list_values(values['size'])}")

    palette = values["palette"].value
    blocks = values["blocks"].value
    blocks_by_position: dict[tuple[int, int, int], Tag] = {}
    chests: list[Tag] = []
    barrels: list[Tag] = []
    for block in blocks:
        block_values: OrderedDict[str, Tag] = block.value
        position = tuple(list_values(block_values["pos"]))
        blocks_by_position[position] = block
        state = block_values["state"].value
        block_name = palette[state].value["Name"].value
        if block_name == "minecraft:chest":
            chests.append(block)
        elif block_name == "minecraft:barrel":
            barrels.append(block)

    if len(chests) != 1:
        raise ValueError(f"Expected exactly one chest, found {len(chests)}")
    if len(barrels) != EXPECTED_BARREL_COUNT:
        raise ValueError(f"Expected {EXPECTED_BARREL_COUNT} decorative barrels, found {len(barrels)}")

    def block_state(position: list[int]) -> OrderedDict[str, Tag]:
        block = blocks_by_position.get(tuple(position))
        if block is None:
            raise ValueError(f"No structure block entry at required position {position}")
        return palette[block.value["state"].value].value

    if block_state(BED_HEAD_POSITION)["Name"].value != "minecraft:light_blue_bed":
        raise ValueError(f"Expected bed head at {BED_HEAD_POSITION}")
    bed_properties = block_state(BED_HEAD_POSITION).get("Properties")
    if bed_properties is None or bed_properties.value.get("part", Tag(TAG_STRING, "")).value != "head":
        raise ValueError(f"Expected the head half of the bed at {BED_HEAD_POSITION}")

    if block_state(ENTRANCE_POSITION)["Name"].value != "minecraft:spruce_door":
        raise ValueError(f"Expected spruce entrance door at {ENTRANCE_POSITION}")
    door_properties = block_state(ENTRANCE_POSITION).get("Properties")
    if door_properties is None or door_properties.value.get("half", Tag(TAG_STRING, "")).value != "lower":
        raise ValueError(f"Expected the lower entrance door block at {ENTRANCE_POSITION}")

    if block_state(HOME_ANCHOR)["Name"].value != "minecraft:air":
        raise ValueError(f"Resident spawn is obstructed at {HOME_ANCHOR}")
    anchor_above = [HOME_ANCHOR[0], HOME_ANCHOR[1] + 1, HOME_ANCHOR[2]]
    if block_state(anchor_above)["Name"].value != "minecraft:air":
        raise ValueError(f"Resident spawn has insufficient head room at {anchor_above}")
    anchor_below = [HOME_ANCHOR[0], HOME_ANCHOR[1] - 1, HOME_ANCHOR[2]]
    if block_state(anchor_below)["Name"].value == "minecraft:air":
        raise ValueError(f"Resident spawn has no floor at {anchor_below}")

    chest_values: OrderedDict[str, Tag] = chests[0].value
    chest_position = list_values(chest_values["pos"])
    if chest_position != CHEST_POSITION:
        raise ValueError(f"Chest moved from expected position: {chest_position}")

    block_entity: OrderedDict[str, Tag] = chest_values["nbt"].value
    items = block_entity.get("Items")
    if items is not None and (items.tag_type != TAG_LIST or items.value):
        raise ValueError("Refusing to replace non-empty chest contents")

    if write_changes:
        block_entity.pop("Items", None)
        block_entity["LootTable"] = Tag(TAG_STRING, LOOT_TABLE)

    loot_table = block_entity.get("LootTable")
    if loot_table is None or loot_table.tag_type != TAG_STRING or loot_table.value != LOOT_TABLE:
        raise ValueError("Chest does not reference the expected loot table")

    other_loot_containers = 0
    for block in blocks:
        block_values = block.value
        if block is chests[0] or "nbt" not in block_values:
            continue
        if "LootTable" in block_values["nbt"].value:
            other_loot_containers += 1
    if other_loot_containers:
        raise ValueError(f"Found {other_loot_containers} unexpected additional loot containers")

    print(f"size={EXPECTED_SIZE}")
    print(f"home_anchor={HOME_ANCHOR} (two blocks clear, solid floor)")
    print(f"bed_head={BED_HEAD_POSITION}")
    print(f"entrance={ENTRANCE_POSITION}")
    print(f"chest={CHEST_POSITION}")
    print(f"decorative_barrels={EXPECTED_BARREL_COUNT} (no loot table changes)")
    print(f"loot_table={LOOT_TABLE}")
    print("additional_loot_containers=0")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--structure",
        type=Path,
        default=Path("src/main/resources/data/funny_cat_stick/structures/dswhale_architecture.nbt"),
    )
    parser.add_argument("--check", action="store_true", help="Verify without rewriting the structure")
    args = parser.parse_args()

    root_name, root = read_nbt(args.structure)
    prepare_structure(root, write_changes=not args.check)
    if not args.check:
        write_nbt(args.structure, root_name, root)
        _, verified_root = read_nbt(args.structure)
        prepare_structure(verified_root, write_changes=False)


if __name__ == "__main__":
    main()
