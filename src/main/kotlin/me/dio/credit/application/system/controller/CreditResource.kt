package me.dio.credit.application.system.controller

import jakarta.validation.Valid
import me.dio.credit.application.system.dto.CreditDTO
import me.dio.credit.application.system.dto.CreditListView
import me.dio.credit.application.system.dto.CreditView
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.service.impl.CreditService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.stream.Collectors

@RestController
@RequestMapping("/api/credit")
class CreditResource(private val creditService: CreditService) {

    @PostMapping
    fun saveCredit(@RequestBody @Valid creditDTO: CreditDTO): ResponseEntity<String> {
        val credit: Credit = this.creditService.save(creditDTO.toEntity())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body("Credit ${credit.creditCode} - Customer ${credit.customer?.firstName} ${credit.customer?.lastName} saved")
    }

    @GetMapping
    fun findAllByCustomerId(@RequestParam(value = "customerId") customerId: Long): ResponseEntity<List<CreditListView>> {
        val creditList = this.creditService.findAllByCustomer(customerId).stream()
            .map { credit: Credit -> CreditListView(credit) }.collect(Collectors.toList())
        return ResponseEntity.status(HttpStatus.OK).body(creditList)
    }

    @GetMapping("/{creditCode}")
    fun findByCreditCode(@RequestParam(value = "customerId") customerId: Long,
        @PathVariable creditCode: UUID) : ResponseEntity<CreditView> {
        val credit: Credit = this.creditService.findByCreditCode(customerId, creditCode)!!
        return ResponseEntity.status(HttpStatus.OK).body(CreditView(credit))
    }

}