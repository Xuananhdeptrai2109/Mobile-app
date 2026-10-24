package com.moneycare.app.ui.group.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import com.moneycare.app.MoneyCareApp
import com.moneycare.app.data.repository.RealGroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateGroupUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

data class MemberUi(
    val id: String,
    val name: String,
    val avatarColor: Color
)

class CreateGroupViewModel(
    private val repository: RealGroupRepository = MoneyCareApp.instance.container.groupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    fun createGroup(
        name: String,
        description: String?,
        budget: Long?,
        members: List<MemberUi>,
        imageBase64: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.createGroup(
                    name = name,
                    description = description,
                    budget = budget,
                    imageBase64 = imageBase64,
                    memberNames = members.filterNot { it.id == "current" }.map { it.name }
                )
                
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                e.printStackTrace()
                val message = if (e is retrofit2.HttpException) {
                    try {
                        val errorBody = e.response()?.errorBody()?.string()
                        val gson = com.google.gson.Gson()
                        val errorMap = gson.fromJson(errorBody, Map::class.java)
                        errorMap["message"]?.toString() ?: "Lỗi kết nối Backend (400)"
                    } catch (parseEx: Exception) {
                        "Dữ liệu không hợp lệ (400)"
                    }
                } else {
                    e.localizedMessage ?: "Không thể tạo nhóm"
                }
                _uiState.update { it.copy(isLoading = false, error = message) }
            }
        }
    }
}
