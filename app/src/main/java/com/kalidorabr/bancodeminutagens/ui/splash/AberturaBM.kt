package com.kalidorabr.bancodeminutagens.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/*
 * Abertura v2 ("Splash Minutagem v2", gerada no Claude Design).
 * Especificação: botão "PULAR ABERTURA" → "PULAR RECAPITULAÇÃO" → "PULAR ENCERRAMENTO",
 * o botão é tocado e vira o ícone, o BM sobe, a barra enche, nome e slogan aparecem.
 */

/**
 * Sinal de "Início pronta": o app marca quando as séries (e as primeiras imagens) já carregaram.
 * A abertura só sai da tela depois disso — ou depois de um tempo máximo, para nunca travar.
 */
object ProntidaoInicial {
    var pronto by mutableStateOf(false)
}

private val Bg = Color(0xFF0B0B0D)
private val Ink = Color(0xFFF2F2F3)
private val Red = Color(0xFFE5484D)
private val Track = Color(0xFF26262C)
private val Muted = Color(0xFF8E8E96)

private val OutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val InOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

/** Fim da animação na escala da especificação (ms). */
private const val HOLD_END_MS = 2150

/**
 * Ritmo da abertura: 1.0 = tempos da especificação (~2,45 s).
 * 1.25 ≈ 3 s no total. Aumente para mais lenta, diminua para mais rápida.
 */
private const val RITMO = 1.25f
private const val EXIT_MS = 340
private const val MAX_WAIT_MS = 1500L

private fun seg(t: Float, start: Int, end: Int, easing: Easing = OutCubic): Float =
    easing.transform(((t - start) / (end - start)).coerceIn(0f, 1f))

private fun lerpF(a: Float, b: Float, k: Float) = a + (b - a) * k

/** Ponto de entrada usado pelo app (MainActivity). */
@Composable
fun AberturaBM(onFinished: () -> Unit) {
    SplashOverlay(
        homeReady = ProntidaoInicial.pronto,
        manrope = TemaATokens.FontSans,
        onFinished = onFinished
    )
}

/**
 * Abertura animada. Fica por cima da Início dentro de um Box.
 * homeReady: true quando a Início terminou de carregar.
 * onFinished: remove o overlay da composição.
 */
