package com.cmu.sweet.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cmu.sweet.data.local.entities.Establishment
import com.cmu.sweet.data.repository.EstablishmentRepository
import com.cmu.sweet.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class RankedEstablishment(
    val establishment: Establishment,
    val averageRating: Float
)

class LeaderboardViewModel(
    application: Application,
    private val establishmentRepo: EstablishmentRepository,
    private val reviewRepo: ReviewRepository
) : AndroidViewModel(application) {

    private val _rankedEstablishments = MutableStateFlow<List<RankedEstablishment>>(emptyList())
    val rankedEstablishments: StateFlow<List<RankedEstablishment>> =
        _rankedEstablishments.asStateFlow()

    init {
        fetchRankedEstablishments()
    }

    private fun fetchRankedEstablishments() {
        viewModelScope.launch {
            try {
                val establishments = establishmentRepo.fetchAll()

                val rankedList = establishments.map { est ->
                    val reviewsResult = reviewRepo.getByEstablishment(est.id)
                    val avg = if (reviewsResult.isSuccess) {
                        val reviews = reviewsResult.getOrNull().orEmpty()
                        if (reviews.isNotEmpty()) reviews.map { it.rating }.average()
                            .toFloat() else 0f
                    } else 0f

                    RankedEstablishment(est, avg)
                }
                    .sortedByDescending { it.averageRating }

                _rankedEstablishments.value = rankedList

            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch establishments or calculate ratings")
            }
        }
    }

    class Factory(
        private val application: Application,
        private val establishmentRepo: EstablishmentRepository,
        private val reviewRepo: ReviewRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LeaderboardViewModel::class.java)) {
                return LeaderboardViewModel(application, establishmentRepo, reviewRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}