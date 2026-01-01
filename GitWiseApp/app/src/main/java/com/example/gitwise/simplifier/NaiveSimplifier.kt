package com.example.gitwise.NaiveSimplifier

import com.example.gitwise.simlifier.Simplifier
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.datatypes.Person


class NaiveSimplifier : Simplifier {
    fun netBalance(transactions: List<Transaction>): Map<Person, Long> {
        val balance = mutableMapOf<Person, Long>()

        // Step 1: Compute net balance per person
        for (transaction in transactions) {
            balance[transaction.payer] = (balance[transaction.payer] ?: 0L) + transaction.sum.toLong()
            balance[transaction.ower]  = (balance[transaction.ower]  ?: 0L) - transaction.sum.toLong()
        }

        return balance
    }

    override fun simplifiy(transactions: List<Transaction>): List<Transaction> {
            val balance = netBalance(transactions)

            // Separate creditors and debtors
            val creditors = ArrayDeque<Pair<Person, Long>>() // positive balance
            val debtors   = ArrayDeque<Pair<Person, Long>>() // negative balance

            for ((person, amount) in balance) {
                when {
                    amount > 0 -> creditors.add(person to amount)
                    amount < 0 -> debtors.add(person to amount)
                }
            }

            // Settle balances
            val result = mutableListOf<Transaction>()

            while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
                val (creditor, credit) = creditors.removeFirst()
                val (debtor, debt)     = debtors.removeFirst()

                val settled = minOf(credit, -debt)

                result.add(
                    Transaction(
                        payer = debtor,
                        ower = creditor,
                        sum = settled.toULong()
                    )
                )

                val remainingCredit = credit - settled
                val remainingDebt   = debt + settled

                if (remainingCredit > 0)
                    creditors.addFirst(creditor to remainingCredit)

                if (remainingDebt < 0)
                    debtors.addFirst(debtor to remainingDebt)
            }

            return result
    }
}
