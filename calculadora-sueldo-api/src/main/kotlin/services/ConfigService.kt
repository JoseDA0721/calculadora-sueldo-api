package com.example.services

import com.example.db.ConfigurationsTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

class ConfigService {
    companion object {
        const val HOURLY_RATE_KEY = "hourly_rate"
    }

    fun updateHourlyRate(rate: Double) {
        transaction {
            // Upsert es la forma perfecta de 'insertar o actualizar'.
            // Buscará una fila con la 'key' que le damos. Si la encuentra,
            // actualiza el 'value'. Si no, crea una nueva fila.
            ConfigurationsTable.upsert(ConfigurationsTable.key) {
                it[key] = HOURLY_RATE_KEY
                it[value] = rate.toString()
            }
        }
    }
}
