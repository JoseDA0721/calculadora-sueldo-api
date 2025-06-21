package com.example.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object RolesTable : Table("roles") {
    val id = integer(name = "id").autoIncrement()
    val name = varchar("nombre", 50)
    val description = text("descripcion")
    override val primaryKey = PrimaryKey(id)
}

object AllowsTable : Table("permisos") {
    val id = integer(name = "id").autoIncrement()
    val name = varchar("nombre", 100)
    val description = text("descripcion")
    override val primaryKey = PrimaryKey(id)
}

object RolesAllowsTable : Table("roles_permisos") {
    val id = integer(name = "id").autoIncrement()
    val roleId = integer(name = "rol_id") references RolesTable.id
    val allowsId = integer(name = "permiso_id") references AllowsTable.id
    override val primaryKey = PrimaryKey(id)
}

object UsersTable : Table("usuarios") {
    val id = integer(name = "id").autoIncrement()
    val roleId = integer(name = "rol_id").autoIncrement() references RolesTable.id
    val name = varchar("user_name", 100)
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100)
    val createdAt = timestamp("creado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

enum class PaymentMethods { efectico, transferencia }
object EmployeesTable : Table("trabajadores") {
    val id = varchar("id", 10)
    val firstName = varchar("nombre", 255)
    val lastName = varchar("aplellido", 255)
    val userId = integer("user_id").uniqueIndex() references UsersTable.id
    val activiti = varchar("actividad", 100)
    val method = enumerationByName("metodo", 20, PaymentMethods::class).default(PaymentMethods.transferencia)
    val email = varchar("email", 255).uniqueIndex()
    val telephone = varchar("telefono", 10).uniqueIndex()
    val telegramChatId = long("telegram_chat_id")
    val createdAt = timestamp("created_at").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

object ContractsTable : Table("contractos") {
    val id = integer("id").autoIncrement()
    val employeeId = varchar("trabajador_id", 10) references EmployeesTable.id
    val hourlyRate = decimal("tarifa_hora", 2, 10)
    val startDate = date("fecha_inicio")
    val endDate = date("fecha_fin")
    val active = bool("active").default(true)
    val createdAt = timestamp("creado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

object WorkLogsTable : Table("registros_horas") {
    val id = integer("id").autoIncrement()
    val contractId = integer(name = "contrato_id") references ContractsTable.id
    val date = date("fecha")
    val hoursWorked = decimal("hours", 2, 10)
    val createdAt = timestamp("creado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

object DiscountsTable : Table("descuentos") {
    val id = integer("id").autoIncrement()
    val contractId = integer(name = "contrato_id") references ContractsTable.id
    val description = text("descripcion")
    val amount = decimal("amount", 2, 10)
    val date = date("fecha")
    val createdAt = timestamp("creado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}


enum class StatusRequestPayment { pendiente, aprobada, rechazada }
object PaymentRequestsTable : Table("solicitudes_pago") {
    val id = integer("id").autoIncrement()
    val contractId = integer(name = "contrato_id") references ContractsTable.id
    val period = varchar("periodo", 7)
    val status = enumerationByName("status", 20, StatusRequestPayment::class).default(StatusRequestPayment.pendiente)
    val dateRequest = date("fecha")
    val reviewBy = integer("revisado_por") references UsersTable.id
    val observation = text("observacion")
    val createdAt = timestamp("creado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

enum class StatusPayment { en_proceso, pagado }
object PaymentTable : Table("pagos") {
    val id = integer("id").autoIncrement()
    val requestId = integer(name = "solicitud_id") references PaymentRequestsTable.id
    val salaryBase = decimal("sueldo_base", 2, 10)
    val discounts = decimal("discuentos", 2, 10)
    val total = decimal("total", 2, 10)
    val status = enumerationByName("status", 20, StatusPayment::class).default(StatusPayment.en_proceso)
    val paymentDate = date("fecha_pago")
    val createdAt = timestamp("creado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

object NotificationsTable : Table("notificaciones") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id") references UsersTable.id
    val message = text("mensaje")
    val read = bool("leido").default(false)
    val date = date("fecha")
    override val primaryKey = PrimaryKey(userId)
}

object StatsMonthlyTable : Table("metricas_mensuales"){
    val id = integer("id").autoIncrement()
    val contractId = integer(name = "contrato_id") references ContractsTable.id
    val period = varchar("periodo", 7)
    val totalHours = decimal("total_horas", 2, 10)
    val netSalary = decimal("sueldo_neto", 2, 10)
    val totalDiscount = decimal("total_discuento", 2, 10)
    val generatedAt = timestamp("generado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

