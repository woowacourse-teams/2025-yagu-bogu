package com.yagubogu.ui.dialog.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.yagubogu.ui.dialog.model.MEMBER_PROFILE_FIXTURE
import com.yagubogu.ui.dialog.model.MemberProfile
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.util.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDialog(
    onDismissRequest: () -> Unit,
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier =
            modifier
                .fillMaxWidth(0.9f)
                .background(Gray050, RoundedCornerShape(12.dp))
                .padding(20.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CancelButton(onClick = onDismissRequest)
                ProfileHeader(memberProfile = memberProfile)
                Spacer(modifier = Modifier.height(20.dp))
                ProfileContent(memberProfile = memberProfile)
            }
        },
    )
}

@Composable
private fun CancelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "닫기",
            tint = Gray500,
            modifier = Modifier.noRippleClickable(onClick = onClick),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileDialogPreview() {
    ProfileDialog(
        onDismissRequest = {},
        memberProfile = MEMBER_PROFILE_FIXTURE,
    )
}
