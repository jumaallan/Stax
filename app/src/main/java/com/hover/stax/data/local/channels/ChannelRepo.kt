package com.hover.stax.data.local.channels

import androidx.lifecycle.LiveData
import com.hover.sdk.database.HoverRoomDatabase
import com.hover.sdk.sims.SimInfo
import com.hover.sdk.sims.SimInfoDao
import com.hover.stax.channels.Channel
import com.hover.stax.database.AppDatabase

class ChannelRepo(db: AppDatabase, sdkDb: HoverRoomDatabase) {

    private val simDao: SimInfoDao = sdkDb.simDao()
    private val channelDao: ChannelDao = db.channelDao()

    // SIMs
    val presentSims: List<SimInfo>
        get() = simDao.present

    val publishedChannels: LiveData<List<Channel>> = channelDao.publishedChannels

    suspend fun getChannel(id: Int): Channel? = channelDao.getChannel(id)

    fun getLiveChannel(id: Int): LiveData<Channel> = channelDao.getLiveChannel(id)

    suspend fun getChannelsByIds(ids: List<Int>): List<Channel> = channelDao.getChannelsByIds(ids)

    suspend fun getChannelsByCountry(channelIds: IntArray, countryCode: String): List<Channel> = channelDao.getChannels(countryCode, channelIds)

    suspend fun getChannelsByCountry(countryCode: String): List<Channel> = channelDao.getChannels(countryCode.uppercase())

    suspend fun update(channel: Channel) = channelDao.update(channel)

    suspend fun insert(channel: Channel) = channelDao.insert(channel)

    suspend fun update(channels: List<Channel>) = channelDao.updateAll(channels)
}