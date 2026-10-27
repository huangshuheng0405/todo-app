package com.huangshuheng.todo.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huangshuheng.todo.R
import com.huangshuheng.todo.auth.AuthValidation
import com.huangshuheng.todo.ui.icons.TodoIcons
import com.huangshuheng.todo.ui.theme.TodoTheme

/**
 * 登录 / 注册页。视觉上刻意和主界面同一套语言：
 * 品牌红、细分割线、无边框输入。
 */
@Composable
fun LoginScreen(vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    val palette = TodoTheme.palette
    val keyboard = LocalSoftwareKeyboardController.current
    val passwordFocus = remember { FocusRequester() }
    var passwordVisible by remember { mutableStateOf(false) }

    fun submit() {
        keyboard?.hide()
        vm.submit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(palette.brand),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = TodoIcons.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.login_brand),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(
                when (state.mode) {
                    AuthMode.SIGN_IN -> R.string.login_subtitle_signin
                    AuthMode.SIGN_UP -> R.string.login_subtitle_signup
                }
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary
        )

        Spacer(modifier = Modifier.height(34.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            AuthMode.entries.forEach { mode ->
                ModeTab(
                    label = stringResource(mode.labelRes),
                    selected = state.mode == mode,
                    onClick = { vm.switchMode(mode) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AuthField(
            label = stringResource(R.string.login_email),
            value = state.email,
            onValueChange = vm::onEmailChange,
            placeholder = stringResource(R.string.login_email_hint),
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onImeAction = { passwordFocus.requestFocus() },
            error = state.emailError
        )

        Spacer(modifier = Modifier.height(18.dp))

        AuthField(
            label = stringResource(R.string.login_password),
            value = state.password,
            onValueChange = vm::onPasswordChange,
            placeholder = stringResource(
                R.string.login_password_hint,
                AuthValidation.MIN_PASSWORD_LENGTH
            ),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onImeAction = { submit() },
            error = state.passwordError,
            modifier = Modifier.focusRequester(passwordFocus),
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailing = {
                Text(
                    text = stringResource(
                        if (passwordVisible) R.string.login_hide_password
                        else R.string.login_show_password
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.brand,
                    modifier = Modifier
                        .clickable { passwordVisible = !passwordVisible }
                        .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                )
            }
        )

        state.error?.let { messageRes ->
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodySmall,
                color = palette.priority1
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (state.canSubmit) palette.brand else palette.brand.copy(alpha = 0.35f)
                )
                .clickable(enabled = state.canSubmit) { submit() },
            contentAlignment = Alignment.Center
        ) {
            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(state.mode.submitLabelRes),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.login_footer_hint),
            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 16.sp),
            color = palette.textTertiary
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ModeTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val palette = TodoTheme.palette
    Column(
        modifier = Modifier
            // 用 Max 取文字的自然宽度；Min 会让中文一个字就换行，标签变竖排
            .width(IntrinsicSize.Max)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            softWrap = false,
            color = if (selected) palette.textPrimary else palette.textTertiary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (selected) palette.brand else Color.Transparent)
        )
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    error: Int?,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null
) {
    val palette = TodoTheme.palette
    val hasError = error != null

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (hasError) palette.priority1 else palette.textSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.textTertiary
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = palette.textPrimary),
                    cursorBrush = SolidColor(palette.brand),
                    visualTransformation = visualTransformation,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onImeAction() },
                        onDone = { onImeAction() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            trailing?.invoke()
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(if (hasError) palette.priority1 else palette.outline)
        )
        if (hasError) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(error),
                style = MaterialTheme.typography.labelSmall,
                color = palette.priority1
            )
        }
    }
}
