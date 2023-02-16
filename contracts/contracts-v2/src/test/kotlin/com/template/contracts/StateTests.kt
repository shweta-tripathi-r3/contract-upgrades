package com.template.contracts

import com.template.states.DepositState
import org.junit.Test

class StateTests {
    @Test
    fun hasFieldOfCorrectType() {
        // Does the field exist?
        DepositState::class.java.getDeclaredField("amount")
        // Is the field of the correct type?
       // assertEquals(LoanState::class.java.getDeclaredField("amount").type, )
    }
}