package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.util.CurrencyFormatter

@Composable
fun OnboardingSetupScreen(
    onCompleteSetup: (userName: String, pin: String, ablPaisa: Long, ublPaisa: Long, walletPaisa: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) } // 1: Name, 2: PIN, 3: Opening Balances

    var userName by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    var ablOpeningInput by remember { mutableStateOf("450000") }
    var ublOpeningInput by remember { mutableStateOf("280500") }
    var walletOpeningInput by remember { mutableStateOf("0") }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .testTag("onboarding_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo & Branding
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(listOf(EmeraldGreen, Color(0xFF047857)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = "Pockitify Logo",
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = buildAnnotatedString {
                append("Pock")
                withStyle(style = SpanStyle(color = MintGreen)) {
                    append("i")
                }
                append("tify")
            },
            style = MaterialTheme.typography.headlineLarge.copy(
                color = TextWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                letterSpacing = (-0.5).sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Track every move your money makes.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextGray,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Progress indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..3) {
                Box(
                    modifier = Modifier
                        .size(width = if (step == i) 32.dp else 10.dp, height = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (step >= i) EmeraldGreen else Color(0xFF1B2E4E))
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Steps Content
        when (step) {
            1 -> {
                // Step 1: User Name
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "What should we call you?",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your name will be used to personalize your offline finance dashboard.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = userName,
                            onValueChange = {
                                userName = it
                                errorMsg = null
                            },
                            placeholder = { Text("e.g. Sultan Ali", color = TextGray) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = EmeraldGreen)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = Color(0xFF263C5F)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_name_input")
                        )
                    }
                }

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMsg ?: "", color = ExpenseRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (userName.trim().isBlank()) {
                            errorMsg = "Please enter your name"
                        } else {
                            errorMsg = null
                            step = 2
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_step1_next")
                ) {
                    Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            2 -> {
                // Step 2: 4-Digit PIN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Set Up a 6-Digit PIN",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your 6-digit PIN secures all offline accounts on this device.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = pin,
                            onValueChange = {
                                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                    pin = it
                                    errorMsg = null
                                }
                            },
                            label = { Text("6-Digit PIN", color = TextGray) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = EmeraldGreen)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = Color(0xFF263C5F)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_pin_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = {
                                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                    confirmPin = it
                                    errorMsg = null
                                }
                            },
                            label = { Text("Confirm 6-Digit PIN", color = TextGray) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = EmeraldGreen)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = Color(0xFF263C5F)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_confirm_pin_input")
                        )
                    }
                }

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMsg ?: "", color = ExpenseRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { step = 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Back", color = TextGray)
                    }

                    Button(
                        onClick = {
                            if (pin.length != 6) {
                                errorMsg = "PIN must be exactly 6 digits"
                            } else if (pin != confirmPin) {
                                errorMsg = "PINs do not match"
                            } else {
                                errorMsg = null
                                step = 3
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(2f)
                            .height(52.dp)
                            .testTag("onboarding_step2_next")
                    ) {
                        Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            3 -> {
                // Step 3: Opening Balances for ABL, UBL, and Wallet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Initial Account Balances",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter current funds for your three primary accounts (PKR):",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // ABL
                        OutlinedTextField(
                            value = ablOpeningInput,
                            onValueChange = { ablOpeningInput = it },
                            label = { Text("Allied Bank (ABL) Opening PKR", color = TextGray) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = MintGreen)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = Color(0xFF263C5F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("opening_abl_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // UBL
                        OutlinedTextField(
                            value = ublOpeningInput,
                            onValueChange = { ublOpeningInput = it },
                            label = { Text("United Bank (UBL) Opening PKR", color = TextGray) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = MintGreen)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = Color(0xFF263C5F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("opening_ubl_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Wallet
                        OutlinedTextField(
                            value = walletOpeningInput,
                            onValueChange = { walletOpeningInput = it },
                            label = { Text("Cash Wallet (Purse) Opening PKR", color = TextGray) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MintGreen)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = Color(0xFF263C5F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("opening_wallet_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { step = 2 },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Back", color = TextGray)
                    }

                    Button(
                        onClick = {
                            val ablPaisa = CurrencyFormatter.parseToPaisa(ablOpeningInput)
                            val ublPaisa = CurrencyFormatter.parseToPaisa(ublOpeningInput)
                            val walletPaisa = CurrencyFormatter.parseToPaisa(walletOpeningInput)
                            onCompleteSetup(userName, pin, ablPaisa, ublPaisa, walletPaisa)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(2f)
                            .height(52.dp)
                            .testTag("complete_setup_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Launch Pockitify", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
