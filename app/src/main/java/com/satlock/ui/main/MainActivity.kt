package com.satlock.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.satlock.ui.adapter.UserAdapter
import com.satlock.usersyncapp.R
import com.satlock.usersyncapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Lista de Usuarios"
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter { user ->
            viewModel.toggleFavorite(user.id)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = userAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.checkNetworkStatus()
            viewModel.refreshUsers()
        }
    }

    private fun observeViewModel() {
        viewModel.allUsers.observe(this) { users ->
            userAdapter.submitList(users)
            updateEmptyState(users.isEmpty())
        }

        viewModel.filteredUsers.observe(this) { filteredUsers ->
            filteredUsers?.let {
                userAdapter.submitList(it)
                updateEmptyState(it.isEmpty())
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.isNetworkAvailable.observe(this) { isAvailable ->
            updateNetworkStatus(isAvailable)
        }

        viewModel.syncStatus.observe(this) { status ->
            when (status) {
                SyncStatus.SYNCING -> {
                    binding.tvSyncStatus.visibility = View.VISIBLE
                    binding.tvSyncStatus.text = "Sincronizando cambios..."
                    binding.tvSyncStatus.setBackgroundColor(getColor(R.color.orange))
                }
                SyncStatus.SYNCED -> {
                    binding.tvSyncStatus.visibility = View.VISIBLE
                    binding.tvSyncStatus.text = "Cambios sincronizados"
                    binding.tvSyncStatus.setBackgroundColor(getColor(R.color.green))
                    binding.tvSyncStatus.postDelayed({
                        binding.tvSyncStatus.visibility = View.GONE
                    }, 2000)
                }
                SyncStatus.PENDING -> {
                    binding.tvSyncStatus.visibility = View.VISIBLE
                    binding.tvSyncStatus.text = "Cambios pendientes"
                    binding.tvSyncStatus.setBackgroundColor(getColor(R.color.yellow))
                }
                SyncStatus.ERROR -> {
                    binding.tvSyncStatus.visibility = View.VISIBLE
                    binding.tvSyncStatus.text = "Error al sincronizar"
                    binding.tvSyncStatus.setBackgroundColor(getColor(R.color.red))
                    binding.tvSyncStatus.postDelayed({
                        binding.tvSyncStatus.visibility = View.GONE
                    }, 3000)
                }
                else -> {
                    binding.tvSyncStatus.visibility = View.GONE
                }
            }
        }
    }

    private fun updateNetworkStatus(isAvailable: Boolean) {
        if (!isAvailable) {
            binding.tvNetworkStatus.visibility = View.VISIBLE
            binding.tvNetworkStatus.text = "Modo offline"
        } else {
            binding.tvNetworkStatus.visibility = View.GONE
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE

            if (viewModel.isNetworkAvailable.value == false) {
                binding.tvEmptyState.text = "No hay datos almacenados.\nConecta a internet para cargar usuarios."
            } else {
                binding.tvEmptyState.text = "No se encontraron usuarios."
            }
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterUsers(newText ?: "")
                return true
            }
        })

        searchView.setOnCloseListener {
            viewModel.clearFilter()
            false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.refreshUsers()
                true
            }
            R.id.action_sync -> {
                if (viewModel.isNetworkAvailable.value == true) {
                    viewModel.syncPendingChanges()
                    Toast.makeText(this, "Iniciando sincronización...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkNetworkStatus()
    }
}