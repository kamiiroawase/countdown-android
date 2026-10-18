package io.github.kaimiroawase.countdown.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import io.github.kaimiroawase.countdown.R

fun showToast(
    context: Context,
    message: String?,
) {
    Toast
        .makeText(
            context,
            message?.ifBlank { null } ?: context.getString(R.string.toast_unknown_error),
            Toast.LENGTH_SHORT,
        ).show()
}

fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(
        interactionSource = null,
        indication = null,
        onClick = onClick,
    )

fun Modifier.selectableNoRipple(
    selected: Boolean,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier =
    this.selectable(
        selected = selected,
        interactionSource = null,
        indication = null,
        role = role,
        onClick = onClick,
    )
