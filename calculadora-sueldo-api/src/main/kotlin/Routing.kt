package com.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.dto.NewEmployeeRequest
import com.example.services.EmployeeService
import io.ktor.server.response.respond
import org.postgresql.util.PSQLException
import com.example.dto.NewWorkLogRequest // <-- Nueva importación
import com.example.services.WorkLogService
import com.example.dto.UpdateHourlyRateRequest // <-- Nueva importación
import com.example.services.ConfigService

fun Application.configureRouting() {
    val employeeService = EmployeeService()
    val workLogService = WorkLogService()
    val configService = ConfigService()

    routing {
        get("/") {
            call.respondText("API CORRIENDO EL PUERTO 8080", contentType = ContentType.Text.Plain)
        }

        get("/api/empleado/{id}") {
            // Obtenemos el ID de la URL (ej: /api/empleado/1)
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "El ID debe ser un número entero.")
                return@get
            }

            // Llamamos al servicio para buscar al empleado
            val employee = employeeService.findById(id)

            if (employee != null) {
                // Si se encuentra, respondemos con 200 OK y los datos
                call.respond(HttpStatusCode.OK, employee)
            } else {
                // Si no se encuentra, respondemos con 404 Not Found
                call.respond(HttpStatusCode.NotFound, "No se encontró el empleado con ID $id.")
            }
        }

        post("/api/empleado") {
            try {
                val request = call.receive<NewEmployeeRequest>()
                val neEmployee = employeeService.createEmployee(request)

                call.respond(HttpStatusCode.Created, neEmployee)
            } catch (e: PSQLException) {
                if(e.sqlState == "23505"){
                    call.respond(HttpStatusCode.Conflict, "Error: El email ya existe.")
                } else {
                    throw e
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
                    "Costo por hora actualizada a ${request.rate}"
                )
            }
        }
    }
}
