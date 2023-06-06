package me.dio.credit.application.system.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import jakarta.persistence.*
import me.dio.credit.application.system.dto.CreditView
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.enumeration.Status
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.service.impl.CreditService
import me.dio.credit.application.system.service.impl.CustomerService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import java.util.Optional.*
import java.util.UUID.*

//@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class CreditServiceTest {

    @MockK
    lateinit var creditRepository: CreditRepository
    @MockK
    lateinit var customerService: CustomerService
    @InjectMockKs
    lateinit var creditService: CreditService

    @Test
    fun `should create a credit`() {
        // given
        val fakeCustomer: Customer = buildCustomer()
        val fakeCredit: Credit = buildCredit()
        every { customerService.findById(any()) } returns fakeCustomer
        every { creditRepository.save(any()) } returns fakeCredit
        // when
        val actual = creditService.save(fakeCredit)
        // then
        Assertions.assertThat(actual).isNotNull
        Assertions.assertThat(actual).isSameAs(fakeCredit)
        verify(exactly = 1) { creditRepository.save(fakeCredit) }
    }

    @Test
    fun `should find all credits of a customer`() {
        // given
        val fakeId: Long = Random().nextLong()
        val fakeCreditList = mutableListOf<Credit>()
        every { creditRepository.findAllByCustomer(fakeId) } returns fakeCreditList

        // when
        val actual = creditService.findAllByCustomer(fakeId)
        // then
        Assertions.assertThat (actual).isNotNull
        Assertions.assertThat(actual).isSameAs(fakeCreditList)
        verify(exactly = 1) { creditRepository.findAllByCustomer(fakeId) }
    }

    @Test
    fun `should find a credit by creditCode and customer id`() {
        // given
        val fakeCustomer: Customer = buildCustomer()
        val fakeCredit: Credit = buildCredit()
        every { customerService.save(fakeCustomer)} returns fakeCustomer
        every { creditRepository.findByCreditCode(UUID.fromString("fd1007b1-c350-4fe7-b3c7-e60e307f02e5")) } returns fakeCredit
        // when
        val actual = creditService.findByCreditCode(1L, UUID.fromString("fd1007b1-c350-4fe7-b3c7-e60e307f02e5"))
        // then
        Assertions.assertThat(actual).isNotNull
        Assertions.assertThat(actual).isExactlyInstanceOf(Credit::class.java)
    }





    private fun buildCustomer(
        firstName: String = "Leo",
        lastName: String = "Big",
        cpf: String = "00000000191",
        income: BigDecimal = BigDecimal.valueOf(10000),
        email: String = "a@rouba",
        password: String = "123456",
        zipCode: String = "90160193",
        street: String = "Ipi",
        id: Long = 1L
    ) = Customer(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        income = income,
        email = email,
        password = password,
        address = Address(
            zipCode,
            street
        ),
        id = id
    )

    private fun buildCredit (
        creditCode: UUID = UUID.fromString("fd1007b1-c350-4fe7-b3c7-e60e307f02e5"),
        creditValue: BigDecimal = BigDecimal.valueOf(100000),
        dayFirstInstallment: LocalDate = LocalDate.of(2024,6,26),
        numberOfInstallments: Int = 60,
        customer: Customer?  = Customer(id = 1L),
        status: Status = Status.IN_PROGRESS,
        id: Long = 1L
    ) = Credit (
        creditCode = creditCode,
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customer = customer,
        status = status,
        id = id
    )

    private fun buildCreditView(
        creditCode: UUID = UUID.fromString("fd1007b1-c350-4fe7-b3c7-e60e307f02e5"),
        creditValue: BigDecimal = BigDecimal.valueOf(100000),
        numberOfInstallments: Int = 60,
        status: Status = Status.IN_PROGRESS,
        incomeCostumer: BigDecimal = BigDecimal.valueOf(10000),
        emailCustomer: String = "a@rouba",
    ) = CreditView(
        creditCode = creditCode,
        creditValue = creditValue,
        numberOfInstallments = numberOfInstallments,
        status = status,
        emailCustomer = emailCustomer,
        incomeCustomer = incomeCostumer
    )
}