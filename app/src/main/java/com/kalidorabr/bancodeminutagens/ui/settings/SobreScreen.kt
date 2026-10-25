package com.kalidorabr.bancodeminutagens.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.History
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.R
import com.kalidorabr.bancodeminutagens.ui.theme.FullTokens
import com.kalidorabr.bancodeminutagens.ui.theme.BancoDeMinutagensTheme
import com.kalidorabr.bancodeminutagens.ui.theme.BorderColor
import com.kalidorabr.bancodeminutagens.ui.theme.DarkBackground
import com.kalidorabr.bancodeminutagens.ui.theme.SurfaceDark
import com.kalidorabr.bancodeminutagens.ui.theme.TextPrimary
import com.kalidorabr.bancodeminutagens.ui.theme.TextSecondary

data class VersaoHistoricoItem(
    val versao: String,
    val data: String,
    val isAtual: Boolean = false,
    val alteracoes: List<String>
)

val historicoVersoesApp = listOf(
    VersaoHistoricoItem(
        versao = "1.8",
        data = "25/10/2026",
        isAtual = true,
        alteracoes = listOf(
            "Integração com o IntroDB: envio das minutagens pelo menu da temporada (escolhendo os episódios) ou sozinho ao tocar em Concluir, com pop-up próprio de progresso.",
            "IntroDB: o app confere o que já está no site (nuvem nos episódios) e oferece \"Usar\" os tempos de lá nas seções vazias, que ficam editáveis.",
            "Modo maratona: Concluir abre o próximo episódio.",
            "Aviso de estreia: notificação no dia em que uma série de Em breve estreia.",
            "Backup automático toda semana numa pasta escolhida.",
            "Duração real de cada episódio (TMDB) na linha do tempo e no aviso de tempo estranho.",
            "Desfazer ao salvar uma seção, e o Continuar volta no episódio que ficou parcial.",
            "Seções do episódio maiores: o bloco inteiro abre o teclado.",
            "Atalhos no ícone do app e aviso de versão nova pelo GitHub.",
            "Versão otimizada: app menor e mais rápido."
        )
    ),
    VersaoHistoricoItem(
        versao = "1.7",
        data = "11/10/2026",
        isAtual = false,
        alteracoes = listOf(
            "Aviso de tempo estranho: ao salvar, o app compara com os outros episódios da série e avisa se a duração parece errada. Toque em \"Salvar assim\" se estiver certo.",
            "Episódios novos: ao abrir o app, ele confere no TMDB se saíram episódios e mostra um cartão na Início com o botão \"Adicionar\".",
            "Nova tela de Estatísticas (Ajustes › Estatísticas): séries, episódios, gráfico da semana, dias seguidos, durações médias e quanto falta em cada série.",
            "Lembrete de backup: depois de 7 dias sem backup em arquivo ou sincronização com o Sheets, uma faixa na Início lembra de fazer um.",
            "Comemoração ao finalizar uma série: confete, pôster no anel verde e uma vibração curta.",
            "Telas vazias com ilustração de claquete.",
            "A Versão Lite foi removida: quem usava passa a abrir no Tema A."
        )
    ),
    VersaoHistoricoItem(
        versao = "1.6",
        data = "10/10/2026",
        isAtual = false,
        alteracoes = listOf(
            "Novo Tema C (streaming): grafite e vermelho, imagem grande da série que se apaga no fundo, botões em pílula e barra de abas com o reflexo da cor de destaque.",
            "Novo Tema D (painel circular): índigo e neon roxo suave, anéis de progresso em tudo, doca redonda flutuante e pop-ups em cartões flutuantes.",
            "No Tema D, a linha do tempo do episódio virou um anel dividido em 3 partes (Abertura, Recap e Encerramento) e os episódios da temporada viraram pontos redondos.",
            "Transições próprias: \"sala de cinema\" no C (telas que crescem do fundo e cortes com zoom) e \"energia\" no D (telas que crescem com mola e anéis que carregam).",
            "Tema B: fonte Archivo, nova transição entre episódios (pilha de papel), topo que acompanha as telas e barra de abas que some ao rolar.",
            "Pop-up central para confirmar a saída do app, com o visual de cada tema."
        )
    ),
    VersaoHistoricoItem(
        versao = "1.5",
        data = "09/10/2026",
        isAtual = false,
        alteracoes = listOf(
            "Nova tela de abertura: o botão \"Pular abertura\" passa por recapitulação e encerramento e vira o ícone do app, em cerca de 3 segundos.",
            "Novo Tema B: claro e quente, com cabeçalho escuro de base curva, títulos em maiúsculas e cards brancos. Escolha em Ajustes › Interface.",
            "Transições próprias do Tema B: o cabeçalho da série estica, o pôster cresce no lugar, a temporada se desdobra, o episódio sobe como uma folha e \"Próximo\" vira a página.",
            "No Tema B, os filtros têm um marcador que estica como elástico, os pop-ups sobem com um pequeno quique e os botões apertam para os lados ao toque.",
            "Otimização: a abertura ficou sem engasgos (redesenha só o necessário a cada quadro) e a Início carrega dados e imagens por trás dela.",
            "Otimização: trocar de tema muda as cores na hora, sem recriar as telas nem perder onde você estava."
        )
    ),
    VersaoHistoricoItem(
        versao = "1.4",
        data = "08/10/2026",
        isAtual = false,
        alteracoes = listOf(
            "Novo visual Tema A: escuro, minimalista e com pop-ups que sobem de baixo.",
            "Novo ícone e nova abertura animada \"Pular abertura\", que já carrega a Início por trás.",
            "Episódio mais prático: \"Igual ao anterior\" por seção, \"Ir para o próximo\" e digitação Continuar → Salvar.",
            "Status automático: a série vira Finalizada ao completar e Incompleta se chegar ao fim com lacunas.",
            "Busca dentro da coleção, aba Em breve com estreias e botão Começar, e Desfazer ao mover para a lixeira.",
            "Visuais Full e Experimental removidos: ficam o Lite e o Tema A."
        )
    ),
    VersaoHistoricoItem(
        versao = "1.3",
        data = "01/10/2026",
        isAtual = false,
        alteracoes = listOf(
            "Nova introdução/splash animada com o emblema oficial 3D e feixe laser BM.",
            "Carregamento instantâneo da home em segundo plano durante a introdução.",
            "Correção e padronização dos textos e resultados vazios da aba 'Em Breve'.",
            "Padronização da tipografia e alinhamento do cabeçalho da versão Full.",
            "Ajustes de layout no card de sincronização automática e badge de pendências.",
            "Visual Lite dedicado e compatível para as telas do Google Sheets e Sobre."
        )
    ),
    VersaoHistoricoItem(
        versao = "1.2",
        data = "27/09/2026",
        isAtual = false,
        alteracoes = listOf(
            "Desfazer após aplicar preenchimento em lote.",
            "Prévia de episódios atualizados e ignorados no preenchimento em lote.",
            "Painel de estatísticas do catálogo e dos episódios.",
            "Exportação e restauração de backup local validado.",
            "Indicador claro de conexão e pendências de sincronização.",
            "Detecção de intervalos de minutagem inválidos ou incomuns."
        )
    ),
    VersaoHistoricoItem(
        versao = "1.1",
        data = "26/09/2026",
        isAtual = false,
        alteracoes = listOf(
            "Redesign moderno com efeito Glassmorphism nos cards e botões.",
            "Identidade visual oficial com novo ícone adaptativo e tela de abertura animada BM.",
            "Redesenho da tela de minutagem de episódios com progresso em segmentos e salvamento automático.",
            "Padronização dos popups de sincronização e mensagens de salvamento.",
            "Ajuste na navegação inferior com guias dedicadas ('Buscar' e 'Em Breve').",
            "Nova tela 'Sobre' com histórico de atualizações do aplicativo."
        )
    ),
    VersaoHistoricoItem(
        versao = "1.0",
        data = "20/09/2026",
        isAtual = false,
        alteracoes = listOf(
            "Lançamento inicial do aplicativo Banco de Minutagens.",
            "Suporte aos modelos de interface Lite e Full.",
            "Sincronização bidirecional e exportação/importação via Google Sheets.",
            "Controle completo de minutagens de abertura, recap e encerramento.",
            "Gerenciamento de lixeira e suporte a identificadores TMDB/IMDb."
        )
    )
)

