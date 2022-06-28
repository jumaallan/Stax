package com.hover.stax.presentation.home

import com.hover.stax.data.model.Account
import com.hover.stax.domain.model.Bonus
import com.hover.stax.domain.model.FinancialTip

data class HomeState (
    val bonuses: List<Bonus> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val financialTips: List<FinancialTip> = emptyList()
)
