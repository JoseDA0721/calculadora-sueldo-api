package com.example

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

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
        }
}
