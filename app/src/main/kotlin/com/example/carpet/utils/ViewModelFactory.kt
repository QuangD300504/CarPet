package com.example.carpet.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Generic ViewModelFactory utility to reduce boilerplate code.
 * 
 * Usage example:
 * ```
 * class HomeViewModelFactory(private val repository: ServiceRepository) :
 *     ViewModelFactory({ HomeViewModel(repository) }, HomeViewModel::class.java)
 * ```
 */
open class ViewModelFactory<T : ViewModel>(
    private val create: () -> T,
    private val viewModelClass: Class<T>
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        if (modelClass.isAssignableFrom(viewModelClass)) {
            return create() as VM
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

