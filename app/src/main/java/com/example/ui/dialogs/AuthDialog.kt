package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.AuthUserState
import com.example.ui.StringsDefinition

@Composable
fun AuthDialog(
    authUser: AuthUserState,
    strings: StringsDefinition,
    onDismiss: () -> Unit,
    onSignIn: (String, String, (Boolean, String?) -> Unit) -> Unit,
    onSignUp: (String, String, String, (Boolean, String?) -> Unit) -> Unit,
    onSignInAnonymously: ((Boolean, String?) -> Unit) -> Unit,
    onSendPasswordReset: (String, (Boolean, String?) -> Unit) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("auth_dialog"),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (authUser.isAuthenticated) {
                    // Profile / Already signed in view
                    UserProfileContent(
                        authUser = authUser,
                        strings = strings,
                        onSignOut = onSignOut,
                        onDismiss = onDismiss
                    )
                } else {
                    // Sign In / Sign Up Forms
                    AuthFormContent(
                        strings = strings,
                        onSignIn = onSignIn,
                        onSignUp = onSignUp,
                        onSignInAnonymously = onSignInAnonymously,
                        onSendPasswordReset = onSendPasswordReset,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun UserProfileContent(
    authUser: AuthUserState,
    strings: StringsDefinition,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    val initial = remember(authUser.displayName, authUser.email) {
        val name = authUser.displayName?.ifBlank { null } ?: authUser.email ?: "U"
        name.take(1).uppercase()
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (authUser.isAnonymous) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            Text(
                text = initial,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = if (authUser.isAnonymous) strings.guestAccount else (authUser.displayName?.ifBlank { null } ?: "DayPlan User"),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    if (!authUser.isAnonymous && !authUser.email.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = authUser.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (authUser.isAnonymous) "Guest Mode" else "Firebase Authenticated",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = {
            onSignOut()
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sign_out_button"),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Text(strings.signOut, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(10.dp))

    TextButton(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Done")
    }
}

@Composable
private fun AuthFormContent(
    strings: StringsDefinition,
    onSignIn: (String, String, (Boolean, String?) -> Unit) -> Unit,
    onSignUp: (String, String, String, (Boolean, String?) -> Unit) -> Unit,
    onSignInAnonymously: ((Boolean, String?) -> Unit) -> Unit,
    onSendPasswordReset: (String, (Boolean, String?) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Sign Up
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resetSuccessMessage by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = strings.account,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Tab Bar (Sign In / Sign Up)
    TabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = {
                selectedTab = 0
                errorMessage = null
                resetSuccessMessage = null
            },
            text = { Text(strings.signIn, fontWeight = FontWeight.SemiBold) },
            modifier = Modifier.testTag("tab_sign_in")
        )
        Tab(
            selected = selectedTab == 1,
            onClick = {
                selectedTab = 1
                errorMessage = null
                resetSuccessMessage = null
            },
            text = { Text(strings.signUp, fontWeight = FontWeight.SemiBold) },
            modifier = Modifier.testTag("tab_sign_up")
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Error Message Card
    AnimatedVisibility(visible = errorMessage != null) {
        errorMessage?.let { err ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    // Success Message Card (e.g. for reset password)
    AnimatedVisibility(visible = resetSuccessMessage != null) {
        resetSuccessMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    if (selectedTab == 1) {
        // Sign Up Full Name Field
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text(strings.fullName) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("full_name_input"),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(10.dp))
    }

    // Email Field
    OutlinedTextField(
        value = email,
        onValueChange = {
            email = it
            errorMessage = null
        },
        label = { Text(strings.email) },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("email_input"),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Password Field
    OutlinedTextField(
        value = password,
        onValueChange = {
            password = it
            errorMessage = null
        },
        label = { Text(strings.password) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle password visibility"
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("password_input"),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = if (selectedTab == 1) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { focusManager.clearFocus() }
        )
    )

    if (selectedTab == 1) {
        Spacer(modifier = Modifier.height(10.dp))
        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
            },
            label = { Text(strings.confirmPassword) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("confirm_password_input"),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
    }

    if (selectedTab == 0) {
        // Forgot password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    if (email.isBlank()) {
                        errorMessage = "Please enter your email above to reset password"
                    } else {
                        isLoading = true
                        onSendPasswordReset(email) { success, err ->
                            isLoading = false
                            if (success) {
                                resetSuccessMessage = strings.resetPasswordSent
                                errorMessage = null
                            } else {
                                errorMessage = err ?: "Failed to send reset email"
                            }
                        }
                    }
                }
            ) {
                Text(strings.forgotPassword, style = MaterialTheme.typography.bodySmall)
            }
        }
    } else {
        Spacer(modifier = Modifier.height(14.dp))
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Primary Action Button (Sign In or Sign Up)
    Button(
        onClick = {
            if (email.isBlank() || password.isBlank()) {
                errorMessage = "Please fill in email and password"
                return@Button
            }
            if (selectedTab == 1 && password != confirmPassword) {
                errorMessage = "Passwords do not match"
                return@Button
            }
            isLoading = true
            errorMessage = null
            resetSuccessMessage = null

            if (selectedTab == 0) {
                onSignIn(email, password) { success, err ->
                    isLoading = false
                    if (!success) {
                        errorMessage = err ?: "Sign in failed"
                    }
                }
            } else {
                onSignUp(email, password, fullName) { success, err ->
                    isLoading = false
                    if (!success) {
                        errorMessage = err ?: "Sign up failed"
                    }
                }
            }
        },
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag(if (selectedTab == 0) "sign_in_button" else "sign_up_button"),
        shape = RoundedCornerShape(14.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = if (selectedTab == 0) strings.signIn else strings.signUp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "  or  ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Continue as Guest button
    OutlinedButton(
        onClick = {
            isLoading = true
            errorMessage = null
            onSignInAnonymously { success, err ->
                isLoading = false
                if (!success) {
                    errorMessage = err ?: "Guest sign in failed"
                }
            }
        },
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("guest_button"),
        shape = RoundedCornerShape(14.dp)
    ) {
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(strings.continueAsGuest, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(10.dp))

    TextButton(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(strings.cancel)
    }
}
