# ADR 0003: Phase 3 distribution and Hijri method hook

Date: 2026-11-11
Status: Distribution and Hijri hook implemented; strategic items deferred
Target release: 2.0.1
Target branch: update

## Context

Phase 3 of the roadmap covers a BOM, Maven Central readiness, a Hijri method hook,
a Kotlin Multiplatform `:core` extraction, replacing `ir.huri:jalalicalendar`, a Play
demo, and a documentation site. These items have very different risk profiles and
dependencies. The Kotlin Multiplatform extraction changes the module/toolchain model,
a Jalali replacement breaks the public `JalaliCalendar` API, and publishing a Play app
or documentation site needs accounts, signing and hosting. The owner has not selected
those strategic options yet.

## Decision

### Implemented in this phase

- Add `:calendar-bom` as a `java-platform` module. It constrains `:library`,
  `:calendar-gregorian`, `:calendar-hijri` and `:calendar-solarHijri` to the root
  version. It is published to Maven Local by JitPack and the publishing workflow.
- Add the Maven Central POM fields once in the root build: name, description, URL,
  Apache-2.0 license, developer and SCM. This removes duplicated metadata from the
  module publication blocks. Signing, namespace verification, credentials and the
  actual Maven Central upload remain release-operator work.
- Add `HijriDateProvider`, `UmmAlQuraHijriDateProvider` and
  `OffsetHijriDateProvider`. Month and week data build cells through the provider, so
  the displayed Hijri date and the epoch-day identity stay consistent when a local
  method differs from Umm al-Qura. The default remains the platform chronology, so
  existing source calls keep their behaviour.

### Deferred, not rejected

- **Kotlin Multiplatform extraction:** needs a new multiplatform toolchain and an
  architecture decision for `java.time`, `HijrahDate` and formatting. The current
  Android/Compose modules stay unchanged until that decision is made.
- **Jalali replacement:** `:calendar-solarHijri` still exposes
  `ir.huri:jalalicalendar`. Replacing it requires a deliberate breaking public API
  decision and a verified pure-Kotlin implementation.
- **Play demo:** the sample remains a debug demo; no Play listing, signing, data-safety
  form or closed-test rollout is included.
- **Documentation site:** no Dokka or hosting dependency is added. The README and
  `docs/` remain the documentation surface.
- **Maven Central publication:** the POM metadata is a prerequisite, not a release.
  No signing key, repository credentials or upload are configured.

## Consequences

- Consumers can import the BOM and omit individual library versions.
- A host using a local Hijri method can supply a provider without changing `:library`
  or the public calendar-independent rendering surface.
- The existing JitPack coordinates and version remain valid.
- The existing `2.0.1` tag predates the BOM; the README marks the `2.1.0` BOM example as
  unreleased until a consumer-verified tag exists.
- Until the deferred items are approved, the project stays an Android Compose library
  rather than a multiplatform library, and JitPack remains the only configured
  remote publication path.
