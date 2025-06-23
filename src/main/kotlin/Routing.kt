package com.example

import com.example.dto.NewContractRequest
import com.example.dto.NewEmployeeRequest
import com.example.dto.NewPaymentRequest
import com.example.dto.NewWorkLogRequest
import com.example.dto.UpdateEmployeeRequest
import com.example.services.ContractService
import com.example.services.EmployeeService
import com.example.services.NotificationService
import com.example.services.PayrollService
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
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.postgresql.util.PSQLException

fun Application.configureRouting() {
    val employeeService = EmployeeService()
    val workLogService = WorkLogService()
    val contractService = ContractService()
    val botToken = environment.config.property("bot.token").getString()
    val notificationService = NotificationService(botToken)
    val payrollService = PayrollService(contractService, workLogService, employeeService, notificationService)

    routing {
        get("/") {
            call.respondText("API CORRIENDO EL PUERTO 8080", contentType = ContentType.Text.Plain)
        }

        route("/api/empleado") {
            post {
                try {
                    val request = call.receive<NewEmployeeRequest>()
                    val newEmployee = employeeService.createEmployee(request)
                    call.respond(HttpStatusCode.Created, newEmployee)
                } catch (e: PSQLException) {
                    if (e.sqlState == "23505") {
                        call.respond(HttpStatusCode.Conflict, "Error: Empleado ya registrado.")
                    } else {
                        throw e
                    }
                }
            }

            route("/{chatId}") {
                get {
                    println(call.parameters["chatId"])
                    val chatId = call.parameters["chatId"]?.toLongOrNull()
                    if (chatId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Chat ID invalido")
                        return@get
                    }
                    val employee = employeeService.findByChatId(chatId)
                    println(employee)
                    if (employee != null) {
                        call.respond(HttpStatusCode.OK, employee)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "No se encontró un empleado para el Chat ID proporcionado")
                    }
                }
            }

            route("/{id}") {
                // GET a /api/empleado/{id}
                get {
                    println(call.parameters["id"])
                    val id = call.parameters["id"].toString()
                    if (id == "") {
                        call.respond(HttpStatusCode.BadRequest, "El ID debe ser su cedula")
                        return@get
                    }
                    val employee = employeeService.findById(id)
                    if (employee != null) {
                        call.respond(HttpStatusCode.OK, employee)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "No se encontró el empleado con ID $id.")
                    }
                }

                patch {
                    val id = call.parameters["id"].toString()
                    if (id == "") {
                        call.respond(HttpStatusCode.BadRequest, "El ID debe ser su cedula")
                        return@patch
                    }
                    try {
                        val request = call.receive<UpdateEmployeeRequest>()
                        val employee = employeeService.updateEmployee(id, request) ?: return@patch
                        call.respond(HttpStatusCode.OK, employee)
                    } catch (e: PSQLException) {
                        if (e.sqlState == "23505") {
                            call.respond(HttpStatusCode.Conflict, "Error: " + e.message)
                        } else {
                            throw e
                        }
                    }
                }
            }
        }

        route("/api/contract") {
            post {
                try {
                    val request = call.receive<NewContractRequest>()
                    val newContract = contractService.createContract(request)
                    call.respond(HttpStatusCode.Created, newContract)
                } catch (e: PSQLException) {
                    call.respond(HttpStatusCode.BadRequest, "ERROR: " + e.message)
                }
            }
        }

        route("/api/work-logs") {
            post {
                try {
                    val request = call.receive<NewWorkLogRequest>()
                    val workLog = workLogService.createWorkLog(request)
                    call.respond(HttpStatusCode.Created, workLog)
                } catch (e: PSQLException) {
                    call.respond(HttpStatusCode.Conflict, "ERROR: " + e.message)
                }
            }
        }

        route("/api/payment-request") {
            post {
                try {
                    val request = call.receive<NewPaymentRequest>()
                    val newPaymentRequest = payrollService.createPaymentRequest(request)
                    call.respond(HttpStatusCode.Created, newPaymentRequest)
                } catch (e: PSQLException) {
                    call.respond(HttpStatusCode.BadRequest, "ERROR: " + e.message)
                }
            }

            get("/status") {
                val employeeId = call.request.queryParameters["employeeId"]
                val period = call.request.queryParameters["period"]

                println(employeeId)
                println(period)

                if (employeeId == null || period == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Faltan los parámetros 'employeeId' o 'period'.",
                    )
                    return@get
                }

                try {
                    val paymentRequestStatus = payrollService.getPaymentRequestStatus(employeeId, period)
                    if (paymentRequestStatus != null) {
                        call.respond(HttpStatusCode.OK, paymentRequestStatus)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "No se encontró una solicitud para ese período.")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            patch("/{requestId}") {
                val requestId = call.parameters["requestId"]?.toIntOrNull()
                if (requestId == null) {
                    call.respond(HttpStatusCode.BadRequest, "El ID debe ser un entero.")
                    return@patch
                }

                val processPaymentRequest = payrollService.processPaymentRequest(requestId)

                if (processPaymentRequest != null) {
                    call.respond(HttpStatusCode.OK, processPaymentRequest)
                } else {
                    call.respond(HttpStatusCode.Conflict, "Hubo un error")
                }
            }
        }

        route("/api/payroll") {
            get("/calculate-salary") {
                val employeeId = call.parameters["employeeId"]
                val period = call.parameters["period"]

                if (employeeId == null || period == null) {
                    call.respond(HttpStatusCode.BadRequest, "Faltan los parámetros 'employeeId' o 'period'.")
                    return@get
                }

                try {
                    val estimatedSalary = payrollService.calculateEstimatedSalary(employeeId, period)
                    call.respond(HttpStatusCode.OK, estimatedSalary)
                } catch (e: PSQLException) {
                    call.respond(HttpStatusCode.BadRequest, "ERROR: " + e.message)
                }
            }
        }

        route("/api/payments") {
            patch("/{paymentId}/pay") {
                val paymentId = call.parameters["paymentId"]?.toIntOrNull()
                if (paymentId == null) {
                    call.respond(HttpStatusCode.BadRequest, "ID de pago inválido.")
                    return@patch
                }

                try {
                    val paidPayment = payrollService.executePayment(paymentId)
                    if (paidPayment != null) {
                        call.respond(HttpStatusCode.OK, paidPayment)
                    } else {
                        call.respond(
                            HttpStatusCode.Conflict,
                            "El pago no pudo ser procesado. " +
                                "Puede que ya haya sido pagado o no exista.",
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Ocurrió un error: ${e.message}",
                    )
                }
            }
        }
    }
}
