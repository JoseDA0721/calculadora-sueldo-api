package com.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        // Lee la configuración de la base de datos desde application.conf
        val configDriver = config.property("database.driver").getString()
        val configUrl = config.property("database.url").getString()
        val configUser = config.property("database.user").getString()
        val configPassword = config.property("database.password").getString()

        // Crea el pool de conexiones con HikariCP
        val hikariConfig =
            HikariConfig().apply {
                driverClassName = configDriver
                jdbcUrl = configUrl
                username = configUser
                password = configPassword
                maximumPoolSize = 3
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            }

        val dataSource = HikariDataSource(hikariConfig)

        // Conecta a la base de datos usando Exposed
        Database.connect(dataSource)

        // Crea las tablas si no existen.
        // Esto es muy útil para el desarrollo.
        transaction {
            SchemaUtils.drop(EmployeesTable, WorkLogsTable, ConfigurationsTable, PaymentsTable)
            SchemaUtils.create(EmployeesTable, ConfigurationsTable, WorkLogsTable, PaymentsTable)
        }

        println("Database initialized and tables created successfully.")
    }
}
