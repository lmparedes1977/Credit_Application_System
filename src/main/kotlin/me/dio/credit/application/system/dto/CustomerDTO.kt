package me.dio.credit.application.system.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Customer
import org.hibernate.validator.constraints.br.CPF
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal


data class CustomerDTO (
    @field:NotEmpty(message = "Invalid input") val firstName: String,
    @field:NotEmpty(message = "Invalid input") val lastName: String,
    @field:CPF(message = "Invalid CPF") val cpf: String,
    @field:NotNull(message = "Invalid input") val income: BigDecimal,
    @field:Email(message = "Invalid email")
    @field:NotEmpty(message = "Invalid input") val email: String,
    @field:NotEmpty(message = "Invalid input") val password: String,
    @field:NotEmpty(message = "Invalid input") val zipCode: String,
    @field:NotEmpty(message = "Invalid input") val street: String
) {
    fun toEntity(): Customer = Customer(
        firstName = this.firstName,
        lastName = this.lastName,
        cpf = this.cpf,
        income = this.income,
        email = this.email,
        password = this.password,
        address = Address(this.zipCode, this.street)
    )
}


