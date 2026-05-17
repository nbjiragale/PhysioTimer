# PhysioTimer UI Design Handoff

This document describes the current UI of the PhysioTimer Android app so another LLM/designer can redesign it with full context.

## App Overview

PhysioTimer is a Jetpack Compose Android app for creating and running physiotherapy exercise timers. Users build an exercise as a sequence of timed steps, set repetitions and start countdown behavior, then run a guided timer with voice/audio/vibration cues.

Primary user jobs:

- Save and manage exercise routines.
- Search saved and recent routines.
- Create or edit multi-step exercise flows.
- Run a large, readable session timer.
- Pause, skip a step, skip a rep, stop, repeat, or finish a session.

## Current Visual Direction

The app currently uses a soft wellness/medical style:

- Light mint-tinted background.
- White rounded cards.
- Teal as the main action/accent color.
- Coral red for destructive actions.
- Small chips, pill buttons, and circular icons.
- Large bold timer typography.
- Gentle gradients and subtle borders instead of heavy shadows.

The UI feels calm and functional, but it is also card-heavy and somewhat generic. A redesign should keep the app clear and usable during exercise, while making it feel more polished, modern, and purpose-built for physiotherapy.

## Current Design Tokens

Colors currently used in `DesignTokens.kt`:

- App background: `#F4FAFA`
- Card background: `#FFFFFF`
- Elevated surface: `#EAF5F4`
- Mid surface / inactive timer arc: `#DCEAE9`
- Primary text: `#132B34`
- Secondary text: `#52666D`
- Dim text/icons: `#8AA0A6`
- Primary teal: `#0E9384`
- Dark teal: `#0F766E`
- Destructive coral: `#D92D20`
- Amber: `#B54708`
- Mint success: `#12B76A`
- Sky blue: `#2E90FA`
- Border: `#D8E7E5`
- Focus border: `#0E9384`

Gradients:

- Teal gradient: `#20C7B4` to `#0E9384`
- Coral gradient: `#FF7A70` to `#D92D20`
- Card gradient: `#FFFFFF` to `#F4FAFA`

Step accent palette:

- Teal `#0E9384`
- Sky `#2E90FA`
- Mint `#12B76A`
- Purple `#7A5AF8`
- Amber `#B54708`
- Gray `#667085`
- Aqua `#15B8A6`
- Light blue `#53B1FD`

## Typography

The app uses default Compose/Material typography with manual font sizing and heavy weights.

Current hierarchy:

- Main screen titles: 30sp, extra bold.
- Editor title: 22sp, extra bold.
- Active timer exercise name: 19sp, extra bold.
- Timer number: 92sp, extra bold.
- Card titles: 17sp, bold.
- Body/supporting text: 13-15sp.
- Small labels: 11-13sp, often semi-bold or extra-bold.

The timer screen depends heavily on very large text and should remain readable at arm's length.

## Navigation Structure

The app uses simple in-memory screen state, not a full navigation graph.

Screens:

- `Home`: saved exercises.
- `History`: recently started exercises.
- `Settings`: placeholder screen.
- `Editor`: create/edit exercise.
- `Timer`: active session timer.
- `Complete`: session completion screen.

Bottom navigation appears on Home, Recent, and Settings only.

Bottom tabs:

- Exercises
- Recent
- Settings

## Screen: Home / Exercises

Purpose: show saved exercise flows and allow search, create, edit, delete, and start.

Current layout:

- Full-screen light mint background.
- Top header with app logo, title `Exercises`, count text, and a settings icon button.
- Search pill below the header.
- Vertical list of exercise cards.
- Floating action button at bottom-right labeled `Create`.
- Bottom navigation bar.

Exercise card:

- White rounded card with thin border.
- Left circular avatar showing first letter of exercise name.
- Exercise name in bold.
- Supporting metadata line, such as total duration or recent start info.
- Small chips for step count and rep count.
- Text button `Edit`.
- Red delete icon button.
- Colored `Start` button with play icon.

Empty/loading states:

- White rounded card.
- Small teal/search icon circle.
- Title and message.

## Screen: Recent

Purpose: show exercises recently started.

Current layout is similar to Home:

- Title `Recent`.
- Supporting text showing loading, empty state, or number recently started.
- Search pill.
- Same exercise cards.
- Bottom navigation bar.

No floating create button on Recent.

## Screen: Settings

Current status: placeholder only.

Current layout:

- Centered title `Settings`.
- Message: `Voice and feedback settings are saved per exercise.`
- Bottom navigation bar.

A redesign could either keep this minimal or create a real settings screen if the product direction calls for it.

## Screen: Create / Edit Exercise

Purpose: build a guided exercise flow.

Current layout:

- Light mint background.
- Top bar with back icon, title `Create Exercise` or `Edit Exercise`, subtitle `Build a guided flow`, and teal gradient `Save` pill.
- Scrollable form.

Sections:

- Exercise details card.
- Validation error panel.
- Steps section.
- Add step button.
- Session section.
- Session settings card.

Exercise details card:

- White rounded card.
- Label `Exercise name`.
- Large rounded text field.
- Chips for number of steps, reps, and total duration.

Step card:

- Rounded card with a light tint based on the step color.
- Circular step number.
- Step name field.
- Duration number field with seconds suffix.
- Delete icon.
- Voice/count controls in a nested translucent panel.
- Switches for `Voice` and `Count`.
- If counting is enabled, a `Count every` interval field appears.

Session settings:

- White rounded card with divider lines.
- Rows for:
  - Repetitions stepper.
  - Start countdown stepper.
  - Count start aloud switch.
  - Start count interval stepper.

