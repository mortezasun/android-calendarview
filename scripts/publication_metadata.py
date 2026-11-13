"""Validation of Gradle Module Metadata artifact references.

The verifier must follow the URL a consumer actually downloads, not the display
name. This module is kept importable so the failure cases can be unit-tested
without constructing a complete Maven publication.
"""

from __future__ import annotations

import json
from pathlib import Path
from urllib.parse import unquote, urlsplit
from urllib.request import url2pathname


def _artifact_url_target(module_path: Path, url: object) -> tuple[Path | None, str | None]:
    """Resolve a metadata URL against the module file, returning an error if invalid."""
    if not isinstance(url, str) or not url.strip():
        return None, "missing url"
    if any(character.isspace() for character in url):
        return None, f"invalid url with whitespace: {url!r}"
    try:
        parsed = urlsplit(url)
    except ValueError as error:
        return None, f"invalid url {url!r}: {error}"
    if parsed.fragment or parsed.query:
        return None, f"invalid url with query/fragment: {url!r}"
    if parsed.scheme:
        if parsed.scheme != "file" or parsed.netloc not in ("", "localhost"):
            return None, f"unsupported non-local url: {url!r}"
        target = Path(url2pathname(unquote(parsed.path)))
    else:
        if parsed.netloc:
            return None, f"invalid network-path url: {url!r}"
        if not parsed.path:
            return None, f"empty url path: {url!r}"
        target = module_path.parent / unquote(parsed.path)
    return target.resolve(), None


def check_module(path: Path) -> list[str]:
    """Return all metadata-reference failures for one `.module` file."""
    failures: list[str] = []
    if not path.is_file() or path.stat().st_size == 0:
        return [f"missing module metadata: {path}"]
    try:
        metadata = json.loads(path.read_text())
    except json.JSONDecodeError as error:
        return [f"invalid module metadata {path}: {error}"]

    for variant in metadata.get("variants", []):
        variant_name = variant.get("name")
        for artifact in variant.get("files", []):
            name = artifact.get("name")
            if not isinstance(name, str) or not name.strip():
                failures.append(f"{path}: variant {variant_name} has a file without a name")
            target, error = _artifact_url_target(path, artifact.get("url"))
            if error:
                failures.append(f"{path}: variant {variant_name} artifact {name!r}: {error}")
                continue
            assert target is not None
            if not target.is_file() or target.stat().st_size == 0:
                failures.append(
                    f"{path}: variant {variant_name} references missing/empty {target}"
                )

        available_at = variant.get("available-at")
        if available_at is not None:
            target, error = _artifact_url_target(path, available_at.get("url"))
            if error:
                failures.append(
                    f"{path}: variant {variant_name} available-at: {error}"
                )
            elif target is not None and (not target.is_file() or target.stat().st_size == 0):
                failures.append(
                    f"{path}: variant {variant_name} available-at target is missing/empty: {target}"
                )
    return failures
