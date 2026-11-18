# Guardian design system

`docs/design.md` stays the contract for color, type, shape, copy, and
accessibility. This document covers the **additive layer** built on top of it:
shared tokens, tonal gradients, and a small set of primitives that give every
screen the same rhythm. Nothing here changes a value from `design.md`.

## Why a second layer

The first screens repeated the same spacing literals, card styling, and status
pill markup. The layer fixes that in one place so a change to the rhythm is a
one-file change, not a screen-by-screen edit. It is deliberately small: tokens,
brushes, and primitives. There is no new theme, no new color scheme, and no
dynamic color.

## Tokens

`ui/theme/Tokens.kt`

| Token | Values | Use |
| --- | --- | --- |
| `GuardianSpacing` | `xxs 2`, `xs 4`, `sm 8`, `md 12`, `lg 16`, `xl 24`, `xxl 32`, `xxxl 48` | 4 dp grid. `gutter 16` for screen edges, `cardPadding 16`, `sectionGap 24`, `hairline 1`, `maxContentWidth 640` |
| `GuardianSizes` | `minTouch 48`, `primaryButtonHeight 52`, `iconBadge 44`, `iconBadgeLarge 64`, `progressHeight 6` | Touch targets, button height, icon tiles, progress rails |
| `GuardianMotion` | `quick 150`, `standard 200`, `slow 300`, `emphasized` easing | State color changes and progress animation only. No entrance animations |
| `GuardianGradients` | `heroSetup`, `heroHealthy`, `heroAttention`, `heroOff`, `primaryButton`, `barStrong`, `barMuted`, `accentLine`, `ambientTop`, `ambientBottom`, `cardBorder` | Tonal brushes derived from the fixed schemes |

Gradients are tonal, never glossy. They interpolate between roles that already
exist in the light and dark schemes, so brand and contrast stay identical to
`design.md`.

`LocalGuardianGradients` is provided by `GuardianTheme`. Every gradient is
resolved per theme; primitives must read it from the composition, never build a
brush at the call site.

## Primitives

`ui/components/GuardianPrimitives.kt`

| Primitive | Contract |
| --- | --- |
| `GuardianBackground` | Full-screen ambient wash: one `background` fill plus two static radial glows. Also provides `LocalContentColor = onBackground`, so text outside a `Surface` stays readable in both themes. Wrap the app once, in `MainActivity` |
| `GuardianCard` | The standard 16 dp card: `surfaceContainerLow`, hairline border, 16 dp shape. Status cards pass their own `containerColor`/`contentColor` |
| `IconBadge` | Squircle icon tile with a soft radar-style halo. 44 dp default, 64 dp for heroes, 88 dp for the pause screen. The halo is decorative and never the only state signal |
| `StatusPill` | Label plus status dot in a small capsule. Tones: `Neutral`, `Primary`, `Success`, `Attention`. Text always states the status, so color is never the only cue |
| `GuardianPrimaryButton` | One primary action. Gradient fill under a transparent Material `Button`, so ripple, focus ring, and enabled semantics are unchanged. Supports `icon`, `loading`, `destructive`, `enabled` |
| `GuardianProgressBar` | Rounded 6 dp rail with no stop indicator. Used by limits and any future metered row |
| `StepProgress` | Five capsules for onboarding, with progress semantics on the row so TalkBack reads one value |

### Rules

- Screens use **one** `GuardianPrimaryButton`. Secondary actions stay text or
  tonal buttons.
- `GuardianCard` is the only card wrapper in new code. Do not restyle `Card`
  locally.
- Icon tiles come from `IconBadge`; do not draw ad-hoc rounded boxes around
  icons.
- State text lives next to the state color, never instead of it.
- Cards stay at 16 dp and pills at `shapes.small`. `design.md` shape rules are
  unchanged.

## Screen composition

1. `MainActivity` wraps `GuardianNavHost` in `GuardianBackground`.
2. Every `Scaffold` sets `containerColor = Color.Transparent`; otherwise the
   opaque scaffold covers the ambient wash. `ParentScaffold` does this once for
   all parent tabs.
3. `GuardianTopBar` already draws a transparent bar, so the wash continues
   behind it.
4. `StickyActionBar` delegates its primary button to `GuardianPrimaryButton`, so
   forms and the pause screen share one action style.
5. Content columns cap at `GuardianSpacing.maxContentWidth` (640 dp) and centre
   on wide screens. Short screens (gate, pause) cap at 420–480 dp.
6. Insets and scaffold padding go **outside** the scroll modifier:
   `.padding(innerPadding).verticalScroll(state)`. Put `statusBarsPadding()`
   before `verticalScroll` too. Applying them inside the scroll lets content
   slide under a transparent top bar while the title stays fixed.

## Accessibility guarantees

- Every interactive element keeps a 48 dp minimum target.
- Contrast comes from the fixed schemes; gradients only interpolate existing
  roles and never carry text on their own.
- `StatusPill` and `ProtectionRow` pair text with color.
- The ambient wash is a static draw pass: no animation, no layout work, and
  TalkBack ignores it.
- `GuardianProgressBar` and `StepProgress` expose progress semantics; the week
  chart keeps its single merged description.
- `design.md` font weights (400/500), no all-caps rule, and no-shadow rule still
  apply. The pause screen remains the only place a shadow would be allowed, and
  the current build uses none.
- Focus is delegated to Material 3: `GuardianPrimaryButton` draws the gradient
  under a transparent Material button so the press, focus ring, and enabled
  semantics come from Material, and design-system rows use Material `clickable`.
  The design-system layer adds no custom focus implementation.

## Motion

`GuardianMotion` is the design-system token source for animation timing; screens
read `GuardianMotion.quickMillis`, `standardMillis`, and `slowMillis` instead of
hardcoding durations. design.md maps the 200 ms transition, 150 ms pause fade, and
300 ms progress animation onto those tokens.

## Typography

`guardianTypography()` maps the design.md token table onto Material 3's
`Typography` object: display/headline/body/label sizes and line heights, weights
400/500 only, and tabular figures (`tnum`) for durations and times.

## Verifying a change

```bash
./gradlew :app:compileDebugKotlin --offline
./gradlew :app:testDebugUnitTest --offline
./gradlew :app:installDebug --offline
```

Then check both themes, 200 percent font scale, and a 320 dp wide device. The
ambient wash must be visible behind every top bar and bottom bar, and no text
may fall back to black on the dark scheme.
