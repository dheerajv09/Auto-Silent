package com.example.autosilent.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.autosilent.R
import com.example.autosilent.util.Permissions
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.findNavController

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(Permissions.hasLocationPermission(this)){
            findNavController(R.id.navHostFragment).navigate(R.id.action_permissionFragment_to_showPlacesFragment)
        }

    }
}