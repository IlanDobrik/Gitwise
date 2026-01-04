package com.example.gitwise

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import com.example.gitwise.logger.TAG
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gitwise.config.clearConfig
import com.example.gitwise.gitmanager.getRepoBase
import com.example.gitwise.ui.theme.GitwiseTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context : Context = this
        // TODO remove
        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }


        enableEdgeToEdge()
        setContent {
            GitwiseTheme {
                GreetingScreen(context,
                {
                    startActivity(Intent(this@MainActivity, TransactionListActivity::class.java))
                },
                {
                    startActivity(Intent(this@MainActivity, ConfigActivity::class.java))
                },
                {
                    reset(context)
                })
            }
        }
    }
}


fun reset(context: Context) {
    getRepoBase(context).deleteRecursively()
    clearConfig(context)
}

@Composable
fun GreetingScreen(context: Context?,
                   viewTransactionOnClick: () -> Unit = {},
                   newConfigOnClick: () -> Unit = {},
                   resetOnClick: () -> Unit = {}) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        // Added padding 16.dp to the Column for better spacing from edges
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            {
                Spacer(modifier = Modifier.height(16.dp))

                // Existing Transaction Button
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewTransactionOnClick()

                    }) {
                    Text("View Transactions")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // New Config Activity Button
                Button(modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        newConfigOnClick()
                    }) {
                    Text("Configuration")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { resetOnClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.height(36.dp) // Smaller height
                ) {
                    Text("Reset")
                }
        })
    }

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GitwiseTheme {
        GreetingScreen(null, {}, {}, {})
    }
}

