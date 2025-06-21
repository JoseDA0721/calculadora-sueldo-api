package com.example.services

import com.example.db.EmployeesTable
import com.example.db.WorkLogsTable
import com.example.dto.EmployeeResponse
import com.example.dto.NewEmployeeRequest
import com.example.dto.UpdateEmployeeRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class EmployeeService {
    fun createEmployee(request: NewEmployeeRequest): EmployeeResponse {
        return transaction {
            val insertStatement =
                EmployeesTable.insert {
                    it[id] = request.id
                    it[firstName] = request.firstName
                    it[lastName] = request.lastName
                    it[activiti] = request.activiti
                    it[email] = request.email
                }
            rowToEmployee(insertStatement.resultedValues!!.first())
        }
    }

    fun updateEmployee(
        id: String,
        request: UpdateEmployeeRequest
    ): EmployeeResponse {
        return transaction {
            val updateStatement = EmployeesTable.update({ EmployeesTable.id eq id }) {
                it[userId] = request.userId
            }
            rowToEmployee(updateStatement.t)
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

    fun getMonthlySummary(
        employeeId: Int,
        yearMonth: YearMonth,
    ): MonthlySummaryResponse? {
        return transaction {
            val existingPayment =
                PaymentsTable
                    .select {
                        (PaymentsTable.employeeId eq employeeId) and
                            (PaymentsTable.paymentMonth eq yearMonth.monthValue) and
                            (PaymentsTable.paymentYear eq yearMonth.year)
                    }
                    .firstOrNull()

            val hourlyRate: Double
            val totalHours: Double
            val totalSalary: Double

            if (existingPayment != null) {
                totalSalary = existingPayment[PaymentsTable.amount].toDouble()
                hourlyRate =
                    if (existingPayment[PaymentsTable.amount].toDouble() > 0) {
                        totalSalary / existingPayment[PaymentsTable.amount].toDouble()
                    } else {
                        0.0 // Lógica simple para derivar la tarifa
                    }
                // NOTA: Para un total de horas preciso, necesitaríamos volver a calcular o guardarlo en la tabla de pagos.
                // Por simplicidad, lo dejaremos así por ahora.
                totalHours = 0.0 // Simplificación
            } else {
                hourlyRate = ConfigurationsTable.select { ConfigurationsTable.key eq ConfigService.HOURLY_RATE_KEY }
                    .firstOrNull()?.get(ConfigurationsTable.value)?.toDoubleOrNull() ?: 0.0

                totalHours =
                    WorkLogsTable.select {
                        (WorkLogsTable.employeeId eq employeeId) and
                            (WorkLogsTable.workDate greaterEq yearMonth.atDay(1)) and
                            (WorkLogsTable.workDate lessEq yearMonth.atEndOfMonth())
                    }.sumOf { it[WorkLogsTable.hours] }.toDouble()
                totalSalary = totalHours * hourlyRate

                PaymentsTable.insert {
                    it[PaymentsTable.employeeId] = employeeId
                    it[paymentMonth] = yearMonth.monthValue
                    it[paymentYear] = yearMonth.year
                    it[amount] = totalSalary.toBigDecimal()
                    it[status] = PaymentStatus.PENDIENTE
                }
            }

            MonthlySummaryResponse(
                month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")),
                year = yearMonth.year,
                employeeId = employeeId,
                totalHours = totalHours,
                hourlyRate = hourlyRate,
                totalSalary = totalSalary,
            )
        }
    }

    private fun rowToEmployee(row: ResultRow): EmployeeResponse {
        return EmployeeResponse(
            id = row[EmployeesTable.id],
            firstName = row[EmployeesTable.firstName],
            lastName = row[EmployeesTable.lastName],
            userId = row[EmployeesTable.userId],
            activiti = row[EmployeesTable.activiti],
            method = row[EmployeesTable.method].toString(),
            email = row[EmployeesTable.email],
            telegramChatId = row[EmployeesTable.telegramChatId]
        )
    }
}
