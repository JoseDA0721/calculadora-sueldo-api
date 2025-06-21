package com.example

import com.example.db.RolesTable
import com.example.db.PaymentTable
import com.example.db.EmployeesTable
import com.example.db.UsersTable
import com.example.db.RolesAllowsTable
import com.example.db.WorkLogsTable
import com.example.db.AllowsTable
import com.example.db.ContractsTable
import com.example.db.DiscountsTable
import com.example.db.NotificationsTable
import com.example.db.PaymentRequestsTable
import com.example.db.StatsMonthlyTable
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.Schema
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class ApplicationTest {
    @Test
    fun testRoot() =
        testApplication {
            environment {
                config = ApplicationConfig("application-test.yaml")
            }

            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
            }

            transaction {
                SchemaUtils.create(
                    RolesTable,
                    AllowsTable,
                    RolesAllowsTable,
                    UsersTable,
                    EmployeesTable,
                    ContractsTable,
                    WorkLogsTable,
                    DiscountsTable,
                    PaymentRequestsTable,
                    PaymentTable,
                    NotificationsTable,
                    StatsMonthlyTable
                )
                SchemaUtils.drop(
                    RolesTable,
                    AllowsTable,
                    RolesAllowsTable,
                    UsersTable,
                    EmployeesTable,
                    ContractsTable,
                    WorkLogsTable,
                    DiscountsTable,
                    PaymentRequestsTable,
                    PaymentTable,
                    NotificationsTable,
                    StatsMonthlyTable
                )
            }
        }
}
