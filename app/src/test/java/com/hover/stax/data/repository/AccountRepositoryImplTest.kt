package com.hover.stax.data.repository

import com.hover.stax.actions.ActionRepo
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.model.Account
import com.hover.stax.domain.repository.AccountRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
internal class AccountRepositoryImplTest {

    private lateinit var accountRepository: AccountRepository

    private val accounts = listOf(
        Account("Stanbic", "#fefefe"),
        Account("Safaricom", "#f2f2f2"),
        Account("Airtel", "#f3f3f3")
    )

    @Before
    fun setUp() {
        val accountRepo = mockk<AccountRepo>()
        val channelRepo = mockk<ChannelRepo>()
        val actionRepo = mockk<ActionRepo>()
        accountRepository = AccountRepositoryImpl(accountRepo, channelRepo, actionRepo, StandardTestDispatcher())
    }


    @Test
    fun `fetch accounts returns list of accounts`() = runTest {
        coEvery { accountRepository.fetchAccounts() } returns flowOf(accounts)

        val accountList = accountRepository.fetchAccounts()

        coVerify { accountRepository.fetchAccounts() }

        assert(accountList.count() > 0)
    }

}