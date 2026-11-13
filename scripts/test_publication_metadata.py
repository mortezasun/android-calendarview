"""Tests for scripts/publication_metadata.py."""

from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path

try:
    from publication_metadata import check_module
except ModuleNotFoundError:
    from scripts.publication_metadata import check_module


class PublicationMetadataTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.directory = Path(self.temp.name)

    def write_module(self, files: list[dict[str, object]]) -> Path:
        module = self.directory / "artifact.module"
        module.write_text(
            json.dumps({"variants": [{"name": "api", "files": files}]}),
            encoding="utf-8",
        )
        return module

    def write_artifact(self, name: str = "artifact.jar") -> None:
        (self.directory / name).write_bytes(b"published artifact")

    def test_relative_url_is_used_when_name_differs(self) -> None:
        self.write_artifact("download.jar")
        module = self.write_module(
            [{"name": "display-name.jar", "url": "download.jar"}],
        )

        self.assertEqual(check_module(module), [])

    def test_missing_url_fails_even_when_name_target_exists(self) -> None:
        self.write_artifact()
        module = self.write_module([{"name": "artifact.jar"}])

        failures = check_module(module)

        self.assertTrue(any("missing url" in failure for failure in failures), failures)

    def test_broken_url_fails_when_name_target_exists(self) -> None:
        self.write_artifact()
        module = self.write_module(
            [{"name": "artifact.jar", "url": "missing.jar"}],
        )

        failures = check_module(module)

        self.assertTrue(
            any("missing/empty" in failure and "missing.jar" in failure for failure in failures),
            failures,
        )

    def test_missing_name_is_reported_separately_from_a_valid_url(self) -> None:
        self.write_artifact()
        module = self.write_module([{"url": "artifact.jar"}])

        failures = check_module(module)

        self.assertTrue(any("without a name" in failure for failure in failures), failures)
        self.assertFalse(any("missing/empty" in failure for failure in failures), failures)


if __name__ == "__main__":
    unittest.main()
