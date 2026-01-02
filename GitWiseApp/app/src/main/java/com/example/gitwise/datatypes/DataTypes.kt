package com.example.gitwise.datatypes

import java.io.Serializable
import java.util.UUID

data class Person(val name: String): Serializable

data class Transaction(
    val reason: String? = null,
    val payer: Person,
    val ower: Person,
    val sum: ULong,

    // Always last for convenience
    val id: UUID = UUID.randomUUID(),
): Serializable
