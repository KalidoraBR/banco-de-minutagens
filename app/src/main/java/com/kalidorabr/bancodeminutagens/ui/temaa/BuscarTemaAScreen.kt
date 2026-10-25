package com.kalidorabr.bancodeminutagens.ui.temaa

import android.content.Context
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kalidorabr.bancodeminutagens.data.TmdbConfiguracaoAusenteException
import com.kalidorabr.bancodeminutagens.data.TmdbSerieResultado
import com.kalidorabr.bancodeminutagens.data.TmdbService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface BuscaTemaA {
    data object Inicial : BuscaTemaA
    data object Carregando : BuscaTemaA
    data class Sucesso(val series: List<TmdbSerieResultado>) : BuscaTemaA
    data class Falha(val mensagem: String) : BuscaTemaA
}

private const val PREFS_BUSCA = "tema_a_buscas"
private const val KEY_RECENTES = "recentes"

private fun lerRecentes(ctx: Context): List<String> =
    ctx.getSharedPreferences(PREFS_BUSCA, Context.MODE_PRIVATE)
        .getString(KEY_RECENTES, "").orEmpty()
        .split('\n').filter { it.isNotBlank() }

private fun salvarRecente(ctx: Context, termo: String): List<String> {
    val novos = (listOf(termo) + lerRecentes(ctx).filterNot { it.equals(termo, true) }).take(6)
    ctx.getSharedPreferences(PREFS_BUSCA, Context.MODE_PRIVATE).edit()
        .putString(KEY_RECENTES, novos.joinToString("\n")).apply()
    return novos
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuscarTemaAScreen(
    modifier: Modifier = Modifier,
    seriesSalvasIds: Set<String> = emptySet(),
    onAdicionarSerie: suspend (TmdbSerieResultado) -> Result<Unit> = { Result.success(Unit) }
) {
    val ctx = LocalContext.current
    val accent = MaterialTheme.colorScheme.primary
    val scope = rememberCoroutineScope()
    var busca by remember { mutableStateOf("") }
    var tentativa by remember { mutableStateOf(0) }
    var estado by remember { mutableStateOf<BuscaTemaA>(BuscaTemaA.Inicial) }
    var recentes by remember { mutableStateOf(lerRecentes(ctx)) }
    var adicionando by remember { mutableStateOf<String?>(null) }
    var erroAdicionar by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(busca, tentativa) {
        val termo = busca.trim()
        if (termo.length < 2) {
            estado = BuscaTemaA.Inicial
            return@LaunchedEffect
        }
        estado = BuscaTemaA.Carregando
        delay(350)
        estado = try {
            BuscaTemaA.Sucesso(TmdbService.pesquisarSeries(termo))
        } catch (e: CancellationException) {
            throw e
        } catch (_: TmdbConfiguracaoAusenteException) {
            BuscaTemaA.Falha("Chave da API do TMDB não configurada.")
        } catch (_: Exception) {
            BuscaTemaA.Falha("Não foi possível buscar. Verifique a conexão.")
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(TemaATokens.DarkBackground)
            .imePadding()
            .padding(top = if (TemaVisual.b) 0.dp else 16.dp)
    ) {
        Column(Modifier.padding(horizontal = TemaATokens.SideMargin), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            CabecalhoPaginaTemaA("Buscar", "Encontre uma série no TMDB para adicionar")
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(TemaATokens.ShapeCardCommon)
                    .background(TemaATokens.DarkGraphiteSurface)
                    .border(2.dp, accent, TemaATokens.ShapeCardCommon)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, null, tint = TemaATokens.TextGray, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                BasicTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    singleLine = true,
                    cursorBrush = SolidColor(accent),
                    textStyle = TextStyle(color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium, fontFamily = TemaATokens.FontSans),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (busca.isEmpty()) Text("Nome da série…", color = TemaATokens.TextMuted, fontSize = 15.sp)
                        inner()
                    }
                )
                if (busca.isNotEmpty()) {
                    Icon(
                        Icons.Filled.Close, "Limpar busca", tint = TemaATokens.TextGray,
                        modifier = Modifier.size(20.dp).clip(CircleShape).clickable { busca = "" }
                    )
                }
            }
            if (busca.isBlank() && recentes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OverlineTemaA("Buscas recentes")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentes.forEach { r ->
                            Text(
                                r, color = TemaATokens.TextSoft, fontSize = 13.sp,
                                modifier = Modifier.clip(CircleShape).background(TemaATokens.DarkCardSurface)
                                    .clickable { busca = r }.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
            erroAdicionar?.let { Text(it, color = TemaATokens.ErroTexto, fontSize = 13.sp) }
        }

        when (val e = estado) {
            BuscaTemaA.Inicial -> VazioBuscaTemaA("Busque uma série", "Digite pelo menos 2 letras. Os dados vêm direto do TMDB.")
            BuscaTemaA.Carregando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accent, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
            }
            is BuscaTemaA.Falha -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(e.mensagem, color = TemaATokens.TextGray, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text("Tentar novamente", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp,
                    modifier = Modifier.clip(TemaATokens.ShapeButtonField).background(accent).clickable { tentativa++ }.padding(horizontal = 18.dp, vertical = 12.dp))
            }
            is BuscaTemaA.Sucesso -> if (e.series.isEmpty()) {
                VazioBuscaTemaA("Nenhuma série encontrada", "Tente buscar por outro nome.")
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = TemaATokens.SideMargin, end = TemaATokens.SideMargin, top = 18.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { OverlineTemaA("Resultados · ${e.series.size}") }
                    items(e.series, key = { it.id }) { s ->
                        ResultadoBuscaTemaA(
                            serie = s,
                            jaSalva = s.id in seriesSalvasIds,
                            adicionando = adicionando == s.id,
                            onAdicionar = {
                                if (s.id in seriesSalvasIds || adicionando != null) return@ResultadoBuscaTemaA
                                adicionando = s.id
                                erroAdicionar = null
                                recentes = salvarRecente(ctx, busca.trim())
                                scope.launch {
                                    onAdicionarSerie(s).onFailure { erroAdicionar = it.message ?: "Falha ao adicionar série." }
                                    adicionando = null
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VazioBuscaTemaA(titulo: String, texto: String) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 40.dp).padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(TemaATokens.DarkGraphiteSurface), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Search, null, tint = TemaATokens.TextGray, modifier = Modifier.size(30.dp))
        }
        Text(titulo, color = TemaATokens.TextWhite, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        Text(texto, color = TemaATokens.TextGray, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ResultadoBuscaTemaA(serie: TmdbSerieResultado, jaSalva: Boolean, adicionando: Boolean, onAdicionar: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        Modifier.fillMaxWidth().clip(TemaATokens.ShapeCardCommon).background(TemaATokens.DarkGraphiteSurface).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(56.dp).height(84.dp).clip(RoundedCornerShape(10.dp)).background(TemaATokens.DarkCardSurface)) {
            serie.posterUrl?.let { AsyncImage(it, "Pôster de ${serie.titulo}", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(serie.titulo, color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(listOf(serie.ano, serie.generos.replace(" • ", " · ")).filter { it.isNotBlank() }.joinToString(" · "), color = TemaATokens.TextGray, fontSize = 12.sp, maxLines = 1)
            if (serie.descricao.isNotBlank()) {
                Text(serie.descricao, color = TemaATokens.TextoCorpo, fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (jaSalva) TemaATokens.SuccessBg else accent)
                .clickable(enabled = !jaSalva && !adicionando, onClick = onAdicionar),
            contentAlignment = Alignment.Center
        ) {
            when {
                adicionando -> CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                jaSalva -> Icon(Icons.Filled.Check, "Já adicionada", tint = TemaATokens.SuccessText, modifier = Modifier.size(20.dp))
                else -> Icon(Icons.Filled.Add, "Adicionar ${serie.titulo}", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}
