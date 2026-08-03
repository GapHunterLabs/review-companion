<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Review Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Local, rule-based code review hints for Java and Kotlin: overly long
  functions, deeply nested conditionals, method-parameter dereferences
  with no preceding null check (Java only), and TODO/FIXME comment
  density.
- Every rule independently toggleable and threshold-configurable, none
  paywalled.
- 100% local — no account, no network call, no rate limit.

### Known gaps

- `NULL_DEREFERENCE` is Java-only — Kotlin's own null-safety type system
  already prevents the exact bug class this rule targets for
  non-platform types.

[Unreleased]: https://github.com/GapHunterLabs/review-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/review-companion/commits/0.1.0
