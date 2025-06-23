package com.example.services

import com.example.db.ContractsTable
import com.example.db.PaymentRequestsTable
import com.example.db.PaymentTable
import com.example.db.StatusRequestPayment
import com.example.dto.EstimatedSalaryResponse
import com.example.dto.NewPaymentRequest
import com.example.dto.PaymentRequestResponse
import com.example.dto.PaymentResponse
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDate

class PayrollService(private val contractService: ContractService, private val workLogService: WorkLogService) {
    fun createPaymentRequest(request: NewPaymentRequest): PaymentRequestResponse {
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

            val newPaymentRequest =
                PaymentRequestsTable.insert {
                    it[contractId] = contractID
                    it[period] = request.period
                    it[dateRequest] = LocalDate.now()
                }.resultedValues!!.first()
            println("Row: " + newPaymentRequest)
            rowToPaymentRequestResponse(newPaymentRequest)
        }
    }

    fun processPaymentRequest(requestId: Int): PaymentResponse? {
        return transaction {
            val (contractId, period) =
                PaymentRequestsTable
                    .slice(PaymentRequestsTable.contractId, PaymentRequestsTable.period)
                    .select { PaymentRequestsTable.id eq requestId }
                    .map {
                        it[PaymentRequestsTable.contractId] to it[PaymentRequestsTable.period]
                    }
                    .first()
            val activeContract = contractService.getActiveContract(contractId) ?: return@transaction null
            val totalHours = workLogService.getTotalHoursForPeriod(contractId, period)

            val salaryBase = totalHours * activeContract.hourlyRate
            val discounts = 0.0
            val total = salaryBase - discounts

            val newPayment =
                PaymentTable.insert {
                    it[this.requestId] = requestId
                    it[this.salaryBase] = salaryBase.toBigDecimal()
                    it[this.discounts] = discounts.toBigDecimal()
                    it[this.total] = total.toBigDecimal()
                    it[paymentDate] = LocalDate.now()
                }.resultedValues?.first()
            PaymentRequestsTable.update({ PaymentRequestsTable.id eq requestId }) {
                it[this.status] = StatusRequestPayment.APROBADA
                it[reviewBy] = 1
            }
            newPayment?.let { rowToPaymentResponse(it) }
        }
    }

    fun calculateEstimatedSalary(
        employeeId: String,
        period: String,
    ): EstimatedSalaryResponse {
        return transaction {
            val activeContract =
                contractService.getContractForEmployee(employeeId)
                    ?: throw Exception("No se encontró un contrato activo para el empleado.")

            val totalHours = workLogService.getTotalHoursForPeriod(activeContract.id, period)

            val totalDiscounts = 0.0

            val hourlyRate = activeContract.hourlyRate
            val baseSalary = totalHours * hourlyRate
            val estimatedNetSalary = baseSalary - totalDiscounts

            EstimatedSalaryResponse(
                period = period,
                hourlyRate = hourlyRate,
                totalHours = totalHours,
                baseSalary = baseSalary,
                totalDiscounts = totalDiscounts,
                estimatedNetSalary = estimatedNetSalary,
            )
        }
    }

    fun getPaymentRequestStatus(
        employeeId: String,
        period: String,
    ): PaymentRequestResponse? {
        return transaction {
            PaymentRequestsTable
                .join(ContractsTable, org.jetbrains.exposed.sql.JoinType.INNER, additionalConstraint = {
                    PaymentRequestsTable.contractId eq ContractsTable.id
                })
                .select {
                    (ContractsTable.employeeId eq employeeId) and (PaymentRequestsTable.period eq period)
                }
                .map { rowToPaymentRequestResponse(it) }
                .firstOrNull()
        }
    }

    private fun rowToPaymentRequestResponse(row: ResultRow): PaymentRequestResponse {
        return PaymentRequestResponse(
            id = row[PaymentRequestsTable.id],
            contractId = row[PaymentRequestsTable.contractId],
            period = row[PaymentRequestsTable.period],
            status = row[PaymentRequestsTable.status],
            dateRequest = row[PaymentRequestsTable.dateRequest].toKotlinLocalDate(),
            reviewBy = row[PaymentRequestsTable.reviewBy.let { it }],
            observation = row[PaymentRequestsTable.observation],
        )
    }

    private fun rowToPaymentResponse(row: ResultRow): PaymentResponse {
        return PaymentResponse(
            id = row[PaymentTable.id],
            requestId = row[PaymentTable.requestId],
            salaryBase = row[PaymentTable.salaryBase].toDouble(),
            discounts = row[PaymentTable.discounts].toDouble(),
            total = row[PaymentTable.total].toDouble(),
            status = row[PaymentTable.status],
            paymentDate = row[PaymentTable.paymentDate].toKotlinLocalDate(),
        )
    }
}
