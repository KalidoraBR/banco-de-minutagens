package com.kalidorabr.bancodeminutagens.ui.temaa.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.R
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens

/** Barra de abas flutuante (pílula) do Tema A: a aba ativa vira um botão claro com nome. */
@Composable
fun HomeBottomNavigationTemaA(
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {}
) {
    val shape = RoundedCornerShape(32.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TemaATokens.DarkBackground)
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp)
            .height(TemaATokens.HeightBottomNav)
            .clip(shape)
            .background(TemaATokens.NavSurface)
            .border(1.dp, TemaATokens.DarkBorder, shape)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItemTemaA(painterResource(R.drawable.ic_home), "Início", selectedTab == 0) { onTabSelected(0) }
        NavItemTemaA(painterResource(R.drawable.ic_search), "Buscar", selectedTab == 1) { onTabSelected(1) }
        NavItemTemaA(painterResource(R.drawable.ic_em_breve), "Em breve", selectedTab == 2) { onTabSelected(2) }
        NavItemTemaA(painterResource(R.drawable.ic_adjusts_sliders), "Ajustes", selectedTab == 3) { onTabSelected(3) }
    }
}

@Composable
private fun NavItemTemaA(painter: Painter, label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) TemaATokens.TextWhite else TemaATokens.NavSurface.copy(alpha = 0f))
            .clickable(onClick = onClick)
            .animateContentSize(tween(200))
            .padding(horizontal = if (selected) 16.dp else 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painter,
            contentDescription = label,
            tint = if (selected) TemaATokens.DarkBackground else TemaATokens.TextGray,
            modifier = Modifier.size(22.dp)
        )
        if (selected) {
            Spacer(Modifier.width(8.dp))
            Text(label, color = TemaATokens.DarkBackground, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
