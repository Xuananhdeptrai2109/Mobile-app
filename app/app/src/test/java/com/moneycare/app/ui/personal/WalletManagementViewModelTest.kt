package com.moneycare.app.ui.personal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalletManagementViewModelTest {
    @Test
    fun normalizeWalletName_trimsValidName() {
        assertEquals("Ví du lịch", normalizeWalletName("  Ví du lịch  "))
    }

    @Test
    fun normalizeWalletName_rejectsBlankName() {
        assertNull(normalizeWalletName("   "))
    }

    @Test
    fun normalizeWalletName_rejectsNameLongerThanLimit() {
        assertNull(normalizeWalletName("a".repeat(101)))
    }
}
