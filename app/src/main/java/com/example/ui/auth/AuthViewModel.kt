package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.SessionEntity
import com.example.data.local.entity.UserAddressEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.security.JwtPayload
import com.example.security.JwtTokenUtil
import com.example.security.Permission
import com.example.security.RbacPolicy
import com.example.security.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AuthTab { PHONE, EMAIL }

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val activeSession: SessionEntity? = null,
    val currentUser: UserEntity? = null,
    val currentJwtPayload: JwtPayload? = null,
    val userPermissions: Set<Permission> = emptySet(),
    val userAddresses: List<UserAddressEntity> = emptyList()
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.activeSessionFlow.collectLatest { session ->
                if (session != null) {
                    val payload = JwtTokenUtil.parseTokenPayload(session.jwtToken)
                    val activeRole = UserRole.fromCode(session.activeRole)
                    val permissions = RbacPolicy.getPermissionsForRoles(listOf(activeRole))

                    _uiState.value = _uiState.value.copy(
                        activeSession = session,
                        currentJwtPayload = payload,
                        userPermissions = permissions
                    )

                    // Fetch user and address
                    observeUserData(session.userId)
                } else {
                    _uiState.value = AuthUiState()
                }
            }
        }
    }

    private fun observeUserData(userId: Long) {
        viewModelScope.launch {
            repository.getCurrentUserFlow(userId).collectLatest { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
            }
        }
        viewModelScope.launch {
            repository.getUserAddressesFlow(userId).collectLatest { addresses ->
                _uiState.value = _uiState.value.copy(userAddresses = addresses)
            }
        }
    }

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.login(identifier, password)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Logged in successfully!"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        password: String,
        cityProvince: String,
        gender: String,
        selectedRole: UserRole,
        storeName: String = "",
        vehicleType: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.register(
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                email = email,
                password = password,
                cityProvince = cityProvince,
                gender = gender,
                selectedRole = selectedRole,
                storeName = storeName,
                vehicleType = vehicleType
            )) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Account created & logged in!"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun switchRole(newRole: UserRole) {
        val session = _uiState.value.activeSession ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.switchUserRole(session.userId, newRole)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Switched active role to ${newRole.displayName}"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun addAddress(label: String, street: String, city: String, isDefault: Boolean) {
        val session = _uiState.value.activeSession ?: return
        viewModelScope.launch {
            repository.addAddress(session.userId, label, street, city, isDefault)
            _uiState.value = _uiState.value.copy(successMessage = "Address added to Address Book")
        }
    }

    fun deleteAddress(addressId: Long) {
        viewModelScope.launch {
            repository.deleteAddress(addressId)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
