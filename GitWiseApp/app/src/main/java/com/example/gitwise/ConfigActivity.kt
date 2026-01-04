package com.example.gitwise

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gitwise.config.Config
import com.example.gitwise.config.getConfig
import com.example.gitwise.config.saveConfig
import com.example.gitwise.datatypes.Person
import com.example.gitwise.ui.theme.GitwiseTheme

class ConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context : Context = this
        val config = getConfig(context)

        enableEdgeToEdge()
        setContent {
            GitwiseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        ConfigScreen(context, config)
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigScreen(context: Context?, config: Config) {
    var name by remember { mutableStateOf(config.person?.name ?: "") }
    var branchName by remember { mutableStateOf(config.branchName) }
    var autoCommit by remember { mutableStateOf(config.commit) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Configuration Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("User Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Branch Field
        OutlinedTextField(
            value = branchName ?: "",
            onValueChange = { branchName = it },
            label = { Text("Default Branch") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Auto Commit Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Enable Auto-Push",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Automatically push changes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = autoCommit,
                onCheckedChange = { autoCommit = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                val newPerson = Person(name = name)
                val newConfig = Config(
                    person = newPerson,
                    commit = autoCommit,
                    branchName = branchName
                )

                context?.let {
                    saveConfig(context, newConfig)
                    Toast.makeText(context, "Settings Saved", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Save Configuration")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigPreview() {
    GitwiseTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                ConfigScreen(null, Config(null, false, null))
            }
        }
    }
}
