package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.ui.theme.AblBlue
import com.example.ui.theme.AblGold
import com.example.ui.theme.AblOrange
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite
import com.example.ui.theme.UblBlue
import com.example.ui.theme.UblGold
import com.example.ui.theme.UblLightBlue
import com.example.ui.theme.WalletGreenDark
import com.example.ui.theme.WalletGreenMedium
import com.example.util.CurrencyFormatter

@Composable
fun TotalBalanceCard(
    totalBalancePaisa: Long,
    isHidden: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F223D),
                        Color(0xFF09172B),
                        Color(0xFF05101F)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 600f)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E3A60),
                        Color(0x3310B981),
                        Color(0xFF152A47)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
            .testTag("total_balance_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Green wallet rounded container
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(EmeraldGreen, Color(0xFF059669))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet Icon",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Total Available Balance",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextGrayLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isHidden) "PKR ••••••••" else CurrencyFormatter.formatPaisa(totalBalancePaisa),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = TextWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("toggle_balance_visibility")
                ) {
                    Icon(
                        imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Balance",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AblAccountCard(
    account: Account?,
    isHidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balance = account?.balancePaisa ?: 0L

    Box(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(AblBlue, Color(0xFF091E36)),
                    start = Offset(0f, 0f),
                    end = Offset(300f, 300f)
                )
            )
            .border(1.dp, Color(0xFF1E385D), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("abl_card")
    ) {
        // Decorative curved gold/orange ribbon in upper right
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val path = Path().apply {
                moveTo(width * 0.45f, 0f)
                cubicTo(
                    width * 0.65f, height * 0.2f,
                    width * 0.6f, height * 0.7f,
                    width, height * 0.85f
                )
                lineTo(width, 0f)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(AblOrange, AblGold, Color(0xFFFFCC00)),
                    start = Offset(width * 0.5f, 0f),
                    end = Offset(width, height * 0.8f)
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ABL Triangle Icon
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF11223A)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val p = Path().apply {
                            moveTo(size.width * 0.5f, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(p, color = AblOrange)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Allied Bank",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                )
            }

            Column {
                Text(
                    text = if (isHidden) "PKR ••••••" else CurrencyFormatter.formatPaisa(balance),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

@Composable
fun UblAccountCard(
    account: Account?,
    isHidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balance = account?.balancePaisa ?: 0L

    Box(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(UblBlue, Color(0xFF073874)),
                    start = Offset(0f, 0f),
                    end = Offset(300f, 300f)
                )
            )
            .border(1.dp, Color(0xFF19559B), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("ubl_card")
    ) {
        // Decorative gold swirl ribbon in upper right
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val path = Path().apply {
                moveTo(width * 0.6f, 0f)
                cubicTo(
                    width * 0.5f, height * 0.45f,
                    width * 0.85f, height * 0.45f,
                    width, height * 0.7f
                )
                lineTo(width, 0f)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(UblGold, Color(0xFFFDE047)),
                    start = Offset(width * 0.6f, 0f),
                    end = Offset(width, height * 0.7f)
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // UBL Pill Icon
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 22.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "UBL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = UblBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Column {
                Text(
                    text = if (isHidden) "PKR ••••••" else CurrencyFormatter.formatPaisa(balance),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

@Composable
fun CashWalletCard(
    account: Account?,
    isHidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balance = account?.balancePaisa ?: 0L

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        WalletGreenDark,
                        WalletGreenMedium,
                        Color(0xFF0F766E)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(600f, 200f)
                )
            )
            .border(1.dp, Color(0xFF0D9488), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("wallet_card")
    ) {
        // Decorative purse/wallet subtle gradient artwork on right
        Canvas(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(140.dp)
        ) {
            val width = size.width
            val height = size.height

            // Stylized leather wallet arc
            drawCircle(
                color = Color(0x2214B8A6),
                radius = height * 0.9f,
                center = Offset(width * 0.8f, height * 0.5f)
            )
            drawCircle(
                color = Color(0x33F59E0B),
                radius = height * 0.25f,
                center = Offset(width * 0.85f, height * 0.3f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wallet icon box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0A5C43)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Cash Wallet Icon",
                        tint = MintGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Cash Wallet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = "Purse",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MintGreen,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHidden) "PKR ••••••" else CurrencyFormatter.formatPaisa(balance),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Details",
                    tint = MintGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
