package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite

@Composable
fun PinLockScreen(
    userName: String,
    isPinUpgradedTo6Digits: Boolean = true,
    onVerifyPin: (pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onUpgradePin: (currentPin: String, newPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Prevent bypassing protected screens using Android back button
    BackHandler {
        val activity = context as? Activity
        activity?.moveTaskToBack(true)
    }

    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Upgrade Dialog State for existing 4-digit PIN users
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var upgradeCurrentPin by remember { mutableStateOf("") }
    var upgradeNewPin by remember { mutableStateOf("") }
    var upgradeConfirmPin by remember { mutableStateOf("") }
    var upgradeError by remember { mutableStateOf<String?>(null) }

    fun onDigitPress(digit: String) {
        if (enteredPin.length < 6) {
            val updated = enteredPin + digit
            enteredPin = updated
            errorMessage = null

            if (updated.length == 6) {
                onVerifyPin(
                    updated,
                    {
                        enteredPin = ""
                        errorMessage = null
                    },
                    { error ->
                        enteredPin = ""
                        errorMessage = error
                    }
                )
            }
        }
    }

    fun onBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    // Modal dialog to securely upgrade existing 4-digit PIN to 6 digits
    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            containerColor = DarkSurfaceCard,
            title = {
                Text(
                    text = "Upgrade to 6-Digit PIN",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Pockitify now requires a 6-digit PIN for enhanced security. Verify your current PIN and choose your new 6-digit PIN. Your financial data will remain completely intact.",
                        color = TextGrayLight,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = upgradeCurrentPin,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) upgradeCurrentPin = it },
                        label = { Text("Current PIN", color = TextGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color(0xFF263C5F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upgrade_current_pin_input")
                    )
                    OutlinedTextField(
                        value = upgradeNewPin,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) upgradeNewPin = it },
                        label = { Text("New 6-Digit PIN", color = TextGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color(0xFF263C5F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upgrade_new_pin_input")
                    )
                    OutlinedTextField(
                        value = upgradeConfirmPin,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) upgradeConfirmPin = it },
                        label = { Text("Confirm New 6-Digit PIN", color = TextGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color(0xFF263C5F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upgrade_confirm_pin_input")
                    )
                    if (upgradeError != null) {
                        Text(
                            text = upgradeError ?: "",
                            color = ExpenseRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (upgradeCurrentPin.isBlank()) {
                            upgradeError = "Please enter your current PIN"
                            return@Button
                        }
                        if (upgradeNewPin.length != 6) {
                            upgradeError = "New PIN must be exactly 6 digits"
                            return@Button
                        }
                        if (upgradeNewPin != upgradeConfirmPin) {
                            upgradeError = "New PINs do not match"
                            return@Button
                        }
                        onUpgradePin(
                            upgradeCurrentPin,
                            upgradeNewPin,
                            {
                                showUpgradeDialog = false
                                upgradeCurrentPin = ""
                                upgradeNewPin = ""
                                upgradeConfirmPin = ""
                                upgradeError = null
                            },
                            { err -> upgradeError = err }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.testTag("submit_pin_upgrade_button")
                ) {
                    Text("Upgrade & Unlock", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showUpgradeDialog = false },
                    modifier = Modifier.testTag("cancel_pin_upgrade_button")
                ) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val compact = maxHeight < 680.dp
        val keypadButtonSize = when {
            maxHeight < 600.dp -> 52.dp
            maxHeight < 720.dp -> 58.dp
            else -> 64.dp
        }
        val sectionSpacing = if (compact) 12.dp else 20.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = if (compact) 12.dp else 20.dp)
                .testTag("pin_lock_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(sectionSpacing, Alignment.CenterVertically)
        ) {
        // Top Logo & Welcome
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(if (compact) 54.dp else 60.dp)
                    .clip(RoundedCornerShape(if (compact) 16.dp else 18.dp))
                    .background(
                        Brush.linearGradient(listOf(EmeraldGreen, Color(0xFF047857)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Pockitify Logo",
                    tint = Color.White,
                    modifier = Modifier.size(if (compact) 28.dp else 30.dp)
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))

            Text(
                text = buildAnnotatedString {
                    append("Pock")
                    withStyle(style = SpanStyle(color = MintGreen)) {
                        append("i")
                    }
                    append("tify")
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (userName.isNotBlank()) "Welcome back, $userName" else "Welcome back",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Enter your 6-digit PIN to unlock",
                style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
            )

            if (!isPinUpgradedTo6Digits) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF132F51))
                        .border(1.dp, MintGreen.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable {
                            upgradeCurrentPin = ""
                            upgradeNewPin = ""
                            upgradeConfirmPin = ""
                            upgradeError = null
                            showUpgradeDialog = true
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("pin_upgrade_banner")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MintGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Using 4-digit PIN? Tap to upgrade to 6 digits",
                            color = MintGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 6 PIN Dots
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 6) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) EmeraldGreen else Color.Transparent)
                            .border(
                                width = 2.dp,
                                color = if (isFilled) EmeraldGreen else Color(0xFF263C5F),
                                shape = CircleShape
                            )
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))
                Text(
                    text = errorMessage ?: "",
                    color = ExpenseRed,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.testTag("pin_error_message")
                )
            }
        }

        // Custom Numeric Keypad
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
        ) {
            // Rows 1-3
            val keyRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )

            for (row in keyRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (digit in row) {
                        PinKeyButton(
                            text = digit,
                            buttonSize = keypadButtonSize,
                            onClick = { onDigitPress(digit) }
                        )
                    }
                }
            }

            // Bottom row: Clear, 0, Backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear button
                Box(
                    modifier = Modifier
                        .size(keypadButtonSize)
                        .clip(CircleShape)
                        .clickable {
                            enteredPin = ""
                            errorMessage = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextGray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                    )
                }

                // 0 button
                PinKeyButton(
                    text = "0",
                    buttonSize = keypadButtonSize,
                    onClick = { onDigitPress("0") }
                )

                // Backspace
                Box(
                    modifier = Modifier
                        .size(keypadButtonSize)
                        .clip(CircleShape)
                        .clickable { onBackspace() }
                        .testTag("keypad_backspace"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = TextWhite,
                        modifier = Modifier.size(if (compact) 20.dp else 22.dp)
                    )
                }
            }
        }
    }
    }
}

@Composable
fun PinKeyButton(
    text: String,
    onClick: () -> Unit,
    buttonSize: androidx.compose.ui.unit.Dp = 72.dp
) {
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(CircleShape)
            .background(DarkSurfaceCard)
            .border(1.dp, Color(0xFF1B2E4E), CircleShape)
            .clickable(onClick = onClick)
            .testTag("keypad_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = TextWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp
            )
        )
    }
}
