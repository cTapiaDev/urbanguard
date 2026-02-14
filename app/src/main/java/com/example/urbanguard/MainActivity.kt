package com.example.urbanguard

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.urbanguard.core.di.IoDispatcher
import com.example.urbanguard.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Retrofit
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    // @Inject lateinit var retrofit: Retrofit
    // @Inject @IoDispatcher lateinit var ioDispatcher: CoroutineDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Timber.d("Hilt: Retrofit Base URL es ${retrofit.baseUrl()}")
        // Timber.d("Hilt: IO Dispatcher es $ioDispatcher")


        // Configuración del NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Vincular BottomNav con la Navigation
        binding.bottomNav.setupWithNavController(navController)

        // Proceso de activación de la toolbar
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController)

        // Lógica de visibilidad de la toolbar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.createReportFragment -> {
                    binding.bottomNav.visibility = View.GONE
                    supportActionBar?.show()
                }
                R.id.homeFragment -> {
                    binding.bottomNav.visibility = View.VISIBLE
                    supportActionBar?.hide()
                }
                else -> {
                    binding.bottomNav.visibility = View.VISIBLE
                    supportActionBar?.show()
                }
            }
        }
    }

    // Le da el funcionamiento a la flecha de la toolbar (ir atrás)
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}