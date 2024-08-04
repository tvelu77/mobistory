package fr.uge.mobistory.localstorage

import fr.uge.mobistory.database.DatabaseManager

data class AppData(val dbManager: DatabaseManager, val appConfig: AppConfig, val appCache: AppCache)
