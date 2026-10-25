package com.kalidorabr.bancodeminutagens.ui.temaa.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens

enum class TipoAvisoTemaA { SUCESSO, DESFAZER, ATENCAO, ERRO }

/** Aviso rápido do Tema A: fica embaixo, acima dos botões e das abas. */
@Composable
fun AvisoTemaA(
    titulo: String,
    tipo: TipoAvisoTemaA,
    modifier: Modifier = Modifier,
    subtitulo: String = "",
    acao: String? = null,
    onAcao: () -> Unit = {}
) {
    val accent = MaterialTheme.colorScheme.primary
    val erro = tipo == TipoAvisoTemaA.ERRO
    val icon: ImageVector
    val fg: Color
    val bg: Color
    when (tipo) {
        TipoAvisoTemaA.SUCESSO -> { icon = Icons.Filled.Check; fg = TemaATokens.SuccessText; bg = TemaATokens.SuccessBg }
        TipoAvisoTemaA.DESFAZER -> { icon = Icons.AutoMirrored.Filled.Undo; fg = TemaATokens.ErroTexto; bg = TemaATokens.ErroFundo }
        TipoAvisoTemaA.ATENCAO -> { icon = Icons.Filled.PriorityHigh; fg = TemaATokens.SemanticWarning; bg = TemaATokens.WarningBg }
        TipoAvisoTemaA.ERRO -> { icon = Icons.Filled.PriorityHigh; fg = Color.White; bg = accent }
    }
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (erro) TemaATokens.ErroFundo else TemaATokens.Neutro)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(24.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, color = if (erro) TemaATokens.ErroTextoForte else TemaATokens.TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (subtitulo.isNotBlank()) {
                Text(subtitulo, color = if (erro) TemaATokens.ErroSub else TemaATokens.TextGray, fontSize = 12.sp)
            }
        }
        if (acao != null) {
            Text(
                acao,
                color = if (erro) Color.White else accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .then(if (erro) Modifier.background(accent) else Modifier)
                    .clickable(onClick = onAcao)
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            )
        }
    }
}
