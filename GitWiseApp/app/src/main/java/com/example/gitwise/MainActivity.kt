package com.example.gitwise

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.example.gitwise.gitmanager.GitManager
import com.example.gitwise.gitmanager.clone
import com.example.gitwise.ui.theme.GitwiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context : Context = this;

        init(context)


        enableEdgeToEdge()
        setContent {
            GitwiseTheme {
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
                            startActivity(Intent(this@MainActivity, TransactionListActivity::class.java))
                        }) {
                            Text("View Transactions")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // New Config Activity Button
                        Button(modifier = Modifier.fillMaxWidth(),
                            onClick = {
                            startActivity(Intent(this@MainActivity, ConfigActivity::class.java))
                        }) {
                            Text("Configuration")
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(

                            onClick = { reset(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.height(36.dp) // Smaller height
                        ) {
                            Text("Reset")
                        }
                    })
                }
            }
        }
    }
}


fun init(context: Context) {
    Log.i(TAG, "initializing")
    try{
        clone(context.filesDir)
    } catch (e: Exception) {
        Log.e(TAG, "Error initializing", e)
    }
    Log.i(TAG, "initialized")
}

fun reset(context: Context) {
    Log.i(TAG, "resetting")
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GitwiseTheme {
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
                Button(modifier = Modifier.fillMaxWidth(),
                    onClick = {

                }) {
                    Text("View Transactions")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // New Config Activity Button
                Button(modifier = Modifier.fillMaxWidth(),
                    onClick = {

                }) {
                    Text("Configuration")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {  },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.height(36.dp),

                ) {
                    Text("Reset")
                }
            }
            )
        }
    }
}
