package com.hover.stax.data.repository

import android.content.Context
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.hover.stax.R
import com.hover.stax.domain.model.Channel
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.data.local.channels.ChannelRepo
import com.hover.stax.domain.repository.ChannelRepository
import org.json.JSONArray

private const val MAX_LOOKUP_COUNT = 40

class ChannelRepositoryImpl(val channelRepo: ChannelRepo, val context: Context) : ChannelRepository {

    override suspend fun presentSims(): List<SimInfo> = channelRepo.presentSims

    override suspend fun getChannelsByIds(ids: List<Int>): List<Channel> = channelRepo.getChannelsByIds(ids)

    override suspend fun getChannelsByCountryCode(ids: IntArray, countryCode: String): List<Channel> = channelRepo.getChannelsByCountry(ids, countryCode)

    override suspend fun filterChannels(countryCode: String, actions: List<HoverAction>): List<Channel> {
        val ids = actions.asSequence().distinctBy { it.channel_id }.map { it.channel_id }.toList()

        return if (countryCode == CountryAdapter.CODE_ALL_COUNTRIES)
            getChunkedChannelsByIds(ids)
        else
            getChannelsByCountryCode(ids.toIntArray(), countryCode)
    }

    override suspend fun updateChannels(data: JSONArray) {
        for (j in 0 until data.length()) {
            var channel = channelRepo.getChannel(data.getJSONObject(j).getJSONObject("attributes").getInt("id"))
            if (channel == null) {
                channel = Channel(data.getJSONObject(j).getJSONObject("attributes"), context.getString(R.string.root_url))
                channelRepo.insert(channel)
            } else channelRepo.update(channel.update(data.getJSONObject(j).getJSONObject("attributes"), context.getString(R.string.root_url)))
        }
    }

    private suspend fun getChunkedChannelsByIds(ids: List<Int>): List<Channel> {
        val channels = mutableListOf<Channel>()

        ids.chunked(MAX_LOOKUP_COUNT).forEach { idList ->
            val results = channelRepo.getChannelsByIds(idList)
            channels.addAll(results)
        }

        return channels
    }

}