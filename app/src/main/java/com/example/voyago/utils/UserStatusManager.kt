package com.example.voyago.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class UserStatusManager(private val uid: String) : DefaultLifecycleObserver {

    private val userDocRef = FirebaseFirestore.getInstance()
        .collection("profiles")
        .document(uid)

    fun setOnline() {
        updateStatus("online")
    }

    fun setOffline() {
        updateStatus("offline")
    }

    private fun updateStatus(state: String) {
        userDocRef.update(
            mapOf(
                "status.state" to state,
                "status.lastChanged" to FieldValue.serverTimestamp()
            )
        ).addOnFailureListener { e ->
            e.printStackTrace()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        setOnline()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        setOffline()
    }
}
