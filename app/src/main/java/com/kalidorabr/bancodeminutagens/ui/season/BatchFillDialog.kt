package com.kalidorabr.bancodeminutagens.ui.season

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kalidorabr.bancodeminutagens.model.EpisodioItem
import com.kalidorabr.bancodeminutagens.model.EstadoMinutagem
import com.kalidorabr.bancodeminutagens.model.TipoMinutagem
import com.kalidorabr.bancodeminutagens.ui.theme.FullTokens
import com.kalidorabr.bancodeminutagens.ui.theme.BorderColor
import com.kalidorabr.bancodeminutagens.ui.theme.SurfaceCard
import com.kalidorabr.bancodeminutagens.ui.theme.SurfaceDark
import com.kalidorabr.bancodeminutagens.ui.theme.TextPrimary
import com.kalidorabr.bancodeminutagens.ui.theme.TextSecondary

data class BatchFillRequest(
    val sourceEpisodeId: String,
    val targetEpisodeIds: Set<String>,
    val sectionTypes: Set<TipoMinutagem>,
    val overwriteExisting: Boolean
)

@Composable
fun BatchFillDialog(
    episodios: List<EpisodioItem>,
    isFullTheme: Boolean,
    onDismiss: () -> Unit,
    onApply: (BatchFillRequest) -> Unit
) {
    val ordered = remember(episodios) { episodios.sortedBy { it.numero } }
    if (ordered.isEmpty()) return
    val defaultSource = remember(ordered) {
        ordered.lastOrNull { episodio -> episodio.minutagens.any { it.estado != EstadoMinutagem.PENDENTE } }
            ?: ordered.first()
    }
    var source by remember { mutableStateOf(defaultSource) }
    var from by remember { mutableStateOf(ordered.firstOrNull { it.numero > defaultSource.numero } ?: ordered.first()) }
    var to by remember { mutableStateOf(ordered.last()) }
    var selectedTypes by remember { mutableStateOf(TipoMinutagem.entries.toSet()) }
    var overwrite by remember { mutableStateOf(false) }

    val lower = minOf(from.numero, to.numero)
    val upper = maxOf(from.numero, to.numero)
    val targets = ordered.filter { it.numero in lower..upper && it.id != source.id }
    val validSourceTypes = selectedTypes.filter { tipo ->
        source.minutagens.firstOrNull { it.tipo == tipo }?.estado != EstadoMinutagem.PENDENTE
    }.toSet()
    val affectedTargets = targets.filter { episode ->
        requestApplicable(episode, validSourceTypes, overwrite)
    }
    val skippedCount = targets.size - affectedTargets.size
    val sourceHasSelectedData = selectedTypes.any { tipo ->
        source.minutagens.firstOrNull { it.tipo == tipo }?.estado != null &&
            source.minutagens.first { it.tipo == tipo }.estado != EstadoMinutagem.PENDENTE
    }
    val enabled = affectedTargets.isNotEmpty() && selectedTypes.isNotEmpty() && sourceHasSelectedData
    val primary = MaterialTheme.colorScheme.primary
    val text = if (isFullTheme) FullTokens.TextWhite else TextPrimary
    val secondary = if (isFullTheme) FullTokens.TextGray else TextSecondary
    val surface = if (isFullTheme) Color(0xFF111116) else SurfaceCard
    val field = if (isFullTheme) Color(0xFF18181F) else SurfaceDark
    val border = if (isFullTheme) primary.copy(alpha = 0.45f) else BorderColor
    val shape = RoundedCornerShape(if (isFullTheme) 20.dp else 16.dp)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .heightIn(max = 760.dp)
                .background(
                    brush = if (isFullTheme) Brush.verticalGradient(listOf(surface, Color(0xFF0C0C10)))
                    else Brush.verticalGradient(listOf(surface, surface)),
                    shape = shape
                )
                .border(1.dp, border, shape)
                .padding(18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Preenchimento em lote", color = text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Copie minutagens para vários episódios", color = secondary, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier.size(38.dp).background(field, RoundedCornerShape(11.dp))
                        .border(1.dp, border, RoundedCornerShape(11.dp)).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Close, "Fechar", tint = text, modifier = Modifier.size(19.dp)) }
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("ORIGEM E INTERVALO", primary)
            Spacer(Modifier.height(10.dp))
            EpisodeSelector("Episódio de origem", source, ordered, field, border, text, secondary) { source = it }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) { EpisodeSelector("Do episódio", from, ordered, field, border, text, secondary) { from = it } }
                Box(Modifier.weight(1f)) { EpisodeSelector("Ao episódio", to, ordered, field, border, text, secondary) { to = it } }
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("O QUE COPIAR", primary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                TipoMinutagem.entries.forEach { tipo ->
                    val label = when (tipo) { TipoMinutagem.ABERTURA -> "Abertura"; TipoMinutagem.RECAP -> "Recap"; TipoMinutagem.ENCERRAMENTO -> "Encerramento" }
                    val checked = tipo in selectedTypes
                    Row(
                        modifier = Modifier.weight(1f).background(field, RoundedCornerShape(10.dp))
                            .border(1.dp, if (checked) primary.copy(alpha = .7f) else border, RoundedCornerShape(10.dp))
                            .clickable { selectedTypes = if (checked) selectedTypes - tipo else selectedTypes + tipo }
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked, null, modifier = Modifier.size(28.dp), colors = CheckboxDefaults.colors(checkedColor = primary))
                        Text(label, color = text, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("COMO APLICAR", primary)
            BatchRadio("Somente campos vazios", !overwrite, primary, text) { overwrite = false }
            BatchRadio("Substituir dados existentes", overwrite, primary, text) { overwrite = true }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(primary.copy(alpha = .09f), RoundedCornerShape(12.dp))
                    .border(1.dp, primary.copy(alpha = .3f), RoundedCornerShape(12.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Info, null, tint = primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("${affectedTargets.size} episódios serão atualizados", color = text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("$skippedCount ignorados • ${if (overwrite) "Dados existentes serão substituídos" else "Dados preenchidos serão preservados"}", color = secondary, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogButton("Cancelar", false, true, primary, field, border, text, Modifier.weight(1f), onDismiss)
                DialogButton("Aplicar em lote", true, enabled, primary, field, border, text, Modifier.weight(1f)) {
                    onApply(BatchFillRequest(source.id, affectedTargets.map { it.id }.toSet(), validSourceTypes, overwrite))
                }
            }
        }
    }
}

internal fun requestApplicable(episode: EpisodioItem, types: Set<TipoMinutagem>, overwrite: Boolean): Boolean =
    types.any { tipo ->
        val current = episode.minutagens.firstOrNull { it.tipo == tipo }
        overwrite || current == null || (current.estado == EstadoMinutagem.PENDENTE && current.tempoInicio.isBlank() && current.tempoFim.isBlank())
    }

@Composable private fun SectionLabel(label: String, color: Color) = Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)

@Composable
private fun EpisodeSelector(label: String, selected: EpisodioItem, options: List<EpisodioItem>, field: Color, border: Color, text: Color, secondary: Color, onSelected: (EpisodioItem) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, color = secondary, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier.fillMaxWidth().height(46.dp).background(field, RoundedCornerShape(11.dp))
                    .border(1.dp, border, RoundedCornerShape(11.dp)).clickable { expanded = true }.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("EP ${selected.numeroFormatado}", color = text, fontSize = 13.sp)
                Icon(Icons.Default.ExpandMore, null, tint = secondary, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded, { expanded = false }, modifier = Modifier.background(field).heightIn(max = 280.dp)) {
                options.forEach { ep -> DropdownMenuItem(text = { Text("EP ${ep.numeroFormatado}", color = text) }, onClick = { expanded = false; onSelected(ep) }) }
            }
        }
    }
}

@Composable private fun BatchRadio(label: String, selected: Boolean, primary: Color, text: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected, onClick, colors = RadioButtonDefaults.colors(selectedColor = primary))
        Text(label, color = text, fontSize = 13.sp)
    }
}

@Composable private fun DialogButton(label: String, primaryButton: Boolean, enabled: Boolean, primary: Color, field: Color, border: Color, text: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(46.dp).background(if (primaryButton && enabled) primary else field, RoundedCornerShape(11.dp))
            .border(1.dp, if (primaryButton && enabled) primary else border, RoundedCornerShape(11.dp)).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(label, color = if (enabled) text else text.copy(alpha = .4f), fontSize = 13.sp, fontWeight = FontWeight.Bold) }
}
