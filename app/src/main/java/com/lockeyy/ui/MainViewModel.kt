package com.lockeyy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockeyy.data.dao.LockedAppDao
import com.lockeyy.data.model.LockedAppEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val lockedAppDao: LockedAppDao,
    private val pinManager: com.lockeyy.core.PinManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Source of truth for locked apps
    private val lockedPackagesFlow = lockedAppDao.getAllLockedApps()
    
    // Check if PIN is set
    // This is a suspend function in PinManager, we can expose a Flow or just check on init
    // For now let's just expose a function
    
    fun setPin(newPin: String) {
        viewModelScope.launch {
            pinManager.savePin(newPin)
        }
    }

    // Installed apps (cached to avoid repeated heavy PM calls, ideally use a Repository)
    private val _installedApps = MutableStateFlow<List<com.lockeyy.ui.model.AppItem>>(emptyList())

    val appListState = combine(
        _installedApps,
        lockedPackagesFlow,
        _searchQuery
    ) { apps, lockedEntities, query ->
        val lockedSet = lockedEntities.map { it.packageName }.toSet()
        val mappedApps = apps.map { app ->
            app.copy(isLocked = lockedSet.contains(app.packageName))
        }
        
        if (query.isBlank()) {
            mappedApps.sortedBy { it.label }
        } else {
            mappedApps.filter { 
                it.label.contains(query, ignoreCase = true) || 
                it.packageName.contains(query, ignoreCase = true) 
            }.sortedBy { it.label }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(0)
            val items = packages.mapNotNull { pkg ->
                // Filter out some system apps if needed, generally we want launchable apps
                val intent = pm.getLaunchIntentForPackage(pkg.packageName)
                if (intent != null) {
                    val label = pkg.applicationInfo.loadLabel(pm).toString()
                    val icon = pkg.applicationInfo.loadIcon(pm)
                    com.lockeyy.ui.model.AppItem(
                        packageName = pkg.packageName,
                        label = label,
                        icon = icon,
                        isLocked = false // Will be updated by combine
                    )
                } else {
                    null
                }
            }
            _installedApps.value = items
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleAppLock(app: com.lockeyy.ui.model.AppItem) {
        viewModelScope.launch {
            if (app.isLocked) {
                lockedAppDao.unlockApp(app.packageName)
            } else {
                lockedAppDao.lockApp(LockedAppEntity(packageName = app.packageName))
            }
        }
    }
}
