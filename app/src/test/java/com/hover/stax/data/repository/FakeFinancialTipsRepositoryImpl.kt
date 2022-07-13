package com.hover.stax.data.repository

import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.domain.repository.FinancialTipsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFinancialTipsRepositoryImpl: FinancialTipsRepository {

    private val tipsList = listOf(
        FinancialTip("1241", "This is a test tip", "Content is also a sample", "Snippet can be longer", 125543636, "This is for sharing", null),
        FinancialTip("1241", "This is a test tip", "Content is also a sample", "Snippet can be longer", 125543636, "This is for sharing", null),
        FinancialTip("1241", "This is a test tip", "Content is also a sample", "Snippet can be longer", 125543636, "This is for sharing", null),
        FinancialTip("1241", "This is a test tip", "Content is also a sample", "Snippet can be longer", 125543636, "This is for sharing", null),
        FinancialTip("1241", "This is a test tip", "Content is also a sample", "Snippet can be longer", 125543636, "This is for sharing", null)
    )

    override suspend fun fetchTips(): Flow<List<FinancialTip>> {
        return flowOf(tipsList)
    }
}