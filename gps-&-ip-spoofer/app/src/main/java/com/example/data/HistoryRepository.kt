package com.example.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: HistoryDao) {
    val allHistory: Flow<List<LocationIpHistory>> = dao.getAllHistory()

    suspend fun insert(history: LocationIpHistory): Long {
        dao.deactivateAll()
        return dao.insertHistory(history.copy(isActive = true))
    }

    suspend fun delete(history: LocationIpHistory) {
        dao.deleteHistory(history)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    suspend fun deactivateAll() {
        dao.deactivateAll()
    }
}
