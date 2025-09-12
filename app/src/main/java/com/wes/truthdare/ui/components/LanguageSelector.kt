package com.wes.truthdare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.wes.truthdare.core.data.Language

/**
 * A component for selecting the app language
 */
@Composable
fun LanguageSelector(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup()
            .padding(16.dp)
    ) {
        Text(
            text = "Language / Taal",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Language.values().forEach { language ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (language == selectedLanguage),
                        onClick = { onLanguageSelected(language) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (language == selectedLanguage),
                    onClick = null // null because we're handling the click on the row
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = when (language) {
                            Language.ENGLISH -> "English"
                            Language.DUTCH -> "Nederlands"
                            Language.GERMAN -> "Deutsch"
                            Language.FRENCH -> "Français"
                            Language.SPANISH -> "Español"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Text(
                        text = when (language) {
                            Language.ENGLISH -> "English"
                            Language.DUTCH -> "Dutch"
                            Language.GERMAN -> "German"
                            Language.FRENCH -> "French"
                            Language.SPANISH -> "Spanish"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}