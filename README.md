# JE Mastery 360 - Native Android App (v2)

Kotlin + Jetpack Compose app for the JE Mastery 360 web app (UKPSC JE Civil Engineering).
Reads the same Firebase project (`je-mastery-360`) that the website and admin panel use.

| | |
|---|---|
| App name | JE Mastery 360 |
| Package | `com.jemastery.app` (matches your google-services.json) |
| Min Android | 7.0 (API 24) |
| Target | Android 14 (API 34) |
| Version | 2.0.0 |

## What is new in v2

**Login** - white screen like the website, big "JE MASTERY 360" title, subtitle,
gradient Google button with the Google G, guest button with a person icon,
Terms / Privacy / Disclaimer links.

**Tiers** - FREE = guest login, PRO = Google login, PRO+ = Google login + isPremium.

**Library** - logo + title bar with dark-mode toggle and tier pill, search box
(quizzes, folders), breadcrumb, 2-column folder grid, locked folders are blurred
with a lock and a PRO / PRO+ chip.

**Quiz cards** - access chip (FREE / PRO / PRO+), timer, question count, section
count, Start / Locked button, Leaderboard / Attempts / Share row.

**Section picker** - multi-section quizzes show "Select Sections" with
"Q.1 to Q.80 (80 Qs)" rows and checkboxes, exactly like the site.

**Mode selector** - ratings card (Quiz Level / Content Quality) when available,
red Start Exam Mode, outlined Start Practice Mode.

**Quiz player**
- practice mode: language dropdown (English / हिंदी / Bilingual), A- / A+ font size,
  Auto ON/OFF auto-next, flag, PYQ badge (blue in practice, red in exam),
  Q-List, Next / Skip with "Auto scrolling On"
- exam mode: countdown timer, palette, mark for review

**Result** - score circle, accuracy, Correct / Wrong / Skipped / Time cards,
Detailed Solution Review with filters (all / correct / wrong / skipped),
AI Solution button (copies the prompt and opens Perplexity), share result.

**Profile** - avatar, @username, email, CURRENT TIER card with validity days,
performance dashboard, background presets + dark mode, Telegram group, log out.

**Payments** - manual UPI flow: Rs.29 / Rs.79 (popular) / Rs.149 plans and a
"Message Admin (@IS1343)" Telegram button.

**Back button** - goes folder by folder, quiz exit asks for confirmation,
result returns to the library.

## Not in this build yet

Leaderboard data, consolidated "attempted / wrong / marked" lists, custom practice
from wrong answers, quiz rating submission, ads, FCM push, offline caching.

## Build the APK

### Option 1 - GitHub Actions (easiest, nothing to install)
1. Create a new repository on GitHub.
2. Upload this whole folder (drag and drop on the repo page).
3. Open the **Actions** tab, click **Build APK**, then **Run workflow**.
4. After ~4 minutes open the finished run and download **JE-Mastery-360-APK**.
5. Install that APK on your phone.

The workflow is already included in `.github/workflows/build-apk.yml`.

### Option 2 - Android Studio
Open the folder, wait for Gradle sync, press Run.

## Google Sign-In

Add this SHA-1 to Firebase -> Project settings -> `com.jemastery.app` -> Add fingerprint:

```
55:0B:75:6A:BD:B6:5C:DF:1D:6E:DF:53:1D:99:A8:04:2A:BA:4A:A8
```

Guest login works without it.

## Data used (Firestore REST, same rules as the website)

```
folders                  name, icon, parentId, accessType
quizzes                  folderId, title, sequence, accessType, timeMinutes, totalQs,
                         isMigrated, textData, sections[], telegramCode
quiz_contents/{quizId}   textData   (when isMigrated = true)
users/{uid}              name, username, email, isPremium, proExpiresAt
users/{uid}/history      quizId, quizTitle, correct, wrong, skipped, total, accuracy,
                         timeTaken, date
app_settings/main_offer  isActive, title, message
```
