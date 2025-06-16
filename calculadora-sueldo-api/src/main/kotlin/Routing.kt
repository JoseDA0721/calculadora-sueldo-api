package com.example

import com.example.db.EmployeesTable
import com.example.db.PaymentStatus
import com.example.db.PaymentsTable
import com.example.dto.NewEmployeeRequest
import com.example.dto.NewWorkLogRequest
import com.example.dto.UpdateHourlyRateRequest
import com.example.services.ConfigService
import com.example.services.EmployeeService
import com.example.services.NotificationService
import com.example.services.WorkLogService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.postgresql.util.PSQLException
import java.time.Instant
import java.time.YearMonth

fun Application.configureRouting() {
    val employeeService = EmployeeService()
    val workLogService = WorkLogService()
    val configService = ConfigService()
    val botToken = environment.config.propertyOrNull("bot.token")?.getString()
    val notificationService = if (botToken != null) NotificationService(botToken) else null

    routing {
        get("/") {
            call.respondText("API CORRIENDO EL PUERTO 8080", contentType = ContentType.Text.Plain)
        }

        route("/api/empleado") { // SIN la barra al final

            // POST a /api/empleado
            post {
                try {
                    val request = call.receive<NewEmployeeRequest>()
                    val newEmployee = employeeService.createEmployee(request)
                    call.respond(HttpStatusCode.Created, newEmployee)
                } catch (e: PSQLException) {
                    if (e.sqlState == "23505") {
                        call.respond(HttpStatusCode.Conflict, "Error: El email ya existe.")
                    } else {
                        throw e
                    }
                }
            }

            // --- Rutas que dependen de un ID, anidadas ---
            route("/{id}") {
                // GET a /api/empleado/{id}
                get {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "El ID debe ser un número entero.")
                        return@get
                    }
                    val employee = employeeService.findById(id)
                    if (employee != null) {
                        call.respond(HttpStatusCode.OK, employee)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "No se encontró el empleado con ID $id.")
                    }
                }

                // GET a /api/empleado/{id}/summary
                get("/summary") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    val month = call.request.queryParameters["month"]?.toIntOrNull() ?: YearMonth.now().monthValue
                    val year = call.request.queryParameters["year"]?.toIntOrNull() ?: YearMonth.now().year

                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "ID de empleado inválido.")
                        return@get
                    }
                    val summary = employeeService.getMonthlySummary(id, YearMonth.of(year, month))
                    if (summary != null) {
                        call.respond(HttpStatusCode.OK, summary)
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "No se pudo generar el resumen.")
                    }
                }
            }
        }

        route("/api/work-logs") {
            post {
                val request = call.receive<NewWorkLogRequest>()
                val workLog = workLogService.create(request)
                call.respond(HttpStatusCode.Created, workLog)
            }
        }

        route("/api/config") {
            put("/hourly-rate") {
                val request = call.receive<UpdateHourlyRateRequest>()
                configService.updateHourlyRate(request.rate)
                call.respond(
                    HttpStatusCode.OK,
                    "Costo por hora actualizada a ${request.rate}",
                )
            }
        }

        route("/api/payments") {
            patch("/{id}/pay") {
                val paymentId = call.parameters["id"]?.toIntOrNull()
                if (paymentId == null) {
                    call.respond(HttpStatusCode.BadRequest, "ID de pago inválido.")
                    return@patch
                }

                var employeeChatId: Long? = null
                var paymentAmount = 0.0

                // Lógica de actualización directa en la ruta por simplicidad
                val updatedRows =
                    transaction {
                        val payment = PaymentsTable.select { PaymentsTable.id eq paymentId }.firstOrNull()
                        if (payment == null) {
                            return@transaction 0
                        }

                        val employeeId = payment[PaymentsTable.employeeId]
                        paymentAmount = payment[PaymentsTable.amount].toDouble()

                        employeeChatId =
                            EmployeesTable.select { EmployeesTable.id eq employeeId }.firstOrNull()?.get(
                                EmployeesTable.telegramChatId,
                            )

                        PaymentsTable.update({ PaymentsTable.id eq paymentId }) {
                            it[status] = PaymentStatus.PAGADO
                            it[updatedAt] = Instant.now()
                        }
                    }

                if (updatedRows > 0) {
                    if (notificationService != null && employeeChatId != null) {
                        notificationService.sendPaymentNotification(employeeChatId!!, paymentAmount)
                    }
                    call.respond(HttpStatusCode.OK, "El pago con ID $paymentId ha sido marcado como PAGADO.")
                } else {
                    call.respond(HttpStatusCode.NotFound, "No se encontró el pago con ID $paymentId.")
                }
            }
        }
    }
}
