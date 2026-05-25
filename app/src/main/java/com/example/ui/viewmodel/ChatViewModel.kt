package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessage
import com.example.data.local.ChatSession
import com.example.data.repository.ChatRepository
import com.example.network.RetrofitClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    // List of all chat sessions
    val sessions: StateFlow<List<ChatSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently selected session ID
    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    // Current messages for the active session
    val messages: StateFlow<List<ChatMessage>> = _currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.getMessagesForSession(sessionId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Input field text
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // AI is typing status
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Error status text
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Search query for sessions filtering
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered sessions based on search title
    val filteredSessions: StateFlow<List<ChatSession>> = combine(sessions, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter { it.title.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Show Creator bio popup or view-panel
    private val _showCreatorInfo = MutableStateFlow(false)
    val showCreatorInfo: StateFlow<Boolean> = _showCreatorInfo.asStateFlow()

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowCreatorInfo(show: Boolean) {
        _showCreatorInfo.value = show
    }

    fun clearError() {
        _error.value = null
    }

    fun selectSession(sessionId: String?) {
        _currentSessionId.value = sessionId
        clearError()
    }

    fun createSession(title: String) {
        viewModelScope.launch {
            try {
                val newId = repository.createNewSession(title)
                _currentSessionId.value = newId
            } catch (e: Exception) {
                _error.value = "Failed to create new chat session: ${e.message}"
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            try {
                repository.deleteSession(sessionId)
                if (_currentSessionId.value == sessionId) {
                    _currentSessionId.value = null
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete chat session: ${e.message}"
            }
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return

        _inputText.value = ""
        _error.value = null

        viewModelScope.launch {
            var sessionId = _currentSessionId.value
            
            // If there's no active session, create a dynamic new one!
            if (sessionId.isNullOrBlank()) {
                val title = if (text.length > 30) "${text.take(27)}..." else text
                sessionId = repository.createNewSession(title)
                _currentSessionId.value = sessionId
            } else {
                // Update existing session's lastUpdated timestamp
                val currentSession = sessions.value.find { it.id == sessionId }
                if (currentSession != null) {
                    repository.saveSession(currentSession.copy(lastUpdated = System.currentTimeMillis()))
                }
            }

            // Save user message in local DB
            try {
                repository.insertMessage(sessionId, "user", text)
            } catch (e: Exception) {
                _error.value = "Failed to save message: ${e.message}"
                return@launch
            }

            // Trigger AI response generation
            _isGenerating.value = true
            try {
                // Fetch up to the latest messages from local database
                val currentHistory = repository.getMessagesForSession(sessionId).first()
                val reply = repository.generateAiResponse(sessionId, text, currentHistory)
                
                // Save AI message to DB
                repository.insertMessage(sessionId, "model", reply)
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred while getting response."
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // Factory to construct ViewModel properly with application context
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                val database = AppDatabase.getDatabase(application)
                val repository = ChatRepository(database.chatDao(), RetrofitClient.service)
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(repository) as T
            }
            throw java.lang.IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
