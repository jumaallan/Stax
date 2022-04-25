package com.hover.stax.login

import android.app.Application
import android.content.Intent
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.hover.stax.R
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException


private const val EMAIL = "email"
private const val USERNAME = "username"
private const val BOUNTY_EMAIL_KEY = "email_for_bounties"

class LoginViewModel(val repo: ScheduleRepo, val application: Application) : ViewModel() {

    lateinit var signInClient: GoogleSignInClient

    val user = MutableLiveData<GoogleSignInAccount>()
    private var optedIn = MutableLiveData(false)

    var email = MediatorLiveData<String?>()
    var progress = MutableLiveData(-1)
    var error = MutableLiveData<String>()
    var username = MediatorLiveData<String?>()

    val postGoogleAuthNav = MutableLiveData<Int>()

    init {
        getEmail()
        getUsername()
    }

    fun signIntoGoogle(data: Intent?, inOrOut: Boolean) {
        optedIn.value = inOrOut
        progress.value = 25
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            setUser(account, account.idToken!!)
            progress.value = 33
        } catch (e: ApiException) {
            Timber.e(e, "Google sign in failed")
            onError(application.getString(R.string.login_google_err))
        }
    }

    private fun uploadUserToStax(email: String?, username: String?, token: String?) {
        if (getUsername().isNullOrEmpty() && !email.isNullOrEmpty()) {
            Timber.e("Uploading user to stax")
            progress.value = 66
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val result = LoginNetworking(application).uploadUserToStax(email, username, optedIn.value!!, token)
                    Timber.e("Uploading user to stax came back: ${result.code}")

                    if (result.code in 200..299) onSuccess(
                        JSONObject(result.body!!.string())
                    )
                    else onError(application.getString(R.string.upload_user_error))
                } catch (e: IOException) {
                    onError(application.getString(R.string.upload_user_error))
                }
            }
        }
    }

    fun uploadLastUser() {
        val account = GoogleSignIn.getLastSignedInAccount(application)
        if (account != null) uploadUserToStax(email.value, account.displayName!!, account.idToken)
        else Timber.e("No account found")
    }

    private fun onSuccess(json: JSONObject) {
        Timber.e(json.toString())

        progress.postValue(100)
        saveResponseData(json)
    }

    private fun setUser(signInAccount: GoogleSignInAccount, idToken: String) {
        Timber.e("setting user: %s", signInAccount.email)
        user.postValue(signInAccount)
        setEmail(signInAccount.email)

        uploadUserToStax(signInAccount.email, signInAccount.displayName, idToken)
    }

    private fun saveResponseData(json: JSONObject?) {
        val data = json?.optJSONObject("data")?.optJSONObject("attributes")
        setUsername(data?.optString("username"))
    }

    fun usernameIsNotSet(): Boolean = getUsername().isNullOrEmpty()

    private fun setUsername(name: String?) {
        Timber.e("setting username %s", name)
        if (!name.isNullOrEmpty()) {
            username.postValue(name)
            Utils.saveString(USERNAME, name, application)
        }
    }

    private fun setEmail(address: String?) {
        Utils.saveString(EMAIL, address, application)
        email.postValue(address)
    }

    private fun getEmail(): String? {
        if (Utils.getString(EMAIL, application) == null && Utils.getString(BOUNTY_EMAIL_KEY, application) != null) {
            email.value = Utils.getString(BOUNTY_EMAIL_KEY, application)
            Utils.saveString(EMAIL, email.value, application)
        } else email.value = Utils.getString(EMAIL, application)
        return email.value
    }

    private fun getUsername(): String? {
        username.value = Utils.getString(USERNAME, application)
        return username.value
    }

    //Sign out user if any step of the login process fails. Have user restart the flow
    private fun onError(message: String) {
        Timber.e(message)
        signInClient.signOut().addOnCompleteListener {
            AnalyticsUtil.logErrorAndReportToFirebase(LoginViewModel::class.java.simpleName, message, null)
            AnalyticsUtil.logAnalyticsEvent(message, application)

            resetAccountDetails()

            progress.postValue(-1)
            error.postValue(message)
        }
    }

    fun silentSignOut() = signInClient.signOut().addOnCompleteListener {
        AnalyticsUtil.logAnalyticsEvent(application.getString(R.string.logout), application)
        resetAccountDetails()

        email.value = null
        username.value = null

        progress.postValue(-1)
    }

    private fun resetAccountDetails() {
        Utils.removeString(EMAIL, application)
        Utils.removeString(USERNAME, application)
    }

}