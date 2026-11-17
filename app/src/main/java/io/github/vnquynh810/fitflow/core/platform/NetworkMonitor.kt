package io.github.vnquynh810.fitflow.core.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class NetworkSnapshot(
    val isConnected: Boolean,
    val isWifi: Boolean,
    val isCellular: Boolean,
)

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun currentSnapshot(): NetworkSnapshot = runCatching {
        val activeNetwork = connectivityManager.activeNetwork
            ?: return NetworkSnapshot(isConnected = false, isWifi = false, isCellular = false)
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return NetworkSnapshot(isConnected = false, isWifi = false, isCellular = false)
        NetworkSnapshot(
            isConnected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
            isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
            isCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
        )
    }.getOrDefault(NetworkSnapshot(isConnected = false, isWifi = false, isCellular = false))

    val isOnlineFlow: Flow<Boolean> = callbackFlow {
        trySend(currentSnapshot().isConnected)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(currentSnapshot().isConnected)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                trySend(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        runCatching { connectivityManager.registerNetworkCallback(request, callback) }
        awaitClose {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged()
}
