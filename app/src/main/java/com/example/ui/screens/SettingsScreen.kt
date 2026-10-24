package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.preferences.UserProfileState
import com.example.data.repository.FinanceRepository
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userProfile: UserProfileState,
    categories: List<Category>,
    financeRepository: FinanceRepository,
    onBack: () -> Unit,
    onUpdateUserName: (String) -> Unit,
    onChangePin: (currentPin: String, newPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onAddCategory: (name: String, type: String) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onResetMonthClicked: () -> Unit,
    onLockApp: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showNameDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf(userProfile.userName) }

    var showPinDialog by remember { mutableStateOf(false) }
    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    var showCategoriesDialog by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var newCatType by remember { mutableStateOf("EXPENSE") }

    var showBackupDialog by remember { mutableStateOf(false) }
    var backupJsonString by remember { mutableStateOf("") }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var restoreStatus by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = DarkSurface,
            title = { Text("Log out of Pockitify?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You will need your PIN to unlock the app again. Your financial data will remain on this device.",
                    color = TextGrayLight
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Log out", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    // Dialog for Name Change
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = DarkSurface,
            title = { Text("Update Name", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { newNameInput = it },
                    label = { Text("Your Name", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = Color(0xFF263C5F)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateUserName(newNameInput)
                        showNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNameDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    // Dialog for Changing PIN
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = DarkSurface,
            title = { Text("Change 6-Digit PIN", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentPinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) currentPinInput = it },
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
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPinInput = it },
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
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) confirmPinInput = it },
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
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError != null) {
                        Text(text = pinError ?: "", color = ExpenseRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput != confirmPinInput) {
                            pinError = "New PINs do not match"
                            return@Button
                        }
                        if (newPinInput.length != 6) {
                            pinError = "PIN must be exactly 6 digits"
                            return@Button
                        }
                        onChangePin(
                            currentPinInput,
                            newPinInput,
                            {
                                showPinDialog = false
                                currentPinInput = ""
                                newPinInput = ""
                                confirmPinInput = ""
                                pinError = null
                            },
                            { err -> pinError = err }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Update PIN", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    // Dialog for Managing Categories
    if (showCategoriesDialog) {
        AlertDialog(
            onDismissRequest = { showCategoriesDialog = false },
            containerColor = DarkSurface,
            title = { Text("Manage Categories", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Add Custom Category",
                        color = MintGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Category Name", color = TextGray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color(0xFF263C5F)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (newCatName.isNotBlank()) {
                                    onAddCategory(newCatName, newCatType)
                                    newCatName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add Category", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Existing Categories",
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = cat.name, color = TextWhite, fontWeight = FontWeight.Medium)
                                Text(text = if (cat.isDefault) "Default • ${cat.type}" else "Custom • ${cat.type}", color = TextGray, fontSize = 11.sp)
                            }
                            if (!cat.isDefault) {
                                IconButton(onClick = { onDeleteCategory(cat) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCategoriesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Done", color = Color.White)
                }
            }
        )
    }

    // Backup Export Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            containerColor = DarkSurface,
            title = { Text("Local Application Backup", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Your local JSON backup data includes accounts, categories, and tracking periods.",
                        color = TextGrayLight,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = backupJsonString,
                        onValueChange = {},
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color(0xFF263C5F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, backupJsonString)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Pockitify Backup JSON")
                        context.startActivity(shareIntent)
                        showBackupDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Share / Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBackupDialog = false }) {
                    Text("Close", color = TextGray)
                }
            }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            containerColor = DarkSurface,
            title = { Text("Restore Local Backup", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Paste your previously saved Pockitify backup JSON below:",
                        color = TextGrayLight,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        placeholder = { Text("Paste JSON here...", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color(0xFF263C5F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                    if (restoreStatus != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = restoreStatus ?: "", color = MintGreen, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val res = financeRepository.restoreJsonBackup(restoreJsonInput)
                            if (res.isSuccess) {
                                restoreStatus = "Data restored successfully!"
                                showRestoreDialog = false
                            } else {
                                restoreStatus = "Invalid backup format: ${res.exceptionOrNull()?.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Restore", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("settings_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Section
        SettingsCard(title = "User Profile") {
            SettingsItem(
                icon = Icons.Default.Person,
                title = "User Name",
                subtitle = userProfile.userName.ifBlank { "User" },
                onClick = {
                    newNameInput = userProfile.userName
                    showNameDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security Section
        SettingsCard(title = "Security & PIN") {
            SettingsItem(
                icon = Icons.Default.Security,
                title = "Change 6-Digit PIN",
                subtitle = "Update your application unlock PIN",
                onClick = {
                    currentPinInput = ""
                    newPinInput = ""
                    confirmPinInput = ""
                    pinError = null
                    showPinDialog = true
                }
            )

            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Lock Pockitify Now",
                subtitle = "Require PIN on next entry",
                onClick = onLockApp
            )

            SettingsItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Logout",
                subtitle = "Log out and require your PIN to sign in again",
                onClick = { showLogoutDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Financial & Categories
        SettingsCard(title = "Preferences & Management") {
            SettingsItem(
                icon = Icons.Default.Category,
                title = "Manage Categories",
                subtitle = "${categories.size} categories configured",
                onClick = { showCategoriesDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.CalendarMonth,
                title = "Manual Reset Month",
                subtitle = "Close current reporting cycle",
                onClick = onResetMonthClicked
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data & Backup
        SettingsCard(title = "Data Management (Offline)") {
            SettingsItem(
                icon = Icons.Default.Download,
                title = "Export Transactions to CSV",
                subtitle = "Export chronological table for spreadsheets",
                onClick = {
                    coroutineScope.launch {
                        val csv = financeRepository.exportTransactionsCsv()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csv)
                            type = "text/csv"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Pockitify Transactions CSV")
                        context.startActivity(shareIntent)
                    }
                }
            )

            SettingsItem(
                icon = Icons.Default.Backup,
                title = "Backup Application Data",
                subtitle = "Export JSON backup locally",
                onClick = {
                    coroutineScope.launch {
                        backupJsonString = financeRepository.exportJsonBackup()
                        showBackupDialog = true
                    }
                }
            )

            SettingsItem(
                icon = Icons.Default.Backup,
                title = "Restore Backup",
                subtitle = "Restore previously saved JSON backup",
                onClick = {
                    restoreJsonInput = ""
                    restoreStatus = null
                    showRestoreDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About
        SettingsCard(title = "About Pockitify") {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Pockitify v1.0",
                subtitle = "Native Android • Offline Room Database • PKR",
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                color = MintGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(16.dp))
                .padding(vertical = 4.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextGray,
                        fontSize = 12.sp
                    )
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(18.dp)
        )
    }
}
