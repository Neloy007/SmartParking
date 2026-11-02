package com.example.smartparking.data.repository

import com.example.smartparking.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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

            firestore
                .collection("users")
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

            val document = firestore
                .collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = document.toObject(User::class.java)
                ?: return Result.failure(
                    Exception("User profile not found"))

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

            val document = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()

            val user = document.toObject(User::class.java)
                ?: return Result.failure(
                    Exception("User profile not found"))

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}