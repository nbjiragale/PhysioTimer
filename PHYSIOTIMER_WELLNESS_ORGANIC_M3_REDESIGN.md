# PhysioTimer Redesign Handoff — Wellness Organic Layered on Material 3

Use this document as the implementation brief for redesigning the full PhysioTimer Android app UI in Jetpack Compose. The app should no longer feel like a prototype. It should feel like a polished, client-ready physiotherapy companion: calm, healing, warm, clinical enough to trust, and friendly enough for repeated home rehab use.

## 1. North Star

**Design direction:** Wellness Organic layered on Material 3.

**Vibe:** calming, healing, spa-like, grounded, warm, therapeutic.

**Reference feeling:** Headspace / Calm-like softness, but adapted to physiotherapy rather than meditation. Do not copy those apps literally. The output should feel purpose-built for rehab: guided, trustworthy, readable during movement, and low-stress.

**Core visual language:**

- Muted sage green, warm beige, dusty lavender, clay/coral accents.
- Soft rounded shapes, generous whitespace, calm hierarchy.
- Layered surfaces instead of repeated white cards everywhere.
- Gentle progress arcs, circular rhythms, soft halos, and calm motion.
- Large readable timer typography and large touch targets.
- Material 3 components underneath, customized with organic colors, shapes, typography, and motion.

## 2. Existing App Context

PhysioTimer is a Jetpack Compose Android app for creating, saving, and running physiotherapy exercise timers. Users build routines as timed steps, set repetitions and countdown behavior, and then run guided sessions with voice/audio/vibration cues.

Current screens:

1. Home / Exercises
2. Recent
3. Settings
4. Create / Edit Exercise
5. Active Timer
6. Completion

Current implementation files likely to modify:

```text
app/src/main/java/com/niranjan/physiotimer/ui/DesignTokens.kt
app/src/main/java/com/niranjan/physiotimer/ui/HomeScreen.kt
app/src/main/java/com/niranjan/physiotimer/ui/EditExerciseScreen.kt
app/src/main/java/com/niranjan/physiotimer/ui/ActiveTimerScreen.kt
app/src/main/java/com/niranjan/physiotimer/ui/CompletionScreen.kt
app/src/main/java/com/niranjan/physiotimer/ui/SharedComponents.kt
app/src/main/java/com/niranjan/physiotimer/ui/PhysioRepTimerApp.kt
app/src/main/java/com/niranjan/physiotimer/data/ExerciseModels.kt
```

Preserve existing workflows and data model unless a UI-only refactor requires safer naming. Prioritize visual polish and component consistency over changing product behavior.

## 3. Product Personality

The app should feel like:

- A calm physiotherapy companion.
- A guided recovery ritual.
- A spa-like health tool, not a gym timer.
- Warm, spacious, and reassuring.
- Clinical enough to be credible, but not hospital-cold.

Avoid:

- Generic meditation app imitation.
- Busy dashboards.
- Harsh workout styling.
- Sharp progress bars.
- Tiny tap targets.
- Repeated nested cards everywhere.
- Overusing teal as the only brand color.
- Making every step tile look equally important.

## 4. Material 3 Strategy

Build on Material 3, but heavily theme it.

Use Material 3 components as the base:

- `Scaffold`
- `NavigationBar`
- `NavigationBarItem`
- `LargeFloatingActionButton` or custom extended FAB
- `Button`, `FilledTonalButton`, `OutlinedButton`, `TextButton`
- `Card`, `ElevatedCard`, `Surface`
- `AssistChip`, `FilterChip`, `SuggestionChip`
- `Switch`
- `TextField` / custom compact fields if needed
- `IconButton`, `FilledTonalIconButton`
- `ModalBottomSheet` if future settings/details are introduced

Then customize via:

- `MaterialTheme.colorScheme`
- `MaterialTheme.typography`
- `MaterialTheme.shapes`
- layered custom surfaces
- custom arcs/rings for timer progress
- custom organic background shapes

Implementation rule: **prefer themed Material 3 components over raw custom boxes**, except for highly branded areas like the timer arc, wellness background layers, and step timeline.

## 5. Color System

Replace the current teal-heavy system with an organic therapeutic palette.

### Core Palette

