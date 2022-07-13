package com.hover.stax.data.repository

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth.assertThat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessaging.getInstance
import com.hover.stax.ApplicationInstance
import com.hover.stax.actions.ActionRepo
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.model.Account
import com.hover.stax.domain.repository.AccountRepository
import com.hover.stax.notifications.PushNotificationsHelper
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.test.KoinTest


@OptIn(ExperimentalCoroutinesApi::class)
internal class AccountRepositoryImplTest : KoinTest {

    private lateinit var accountRepository: AccountRepository

    private lateinit var accountRepo: AccountRepo
    private lateinit var channelRepo: ChannelRepo
    private lateinit var actionRepo: ActionRepo

    private lateinit var firebaseMessaging: FirebaseMessaging
    private lateinit var context: Context

    private val accounts = listOf(
        Account("Stanbic", "#fefefe").apply { id = 1 },
        Account("Safaricom", "#f2f2f2").apply { id = 2 },
        Account("Airtel", "#f3f3f3").apply { id = 3 }
    )

    private val channels = listOf(
        Channel(651234, "KCB Bank").apply {
            logoUrl = "logo.jpg"
            accountNo = "12345676"
            countryAlpha2 = "KE"
            primaryColorHex = "#2A561E"
            secondaryColorHex = "#E2E2E2"
            defaultAccount = false
        },
        Channel(123478, "Telkom").apply {
            logoUrl = "logo.jpg"
            accountNo = "087776313"
            countryAlpha2 = "KE"
            primaryColorHex = "#3AB4H7"
            secondaryColorHex = "#EEEEEE"
            defaultAccount = false
        }
    )

    @Before
    fun setUp() {
        accountRepo = mockk()
        channelRepo = mockk()
        actionRepo = mockk()

        accountRepository = AccountRepositoryImpl(accountRepo, channelRepo, actionRepo, StandardTestDispatcher())

        firebaseMessaging = mockk()
        context = mockk()

//        every { context.getString(any(), any()) } answers { "aaa" }
    }

    @Test
    fun `fetch accounts returns list of accounts`() = runTest {
        coEvery { accountRepo.getAccounts() } returns flowOf(accounts)

        val accountList = accountRepository.fetchAccounts()

        coEvery { accountRepo.getAccounts() }

        assertThat(accountList.count()).isGreaterThan(0)
    }

    @Test
    fun `fetch accounts returns empty when no accounts are set`() = runTest {
        coEvery { accountRepo.getAccounts() } returns emptyFlow()

        val accountList = accountRepository.fetchAccounts()

        coEvery { accountRepo.getAccounts() }

        assertThat(accountList.count()).isEqualTo(0)
    }

    @Test
    fun `create accounts returns ids of created accounts`() = runTest(StandardTestDispatcher()) {
        startKoin {
            androidContext(ApplicationInstance())
        }

        coEvery { accountRepo.getDefaultAccountAsync() } returns accounts.first()
        every { actionRepo.getActions(channelId = any(), type = any()) } returns emptyList()
        every { channelRepo.update(channels = any()) } just runs
        coEvery { accountRepo.insert(accounts = any()) } returns accounts.map { it.id.toLong() }

        mockkStatic("com.google.firebase.messaging.FirebaseMessaging")
        every { getInstance() } returns firebaseMessaging

//        with(mockk<Context>()) { every { Context.getString(any()) } returns "aaa" }
        val subscribeToTopicTask = mockk<Task<Void>>()
        every { firebaseMessaging.subscribeToTopic(any()) } returns subscribeToTopicTask

        val accountIds = accountRepository.createAccounts(channels)

        coVerify { accountRepo.getDefaultAccountAsync() }
        verify { actionRepo.getActions(channelId = any(), type = any()) }
        verify { channelRepo.update(channels = any()) }
        verify { PushNotificationsHelper.joinChannelGroup(any(), any()) }
        coVerify { accountRepo.insert(accounts = any()) }

        assertThat(accountIds).contains(accounts.map { it.id.toLong() })
    }

}