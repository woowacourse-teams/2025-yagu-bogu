package com.yagubogu.ui.home.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.home_check_in
import yagubogu.composeapp.generated.resources.ic_marker_pin

@Composable
fun CheckInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Primary500,
                contentColor = White,
            ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_marker_pin),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.home_check_in),
            style = PretendardBold20,
        )
    }
}

@Preview
@Composable
private fun CheckInButtonPreview() {
    CheckInButton(onClick = {})
}
