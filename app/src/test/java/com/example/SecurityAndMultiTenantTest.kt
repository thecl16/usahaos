package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ActiveSessionEntity
import com.example.data.local.entity.BusinessEntity
import com.example.data.local.entity.BusinessUserEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.BusinessRepository
import com.example.domain.model.BusinessType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SecurityAndMultiTenantTest {

    private lateinit var db: AppDatabase
    private lateinit var authRepo: AuthRepository
    private lateinit var businessRepo: BusinessRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        authRepo = AuthRepository(db.userDao(), db.activeSessionDao(), db.businessUserDao())
        businessRepo = BusinessRepository(
            db.businessDao(),
            db.businessUserDao(),
            db.featureFlagDao(),
            db.activeSessionDao()
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    // 1. PBKDF2 hash verification
    @Test
    fun `test PBKDF2 hash format and verification`() {
        val plain = "SandiAman123!"
        val hash = AuthRepository.hashPbkdf2(plain)

        assertTrue(AuthRepository.isPbkdf2Hash(hash))
        val parts = hash.split("$")
        assertEquals(4, parts.size)
        assertEquals("pbkdf2", parts[0])
        assertEquals("12000", parts[1])
        assertTrue(parts[2].isNotBlank()) // Salt
        assertTrue(parts[3].isNotBlank()) // Derived hash

        // Correct password matches
        assertTrue(AuthRepository.verifyPbkdf2(plain, hash))
        // Wrong password fails
        assertFalse(AuthRepository.verifyPbkdf2("Salah123", hash))
    }

    // 2. Legacy SHA-256 password login succeeds and automatically upgraded to PBKDF2
    @Test
    fun `test legacy SHA-256 password login succeeds and upgrades to PBKDF2`() = runBlocking {
        val email = "legacy@usahaos.com"
        val passwordPlain = "LegacyPassword123"
        val legacySha256Hash = AuthRepository.hashLegacySha256(passwordPlain)

        val legacyUser = UserEntity(
            id = "user-legacy-1",
            email = email,
            fullName = "Legacy User",
            phone = "0811111111",
            passwordHash = legacySha256Hash
        )
        db.userDao().insertUser(legacyUser)

        assertFalse(AuthRepository.isPbkdf2Hash(legacyUser.passwordHash))

        // Login with correct password
        val loginResult = authRepo.login(email, passwordPlain)
        assertTrue(loginResult.isSuccess)

        // Check that database was upgraded to PBKDF2
        val updatedUser = db.userDao().getUserById("user-legacy-1")
        assertNotNull(updatedUser)
        assertTrue(AuthRepository.isPbkdf2Hash(updatedUser!!.passwordHash))
        assertTrue(AuthRepository.verifyPbkdf2(passwordPlain, updatedUser.passwordHash))

        // Subsequent login works seamlessly with PBKDF2
        val subsequentLogin = authRepo.login(email, passwordPlain)
        assertTrue(subsequentLogin.isSuccess)
    }

    // 3. Wrong password fails
    @Test
    fun `test wrong password fails login`() = runBlocking {
        val email = "budi@usahaos.com"
        val password = "PasswordKuat123"

        val regResult = authRepo.register("Budi Santoso", email, "0812345678", password)
        assertTrue(regResult.isSuccess)

        val wrongLoginResult = authRepo.login(email, "PasswordSalah")
        assertTrue(wrongLoginResult.isFailure)
        assertEquals(
            "Kata sandi salah. Silakan coba lagi.",
            wrongLoginResult.exceptionOrNull()?.message
        )
    }

    // 4. Switch business belonging to user succeeds
    @Test
    fun `test switch business belonging to user succeeds`() = runBlocking {
        val user = authRepo.register("Owner Usaha", "owner@usahaos.com", "0812345678", "OwnerPass123").getOrThrow()

        // Create 2 businesses
        val biz1 = businessRepo.createBusiness(
            name = "Toko Usaha Satu",
            type = BusinessType.RETAIL,
            address = "Jl. Satu No 1",
            phone = "081111",
            email = "satu@test.com",
            currency = "IDR",
            userId = user.id
        ).getOrThrow()

        val biz2 = businessRepo.createBusiness(
            name = "Toko Usaha Dua",
            type = BusinessType.FNB,
            address = "Jl. Dua No 2",
            phone = "082222",
            email = "dua@test.com",
            currency = "IDR",
            userId = user.id
        ).getOrThrow()

        // Switch back to biz1
        val switchResult = businessRepo.switchActiveBusiness(biz1.id)
        assertTrue(switchResult.isSuccess)

        val session = db.activeSessionDao().getSession()
        assertEquals(biz1.id, session?.currentBusinessId)

        // Switch to biz2
        val switchResult2 = businessRepo.switchActiveBusiness(biz2.id)
        assertTrue(switchResult2.isSuccess)

        val session2 = db.activeSessionDao().getSession()
        assertEquals(biz2.id, session2?.currentBusinessId)
    }

    // 5. Switch business belonging to another user fails
    @Test
    fun `test switch business belonging to another user fails`() = runBlocking {
        val userA = authRepo.register("User A", "usera@test.com", "0811", "PassUserA123").getOrThrow()
        val bizA = businessRepo.createBusiness("Biz A", BusinessType.RETAIL, "Jl A", "0811", "a@test.com", "IDR", userA.id).getOrThrow()

        val userB = authRepo.register("User B", "userb@test.com", "0822", "PassUserB123").getOrThrow()
        val bizB = businessRepo.createBusiness("Biz B", BusinessType.SERVICE, "Jl B", "0822", "b@test.com", "IDR", userB.id).getOrThrow()

        // Log in as user A
        authRepo.login("usera@test.com", "PassUserA123")
        val sessionBefore = db.activeSessionDao().getSession()
        assertEquals(userA.id, sessionBefore?.currentUserId)

        // User A tries to switch to Biz B (belonging to User B)
        val switchResult = businessRepo.switchActiveBusiness(bizB.id)
        assertTrue(switchResult.isFailure)
        assertEquals("Anda tidak memiliki akses aktif ke usaha ini.", switchResult.exceptionOrNull()?.message)

        // Ensure session currentBusinessId was NOT switched to bizB
        val sessionAfter = db.activeSessionDao().getSession()
        assertEquals(bizA.id, sessionAfter?.currentBusinessId)
    }

    // 6. Inactive membership fails
    @Test
    fun `test inactive membership fails to switch business`() = runBlocking {
        val user = authRepo.register("Karyawan Nonaktif", "karyawan@test.com", "0833", "KaryawanPass123").getOrThrow()
        val bizId = "biz-corp-123"

        // Insert business
        db.businessDao().insertBusiness(
            BusinessEntity(
                id = bizId,
                name = "Corp Alpha",
                type = BusinessType.RETAIL.code,
                address = "Jl. Alpha",
                phone = "0833",
                email = "corp@test.com",
                currency = "IDR"
            )
        )

        // Insert INACTIVE membership
        db.businessUserDao().insertBusinessUser(
            BusinessUserEntity(
                id = UUID.randomUUID().toString(),
                businessId = bizId,
                userId = user.id,
                role = "STAFF",
                isActive = false
            )
        )

        val switchResult = businessRepo.switchActiveBusiness(bizId)
        assertTrue(switchResult.isFailure)
        assertEquals("Anda tidak memiliki akses aktif ke usaha ini.", switchResult.exceptionOrNull()?.message)
    }

    // 7. Login without currentBusinessId selects valid business belonging to user
    @Test
    fun `test login without currentBusinessId auto selects valid business belonging to user`() = runBlocking {
        val user = authRepo.register("Owner Auto", "auto@test.com", "0844", "AutoPass123").getOrThrow()
        val biz = businessRepo.createBusiness("Toko Auto", BusinessType.RETAIL, "Jl Auto", "0844", "auto@test.com", "IDR", user.id).getOrThrow()

        // Clear or simulate session without currentBusinessId
        db.activeSessionDao().setSession(
            ActiveSessionEntity(
                id = 1,
                currentUserId = null,
                currentBusinessId = null
            )
        )

        // Login
        val loginResult = authRepo.login("auto@test.com", "AutoPass123")
        assertTrue(loginResult.isSuccess)

        val session = db.activeSessionDao().getSession()
        assertNotNull(session)
        assertEquals(user.id, session?.currentUserId)
        assertEquals(biz.id, session?.currentBusinessId)
    }

    // Bonus: Test secure reset password with old password verification
    @Test
    fun `test secure reset password with old password verification`() = runBlocking {
        val user = authRepo.register("Reset Test", "reset@test.com", "0855", "CurrentPassword123").getOrThrow()

        // Reset password with WRONG current password
        val failResult = authRepo.resetPassword("WrongOldPass", "NewSecretPassword123")
        assertTrue(failResult.isFailure)
        assertEquals("Kata sandi saat ini salah.", failResult.exceptionOrNull()?.message)

        // Reset password with CORRECT current password
        val successResult = authRepo.resetPassword("CurrentPassword123", "NewSecretPassword123")
        assertTrue(successResult.isSuccess)

        // Check login with new password succeeds
        val loginWithNew = authRepo.login("reset@test.com", "NewSecretPassword123")
        assertTrue(loginWithNew.isSuccess)

        // Check login with old password fails
        val loginWithOld = authRepo.login("reset@test.com", "CurrentPassword123")
        assertTrue(loginWithOld.isFailure)
    }
}