```kotlin
object WellnessColors {
    val Sage50 = Color(0xFFF4F8F1)
    val Sage75 = Color(0xFFEEF5EA)
    val Sage100 = Color(0xFFE4EEDD)
    val Sage200 = Color(0xFFC9DCC1)
    val Sage300 = Color(0xFFA8C09C)
    val Sage500 = Color(0xFF6F8F64)
    val Sage600 = Color(0xFF58754F)
    val Sage700 = Color(0xFF3F5C39)

    val Beige50 = Color(0xFFFFFBF5)
    val Beige100 = Color(0xFFF8EFE2)
    val Beige200 = Color(0xFFEBDCC7)
    val Beige300 = Color(0xFFD9C5AA)

    val Lavender50 = Color(0xFFF8F4FA)
    val Lavender100 = Color(0xFFEDE3F1)
    val Lavender200 = Color(0xFFD9C7E1)
    val Lavender400 = Color(0xFF9C7EAD)
    val Lavender600 = Color(0xFF725684)

    val Clay50 = Color(0xFFFFF4EF)
    val Clay100 = Color(0xFFF7D9CB)
    val Clay400 = Color(0xFFD98B6E)
    val Clay600 = Color(0xFFB95F45)

    val SkyMist50 = Color(0xFFF1F7FA)
    val SkyMist200 = Color(0xFFC7DDE7)
    val SkyMist500 = Color(0xFF6F9CAD)

    val Ink900 = Color(0xFF1F2A24)
    val Ink700 = Color(0xFF435047)
    val Ink500 = Color(0xFF6E7A71)
    val Ink300 = Color(0xFFA5AEA7)

    val White = Color(0xFFFFFFFF)
    val Scrim = Color(0x660F1F18)
}
```

### Material 3 Light Color Scheme

```kotlin
val WellnessLightColorScheme = lightColorScheme(
    primary = WellnessColors.Sage600,
    onPrimary = WellnessColors.White,
    primaryContainer = WellnessColors.Sage100,
    onPrimaryContainer = WellnessColors.Sage700,

    secondary = WellnessColors.Lavender600,
    onSecondary = WellnessColors.White,
    secondaryContainer = WellnessColors.Lavender100,
    onSecondaryContainer = WellnessColors.Lavender600,

    tertiary = WellnessColors.Clay600,
    onTertiary = WellnessColors.White,
    tertiaryContainer = WellnessColors.Clay100,
    onTertiaryContainer = WellnessColors.Clay600,

    background = WellnessColors.Beige50,
    onBackground = WellnessColors.Ink900,
    surface = WellnessColors.Beige50,
    onSurface = WellnessColors.Ink900,
    surfaceVariant = WellnessColors.Sage75,
    onSurfaceVariant = WellnessColors.Ink700,

    outline = Color(0xFFD7D0C3),
    outlineVariant = Color(0xFFE8DFD2),

    error = Color(0xFFBA4E42),
    onError = WellnessColors.White,
    errorContainer = Color(0xFFFFE2DC),
    onErrorContainer = Color(0xFF7B241C)
)
```

### Usage Rules

- **Sage** = primary brand, healing, start/play, active progress.
- **Beige** = background warmth, calm base.
- **Lavender** = secondary accents, rest/recovery, gentle contrast.
- **Clay** = destructive actions, warnings, stop session. Use sparingly.
- **Sky mist** = neutral info accents.
- **Ink** = text only; avoid pure black.

Do not use bright saturated teal as the primary identity anymore. Teal can remain as a compatibility accent only if already tied to step colors, but the new dominant feel should be sage/beige/lavender.

## 6. Typography

Use Material 3 typography roles, but make the system softer and more editorial.

Suggested type approach:

- Use default Android sans if no custom font is available.
- If using a bundled font is possible later, choose a warm geometric/humanist sans.
- Avoid overly heavy `ExtraBold` everywhere.
- Use confidence through spacing and hierarchy, not just weight.

### Recommended Roles

```kotlin
val WellnessTypography = Typography(
    displayLarge = TextStyle(fontSize = 88.sp, lineHeight = 92.sp, fontWeight = FontWeight.SemiBold),
    displayMedium = TextStyle(fontSize = 64.sp, lineHeight = 68.sp, fontWeight = FontWeight.SemiBold),
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
)
```

