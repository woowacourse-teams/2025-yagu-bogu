package com.yagubogu.ui.home.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White

@Composable
fun CheckInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(100),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Primary500,
                contentColor = White,
            ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_marker_pin),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.home_check_in),
            style = PretendardBold20,
        )
    }
}

@Preview
@Composable
private fun CheckInButtonPreview() {
    CheckInButton(onClick = {})
}
