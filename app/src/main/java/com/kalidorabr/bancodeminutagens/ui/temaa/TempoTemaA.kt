package com.kalidorabr.bancodeminutagens.ui.temaa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetColors

/*
 * Digitação de tempo do app: os números entram da direita para a esquerda.
 * "2" = 00:02 · "20" = 00:20 · "130" = 01:30 · "1234" = 12:34.
 */

/** Converte "02:14" nos dígitos sem zeros à esquerda ("214"). */
fun digitosTempoTemaA(tempo: String): String = tempo.filter { it.isDigit() }.takeLast(4).trimStart('0')

fun formatarTempoTemaA(digitos: String): String {
    val d = digitos.takeLast(4).padStart(4, '0')
    return d.take(2) + ":" + d.substring(2)
}

/** Segundos totais, ou null se vazio. */
fun segundosTempoTemaA(digitos: String): Int? {
    if (digitos.isEmpty()) return null
    val d = digitos.takeLast(4).padStart(4, '0')
    return d.take(2).toInt() * 60 + d.substring(2).toInt()
}

fun segundosValidosTemaA(digitos: String): Boolean =
    digitos.isEmpty() || digitos.takeLast(4).padStart(4, '0').substring(2).toInt() < 60

/** Aplica uma tecla aos dígitos. */
fun teclaTempoTemaA(atual: String, tecla: String): String = when (tecla) {
    "Apagar" -> atual.dropLast(1)
    "Limpar" -> ""
    else -> if (atual.length < 4) (atual + tecla).trimStart('0') else atual
}

@Composable
fun TecladoTempoTemaA(enabled: Boolean = true, onTecla: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("Limpar", "0", "Apagar")).forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                linha.forEach { k ->
                    val acao = k.length > 1
                    Box(
                        Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(TemaASheetColors.CloseButton)
                            .clickable(enabled = enabled) { onTecla(k) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            k,
                            color = (if (acao) TemaATokens.TextGray else TemaATokens.TextWhite).copy(alpha = if (enabled) 1f else 0.35f),
                            fontSize = if (acao) 13.sp else 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
