package com.moneycare.app.ui.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.moneycare.app.ui.components.DecorativeGroundStrip
import com.moneycare.app.ui.components.PrimaryButton
import com.moneycare.app.ui.components.SecondaryButton
import com.moneycare.app.ui.components.SocialLoginRow
import com.moneycare.app.ui.components.SocialProvider
import com.moneycare.app.ui.components.StaggerItem
import com.moneycare.app.ui.components.WelcomeIllustration
import com.moneycare.app.ui.theme.MintPrimary
import com.moneycare.app.ui.theme.MoneyCareSpacing

private const val SOCIAL_AUTH_ENABLED = false

/**
 * First screen the user sees — the app's welcome / landing page.
 */
@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onSocialLoginClick: (SocialProvider) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MoneyCareSpacing.L),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(MoneyCareSpacing.XL))

        StaggerItem(index = 0) {
            WelcomeHeader()
        }

        Spacer(Modifier.height(MoneyCareSpacing.L))

        StaggerItem(index = 1) {
            WelcomeIllustration()
        }

        Spacer(Modifier.height(MoneyCareSpacing.L))

        StaggerItem(index = 2) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MoneyCareSpacing.S)
            ) {
                PrimaryButton(
                    text = "Đăng nhập",
                    onClick = onLoginClick,
                    leadingIcon = Icons.Filled.Lock
                )
                SecondaryButton(
                    text = "Đăng ký tài khoản",
                    onClick = onRegisterClick,
                    leadingIcon = Icons.Filled.Person
                )
            }
        }

        if (SOCIAL_AUTH_ENABLED) {
            Spacer(Modifier.height(MoneyCareSpacing.L))

            StaggerItem(index = 3) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hoặc đăng nhập với",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(MoneyCareSpacing.M))
                    SocialLoginRow(
                        onGoogleClick = { onSocialLoginClick(SocialProvider.GOOGLE) },
                        onFacebookClick = { onSocialLoginClick(SocialProvider.FACEBOOK) },
                        onAppleClick = { onSocialLoginClick(SocialProvider.APPLE) }
                    )
                }
            }
        }

        Spacer(Modifier.height(MoneyCareSpacing.L))

        DecorativeGroundStrip()
        
        Spacer(Modifier.height(MoneyCareSpacing.L))
    }
}

@Composable
private fun WelcomeHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Chào mừng bạn đến với",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(MoneyCareSpacing.XXS))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = MintPrimary, fontWeight = FontWeight.Bold, fontSize = 26.sp)) {
                    append("MoneyCare ")
                }
                withStyle(SpanStyle(fontSize = 22.sp)) {
                    append("❤\uFE0F")
                }
            },
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(MoneyCareSpacing.XS))
        Text(
            text = "Quản lý chi tiêu dễ dàng, cuộc sống nhẹ nhàng hơn 🌱",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
