package com.moneycare.app.ui.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.GroupMember
import com.moneycare.app.ui.theme.LightGreen
import com.moneycare.app.ui.theme.PrimaryGreen
import com.moneycare.app.ui.theme.TextPrimary
import com.moneycare.app.ui.theme.TextSecondary

@Composable
fun GroupHeroSection(
    groupName: String,
    members: List<GroupMember>,
    onEditName: () -> Unit,
    onAddMember: () -> Unit,
    onMemberClick: (GroupMember) -> Unit,
    canManageGroup: Boolean,
    heroImageUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val heroBitmap = remember(heroImageUrl) {
        if (heroImageUrl.isNullOrBlank()) null
        else try {
            val cleanBase64 = if (heroImageUrl.contains(",")) heroImageUrl.substringAfter(",") else heroImageUrl
            val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFBEE3D8), LightGreen))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (heroBitmap != null) {
                Image(
                    bitmap = heroBitmap,
                    contentDescription = groupName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("🏝️ 🦖", style = MaterialTheme.typography.displaySmall)
            }
        }

        Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = groupName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (canManageGroup) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Chỉnh sửa tên nhóm",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onEditName)
                )
            }
        }

        Text(
            text = "${members.size} thành viên",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )

        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy((-10).dp)
        ) {
            members.take(4).forEach { member ->
                MemberAvatar(member = member, onClick = { onMemberClick(member) })
            }
            if (canManageGroup) AddMemberButton(onClick = onAddMember)
        }
    }
}

@Composable
private fun RowScope.MemberAvatar(
    member: GroupMember,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(LightGreen)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = member.name.take(1),
            color = PrimaryGreen,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AddMemberButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Add, contentDescription = "Thêm thành viên", tint = PrimaryGreen)
    }
}
