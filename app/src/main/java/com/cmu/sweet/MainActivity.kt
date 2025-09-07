package com.cmu.sweet
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cmu.sweet.data.repository.UserRepository
import com.cmu.sweet.ui.theme.SweetTheme
import com.cmu.sweet.ui.navigation.AppNavGraph
import kotlin.getValue

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SweetTheme {
                AppNavGraph()
            }
        }
    }
}
