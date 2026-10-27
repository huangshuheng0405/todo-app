package com.example.app2.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidationTest {

    @Test
    fun normalizeEmailTrimsAndLowercases() {
        assertEquals("you@example.com", AuthValidation.normalizeEmail("  You@Example.COM "))
    }

    @Test
    fun validEmailsPass() {
        listOf(
            "a@b.co",
            "first.last@example.com",
            "user+tag@sub.example.cn",
            "a_b-c@my-domain.org"
        ).forEach { email ->
            assertNull("应通过：$email", AuthValidation.emailError(email))
        }
    }

    @Test
    fun blankEmailIsRejected() {
        assertEquals("请输入邮箱", AuthValidation.emailError("   "))
    }

    @Test
    fun malformedEmailsAreRejected() {
        listOf(
            "abc",
            "a@b",
            "a b@c.com",
            "@b.com",
            "a@.com",
            "a@b.c"
        ).forEach { email ->
            assertNotNull("应拒绝：$email", AuthValidation.emailError(email))
        }
    }

    @Test
    fun passwordShorterThanSixIsRejected() {
        assertEquals("密码至少 6 位", AuthValidation.passwordError("12345"))
    }

    @Test
    fun passwordOfSixIsAccepted() {
        assertNull(AuthValidation.passwordError("123456"))
    }

    @Test
    fun emptyPasswordIsRejected() {
        assertEquals("请输入密码", AuthValidation.passwordError(""))
    }
}
