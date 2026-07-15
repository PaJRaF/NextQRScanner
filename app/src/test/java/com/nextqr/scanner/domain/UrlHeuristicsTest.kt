package com.nextqr.scanner.domain

import com.google.common.truth.Truth.assertThat
import com.nextqr.scanner.domain.model.UrlWarning
import com.nextqr.scanner.domain.usecase.UrlHeuristics
import org.junit.Before
import org.junit.Test

class UrlHeuristicsTest {

    private lateinit var heuristics: UrlHeuristics

    @Before
    fun setUp() {
        heuristics = UrlHeuristics()
    }

    @Test
    fun `clean https url has no warnings`() {
        assertThat(heuristics.analyze("https://www.google.com/search")).isEmpty()
    }

    @Test
    fun `ip host is flagged`() {
        val warnings = heuristics.analyze("http://192.168.0.1/login")
        assertThat(warnings).contains(UrlWarning.IP_ADDRESS_HOST)
        assertThat(warnings).contains(UrlWarning.NON_HTTPS)
    }

    @Test
    fun `shortener domain is flagged`() {
        assertThat(heuristics.analyze("https://bit.ly/abc123"))
            .contains(UrlWarning.SHORTENED_LINK)
    }

    @Test
    fun `punycode host is flagged`() {
        assertThat(heuristics.analyze("https://xn--e1awd7f.com"))
            .contains(UrlWarning.PUNYCODE_HOST)
    }

    @Test
    fun `unusual tld is flagged`() {
        assertThat(heuristics.analyze("https://free-money.zip"))
            .contains(UrlWarning.UNUSUAL_TLD)
    }

    @Test
    fun `embedded credentials are flagged`() {
        assertThat(heuristics.analyze("https://user:pass@evil.com"))
            .contains(UrlWarning.EMBEDDED_CREDENTIALS)
    }

    @Test
    fun `qrljacking login redirect is flagged`() {
        assertThat(heuristics.analyze("https://good.com/?redirect=https://evil.com/login"))
            .contains(UrlWarning.QRLJACKING_LOGIN_REDIRECT)
    }

    @Test
    fun `mixed script lookalike host is flagged`() {
        // 'а' here is Cyrillic U+0430, not Latin 'a'.
        assertThat(heuristics.analyze("https://pаypal.com"))
            .contains(UrlWarning.LOOKALIKE_CHARACTERS)
    }
}
