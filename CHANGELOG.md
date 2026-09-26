<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Review Companion Changelog

## [Unreleased]

## [0.2.1]

### Fixed

- Review/star CTA now links to this plugin's own Marketplace
  reviews page instead of the vendor's generic plugin list.

## [0.2.0]

### Added

- The TODO/FIXME density rule now also counts `HACK` comments -- the
  same class of "revisit this later" marker most real style guides
  and linters group alongside TODO/FIXME.

## [0.1.2]

### Added

- Review/star CTA: after 10 distinct real findings across any of the 4
  rules (long function, nested conditional, null dereference, TODO/FIXME
  density), a one-time notification asks whether to rate the plugin on
  Marketplace, with a permanent "Don't ask again" option. Standard
  mechanism used catalog-wide since 2026-08-24,
  rolled out to this plugin now.

## [0.1.1]

### Fixed

- Marketplace listing icon not rendering (showed a broken "plugin icon"
  placeholder) — replaced with the same icon already proven to render
  correctly on other Gap Hunter Labs listings.

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

[Unreleased]: https://github.com/GapHunterLabs/review-companion/compare/0.2.1...HEAD
[0.2.1]: https://github.com/GapHunterLabs/review-companion/compare/0.2.0...0.2.1
[0.2.0]: https://github.com/GapHunterLabs/review-companion/compare/0.1.2...0.2.0
[0.1.2]: https://github.com/GapHunterLabs/review-companion/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/GapHunterLabs/review-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/review-companion/commits/0.1.0
