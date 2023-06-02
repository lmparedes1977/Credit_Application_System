package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.CreditDTO
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@SpringBootTest
@ActiveProfiles("Test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {

    @Autowired
    private lateinit var creditRepository: CreditRepository
    @Autowired
    private lateinit var mockMvc: MockMvc
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    @Autowired
    private lateinit var customerRepository: CustomerRepository

    companion object {
        const val URL: String = "/api/credit"
    }

    @BeforeEach
    fun setup() = creditRepository.deleteAll()

    @AfterEach
    fun tearDown() = creditRepository.deleteAll()

    @Test
    fun `should create a credit and return 201 status`() {
        // given
        customerRepository.save(buildCustomer())
        val creditDTO: CreditDTO = buildCreditDTO()
        val valueAsString: String = objectMapper.writeValueAsString(creditDTO)
        // When & then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(valueAsString))
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save a credit if costumer dont exist and return 400 status`() {
        // given
        val creditDTO: CreditDTO = buildCreditDTO()
        val valueAsString: String = objectMapper.writeValueAsString(creditDTO)
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post(CustomerResourceTest.URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(valueAsString))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find all credits of a customer and return status 200`() {
        // given
        val customer: Customer = customerRepository.save(buildCustomer())
        val credit1: Credit = buildCredit(customer = customer)
        val credit2: Credit = buildCredit(customer = customer)
        credit1.creditCode = UUID.fromString("bcceb8bc-d853-44d5-83a8-ce6d9e1e9fb5")
        credit2.creditCode = UUID.fromString("fd1007b1-c350-4fe7-b3c7-e60e307f02e5")
        creditRepository.save(credit1)
        creditRepository.save(credit2)
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("$URL?customerId=${customer.id}")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find all credits of a customer and return empty list`() {
        // given
        val fakeCustomerId: Long = 1L
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("$URL?customerId=$fakeCustomerId")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find credit by credit code and user id and return status 200`() {
        // given
        val customer: Customer = customerRepository.save(buildCustomer())
        val credit: Credit = buildCredit(customer = customer)
        credit.creditCode = UUID.fromString("bcceb8bc-d853-44d5-83a8-ce6d9e1e9fb5")
        creditRepository.save(credit)
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("$URL/${credit.creditCode}?customerId=${customer.id}")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditCode").value("bcceb8bc-d853-44d5-83a8-ce6d9e1e9fb5"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditValue").value("100000"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfInstallments").value("5"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.emailCustomer").value("b@rouba"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.incomeCustomer").value("10000"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find a credit by credit code wrong and return status 400`() {
        // given
        val customer: Customer = customerRepository.save(buildCustomer())
        val credit: Credit = buildCredit(customer = customer)
        creditRepository.save(credit)
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("$URL/bcceb8bc-d853-44d5-83a8-ce6d9e1e9fb5?customerId=${customer.id}")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consul the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timeStamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(409))
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }



    private fun buildCreditDTO(
        creditValue: BigDecimal = BigDecimal.valueOf(1000000),
        dayFirstInstallment: LocalDate = LocalDate.of(2024, Calendar.JULY, 1),
        numberOfInstallments: Int = 5,
        customerId: Long = 1L
    ) = CreditDTO(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customerId
    )

    private fun buildCredit(
        creditValue: BigDecimal = BigDecimal.valueOf(100000),
        dayFirstInstallment: LocalDate = LocalDate.of(2024, Calendar.JULY, 1),
        numberOfInstallments: Int = 5,
        customer: Customer = buildCustomer()
    ) = Credit(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customer = customer
    )

    private fun buildCustomer(
        firstName: String = "Leti",
        lastName: String = "Big",
        cpf: String = "92006299034",
        income: BigDecimal = BigDecimal.valueOf(10000),
        email: String = "b@rouba",
        password: String = "123456",
        zipCode: String = "90160193",
        street: String = "Ipi",
    ) = Customer(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        income = income,
        email = email,
        password = password,
        address = Address(
            zipCode = zipCode,
            street = street
        ),
    )
}