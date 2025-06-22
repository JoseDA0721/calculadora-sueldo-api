package com.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        // Lee la configuración de la base de datos desde application.yaml
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

        Database.connect(dataSource)

        val flyway =
            Flyway.configure()
                .dataSource(dataSource)
                .load()

        flyway.migrate()

        println("Database initialized and tables created successfully.")
    }
}
