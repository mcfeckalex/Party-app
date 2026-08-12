# Smutten

An Android app that guides players through **Smutten**, a precision drinking
game played with glasses, drinks, and a kitchen/bar scale.

## The rules

> Weigh your drink. Roll 5 dice, add up the number of dots, and drink that
> many grams from your drink. All players drink. Weigh your drink again to
> check the result. Best of three. The loser has to finish their drink!

The original rules don't spell out exactly how a round is "won" or "lost,"
or how "best of three" resolves to a single loser, so this app makes the
following (adjustable) judgment calls:

- **Round loss:** each round, whoever's actual grams-drunk (weight before −
  weight after) deviates the most from the dice total loses that round.
  Ties (within 0.5 g) share the loss.
- **Overall loser:** after 3 rounds, whoever lost the most rounds is the
  overall loser. If players are tied on round losses, the tiebreaker is
  total deviation (in grams) summed across all 3 rounds — whoever was
  furthest off target overall loses the tiebreak.
- **Device setup:** one shared phone, passed around. The app walks through
  players one at a time for each weigh-in ("pass-and-play"), so no
  networking or second device is required.
- **Weighing:** entirely manual entry. Players read the grams off their own
  scale and type it in — there's no Bluetooth/scale integration.

If any of these interpretations don't match how you actually play, the
scoring logic lives in one place: `GameViewModel.finishRound()` and
`GameViewModel.determineOverallLosers()`.

## How a game flows

1. **Home** — rules summary, "New Game".
2. **Player setup** — add 2+ player names.
3. Each of 3 rounds:
   - **Roll 5 dice** (animated) — the pip total is the round's gram target.
   - **Weigh in** — each player enters their drink's starting weight.
   - **Drink** — a prompt to drink, aiming for the target.
   - **Weigh in again** — each player enters their drink's ending weight.
   - **Round result** — grams consumed, deviation from target, and who lost
     the round.
4. **Game over** — the overall loser(s) are shown, along with each player's
   round-loss tally. Options to replay with the same players or start over.

## Project structure

Standard single-module Android app, Kotlin + Jetpack Compose (Material 3),
no external backend:

```
app/src/main/java/com/smutten/app/
├── MainActivity.kt
├── model/GameModels.kt        # Player, GamePhase, RoundResult, GameUiState
├── viewmodel/GameViewModel.kt # all game/scoring logic
├── navigation/SmuttenNavHost.kt
└── ui/
    ├── theme/                 # Material 3 theme + colors
    ├── components/DiceFace.kt # canvas-drawn dice with pips
    └── screens/                # Home, PlayerSetup, Game (all round phases),
                                 # RoundResult, GameOver
```

## Building

Requires an installed Android SDK (Android Studio's SDK manager is the
easiest way to get one) with `compileSdk 34` / `minSdk 26` available.

```bash
export ANDROID_HOME=/path/to/your/android-sdk   # or set local.properties
./gradlew assembleDebug
./gradlew installDebug   # with a device/emulator connected
```

Or just open the project root in Android Studio and run it — no manual
configuration needed beyond having the SDK installed.

> Note: this environment doesn't have the Android SDK installed, so the
> Gradle build itself hasn't been run here — the code has been written and
> reviewed carefully, but give `./gradlew assembleDebug` a run locally
> before you rely on it.

## CI / Releases

Two GitHub Actions workflows build the APK in CI (where a full Android SDK
is available):

- **`.github/workflows/build.yml`** — runs on every push to `main` /
  `claude/**` and on pull requests. Builds a debug APK and uploads it as a
  workflow artifact (Actions tab → the run → Artifacts).
- **`.github/workflows/release.yml`** — runs when you push a tag like
  `v1.0.0` (or trigger it manually from the Actions tab). Builds the debug
  APK and publishes it as a downloadable asset on a GitHub Release.

Both build the **debug** variant, which is auto-signed with the standard
Android debug keystore — installable directly on a phone (enable "install
unknown apps" for whatever app you use to open the file) without needing to
set up release signing/keystore secrets. To cut a new release:

```bash
git tag v1.0.0
git push origin v1.0.0
```
