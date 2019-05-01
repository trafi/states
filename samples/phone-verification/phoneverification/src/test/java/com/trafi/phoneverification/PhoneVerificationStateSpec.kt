package com.trafi.phoneverification

import com.trafi.phoneverification.Effect.*
import com.trafi.phoneverification.Event.*
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object PhoneVerificationStateSpec : Spek({
    describe("Phone verification") {

        val phone = "+00000000000"
        var state = PhoneVerificationState(phone)
        beforeEach { state = PhoneVerificationState(phone) }

        it("immediately sends SMS") { assertEquals(SendSms, state.effect) }
        it("shows progress") { assertTrue(state.showProgress) }

        describe("when SMS is sent") {
            beforeEach { state = state.reduce(EffectHandled).reduce(SmsSent) }

            it("does not show progress") { assertFalse(state.showProgress) }

            describe("when SMS is received") {
                beforeEach { state = state.reduce(SmsReceived("Your code is 1234")) }

                it("parses code correctly") { assertEquals("1234", state.code) }
                it("checks code") { assertEquals(CheckCode(phone, code = "1234"), state.effect) }
                it("shows progress") { assertTrue(state.showProgress) }

                describe("when code is correct") {
                    beforeEach { state = state.reduce(CodeVerified) }

                    it("does not show progress") { assertFalse(state.showProgress) }
                    it("is complete") { assertEquals(Close, state.effect) }
                }

                describe("when code is incorrect") {
                    beforeEach { state = state.reduce(CodeRejected) }

                    it("does not show progress") { assertFalse(state.showProgress) }
                    it("shows error") { assertThat(state.effect, instanceOf(ShowError::class.java)) }
                }
            }

            describe("when unrelated SMS is received") {
                beforeEach { state = state.reduce(SmsReceived("Welcome to #AppBuilders19")) }

                it("does not parse code") { assertEquals("", state.code) }
                it("does not check code") { assertNull(state.effect) }
            }

            describe("when incomplete code is entered") {
                beforeEach { state = state.reduce(CodeEntered("1")) }

                it("updates code correctly") { assertEquals("1", state.code) }
                it("does not check code yet") { assertNull(state.effect) }
                it("does not show progress") { assertFalse(state.showProgress) }

                describe("when complete code is entered") {
                    beforeEach { state = state.reduce(CodeEntered("1234")) }

                    it("updates code correctly") { assertEquals("1234", state.code) }
                    it("checks code") { assertEquals(CheckCode(phone, code = "1234"), state.effect) }
                    it("shows progress") { assertTrue(state.showProgress) }

                    describe("when code is correct") {
                        beforeEach { state = state.reduce(CodeVerified) }

                        it("does not show progress") { assertFalse(state.showProgress) }
                        it("is complete") { assertEquals(Close, state.effect) }
                    }

                    describe("when code is incorrect") {
                        beforeEach { state = state.reduce(CodeRejected) }

                        it("does not show progress") { assertFalse(state.showProgress) }
                        it("shows error") { assertThat(state.effect, instanceOf(ShowError::class.java)) }
                    }
                }
            }

            it("does not allow resend") { assertFalse(state.resendEnabled) }

            describe("when 7 seconds pass") {
                beforeEach { repeat(7) { state = state.reduce(SecondPassed) } }

                it("does not allow resend yet") { assertFalse(state.resendEnabled) }
                it("displays 3 seconds remaining") { assertTrue(state.resendButtonText.contains("(3)")) }

                describe("when 4 more seconds pass") {
                    beforeEach { repeat(4) { state = state.reduce(SecondPassed) } }

                    it("enables resend") { assertTrue(state.resendEnabled) }

                    describe("when resend is tapped") {
                        beforeEach { state = state.reduce(ResendTapped) }

                        it("sends SMS") { assertEquals(SendSms, state.effect) }
                        it("shows progress") { assertTrue(state.showProgress) }
                    }
                }
            }
        }

        describe("when SMS send fails") {
            beforeEach { state = state.reduce(EffectHandled).reduce(SmsFailed) }

            it("does not show progress") { assertFalse(state.showProgress) }
            it("shows error") { assertThat(state.effect, instanceOf(ShowError::class.java)) }

            describe("when error is dismissed") {
                beforeEach { state = state.reduce(EffectHandled) }

                it("enables resend") { assertTrue(state.resendEnabled) }

                describe("when resend is tapped") {
                    beforeEach { state = state.reduce(ResendTapped) }

                    it("sends SMS") { assertEquals(SendSms, state.effect) }
                    it("shows progress") { assertTrue(state.showProgress) }
                }
            }
        }

    }
})