Timer number should stay huge and readable, but reduce harshness:

- Current: 92sp extra-bold.
- New: 88-96sp semi-bold, slightly tighter, with calm ink color.
- For small screens, scale down to 76-84sp.

## 7. Shape System

Use roundness as a brand cue, but create hierarchy.

```kotlin
object WellnessRadius {
    val Tiny = 8.dp
    val Small = 12.dp
    val Medium = 18.dp
    val Large = 24.dp
    val XLarge = 32.dp
    val Pill = 999.dp
}
```

Guidelines:

- Background layers: 32-40dp.
- Main content panels: 28-32dp.
- Cards: 24-28dp.
- Inputs: 18-22dp.
- Chips: pill.
- Primary buttons: pill.
- Timer control buttons: circular or pill depending on importance.

Avoid using the same 20dp rounded card for everything. Vary containers intentionally.

## 8. Spacing System

```kotlin
object WellnessSpacing {
    val Xxs = 4.dp
    val Xs = 8.dp
    val Sm = 12.dp
    val Md = 16.dp
    val Lg = 20.dp
    val Xl = 24.dp
    val Xxl = 32.dp
    val Xxxl = 40.dp
}
```

Rules:

- Screen horizontal padding: 20dp on phones, 24dp if width allows.
- Card internal padding: 18-24dp.
- Timer screen should breathe: fewer elements, larger gaps.
- Form fields should be grouped by meaning, not packed tightly.
- Minimum interactive target: 48dp width/height; critical timer controls should be 56-72dp.

## 9. Elevation and Layering

The redesign should feel layered, not shadow-heavy.

Use:

- soft tonal surfaces
- subtle borders with low opacity
- minimal elevation
- gradients only where they add calm focus
- blurred/soft organic background shapes where feasible

### Surface Levels

```kotlin
object WellnessSurfaces {
    val Screen = WellnessColors.Beige50
    val LayerSoft = WellnessColors.Sage50
    val LayerWarm = WellnessColors.Beige100
    val LayerLavender = WellnessColors.Lavender50
    val Card = Color(0xFFFFFEFB)
    val CardMuted = Color(0xFFF7F1E8)
}
```

Surface approach:

- App background: warm beige.
- Header area: soft organic sage/lavender blobs or gradient.
- Main list panels: off-white, not stark white.
- Timer focus area: elevated calm panel with ring and subtle halo.
- Inputs: warm-white or sage-tinted fields.

## 10. Motion and Progress

Motion should be gentle and therapeutic.

Use:

- `animateFloatAsState` for arcs.
- `animateDpAsState` for expanding active step tile.
- `AnimatedVisibility` for secondary controls.
- Slow pulsing halo during active timer.
- No bouncy gamified motion.
- No aggressive loading spinners.

Progress style:

- Prefer arcs, rings, and soft timelines.
- Avoid sharp rectangular progress bars.
- Use round stroke caps.
- Use sweep gradients only for primary timer progress.
- Use lavender/sage step dots for sequence progress.

## 11. Shared Components to Build or Refactor

Create/refactor these components in `SharedComponents.kt` or a new UI components file.

### 11.1 WellnessScreen

A common screen wrapper with organic background.

Responsibilities:

- Fill max size.
- Apply warm beige background.
- Optionally draw large translucent sage/lavender/clay blobs behind content.
- Provide safe area padding.

Suggested implementation:

```kotlin
@Composable
fun WellnessScreen(
    modifier: Modifier = Modifier,
    showOrganicBackground: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (showOrganicBackground) {
            OrganicBackground()
        }
        content()
    }
}
```

### 11.2 OrganicBackground

Draw non-interactive soft shapes.

Suggested style:

- top-right large sage circle/oval at 10-16% opacity
- bottom-left lavender oval at 8-12% opacity
- optional beige/cloud layer behind main content

Use `Canvas` or layered `Box` shapes.

### 11.3 WellnessCard

A softer card, not always pure white.

```kotlin
@Composable
fun WellnessCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFFFFEFB),
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable ColumnScope.() -> Unit
)
```

Rules:

