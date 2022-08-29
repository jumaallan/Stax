package com.hover.stax.domain.use_case.channels

import com.google.firebase.messaging.FirebaseMessaging
import com.hover.sdk.sims.SimInfo
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.domain.model.Channel
import com.hover.stax.domain.repository.ChannelRepository
import com.hover.stax.presentation.bounties.CODE_ALL_COUNTRIES
import kotlinx.coroutines.flow.flow

class ChannelsUseCase(private val channelRepository: ChannelRepository) {

    val publishedChannels = channelRepository.publishedChannels

    val presentSims = flow { emit(channelRepository.presentSims()) }

    fun updateCountryChannels(channels: List<Channel>, countryCode: String?): List<Channel> = if (countryCode.isNullOrEmpty() || countryCode == CODE_ALL_COUNTRIES)
        channels
    else
        channels.filter { it.countryAlpha2 == countryCode }

    suspend fun getCountryList(channels: List<Channel>): List<String> {
        val countryCodes = mutableListOf(CountryAdapter.CODE_ALL_COUNTRIES)
        countryCodes.addAll(channels.map { it.countryAlpha2 }.distinct().sorted())

        return countryCodes
    }

    suspend fun updateChannel(channel: Channel) = channelRepository.updateChannel(channel)

    suspend fun getChannelsByCountry(countryCode: String): List<Channel> = channelRepository.getChannelsByCountry(listOf(countryCode))

    fun setFirebaseSubscriptions(sims: List<SimInfo>) {
        if (sims.isEmpty()) return

        val hniList = mutableListOf<String>()

        sims.forEach { sim ->
            if (!hniList.contains(sim.osReportedHni) && sim.countryIso != null) {
                FirebaseMessaging.getInstance().subscribeToTopic("sim-".plus(sim.osReportedHni))
                FirebaseMessaging.getInstance().subscribeToTopic(sim.countryIso.uppercase())
                hniList.add(sim.osReportedHni)
            }
        }
    }

}