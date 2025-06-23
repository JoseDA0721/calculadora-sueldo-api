package com.example.services

import com.example.db.ContractsTable
import com.example.db.WorkLogsTable
import com.example.dto.NewWorkLogRequest
import com.example.dto.WorkLogResponse
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.YearMonth

class WorkLogService {
    fun createWorkLog(request: NewWorkLogRequest): WorkLogResponse {
        return transaction {
            val contractID =
                ContractsTable
                    .slice(ContractsTable.id)
                    .select {
                        (ContractsTable.employeeId eq request.employeeId) and
                            (ContractsTable.active eq true)
                    }
                    .map { it[ContractsTable.id] }
                    .first()
            println("ID del contrato: " + contractID)
            val newWorkLog =
                WorkLogsTable
                    .insert {
                        it[contractId] = contractID.toInt()
                        it[date] = LocalDate.now()
                        it[hoursWorked] = request.hoursWorked.toBigDecimal()
                    }.resultedValues!!.first()
            rowToWorkLog(newWorkLog)
        }
    }

    fun getTotalHoursForPeriod(
        contractId: Int,
        period: String,
    ): Double {
        val yearMonth = YearMonth.parse(period)
        val startDay = yearMonth.atDay(1)
        val endDay = yearMonth.atEndOfMonth()
        var totalHours = 0.0
        return transaction {
            val workLogs =
                WorkLogsTable
                    .select {
                        (WorkLogsTable.contractId eq contractId) and
                            (WorkLogsTable.date.between(startDay, endDay))
                    }.map { rowToWorkLog(it) }
            for (workLog in workLogs) {
                totalHours += workLog.hoursWorked
            }
            totalHours
        }
    }

    private fun rowToWorkLog(row: ResultRow): WorkLogResponse {
        return WorkLogResponse(
            id = row[WorkLogsTable.id],
            contractId = row[WorkLogsTable.contractId],
            date = row[WorkLogsTable.date].toKotlinLocalDate(),
            hoursWorked = row[WorkLogsTable.hoursWorked].toDouble(),
        )
    }
}
