package com.hover.stax.presentation.channels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hover.sdk.api.Hover
import com.hover.sdk.sims.SimInfo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.Channel
import com.hover.stax.domain.use_case.accounts.CreateAccountsUseCase
import com.hover.stax.domain.use_case.accounts.GetAccountsUseCase
import com.hover.stax.domain.use_case.bonus.GetBonusesUseCase
import com.hover.stax.domain.use_case.channels.ChannelsUseCase
import com.hover.stax.presentation.bounties.CODE_ALL_COUNTRIES
import com.hover.stax.utils.Utils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel as KChannel

class AddChannelViewModel(
    private val channelsUseCase: ChannelsUseCase,
    private val accountsUseCase: GetAccountsUseCase,
    private val createAccountsUseCase: CreateAccountsUseCase,
    private val bonusesUseCase: GetBonusesUseCase,
    private val application: Application
) : ViewModel() {

    private var simReceiver: BroadcastReceiver? = null
    private val sims = MutableStateFlow<List<SimInfo>>(emptyList())
    private val allChannels = MutableStateFlow<List<Channel>>(emptyList())

    private val accountChannel = KChannel<Account>()
    val accountCallback = accountChannel.receiveAsFlow()

    private val accountCreatedEvent = MutableSharedFlow<Boolean>()
    val accountEventFlow = accountCreatedEvent.asSharedFlow()

    private val _channelsState: MutableStateFlow<ChannelsState> = MutableStateFlow(ChannelsState())
    private val channelsState = _channelsState.asStateFlow()

    init {
        setSimBroadCastReceiver()
        loadSims()

        loadData()
    }

    private fun loadSims() {
        updateSims()

        simReceiver?.let {
            LocalBroadcastManager.getInstance(application)
                .registerReceiver(it, IntentFilter(Utils.getPackage(application).plus(".NEW_SIM_INFO_ACTION")))
        }

        Hover.updateSimInfo(application)
    }

    private fun setSimBroadCastReceiver() {
        simReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                updateSims()
            }
        }
    }

    private fun updateSims() = viewModelScope.launch {
        channelsUseCase.presentSims.collect {
            sims.update { it }
        }
    }

    private fun loadData() {
        sims.onEach {
            channelsUseCase.setFirebaseSubscriptions(it)

            onSimUpdate(it.map { sim -> sim.countryIso }.toSet())
        }.launchIn(viewModelScope)

        channelsUseCase.publishedChannels.onEach { channels ->
            allChannels.update { channels }

            loadChannelCountryList(channels)
        }.launchIn(viewModelScope)
    }

    private fun onSimUpdate(countryCodes: Set<String>) = viewModelScope.launch {
        if (countryCodes.isEmpty()) return@launch

        countryCodes.forEach { code ->
            if (channelsUseCase.getChannelsByCountry(code).isNotEmpty()) {
                updateCountry(code)
            }
        }
    }

    private fun loadChannelCountryList(channels: List<Channel>) = viewModelScope.launch {
        _channelsState.update { it.copy(countries = channelsUseCase.getCountryList(channels)) }
    }

    private fun updateCountry(code: String) {
        _channelsState.update { it.copy(country = code) }

        updateCountryChannels(code.uppercase())
    }

    private fun updateCountryChannels(code: String) = viewModelScope.launch {
        val channels = when {
            code.isEmpty() || code == CODE_ALL_COUNTRIES -> allChannels.value
            else -> allChannels.value.filter { it.countryAlpha2 == code }
        }

        runFilter(channels = channels)
    }

    fun search(query: String) = runFilter(query = query)

    private fun runFilter(channels: List<Channel> = allChannels.value, query: String? = "") = viewModelScope.launch {
        val channelList = channels.filter { standardizeString(it.toString()).contains(standardizeString(query)) }

        bonusesUseCase.bonusList.collect { list ->
            val ids = list.map { it.purchaseChannel }
            val results = if (ids.isEmpty())
                channelList
            else
                channelList.filterNot { ids.contains(it.id) }

            _channelsState.update { it.copy(channels = results) }
        }
    }

    private fun standardizeString(value: String?): String {
        // a non null String always contains an empty string
        if (value == null) return ""
        return value.lowercase().replace(" ", "").replace("#", "").replace("-", "");
    }

    fun validateAccount(channelId: Int) = viewModelScope.launch {
        val accounts = accountsUseCase.getAccountsByChannel(channelId)

        if (accounts.isEmpty())
            createAccountsUseCase.createAccount(channelId)

        accountCreatedEvent.emit(true)
    }

    fun createAccounts(channels: List<Channel>) = viewModelScope.launch {
        val accountIds = createAccountsUseCase(channels)
        promptBalanceCheck(accountIds.first().toInt())
    }

    private fun promptBalanceCheck(accountId: Int) = viewModelScope.launch {
        val account = accountsUseCase.getAccount(accountId)
        account?.let { accountChannel.send(it) }
    }

    override fun onCleared() {
        try {
            simReceiver?.let {
                LocalBroadcastManager.getInstance(application).unregisterReceiver(it)
            }
        } catch (ignored: Exception) {
        }
        super.onCleared()
    }
}

data class ChannelsState(
    val isLoading: Boolean = false,
    val country: String = "",
    val countries: List<String> = emptyList(),
    val channels: List<Channel> = emptyList()
)