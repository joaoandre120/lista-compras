package com.joaoandre120.listadecompras.data.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Conversões entre datas de calendário no formato `AAAA-MM-DD` e o que a interface usa.
 *
 * O seletor de datas do Material 3 trabalha com milissegundos em UTC (meia-noite do dia
 * escolhido). Por isso as conversões do seletor usam UTC: usar o fuso local deslocaria o
 * dia no Brasil. Só [today] usa o fuso local, porque "hoje" é o dia do usuário.
 * Cada chamada cria seu próprio `SimpleDateFormat`, que não é seguro entre threads.
 */
object IsoDate {

    private const val ISO = "yyyy-MM-dd"
    private const val DISPLAY = "dd/MM/yyyy"

    private fun formatter(pattern: String, utc: Boolean): SimpleDateFormat =
        SimpleDateFormat(pattern, Locale.ROOT).apply {
            isLenient = false   // "2026-13-45" deve falhar, não "corrigir"
            if (utc) timeZone = TimeZone.getTimeZone("UTC")
        }

    /** @return a data de hoje, no fuso do aparelho, como `AAAA-MM-DD`. */
    fun today(): String = formatter(ISO, utc = false).format(Date())

    /**
     * @param millis valor devolvido pelo seletor de datas (UTC).
     * @return o dia escolhido como `AAAA-MM-DD`.
     */
    fun fromPickerMillis(millis: Long): String = formatter(ISO, utc = true).format(Date(millis))

    /**
     * @param iso data no formato `AAAA-MM-DD`.
     * @return milissegundos UTC para o seletor, ou `null` se o texto for inválido.
     */
    fun toPickerMillis(iso: String): Long? =
        runCatching { formatter(ISO, utc = true).parse(iso)?.time }.getOrNull()

    /**
     * @param iso data no formato `AAAA-MM-DD`.
     * @return a data como `dd/MM/aaaa`; o próprio texto se for inválido.
     */
    fun display(iso: String): String {
        val millis = toPickerMillis(iso) ?: return iso
        return formatter(DISPLAY, utc = true).format(Date(millis))
    }
}