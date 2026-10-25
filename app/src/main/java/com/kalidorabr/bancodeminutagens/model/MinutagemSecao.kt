package com.kalidorabr.bancodeminutagens.model

enum class TipoMinutagem {
    ABERTURA,
    RECAP,
    ENCERRAMENTO
}

enum class EstadoMinutagem {
    PREENCHIDO,
    NAO_POSSUI,
    PENDENTE
}

data class MinutagemSecao(
    val tipo: TipoMinutagem,
    val estado: EstadoMinutagem = EstadoMinutagem.PENDENTE,
    val tempoInicio: String = "",
    val tempoFim: String = "",
    val isFilled: Boolean = estado != EstadoMinutagem.PENDENTE,
    val hasNot: Boolean = estado == EstadoMinutagem.NAO_POSSUI,
    val confirmadaIndividualmente: Boolean = false
) {
    val textoExibicao: String
        get() = when (estado) {
            EstadoMinutagem.PREENCHIDO -> "$tempoInicio – $tempoFim"
            EstadoMinutagem.NAO_POSSUI -> "Não possui"
            EstadoMinutagem.PENDENTE -> "Não preenchido"
        }

    val duracaoFormatada: String
        get() = when (estado) {
            EstadoMinutagem.PREENCHIDO -> {
                val secInicio = parseMmSsToSeconds(tempoInicio)
                val secFim = parseMmSsToSeconds(tempoFim)
                if (secInicio != null && secFim != null && secFim >= secInicio) {
                    formatSecondsToMmSs(secFim - secInicio)
                } else {
                    "$tempoInicio – $tempoFim"
                }
            }
            EstadoMinutagem.NAO_POSSUI -> "Não possui"
            EstadoMinutagem.PENDENTE -> "Não preenchido"
        }

    companion object {
        fun parseMmSsToSeconds(timeStr: String): Int? {
            val parts = timeStr.split(":")
            if (parts.size != 2) return null
            val min = parts[0].toIntOrNull() ?: return null
            val sec = parts[1].toIntOrNull() ?: return null
            if (min < 0 || sec < 0 || sec > 59 || min > (Int.MAX_VALUE - sec) / 60) return null
            return min * 60 + sec
        }

        fun formatSecondsToMmSs(totalSec: Int): String {
            val min = totalSec / 60
            val sec = totalSec % 60
            val minStr = if (min < 10) "0$min" else "$min"
            val secStr = if (sec < 10) "0$sec" else "$sec"
            return "$minStr:$secStr"
        }

        fun formatDigitsToMmSs(rawInput: String): String {
            val digits = rawInput.filter { it.isDigit() }.take(4)
            return when (digits.length) {
                0 -> ""
                1 -> "00:0$digits"
                2 -> "00:$digits"
                3 -> "0${digits.substring(0, 1)}:${digits.substring(1)}"
                else -> "${digits.substring(0, 2)}:${digits.substring(2)}"
            }
        }

        fun somarTempos(tempoInicial: String, duracaoFixa: String): String? {
            val secInicio = parseMmSsToSeconds(tempoInicial) ?: return null
            val secDuracao = parseMmSsToSeconds(duracaoFixa) ?: return null
            val totalSec = secInicio + secDuracao
            return formatSecondsToMmSs(totalSec)
        }
    }
}
