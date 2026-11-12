# Tablet verification for the adaptive review

Prepared on 2026-11-12 for branch `update`, after commit `8bd281c`.

The existing instrumentation workflow remains unchanged. `android-tablet.yml` adds a separate API 29 emulator job with a 1600x2560 pixel window at 240 dpi in portrait orientation. It uses a real resized emulator display, not only the virtual required-size fixture in the adaptive layout tests.

The workflow records the exact checked-out commit, builds the sample and both instrumentation APKs, runs all JVM tests plus core/sample lint, and invokes the existing `run-connected-tests.sh` without filtering or skipping tests. Both core and sample connected suites run. Reports and diagnostics are uploaded even after failure when available. A failed build or test remains a failed job.

This is additional verification infrastructure, not a successful test result. No run outcome, build success or passing count has been verified when preparing this patch. The Actions check-runs endpoint could not be retrieved in this session. Review the actual job logs and artifacts for the exact candidate revision before closing any finding.

Scope limits: this adds a portrait tablet run at API 29 only. Actual fold/posture delivery, landscape, OS split-screen resizing, newer APIs, large fonts and TalkBack remain acceptance work. Synthetic fold tests still use the sample override. No screenshot or manual visual acceptance is claimed.

Next work: inspect current CI results and fix failures; implement 336/376 dp sample month-width handling; add real calendar selection/month/scroll continuity tests; complete device checks and reconcile verification claims in the project documentation. A merge into master or release is not performed here.

This additional emulator job consumes GitHub Actions runner time. No runtime dependency, published library API, version or tag is changed.
