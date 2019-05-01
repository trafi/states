package com.trafi.phoneverification

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.trafi.state.StateMachine
import com.trafi.state.android.lifecycle.subscribeWithAutoDispose
import kotlinx.android.synthetic.main.phone_verification_fragment.*

class PhoneVerificationFragment : Fragment() {

    companion object {
        fun newInstance() = PhoneVerificationFragment()
    }

    private val machine by lazy { StateMachine(PhoneVerificationState("+4101234567")) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.phone_verification_fragment, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // events

        code_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                machine.transition(Event.CodeEntered(s.toString()))
            }
        })

        // .. other events are left as an exercise for the reader

        // outputs

        machine.subscribeWithAutoDispose(viewLifecycleOwner) { boundState, newState ->
            if (boundState?.code != newState.code) code_input.setTextAndSelection(newState.code)
            message.text = "We sent you a 4-digit code to ${newState.phone}"

            // .. other outputs are left as an exercise for the reader
        }
    }

}

private fun EditText.setTextAndSelection(text: CharSequence) {
    setText(text)
    setSelection(text.length)
}
