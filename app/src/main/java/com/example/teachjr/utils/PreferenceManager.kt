import android.content.Context

class PreferenceManager(
    context: Context
) {

    private var prefs = context.getSharedPreferences(PREF_USER_FILE, Context.MODE_PRIVATE)

    fun saveUsername(username : String) {
        val prefEditor = prefs.edit()
        prefEditor.putString(PREF_USERNAME_KEY, username)
        prefEditor.apply()
    }

    fun getUsername() : String? {
        // 'null' is default value here
        return prefs.getString(PREF_USERNAME_KEY, null)
    }

    fun saveEmail(email : String) {
        val prefEditor = prefs.edit()
        prefEditor.putString(PREF_EMAIL_KEY, email)
        prefEditor.apply()
    }

    fun getEmail() : String? {
        // 'null' is default value here
        return prefs.getString(PREF_EMAIL_KEY, null)
    }

    fun logoutUser() {
        prefs.edit().clear().apply()
    }

    companion object {

        const val PREF_USER_FILE = "PREF_USER_FILE"
        const val PREF_USERNAME_KEY = "PREF_USERNAME_KEY"
        const val PREF_EMAIL_KEY = "PREF_EMAIL_KEY"
    }
}