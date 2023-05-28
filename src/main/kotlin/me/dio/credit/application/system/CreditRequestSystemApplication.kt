package me.dio.credit.application.system

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CreditRequestSystemApplication

fun main(args: Array<String>) {
	runApplication<CreditRequestSystemApplication>(*args)
}
