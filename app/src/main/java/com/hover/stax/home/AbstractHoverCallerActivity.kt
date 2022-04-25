package com.hover.stax.home

import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.hover.HoverSession
import com.hover.stax.hover.HoverViewModel
import com.hover.stax.login.AbstractGoogleAuthActivity
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

abstract class AbstractHoverCallerActivity : AbstractGoogleAuthActivity(), PushNotificationTopicsInterface {

    private val viewModel: HoverViewModel by viewModel()
    private val balancesViewModel: BalancesViewModel by viewModel()

    private fun runAction(hsb: HoverSession.Builder) = try {
        hsb.run()
    } catch (e: Exception) {
        runOnUiThread { UIHelper.flashMessage(this, getString(R.string.error_running_action)) }

        val data = JSONObject()
        try {
            data.put("actionId", hsb.action.id)
        } catch (ignored: JSONException) {
        }

        AnalyticsUtil.logAnalyticsEvent("Failed Actions", data, this)
        Timber.e(e)
    }

    fun run(account: Account, type: String, extras: HashMap<String, String>?, index: Int) {
        run(account, viewModel.getAction(account.channelId, type), extras, index)
    }

    fun run(account: Account, action: HoverAction, extras: HashMap<String, String>?, index: Int) {
        val hsb = HoverSession.Builder(action, account, this@AbstractHoverCallerActivity,index)
        if (!extras.isNullOrEmpty()) hsb.extras(extras)
        runAction(hsb)
    }

    fun makeCall(action: HoverAction, account: Account) {
        val hsb = HoverSession.Builder(action, account, this@AbstractHoverCallerActivity, Constants.REQUEST_REQUEST)
        runAction(hsb)
    }

    fun retry(transaction: StaxTransaction) {
        lifecycleScope.launch(Dispatchers.IO) {
//            val actionAndChannelPair = balancesViewModel.getActionAndChannel(transaction.action_id, transaction.channel_id)
//            val accountNumber = contactRepo.getAccountNumber(transaction.counterparty_id)

//            val hsb = HoverSession.Builder(transaction.action_id, transaction.account, this@AbstractHoverCallerActivity, Constants.TRANSFERRED_INT)
//                .extra(HoverAction.AMOUNT_KEY, Utils.formatAmount(transaction.amount.toString()))
//                .extra(HoverAction.ACCOUNT_KEY, accountNumber)
//                .extra(HoverAction.PHONE_KEY, accountNumber)

//            runAction(hsb)
        }
    }

//    private fun makeCall(action: HoverAction, account: Account? = null, selectedAccount: Account? = null) {
//        val hsb = HoverSession.Builder(action, channel ?: channelsViewModel.activeChannel.value!!, this, getRequestCode(action.transaction_type))
//
//        if (action.transaction_type != HoverAction.FETCH_ACCOUNTS) {
//            hsb.extra(HoverAction.AMOUNT_KEY, transferViewModel.amount.value)
//                    .extra(HoverAction.NOTE_KEY, transferViewModel.note.value)
//                    .extra(Constants.ACCOUNT_NAME, selectedAccount?.name)
//
//            actionSelectViewModel.nonStandardVariables.value?.forEach {
//                hsb.extra(it.key, it.value)
//            }
//
//            selectedAccount?.run { hsb.setAccountId(id.toString()) }
//            transferViewModel.contact.value?.let { addRecipientInfo(hsb) }
//        }
//
//        runAction(hsb)
//    }

//fun submitPaymentRequest(action: HoverAction, account: Account) {
//    val hsb = HoverSession.Builder(action, account, this, Constants.PAYBILL_REQUEST)
//        .extra(HoverAction.AMOUNT_KEY, paybillViewModel.amount.value)
//        .extra("businessNo", paybillViewModel.businessNumber.value)
//        .extra(Constants.ACCOUNT_NAME, account.name)
//        .extra(HoverAction.ACCOUNT_KEY, paybillViewModel.accountNumber.value)
//
//    runAction(hsb)
//
//    val data = JSONObject()
//    try {
//        data.put("businessNo", paybillViewModel.businessNumber.value)
//    } catch (e: Exception) {
//        Timber.e(e)
//    }
//
//    AnalyticsUtil.logAnalyticsEvent(getString(R.string.finish_transfer, TransactionType.type), data, this)
//}

    private fun getRequestCode(transactionType: String): Int {
        return if (transactionType == HoverAction.FETCH_ACCOUNTS) Constants.FETCH_ACCOUNT_REQUEST
        else Constants.TRANSFER_REQUEST
    }

    fun makeCall(a: HoverAction) {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_run_bounty_session), this)
        updatePushNotifGroupStatus(a)
        call(a.public_id)
    }

    fun retryCall(actionId: String) {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_retry_bounty_session), this)
        call(actionId)
    }

    private fun call(actionId: String) {
        val i = HoverParameters.Builder(this).request(actionId).setEnvironment(HoverParameters.MANUAL_ENV).buildIntent()
        startActivityForResult(i, Constants.BOUNTY_REQUEST)
    }

    private fun updatePushNotifGroupStatus() {
        joinTransactionGroup(this)
        leaveNoUsageGroup(this)
    }

    private fun updatePushNotifGroupStatus(a: HoverAction) {
        joinAllBountiesGroup(this)
        joinBountyCountryGroup(a.country_alpha2.uppercase(), this)
    }
}