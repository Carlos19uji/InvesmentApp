package com.example.groupprojectapp

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

fun createPortfolioForNewUser(userID: String){
    val db = FirebaseFirestore.getInstance()
    val userPorfolioRef = db.collection("users").document(userID).collection("portfolio")

    userPorfolioRef.get().addOnCompleteListener{ task ->
        if (task.isSuccessful){
            if (task.result?.isEmpty == true){
                Log.d("createPortfolio", "User portfolio collection is empty, initializing.")
                val initialPortfolio = hashMapOf(
                    "name" to "crear",   // Nombre de la criptomoneda o stock
                    "units" to 1         // Unidades de esa criptomoneda o stock
                )

                // Crear un documento con los datos predeterminados
                userPorfolioRef.document("crear").set(initialPortfolio)
                    .addOnSuccessListener {
                        Log.d("createPortfolio", "Portfolio initialized for new user.")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("createPortfolio", "Error initializing portfolio", exception)
                    }
            }
        } else {
            Log.e("createPortfolio", "Error creating user portfolio collection", task.exception)
        }
    }
}

fun addItemPortfolio(userID: String, itemName: String, itemUnits: Int){
    val db =  FirebaseFirestore.getInstance()
    val userPortfolioRef = db.collection("users").document(userID).collection("portfolio")

    userPortfolioRef.document(itemName).get().addOnCompleteListener{ task ->
        if (task.isSuccessful){
            val document = task.result
            if (document.exists()){
                val currentUnits = document.getLong("units")?.toInt()?:0
                val newUnits = currentUnits + itemUnits
                userPortfolioRef.document(itemName).update("units", newUnits)
                    .addOnSuccessListener {
                        Log.d("addItemToPortfolio", "Updated units for $itemName to $newUnits")
                    }
                    .addOnFailureListener { e ->
                        Log.e("addItemToPortfolio", "Error updating units: $e")
                    }
            }else{
                val itemData = hashMapOf("name" to itemName, "units" to itemUnits)
                userPortfolioRef.document(itemName).set(itemData)
                    .addOnSuccessListener {
                        Log.d("addItemToPortfolio", "Added $itemName with $itemUnits units to portfolio.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("addItemToPortfolio", "Error adding item: $e")
                    }
            }
        } else {
            Log.e("addItemToPortfolio", "Error getting item from portfolio", task.exception)
        }
    }
}

fun removeItemFromPortfolio(userID: String, itemName: String, itemUnits: Int){
    val db = FirebaseFirestore.getInstance()
    val userPortfolioRef = db.collection("users").document(userID).collection("portfolio")

    userPortfolioRef.document(itemName).get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val document = task.result
            if (document.exists()) {
                val currentUnits = document.getLong("units")?.toInt() ?: 0

                if (currentUnits < itemUnits) {
                    Log.e("removeItemFromPortfolio", "Not enough units to sell for $itemName.")
                    return@addOnCompleteListener
                }

                val newUnits = currentUnits - itemUnits

                if (newUnits > 0) {
                    userPortfolioRef.document(itemName).update("units", newUnits)
                        .addOnSuccessListener {
                            Log.d("removeItemFromPortfolio", "Updated units for $itemName to $newUnits.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("removeItemFromPortfolio", "Error updating units: $e")
                        }
                } else {
                    userPortfolioRef.document(itemName).delete()
                        .addOnSuccessListener {
                            Log.d("removeItemFromPortfolio", "Removed $itemName from portfolio.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("removeItemFromPortfolio", "Error removing item: $e")
                        }
                }
            } else {
                Log.e("removeItemFromPortfolio", "Item does not exist in portfolio.")
            }
        } else {
            Log.e("removeItemFromPortfolio", "Error getting item from portfolio", task.exception)
        }
    }
}

