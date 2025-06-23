package com.example

import com.example.db.AllowsTable
import com.example.db.ContractsTable
import com.example.db.DatabaseFactory
import com.example.db.DiscountsTable
import com.example.db.EmployeesTable
import com.example.db.NotificationsTable
import com.example.db.PaymentRequestsTable
import com.example.db.PaymentTable
import com.example.db.RolesAllowsTable
import com.example.db.RolesTable
import com.example.db.StatsMonthlyTable
import com.example.db.StatusPayment
import com.example.db.StatusRequestPayment
import com.example.db.UsersTable
import com.example.db.WorkLogsTable
import com.example.dto.NewContractRequest
import com.example.dto.NewEmployeeRequest
import com.example.dto.NewPaymentRequest
import com.example.dto.NewWorkLogRequest
import com.example.services.ContractService
import com.example.services.EmployeeService
import com.example.services.NotificationService
import com.example.services.PayrollService
import com.example.services.WorkLogService
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class PayrollServiceTest {


    private lateinit var employeeService: EmployeeService
    private lateinit var contractService: ContractService
    private lateinit var workLogService: WorkLogService
    private lateinit var payrollService: PayrollService
    private lateinit var notificationService: NotificationService

    @Before
    fun setup() {

        val config = ApplicationConfig("application-test.yaml")
        DatabaseFactory.init(config)

        transaction {
            // Verifica que existan roles esperados
            val adminExists = RolesTable.select { RolesTable.name eq "Administrador" }.count() > 0
            val empleadoExists = RolesTable.select { RolesTable.name eq "Empleado" }.count() > 0
            check(adminExists && empleadoExists) { "Los roles base no fueron cargados correctamente." }
        }

        employeeService = EmployeeService()
        contractService = ContractService()
        workLogService = WorkLogService()
        notificationService = NotificationService("fake-token-for-testing") //
        payrollService = PayrollService(contractService, workLogService, employeeService, notificationService)
    }

    @Test
    fun `test create and process payment request`() {
        transaction {
            // 1. Crear Empleado
            val newEmployee = employeeService.createEmployee(
                NewEmployeeRequest(
                    id = "1234567890",
                    firstName = "John",
                    lastName = "Doe",
                    activity = "Developer",
                    email = "john.doe@example.com"
                )
            )
            assertNotNull(newEmployee)

            // 2. Crear Contrato
            val newContract = contractService.createContract(
                NewContractRequest(
                    employeeId = newEmployee.id,
                    hourlyRate = 10.0
                )
            )
            assertNotNull(newContract)

            // 3. Registrar Horas de Trabajo
            workLogService.createWorkLog(
                NewWorkLogRequest(
                    employeeId = newEmployee.id,
                    hoursWorked = 80.0
                )
            )

            // 4. Crear Solicitud de Pago
            val paymentRequest = payrollService.createPaymentRequest(
                NewPaymentRequest(
                    employeeId = newEmployee.id,
                    period = "2025-06"
                )
            )
            assertNotNull(paymentRequest)
            assertEquals(StatusRequestPayment.PENDIENTE, paymentRequest.status) //

            // 5. Procesar Solicitud de Pago
            val paymentResponse = payrollService.processPaymentRequest(paymentRequest.id)
            assertNotNull(paymentResponse)
            assertEquals(800.0, paymentResponse.salaryBase)
            assertEquals(800.0, paymentResponse.total)
            assertEquals(StatusRequestPayment.APROBADA, payrollService.getPaymentRequestStatus(newEmployee.id, "2025-06")?.status) //

            // 6. Ejecutar Pago
            val executedPayment = payrollService.executePayment(paymentResponse.id)
            assertNotNull(executedPayment)
            assertEquals(StatusPayment.PAGADO, executedPayment.status) //
        }
    }
}