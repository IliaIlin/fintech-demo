package com.myfintech.payments

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.myfintech.payments.controller.GlobalExceptionHandler.Companion.ACCOUNT_NOT_FOUND_ERROR_MESSAGE
import com.myfintech.payments.controller.GlobalExceptionHandler.Companion.BAD_REQUEST_ERROR_MESSAGE
import com.myfintech.payments.controller.GlobalExceptionHandler.Companion.INSUFFICIENT_FUNDS_ERROR_MESSAGE
import com.myfintech.payments.dto.*
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class APIIntegrationTest {

    companion object {
        @Container
        val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpassword")

        @JvmStatic
        @BeforeAll
        fun setup() {
            postgresContainer.start()
            System.setProperty("spring.datasource.url", postgresContainer.jdbcUrl)
            System.setProperty("spring.datasource.username", postgresContainer.username)
            System.setProperty("spring.datasource.password", postgresContainer.password)
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            postgresContainer.stop()
        }
    }

    @LocalServerPort
    var port: Int = 0

    val objectMapper: ObjectMapper = jacksonObjectMapper()

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        jdbcTemplate.execute("truncate table transaction restart identity cascade")
        jdbcTemplate.execute("truncate table account restart identity cascade")
    }

    @Nested
    inner class HappyFlows {
        @Test
        fun `create account`() {
            val accountBalanceValue = "100.9555"
            val accountBalance = BigDecimal(accountBalanceValue)

            val response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(AccountCreationRequest(balance = accountBalance)))
                .post("/api/accounts")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().response().body.prettyPrint()

            assertThat(objectMapper.readValue<AccountDetailsResponse>(response))
                .isEqualTo(AccountDetailsResponse(1, BigDecimal(accountBalanceValue)))
            assertThat(
                jdbcTemplate.queryForObject(
                    "select balance from account where account_id=1",
                    String::class.java
                )
            )
                .isEqualTo(accountBalanceValue)
        }

        @Test
        fun `get account by id`() {
            val accountBalanceValue = "100.0000"
            jdbcTemplate.execute("insert into account (balance) values ($accountBalanceValue)")

            val response = given()
                .port(port)
                .get("/api/accounts/1")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().response().body.prettyPrint()

            assertThat(objectMapper.readValue<AccountDetailsResponse>(response))
                .isEqualTo(AccountDetailsResponse(1, BigDecimal(accountBalanceValue)))
        }

        @Test
        fun `create a transaction and update accounts balances`() {
            jdbcTemplate.execute("insert into account (balance) values (100.0000)")
            jdbcTemplate.execute("insert into account (balance) values (150.5000)")

            val response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(
                    objectMapper.writeValueAsString(
                        TransactionCreationRequest(
                            amount = BigDecimal("55.5000"),
                            fromAccountId = 2,
                            toAccountId = 1
                        )
                    )
                )
                .post("/api/transactions")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().response().body.prettyPrint()

            assertThat(objectMapper.readValue<TransactionDetailsResponse>(response))
                .isEqualTo(TransactionDetailsResponse(1, BigDecimal("55.5000"), 2, 1))

            assertThat(
                jdbcTemplate.queryForObject(
                    "select amount from transaction where tx_id=1",
                    String::class.java
                )
            )
                .isEqualTo("55.5000")
            assertThat(
                jdbcTemplate.queryForObject(
                    "select balance from account where account_id=1",
                    String::class.java
                )
            )
                .isEqualTo("155.5000")
            assertThat(
                jdbcTemplate.queryForObject(
                    "select balance from account where account_id=2",
                    String::class.java
                )
            )
                .isEqualTo("95.0000")
        }
    }

    @Nested
    inner class ExceptionFlows {

        @Test
        fun `get account by id is requested for not existing account`() {
            val response = given()
                .port(port)
                .get("/api/accounts/1")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().response().body.prettyPrint()

            assertThat(objectMapper.readValue<ErrorResponse>(response))
                .isEqualTo(ErrorResponse(ACCOUNT_NOT_FOUND_ERROR_MESSAGE))
        }

        @Test
        fun `create transaction between existing and not existing account ids`() {
            jdbcTemplate.execute("insert into account (balance) values (100.0000)")

            val response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(
                    objectMapper.writeValueAsString(
                        TransactionCreationRequest(
                            amount = BigDecimal("55.5000"),
                            fromAccountId = 2,
                            toAccountId = 1
                        )
                    )
                )
                .post("/api/transactions")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().response().body.prettyPrint()

            assertThat(objectMapper.readValue<ErrorResponse>(response))
                .isEqualTo(ErrorResponse(ACCOUNT_NOT_FOUND_ERROR_MESSAGE))
        }

        @Test
        fun `create transaction body is not deserializable`() {
            val response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(
                    """
                     {
	                    "amount": 100,
	                    "fromAccountId": "1"
                     }
                    """.trimIndent()
                )
                .post("/api/transactions")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().response().body.prettyPrint()

            assertThat(objectMapper.readValue<ErrorResponse>(response))
                .isEqualTo(ErrorResponse(BAD_REQUEST_ERROR_MESSAGE))
        }

        @Test
        fun `create account body is not deserializable`() {
            val response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(
                    """
                     {
	                    "balance":
                     }
                    """.trimIndent()
                )
                .post("/api/accounts")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().response().body.prettyPrint()

            assertThat(objectMapper.readValue<ErrorResponse>(response))
                .isEqualTo(ErrorResponse(BAD_REQUEST_ERROR_MESSAGE))
        }

        @Test
        fun `create a transaction where fromAccount doesn't have enough funds to complete transaction`() {
            jdbcTemplate.execute("insert into account (balance) values (100.0000)")
            jdbcTemplate.execute("insert into account (balance) values (50.0000)")

            val response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(
                    objectMapper.writeValueAsString(
                        TransactionCreationRequest(
                            amount = BigDecimal("200.0000"),
                            fromAccountId = 2,
                            toAccountId = 1
                        )
                    )
                )
                .post("/api/transactions")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .extract().response().body.prettyPrint()

            assertThat(objectMapper.readValue<ErrorResponse>(response))
                .isEqualTo(ErrorResponse(INSUFFICIENT_FUNDS_ERROR_MESSAGE))
        }
    }
}