package com.hover.stax.data.repository

import com.google.common.truth.Truth.assertThat
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.domain.repository.FinancialTipsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class FinancialTipsRepositoryImplTest {

    private lateinit var financialTipsRepository: FinancialTipsRepository

    private val tipsList = listOf(
        FinancialTip("1241", "This is a test tip", "Content is also a sample", "Snippet can be longer", 125543636, "This is for sharing", null),
        FinancialTip("1241", "This is a test tip", "Content is also a sample", "Snippet can be longer", 125543636, "This is for sharing", null),
        FinancialTip("1241", "This is a test tip", "Content is also a sample", "Snippet can be longer", 125543636, "This is for sharing", null),
        FinancialTip("1241", "This is a test tip", "Content is also a sample", "Snippet can be longer", 125543636, "This is for sharing", null),
        FinancialTip("1241", "This is a test tip", "Content is also a sample", "Snippet can be longer", 125543636, "This is for sharing", null)
    )

    /**
     * Uses a fake repository to avoid implementing tests on Firebase.
     * Only Stax code is under test, not Firebase.
     */
    @Before
    fun setup() {
        financialTipsRepository = mockk<FakeFinancialTipsRepositoryImpl>()
    }

    @Test
    fun `fetch tips from firebase returns a list of tips`() = runTest {
        coEvery { financialTipsRepository.fetchTips() } returns flowOf(tipsList)

        val tips = financialTipsRepository.fetchTips().count()

        coVerify { financialTipsRepository.fetchTips() }

        assertThat(tips).isGreaterThan(0)
    }

    @Test
    fun `fetch tips from firebase returns empty list`() = runTest {
        coEvery { financialTipsRepository.fetchTips() } returns emptyFlow()

        val tips = financialTipsRepository.fetchTips().count()

        coVerify { financialTipsRepository.fetchTips() }

        assertThat(tips).isEqualTo(0)
    }

}