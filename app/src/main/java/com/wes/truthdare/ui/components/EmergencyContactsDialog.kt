package com.wes.truthdare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wes.truthdare.R
import android.content.Intent
import android.net.Uri

/**
 * Dialog for emergency contacts
 */
@Composable
fun EmergencyContactsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // Emergency contact data
    val emergencyContacts = listOf(
        EmergencyContact(
            name = "Emergency Services",
            number = "112",
            icon = Icons.Default.MedicalServices,
            description = "Call emergency services (police, ambulance, fire)"
        ),
        EmergencyContact(
            name = "Taxi Service",
            number = "0800-TAXI",
            icon = Icons.Default.LocalTaxi,
            description = "Call a taxi to get home safely"
        ),
        EmergencyContact(
            name = "Alcohol Helpline",
            number = "0900-1995",
            icon = Icons.Default.Call,
            description = "Get help with alcohol-related issues"
        )
    )
    
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
                // Header
                Text(
                    text = stringResource(R.string.emergency_contacts),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Need help? These contacts are available 24/7.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contacts list
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(emergencyContacts) { contact ->
                        EmergencyContactItem(
                            contact = contact,
                            onCallClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${contact.number}")
                                }
                                context.startActivity(intent)
                            }
                        )
                        
                        Divider()
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Close button
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

/**
 * Individual emergency contact item
 */
@Composable
fun EmergencyContactItem(
    contact: EmergencyContact,
    onCallClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = contact.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = contact.description,
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = contact.number,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        IconButton(onClick = onCallClick) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call ${contact.name}",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Data class for emergency contact
 */
data class EmergencyContact(
    val name: String,
    val number: String,
    val icon: ImageVector,
    val description: String
)