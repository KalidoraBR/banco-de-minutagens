package com.kalidorabr.bancodeminutagens.ui.temas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAPrimaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASecondaryButton

enum class EstadoItemIntroDb { ESPERANDO, ENVIANDO, ENVIADO, JA_NO_SITE, PULADO, ERRO }

data class ItemProgressoIntroDb(val rotulo: String, val estado: EstadoItemIntroDb, val detalhe: String = "")

/** Estado do pop-up de envio ao IntroDB. [aviso] = problema que parou tudo (chave, IMDb, limite). */
data class ProgressoIntroDb(
    val serie: String,
    val itens: List<ItemProgressoIntroDb>,
    val etapa: String = "",
    val terminado: Boolean = false,
    val aviso: String? = null
) {
    val enviados: Int get() = itens.count { it.estado == EstadoItemIntroDb.ENVIADO }
    val resolvidos: Int get() = itens.count { it.estado != EstadoItemIntroDb.ESPERANDO && it.estado != EstadoItemIntroDb.ENVIANDO }
}

/** Pop-up exclusivo do envio ao IntroDB: progresso, lista de seções e resumo. */
@Composable
fun DialogoIntroDb(progresso: ProgressoIntroDb?, onFechar: () -> Unit) {
    if (progresso == null) return
    val accent = MaterialTheme.colorScheme.primary
    val fracao by animateFloatAsState(
        if (progresso.itens.isEmpty()) 0f else progresso.resolvidos.toFloat() / progresso.itens.size,
        tween(300), label = "progressoIntroDb"
    )
    Dialog(onDismissRequest = { if (progresso.terminado) onFechar() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 400.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(TemaATokens.DarkGraphiteSurface)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (progresso.terminado && progresso.aviso == null) Icons.Outlined.CloudDone else Icons.Outlined.CloudUpload,
                            null, tint = accent, modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            when {
                                !progresso.terminado -> "Enviando ao IntroDB"
                                progresso.aviso != null -> "Envio interrompido"
                                else -> "Envio concluído"
                            },
                            color = TemaATokens.TextWhite, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold
                        )
                        Text(progresso.serie, color = TemaATokens.TextGray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(TemaATokens.Track)) {
                        Box(Modifier.fillMaxWidth(fracao).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(accent))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${(fracao * 100).toInt()}%",
                            color = TemaATokens.TextWhite, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            progresso.etapa.ifBlank { "${progresso.resolvidos} de ${progresso.itens.size} seções" },
                            color = TemaATokens.TextGray, fontSize = 12.sp, modifier = Modifier.weight(1f)
                        )
                        Text(
                            if (progresso.enviados == 1) "1 enviada" else "${progresso.enviados} enviadas",
                            color = TemaATokens.SuccessText, fontSize = 12.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }

                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(TemaATokens.DarkBackground)
                ) {
                    items(progresso.itens) { item -> LinhaItemIntroDb(item) }
                }

                progresso.aviso?.let {
                    Text(
                        it, color = TemaATokens.ErroTexto, fontSize = 13.sp, lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(TemaATokens.ErroFundo).padding(12.dp)
                    )
                }

                if (progresso.terminado) TemaAPrimaryButton("Fechar", onClick = onFechar)
                else TemaASecondaryButton("Continuar em segundo plano", onFechar, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LinhaItemIntroDb(item: ItemProgressoIntroDb) {
    val (cor, texto) = when (item.estado) {
        EstadoItemIntroDb.ESPERANDO -> TemaATokens.TextMuted to "Na fila"
        EstadoItemIntroDb.ENVIANDO -> MaterialTheme.colorScheme.primary to "Enviando…"
        EstadoItemIntroDb.ENVIADO -> TemaATokens.SuccessText to "Enviada"
        EstadoItemIntroDb.JA_NO_SITE -> TemaATokens.TextGray to "Já está no site"
        EstadoItemIntroDb.PULADO -> TemaATokens.SemanticWarning to "Pulada"
        EstadoItemIntroDb.ERRO -> TemaATokens.ErroTexto to "Erro"
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            when (item.estado) {
                EstadoItemIntroDb.ENVIANDO -> CircularProgressIndicator(Modifier.size(16.dp), color = cor, strokeWidth = 2.dp)
                EstadoItemIntroDb.ENVIADO -> Icon(Icons.Filled.Check, null, tint = cor, modifier = Modifier.size(18.dp))
                EstadoItemIntroDb.ERRO -> Icon(Icons.Filled.Close, null, tint = cor, modifier = Modifier.size(18.dp))
                EstadoItemIntroDb.ESPERANDO -> Box(Modifier.size(8.dp).clip(CircleShape).background(cor))
                else -> Icon(Icons.Filled.Remove, null, tint = cor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(item.rotulo, color = TemaATokens.TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (item.detalhe.isNotBlank()) Text(item.detalhe, color = TemaATokens.TextGray, fontSize = 11.sp, lineHeight = 14.sp)
        }
        Text(texto, color = cor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
