
package com.example.smartparking.data.repository

import com.example.smartparking.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Supports both Firestore Timestamp and Long values.
    private fun timestampToMillis(value: Any?): Long {
        return when (value) {
            is Timestamp -> value.toDate().time
            is Number -> value.toLong()
            else -> 0L
        }
    }

    // Safely converts Firestore user documents into User objects.
    private fun mapUserDocument(document: DocumentSnapshot): User {

        return User(
            uid = document.id,

            name = document.getString("name") ?: "",
            email = document.getString("email") ?: "",
            phone = document.getString("phone") ?: "",
            bkashNumber = document.getString("bkashNumber") ?: "",
            role = document.getString("role") ?: "",
            profileImageUrl = document.getString("profileImageUrl") ?: "",

            carNumber = document.getString("carNumber") ?: "",
            nidNumber = document.getString("nidNumber") ?: "",
            drivingLicenseNumber = document.getString("drivingLicenseNumber") ?: "",

            verificationStatus = document.getString("verificationStatus")
                ?: "NOT_SUBMITTED",

            verificationSubmittedAt = timestampToMillis(
                document.get("verificationSubmittedAt")
            ),

            verifiedAt = timestampToMillis(
                document.get("verifiedAt")
            ),

            rejectionReason = document.getString("rejectionReason") ?: "",

            isVerified = document.getBoolean("isVerified") ?: false,

            drivingLicenseImageUrl = document.getString(
                "drivingLicenseImageUrl"
            ) ?: "",

            carImageUrl = document.getString("carImageUrl") ?: "",

            createdAt = timestampToMillis(
                document.get("createdAt")
            ).takeIf { it > 0L } ?: System.currentTimeMillis()
        )
    }

    suspend fun registerUser(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: String
    ): Result<User> {

        return try {

            val result = auth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = result.user
                ?: return Result.failure(
                    Exception("User registration failed")
                )

            val user = User(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                phone = phone,
                role = role
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(
        email: String,
        password: String
    ): Result<User> {

        return try {

            val result = auth
                .signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = result.user
                ?: return Result.failure(
                    Exception("Login failed")
                )

            val document = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (!document.exists()) {
                return Result.failure(
                    Exception("User profile not found")
                )
            }

            val user = mapUserDocument(document)

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getCurrentUser(): Result<User> {

        return try {

            val uid = auth.currentUser?.uid
                ?: return Result.failure(
                    Exception("User not logged in")
                )

            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            if (!document.exists()) {
                return Result.failure(
                    Exception("User profile not found")
                )
            }

            val user = mapUserDocument(document)

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
