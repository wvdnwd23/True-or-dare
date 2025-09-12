package com.wes.truthdare.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wes.truthdare.R

/**
 * Card displaying emergency contact options
 */
@Composable
fun EmergencyContactsCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = stringResource(R.string.emergency_contacts),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "If you or someone needs help, don't hesitate to use these options:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Emergency Services Button
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:112") // European emergency number
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text("Emergency Services (112)")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Taxi Button
            OutlinedButton(
                onClick = {
                    // Open maps app with taxi search
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("geo:0,0?q=taxi")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocalTaxi,
                    contentDescription = null
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text("Find a Taxi")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Ride Service Button
            OutlinedButton(
                onClick = {
                    // Try to open Uber app, or fallback to website
                    val uberPackageName = "com.ubercab"
                    val intent = context.packageManager.getLaunchIntentForPackage(uberPackageName)
                    
                    if (intent != null) {
                        // Uber app is installed, launch it
                        context.startActivity(intent)
                    } else {
                        // Uber app not installed, open website
                        val webIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://m.uber.com")
                        }
                        context.startActivity(webIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocalTaxi,
                    contentDescription = null
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text("Ride Service (Uber)")
            }
        }
    }
}