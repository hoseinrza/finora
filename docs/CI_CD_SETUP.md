# CI/CD setup

Two GitHub Actions workflows were added:

- **`.github/workflows/pr-checks.yml`** - runs on every PR into `main`: Android Lint, debug unit
  tests, and a debug APK build. Needs no secrets.
- **`.github/workflows/release.yml`** - runs on every push to `main` (and can be triggered
  manually via "Run workflow"): builds a signed release AAB + APK. **Requires the repository
  secrets below**, or it fails fast with a clear error instead of silently producing a
  debug-signed build.

## Required repository secrets (Settings → Secrets and variables → Actions)

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | Your upload keystore file, base64-encoded (see below). |
| `KEYSTORE_PASSWORD` | The keystore's store password. |
| `KEY_ALIAS` | The signing key's alias inside the keystore. |
| `KEY_PASSWORD` | The signing key's password. |
| `GOOGLE_SERVICES_JSON_BASE64` | Optional. Your Firebase `google-services.json`, base64-encoded. Only needed if you want Crashlytics to actually report from release builds - the build succeeds without it either way. |

**Never commit the keystore file, `google-services.json`, or any of the values above.** `.gitignore`
already blocks common keystore filenames (`*.jks`, `*.keystore`, `*.p12`, `keystore.properties`,
`key.properties`) and `google-services.json` should be added there too if you add it locally for
manual testing.

## Generating a keystore (if you don't have one yet)

```bash
keytool -genkeypair -v -keystore release.keystore -alias upload \
  -keyalg RSA -keysize 2048 -validity 10000
```

Keep this file somewhere safe outside the repo (a password manager, a secrets vault) - if it's
lost, you cannot publish updates to an app already live on Play Store under the same signing key.

## Encoding secrets to base64

```bash
base64 -w 0 release.keystore > release.keystore.b64        # macOS: base64 release.keystore | tr -d '\n'
base64 -w 0 app/google-services.json > google-services.b64
```

Paste the resulting single-line string as the secret value.

## Local development

Locally, without these env vars set, `./gradlew assembleRelease` still works - it falls back to
debug signing with a build-time warning (see `app/build.gradle.kts`). That build is **not**
uploadable to Play Store; it's only useful for locally testing the release build type (R8
minification, shrinking, etc.) end to end.
