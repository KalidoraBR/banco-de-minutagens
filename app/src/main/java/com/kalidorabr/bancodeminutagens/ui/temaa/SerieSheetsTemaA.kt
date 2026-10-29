package com.kalidorabr.bancodeminutagens.ui.temaa

import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAGroup
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAGroupDivider
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAPrimaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASecondaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetColors
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetHeader
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASwitch

private val opcoesStatusTemaA = listOf(
    "Em andamento" to TemaATokens.SemanticWarning,
    "Finalizada" to TemaATokens.SemanticSuccess,
    "Incompleta" to TemaATokens.TextGray,
    "Em breve" to Color(0xFF3E7BFA)
)

@Composable
fun ColumnScope.StatusSheetContentTemaA(
    nomeSerie: String,
    statusAtual: String,
    onClose: () -> Unit,
    onSave: (String) -> Unit
) {
    var escolhido by remember(statusAtual) {
        mutableStateOf(opcoesStatusTemaA.firstOrNull { it.first.equals(statusAtual, true) }?.first ?: "Em andamento")
    }
    val accent = MaterialTheme.colorScheme.primary
    TemaASheetHeader(nomeSerie, "Status da série", onClose)
    TemaAGroup {
        opcoesStatusTemaA.forEachIndexed { i, (nome, cor) ->
            if (i > 0) TemaAGroupDivider()
            val on = escolhido == nome
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { escolhido = nome }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(cor))
                Spacer(Modifier.width(12.dp))
                Text(
                    com.kalidorabr.bancodeminutagens.model.StatusPlanilha.rotulo(nome),
                    modifier = Modifier.weight(1f),
                    color = if (on) accent else TemaATokens.TextWhite,
                    fontSize = 15.sp,
                    fontWeight = if (on) FontWeight.ExtraBold else FontWeight.SemiBold
                )
                RadioTemaA(on)
            }
        }
    }
    TemaAPrimaryButton("Salvar", onClick = { onSave(escolhido) })
}

@Composable
fun RadioTemaA(on: Boolean) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(2.dp, if (on) accent else TemaATokens.TextMuted, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (on) Box(Modifier.size(10.dp).clip(CircleShape).background(accent))
    }
}

@Composable
fun ColumnScope.IdentificadoresSheetContentTemaA(
    nomeSerie: String,
    tmdbId: String,
    imdbIdAtual: String,
    tvdbIdAtual: String,
    onClose: () -> Unit,
    onSave: (imdbId: String, tvdbId: String, resumo: String) -> Unit
) {
    var imdb by remember(imdbIdAtual) { mutableStateOf(imdbIdAtual) }
    var tvdb by remember(tvdbIdAtual) { mutableStateOf(tvdbIdAtual) }
    TemaASheetHeader(nomeSerie, "Identificadores", onClose)
    CampoTemaA("TMDB ID", tmdbId, {}, readOnly = true, extra = "Somente leitura")
    CampoTemaA("IMDb ID", imdb, { imdb = it.trim() }, placeholder = "Ex.: tt1234567")
    CampoTemaA("TVDB ID", tvdb, { tvdb = it.trim() }, placeholder = "Ex.: 123456")
    TemaAPrimaryButton("Salvar", onClick = {
        val resumo = when {
            imdb.isNotBlank() && tvdb.isNotBlank() -> "IMDb • TVDB"
            imdb.isNotBlank() -> "IMDb"
            tvdb.isNotBlank() -> "TVDB"
            else -> "TMDB"
        }
        onSave(imdb, tvdb, resumo)
    })
}

@Composable
fun CampoTemaA(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    extra: String = "",
    placeholder: String = "",
    mono: Boolean = true,
    secreto: Boolean = false
) {
    var focado by remember { mutableStateOf(false) }
    // campo secreto (ex.: chave de API): mostra pontinhos até tocar no olho
    var mostrar by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text(label, color = TemaATokens.TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (extra.isNotBlank()) Text(extra, color = TemaATokens.TextoApagado, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        val shape = RoundedCornerShape(14.dp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            singleLine = true,
            visualTransformation = if (secreto && !mostrar) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            cursorBrush = SolidColor(accent),
            textStyle = TextStyle(
                color = if (readOnly) TemaATokens.TextoApagado else TemaATokens.TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = if (mono) TemaATokens.FontMono else TemaATokens.FontSans
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .onFocusChanged { focado = it.isFocused }
                .clip(shape)
                .background(if (readOnly) TemaASheetColorsLocal.ReadOnly else TemaATokens.DarkBackground)
                .then(
                    when {
                        readOnly -> Modifier
                        focado -> Modifier.border(2.dp, accent, shape)
                        else -> Modifier.border(1.dp, TemaATokens.DarkBorder, shape)
                    }
                ),
            decorationBox = { inner ->
                Box(Modifier.padding(start = 14.dp, end = if (secreto) 48.dp else 14.dp), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty() && placeholder.isNotBlank()) {
                        Text(placeholder, color = TemaATokens.TextMuted, fontSize = 16.sp, fontFamily = TemaATokens.FontMono)
                    }
                    inner()
                }
                if (secreto && value.isNotEmpty()) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    androidx.compose.material3.Icon(
                        if (mostrar) androidx.compose.material.icons.Icons.Outlined.VisibilityOff else androidx.compose.material.icons.Icons.Outlined.Visibility,
                        if (mostrar) "Esconder" else "Mostrar",
                        tint = TemaATokens.TextGray,
                        modifier = Modifier.padding(end = 6.dp).size(40.dp).clip(RoundedCornerShape(20.dp)).clickable { mostrar = !mostrar }.padding(10.dp)
                    )
                }
            }
        )
    }
}

