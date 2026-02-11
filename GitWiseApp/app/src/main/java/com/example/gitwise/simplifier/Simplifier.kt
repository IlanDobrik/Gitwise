package com.example.gitwise.simplifier

import com.example.gitwise.datatypes.Transaction


interface Simplifier {
    fun simplify(transactions: List<Transaction>) : List<Transaction>
}
