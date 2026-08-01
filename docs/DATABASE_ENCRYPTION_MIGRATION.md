# Database encryption migration notes

## What changed

`FinoraDatabase` now opens its Room database through SQLCipher
(`net.sqlcipher.database.SupportFactory`) instead of plain `SupportSQLiteOpenHelper`. The
database file on disk is now a full-database AES-256 encrypted SQLCipher file rather than
plaintext SQLite.

The encryption passphrase is:
- A random 256-bit value generated once per install (`DatabasePassphraseProvider`).
- Never hardcoded, never logged, never transmitted.
- Stored in `EncryptedSharedPreferences`, itself encrypted with a key held in the Android
  Keystore (hardware-backed on devices that support it) via `MasterKey`.

## Why the database file was renamed

The Room `@Database(version = 1, ...)` schema version **did not change** - this is not a Room
schema migration. It's a storage-engine change: SQLCipher cannot open a file that was written
by plain SQLite (different file header/format), and there is no reasonable in-place way to
"migrate" an existing plaintext SQLite file into a SQLCipher file without a decrypt-then-encrypt
rewrite step.

Given the app has not yet shipped a production release (`versionCode = 1`, never published),
there is no real user data to preserve, so the simplest and safest option was taken:

- Old file: `finora_database` (plaintext SQLite)
- New file: `finora_database_encrypted.db` (SQLCipher)

Any existing debug/dev install will simply start a **fresh, empty, encrypted database** the
first time it runs this build - the old plaintext file is left untouched on disk (orphaned, not
deleted) and is no longer referenced by the app.

## If this needs to ship to users with existing data in the future

If a plaintext release ever does ship and later needs to move to this encrypted scheme with
data preservation, do **not** reuse this change as-is. Instead:

1. On first run of the new version, detect the old `finora_database` file still exists.
2. Open it read-only with plain `SupportSQLiteOpenHelper` (no SQLCipher).
3. Open/create the new encrypted `finora_database_encrypted.db` via the `SupportFactory` path
   already in place.
4. Copy all rows table-by-table (DAOs already expose everything needed).
5. Verify the copy, then delete the old plaintext file.

This was **not implemented** here because there is no production data to migrate yet; adding
this dead code path now would be unverifiable and unnecessary risk.

## Room schema migrations (unrelated, still apply normally)

`fallbackToDestructiveMigration()` is unchanged and still governs ordinary Room schema-version
bumps (adding tables/columns, etc.) going forward - that behavior is independent of the
SQLCipher change above. Once this app has real production users, replace
`fallbackToDestructiveMigration()` with explicit `Migration` objects before shipping any schema
change, so user data isn't wiped on upgrade.

## Files changed

- `app/src/main/java/com/example/data/FinoraDatabase.kt` - opens the DB via SQLCipher's
  `SupportFactory`; renamed database file.
- `app/src/main/java/com/example/security/DatabasePassphraseProvider.kt` - new file; generates/
  stores the passphrase.
- `app/build.gradle.kts` - added `net.zetetic:sqlcipher-android`, `androidx.sqlite:sqlite`,
  `androidx.security:security-crypto` dependencies.
- `gradle/libs.versions.toml` - added the corresponding version catalog entries.