- Use tonal border: `outlineVariant.copy(alpha = 0.65f)`.
- Elevation: 0-2dp only.
- Prefer content spacing over dividers.

### 11.4 PrimaryWellnessButton

A pill button for main actions.

Style:

- sage gradient or solid sage depending on platform support.
- 52-56dp height.
- pill shape.
- icon optional.
- label in `labelLarge`.

Use for: Start, Save, Done, Create.

### 11.5 TonalWellnessButton

Filled tonal pill for secondary actions.

Use for: Edit, Do Again, Start Now, Skip Step.

### 11.6 SoftIconButton

Replace current square teal icon buttons with warmer tonal icon buttons.

- Size: 44-48dp.
- Shape: 16-18dp or circle depending context.
- Background: sage/lavender/beige tint.
- Icon: ink700 or semantic accent.

### 11.7 WellnessChip

Small rounded semantic chip.

Use for metadata:

- steps
- reps
- duration
- voice enabled
- current rep

Chips should be calm and low-contrast, not noisy.

### 11.8 GentleProgressArc

Custom Compose canvas component for timer/progress.

Requirements:

- Partial circular arc, not full sharp circle.
- Round stroke caps.
- Inactive track in sage100 or beige200.
- Active track in sage/lavender gradient.
- Optional soft halo while running.
- Progress should animate smoothly.

Suggested API:

```kotlin
@Composable
fun GentleProgressArc(
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    trackColor: Color = WellnessColors.Sage100,
    isRunning: Boolean = false,
    strokeWidth: Dp = 18.dp,
    startAngle: Float = 145f,
    sweepAngle: Float = 250f
)
```

### 11.9 StepTimeline

Replace cramped row of small tiles with a calmer sequence indicator.

Options:

- horizontal soft pills where active step expands
- small dots connected by faint line
- active step card below the dots

Preferred approach for timer:

- Use dots/pills for quick progress overview.
- Show current step details separately in the timer panel.
- Do not force users to read all steps during movement.

## 12. Per-Screen Redesign

## 12.1 Home / Exercises

### Goal

Make the saved routines feel curated and ready to start. The main action should be obvious. The screen should feel polished immediately when opened.

### Layout

Use `WellnessScreen` + `Scaffold`.

Top area:

- Organic background layer.
- Greeting-style header:
  - small label: `PhysioTimer`
  - headline: `Your recovery flows`
  - supportive subtitle: `Choose a routine and move at your pace.`
- Settings icon as soft icon button.

Search:

- Use a warm rounded search field.
- Placeholder: `Search routines`
- Height: 52dp.
- Container: off-white / sage50.

Routine list:

- Use fewer hard card borders.
- Use large soft cards with clear start button.
- Keep metadata but reduce visual noise.

FAB:

- Use extended pill FAB: `Create routine`.
- Sage primary.
- Place above navigation bar with breathing room.

### Exercise Card Redesign

Current card is functional but generic. Replace with a richer `RoutineCard`.

Structure:

1. Top row:
   - organic initial/avatar circle using step accent or sage/lavender tint
   - exercise name
   - more/delete menu or small edit action
2. Middle:
   - duration prominent: `8 min` or `2:30`
   - supporting line: `4 steps • 3 reps`
3. Bottom:
   - left: small chips for Voice / Count if relevant
   - right: primary pill `Start`

Visual style:

- background: `Card`
- shape: 30dp
- padding: 20dp
- border: outlineVariant alpha 0.5
- optional top-left very subtle gradient wash

Do not make `Edit`, delete, and `Start` visually equal. `Start` is primary.

### Empty State

Make it feel warm, not sterile.

Copy:

```text
Start with one gentle routine
Create a simple guided flow for your rehab exercises.
```

CTA: `Create routine`

Visual:

- soft sage/lavender circular illustration using existing timer/play icons.

## 12.2 Recent

### Goal

Recent should feel like “continue recovery,” not a duplicate of Home.

Header:

- headline: `Continue where you left off`
- subtitle: `Recently started routines appear here.`

Cards:

- Same `RoutineCard`, but metadata should emphasize last used:
  - `Started today`
  - `Last used 2 days ago`

Empty state:

```text
No recent sessions yet
Start a routine and it will appear here for quick access.
```

