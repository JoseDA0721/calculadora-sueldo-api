package com.example

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() =
        testApplication {
            environment {
                config =
                    MapApplicationConfig(
                        "database.driver" to "org.postgresql.Driver",
                        "database.url" to "jdbc:postgresql://localhost:5432/testdb",
                        "database.user" to "testuser",
                        "database.password" to "testpassword",
                    )
            }

            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
            }
        }
}
