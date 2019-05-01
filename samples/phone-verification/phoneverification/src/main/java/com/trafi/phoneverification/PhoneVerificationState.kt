package com.trafi.phoneverification

import com.trafi.phoneverification.Effect.*
import com.trafi.phoneverification.Event.*
import com.trafi.state.State

private const val START_WAIT_SECONDS = 10

sealed class Event {
    object SmsSent : Event()
    object SmsFailed : Event()
    data class CodeEntered(val code: String) : Event()
    object CodeVerified : Event()
    object CodeRejected : Event()
    data class SmsReceived(val message: String) : Event()
    object SecondPassed : Event()
    object ResendTapped : Event()
    object EffectHandled : Event()
}

data class PhoneVerificationState(
    val phone: String,
    val code: String = "",
    private val inProgress: Boolean = true,
    private val waitSeconds: Int = START_WAIT_SECONDS,
    val effect: Effect? = SendSms
): State<PhoneVerificationState, Event> {

    override fun reduce(event: Event) = when (event) {
        SmsSent -> copy(inProgress = false)
        SmsFailed -> copy(inProgress = false, waitSeconds = 0, effect = ShowError("Could not send SMS"))
        is CodeEntered -> checkCode(event.code)
        CodeVerified -> copy(inProgress = false, effect = Close)
        CodeRejected -> copy(inProgress = false, effect = ShowError("Code rejected"))
        is SmsReceived -> event.message.parseCode()?.let { checkCode(it) } ?: copy()
        SecondPassed -> copy(waitSeconds = maxOf(0, waitSeconds - 1))
        ResendTapped -> copy(effect = SendSms, inProgress = true, waitSeconds = START_WAIT_SECONDS)
        EffectHandled -> copy(effect = null)
    }

    private fun checkCode(code: String): PhoneVerificationState {
        if (code.length < 4) return copy(code = code)
        val trimmedCode = code.take(4)
        return copy(code = trimmedCode, inProgress = true, effect = CheckCode(phone, trimmedCode))
    }

    val showProgress get() = inProgress
    val resendEnabled get() = waitSeconds == 0
    val resendButtonText get() = waitSeconds.let { if (it > 0) "($it) " else "" } + "Resend text"
}

private fun String.parseCode() = "\\d{4}".toRegex().find(this)?.value

sealed class Effect {
    object SendSms : Effect()
    data class CheckCode(val phone: String, val code: String) : Effect()
    data class ShowError(val message: String) : Effect()
    object Close : Effect()
}
