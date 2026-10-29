package com.joaoandre120.listadecompras.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/**
 * ViewModel das listas de um grupo (provisório, evolui na fatia 2).
 *
 * @property listId Id do grupo recebido pela key de navegação.
 */
class ListItemsViewModel(val listId: String) : ViewModel() {

    companion object {
        /**
         * Cria a factory que injeta [groupId] no ViewModel.
         *
         * @param groupId Id do grupo exibido.
         * @return Factory para uso em `viewModel(factory = ...)`.
         */
        fun factory(groupId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer { ListItemsViewModel(groupId) }
        }
    }
}