Validation:

- Coral-tinted error panel with a list of validation messages.

## Screen: Active Timer

Purpose: run an exercise session with highly readable progress and controls.

Current layout:

- Light mint background.
- Top bar with back button, centered exercise name, planned total duration, and a voice icon.
- Summary row with two chips: current rep and current step/status.
- Large central timer focus card.
- Horizontal step preview row.
- Bottom controls.
- Red `Stop session` text button.

Timer focus card:

- White rounded card with a subtle shadow and border.
- Step/status pill near top.
- Large circular progress ring.
- Large remaining seconds text in the center.
- `seconds` label under the number.
- Instructional message under the ring:
  - Preparing: `Get comfortable. First rep starts soon.`
  - Running: `Follow the voice cue and keep your movement steady.`
  - Paused: `Paused`

Timer ring:

- Partial circular arc, not a full 360-degree circle.
- Inactive arc uses pale mint.
- Active arc uses sweep gradient from current step accent to dark teal and back.
- Faint animated halo while running.

Step preview row:

- Horizontal scroll.
- Each step is a small rounded tile.
- Active step becomes wider, tinted, and shows a check icon.
- Each tile shows index, name, and duration.

Controls:

- Left soft button: `Start Now` during preparation, otherwise `Skip Step`.
- Center large circular teal gradient play/pause button.
- Right soft button: `Skip Rep`, disabled while preparing.
- Stop session button below, coral colored.

Important redesign note: this screen is the most critical. It must be readable during movement, with large touch targets, strong visual hierarchy, and low cognitive load.

## Screen: Completion

Purpose: celebrate a finished exercise and offer next actions.

Current layout:

- Light mint background.
- Large green success check graphic.
- Heading `Well Done`.
- Subtitle `Exercise completed.`
- Two stat cards:
  - Reps
  - Duration
- Bottom primary teal gradient `Done` button.
- Secondary outlined `Do Again` button.

Success graphic:

- Concentric mint circles.
- Green gradient circle.
- White check icon.

## Shared Components

Recurring UI patterns:

- `TealIconButton`: 40dp rounded square, elevated mint background, border, dark icon.
- `SoftControlButton`: 52dp rounded rectangle, pale background, icon and text.
- `CompactTextField`: 40dp rounded input.
- `CompactNumberField`: 40dp rounded number input with optional icon and suffix.
- `SmallSwitch`: Material switch with accent-colored checked track.
- `Stepper`: minus and plus icons around a numeric value.
- `MiniChip`: tiny rounded pill with tinted background.
- `WellnessCard`: white rounded 20dp card with border and 16dp padding.
- `PrimaryPillButton`: teal gradient rounded button.

## Existing Assets

The app includes:

- App logo: `physio_timer_logo.png`
- Icons for audio lines, stop, count, delete, history, pause, play, search, settings, skip forward, success check, and timer.
- Voice/audio assets for cues such as Lift, Hold, Relax, Breathe, Rest, Go, Paused, Resuming, Well Done, and count numbers.

## Product Personality

Desired tone:

- Calm.
- Reassuring.
- Clinical enough to feel trustworthy.
- Friendly enough for daily home exercise.
- Focused on recovery and consistency, not fitness hype.

Avoid:

- Busy dashboards.
- Tiny tap targets.
- Overly playful visuals.
- Dark, high-contrast workout/gym styling.
- Generic meditation app styling.
- Excessive cards inside cards.

## Redesign Goals

Use this as the redesign brief:

- Make the UI feel like a polished physiotherapy companion, not a generic timer.
- Keep the active timer screen extremely legible and touch-friendly.
- Improve hierarchy on Home and Recent so the main action, `Start`, is obvious.
- Make editing steps feel lighter and less cramped.
- Create a more intentional visual system for step colors and progress.
- Preserve the core data model and current workflows unless explicitly changing UX.
- Prefer stable layouts that work on small Android phones.
- Keep bottom navigation simple.
- Give Settings a real purpose or remove/de-emphasize it.
- Reduce visual repetition where every element looks like a rounded card.

## Implementation Context

Technology:

- Android native app.
- Kotlin.
- Jetpack Compose.
- Material 3 components mixed with custom Compose components.

Important files:

- `app/src/main/java/com/niranjan/physiotimer/ui/DesignTokens.kt`
- `app/src/main/java/com/niranjan/physiotimer/ui/HomeScreen.kt`
- `app/src/main/java/com/niranjan/physiotimer/ui/EditExerciseScreen.kt`
- `app/src/main/java/com/niranjan/physiotimer/ui/ActiveTimerScreen.kt`
- `app/src/main/java/com/niranjan/physiotimer/ui/CompletionScreen.kt`
- `app/src/main/java/com/niranjan/physiotimer/ui/SharedComponents.kt`
- `app/src/main/java/com/niranjan/physiotimer/ui/PhysioRepTimerApp.kt`
- `app/src/main/java/com/niranjan/physiotimer/data/ExerciseModels.kt`

## Suggested Prompt For Redesign LLM

Redesign the PhysioTimer Jetpack Compose UI using the context in this document. Keep the app's existing workflows: Home, Recent, Settings, Create/Edit Exercise, Active Timer, and Completion. Focus especially on improving the Active Timer screen for physiotherapy use: large readable timer, clear current step, clear rep/step progress, safe touch targets, and calm visual feedback. Produce a cohesive design system with colors, typography, spacing, components, and per-screen Compose implementation guidance. Keep the tone calm, clinical, reassuring, and modern.
