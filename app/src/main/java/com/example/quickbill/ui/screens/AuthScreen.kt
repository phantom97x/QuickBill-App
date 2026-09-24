package com.example.quickbill.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quickbill.ui.components.QuickBillGradientButton
import com.example.quickbill.ui.components.QuickBillLogo
import com.example.quickbill.util.AppLanguage
import com.example.quickbill.util.AppStrings
import com.example.ui.theme.HeaderGradient

@Composable
fun AuthScreen(
    onLogin: (String, String, (Boolean) -> Unit) -> Unit,
    onSignup: (String, String, String, (Boolean) -> Unit) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    // Default to Create Shop so user can immediately register their own account
    var isSignup by remember { mutableStateOf(true) }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large QuickBill Logo
                QuickBillLogo(size = 64.dp)

                Text(
                    text = "QuickBill",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A)
                )

                Text(
                    text = "Billing & Inventory Management",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                // Mode Selector Toggle: Create Shop vs Sign In
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    isSignup = true
                                    errorMessage = null
                                }
                                .testTag("btn_tab_create_shop"),
                            color = if (isSignup) Color(0xFF1E3A8A) else Color.Transparent
                        ) {
                            Text(
                                text = "Create Shop",
                                color = if (isSignup) Color.White else Color(0xFF64748B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    isSignup = false
                                    errorMessage = null
                                }
                                .testTag("btn_tab_sign_in"),
                            color = if (!isSignup) Color(0xFF1E3A8A) else Color.Transparent
                        ) {
                            Text(
                                text = "Sign In",
                                color = if (!isSignup) Color.White else Color(0xFF64748B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                if (isSignup) {
                    // Shop Name Input
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text(AppStrings.get("shop_name", language)) },
                        placeholder = { Text("e.g. Patil Hardware Store") },
                        leadingIcon = {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF2563EB))
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_auth_shop_name"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    // Owner Name (Optional)
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner Name (Optional)") },
                        placeholder = { Text("e.g. Rahul Patil") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2563EB))
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_auth_owner_name"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }

                // Phone Input (10 digits)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                    label = { Text(AppStrings.get("phone_number", language)) },
                    placeholder = { Text("10 digit mobile number") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2563EB))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auth_phone"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                // Password / PIN Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(AppStrings.get("password", language)) },
                    placeholder = { Text("Create password or 4-digit PIN") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2563EB))
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auth_password"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                QuickBillGradientButton(
                    text = if (isSignup) AppStrings.get("signup_btn", language) else AppStrings.get("login_btn", language),
                    onClick = {
                        if (isSignup && shopName.isBlank()) {
                            errorMessage = "Please enter your shop name"
                            return@QuickBillGradientButton
                        }
                        if (phone.length < 10) {
                            errorMessage = "Please enter a valid 10-digit phone number"
                            return@QuickBillGradientButton
                        }
                        if (password.isBlank()) {
                            errorMessage = "Please enter password or PIN"
                            return@QuickBillGradientButton
                        }

                        isLoading = true
                        errorMessage = null

                        if (isSignup) {
                            onSignup(phone, password, shopName) { success ->
                                isLoading = false
                                if (!success) errorMessage = "Shop creation failed. Phone may already exist."
                            }
                        } else {
                            onLogin(phone, password) { success ->
                                isLoading = false
                                if (!success) errorMessage = "Invalid mobile number or PIN"
                            }
                        }
                    },
                    gradient = HeaderGradient,
                    loading = isLoading,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_auth_submit"
                )

                Text(
                    text = if (isSignup) AppStrings.get("switch_to_login", language) else AppStrings.get("switch_to_signup", language),
                    color = Color(0xFF2563EB),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            isSignup = !isSignup
                            errorMessage = null
                        }
                        .padding(4.dp)
                        .testTag("btn_auth_toggle_mode")
                )
            }
        }
    }
}
