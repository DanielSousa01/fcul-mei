package com.example.marsphotos.network

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseService {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databasePhotosReference = firebaseDatabase.reference.child("photos")
    private val databaseRollsReference = firebaseDatabase.reference.child("rolls")

    suspend fun savePhotos(photoPair: PhotoPair) {
        suspendCoroutine { continuation ->
            databasePhotosReference.push().setValue(photoPair)
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

suspend fun getLastPhotoPair(): PhotoPair? {
    return suspendCoroutine { continuation ->
        databasePhotosReference.limitToLast(1).get()
            .addOnSuccessListener { dataSnapshot ->
                val photoPair = try {
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        val photoPair = dataSnapshot.children.last().getValue(PhotoPair::class.java)
                        Log.d("FirebaseService", "Last photo pair retrieved: $photoPair")
                        photoPair
                    } else {
                        Log.d("FirebaseService", "No photo pair found")
                        null
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseService", "Error parsing photo pair", e)
                    null
                }
                continuation.resume(photoPair)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
}

    suspend fun getRolls(): Int {
        return suspendCoroutine { continuation ->
            databaseRollsReference.get().addOnSuccessListener {
                val rolls = it.getValue(Int::class.java) ?: 0
                continuation.resume(rolls)
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }
    }

    fun incrementRolls() {
        databaseRollsReference.get().addOnSuccessListener {
            val currentRolls = it.getValue(Int::class.java) ?: 0
            databaseRollsReference.setValue(currentRolls + 1)
        }
    }
}