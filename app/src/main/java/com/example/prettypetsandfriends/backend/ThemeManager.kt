import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

object ThemeManager {
    private val THEME_KEY = stringPreferencesKey("app_theme")

    fun getThemeFlow(context: Context): Flow<AppTheme> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[THEME_KEY]?.let { AppTheme.valueOf(it) } ?: AppTheme.SYSTEM
        }

    suspend fun saveTheme(context: Context, theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
}

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}