package com.theshire.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.theshire.app.data.AppDatabase
import com.theshire.app.data.LegumeDao
import com.theshire.app.data.LegumeEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

class LegumeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val legumeDao: LegumeDao = AppDatabase.getDatabase(application).legumeDao()
    
    val legumes: Flow<List<LegumeEntity>> = legumeDao.getAllLegumes()
    
    fun ajouterLegume(nom: String, categorie: String, conseils: String) {
        viewModelScope.launch {
            val legume = LegumeEntity(
                nom = nom,
                categorie = categorie,
                conseils = conseils
            )
            legumeDao.insertLegume(legume)
        }
    }
    
    fun supprimerLegume(legume: LegumeEntity) {
        viewModelScope.launch {
            legumeDao.deleteLegume(legume)
        }
    }
}
