package com.example.gitwise.simlifier

import com.example.gitwise.datatypes.Transaction


interface Simplifier {
    fun simplify(transactions: List<Transaction>) : List<Transaction>
}
