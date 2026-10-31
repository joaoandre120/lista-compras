package com.joaoandre120.listadecompras.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joaoandre120.listadecompras.data.repository.ConnectivityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Estado global do app, compartilhado pela barra superior.
 *
 * Estende `AndroidViewModel` porque o monitor de conexão precisa de um contexto;
 * o `Application` é seguro aqui, pois vive tanto quanto o app (sem vazar Activity).
 *
 * @param application aplicação, usada para criar o [ConnectivityRepository].
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val connectivityRepository = ConnectivityRepository(application)

    /**
     * `true` com internet. Começa em `true` para o ícone de offline não piscar
     * nos primeiros milissegundos, antes da primeira leitura real.
     * Deixa de observar 5 s depois que o app sai de tela.
     */
    val isOnline: StateFlow<Boolean> = connectivityRepository.observeOnline()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            true
        )

}
