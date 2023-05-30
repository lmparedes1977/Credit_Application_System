package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.CustomerDTO
import me.dio.credit.application.system.entity.Customer
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

@SpringBootTest
@ActiveProfiles("Teste")
@AutoConfigureMockMvc
@ContextConfiguration
class CustomerResourceTest {

    @Autowired private lateinit var customerRepository: CustomerRepository
    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/customer"
    }

    @BeforeEach fun setup() = customerRepository.deleteAll()

    @AfterEach fun tearDown() = customerRepository.deleteAll()

    @Test
    fun `should create a customer and return 201 status`() {
        // given
        val customerDTO: CustomerDTO = builderCustomerDTO()
        val valueAsString: String = objectMapper.writeValueAsString(customerDTO)
        // When & then
        mockMvc.perform(MockMvcRequestBuilders.post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(valueAsString))
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Leo"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Big"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("00000000191"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.income").value("10000"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("a@rouba"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("90160193"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Ipi"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("1"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save a customer with same cpf and return 409 status`() {
        // given
        customerRepository.save(builderCustomerDTO().toEntity())
        val customerDTO: CustomerDTO = builderCustomerDTO()
        val valueAsString: String = objectMapper.writeValueAsString(customerDTO)
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(valueAsString))
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consul the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timeStamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(409))
            .andExpect(MockMvcResultMatchers.jsonPath("$.exception").value("class org.springframework.dao.DataIntegrityViolationException"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save a customer if firstName filed empty and return 400 status`() {
        // given
        val customerDTO: CustomerDTO = builderCustomerDTO(firstName = "")
        val valueAsString: String = objectMapper.writeValueAsString(customerDTO)
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(valueAsString))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consul the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timeStamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find customer by id and return status 200`() {
        // given
        val customer: Customer = customerRepository.save(builderCustomerDTO().toEntity())
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("$URL/${customer.id}")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Leo"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Big"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("00000000191"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.income").value("10000"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("a@rouba"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("90160193"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Ipi"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("1"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find customer with invalid id and return status 400`() {
        // given
        val invalidId: Long = 2L
        //when & then
        mockMvc.perform(MockMvcRequestBuilders.get("$URL/$invalidId")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consul the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timeStamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should delete a customer by id and return nothing`() {
        // given
        val customer: Customer = customerRepository.save(builderCustomerDTO().toEntity())
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.delete("$URL/${customer.id}")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNoContent)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should try delete a customer with unexistent id and return bad request`() {
        // given
        val invalidId: Long = 1L
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.delete("$URL/$invalidId")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andDo(MockMvcResultHandlers.print())
    }

    private fun builderCustomerDTO(
        firstName: String = "Leo",
        lastName: String = "Big",
        cpf: String = "00000000191",
        income: BigDecimal = BigDecimal.valueOf(10000),
        email: String = "a@rouba",
        password: String = "123456",
        zipCode: String = "90160193",
        street: String = "Ipi"
        // id: Long = 1L
    ) = CustomerDTO (
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        income = income,
        email = email,
        password = password,
        zipCode = zipCode,
        street = street
        )


}