@Composable
fun SplashOverlay(homeReady: Boolean, manrope: FontFamily, onFinished: () -> Unit) {
    val clock = remember { Animatable(0f) } // ms na escala da especificação, linear
    val exit = remember { Animatable(0f) }  // 0..1
    val ready by rememberUpdatedState(homeReady)
    val finished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        // Deixa passar o esforço inicial (o app montando a Início por baixo) antes de animar.
        repeat(3) { withFrameNanos { } }
        delay(120)
        clock.animateTo(
            HOLD_END_MS.toFloat(),
            tween((HOLD_END_MS * RITMO).toInt(), easing = LinearEasing)
        )
        withTimeoutOrNull(MAX_WAIT_MS) { snapshotFlow { ready }.first { it } }
        exit.animateTo(1f, tween(EXIT_MS, easing = OutCubic))
        finished()
    }

    val t: () -> Float = { clock.value }
    val labelStyle = TextStyle(fontFamily = manrope, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 0.08.em, color = Ink, textAlign = TextAlign.Center)
    val bmStyle = TextStyle(fontFamily = manrope, fontWeight = FontWeight.ExtraBold, fontSize = 76.sp, lineHeight = 84.sp, letterSpacing = (-0.02).em)

    BoxWithConstraints(
        Modifier.fillMaxSize().graphicsLayer { alpha = 1f - exit.value }.background(Bg)
    ) {
        // Âncora: centro horizontal, centro vertical − 58dp
        val anchorY = -58.dp
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                val s = 1f + 0.04f * exit.value
                scaleX = s; scaleY = s
                transformOrigin = TransformOrigin(0.5f, (maxHeight / 2 + anchorY) / maxHeight)
            }
        ) {
            // Moldura (botão → ícone) e barra vermelha
            Canvas(Modifier.fillMaxSize()) {
                val tt = t()
                val inK = seg(tt, 0, 260)
                val press = when {
                    tt < 960 -> 0f
                    tt < 1040 -> seg(tt, 960, 1040)
                    else -> 1f - seg(tt, 1040, 1200)
                }
                val m = seg(tt, 1040, 1440, InOutCubic)
                val w = lerpF(296f, 248f, m).dp.toPx()
                val h = lerpF(58f, 148f, m).dp.toPx()
                val r = lerpF(4f, 28f, m).dp.toPx()
                val sw = lerpF(2f, 12f, m).dp.toPx()
                val ax = size.width / 2
                val ay = size.height / 2 + anchorY.toPx()
                val cy = ay + lerpF(30f, 0f, m).dp.toPx()
                val sc = lerpF(0.94f, 1f, inK) - 0.04f * press

                scale(sc, pivot = Offset(ax, cy)) {
                    val tl = Offset(ax - w / 2, cy - h / 2)
                    drawRoundRect(lerp(Bg, Track, press * (1f - m)), tl, Size(w, h), CornerRadius(r))
                    drawRoundRect(
                        Ink.copy(alpha = inK), tl + Offset(sw / 2, sw / 2), Size(w - sw, h - sw),
                        CornerRadius((r - sw / 2).coerceAtLeast(0f)), style = Stroke(sw)
                    )
                }

                val trackK = seg(tt, 1320, 1440)
                if (trackK > 0f) {
                    val bw = 196.dp.toPx(); val bh = 9.dp.toPx(); val br = CornerRadius(bh / 2)
                    val btl = Offset(ax - bw / 2, ay + 36.dp.toPx())
                    drawRoundRect(Track.copy(alpha = trackK), btl, Size(bw, bh), br)
                    val fill = 0.62f * seg(tt, 1400, 1900, InOutCubic)
                    if (fill > 0f) drawRoundRect(Red.copy(alpha = trackK), btl, Size(bw * fill, bh), br)
                }
            }

            // Rótulos do botão Pular (área 296×58, centro em âncora + 30dp)
            Box(
                Modifier.align(Alignment.Center).offset(y = anchorY + 30.dp).size(296.dp, 58.dp)
                    .graphicsLayer {
                        val tt = t()
                        val press = when {
                            tt < 960 -> 0f; tt < 1040 -> seg(tt, 960, 1040); else -> 1f - seg(tt, 1040, 1200)
                        }
                        val s = lerpF(0.94f, 1f, seg(tt, 0, 260)) - 0.04f * press
                        scaleX = s; scaleY = s
                    }
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                SkipLabel("PULAR ABERTURA", labelStyle, t) { tt ->
                    val s1 = seg(tt, 440, 560); seg(tt, 0, 260) * (1f - s1) to -20f * s1
                }
                SkipLabel("PULAR RECAPITULAÇÃO", labelStyle, t) { tt ->
                    val s1 = seg(tt, 440, 560); val s2 = seg(tt, 700, 820)
                    s1 * (1f - s2) to (20f * (1f - s1) - 20f * s2)
                }
                SkipLabel("PULAR ENCERRAMENTO", labelStyle, t) { tt ->
                    val s2 = seg(tt, 700, 820); val s3 = seg(tt, 960, 1040)
                    s2 * (1f - s3) to 20f * (1f - s2)
                }
            }

            // BM + símbolo ⏭ (linha centrada em âncora − 12dp)
            Row(
                Modifier.align(Alignment.Center).offset(y = anchorY - 12.dp).height(84.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(Modifier.height(84.dp).clipToBounds()) {
                    Text("B", style = bmStyle.copy(color = Ink), modifier = Modifier.graphicsLayer {
                        translationY = (1f - seg(t(), 1300, 1600)) * 84.dp.toPx()
                    })
                    Text("M", style = bmStyle.copy(color = Red), modifier = Modifier.graphicsLayer {
                        translationY = (1f - seg(t(), 1360, 1660)) * 84.dp.toPx()
                    })
                }
                Canvas(Modifier.size(41.dp, 34.dp).graphicsLayer {
                    val k = seg(t(), 1460, 1720)
                    alpha = k; translationX = (1f - k) * -16.dp.toPx()
                }) {
                    val u = size.width / 120f; val v = size.height / 100f
                    fun tri(x0: Float) = Path().apply {
                        moveTo(x0 * u, 8 * v); lineTo((x0 + 44) * u, 50 * v); lineTo(x0 * u, 92 * v); close()
                    }
                    drawPath(tri(0f), Ink); drawPath(tri(44f), Ink)
                    drawRect(Ink, Offset(98 * u, 8 * v), Size(14 * u, 84 * v))
                }
            }

            // Nome e slogan
            Text(
                "Banco de Minutagens",
                style = TextStyle(fontFamily = manrope, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp, color = Ink),
                modifier = Modifier.align(Alignment.Center).offset(y = anchorY + 130.dp).graphicsLayer {
                    val k = seg(t(), 1560, 1900); alpha = k; translationY = (1f - k) * 12.dp.toPx()
                }
            )
            Text(
                "Abertura - Recapitulação - Encerramento",
                style = TextStyle(fontFamily = manrope, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 20.sp, letterSpacing = 0.02.em, color = Muted),
                modifier = Modifier.align(Alignment.Center).offset(y = anchorY + 162.dp).graphicsLayer {
                    val k = seg(t(), 1660, 2000); alpha = k; translationY = (1f - k) * 12.dp.toPx()
                }
            )
        }
    }
}

/** spec(t) devolve (alpha, deslocamento Y em dp). */
@Composable
private fun SkipLabel(text: String, style: TextStyle, clock: () -> Float, spec: (Float) -> Pair<Float, Float>) {
    // O relógio é lido dentro do graphicsLayer, então não há recomposição por frame.
    Text(text, style = style, maxLines = 1, modifier = Modifier.width(296.dp).graphicsLayer {
        val (a, dy) = spec(clock())
        alpha = a; translationY = dy.dp.toPx()
    })
}
