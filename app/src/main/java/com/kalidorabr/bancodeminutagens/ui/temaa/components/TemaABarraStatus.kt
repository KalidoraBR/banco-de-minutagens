package com.kalidorabr.bancodeminutagens.ui.temaa.components

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Mede o brilho da faixa de cima da imagem (onde fica a barra de status).
 * Retorna true quando a imagem é clara ali, ou seja, os ícones devem ficar escuros.
 */
fun topoClaroTemaA(bitmap: Bitmap, faixa: Float = 0.15f): Boolean {
    val w = bitmap.width
    val h = (bitmap.height * faixa).toInt().coerceAtLeast(1)
    val passoX = (w / 24).coerceAtLeast(1)
    val passoY = (h / 6).coerceAtLeast(1)
    var soma = 0.0
    var n = 0
    var y = 0
    while (y < h) {
        var x = 0
        while (x < w) {
            val c = bitmap.getPixel(x, y)
            // Luminância percebida (0 a 255)
            soma += 0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c)
            n++
            x += passoX
        }
        y += passoY
    }
    return n > 0 && soma / n > 150
}

/**
 * Deixa os ícones da barra de status escuros ([iconesEscuros] = true) ou brancos.
 * Ao sair da tela, volta para brancos (padrão do app escuro).
 */
@Composable
fun BarraStatusAdaptativaTemaA(iconesEscuros: Boolean) {
    // No Tema B a faixa de cima é sempre escura: ícones sempre brancos
    // No Tema B a faixa de cima é escura (ícones brancos), menos na Série, onde vale a regra do brilho
    val tv = com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual
    if (tv.b && !tv.semFaixa) return
    val view = LocalView.current
    DisposableEffect(iconesEscuros) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = iconesEscuros
        }
        onDispose {
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }
}