@Composable
fun SobreScreen(
    modifier: Modifier = Modifier,
    isFullTheme: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    val backgroundColor = if (isFullTheme) FullTokens.DarkBackground else DarkBackground
    val cardBackground = if (isFullTheme) {
        FullTokens.GlassCardBackground
    } else {
        Brush.linearGradient(listOf(SurfaceDark, SurfaceDark))
    }
    val cardBorder = if (isFullTheme) {
        FullTokens.GlassCardBorder
    } else {
        Brush.linearGradient(listOf(BorderColor, BorderColor))
    }
    val cardShape = if (isFullTheme) RoundedCornerShape(18.dp) else RoundedCornerShape(12.dp)
    val titleColor = if (isFullTheme) FullTokens.TextWhite else TextPrimary
    val subtitleColor = if (isFullTheme) FullTokens.TextGray else TextSecondary

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header (Navegação via botão Voltar do sistema/celular)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sobre o Aplicativo",
                color = titleColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card Principal do Aplicativo
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBackground, cardShape)
                        .border(1.dp, cardBorder, cardShape)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF0D0D0E), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home),
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Banco de Minutagens",
                        color = titleColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Seu catálogo de séries, do seu jeito.",
                        color = subtitleColor,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .background(primaryColor.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, primaryColor.copy(alpha = 0.8f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Versão ${historicoVersoesApp.first { it.isAtual }.versao} • Atualizada",
                            color = primaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Título do Histórico
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = "Histórico de Atualizações",
                        color = titleColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Lista de Versões do Histórico
            items(historicoVersoesApp) { itemVersao ->
                var expanded by remember(itemVersao.versao) { mutableStateOf(itemVersao.isAtual) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBackground, cardShape)
                        .border(1.dp, cardBorder, cardShape)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Versão ${itemVersao.versao}",
                                color = titleColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (itemVersao.isAtual) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF22C55E).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Atual",
                                        color = Color(0xFF22C55E),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = itemVersao.data,
                                color = subtitleColor,
                                fontSize = 12.sp
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) "Recolher notas" else "Ver notas",
                                tint = subtitleColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.06f))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            itemVersao.alteracoes.forEach { alteracao ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "•",
                                        color = primaryColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = alteracao,
                                        color = subtitleColor,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Preview(
    name = "Sobre Screen Lite Preview",
    showBackground = true,
    backgroundColor = 0xFF080808
)
@Composable
private fun SobreScreenLitePreview() {
    BancoDeMinutagensTheme {
        SobreScreen(isFullTheme = false)
    }
}

@Preview(
    name = "Sobre Screen Full Preview",
    showBackground = true,
    backgroundColor = 0xFF0D0D0E
)
@Composable
private fun SobreScreenFullPreview() {
    BancoDeMinutagensTheme {
        SobreScreen(isFullTheme = true)
    }
}
