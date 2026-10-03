package com.example.data.repository

import com.example.data.local.dao.ActiveSessionDao
import com.example.data.local.dao.BusinessUserDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ActiveSessionEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class AuthRepository(
    private val userDao: UserDao,
    private val activeSessionDao: ActiveSessionDao,
    private val businessUserDao: BusinessUserDao
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
            passwordHash = hashPbkdf2(passwordPlain)
        )

        userDao.insertUser(newUser)

        // Set session for new user
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

        val isPbkdf2 = isPbkdf2Hash(user.passwordHash)
        val passwordValid = if (isPbkdf2) {
            verifyPbkdf2(passwordPlain, user.passwordHash)
        } else {
            // Legacy SHA-256 verification
            verifyLegacySha256(passwordPlain, user.passwordHash)
        }

        if (!passwordValid) {
            return@withContext Result.failure(IllegalArgumentException("Kata sandi salah. Silakan coba lagi."))
        }

        // Backward compatibility: If user still has legacy SHA-256 hash, automatically upgrade to PBKDF2
        var currentUserEntity = user
        if (!isPbkdf2) {
            val upgradedHash = hashPbkdf2(passwordPlain)
            userDao.updatePassword(user.id, upgradedHash)
            currentUserEntity = user.copy(passwordHash = upgradedHash)
        }

        // Auto-select valid business for this user
        val userBusinesses = businessUserDao.getBusinessesForUserList(currentUserEntity.id)
        val currentSession = activeSessionDao.getSession()

        val selectedBusinessId = if (currentSession?.currentUserId == currentUserEntity.id &&
            currentSession.currentBusinessId != null &&
            userBusinesses.any { it.id == currentSession.currentBusinessId }
        ) {
            currentSession.currentBusinessId
        } else if (userBusinesses.isNotEmpty()) {
            userBusinesses.first().id
        } else {
            null
        }

        activeSessionDao.setSession(
            ActiveSessionEntity(
                id = 1,
                currentUserId = currentUserEntity.id,
                currentBusinessId = selectedBusinessId
            )
        )

        Result.success(currentUserEntity)
    }

    suspend fun resetPassword(currentPasswordPlain: String, newPasswordPlain: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (newPasswordPlain.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Kata sandi baru minimal 6 karakter"))
        }

        val session = activeSessionDao.getSession()
        val currentUserId = session?.currentUserId
            ?: return@withContext Result.failure(IllegalStateException("Sesi pengguna tidak valid. Silakan login terlebih dahulu."))

        val user = userDao.getUserById(currentUserId)
            ?: return@withContext Result.failure(IllegalStateException("Akun pengguna tidak ditemukan."))

        val isPbkdf2 = isPbkdf2Hash(user.passwordHash)
        val currentPasswordValid = if (isPbkdf2) {
            verifyPbkdf2(currentPasswordPlain, user.passwordHash)
        } else {
            verifyLegacySha256(currentPasswordPlain, user.passwordHash)
        }

        if (!currentPasswordValid) {
            return@withContext Result.failure(IllegalArgumentException("Kata sandi saat ini salah."))
        }

        val newHash = hashPbkdf2(newPasswordPlain)
        userDao.updatePassword(user.id, newHash)
        Result.success(Unit)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        activeSessionDao.clearSession()
    }

    companion object {
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val PBKDF2_ITERATIONS = 12000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_LENGTH_BYTES = 16

        fun isPbkdf2Hash(hash: String): Boolean {
            return hash.startsWith("pbkdf2$")
        }

        fun hashPbkdf2(
            password: String,
            iterations: Int = PBKDF2_ITERATIONS,
            salt: ByteArray = generateSalt()
        ): String {
            val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val hashBytes = factory.generateSecret(spec).encoded
            val saltBase64 = Base64.getEncoder().encodeToString(salt)
            val hashBase64 = Base64.getEncoder().encodeToString(hashBytes)
            return "pbkdf2$$iterations$$saltBase64$$hashBase64"
        }

        fun verifyPbkdf2(password: String, storedHash: String): Boolean {
            val parts = storedHash.split("$")
            if (parts.size != 4 || parts[0] != "pbkdf2") return false

            val iterations = parts[1].toIntOrNull() ?: return false
            val salt = try {
                Base64.getDecoder().decode(parts[2])
            } catch (e: Exception) {
                return false
            }
            val expectedHash = try {
                Base64.getDecoder().decode(parts[3])
            } catch (e: Exception) {
                return false
            }

            val spec = PBEKeySpec(password.toCharArray(), salt, iterations, expectedHash.size * 8)
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val computedHash = factory.generateSecret(spec).encoded

            return MessageDigest.isEqual(computedHash, expectedHash)
        }

        fun hashLegacySha256(plain: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(plain.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun verifyLegacySha256(password: String, storedHash: String): Boolean {
            val candidateHash = hashLegacySha256(password)
            return MessageDigest.isEqual(
                candidateHash.toByteArray(Charsets.UTF_8),
                storedHash.toByteArray(Charsets.UTF_8)
            )
        }

        private fun generateSalt(): ByteArray {
            val random = SecureRandom()
            val salt = ByteArray(SALT_LENGTH_BYTES)
            random.nextBytes(salt)
            return salt
        }
    }
}
