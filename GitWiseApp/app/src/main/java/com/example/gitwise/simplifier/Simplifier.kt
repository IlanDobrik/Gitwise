package com.example.gitwise.simlifier

import com.example.gitwise.datatypes.Transaction


interface Simplifier {
    fun simplifiy(transactions: List<Transaction>) : List<Transaction>
}
