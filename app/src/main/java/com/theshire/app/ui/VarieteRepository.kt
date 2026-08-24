package com.theshire.app.ui

import android.content.Context
import com.theshire.app.data.AppDatabase
import com.theshire.app.data.VarieteEntity
import kotlinx.coroutines.flow.Flow

class VarieteRepository(context: Context) {
    
    private val legumeDao = AppDatabase.getDatabase(context).legumeDao()
    
    fun getVarietesForLegume(legumeNom: String): Flow<List<VarieteEntity>> {
        return legumeDao.getVarietesForLegume(legumeNom)
    }
    
    suspend fun ajouterVarietesPredefinies() {
        if (legumeDao.countVarietes() == 0) {
            getVarietesPredefinies().forEach { variete ->
                legumeDao.insertVariete(variete)
            }
        }
    }
    
    private fun getVarietesPredefinies(): List<VarieteEntity> {
        return listOf(
            // ===== CAROTTE =====
            VarieteEntity(
                legumeParent = "Carotte",
                nom = "Nantaise",
                description = "La variété la plus cultivée en France. Racine cylindrique, orange vif, sucrée.",
                semis = "Mars à juillet",
                plantation = "Semis direct en pleine terre",
                recolte = "3 à 4 mois après semis",
                entretien = "Éclaircir à 5 cm, biner régulièrement",
                particularites = "Excellente conservation, idéale pour débutants"
            ),
            VarieteEntity(
                legumeParent = "Carotte",
                nom = "Touchon",
                description = "Racine demi-longue, précoce, très tendre.",
                semis = "Février à avril (sous abri) ou avril à juillet",
                plantation = "Semis direct",
                recolte = "2 à 3 mois après semis",
                entretien = "Arrosage régulier, éclaircir à 5 cm",
                particularites = "Précoce, idéale pour cultures de printemps"
            ),
            VarieteEntity(
                legumeParent = "Carotte",
                nom = "Chantenay",
                description = "Racine courte et large, rouge-orangé, très sucrée.",
                semis = "Avril à juillet",
                plantation = "Semis direct",
                recolte = "3 à 4 mois après semis",
                entretien = "Sol meuble, arrosage modéré",
                particularites = "Idéale pour sols lourds, bonne conservation"
            ),
            VarieteEntity(
                legumeParent = "Carotte",
                nom = "Amsterdam",
                description = "Racine fine et longue, précoce, croquante.",
                semis = "Février à mars (sous abri) ou avril à juin",
                plantation = "Semis direct",
                recolte = "2 à 3 mois après semis",
                entretien = "Arrosage régulier, éclaircir",
                particularites = "Parfaite pour cultures hâtives sous abri"
            ),
            VarieteEntity(
                legumeParent = "Carotte",
                nom = "Colmar",
                description = "Grosse racine, très productive, pour conservation hivernale.",
                semis = "Mai à juillet",
                plantation = "Semis direct",
                recolte = "4 à 5 mois après semis",
                entretien = "Butter les épaules, pailler",
                particularites = "Excellente conservation en cave"
            ),

            // ===== TOMATE =====
            VarieteEntity(
                legumeParent = "Tomate",
                nom = "Marmande",
                description = "Fruit côtelé, charnu, saveur intense. Variété ancienne française.",
                semis = "Février à avril (sous abri)",
                plantation = "Mai (après les gelées)",
                recolte = "Juillet à octobre",
                entretien = "Tuteurer, effeuiller, arroser au pied",
                particularites = "Idéale farcie, résistante à la sécheresse"
            ),
            VarieteEntity(
                legumeParent = "Tomate",
                nom = "Cœur de Bœuf",
                description = "Gros fruit en forme de cœur, chair dense, peu de graines.",
                semis = "Février à avril (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à octobre",
                entretien = "Tuteurer solidement, supprimer les gourmands",
                particularites = "Parfaite en salade, goût exceptionnel"
            ),
            VarieteEntity(
                legumeParent = "Tomate",
                nom = "Roma",
                description = "Fruit allongé, peu d'eau, idéale pour sauces et conserves.",
                semis = "Février à avril (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à septembre",
                entretien = "Tuteurer, arroser régulièrement au pied",
                particularites = "Parfaite pour coulis, tomates séchées"
            ),
            VarieteEntity(
                legumeParent = "Tomate",
                nom = "Cerise",
                description = "Petits fruits en grappes, très sucrés, production abondante.",
                semis = "Février à avril (sous abri)",
                plantation = "Mai",
                recolte = "Juin à octobre",
                entretien = "Tuteurer, arrosage régulier",
                particularites = "Idéale apéritif, résistante aux maladies"
            ),
            VarieteEntity(
                legumeParent = "Tomate",
                nom = "Ananas",
                description = "Gros fruit jaune-orangé marbré de rouge, très parfumé.",
                semis = "Février à avril (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à octobre",
                entretien = "Tuteurer, pailler, arroser au pied",
                particularites = "Saveur douce et fruitée, très productive"
            ),

            // ===== SALADE =====
            VarieteEntity(
                legumeParent = "Salade",
                nom = "Batavia",
                description = "Feuilles croquantes, pomme volumineuse, résistante à la chaleur.",
                semis = "Mars à août",
                plantation = "Repiquage ou semis direct",
                recolte = "6 à 8 semaines après semis",
                entretien = "Pailler, arroser régulièrement",
                particularites = "Résistante à la montaison, croquante"
            ),
            VarieteEntity(
                legumeParent = "Salade",
                nom = "Laitue",
                description = "Pomme serrée, feuilles tendres, saveur douce.",
                semis = "Mars à septembre",
                plantation = "Repiquage",
                recolte = "6 à 8 semaines après semis",
                entretien = "Arroser le matin, pailler",
                particularites = "Classique incontournable, tendre"
            ),
            VarieteEntity(
                legumeParent = "Salade",
                nom = "Romaine",
                description = "Feuilles allongées, croquantes, saveur légèrement amère.",
                semis = "Avril à août",
                plantation = "Repiquage",
                recolte = "8 à 10 semaines après semis",
                entretien = "Arrosage régulier, pailler",
                particularites = "Résistante à la chaleur, idéale en salade César"
            ),
            VarieteEntity(
                legumeParent = "Salade",
                nom = "Mâche",
                description = "Petites feuilles vert foncé, saveur douce, très rustique.",
                semis = "Août à octobre",
                plantation = "Semis direct",
                recolte = "Novembre à mars",
                entretien = "Pailler, protéger du gel",
                particularites = "Culture d'hiver, très résistante au froid"
            ),
            VarieteEntity(
                legumeParent = "Salade",
                nom = "Roquette",
                description = "Feuilles découpées, saveur poivrée, croissance rapide.",
                semis = "Mars à septembre",
                plantation = "Semis direct",
                recolte = "4 à 6 semaines après semis",
                entretien = "Arrosage régulier, couper les feuilles",
                particularites = "Pousse rapide, plusieurs récoltes possibles"
            ),

            // ===== COURGETTE =====
            VarieteEntity(
                legumeParent = "Courgette",
                nom = "Noire de Milan",
                description = "Fruit vert foncé, précoce, très productive.",
                semis = "Avril (sous abri)",
                plantation = "Mai",
                recolte = "Juin à octobre",
                entretien = "Pailler, arroser abondamment",
                particularites = "La variété la plus cultivée en France"
            ),
            VarieteEntity(
                legumeParent = "Courgette",
                nom = "Ronde de Nice",
                description = "Fruit rond, idéal farci, chair fine.",
                semis = "Avril (sous abri)",
                plantation = "Mai",
                recolte = "Juin à octobre",
                entretien = "Arroser au pied, pailler",
                particularites = "Parfaite farcie, originale"
            ),
            VarieteEntity(
                legumeParent = "Courgette",
                nom = "Gold Rush",
                description = "Fruit jaune doré, précoce, très productive.",
                semis = "Avril (sous abri)",
                plantation = "Mai",
                recolte = "Juin à octobre",
                entretien = "Arroser régulièrement, pailler",
                particularites = "Belle couleur jaune, saveur douce"
            ),
            VarieteEntity(
                legumeParent = "Courgette",
                nom = "Longue de Florence",
                description = "Fruit vert clair rayé, allongé, très productif.",
                semis = "Avril (sous abri)",
                plantation = "Mai",
                recolte = "Juin à octobre",
                entretien = "Pailler, arroser abondamment",
                particularites = "Productive, résistante à l'oïdium"
            ),
            VarieteEntity(
                legumeParent = "Courgette",
                nom = "Blanche de Virginie",
                description = "Fruit blanc crème, chair ferme, buissonnante.",
                semis = "Avril (sous abri)",
                plantation = "Mai",
                recolte = "Juin à octobre",
                entretien = "Arroser au pied, pailler",
                particularites = "Originale, non coureuse, productive"
            ),

            // ===== HARICOT VERT =====
            VarieteEntity(
                legumeParent = "Haricot vert",
                nom = "Contender",
                description = "Gousse verte, charnue, sans fil, précoce.",
                semis = "Avril à juillet",
                plantation = "Semis direct",
                recolte = "2 mois après semis",
                entretien = "Butter, arroser régulièrement",
                particularites = "Très productive, résistante aux maladies"
            ),
            VarieteEntity(
                legumeParent = "Haricot vert",
                nom = "Fin de Bagnols",
                description = "Gousse fine, très longue, saveur délicate.",
                semis = "Avril à juillet",
                plantation = "Semis direct",
                recolte = "2 à 3 mois après semis",
                entretien = "Tuteurer, arroser",
                particularites = "Variété ancienne française, très fine"
            ),
            VarieteEntity(
                legumeParent = "Haricot vert",
                nom = "Beurre",
                description = "Gousse jaune doré, charnue, saveur douce.",
                semis = "Mai à juillet",
                plantation = "Semis direct",
                recolte = "2 mois après semis",
                entretien = "Butter, pailler",
                particularites = "Couleur jaune, tendre, sans fil"
            ),
            VarieteEntity(
                legumeParent = "Haricot vert",
                nom = "Mangetout",
                description = "Gousse sans parchemin, se mange entière.",
                semis = "Avril à juillet",
                plantation = "Semis direct",
                recolte = "2 mois après semis",
                entretien = "Arroser, butter",
                particularites = "Gousses plates, très tendres"
            ),
            VarieteEntity(
                legumeParent = "Haricot vert",
                nom = "Cobra",
                description = "Gousse verte, longue, très productive.",
                semis = "Mai à juillet",
                plantation = "Semis direct",
                recolte = "2 mois après semis",
                entretien = "Tuteurer, arroser",
                particularites = "Grimpe haut, production étalée"
            ),

            // ===== POMME DE TERRE =====
            VarieteEntity(
                legumeParent = "Pomme de terre",
                nom = "Charlotte",
                description = "Chair ferme, excellente cuisson vapeur, salade.",
                semis = "Mars à avril (plantation)",
                plantation = "Mars à avril",
                recolte = "90 à 120 jours",
                entretien = "Butter, arroser",
                particularites = "La préférée des Français, chair ferme"
            ),
            VarieteEntity(
                legumeParent = "Pomme de terre",
                nom = "Bintje",
                description = "Chair farineuse, parfaite pour frites et purée.",
                semis = "Mars à avril (plantation)",
                plantation = "Mars à avril",
                recolte = "90 à 120 jours",
                entretien = "Butter, pailler",
                particularites = "La référence pour les frites"
            ),
            VarieteEntity(
                legumeParent = "Pomme de terre",
                nom = "Ratte",
                description = "Petite, allongée, chair ferme, goût de noisette.",
                semis = "Mars à avril (plantation)",
                plantation = "Mars à avril",
                recolte = "90 à 120 jours",
                entretien = "Butter, arroser",
                particularites = "Gastronomique, parfaite vapeur"
            ),
            VarieteEntity(
                legumeParent = "Pomme de terre",
                nom = "Amandine",
                description = "Chair ferme, précoce, très productive.",
                semis = "Février à mars (plantation)",
                plantation = "Février à mars",
                recolte = "80 à 90 jours",
                entretien = "Butter, arroser",
                particularites = "Précoce, idéale primeur"
            ),
            VarieteEntity(
                legumeParent = "Pomme de terre",
                nom = "Monalisa",
                description = "Chair fondante, polyvalente, bonne conservation.",
                semis = "Mars à avril (plantation)",
                plantation = "Mars à avril",
                recolte = "90 à 120 jours",
                entretien = "Butter, pailler",
                particularites = "Polyvalente, se conserve bien"
            ),

            // ===== OIGNON =====
            VarieteEntity(
                legumeParent = "Oignon",
                nom = "Jaune de Mulhouse",
                description = "Bulbe jaune, conservation excellente.",
                semis = "Février à avril",
                plantation = "Mars à avril",
                recolte = "Juillet à septembre",
                entretien = "Biner, éviter l'excès d'eau",
                particularites = "Excellente conservation, polyvalent"
            ),
            VarieteEntity(
                legumeParent = "Oignon",
                nom = "Rouge de Brunswick",
                description = "Bulbe rouge, doux, idéal cru en salade.",
                semis = "Février à avril",
                plantation = "Mars à avril",
                recolte = "Juillet à septembre",
                entretien = "Biner, arroser modérément",
                particularites = "Doux, parfait cru"
            ),
            VarieteEntity(
                legumeParent = "Oignon",
                nom = "Blanc de Paris",
                description = "Bulbe blanc, précoce, doux.",
                semis = "Février à mars",
                plantation = "Mars à avril",
                recolte = "Mai à juillet",
                entretien = "Arroser, biner",
                particularites = "Précoce, idéal printemps"
            ),
            VarieteEntity(
                legumeParent = "Oignon",
                nom = "Échalote grise",
                description = "Saveur fine, très appréciée en gastronomie.",
                semis = "Octobre à novembre (plantation)",
                plantation = "Octobre à novembre",
                recolte = "Juin à juillet",
                entretien = "Biner, pailler",
                particularites = "Goût raffiné, gastronomique"
            ),
            VarieteEntity(
                legumeParent = "Oignon",
                nom = "Rouge de Florence",
                description = "Long, rouge, doux, parfait cru.",
                semis = "Février à avril",
                plantation = "Mars à avril",
                recolte = "Juillet à septembre",
                entretien = "Biner, arroser modérément",
                particularites = "Forme allongée, très doux"
            ),

            // ===== POIREAU =====
            VarieteEntity(
                legumeParent = "Poireau",
                nom = "Bleu de Solaise",
                description = "Fût bleu-violet, très résistant au froid.",
                semis = "Février à avril",
                plantation = "Mai à juillet",
                recolte = "Octobre à mars",
                entretien = "Butter pour blanchir",
                particularites = "Très rustique, idéal hiver"
            ),
            VarieteEntity(
                legumeParent = "Poireau",
                nom = "Jaune Gros du Poitou",
                description = "Fût jaune, gros, précoce.",
                semis = "Février à avril",
                plantation = "Mai à juillet",
                recolte = "Septembre à décembre",
                entretien = "Butter, pailler",
                particularites = "Précoce, productif"
            ),
            VarieteEntity(
                legumeParent = "Poireau",
                nom = "Monstrueux de Carentan",
                description = "Très gros fût, productif, rustique.",
                semis = "Février à avril",
                plantation = "Mai à juillet",
                recolte = "Octobre à mars",
                entretien = "Butter, arroser",
                particularites = "Gros calibre, productif"
            ),
            VarieteEntity(
                legumeParent = "Poireau",
                nom = "Long de Mézières",
                description = "Fût long et fin, saveur délicate.",
                semis = "Février à avril",
                plantation = "Mai à juillet",
                recolte = "Octobre à décembre",
                entretien = "Butter profondément",
                particularites = "Fin et délicat, gastronomique"
            ),
            VarieteEntity(
                legumeParent = "Poireau",
                nom = "Électra",
                description = "Croissance rapide, résistant à la rouille.",
                semis = "Mars à mai",
                plantation = "Mai à juillet",
                recolte = "Septembre à novembre",
                entretien = "Butter, arroser",
                particularites = "Résistant aux maladies, précoce"
            ),

            // ===== ÉPINARD =====
            VarieteEntity(
                legumeParent = "Épinard",
                nom = "Géant d'Hiver",
                description = "Grandes feuilles, très rustique, pour culture hivernale.",
                semis = "Août à octobre",
                plantation = "Semis direct",
                recolte = "Novembre à mars",
                entretien = "Pailler, protéger",
                particularites = "Résiste au gel, productif"
            ),
            VarieteEntity(
                legumeParent = "Épinard",
                nom = "Matador",
                description = "Feuilles épaisses, croissance rapide.",
                semis = "Mars à mai",
                plantation = "Semis direct",
                recolte = "6 à 8 semaines",
                entretien = "Arroser, éclaircir",
                particularites = "Productif, résistant à la chaleur"
            ),
            VarieteEntity(
                legumeParent = "Épinard",
                nom = "Viking",
                description = "Feuilles rondes, très productif.",
                semis = "Mars à mai",
                plantation = "Semis direct",
                recolte = "6 à 8 semaines",
                entretien = "Arroser régulièrement",
                particularites = "Productif, bonne tenue"
            ),
            VarieteEntity(
                legumeParent = "Épinard",
                nom = "Butterflay",
                description = "Feuilles tendres, saveur douce.",
                semis = "Mars à mai, août à septembre",
                plantation = "Semis direct",
                recolte = "6 à 8 semaines",
                entretien = "Pailler, arroser",
                particularites = "Tendre, parfait en salade"
            ),
            VarieteEntity(
                legumeParent = "Épinard",
                nom = "Monstrueux de Viroflay",
                description = "Très grandes feuilles, variété ancienne.",
                semis = "Mars à mai",
                plantation = "Semis direct",
                recolte = "6 à 8 semaines",
                entretien = "Arroser abondamment",
                particularites = "Feuilles géantes, productif"
            ),

            // ===== RADIS =====
            VarieteEntity(
                legumeParent = "Radis",
                nom = "De 18 jours",
                description = "Croissance ultra rapide, rouge rond.",
                semis = "Mars à septembre",
                plantation = "Semis direct",
                recolte = "18 jours après semis",
                entretien = "Arroser quotidiennement",
                particularites = "Le plus rapide, idéal enfants"
            ),
            VarieteEntity(
                legumeParent = "Radis",
                nom = "Glaçon",
                description = "Blanc, allongé, croquant.",
                semis = "Mars à septembre",
                plantation = "Semis direct",
                recolte = "3 à 4 semaines",
                entretien = "Arroser régulièrement",
                particularites = "Croquant, original"
            ),
            VarieteEntity(
                legumeParent = "Radis",
                nom = "Flamboyant",
                description = "Rouge vif, long, croquant.",
                semis = "Mars à septembre",
                plantation = "Semis direct",
                recolte = "3 à 4 semaines",
                entretien = "Arroser, éclaircir",
                particularites = "Belle couleur, croquant"
            ),
            VarieteEntity(
                legumeParent = "Radis",
                nom = "Noir Gros Long",
                description = "Radis noir d'hiver, saveur forte.",
                semis = "Juillet à août",
                plantation = "Semis direct",
                recolte = "3 à 4 mois",
                entretien = "Arroser, pailler",
                particularites = "Conservation hivernale, fort"
            ),
            VarieteEntity(
                legumeParent = "Radis",
                nom = "Cerise",
                description = "Rond, rouge, croquant, précoce.",
                semis = "Mars à septembre",
                plantation = "Semis direct",
                recolte = "3 semaines",
                entretien = "Arroser quotidiennement",
                particularites = "Classique, croquant"
            ),

            // ===== BROCOLI =====
            VarieteEntity(
                legumeParent = "Brocoli",
                nom = "Calabrais",
                description = "Tête verte, compacte, productive.",
                semis = "Mars à juin",
                plantation = "Avril à juillet",
                recolte = "3 à 4 mois",
                entretien = "Pailler, arroser",
                particularites = "Classique, productif"
            ),
            VarieteEntity(
                legumeParent = "Brocoli",
                nom = "Verdia",
                description = "Précoce, tête bien formée.",
                semis = "Mars à mai",
                plantation = "Avril à juin",
                recolte = "2 à 3 mois",
                entretien = "Arroser régulièrement",
                particularites = "Précoce, bon rendement"
            ),
            VarieteEntity(
                legumeParent = "Brocoli",
                nom = "Marathon",
                description = "Tête grosse, compacte, tardive.",
                semis = "Mai à juillet",
                plantation = "Juin à août",
                recolte = "3 à 4 mois",
                entretien = "Pailler, arroser",
                particularites = "Tardif, grosse production"
            ),
            VarieteEntity(
                legumeParent = "Brocoli",
                nom = "Belstar",
                description = "Bio, rustique, bonne reprise.",
                semis = "Mars à juin",
                plantation = "Avril à juillet",
                recolte = "3 mois",
                entretien = "Arroser, pailler",
                particularites = "Adapté bio, rustique"
            ),
            VarieteEntity(
                legumeParent = "Brocoli",
                nom = "Parthenon",
                description = "Précoce, tête compacte.",
                semis = "Février à avril",
                plantation = "Mars à mai",
                recolte = "2 à 3 mois",
                entretien = "Arroser, pailler",
                particularites = "Très précoce, productif"
            ),

            // ===== POIVRON =====
            VarieteEntity(
                legumeParent = "Poivron",
                nom = "Doux d'Espagne",
                description = "Long, rouge, très doux.",
                semis = "Février à mars (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à octobre",
                entretien = "Tuteurer, pailler",
                particularites = "Très doux, parfait grillé"
            ),
            VarieteEntity(
                legumeParent = "Poivron",
                nom = "Carré d'Asti",
                description = "Carré, rouge ou jaune, épais.",
                semis = "Février à mars (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à octobre",
                entretien = "Tuteurer, arroser",
                particularites = "Chair épaisse, parfait farci"
            ),
            VarieteEntity(
                legumeParent = "Poivron",
                nom = "Petit Marseillais",
                description = "Petit, rouge, précoce.",
                semis = "Février à mars (sous abri)",
                plantation = "Mai",
                recolte = "Juin à septembre",
                entretien = "Arroser, pailler",
                particularites = "Précoce, parfait pour petits espaces"
            ),
            VarieteEntity(
                legumeParent = "Poivron",
                nom = "Lamuyo",
                description = "Très gros, allongé, rouge ou vert.",
                semis = "Février à mars (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à octobre",
                entretien = "Tuteurer, arroser",
                particularites = "Très gros fruits, productif"
            ),
            VarieteEntity(
                legumeParent = "Poivron",
                nom = "California Wonder",
                description = "Carré, vert puis rouge, classique.",
                semis = "Février à mars (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à octobre",
                entretien = "Pailler, arroser",
                particularites = "Classique, polyvalent"
            ),

            // ===== BETTERAVE =====
            VarieteEntity(
                legumeParent = "Betterave",
                nom = "Rouge de Détroit",
                description = "Ronde, rouge foncé, sucrée.",
                semis = "Avril à juin",
                plantation = "Semis direct",
                recolte = "3 à 4 mois",
                entretien = "Éclaircir, biner",
                particularites = "Classique, sucrée"
            ),
            VarieteEntity(
                legumeParent = "Betterave",
                nom = "Crapaudine",
                description = "Allongée, ancienne, très sucrée.",
                semis = "Avril à juin",
                plantation = "Semis direct",
                recolte = "3 à 4 mois",
                entretien = "Arroser, pailler",
                particularites = "Ancienne, excellent goût"
            ),
            VarieteEntity(
                legumeParent = "Betterave",
                nom = "Chioggia",
                description = "Ronde, rayée blanc et rouge.",
                semis = "Avril à juin",
                plantation = "Semis direct",
                recolte = "3 mois",
                entretien = "Éclaircir, arroser",
                particularites = "Belle couleur, douce"
            ),
            VarieteEntity(
                legumeParent = "Betterave",
                nom = "Jaune Burpee's",
                description = "Jaune doré, douce.",
                semis = "Avril à juin",
                plantation = "Semis direct",
                recolte = "3 mois",
                entretien = "Arroser, biner",
                particularites = "Couleur jaune, douce"
            ),
            VarieteEntity(
                legumeParent = "Betterave",
                nom = "Blanche Albina Veredura",
                description = "Blanche, très sucrée.",
                semis = "Avril à juin",
                plantation = "Semis direct",
                recolte = "3 mois",
                entretien = "Arroser, pailler",
                particularites = "Blanche, sucrée"
            ),

            // ===== CONCOMBRE =====
            VarieteEntity(
                legumeParent = "Concombre",
                nom = "Marketer",
                description = "Long, vert foncé, productif.",
                semis = "Mars à avril (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à septembre",
                entretien = "Tuteurer, arroser",
                particularites = "Classique, productif"
            ),
            VarieteEntity(
                legumeParent = "Concombre",
                nom = "Long de Chine",
                description = "Très long, fin, croquant.",
                semis = "Mars à avril (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à septembre",
                entretien = "Tuteurer, pailler",
                particularites = "Fin, croquant, original"
            ),
            VarieteEntity(
                legumeParent = "Concombre",
                nom = "Noa",
                description = "Court, épineux, croquant.",
                semis = "Mars à avril (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à septembre",
                entretien = "Arroser abondamment",
                particularites = "Croquant, sans amertume"
            ),
            VarieteEntity(
                legumeParent = "Concombre",
                nom = "Lemon",
                description = "Rond, jaune, doux.",
                semis = "Mars à avril (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à septembre",
                entretien = "Tuteurer, arroser",
                particularites = "Rond et jaune, original"
            ),
            VarieteEntity(
                legumeParent = "Concombre",
                nom = "Gynial",
                description = "Sans amertume, productif.",
                semis = "Mars à avril (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à septembre",
                entretien = "Pailler, arroser",
                particularites = "Sans amertume, digeste"
            ),

            // ===== AIL =====
            VarieteEntity(
                legumeParent = "Ail",
                nom = "Blanc de Lomagne",
                description = "Blanc, gros, saveur puissante.",
                semis = "Octobre à novembre",
                plantation = "Octobre à novembre",
                recolte = "Juin à juillet",
                entretien = "Biner, éviter l'excès d'eau",
                particularites = "AOP, gastronomique"
            ),
            VarieteEntity(
                legumeParent = "Ail",
                nom = "Rose de Lautrec",
                description = "Rose, saveur subtile, AOP.",
                semis = "Octobre à novembre",
                plantation = "Octobre à novembre",
                recolte = "Juin à juillet",
                entretien = "Biner, pailler",
                particularites = "AOP, très parfumé"
            ),
            VarieteEntity(
                legumeParent = "Ail",
                nom = "Violet de Cadours",
                description = "Violet, saveur douce.",
                semis = "Octobre à novembre",
                plantation = "Octobre à novembre",
                recolte = "Juin à juillet",
                entretien = "Biner, arroser modérément",
                particularites = "Doux, longue conservation"
            ),
            VarieteEntity(
                legumeParent = "Ail",
                nom = "Germidour",
                description = "Violet, productif, certifié.",
                semis = "Octobre à novembre",
                plantation = "Octobre à novembre",
                recolte = "Juin à juillet",
                entretien = "Biner, pailler",
                particularites = "Productif, bonne conservation"
            ),
            VarieteEntity(
                legumeParent = "Ail",
                nom = "Messidrome",
                description = "Blanc, précoce, productif.",
                semis = "Octobre à novembre",
                plantation = "Octobre à novembre",
                recolte = "Juin",
                entretien = "Biner, arroser modérément",
                particularites = "Précoce, productif"
            ),

            // ===== PETIT POIS =====
            VarieteEntity(
                legumeParent = "Petit pois",
                nom = "Nain très hâtif d'Annonay",
                description = "Nain, précoce, productif.",
                semis = "Février à mai",
                plantation = "Semis direct",
                recolte = "3 mois",
                entretien = "Tuteurer, pailler",
                particularites = "Précoce, idéal petit jardin"
            ),
            VarieteEntity(
                legumeParent = "Petit pois",
                nom = "Téléphone à rames",
                description = "Grimpant, grand, très productif.",
                semis = "Février à mai",
                plantation = "Semis direct",
                recolte = "3 à 4 mois",
                entretien = "Tuteurer solidement",
                particularites = "Très productif, grimpe haut"
            ),
            VarieteEntity(
                legumeParent = "Petit pois",
                nom = "Merveille de Kelvedon",
                description = "Nain, grains ridés, sucrés.",
                semis = "Février à mai",
                plantation = "Semis direct",
                recolte = "3 mois",
                entretien = "Pailler, arroser",
                particularites = "Très sucré, nain"
            ),
            VarieteEntity(
                legumeParent = "Petit pois",
                nom = "Douce Provence",
                description = "Nain, précoce, tendre.",
                semis = "Février à avril",
                plantation = "Semis direct",
                recolte = "2 à 3 mois",
                entretien = "Arroser, pailler",
                particularites = "Précoce, tendre"
            ),
            VarieteEntity(
                legumeParent = "Petit pois",
                nom = "Serpette Guilloteaux",
                description = "Grimpant, ancien, productif.",
                semis = "Février à mai",
                plantation = "Semis direct",
                recolte = "3 à 4 mois",
                entretien = "Tuteurer, arroser",
                particularites = "Ancien, productif"
            ),

            // ===== AUBERGINE =====
            VarieteEntity(
                legumeParent = "Aubergine",
                nom = "Violette de Florence",
                description = "Ronde, violette, précoce.",
                semis = "Février à mars (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à octobre",
                entretien = "Tuteurer, pailler",
                particularites = "Précoce, charnue"
            ),
            VarieteEntity(
                legumeParent = "Aubergine",
                nom = "Barbentane",
                description = "Allongée, violet foncé, productive.",
                semis = "Février à mars (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à octobre",
                entretien = "Arroser, tuteurer",
                particularites = "Productive, classique"
            ),
            VarieteEntity(
                legumeParent = "Aubergine",
                nom = "Ronde de Valence",
                description = "Ronde, violette, douce.",
                semis = "Février à mars (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à octobre",
                entretien = "Pailler, arroser",
                particularites = "Douce, parfaite farcie"
            ),
            VarieteEntity(
                legumeParent = "Aubergine",
                nom = "Blanche",
                description = "Blanche, ronde, douce.",
                semis = "Février à mars (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à octobre",
                entretien = "Tuteurer, arroser",
                particularites = "Originale, douce"
            ),
            VarieteEntity(
                legumeParent = "Aubergine",
                nom = "Dourga",
                description = "Blanche et violette, précoce.",
                semis = "Février à mars (sous abri)",
                plantation = "Mai",
                recolte = "Juillet à septembre",
                entretien = "Arroser, pailler",
                particularites = "Précoce, originale"
            ),

            // ===== CHOU-FLEUR =====
            VarieteEntity(
                legumeParent = "Chou-fleur",
                nom = "Merveille de toutes saisons",
                description = "Polyvalent, tête blanche.",
                semis = "Mars à juin",
                plantation = "Avril à juillet",
                recolte = "3 à 4 mois",
                entretien = "Pailler, arroser",
                particularites = "Polyvalent, productif"
            ),
            VarieteEntity(
                legumeParent = "Chou-fleur",
                nom = "Boule de neige",
                description = "Précoce, tête compacte.",
                semis = "Février à avril",
                plantation = "Mars à mai",
                recolte = "2 à 3 mois",
                entretien = "Arroser régulièrement",
                particularites = "Précoce, compact"
            ),
            VarieteEntity(
                legumeParent = "Chou-fleur",
                nom = "Violet de Sicile",
                description = "Violet, original, doux.",
                semis = "Avril à juin",
                plantation = "Mai à juillet",
                recolte = "3 à 4 mois",
                entretien = "Pailler, arroser",
                particularites = "Couleur violette, doux"
            ),
            VarieteEntity(
                legumeParent = "Chou-fleur",
                nom = "Romanesco",
                description = "Vert, spirales, croquant.",
                semis = "Mai à juillet",
                plantation = "Juin à août",
                recolte = "3 à 4 mois",
                entretien = "Arroser, pailler",
                particularites = "Spirales, croquant, décoratif"
            ),
            VarieteEntity(
                legumeParent = "Chou-fleur",
                nom = "Extra hâtif d'Angers",
                description = "Très précoce, tête blanche.",
                semis = "Janvier à mars (sous abri)",
                plantation = "Février à avril",
                recolte = "2 mois",
                entretien = "Arroser, protéger",
                particularites = "Très précoce"
            ),

            // ===== POTIRON =====
            VarieteEntity(
                legumeParent = "Potiron",
                nom = "Rouge vif d'Étampes",
                description = "Rouge, gros, chair sucrée.",
                semis = "Avril (sous abri)",
                plantation = "Mai",
                recolte = "Septembre à novembre",
                entretien = "Pailler, arroser",
                particularites = "Gros, sucré, décoratif"
            ),
            VarieteEntity(
                legumeParent = "Potiron",
                nom = "Musquée de Provence",
                description = "Bronzé, côtelé, sucré.",
                semis = "Avril (sous abri)",
                plantation = "Mai",
                recolte = "Septembre à novembre",
                entretien = "Pailler, arroser",
                particularites = "Sucré, longue conservation"
            ),
            VarieteEntity(
                legumeParent = "Potiron",
                nom = "Butternut",
                description = "Allongé, beige, doux.",
                semis = "Avril (sous abri)",
                plantation = "Mai",
                recolte = "Septembre à novembre",
                entretien = "Pailler, arroser",
                particularites = "Doux, parfait en velouté"
            ),
            VarieteEntity(
                legumeParent = "Potiron",
                nom = "Potimarron",
                description = "Rouge, petit, goût de châtaigne.",
                semis = "Avril (sous abri)",
                plantation = "Mai",
                recolte = "Septembre à novembre",
                entretien = "Arroser, pailler",
                particularites = "Goût châtaigne, productif"
            ),
            VarieteEntity(
                legumeParent = "Potiron",
                nom = "Galeux d'Eysines",
                description = "Rose, verruqueux, sucré.",
                semis = "Avril (sous abri)",
                plantation = "Mai",
                recolte = "Septembre à novembre",
                entretien = "Pailler, arroser",
                particularites = "Sucré, original"
            ),

            // ===== NAVET =====
            VarieteEntity(
                legumeParent = "Navet",
                nom = "Blanc globe à collet violet",
                description = "Rond, blanc et violet, classique.",
                semis = "Mars à août",
                plantation = "Semis direct",
                recolte = "2 à 3 mois",
                entretien = "Éclaircir, arroser",
                particularites = "Classique, polyvalent"
            ),
            VarieteEntity(
                legumeParent = "Navet",
                nom = "Jaune Boule d'Or",
                description = "Jaune, rond, doux.",
                semis = "Mars à août",
                plantation = "Semis direct",
                recolte = "2 à 3 mois",
                entretien = "Arroser, biner",
                particularites = "Doux, belle couleur"
            ),
            VarieteEntity(
                legumeParent = "Navet",
                nom = "De Nancy",
                description = "Long, blanc, rustique.",
                semis = "Mars à août",
                plantation = "Semis direct",
                recolte = "2 à 3 mois",
                entretien = "Éclaircir, pailler",
                particularites = "Rustique, bonne conservation"
            ),
            VarieteEntity(
                legumeParent = "Navet",
                nom = "Rave d'Auvergne hâtif",
                description = "Précoce, blanc, tendre.",
                semis = "Mars à mai",
                plantation = "Semis direct",
                recolte = "2 mois",
                entretien = "Arroser régulièrement",
                particularites = "Précoce, tendre"
            ),
            VarieteEntity(
                legumeParent = "Navet",
                nom = "Noir de Pardailhan",
                description = "Noir, rustique, sucré.",
                semis = "Juillet à août",
                plantation = "Semis direct",
                recolte = "3 à 4 mois",
                entretien = "Pailler, arroser",
                particularites = "Conservation hivernale, sucré"
            ),

            // ===== BASILIC =====
            VarieteEntity(
                legumeParent = "Basilic",
                nom = "Grand Vert",
                description = "Grandes feuilles vertes, classique.",
                semis = "Mars à mai (sous abri)",
                plantation = "Mai",
                recolte = "Juin à octobre",
                entretien = "Pincer les fleurs",
                particularites = "Le classique du pesto"
            ),
            VarieteEntity(
                legumeParent = "Basilic",
                nom = "Fin Vert Nain",
                description = "Petites feuilles, compact.",
                semis = "Mars à mai (sous abri)",
                plantation = "Mai",
                recolte = "Juin à octobre",
                entretien = "Pincer, arroser",
                particularites = "Compact, idéal pot"
            ),
            VarieteEntity(
                legumeParent = "Basilic",
                nom = "Pourpre",
                description = "Feuilles pourpres, décoratif.",
                semis = "Mars à mai (sous abri)",
                plantation = "Mai",
                recolte = "Juin à octobre",
                entretien = "Pincer, arroser",
                particularites = "Décoratif, parfumé"
            ),
            VarieteEntity(
                legumeParent = "Basilic",
                nom = "Citron",
                description = "Parfum citronné, original.",
                semis = "Mars à mai (sous abri)",
                plantation = "Mai",
                recolte = "Juin à octobre",
                entretien = "Arroser, pincer",
                particularites = "Parfum citron, original"
            ),
            VarieteEntity(
                legumeParent = "Basilic",
                nom = "Thaï",
                description = "Parfum anisé, cuisine asiatique.",
                semis = "Mars à mai (sous abri)",
                plantation = "Mai",
                recolte = "Juin à octobre",
                entretien = "Arroser, pincer",
                particularites = "Cuisine asiatique, anisé"
            ),

            // ===== PERSIL =====
            VarieteEntity(
                legumeParent = "Persil",
                nom = "Commun",
                description = "Feuilles plates, très parfumé.",
                semis = "Mars à août",
                plantation = "Semis direct",
                recolte = "Toute l'année",
                entretien = "Couper régulièrement",
                particularites = "Le plus parfumé"
            ),
            VarieteEntity(
                legumeParent = "Persil",
                nom = "Frisé",
                description = "Feuilles frisées, décoratif.",
                semis = "Mars à août",
                plantation = "Semis direct",
                recolte = "Toute l'année",
                entretien = "Couper, arroser",
                particularites = "Décoratif, croquant"
            ),
            VarieteEntity(
                legumeParent = "Persil",
                nom = "Géant d'Italie",
                description = "Grandes feuilles plates.",
                semis = "Mars à août",
                plantation = "Semis direct",
                recolte = "Toute l'année",
                entretien = "Arroser, couper",
                particularites = "Grandes feuilles, parfumé"
            ),
            VarieteEntity(
                legumeParent = "Persil",
                nom = "Tubéreux",
                description = "Racine comestible, feuilles parfumées.",
                semis = "Mars à mai",
                plantation = "Semis direct",
                recolte = "Automne",
                entretien = "Pailler, arroser",
                particularites = "Racine + feuilles comestibles"
            ),
            VarieteEntity(
                legumeParent = "Persil",
                nom = "Nain",
                description = "Compact, idéal pot.",
                semis = "Mars à août",
                plantation = "Semis direct",
                recolte = "Toute l'année",
                entretien = "Arroser, couper",
                particularites = "Compact, parfait balcon"
            ),

            // ===== THYM =====
            VarieteEntity(
                legumeParent = "Thym",
                nom = "Commun",
                description = "Classique, parfumé.",
                semis = "Mars à mai",
                plantation = "Mars à mai",
                recolte = "Toute l'année",
                entretien = "Tailler après floraison",
                particularites = "Le plus courant"
            ),
            VarieteEntity(
                legumeParent = "Thym",
                nom = "Citron",
                description = "Parfum citronné.",
                semis = "Mars à mai",
                plantation = "Mars à mai",
                recolte = "Toute l'année",
                entretien = "Tailler, arroser modérément",
                particularites = "Parfum citron, original"
            ),
            VarieteEntity(
                legumeParent = "Thym",
                nom = "Orange",
                description = "Parfum orange.",
                semis = "Mars à mai",
                plantation = "Mars à mai",
                recolte = "Toute l'année",
                entretien = "Tailler, pailler",
                particularites = "Parfum orange, doux"
            ),
            VarieteEntity(
                legumeParent = "Thym",
                nom = "Serpolet",
                description = "Sauvage, tapissant.",
                semis = "Mars à mai",
                plantation = "Mars à mai",
                recolte = "Toute l'année",
                entretien = "Tailler légèrement",
                particularites = "Tapissant, parfait couvre-sol"
            ),
            VarieteEntity(
                legumeParent = "Thym",
                nom = "Thym argenté",
                description = "Feuilles panachées, décoratif.",
                semis = "Mars à mai",
                plantation = "Mars à mai",
                recolte = "Toute l'année",
                entretien = "Tailler, arroser peu",
                particularites = "Décoratif, parfumé"
            ),

            // ===== ROMARIN =====
            VarieteEntity(
                legumeParent = "Romarin",
                nom = "Commun",
                description = "Classique, vigoureux.",
                semis = "Mars à mai (plantation)",
                plantation = "Mars à mai",
                recolte = "Toute l'année",
                entretien = "Tailler après floraison",
                particularites = "Le plus courant"
            ),
            VarieteEntity(
                legumeParent = "Romarin",
                nom = "Prostré",
                description = "Rampant, idéal rocaille.",
                semis = "Mars à mai (plantation)",
                plantation = "Mars à mai",
                recolte = "Toute l'année",
                entretien = "Tailler légèrement",
                particularites = "Rampant, décoratif"
            ),
            VarieteEntity(
                legumeParent = "Romarin",
                nom = "Arp",
                description = "Très rustique, résistant au froid.",
                semis = "Mars à mai (plantation)",
                plantation = "Mars à mai",
                recolte = "Toute l'année",
                entretien = "Tailler, pailler",
                particularites = "Résiste au froid intense"
            ),
            VarieteEntity(
                legumeParent = "Romarin",
                nom = "Tuscan Blue",
                description = "Fleurs bleues, dressé.",
                semis = "Mars à mai (plantation)",
                plantation = "Mars à mai",
                recolte = "Toute l'année",
                entretien = "Tailler, arroser peu",
                particularites = "Belles fleurs bleues"
            ),
            VarieteEntity(
                legumeParent = "Romarin",
                nom = "Roseus",
                description = "Fleurs roses, original.",
                semis = "Mars à mai (plantation)",
                plantation = "Mars à mai",
                recolte = "Toute l'année",
                entretien = "Tailler après floraison",
                particularites = "Fleurs roses, décoratif"
            ),

            // ===== MENTHE =====
            VarieteEntity(
                legumeParent = "Menthe",
                nom = "Verte",
                description = "Classique, rafraîchissante.",
                semis = "Mars à mai (plantation)",
                plantation = "Mars à mai",
                recolte = "Avril à octobre",
                entretien = "Limiter l'expansion",
                particularites = "La plus courante"
            ),
            VarieteEntity(
                legumeParent = "Menthe",
                nom = "Poivrée",
                description = "Forte, mentholée.",
                semis = "Mars à mai (plantation)",
                plantation = "Mars à mai",
                recolte = "Avril à octobre",
                entretien = "Limiter l'expansion",
                particularites = "Intense, mentholée"
            ),
            VarieteEntity(
                legumeParent = "Menthe",
                nom = "Citron",
                description = "Parfum citronné, douce.",
                semis = "Mars à mai (plantation)",
                plantation = "Mars à mai",
                recolte = "Avril à octobre",
                entretien = "Tailler, arroser",
                particularites = "Parfum citron, douce"
            ),
            VarieteEntity(
                legumeParent = "Menthe",
                nom = "Chocolat",
                description = "Parfum chocolaté, originale.",
                semis = "Mars à mai (plantation)",
                plantation = "Mars à mai",
                recolte = "Avril à octobre",
                entretien = "Limiter l'expansion",
                particularites = "Parfum chocolat, original"
            ),
            VarieteEntity(
                legumeParent = "Menthe",
                nom = "Marocaine",
                description = "Douce, idéale thé.",
                semis = "Mars à mai (plantation)",
                plantation = "Mars à mai",
                recolte = "Avril à octobre",
                entretien = "Tailler, arroser",
                particularites = "Parfaite pour le thé"
            ),

            // ===== CIBOULETTE =====
            VarieteEntity(
                legumeParent = "Ciboulette",
                nom = "Commune",
                description = "Classique, fine.",
                semis = "Mars à mai",
                plantation = "Mars à mai",
                recolte = "Avril à octobre",
                entretien = "Couper régulièrement",
                particularites = "La plus courante"
            ),
            VarieteEntity(
                legumeParent = "Ciboulette",
                nom = "Ail",
                description = "Goût d'ail, asiatique.",
                semis = "Mars à mai",
                plantation = "Mars à mai",
                recolte = "Avril à octobre",
                entretien = "Couper, arroser",
                particularites = "Goût d'ail, cuisine asiatique"
            ),
            VarieteEntity(
                legumeParent = "Ciboulette",
                nom = "Géante",
                description = "Plus grande, productive.",
                semis = "Mars à mai",
                plantation = "Mars à mai",
                recolte = "Avril à octobre",
                entretien = "Couper régulièrement",
                particularites = "Productive, grande"
            ),
            VarieteEntity(
                legumeParent = "Ciboulette",
                nom = "Fleurs blanches",
                description = "Fleurs blanches, décorative.",
                semis = "Mars à mai",
                plantation = "Mars à mai",
                recolte = "Avril à octobre",
                entretien = "Couper, arroser",
                particularites = "Fleurs blanches décoratives"
            ),
            VarieteEntity(
                legumeParent = "Ciboulette",
                nom = "Sibérienne",
                description = "Très rustique.",
                semis = "Mars à mai",
                plantation = "Mars à mai",
                recolte = "Avril à octobre",
                entretien = "Couper, pailler",
                particularites = "Résiste au froid extrême"
            ),

            // ===== CORIANDRE =====
            VarieteEntity(
                legumeParent = "Coriandre",
                nom = "Commune",
                description = "Classique, parfumée.",
                semis = "Mars à septembre",
                plantation = "Semis direct",
                recolte = "6 à 8 semaines",
                entretien = "Éclaircir, arroser",
                particularites = "La plus courante"
            ),
            VarieteEntity(
                legumeParent = "Coriandre",
                nom = "Slow Bolt",
                description = "Résistante à la montaison.",
                semis = "Mars à septembre",
                plantation = "Semis direct",
                recolte = "6 à 8 semaines",
                entretien = "Arroser, éclaircir",
                particularites = "Ne monte pas vite en graines"
            ),
            VarieteEntity(
                legumeParent = "Coriandre",
                nom = "Marocaine",
                description = "Très parfumée.",
                semis = "Mars à septembre",
                plantation = "Semis direct",
                recolte = "6 à 8 semaines",
                entretien = "Éclaircir, arroser",
                particularites = "Intense, cuisine marocaine"
            ),
            VarieteEntity(
                legumeParent = "Coriandre",
                nom = "Santo",
                description = "Productive, lente à monter.",
                semis = "Mars à septembre",
                plantation = "Semis direct",
                recolte = "6 à 8 semaines",
                entretien = "Arroser, pailler",
                particularites = "Productive, durable"
            ),
            VarieteEntity(
                legumeParent = "Coriandre",
                nom = "Lemon",
                description = "Parfum citronné.",
                semis = "Mars à septembre",
                plantation = "Semis direct",
                recolte = "6 à 8 semaines",
                entretien = "Éclaircir, arroser",
                particularites = "Parfum citron, original"
            ),

            // ===== ANETH =====
            VarieteEntity(
                legumeParent = "Aneth",
                nom = "Commun",
                description = "Classique, parfumé.",
                semis = "Avril à juillet",
                plantation = "Semis direct",
                recolte = "Juin à septembre",
                entretien = "Éclaircir, tuteurer",
                particularites = "Le plus courant"
            ),
            VarieteEntity(
                legumeParent = "Aneth",
                nom = "Bouquet",
                description = "Compact, productif.",
                semis = "Avril à juillet",
                plantation = "Semis direct",
                recolte = "Juin à septembre",
                entretien = "Arroser, éclaircir",
                particularites = "Compact, parfait bouquet"
            ),
            VarieteEntity(
                legumeParent = "Aneth",
                nom = "Fernleaf",
                description = "Nain, idéal pot.",
                semis = "Avril à juillet",
                plantation = "Semis direct",
                recolte = "Juin à septembre",
                entretien = "Arroser modérément",
                particularites = "Nain, parfait balcon"
            ),
            VarieteEntity(
                legumeParent = "Aneth",
                nom = "Mammoth",
                description = "Grand, très productif.",
                semis = "Avril à juillet",
                plantation = "Semis direct",
                recolte = "Juin à septembre",
                entretien = "Tuteurer, arroser",
                particularites = "Grand, productif"
            ),
            VarieteEntity(
                legumeParent = "Aneth",
                nom = "Dukat",
                description = "Très parfumé.",
                semis = "Avril à juillet",
                plantation = "Semis direct",
                recolte = "Juin à septembre",
                entretien = "Éclaircir, arroser",
                particularites = "Intense, parfumé"
            )
        )
    }
}
