package com.hover.stax.accounts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Constants.size55
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxDropdownLayout
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import timber.log.Timber


class AccountDropdown(context: Context, attributeSet: AttributeSet) : StaxDropdownLayout(context, attributeSet), Target {

    private var showSelected: Boolean = true
    private var helperText: String? = null
    private var highlightListener: HighlightListener? = null

    var highlightedAccount: Account? = null

    init {
        getAttrs(context, attributeSet)
    }

    private fun getAttrs(context: Context, attributeSet: AttributeSet) {
        val attributes = context.theme.obtainStyledAttributes(attributeSet, R.styleable.ChannelDropdown, 0, 0)

        try {
            showSelected = attributes.getBoolean(R.styleable.ChannelDropdown_show_selected, true)
            helperText = attributes.getString(R.styleable.ChannelDropdown_initial_helper_text)
        } finally {
            attributes.recycle()
        }
    }

    fun setListener(listener: HighlightListener) {
        highlightListener = listener
    }

    private fun accountUpdate(accounts: List<Account>) {
        if (!accounts.isNullOrEmpty() /*&& !hasExistingContent()*/) {
            updateChoices(accounts)
        } else if (!hasExistingContent()) {
            setState(context.getString(R.string.accounts_error_no_accounts), NONE)
        }
    }

    private fun setDropdownValue(account: Account?) {
        account?.let {
            autoCompleteTextView.setText(it.alias, false)
            UIHelper.loadPicasso(it.logoUrl, size55, this)
            highlightedAccount = account
        }
    }

    private fun updateChoices(accounts: List<Account>) {
        if (highlightedAccount == null) setDropdownValue(null)

        val adapter = AccountDropdownAdapter(accounts, context)
        autoCompleteTextView.apply {
            setAdapter(adapter)
            setOnItemClickListener { parent, _, position, _ -> onSelect(parent.getItemAtPosition(position) as Account) }
        }

        onSelect(accounts.firstOrNull { it.isDefault })
    }

    private fun onSelect(account: Account?) {
        setDropdownValue(account)
        account?.let { highlightListener?.highlightAccount(it) }
    }

    private fun hasExistingContent(): Boolean = autoCompleteTextView.adapter != null && autoCompleteTextView.adapter.count > 0

    fun setObservers(viewModel: ChannelsViewModel, lifecycleOwner: LifecycleOwner) {
        with(viewModel) {
            val selectedObserver = object : Observer<List<Channel>> {
                override fun onChanged(t: List<Channel>?) {
                    Timber.e("Got new selected channels ${t?.size}")
                }
            }

            accounts.observe(lifecycleOwner) { accountUpdate(it) }

            selectedChannels.observe(lifecycleOwner, selectedObserver)
            activeAccount.observe(lifecycleOwner) { if (it != null && showSelected) setState(helperText, NONE); Timber.e("Setting state null") }
            channelActions.observe(lifecycleOwner) {
                setState(it, viewModel)
            }
        }
    }

    private fun setState(actions: List<HoverAction>, viewModel: ChannelsViewModel) {
        when {
            viewModel.activeAccount.value != null && (actions.isNullOrEmpty()) -> setState(
                context.getString(
                    R.string.no_actions_fielderror,
                    HoverAction.getHumanFriendlyType(context, viewModel.getActionType())
                ), ERROR
            )

            !actions.isNullOrEmpty() && actions.size == 1 && !actions.first().requiresRecipient() && viewModel.getActionType() != HoverAction.BALANCE ->
                setState(
                    context.getString(
                        if (actions.first().transaction_type == HoverAction.AIRTIME) R.string.self_only_airtime_warning
                        else R.string.self_only_money_warning
                    ), INFO
                )

            viewModel.activeAccount.value != null && showSelected -> setState(helperText, SUCCESS)
        }
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        val d: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
        d.isCircular = true
        autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null)
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        Timber.e(e)
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_grey_circle_small, 0, 0, 0)
    }

    interface HighlightListener {
        fun highlightAccount(account: Account)
    }
}