No create FAB unless there are no saved exercises. If empty and no saved routines are known, offer `Create routine`.

## 12.3 Settings

### Goal

Either give Settings a real purpose or make it intentionally minimal.

Recommended: create a real calm settings screen with app-level preferences if available, even if some are informational.

Sections:

1. `Session guidance`
   - Voice cues
   - Vibration feedback
   - Count aloud defaults
2. `Comfort`
   - Keep screen awake during timer
   - Larger timer text
   - Reduced motion
3. `About`
   - App name/version if available
   - note: `Per-routine voice and count settings are configured in each exercise.`

If these settings are not wired in the data model yet, keep them as disabled or informational rows, not fake working controls.

Design:

- Use grouped settings panels with soft row containers.
- Icons in sage/lavender circles.
- Avoid a blank placeholder.

## 12.4 Create / Edit Exercise

### Goal

Make routine creation feel calm and manageable. The current UI is card-heavy and cramped. The redesign should feel like building a guided recovery flow step by step.

### Layout

Top bar:

- Back soft icon button.
- Title: `Create routine` / `Edit routine`.
- Subtitle: `Build a guided flow`.
- Save button as primary pill.

Main scroll:

1. Routine name panel
2. Steps timeline/editor
3. Session rhythm panel
4. Save CTA at bottom as backup for long screens

### Routine Details Panel

Use a single warm panel.

Fields:

- `Routine name`
- summary chips: `4 steps`, `3 reps`, `8 min`

Text field should be bigger and calmer:

- height 56dp
- rounded 20dp
- no harsh border unless focused

### Steps Section

Current step cards include too many controls packed together. Redesign as editable step blocks with progressive disclosure.

Each step card:

- left: soft step number circle
- top: step name field
- duration field as a prominent small pill or compact field
- voice/count controls below in a soft nested row
- delete icon de-emphasized, clay only on hover/press or as icon tint

Step card background should vary subtly by step accent:

- Lift / active: sage tint
- Hold: lavender tint
- Rest / breathe: sky mist or beige
- Relax: clay/beige tint

Do not use highly saturated step colors.

### Step Accent Palette

```kotlin
val StepAccentPalette = listOf(
    StepAccent("Sage", Color(0xFF6F8F64), Color(0xFFEAF2E5)),
    StepAccent("Lavender", Color(0xFF8E72A0), Color(0xFFF1EAF5)),
    StepAccent("Clay", Color(0xFFC47A5B), Color(0xFFFFEEE7)),
    StepAccent("Sky Mist", Color(0xFF6F9CAD), Color(0xFFEAF4F8)),
    StepAccent("Olive", Color(0xFF8A8F55), Color(0xFFF1F2E2)),
    StepAccent("Warm Sand", Color(0xFFA8875F), Color(0xFFF6EDDF))
)
```

### Add Step Button

Use a full-width tonal pill:

```text
+ Add another step
```

Height: 52dp.

### Session Rhythm Panel

Rename from “Session settings” to `Session rhythm`.

Rows:

- Repetitions
- Start countdown
- Count start aloud
- Count interval

Use large steppers with 48dp touch targets.

Copy improvements:

- `Repetitions`
- `Start countdown`
- `Speak countdown`
- `Countdown interval`

### Validation

Use a warm, clear validation panel.

- background: errorContainer
- title: `Needs a quick fix`
- list messages
- clay icon

Avoid alarming red unless destructive.

## 12.5 Active Timer

This is the most important screen. It must be usable during movement, at arm's length, and with low cognitive load.

### Goal

Create a focused guided-session experience. Users should instantly know:

1. What step they are on.
2. How many seconds remain.
3. Whether the session is running or paused.
4. How to pause/resume safely.
5. How to stop if needed.

### Layout

Use a vertically centered composition with generous breathing room.

Suggested hierarchy:

1. Top session header
2. Rep/step calm status strip
3. Main timer sanctuary panel
4. Step rhythm preview
5. Controls
6. Stop session

### Top Header

Keep minimal.

- Back icon or close icon left.
- Center:
  - exercise name, one line max
  - planned total duration below
- Voice icon right.

Use no heavy app bar. Let the background breathe.

### Status Strip

Replace multiple chips with one calm strip.

Example:

