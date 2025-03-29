package otus.homework.customview.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import otus.homework.customview.CustomViewApp
import otus.homework.customview.domain.GenerateNewSpendsUseCase
import otus.homework.customview.domain.GetSpendsUseCase

class ViewModel(
    getSpendsUseCase: GetSpendsUseCase,
    private val generateNewSpendsUseCase: GenerateNewSpendsUseCase,
): ViewModel() {

    private val _spends = getSpendsUseCase.gesSpends()
    val spends = _spends

    fun generateNewSpends() = generateNewSpendsUseCase.generateNewSpends()

    companion object {

        val Factory = object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY]) as CustomViewApp
                return ViewModel(
                    getSpendsUseCase = GetSpendsUseCase(application.repository),
                    generateNewSpendsUseCase = GenerateNewSpendsUseCase(application.repository)
                ) as T
            }
        }
    }
}