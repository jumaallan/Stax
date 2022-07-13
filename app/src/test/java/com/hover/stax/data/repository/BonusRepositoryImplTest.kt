@file:OptIn(ExperimentalCoroutinesApi::class)

package com.hover.stax.data.repository

import com.google.common.truth.Truth.assertThat
import com.hover.sdk.sims.SimInfo
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.data.local.bonus.BonusRepo
import com.hover.stax.domain.model.Bonus
import com.hover.stax.domain.repository.BonusRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class BonusRepositoryImplTest {

    private lateinit var bonusRepository: BonusRepository

    @MockK
    private lateinit var bonusRepo: BonusRepo

    @MockK
    private lateinit var channelRepo: ChannelRepo

    private val bonusList = listOf(
        Bonus(124425, 42624, 0.05, "Get 5% bonus airtime on your purchase"),
        Bonus(676262, 35626, 0.05, "Get 5% bonus airtime on your purchase")
    )

    private val channels = listOf(
        Channel(651234, "KCB Bank").apply {
            logoUrl = "logo.jpg"
            accountNo = "12345676"
            countryAlpha2 = "KE"
            primaryColorHex = "#2A561E"
            secondaryColorHex = "#E2E2E2"
            defaultAccount = false
            hniList = "KE, TZ"
        },
        Channel(123478, "Telkom").apply {
            logoUrl = "logo.jpg"
            accountNo = "087776313"
            countryAlpha2 = "KE"
            primaryColorHex = "#3AB4H7"
            secondaryColorHex = "#EEEEEE"
            defaultAccount = false
            hniList = "KE, TZ"
        }
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        bonusRepository = BonusRepositoryImpl(bonusRepo, channelRepo, StandardTestDispatcher())
    }

    @Test
    fun fetchBonuses() = runTest {

    }

    @Test
    fun getBonusList() = runTest(StandardTestDispatcher()) {
        coEvery { bonusRepo.bonuses } returns flowOf(bonusList)
        coEvery { channelRepo.presentSims } returns listOf(SimInfo(), SimInfo())
        coEvery { channelRepo.getChannelsByIdsAsync(any()) } returns channels

        val items = bonusRepository.getBonusList().count()

        coVerify { bonusRepo.bonuses }

        assertThat(items).isGreaterThan(0)
    }

}