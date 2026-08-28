package com.example.pelarikalcer.ui.screens.run

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pelarikalcer.data.local.dao.RunDao
import com.example.pelarikalcer.data.local.dao.UserDao
import com.example.pelarikalcer.data.local.entity.RunEntity
import com.google.android.gms.location.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

data class RunState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val durationSeconds: Int = 0,
    val distanceKm: Double = 0.0,
    val paceMinPerKm: Double = 0.0,
    val caloriesBurned: Int = 0,
    val startTime: Long = 0L,
    val savedRunId: Long? = null,
    val isFinished: Boolean = false,
    // Elevation data
    val elevationGainM: Double = 0.0,
    val elevationLossM: Double = 0.0,
    val currentAltitudeM: Double = 0.0,
    val maxAltitudeM: Double = 0.0,
    // Coordinates for OSM map
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val routePoints: List<org.osmdroid.util.GeoPoint> = emptyList()
)

class RunViewModel(
    private val runDao: RunDao,
    private val userDao: UserDao,
    private val userId: Int,
    private val userWeightKg: Double = 65.0,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(RunState())
    val state: StateFlow<RunState> = _state

    private var timerJob: Job? = null
    private var startTimestamp = 0L

    // GPS tracking
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private var lastLocation: Location? = null
    private var lastAltitude: Double? = null
    private var accumulatedDistanceKm: Double = 0.0
    private var accumulatedElevationGain: Double = 0.0
    private var accumulatedElevationLoss: Double = 0.0
    private var maxAltitude: Double = Double.MIN_VALUE
    private val recordedRoutePoints = mutableListOf<org.osmdroid.util.GeoPoint>()

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 3000L
    ).apply {
        setMinUpdateDistanceMeters(5f)
        setGranularity(Granularity.GRANULARITY_FINE)
        setWaitForAccurateLocation(false)
    }.build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            if (!_state.value.isRunning || _state.value.isPaused) return

            val point = org.osmdroid.util.GeoPoint(location.latitude, location.longitude)
            recordedRoutePoints.add(point)

            // Distance
            lastLocation?.let { prev ->
                val dist = prev.distanceTo(location) / 1000.0
                if (dist > 0.003) { // Filter noise < 3m
                    accumulatedDistanceKm += dist
                }
            }
            lastLocation = location

            // Elevation
            if (location.hasAltitude()) {
                val alt = location.altitude
                lastAltitude?.let { prev ->
                    val diff = alt - prev
                    if (abs(diff) > 0.5) { // Filter noise < 0.5m
                        if (diff > 0) accumulatedElevationGain += diff
                        else accumulatedElevationLoss += abs(diff)
                    }
                }
                lastAltitude = alt
                if (alt > maxAltitude) maxAltitude = alt
            }

            updateMetrics(location)
        }
    }

    private fun updateMetrics(location: Location? = null) {
        val dist = accumulatedDistanceKm
        val secs = _state.value.durationSeconds
        val pace = if (secs > 0 && dist > 0) (secs / 60.0) / dist else 0.0
        val calories = calculateCalories(dist, userWeightKg)
        _state.value = _state.value.copy(
            distanceKm = dist,
            paceMinPerKm = pace,
            caloriesBurned = calories,
            elevationGainM = accumulatedElevationGain,
            elevationLossM = accumulatedElevationLoss,
            maxAltitudeM = if (maxAltitude == Double.MIN_VALUE) 0.0 else maxAltitude,
            currentAltitudeM = location?.altitude ?: _state.value.currentAltitudeM,
            currentLatitude = location?.latitude ?: _state.value.currentLatitude,
            currentLongitude = location?.longitude ?: _state.value.currentLongitude,
            routePoints = recordedRoutePoints.toList()
        )
    }

    @SuppressLint("MissingPermission")
    fun startRun() {
        startTimestamp = System.currentTimeMillis()
        accumulatedDistanceKm = 0.0
        accumulatedElevationGain = 0.0
        accumulatedElevationLoss = 0.0
        maxAltitude = Double.MIN_VALUE
        lastLocation = null
        lastAltitude = null
        recordedRoutePoints.clear()

        _state.value = RunState(
            isRunning = true,
            isPaused = false,
            startTime = startTimestamp
        )
        startTimer()

        // Start GPS if permission granted
        if (hasLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    fun pauseResume() {
        if (_state.value.isRunning && !_state.value.isPaused) {
            timerJob?.cancel()
            _state.value = _state.value.copy(isPaused = true)
            fusedLocationClient.removeLocationUpdates(locationCallback)
            lastLocation = null // Reset last location to avoid jump on resume
        } else if (_state.value.isPaused) {
            _state.value = _state.value.copy(isPaused = false)
            startTimer()
            if (hasLocationPermission()) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        }
    }

    fun finishRun() {
        timerJob?.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        val currentState = _state.value
        if (currentState.distanceKm < 0.01) {
            _state.value = currentState.copy(isRunning = false, isFinished = true)
            return
        }
        viewModelScope.launch {
            val run = RunEntity(
                userId = userId,
                distanceKm = currentState.distanceKm,
                durationSeconds = currentState.durationSeconds,
                avgPaceMinutesPerKm = currentState.paceMinPerKm,
                caloriesBurned = currentState.caloriesBurned,
                routeGeometry = null,
                startTime = currentState.startTime,
                endTime = System.currentTimeMillis(),
                elevationGainM = currentState.elevationGainM,
                elevationLossM = currentState.elevationLossM,
                maxAltitudeM = currentState.maxAltitudeM
            )
            val runId = runDao.insertRun(run)
            _state.value = currentState.copy(isRunning = false, savedRunId = runId, isFinished = true)
        }
    }

    // For non-GPS simulation (indoor/testing)
    fun simulateDistance(addKm: Double) {
        accumulatedDistanceKm += addKm
        updateMetrics()
    }

    private fun calculateCalories(distanceKm: Double, weightKg: Double): Int {
        return (distanceKm * weightKg * 1.036).toInt()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                if (!_state.value.isPaused) {
                    val newSecs = _state.value.durationSeconds + 1
                    val dist = _state.value.distanceKm
                    val pace = if (dist > 0) (newSecs / 60.0) / dist else 0.0
                    _state.value = _state.value.copy(
                        durationSeconds = newSecs,
                        paceMinPerKm = pace
                    )
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}

class RunViewModelFactory(
    private val runDao: RunDao,
    private val userDao: UserDao,
    private val userId: Int,
    private val userWeightKg: Double,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RunViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RunViewModel(runDao, userDao, userId, userWeightKg, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
