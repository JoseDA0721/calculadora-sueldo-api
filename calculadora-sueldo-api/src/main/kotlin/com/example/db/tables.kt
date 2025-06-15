package com.example.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object EmployeesTable : Table("empleados") {
    val id = integer("id").autoIncrement()
    val firstName = varchar("first_name", 255)
    val lastName = varchar("last_name", 255)
    val email = varchar("email", 255).uniqueIndex() // El email debe ser único
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}

object ConfigurationsTable : Table("configurations") {
    val key = varchar("key", 50)
    val value = varchar("value", 255)
    override val primaryKey = PrimaryKey(key) // La 'key' es la llave primaria
}

object WorkLogsTable : Table("work_logs") {
    val id = integer("id").autoIncrement()
    // Esta es la llave foránea que conecta con la tabla de empleados
    val employeeId = integer("employee_id").references(EmployeesTable.id)
    val workDate = date("work_date")
    val hours = decimal("hours", 5, 2) // Ej: 5 dígitos en total, 2 decimales (hasta 999.99)
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}