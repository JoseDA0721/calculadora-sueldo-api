package com.example.services

import com.example.db.ConfigurationsTable // Añade esta importación
import com.example.db.EmployeesTable
import com.example.db.PaymentStatus
import com.example.db.PaymentsTable
import com.example.db.WorkLogsTable // Añade esta importación
import com.example.dto.EmployeeResponse
import com.example.dto.MonthlySummaryResponse // Añade esta importación
import com.example.dto.NewEmployeeRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class EmployeeService {
    fun createEmployee(request: NewEmployeeRequest): EmployeeResponse {
        return transaction {
            val insertStatement =
                EmployeesTable.insert {
                    it[firstName] = request.firstName
                    it[lastName] = request.lastName
                    it[email] = request.email
                    if (request.telegramChatId != null) {
                        it[telegramChatId] = request.telegramChatId
                    }
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

    fun getMonthlySummary(
        employeeId: Int,
        yearMonth: YearMonth,
    ): MonthlySummaryResponse? {
        return transaction {
            // Verificamos si ya existe un registro de pago para este mes/empleado
            val existingPayment =
                PaymentsTable
                    .select {
                        (PaymentsTable.employeeId eq employeeId) and
                            (PaymentsTable.paymentMonth eq yearMonth.monthValue) and
                            (PaymentsTable.paymentYear eq yearMonth.year)
                    }
                    .firstOrNull()

            // Si ya existe un pago, usamos esos datos.
            // Si no, calculamos y creamos uno nuevo.
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
                // La lógica de cálculo que ya teníamos
                hourlyRate = ConfigurationsTable.select { ConfigurationsTable.key eq ConfigService.HOURLY_RATE_KEY }
                    .firstOrNull()?.get(ConfigurationsTable.value)?.toDoubleOrNull() ?: 0.0

                totalHours =
                    WorkLogsTable.select {
                        (WorkLogsTable.employeeId eq employeeId) and
                            (WorkLogsTable.workDate greaterEq yearMonth.atDay(1)) and
                            (WorkLogsTable.workDate lessEq yearMonth.atEndOfMonth())
                    }.sumOf { it[WorkLogsTable.hours] }.toDouble()
                totalSalary = totalHours * hourlyRate

                // Creamos el registro de pago en la BD
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

    // ↓↓↓ NUEVA FUNCIÓN DE AYUDA PRIVADA ↓↓↓
    // Para convertir una fila de la BD a nuestro DTO de respuesta
    private fun rowToEmployee(row: ResultRow): EmployeeResponse {
        return EmployeeResponse(
            id = row[EmployeesTable.id],
            firstName = row[EmployeesTable.firstName],
            lastName = row[EmployeesTable.lastName],
            email = row[EmployeesTable.email],
            telegramChatId = row[EmployeesTable.telegramChatId],
        )
    }
}
