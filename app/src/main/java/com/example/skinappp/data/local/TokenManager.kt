package com.example.skinappp.data.local

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserPreferencesManager — single source of truth for all user-related
 * data stored in EncryptedSharedPreferences.
 *
 * Each field is exposed as a Kotlin property:
 *   - Getter reads directly from SharedPreferences.
 *   - Setter writes atomically (apply()).
 *
 * Atomic bulk writes (e.g. on sign-in) use [saveAuthResponse].
 */
@Singleton
class UserPreferencesManager @Inject constructor(
    private val prefs: SharedPreferences
) {

    // ─── Keys ────────────────────────────────────────────────────────────────
    companion object {
        private const val KEY_JWT_TOKEN     = "jwt_token"
        private const val KEY_USER_ID       = "user_id"
        private const val KEY_EMAIL         = "user_email"
        private const val KEY_DISPLAY_NAME  = "display_name"   // e.g. from Google profile
        private const val KEY_AUTH_PROVIDER = "auth_provider"  // "EMAIL" | "GOOGLE"
        private const val KEY_MEMBER_SINCE  = "member_since"   // ISO date string
    }

    // ─── Auth token ──────────────────────────────────────────────────────────

    var jwtToken: String?
        get() = prefs.getString(KEY_JWT_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_JWT_TOKEN, value).apply()

    // ─── Identity ────────────────────────────────────────────────────────────

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var email: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    /**
     * Human-readable display name.
     * Populated from:
     *  - [saveAuthResponse] fullName param for email/password sign-up.
     *  - [saveGoogleProfile] for Google OAuth (uses GoogleSignInAccount.displayName).
     */
    var displayName: String?
        get() = prefs.getString(KEY_DISPLAY_NAME, null)
        set(value) = prefs.edit().putString(KEY_DISPLAY_NAME, value).apply()

    var authProvider: String?
        get() = prefs.getString(KEY_AUTH_PROVIDER, null)
        set(value) = prefs.edit().putString(KEY_AUTH_PROVIDER, value).apply()

    var memberSince: String?
        get() = prefs.getString(KEY_MEMBER_SINCE, null)
        set(value) = prefs.edit().putString(KEY_MEMBER_SINCE, value).apply()

    // ─── Computed ────────────────────────────────────────────────────────────

    val isLoggedIn: Boolean get() = jwtToken != null

    // ─── Bulk writes ─────────────────────────────────────────────────────────

    /**
     * Atomically saves all auth data returned by the backend.
     * Call this after a successful email/password login or register.
     * Optionally pass [displayName] for registrations where the user typed their name.
     */
    fun saveAuthResponse(
        token: String,
        userId: String,
        email: String,
        displayName: String? = null,
        provider: String = "EMAIL"
    ) {
        prefs.edit()
            .putString(KEY_JWT_TOKEN, token)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .apply { if (displayName != null) putString(KEY_DISPLAY_NAME, displayName) }
            .putString(KEY_AUTH_PROVIDER, provider)
            .also { editor ->
                if (memberSince == null) {
                    editor.putString(KEY_MEMBER_SINCE, java.time.LocalDate.now().toString())
                }
            }
            .apply()
    }

    /**
     * Saves the Google account's display name and photo after a successful
     * Google OAuth sign-in. Call this in addition to [saveAuthResponse].
     *
     * @param displayName  GoogleSignInAccount.displayName (e.g. "Sarah Williams")
     * @param givenName    GoogleSignInAccount.givenName   (first name fallback)
     */
    fun saveGoogleProfile(displayName: String?, givenName: String?) {
        val name = displayName ?: givenName ?: return
        prefs.edit()
            .putString(KEY_DISPLAY_NAME, name)
            .putString(KEY_AUTH_PROVIDER, "GOOGLE")
            .also { editor ->
                if (memberSince == null) {
                    editor.putString(KEY_MEMBER_SINCE, java.time.LocalDate.now().toString())
                }
            }
            .apply()
    }

    // ─── Backward-compat helpers (used by legacy callers) ────────────────────

    fun getToken(): String? = jwtToken

    @JvmName("getUserIdHelper")
    fun getUserId(): String? = userId
    fun getFullName(): String? = displayName

    // ─── Logout ──────────────────────────────────────────────────────────────

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}

/**
 * Backward-compatibility alias.
 * New code should inject [UserPreferencesManager] directly.
 * Existing callers that still inject [TokenManager] continue to compile.
 */
typealias TokenManager = UserPreferencesManager

