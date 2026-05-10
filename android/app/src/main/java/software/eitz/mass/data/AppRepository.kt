package software.eitz.mass.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppRepository(private val context: Context) {

    fun getAssignmentFlow(tileId: String): Flow<String?> {
        val key = stringPreferencesKey("tile_$tileId")
        return context.dataStore.data.map { preferences ->
            preferences[key]
        }
    }

    suspend fun saveAssignment(tileId: String, packageName: String) {
        val key = stringPreferencesKey("tile_$tileId")
        context.dataStore.edit { preferences ->
            preferences[key] = packageName
        }
    }

    suspend fun clearAssignment(tileId: String) {
        val key = stringPreferencesKey("tile_$tileId")
        context.dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}
