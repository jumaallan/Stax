package com.hover.stax.data.local.channels

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hover.stax.accounts.ChannelWithAccounts
import com.hover.stax.domain.model.Channel
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {

    @get:Query("SELECT * FROM channels WHERE published = 1 ORDER BY isFavorite DESC, name ASC")
    val publishedChannels: Flow<List<Channel>>

    @get:Query("SELECT * FROM channels ORDER BY name ASC")
    val allChannels: LiveData<List<Channel>>

    @Query("SELECT * FROM channels WHERE selected = :selected ORDER BY defaultAccount DESC, name ASC")
    fun getSelected(selected: Boolean): LiveData<List<Channel>>

    @Query("SELECT * FROM channels WHERE id IN (:channel_ids) ORDER BY name ASC")
    suspend fun getChannelsByIds(channel_ids: List<Int>): List<Channel>

    @Query("SELECT * FROM channels WHERE id IN (:channel_ids) ORDER BY name ASC")
    fun getChannels(channel_ids: IntArray): LiveData<List<Channel>>

    @Query("SELECT * FROM channels WHERE country_alpha2 = :countryCode ORDER BY name ASC")
    suspend fun getChannels(countryCode: String): List<Channel>

    @Query("SELECT * FROM channels WHERE country_alpha2 in (:countryCodes) ORDER BY name ASC")
    suspend fun getChannels(countryCodes: List<String>): List<Channel>

    @Query("SELECT * FROM channels WHERE country_alpha2 = :countryCode AND id IN (:channel_ids) ORDER BY name ASC")
    suspend fun getChannels(countryCode: String, channel_ids: IntArray): List<Channel>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannel(id: Int): Channel?

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    fun getLiveChannel(id: Int): LiveData<Channel>

    @get:Query("SELECT * FROM channels")
    val channels: List<Channel>

    @Transaction
    @Query("SELECT * FROM channels where selected = 1 ORDER BY name ASC")
    fun getChannelsAndAccounts(): List<ChannelWithAccounts>

    @Transaction
    @Query("SELECT * FROM channels where id = :id ORDER BY name ASC")
    fun getChannelAndAccounts(id: Int): ChannelWithAccounts?

    @get:Query("SELECT COUNT(id) FROM channels")
    val dataCount: Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg channels: Channel?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channel: Channel?)

    @Update
    suspend fun update(channel: Channel)

    @Update
    suspend fun updateAll(channel: List<Channel>)

    @Delete
    suspend fun delete(channel: Channel)

    @Query("DELETE FROM channels")
    suspend fun deleteAll()

}