package com.example.gitwise

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gitwise.config.Config
import com.example.gitwise.config.getConfig
import com.example.gitwise.config.saveConfig
import com.example.gitwise.datatypes.Person
import com.example.gitwise.gitmanager.GitViewModel

@Composable
fun ConfigScreen(navController: NavController, viewModel: GitViewModel = viewModel()) {
    val context = LocalContext.current
    val config = remember { getConfig(context) }
    val repoMembers by viewModel.repoMembers.collectAsState()

    var name by remember { mutableStateOf(config.person?.name ?: "") }
    var branchName by remember { mutableStateOf(config.branchName) }
    var autoCommit by remember { mutableStateOf(config.commit) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
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
            DropdownTextField(
                label = "User Name",
                value = name,
                suggestions = repoMembers,
                onValueChange = { name = it }
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

                    saveConfig(context, newConfig)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save Configuration")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigScreenPreview() {
    com.example.gitwise.ui.theme.GitwiseTheme {
        ConfigScreen(navController = rememberNavController())
    }
}
