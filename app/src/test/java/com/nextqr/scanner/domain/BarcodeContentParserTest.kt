package com.nextqr.scanner.domain

import com.google.common.truth.Truth.assertThat
import com.nextqr.scanner.domain.model.ParsedContent
import com.nextqr.scanner.domain.model.ScanContentType
import com.nextqr.scanner.domain.usecase.BarcodeContentParser
import org.junit.Before
import org.junit.Test

class BarcodeContentParserTest {

    private lateinit var parser: BarcodeContentParser

    @Before
    fun setUp() {
        parser = BarcodeContentParser()
    }

    @Test
    fun `parses https url`() {
        val result = parser.parse("https://example.com/path")
        assertThat(result).isInstanceOf(ParsedContent.Url::class.java)
        assertThat((result as ParsedContent.Url).url).isEqualTo("https://example.com/path")
    }

    @Test
    fun `bare domain is normalised to https`() {
        val result = parser.parse("example.com")
        assertThat(result).isInstanceOf(ParsedContent.Url::class.java)
        assertThat((result as ParsedContent.Url).url).isEqualTo("https://example.com")
    }

    @Test
    fun `parses wifi payload with escaped separators`() {
        val result = parser.parse("WIFI:T:WPA;S:MyNet;P:pa\\;ss;H:true;;")
        assertThat(result).isInstanceOf(ParsedContent.Wifi::class.java)
        result as ParsedContent.Wifi
        assertThat(result.ssid).isEqualTo("MyNet")
        assertThat(result.password).isEqualTo("pa;ss")
        assertThat(result.encryption).isEqualTo(ParsedContent.Wifi.Encryption.WPA)
        assertThat(result.hidden).isTrue()
    }

    @Test
    fun `parses mailto with subject and body`() {
        val result = parser.parse("mailto:a@b.com?subject=Hi&body=There")
        assertThat(result).isInstanceOf(ParsedContent.Email::class.java)
        result as ParsedContent.Email
        assertThat(result.address).isEqualTo("a@b.com")
        assertThat(result.subject).isEqualTo("Hi")
        assertThat(result.body).isEqualTo("There")
    }

    @Test
    fun `parses geo uri`() {
        val result = parser.parse("geo:52.2297,21.0122?q=Warsaw")
        assertThat(result).isInstanceOf(ParsedContent.Geo::class.java)
        result as ParsedContent.Geo
        assertThat(result.latitude).isEqualTo(52.2297)
        assertThat(result.longitude).isEqualTo(21.0122)
        assertThat(result.label).isEqualTo("Warsaw")
    }

    @Test
    fun `parses tel`() {
        val result = parser.parse("tel:+48123456789")
        assertThat(result).isInstanceOf(ParsedContent.Phone::class.java)
        assertThat((result as ParsedContent.Phone).number).isEqualTo("+48123456789")
    }

    @Test
    fun `parses bitcoin uri`() {
        val result = parser.parse("bitcoin:1A1zP1?amount=0.5")
        assertThat(result).isInstanceOf(ParsedContent.Crypto::class.java)
        result as ParsedContent.Crypto
        assertThat(result.scheme).isEqualTo("bitcoin")
        assertThat(result.address).isEqualTo("1A1zP1")
        assertThat(result.amount).isEqualTo("0.5")
    }

    @Test
    fun `parses epc payment`() {
        val payload = "BCD\n002\n1\nSCT\nBPHKPLPK\nJan Kowalski\nPL61109010140000071219812874\nEUR12.50\n\nInvoice 1"
        val result = parser.parse(payload)
        assertThat(result).isInstanceOf(ParsedContent.Payment::class.java)
        result as ParsedContent.Payment
        assertThat(result.recipientName).isEqualTo("Jan Kowalski")
        assertThat(result.iban).isEqualTo("PL61109010140000071219812874")
        assertThat(result.amount).isEqualTo("12.50")
    }

    @Test
    fun `plain text falls through`() {
        val result = parser.parse("just some text")
        assertThat(result.type).isEqualTo(ScanContentType.TEXT)
    }
}
