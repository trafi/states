package com.trafi.phoneverification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PhoneVerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_verification_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PhoneVerificationFragment.newInstance())
                .commitNow()
        }
    }

}
