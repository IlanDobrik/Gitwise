package com.example.gitwise.datatypes

import java.io.Serializable
import java.util.UUID

data class Person(val name: String): Serializable

data class Debt(val person: Person, val amount: ULong): Serializable

data class Transaction(
    val reason: String? = null,
    val payer: Person,
    val debts: List<Debt>,

    // Always last for convenience
    val id: UUID = UUID.randomUUID(),
    val isValid: Boolean = true
): Serializable {
    val sum: ULong get() = debts.sumOf { it.amount }
}
