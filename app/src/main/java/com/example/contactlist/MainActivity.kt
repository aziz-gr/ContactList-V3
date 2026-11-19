package com.example.contactlist

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.contactlist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    @Suppress("unused")
    private val viewModel: ContactViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMain)

        binding.toolbarMain.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
            when (currentFragment) {
                is ListeContactsFragment -> {
                    updateTitle(getString(R.string.title_contact_list), showBack = false)
                }
                is DetailContactFragment -> {
                    updateTitle(getString(R.string.title_contact_detail), showBack = true)
                }
                is CarteContactFragment -> {
                    updateTitle(getString(R.string.title_carte), showBack = true)
                }
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, ListeContactsFragment())
                .commit()
        }
    }

    fun updateTitle(title: String, showBack: Boolean) {
        binding.toolbarMain.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(showBack)
        if (showBack) {
            binding.toolbarMain.setNavigationIcon(R.drawable.ic_arrow_back)
        } else {
            binding.toolbarMain.navigationIcon = null
        }
    }
}
