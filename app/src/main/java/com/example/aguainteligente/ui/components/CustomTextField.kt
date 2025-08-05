package com.example.aguainteligente.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.aguainteligente.ui.theme.BlueElectric

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
       value = value,
       onValueChange = onValueChange,
       modifier = modifier,
       label = label,
       leadingIcon = leadingIcon,
       visualTransformation = visualTransformation,
       shape = RoundedCornerShape(12.dp),
       colors = TextFieldDefaults.colors(
           focusedIndicatorColor = BlueElectric,
           unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
           cursorColor = BlueElectric,
           focusedLabelColor = BlueElectric,
           unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
           focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
       )
    )
}