```text
Rep 2 of 3   •   Step 3 of 5
```

Use a warm translucent pill container.

If preparing:

```text
Preparing to begin
```

If paused:

```text
Paused — resume when ready
```

### Timer Sanctuary Panel

This is the hero component.

Visual:

- large rounded organic card/panel, 34-40dp radius
- warm off-white surface
- subtle sage/lavender background glow
- timer arc large and centered
- current step pill above or inside arc
- timer number large and calm
- message below

Content order inside:

1. Current step pill: `Hold` / `Rest` / `Lift`
2. Gentle progress arc
3. Remaining seconds number
4. Unit label: `seconds`
5. Guidance copy

Guidance copy examples:

- Preparing: `Settle in. Your first movement starts soon.`
- Running: `Move slowly and keep your breathing steady.`
- Rest: `Release tension and reset your posture.`
- Paused: `Paused. Resume when you feel ready.`
- Final step: `Almost there — stay steady.`

### Timer Arc

Implement a custom `GentleProgressArc`.

- Partial arc from ~145° to 395°.
- Round caps.
- Stroke width 16-20dp.
- Active segment sage → lavender gradient.
- Track sage100/beige200.
- Add subtle pulsing halo while running.
- Paused state: stop halo and shift active color to lavender/ink muted.

Do not use sharp linear bars for the main timer.

### Step Rhythm Preview

Make it glanceable rather than dense.

Preferred structure:

- horizontal dots/pills for each step
- active item expands into a soft pill with step name
- completed steps use low-contrast filled dot
- upcoming steps use outline/track

Example:

```text
●  ●  [ Hold 20s ]  ○  ○
```

For many steps, allow horizontal scroll but keep active centered if feasible.

### Controls

Critical controls must be large and safe.

Layout:

- Center: large circular Play/Pause, 72dp.
- Left: Skip Step tonal pill, 56dp high.
- Right: Skip Rep tonal pill, 56dp high.
- Stop session: separate clay text/outlined button below, not near play/pause.

State rules:

- Preparing: left button becomes `Start now`.
- Preparing: `Skip Rep` disabled.
- Running: center shows pause.
- Paused: center shows play/resume.

Use explicit labels. Do not rely on icons only.

### Accessibility

- Timer text should be readable from arm's length.
- All controls min 48dp, primary control 72dp.
- Provide content descriptions for icon buttons.
- Avoid relying on color only for current step; use text and shape.
- Reduced motion setting should disable pulse/halo animation.

## 12.6 Completion

### Goal

Celebrate completion gently. It should feel encouraging, not like a fitness trophy screen.

Headline:

```text
Well done
```

Subtitle options:

```text
You completed your recovery flow.
```

or

```text
Nice work staying consistent.
```

Visual:

- soft concentric sage/lavender circles
- success check or gentle leaf/check icon
- no confetti unless extremely subtle

Stats:

- `Reps completed`
- `Total time`

Buttons:

- Primary: `Done`
- Secondary: `Do again`

Use a bottom action area with full-width buttons.

## 13. Navigation Bar

Bottom nav should be simple and soft.

Style:

- container: warm off-white with subtle top border or floating rounded container
- selected item: sage container pill
- unselected: ink500
- labels visible

Screens: Exercises, Recent, Settings.

Avoid bright active indicators. Make it feel calm and premium.

## 14. Copy Refresh

Use reassuring rehab language.

Replace generic or mechanical labels where helpful:

| Current | New |
|---|---|
| Exercises | Routines or Recovery flows |
| Create Exercise | Create routine |
| Build a guided flow | Build a gentle guided flow |
| Start | Start |
| Stop session | Stop session |
| Skip Step | Skip step |
| Skip Rep | Skip rep |
| Well Done | Well done |
| Exercise completed. | You completed your recovery flow. |
| Count start aloud | Speak countdown |

Keep labels short during timer use.

## 15. Implementation Plan for Codex

Execute in this order.

### Phase 1 — Design Tokens

1. Update `DesignTokens.kt` with:
   - new color palette
   - M3 `ColorScheme`
   - typography roles
   - shape tokens
   - spacing tokens
   - step accent palette
2. Create compatibility aliases only if needed to avoid massive compile errors.
3. Ensure old teal tokens are no longer visually dominant.

