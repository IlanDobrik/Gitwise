package com.example.gitwise.datatypes

data class Person(val name: String)

data class Transaction(
    val payer: Person,
    val ower: Person,
    val sum: ULong
)