package com.kalidorabr.bancodeminutagens.ui.temas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kalidorabr.bancodeminutagens.data.VisualMode
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens
import com.kalidorabr.bancodeminutagens.ui.temaa.components.EasingTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.cliqueTemaA
import com.kalidorabr.bancodeminutagens.ui.temab.BaseCurvaB
import com.kalidorabr.bancodeminutagens.ui.temab.EasingFirmeB
import com.kalidorabr.bancodeminutagens.ui.temab.EscuroCabecalhoB
import com.kalidorabr.bancodeminutagens.ui.temab.TextoSobreEscuroB
import com.kalidorabr.bancodeminutagens.ui.temab.cliqueTemaB

/**
 * Pop-up central "Quer sair do aplicativo?", aberto ao apertar voltar na Início.
 * Cada tema tem o seu desenho e o seu jeito de aparecer.
 */
@Composable
fun TelaConfirmarSaida(visivel: Boolean, onFicar: () -> Unit, onSair: () -> Unit) {
    if (!visivel) return
    Dialog(
        onDismissRequest = onFicar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onFicar),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .padding(horizontal = 28.dp)
                    .widthIn(max = 380.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
            ) {
                when (LocalModoVisual.current) {
                    VisualMode.TEMA_B -> SairTemaB(onFicar, onSair)
                    else -> SairTemaA(onFicar, onSair)
                }
            }
        }
    }
}

/** Tema A: cartão escuro que surge do centro (leve aumento + esmaecer), botões lado a lado. */
@Composable
private fun SairTemaA(onFicar: () -> Unit, onSair: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val p = remember { Animatable(0f) }
    LaunchedEffect(Unit) { p.animateTo(1f, tween(260, easing = EasingTemaA)) }
    Column(
        Modifier
            .graphicsLayer {
                alpha = p.value
                val s = 0.92f + 0.08f * p.value
                scaleX = s; scaleY = s
            }
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(TemaATokens.DarkCardSurface)
            .padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = accent, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Sair do aplicativo?", color = TemaATokens.TextWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Suas minutagens já estão salvas.",
            color = TemaATokens.TextGray, fontSize = 14.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        // Cancelar à esquerda (fundo cinza) e Sair à direita (vermelho), mesmo tamanho
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .weight(1f)
                    .cliqueTemaA(onClick = onFicar)
                    .height(TemaATokens.HeightPrimaryButton)
                    .clip(TemaATokens.ShapePrimaryButton)
                    .background(TemaATokens.Neutro),
                contentAlignment = Alignment.Center
            ) { Text("Cancelar", color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold) }
            com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAPrimaryButton("Sair", onSair, Modifier.weight(1f))
        }
    }
}

/**
 * Tema B: cartão claro com faixa escura de base curva no topo e título em maiúsculas.
 * Sobe um pouco e assenta com um pequeno quique; barra com CANCELAR à esquerda
 * e o botão vermelho à direita, como os pop-ups do tema.
 */
@Composable
private fun SairTemaB(onFicar: () -> Unit, onSair: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val p = remember { Animatable(0f) }
    val barra = remember { Animatable(0f) }
    LaunchedEffect(Unit) { p.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 420f)) }
    LaunchedEffect(Unit) { barra.animateTo(1f, tween(280, delayMillis = 160, easing = EasingFirmeB)) }
    val sobe = with(LocalDensity.current) { 40.dp.toPx() }
    Column(
        Modifier
            .graphicsLayer {
                alpha = p.value.coerceIn(0f, 1f)
                translationY = (1f - p.value) * sobe
            }
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(TemaATokens.DarkBackground)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(BaseCurvaB(18.dp))
                .background(EscuroCabecalhoB)
                .padding(top = 22.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("SAIR DO APP?", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
            Text("Banco de Minutagens", color = TextoSobreEscuroB, fontSize = 12.sp)
        }
        Text(
            "Suas minutagens já estão salvas. Quer mesmo fechar o aplicativo?",
            color = TemaATokens.TextoCorpo,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 6.dp)
        )
        Row(
            Modifier
                .graphicsLayer {
                    alpha = barra.value
                    translationY = (1f - barra.value) * sobe / 2
                }
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Os dois botões têm a mesma largura e o texto fica centralizado em cada um
            Box(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TemaATokens.Neutro)
                    .cliqueTemaB(onClick = onFicar),
                contentAlignment = Alignment.Center
            ) {
                Text("CANCELAR", color = TemaATokens.TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent)
                    .cliqueTemaB(vibrar = true, onClick = onSair),
                contentAlignment = Alignment.Center
            ) {
                Text("SAIR", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
            }
        }
    }
}