### Phase 2 — Shared Components

Refactor or create:

- `WellnessScreen`
- `OrganicBackground`
- `WellnessCard`
- `PrimaryWellnessButton`
- `TonalWellnessButton`
- `SoftIconButton`
- `WellnessChip`
- `GentleProgressArc`
- `StepTimeline`
- `RoutineCard`
- `SettingsSection`
- `StepperRow`

### Phase 3 — Theme Wiring

In `PhysioRepTimerApp.kt` or app theme wrapper:

- apply `MaterialTheme(colorScheme = WellnessLightColorScheme, typography = WellnessTypography, shapes = WellnessShapes)`
- ensure system bars use warm background / transparent where appropriate
- keep dynamic color off unless specifically desired; this brand should remain stable for client submission

### Phase 4 — Home and Recent

- Replace current generic header with wellness header.
- Replace search pill with warm M3 search field.
- Replace exercise cards with `RoutineCard`.
- Make `Start` visually primary.
- Add extended create FAB on Home.
- Make Recent feel like continuation, not duplicate Home.

### Phase 5 — Editor

- Redesign top bar.
- Replace stacked cards with calmer panels.
- Improve step cards and session rhythm section.
- Preserve all existing form logic and validation.
- Make all controls touch-friendly.

### Phase 6 — Active Timer

- Build `GentleProgressArc` first.
- Replace timer card with sanctuary panel.
- Replace chips with status strip.
- Replace step preview row with `StepTimeline`.
- Improve control layout and touch targets.
- Add paused/preparing/running visual states.

### Phase 7 — Completion and Settings

- Redesign completion with gentle success moment.
- Upgrade settings from placeholder to meaningful grouped screen, or intentionally minimal informational screen if no settings are wired.

### Phase 8 — Polish and QA

Test:

- small Android phone width
- long exercise names
- many steps
- 1 step / 1 rep routines
- paused state
- preparing state
- completed state
- empty Home
- empty Recent
- validation errors
- light mode contrast
- touch targets

## 16. Acceptance Criteria

The redesign is successful when:

- The app no longer looks like a prototype.
- The brand clearly reads as wellness/organic/rehab.
- Material 3 components are visibly present but custom-themed.
- The Active Timer screen is dramatically more polished and readable.
- Main actions are obvious.
- Editor feels less cramped.
- Cards are not overused; surfaces feel layered.
- Progress is shown through arcs/timelines, not sharp bars.
- All important touch targets are at least 48dp.
- The implementation remains stable on small Android phones.
- Workflows remain unchanged.

## 17. Suggested Codex Prompt

Use this prompt with Codex:

```text
Redesign the PhysioTimer Jetpack Compose UI using the attached redesign handoff. The target style is “Wellness Organic layered on Material 3”: calming, healing, spa-like, rehab-focused, with muted sage green, warm beige, dusty lavender, soft rounded shapes, generous whitespace, and gentle progress arcs.

Keep the existing app workflows and data model: Home, Recent, Settings, Create/Edit Exercise, Active Timer, and Completion. Prioritize visual polish and client-ready quality. Use Material 3 components where possible, but create custom shared components for organic background layers, wellness cards, primary/tonal buttons, gentle progress arcs, and step timeline.

Start by updating DesignTokens.kt and shared components, then apply the system screen by screen. The Active Timer screen is the highest priority: it must be readable during movement, have a large calm timer, gentle partial progress arc, clear step/rep status, large safe controls, and reassuring rehab copy.

Do not change core business logic unless required for UI compile fixes. Preserve existing audio/voice/count behavior. Make the app feel polished, therapeutic, and trustworthy.
```

## 18. External Design/Platform References

Use these as implementation guidance, not as visual assets to copy:

- Material 3 in Jetpack Compose: https://developer.android.com/develop/ui/compose/designsystems/material3
- Material Design 3 for Jetpack Compose: https://m3.material.io/develop/android/jetpack-compose
- Material 3 theming concepts: https://codelabs.developers.google.com/jetpack-compose-theming
- Material accessibility structure and target sizes: https://m3.material.io/foundations/designing/structure
- Android touch target accessibility guidance: https://support.google.com/accessibility/android/answer/7101858
```
