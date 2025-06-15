package com.example.services

import com.example.db.EmployeesTable
import com.example.dto.EmployeeResponse
import com.example.dto.NewEmployeeRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class EmployeeService {

    fun createEmployee(request: NewEmployeeRequest): EmployeeResponse {
        return transaction {
            val insertStatement = EmployeesTable.insert {
                it[firstName] = request.firstName
                it[lastName] = request.lastName
                it[email] = request.email
            }
            // Usamos la nueva función de ayuda para mantener el código limpio
            rowToEmployee(insertStatement.resultedValues!!.first())
        }
    }

    // ↓↓↓ NUEVA FUNCIÓN PÚBLICA ↓↓↓
    fun findById(id: Int): EmployeeResponse? {
        return transaction {
            EmployeesTable
                .select { EmployeesTable.id eq id }
                .map { rowToEmployee(it) } // Reutilizamos la función de ayuda
                .firstOrNull() // Devuelve el primer resultado o null si no se encuentra
        }
    }

    // ↓↓↓ NUEVA FUNCIÓN DE AYUDA PRIVADA ↓↓↓
    // Para convertir una fila de la BD a nuestro DTO de respuesta
    private fun rowToEmployee(row: ResultRow): EmployeeResponse {
        return EmployeeResponse(
            id = row[EmployeesTable.id],
            firstName = row[EmployeesTable.firstName],
            lastName = row[EmployeesTable.lastName],
            email = row[EmployeesTable.email]
        )
    }
}