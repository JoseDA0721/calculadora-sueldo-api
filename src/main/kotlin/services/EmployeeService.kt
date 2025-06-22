package com.example.services

import com.example.db.EmployeesTable
import com.example.db.PaymentMethods
import com.example.db.UsersTable
import com.example.dto.EmployeeResponse
import com.example.dto.NewEmployeeRequest
import com.example.dto.UpdateEmployeeRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt

class EmployeeService {
    fun createEmployee(request: NewEmployeeRequest): EmployeeResponse {
        return transaction {
            val newUser =
                UsersTable.insert {
                    it[roleId] = if (request.activity.equals("admin", ignoreCase = true)) 1 else 2
                    it[name] = request.id
                    val hashedPassword = BCrypt.hashpw(request.id, BCrypt.gensalt())
                    it[password] = hashedPassword
                }[UsersTable.id]
            val newEmployee =
                EmployeesTable.insert {
                    it[id] = request.id
                    it[firstName] = request.firstName
                    it[lastName] = request.lastName
                    it[userId] = newUser
                    it[activity] = request.activity
                    it[method] = PaymentMethods.TRANSFERENCIA
                    it[email] = request.email
                    it[telephone] = "" // Deberías considerar añadir esto al DTO también
                    it[telegramChatId] = 0 // Y esto
                }.resultedValues!!.first()

            rowToEmployee(newEmployee)
        }
    }

    fun updateEmployee(
        id: String,
        request: UpdateEmployeeRequest,
    ): EmployeeResponse? {
        return transaction {
            val updateStatement =
                EmployeesTable.update({ EmployeesTable.id eq id }) {
                        statements ->
                    request.activity?.let { newActivity -> statements[activity] = newActivity }
                    request.method?.let { newMethod -> statements[method] = newMethod }
                    request.email?.let { newEmail -> statements[email] = newEmail }
                    request.telephone?.let { newTelephone -> statements[telephone] = newTelephone }
                    request.telegramChatId?.let { newTelegramChatId -> statements[telegramChatId] = newTelegramChatId }
                }
            findById(id)
        }
    }

    fun findById(id: String): EmployeeResponse? {
        return transaction {
            EmployeesTable
                .select { EmployeesTable.id eq id }
                .map { rowToEmployee(it) }
                .firstOrNull()
        }
    }

    private fun rowToEmployee(row: ResultRow): EmployeeResponse {
        return EmployeeResponse(
            id = row[EmployeesTable.id],
            firstName = row[EmployeesTable.firstName],
            lastName = row[EmployeesTable.lastName],
            userId = row[EmployeesTable.userId],
            activity = row[EmployeesTable.activity],
            method = row[EmployeesTable.method],
            email = row[EmployeesTable.email],
            telegramChatId = row[EmployeesTable.telegramChatId],
        )
    }
}
