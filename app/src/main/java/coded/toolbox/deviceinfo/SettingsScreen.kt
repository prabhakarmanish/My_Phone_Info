package coded.toolbox.deviceinfo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.ads.AdSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    adBannerManager: AdBannerManager
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_arrow_back_ios_new_24),
                            contentDescription = "Back Icon",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp) // Vertical spacing between items
            ) {
                item {
                    SettingItem(
                        text = "Share with your friends",
                        icon = painterResource(id = R.drawable.outline_ios_share_24),
                        onClick = {
                            shareApp(context = context)
                        } // Empty clickable block
                        , topRounded = true
                    )
                }

                item {
                    SettingItem(
                        text = "Rate Us",
                        icon = painterResource(id = R.drawable.rounded_star_rate_24),
                        onClick = {
                            rateUs(context)
                        } // Empty clickable block
                    )
                }

                item {
                    SettingItem(
                        text = "Terms & Conditions",
                        icon = painterResource(id = R.drawable.termandconditions),
                        endIcon = painterResource(id = R.drawable.rounded_link_24),
                        onClick = {
                            openUrl(
                                "https://mydeviceinfoprivacypolicy.blogspot.com/2025/01/my-device-info-terms-conditions.html",
                                context
                            )
                        } // Empty clickable block
                    )
                }

                item {
                    SettingItem(
                        text = "Privacy Policy",
                        icon = painterResource(id = R.drawable.outline_privacy_tip_24),
                        endIcon = painterResource(id = R.drawable.rounded_link_24),
                        onClick = {
                            openUrl(
                                "https://mydeviceinfoprivacypolicy.blogspot.com/2025/01/my-device-info-privacy-policy.html",
                                context
                            )
                        } // Empty clickable block
                    )
                }

                item {
                    SettingItem(
                        text = "App Version:",
                        icon = painterResource(id = R.drawable.rounded_conversion_path_24),
                        endText = "1.0.5",
                        onClick = {
                            showAppVersion(context)
                        } // Empty clickable block
                        , bottomRounded = true
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(26.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BannerAd(adBannerManager, MEDIUM_RECTANGLE_AD_ID, AdSize.MEDIUM_RECTANGLE)
                    }
                }

            }
        },
        containerColor = MaterialTheme.colorScheme.background
    )
}

@Composable
fun SettingItem(
    text: String,
    icon: Painter,
    endIcon: Painter? = null,
    endText: String? = null,
    onClick: () -> Unit, // Empty clickable block
    topRounded: Boolean = false, // Flag to round top corners
    bottomRounded: Boolean = false // Flag to round bottom corners
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Define the shape based on top and bottom rounded flags
    val shape = when {
        topRounded && bottomRounded -> RoundedCornerShape(16.dp)
        topRounded -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        bottomRounded -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(0.dp) // No rounded corners if none specified
    }

    // Apply clickable modifier with ripple and clipping
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape) // Ensure the content inside the Box respects the rounded corners
            .clickable(
                onClick = onClick,
                indication = rememberRipple(bounded = true),
                interactionSource = interactionSource
            )
            .padding(vertical = 1.dp) // Vertical spacing between items
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = text, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.weight(1f))

                endIcon?.let {
                    Icon(
                        painter = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                endText?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}


// Function to open share intent
private fun shareApp(context: Context) {
    val appPackageName = context.packageName // Get the app's package name
    val appLink =
        "https://play.google.com/store/apps/details?id=$appPackageName" // Create the Play Store link

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Check out this app: $appLink") // Add the app link to the text
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null) // Show a chooser for sharing options
    context.startActivity(shareIntent) // Start the sharing activity
}


// Function to navigate to Play Store
private fun rateUs(context: Context) {
    val appPackageName = context.packageName
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
        )
    } catch (e: ActivityNotFoundException) {
        // If the Play Store app is not installed, open in a browser
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
        )
    }
}

// Function to open a URL
private fun openUrl(url: String, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

// Function to show app version (e.g., via Toast)
private fun showAppVersion(context: Context) {
    val appPackageName = context.packageName
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
        )
    } catch (e: ActivityNotFoundException) {
        // If the Play Store app is not installed, open in a browser
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
        )
    }
    // Toast.makeText(context, "App Version: 1.0.0", Toast.LENGTH_SHORT).show()
}