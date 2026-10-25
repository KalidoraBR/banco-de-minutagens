package com.kalidorabr.bancodeminutagens.ui.temaa.components

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual
import androidx.compose.animation.core.spring

/** Cores dos pop-ups do Tema A. */
object TemaASheetColors {
    private fun t(a: Long, b: Long, c: Long, d: Long) = Color(
        when (com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual.modo) { 1 -> b; 2 -> c; 3 -> d; else -> a }
    )
    val Sheet get() = t(0xFF141417, 0xFFF4F1EC, 0xFF222329, 0xFF151129)
    val Group get() = t(0xFF1A1A1F, 0xFFFFFFFF, 0xFF2C2D34, 0xFF1C1638)
    val GroupDivider get() = t(0xFF24242A, 0xFFEFEAE2, 0xFF33343C, 0xFF2E2758)
    val CloseButton get() = if (com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual.b) Color(0xFFEAE4DB) else TemaATokens.Neutro
    val Handle get() = t(0xFF34343B, 0xFFD8D1C7, 0xFF4A4B52, 0xFF3A3170)
    val Scrim get() = t(0xA8000000, 0x80141218, 0xB8080A0A, 0xBC060410)
    val MenuDanger get() = TemaATokens.Perigo
    val MenuDangerBg get() = TemaATokens.PerigoFundo
}

/**
 * Pop-up que sobe de baixo com a tela escurecida atrás.
 * Painel 320ms, escurecimento 280ms. O fundo do painel passa por baixo da barra de 3 botões;
 * o conteúdo fica acima dela.
 */
/** Quantos pop-ups de baixo estão abertos: a barra de abas se esconde enquanto houver algum. */
object SheetsAbertos {
    var quantos by androidx.compose.runtime.mutableIntStateOf(0)
}

@Composable
fun TemaABottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val state = remember { MutableTransitionState(false) }
    LaunchedEffect(visible) { state.targetState = visible }
    if (visible) androidx.compose.runtime.DisposableEffect(Unit) {
        SheetsAbertos.quantos++
        onDispose { SheetsAbertos.quantos-- }
    }
    if (!state.currentState && !state.targetState) return

    if (visible) BackHandler(onBack = onDismiss)

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visibleState = state,
            enter = fadeIn(tween(280)),
            exit = fadeOut(tween(280))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(TemaASheetColors.Scrim)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }
        AnimatedVisibility(
            visibleState = state,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = if (TemaVisual.b || TemaVisual.d) slideInVertically(spring(dampingRatio = 0.68f, stiffness = 420f)) { it }
            else slideInVertically(tween(320)) { it },
            exit = if (TemaVisual.b) slideOutVertically(tween(240, easing = com.kalidorabr.bancodeminutagens.ui.temab.EasingFirmeB)) { it }
            else slideOutVertically(tween(320)) { it }
        ) {
            // Tema D: cartão flutuante, solto das bordas, com contorno roxo suave
            val flutua = TemaVisual.d
            val formaSheet = if (flutua) RoundedCornerShape(30.dp) else RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            Column(
                modifier = Modifier
                    .then(if (flutua) Modifier.navigationBarsPadding().padding(start = 12.dp, end = 12.dp, bottom = 12.dp) else Modifier)
                    .fillMaxWidth()
                    .clip(formaSheet)
                    .background(TemaASheetColors.Sheet)
                    .then(if (flutua) Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), formaSheet) else Modifier)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TemaASheetColors.Handle)
                )
                content()
            }
        }
    }
}

@Composable
fun TemaASheetHeader(subtitle: String, title: String, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (subtitle.isNotBlank()) Text(subtitle, color = TemaATokens.TextGray, fontSize = 12.sp, maxLines = 1)
            Text(title, color = TemaATokens.TextWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(TemaASheetColors.CloseButton)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = TemaATokens.TextWhite, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun TemaAPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    height: Dp = TemaATokens.HeightPrimaryButton,
    leading: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .cliqueTemaA(enabled = enabled, vibrar = true, onClick = onClick)
            .fillMaxWidth()
            .height(height)
            .clip(TemaATokens.ShapePrimaryButton)
            .background(if (enabled) color else TemaATokens.DarkCardSurface),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text,
            color = if (enabled) Color.White else TemaATokens.TextMuted,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun TemaASecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = TemaATokens.HeightPrimaryButton
) {
    Box(
        modifier = modifier
            .cliqueTemaA(enabled = enabled, onClick = onClick)
            .height(height)
            .clip(TemaATokens.ShapePrimaryButton)
            .background(TemaATokens.Neutro),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) TemaATokens.TextWhite else TemaATokens.TextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TemaASwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .width(48.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (checked) accent else TemaATokens.Track)
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            Modifier
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .padding(3.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun TemaAGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(TemaASheetColors.Group),
        content = content
    )
}

@Composable
fun TemaAGroupDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(TemaASheetColors.GroupDivider)
    )
}

data class TemaAMenuItem(val label: String, val danger: Boolean = false, val onClick: () -> Unit)

/**
 * Menu ⋮ do Tema A: escurece a tela e abre um cartão no canto superior direito (180ms).
 */
@Composable
fun BoxScope.TemaAOverflowMenu(
    visible: Boolean,
    onDismiss: () -> Unit,
    items: List<TemaAMenuItem>,
    topOffset: Dp = 56.dp
) {
    val state = remember { MutableTransitionState(false) }
    LaunchedEffect(visible) { state.targetState = visible }
    if (!state.currentState && !state.targetState) return
    if (visible) BackHandler(onBack = onDismiss)

    AnimatedVisibility(visibleState = state, enter = fadeIn(tween(180)), exit = fadeOut(tween(160))) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
    }
    AnimatedVisibility(
        visibleState = state,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .statusBarsPadding()
            .offset(y = topOffset)
            .padding(end = 16.dp, top = 8.dp),
        enter = if (TemaVisual.b) fadeIn(tween(120)) + androidx.compose.animation.expandVertically(tween(260, easing = com.kalidorabr.bancodeminutagens.ui.temab.EasingFirmeB), expandFrom = Alignment.Top)
        else fadeIn(tween(180)) + scaleIn(tween(200), initialScale = 0.9f, transformOrigin = TransformOrigin(1f, 0f)),
        exit = if (TemaVisual.b) fadeOut(tween(140)) + androidx.compose.animation.shrinkVertically(tween(180), shrinkTowards = Alignment.Top)
        else fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.9f, transformOrigin = TransformOrigin(1f, 0f))
    ) {
        Column(
            Modifier
                .width(240.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(TemaASheetColors.Group)
                .border(1.dp, TemaATokens.Track, RoundedCornerShape(18.dp))
                .padding(6.dp)
        ) {
            items.forEach { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (item.danger) TemaASheetColors.MenuDangerBg else Color.Transparent)
                        .clickable {
                            onDismiss()
                            item.onClick()
                        }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.label,
                        color = if (item.danger) TemaASheetColors.MenuDanger else TemaATokens.TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
