package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentBlueSoft
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedSoft
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.util.CurrencyFormatter

@Composable
fun TransactionItemView(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCredit = transaction.type == "CREDIT"
    val isExpense = transaction.type == "EXPENSE"
    val isTransfer = transaction.type == "TRANSFER"

    val iconBgColor = when {
        isCredit -> EmeraldGlow
        isExpense -> ExpenseRedSoft
        else -> AccentBlueSoft
    }

    val iconTint = when {
        isCredit -> EmeraldGreen
        isExpense -> ExpenseRed
        else -> AccentBlue
    }

    val amountColor = when {
        isCredit -> EmeraldGreen
        isExpense -> ExpenseRed
        else -> AccentBlue
    }

    val prefix = when {
        isCredit -> "+ "
        isExpense -> "- "
        else -> ""
    }

    val accountLabel = when {
        isCredit -> transaction.toAccountId?.uppercase() ?: "Account"
        isExpense -> transaction.fromAccountId?.uppercase() ?: "Account"
        else -> "${transaction.fromAccountId?.uppercase()} to ${transaction.toAccountId?.uppercase()}"
    }

    val title = if (isTransfer) {
        "${transaction.fromAccountId?.uppercase()} to ${transaction.toAccountId?.uppercase()}"
    } else {
        transaction.category
    }

    val subtitle = if (isTransfer) {
        CurrencyFormatter.formatDateTime(transaction.timestamp)
    } else {
        "$accountLabel • ${CurrencyFormatter.formatDateTime(transaction.timestamp)}"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .testTag("tx_item_${transaction.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Circular status icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isCredit -> Icons.Default.ArrowDownward
                        isExpense -> Icons.Default.ArrowUpward
                        else -> Icons.Default.CompareArrows
                    },
                    contentDescription = transaction.type,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextGray,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$prefix${CurrencyFormatter.formatPaisa(transaction.amountPaisa)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = amountColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
                if (isTransfer) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceCard)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Transfer",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextGray,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Details",
                tint = TextGray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
