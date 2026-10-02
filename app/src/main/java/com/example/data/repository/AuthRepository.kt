package com.example.data.repository

import com.example.data.local.dao.ActiveSessionDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ActiveSessionEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class AuthRepository(
    private val userDao: UserDao,
    private val activeSessionDao: ActiveSessionDao
) {
    val activeSession: Flow<ActiveSessionEntity?> = activeSessionDao.getSessionFlow()

    fun getUserByIdFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        passwordPlain: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanName = fullName.trim()

        if (cleanName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Nama lengkap tidak boleh kosong"))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return@withContext Result.failure(IllegalArgumentException("Format email tidak valid"))
        }
        if (passwordPlain.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Kata sandi minimal 6 karakter"))
        }

        val existingUser = userDao.getUserByEmail(cleanEmail)
        if (existingUser != null) {
            return@withContext Result.failure(IllegalStateException("Email sudah terdaftar. Silakan login."))
        }

        val newUser = UserEntity(
            id = UUID.randomUUID().toString(),
            email = cleanEmail,
            fullName = cleanName,
            phone = phone.trim(),
            passwordHash = hashPassword(passwordPlain)
        )

        userDao.insertUser(newUser)

        // Set session
        activeSessionDao.setSession(
            ActiveSessionEntity(
                id = 1,
                currentUserId = newUser.id,
                currentBusinessId = null
            )
        )

        Result.success(newUser)
    }

    suspend fun login(email: String, passwordPlain: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext Result.failure(IllegalArgumentException("Akun tidak ditemukan. Periksa email Anda."))

        val targetHash = hashPassword(passwordPlain)
        if (user.passwordHash != targetHash) {
            return@withContext Result.failure(IllegalArgumentException("Kata sandi salah. Silakan coba lagi."))
        }

        // Maintain existing business if user previously selected it, or leave null to check
        val currentSession = activeSessionDao.getSession()
        val businessId = if (currentSession?.currentUserId == user.id) currentSession.currentBusinessId else null

        activeSessionDao.setSession(
            ActiveSessionEntity(
                id = 1,
                currentUserId = user.id,
                currentBusinessId = businessId
            )
        )

        Result.success(user)
    }

    suspend fun resetPassword(email: String, newPasswordPlain: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (newPasswordPlain.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Kata sandi baru minimal 6 karakter"))
        }

        val user = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext Result.failure(IllegalArgumentException("Email tidak terdaftar di sistem."))

        val newHash = hashPassword(newPasswordPlain)
        userDao.updatePassword(user.id, newHash)
        Result.success(Unit)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        activeSessionDao.clearSession()
    }

    private fun hashPassword(plain: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(plain.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
