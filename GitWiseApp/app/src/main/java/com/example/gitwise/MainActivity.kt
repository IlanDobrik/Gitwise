package com.example.gitwise

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gitwise.gitmanager.GitManager
import com.example.gitwise.gitmanager.clone
import com.example.gitwise.ui.theme.GitwiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init(this)

        enableEdgeToEdge()
        setContent {
            GitwiseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Greeting(name = "Android")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            startActivity(Intent(this@MainActivity, TransactionListActivity::class.java))
                        }) {
                            Text("View Transactions")
                        }
                    }
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
        Greeting("Android")
    }
}
