package com.hover.stax.domain.repository

import android.content.Context
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.hover.stax.channels.Channel
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

interface ChannelRepository {

    val presentSims: List<SimInfo>

    suspend fun getChannelsByIds(ids: List<Int>): List<Channel>

    suspend fun getChannelsByCountryCode(ids: IntArray, countryCode: String): List<Channel>

    suspend fun filterChannels(countryCode: String, actions: List<HoverAction>): List<Channel>

    suspend fun updateChannels(data: JSONArray)
}