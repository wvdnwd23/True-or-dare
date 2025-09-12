package com.wes.truthdare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Dialog to display emergency contacts and resources
 */
@Composable
fun EmergencyContactsDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Emergency Resources",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "If you or someone else needs help, don't hesitate to use these resources.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Emergency Services
                EmergencyContactItem(
                    icon = Icons.Default.Call,
                    title = "Emergency Services",
                    description = "Call 911 (US) or 112 (EU)",
                    buttonText = "Call"
                )
                
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Taxi Services
                EmergencyContactItem(
                    icon = Icons.Default.LocalTaxi,
                    title = "Taxi Services",
                    description = "Get a safe ride home",
                    buttonText = "Call Taxi"
                )
                
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Poison Control
                EmergencyContactItem(
                    icon = Icons.Default.Call,
                    title = "Poison Control",
                    description = "1-800-222-1222 (US)",
                    buttonText = "Call"
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun EmergencyContactItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    buttonText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 16.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        OutlinedButton(
            onClick = { /* Would implement actual calling functionality */ }
        ) {
            Text(buttonText)
        }
    }
}