private object TemaASheetColorsLocal {
    val ReadOnly = TemaATokens.Neutro
}


@Composable
fun ColumnScope.MinutagemFixaSheetContentTemaA(
    nomeSerie: String,
    aberturaAtiva: Boolean,
    recapAtivo: Boolean,
    encerramentoAtivo: Boolean,
    duracaoFixaAbertura: String,
    duracaoFixaRecap: String,
    duracaoFixaEncerramento: String,
    onClose: () -> Unit,
    onSave: (Boolean, Boolean, Boolean, String, String, String) -> Unit
) {
    var ab by remember { mutableStateOf(aberturaAtiva) }
    var rec by remember { mutableStateOf(recapAtivo) }
    var enc by remember { mutableStateOf(encerramentoAtivo) }
    var dAb by remember { mutableStateOf(duracaoFixaAbertura) }
    var dRec by remember { mutableStateOf(duracaoFixaRecap) }
    var dEnc by remember { mutableStateOf(duracaoFixaEncerramento) }
    var editando by remember { mutableStateOf<String?>(null) }

    val secaoEditando = editando
    if (secaoEditando != null) {
        // Editor da duração dentro do mesmo pop-up (mesmo teclado do episódio)
        val atual = when (secaoEditando) { "Abertura" -> dAb; "Recap" -> dRec; else -> dEnc }
        var dig by remember(secaoEditando) { mutableStateOf(digitosTempoTemaA(atual)) }
        val valido = dig.isNotEmpty() && segundosValidosTemaA(dig) && (segundosTempoTemaA(dig) ?: 0) > 0
        fun gravar(v: String) {
            when (secaoEditando) { "Abertura" -> dAb = v; "Recap" -> dRec = v; else -> dEnc = v }
            editando = null
        }
        TemaASheetHeader(nomeSerie, "Duração fixa · $secaoEditando", onClose = { editando = null })
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(TemaASheetColors.Group)
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Duração", color = TemaATokens.TextGray, fontSize = 12.sp)
            Text(
                formatarTempoTemaA(dig),
                color = if (dig.isEmpty()) TemaATokens.TextMuted else TemaATokens.TextWhite,
                fontFamily = TemaATokens.FontMono,
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            if (!segundosValidosTemaA(dig)) "Os segundos vão até 59." else "Digite da direita para a esquerda: 130 = 01:30.",
            color = if (!segundosValidosTemaA(dig)) TemaATokens.ErroTexto else TemaATokens.TextGray,
            fontSize = 12.sp
        )
        TecladoTempoTemaA { dig = teclaTempoTemaA(dig, it) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TemaASecondaryButton("Remover", onClick = { gravar("") }, modifier = Modifier.weight(1f))
            TemaAPrimaryButton("Confirmar", onClick = { gravar(formatarTempoTemaA(dig)) }, modifier = Modifier.weight(1f), enabled = valido)
        }
        return
    }

    TemaASheetHeader(nomeSerie, "Minutagem fixa", onClose)
    Text(
        "Durações aplicadas a todos os episódios da série. Toque numa seção para editar.",
        color = TemaATokens.TextGray,
        fontSize = 13.sp,
        lineHeight = 19.sp
    )
    TemaAGroup {
        LinhaFixaTemaA("Abertura", Icons.Filled.PlayArrow, dAb, ab, { ab = it }, { editando = "Abertura" })
        TemaAGroupDivider()
        LinhaFixaTemaA("Recap", Icons.Filled.Replay, dRec, rec, { rec = it }, { editando = "Recap" }, warning = true)
        TemaAGroupDivider()
        LinhaFixaTemaA("Encerramento", Icons.Filled.Stop, dEnc, enc, { enc = it }, { editando = "Encerramento" })
    }
    TemaAPrimaryButton("Salvar", onClick = { onSave(ab, rec, enc, dAb, dRec, dEnc) })
}

@Composable
private fun LinhaFixaTemaA(
    nome: String,
    icon: ImageVector,
    duracao: String,
    ativo: Boolean,
    onAtivo: (Boolean) -> Unit,
    onEditar: () -> Unit,
    warning: Boolean = false
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditar)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (warning) TemaATokens.WarningBg else TemaATokens.ErroFundo),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (warning) TemaATokens.SemanticWarning else TemaATokens.ErroTexto, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(nome, color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (duracao.isNotBlank()) {
                Text(duracao, color = TemaATokens.TextWhite, fontSize = 13.sp, fontFamily = TemaATokens.FontMono)
            } else {
                Text("Não possui", color = TemaATokens.TextoApagado, fontSize = 12.sp)
            }
        }
        TemaASwitch(ativo, onAtivo)
    }
}
