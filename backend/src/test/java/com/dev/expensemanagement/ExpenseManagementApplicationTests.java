package com.dev.expensemanagement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExpenseManagementApplicationTests {

    @Test
    void applicationClassIsAvailableWithoutStartingTheDevelopmentDatabase() {
        assertNotNull(ExpenseManagementApplication.class);
    }

}
