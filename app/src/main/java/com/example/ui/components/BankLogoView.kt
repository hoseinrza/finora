package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

data class BankInfo(
    val name: String,
    val shortCode: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    @DrawableRes val iconRes: Int,
    val textColor: Color = Color.White
)

object BankRegistry {

    private val BANKS = listOf(
        BankInfo("بلوبانک", "بلو", Color(0xFF2563EB), Color(0xFF1D4ED8), R.drawable.ic_bank_blu),
        BankInfo("بلو", "بلو", Color(0xFF2563EB), Color(0xFF1D4ED8), R.drawable.ic_bank_blu),
        BankInfo("سامان", "سامان", Color(0xFF0284C7), Color(0xFF0369A1), R.drawable.ic_bank_saman),
        BankInfo("ملت", "ملت", Color(0xFFE11D48), Color(0xFFBE123C), R.drawable.ic_bank_mellat),
        BankInfo("ملی", "ملی", Color(0xFF1E3A8A), Color(0xFFD97706), R.drawable.ic_bank_melli),
        BankInfo("پاسارگاد", "پاسارگاد", Color(0xFFD97706), Color(0xFFB45309), R.drawable.ic_bank_pasargad),
        BankInfo("تجارت", "تجارت", Color(0xFF0F766E), Color(0xFF115E59), R.drawable.ic_bank_tejarat),
        BankInfo("پارسیان", "پارسیان", Color(0xFFB45309), Color(0xFF78350F), R.drawable.ic_bank_parsian),
        BankInfo("صادرات", "صادرات", Color(0xFF0369A1), Color(0xFF075985), R.drawable.ic_bank_saderat),
        BankInfo("سپه", "سپه", Color(0xFF334155), Color(0xFF1E293B), R.drawable.ic_bank_sepah),
        BankInfo("کشاورزی", "کشاورزی", Color(0xFF15803D), Color(0xFF166534), R.drawable.ic_bank_keshavarzi),
        BankInfo("مسکن", "مسکن", Color(0xFFC2410C), Color(0xFF9A3412), R.drawable.ic_bank_maskan),
        BankInfo("آینده", "آینده", Color(0xFF6D28D9), Color(0xFF5B21B6), R.drawable.ic_bank_ayandeh),
        BankInfo("رفاه", "رفاه", Color(0xFF1D4ED8), Color(0xFF1E40AF), R.drawable.ic_bank_refah),
        BankInfo("شهر", "شهر", Color(0xFFB91C1C), Color(0xFF991B1B), R.drawable.ic_bank_shahr),
        BankInfo("دی", "دی", Color(0xFF0284C7), Color(0xFF0369A1), R.drawable.ic_bank_dey),
        BankInfo("اقتصاد نوین", "اقتصاد", Color(0xFF7C3AED), Color(0xFF6D28D9), R.drawable.ic_bank_eghtesad_novin),
        BankInfo("سینا", "سینا", Color(0xFF2563EB), Color(0xFF1D4ED8), R.drawable.ic_bank_sina),
        BankInfo("کارآفرین", "کارآفرین", Color(0xFF059669), Color(0xFF047857), R.drawable.ic_bank_karafarin),
        BankInfo("صنعت و معدن", "صنعت", Color(0xFFD97706), Color(0xFFB45309), R.drawable.ic_bank_sanat_madan),
        BankInfo("سرمایه", "سرمایه", Color(0xFF0284C7), Color(0xFF0369A1), R.drawable.ic_bank_sarmaye),
        BankInfo("قوامین", "قوامین", Color(0xFF334155), Color(0xFF1E293B), R.drawable.ic_bank_ghavamin),
        BankInfo("انصار", "انصار", Color(0xFF1E3A8A), Color(0xFF1E40AF), R.drawable.ic_bank_ansar)
    )

    fun findBank(vararg texts: String?): BankInfo? {
        val combined = texts.filterNotNull().joinToString(" ")
        if (combined.isBlank()) return null

        return BANKS.firstOrNull { combined.contains(it.name) || combined.contains(it.shortCode) }
    }

    fun getBankInfo(text: String?): BankInfo {
        val found = findBank(text)
        if (found != null) return found

        if (text.isNullOrBlank()) {
            return BankInfo("بانک", "بانک", Color(0xFF0284C7), Color(0xFF0369A1), R.drawable.ic_bank_default)
        }

        val short = if (text.length <= 4) text else text.take(3)
        return BankInfo(text, short, Color(0xFF0284C7), Color(0xFF0369A1), R.drawable.ic_bank_default)
    }
}

@Composable
fun BankLogoBadge(
    bankName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    showBorder: Boolean = true
) {
    val bank = BankRegistry.getBankInfo(bankName)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .then(
                if (showBorder) Modifier.border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(size * 0.32f)
                ) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = bank.iconRes),
            contentDescription = bank.name,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun BankChip(
    bankName: String?,
    modifier: Modifier = Modifier
) {
    val bank = BankRegistry.findBank(bankName) ?: BankRegistry.getBankInfo(bankName)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bank.primaryColor.copy(alpha = 0.15f))
            .border(0.5.dp, bank.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(bank.primaryColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = bank.shortCode,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = bank.primaryColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
