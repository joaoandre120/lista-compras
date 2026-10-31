package com.joaoandre120.listadecompras.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Informa se o aparelho tem uma conexão com a internet.
 *
 * Reflete a rede do aparelho, não o estado do Firestore: o Firestore continua
 * funcionando offline com o cache local e sincroniza quando a rede volta.
 *
 * @param context contexto usado para obter o `ConnectivityManager`.
 */
class ConnectivityRepository(context: Context) {

    private val connectivityManager =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)

    /**
     * Observa em tempo real se há internet validada.
     *
     * Emite o estado atual ao começar, porque o callback só dispara em
     * *mudanças*: sem rede desde o início, ele nunca chamaria nada.
     *
     * @return fluxo de `true` (online) e `false` (offline), sem repetições seguidas.
     */
    fun observeOnline(): Flow<Boolean> = callbackFlow {
        trySend(isOnlineNow())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(caps.hasInternet())
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    /** @return `true` se a rede ativa agora tem internet validada. */
    private fun isOnlineNow(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasInternet()
    }

    private fun NetworkCapabilities.hasInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

}
