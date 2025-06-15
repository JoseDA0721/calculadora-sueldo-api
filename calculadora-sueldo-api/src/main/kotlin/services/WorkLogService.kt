package com.example.services

import com.example.db.WorkLogsTable
import com.example.dto.NewWorkLogRequest
import com.example.dto.WorkLogResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

class WorkLogService {

    fun create(request: NewWorkLogRequest): WorkLogResponse {
        val createdLog = transaction {
            val insertStatement = WorkLogsTable.insert {
                it[employeeId] = request.employeeId
                it[workDate] = LocalDate.parse(request.date) // Convertimos el String a fecha
                it[hours] = request.hours.toBigDecimal()
            }
            insertStatement.resultedValues!!.first()
        }

        return WorkLogResponse(
            id = createdLog[WorkLogsTable.id],
            employeeId = createdLog[WorkLogsTable.employeeId],
            date = createdLog[WorkLogsTable.workDate].toString(),
            hours = createdLog[WorkLogsTable.hours].toDouble()
        )
    }
}