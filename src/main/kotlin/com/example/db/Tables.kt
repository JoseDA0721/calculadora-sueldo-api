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
    val roleId = integer(name = "rol_id") references RolesTable.id
    val name = varchar("user_name", 100)
    val password = varchar("password", 100)
    val createdAt = timestamp("creado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

enum class PaymentMethods { EFECTIVO, TRANSFERENCIA }

object EmployeesTable : Table("trabajadores") {
    val id = varchar("id", 10)
    val firstName = varchar("nombre", 255)
    val lastName = varchar("apellido", 255)
    val userId = integer("user_id").uniqueIndex() references UsersTable.id
    val activity = varchar("actividad", 100)
    val method = enumerationByName("metodo", 20, PaymentMethods::class).default(PaymentMethods.TRANSFERENCIA)
    val email = varchar("email", 255).uniqueIndex()
    val telephone = varchar("telefono", 10).uniqueIndex()
    val telegramChatId = long("telegram_chat_id")
    val createdAt = timestamp("created_at").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

object ContractsTable : Table("contratos") {
    val id = integer("id").autoIncrement()
    val employeeId = varchar("trabajador_id", 10) references EmployeesTable.id
    val hourlyRate = decimal("tarifa_hora", 10, 2)
    val startDate = date("fecha_inicio")
    val endDate = date("fecha_fin")
    val active = bool("activo").default(true)
    val createdAt = timestamp("creado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

object WorkLogsTable : Table("registros_horas") {
    val id = integer("id").autoIncrement()
    val contractId = integer(name = "contrato_id") references ContractsTable.id
    val date = date("fecha")
    val hoursWorked = decimal("horas_trabajadas", 10, 2)
    val createdAt = timestamp("creado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

object DiscountsTable : Table("descuentos") {
    val id = integer("id").autoIncrement()
    val contractId = integer(name = "contrato_id") references ContractsTable.id
    val description = text("descripcion")
    val amount = decimal("amount", 10, 2)
    val date = date("fecha")
    val createdAt = timestamp("creado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

enum class StatusRequestPayment { PENDIENTE, APROBADA, RECHAZADA }

object PaymentRequestsTable : Table("solicitudes_pago") {
    val id = integer("id").autoIncrement()
    val contractId = integer(name = "contrato_id") references ContractsTable.id
    val period = varchar("periodo", 7)
    val status = enumerationByName("estado", 20, StatusRequestPayment::class).default(StatusRequestPayment.PENDIENTE)
    val dateRequest = date("fecha_solicitud")
    val reviewBy = integer("revisado_por") references UsersTable.id
    val observation = text("observacion")
    val createdAt = timestamp("creado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}

enum class StatusPayment { EN_PROCESO, PAGADO }

object PaymentTable : Table("pagos") {
    val id = integer("id").autoIncrement()
    val requestId = integer(name = "solicitud_id").uniqueIndex() references PaymentRequestsTable.id
    val salaryBase = decimal("sueldo_base", 10, 2)
    val discounts = decimal("discuentos", 10, 2)
    val total = decimal("total", 10, 2)
    val status = enumerationByName("estado", 20, StatusPayment::class).default(StatusPayment.EN_PROCESO)
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
    override val primaryKey = PrimaryKey(id)
}

object StatsMonthlyTable : Table("metricas_mensuales") {
    val id = integer("id").autoIncrement()
    val contractId = integer(name = "contrato_id") references ContractsTable.id
    val period = varchar("periodo", 7)
    val totalHours = decimal("total_horas", 10, 2)
    val netSalary = decimal("sueldo_neto", 10, 2)
    val totalDiscount = decimal("total_discuento", 10, 2)
    val generatedAt = timestamp("generado_en").default(Instant.now())
    override val primaryKey = PrimaryKey(id)
}
