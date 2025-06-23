package com.example.services

import com.example.db.ContractsTable
import com.example.dto.ContractResponse
import com.example.dto.NewContractRequest
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDate

class ContractService {
    fun createContract(request: NewContractRequest): ContractResponse {
        return transaction {
            ContractsTable
                .update({
                    (ContractsTable.employeeId eq request.employeeId) and (ContractsTable.active eq true)
                }) {
                    it[active] = false
                    it[endDate] = LocalDate.now()
                }

            val newContract =
                ContractsTable.insert {
                    it[employeeId] = request.employeeId
                    it[hourlyRate] = request.hourlyRate.toBigDecimal()
                    it[startDate] = LocalDate.now()
                }.resultedValues!!.first()

            rowToContractResponse(newContract)
        }
    }

    fun getContractsForEmployee(employeeId: String): List<ContractResponse> {
        return transaction {
            val contractsList =
                ContractsTable
                    .select { ContractsTable.employeeId eq employeeId }
                    .map { rowToContractResponse(it) }
            contractsList
        }
    }

    fun getActiveContract(contractId: Int): ContractResponse? {
        return transaction {
            ContractsTable
                .select {
                    (ContractsTable.id eq contractId) and
                        (ContractsTable.active eq true)
                }
                .map { rowToContractResponse(it) }
                .firstOrNull()
        }
    }

    fun getContractForEmployee(employeeId: String): ContractResponse? {
        return transaction {
            ContractsTable
                .select {
                    (ContractsTable.employeeId eq employeeId)
                }
                .map { rowToContractResponse(it) }
                .firstOrNull()
        }
    }

    fun isActiveContract(contractId: Int): Boolean {
        return transaction {
            ContractsTable
                .select {
                    (ContractsTable.id eq contractId) and
                        (ContractsTable.active eq true)
                }
                .limit(1)
                .any()
        }
    }

    private fun rowToContractResponse(row: ResultRow): ContractResponse {
        return ContractResponse(
            id = row[ContractsTable.id],
            employeeId = row[ContractsTable.employeeId],
            hourlyRate = row[ContractsTable.hourlyRate].toDouble(),
            startDate = row[ContractsTable.startDate].toKotlinLocalDate(),
            endDate = row[ContractsTable.endDate]?.toKotlinLocalDate(),
            active = row[ContractsTable.active],
        )
    }
}
