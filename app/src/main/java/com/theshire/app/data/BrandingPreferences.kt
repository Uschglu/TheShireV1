package com.theshire.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Configuration du branding (personnalisation white-label).
 * 
 * Permet à une jardinerie acheteuse de personnaliser :
 * - Le nom de l'app
 * - Le slogan (optionnel)
 * - Le logo (fichier local ou logo par défaut)
 * - Les couleurs (thème prédéfini ou personnalisé)
 * - Le code PIN de protection de l'écran de configuration
 * 
 * Stockage : SharedPreferences "potager_branding_prefs"
 * 
 * L'objet global `BrandingApp` expose les valeurs courantes de manière
 * observable (Compose) : toute modification déclenche une recomposition.
 */
object BrandingPreferences {
    
    private const val PREFS_NAME = "potager_branding_prefs"
    
    private const val KEY_NOM_APP = "nom_app"
    private const val KEY_SLOGAN = "slogan"
    private const val KEY_LOGO_PATH = "logo_path"
    private const val KEY_THEME_ID = "theme_id"
    private const val KEY_COULEUR_VERT_PRINCIPAL = "couleur_vert_principal"
    private const val KEY_COULEUR_TERRACOTTA = "couleur_terracotta"
    private const val KEY_COULEUR_BRUN_DOUX = "couleur_brun_doux"
    private const val KEY_COULEUR_VERT_CLAIR = "couleur_vert_clair"
    private const val KEY_PIN_CODE = "pin_code"
    
    // ========== VALEURS PAR DÉFAUT ==========
    
    const val NOM_APP_DEFAUT = "Potager Shire"
    const val SLOGAN_DEFAUT = "Votre jardin, guidé toute l'année"
    const val THEME_ID_DEFAUT = "vert"
    const val PIN_CODE_DEFAUT = "1234"
    
    // Couleurs par défaut (thème "vert" = Potager Shire original)
    const val COULEUR_VERT_PRINCIPAL_DEFAUT = "#5B8C5A"
    const val COULEUR_TERRACOTTA_DEFAUT = "#C67B4B"
    const val COULEUR_BRUN_DOUX_DEFAUT = "#8B7355"
    const val COULEUR_VERT_CLAIR_DEFAUT = "#8BC34A"
    
    // ========== THÈMES PRÉDÉFINIS ==========
    
    /**
     * Thème de couleurs prédéfini.
     * 
     * @param id Identifiant technique (utilisé en stockage)
     * @param nom Nom affiché à l'utilisateur
     * @param vertPrincipal Couleur principale (boutons, titres)
     * @param terracotta Couleur d'accent secondaire
     * @param brunDoux Couleur tertiaire (textes secondaires, accents)
     * @param vertClair Couleur claire (highlights, icônes)
     */
    data class ThemeCouleurs(
        val id: String,
        val nom: String,
        val vertPrincipal: String,
        val terracotta: String,
        val brunDoux: String,
        val vertClair: String
    )
    
    val THEMES_PREDEFINIS: List<ThemeCouleurs> = listOf(
        ThemeCouleurs(
            id = "vert",
            nom = "Vert Potager",
            vertPrincipal = "#5B8C5A",
            terracotta = "#C67B4B",
            brunDoux = "#8B7355",
            vertClair = "#8BC34A"
        ),
        ThemeCouleurs(
            id = "orange",
            nom = "Orange Automne",
            vertPrincipal = "#E67E22",
            terracotta = "#D35400",
            brunDoux = "#A0522D",
            vertClair = "#F39C12"
        ),
        ThemeCouleurs(
            id = "bleu",
            nom = "Bleu Jardin",
            vertPrincipal = "#2E86AB",
            terracotta = "#1C5F7A",
            brunDoux = "#4A6572",
            vertClair = "#5DADE2"
        ),
        ThemeCouleurs(
            id = "rouge",
            nom = "Rouge Terre",
            vertPrincipal = "#C0392B",
            terracotta = "#96281B",
            brunDoux = "#7B241C",
            vertClair = "#E74C3C"
        ),
        ThemeCouleurs(
            id = "violet",
            nom = "Violet Lavande",
            vertPrincipal = "#8E44AD",
            terracotta = "#6C3483",
            brunDoux = "#5B2C6F",
            vertClair = "#BB8FCE"
        )
    )
    
    fun getThemeParId(id: String): ThemeCouleurs {
        return THEMES_PREDEFINIS.find { it.id == id } ?: THEMES_PREDEFINIS[0]
    }
    
    // ========== STRUCTURE DE CONFIGURATION ==========
    
    /**
     * Configuration complète du branding.
     * 
     * @param nomApp Nom affiché dans l'app
     * @param slogan Slogan (optionnel, peut être vide)
     * @param logoPath Chemin de fichier local pour le logo (null = logo par défaut res/drawable)
     * @param themeId Identifiant du thème ("vert", "orange", "bleu", "rouge", "violet", "custom")
     * @param couleurVertPrincipal Couleur hex personnalisée (utilisée seulement si themeId == "custom")
     * @param couleurTerracotta Couleur hex personnalisée
     * @param couleurBrunDoux Couleur hex personnalisée
     * @param couleurVertClair Couleur hex personnalisée
     * @param pinCode Code PIN de protection de l'écran config (4 chiffres)
     */
    data class BrandingConfig(
        val nomApp: String = NOM_APP_DEFAUT,
        val slogan: String = SLOGAN_DEFAUT,
        val logoPath: String? = null,
        val themeId: String = THEME_ID_DEFAUT,
        val couleurVertPrincipal: String = COULEUR_VERT_PRINCIPAL_DEFAUT,
        val couleurTerracotta: String = COULEUR_TERRACOTTA_DEFAUT,
        val couleurBrunDoux: String = COULEUR_BRUN_DOUX_DEFAUT,
        val couleurVertClair: String = COULEUR_VERT_CLAIR_DEFAUT,
        val pinCode: String = PIN_CODE_DEFAUT
    )
    
    // ========== LECTURE ==========
    
    /**
     * Charge la configuration complète.
     * Si des valeurs manquent, utilise les défauts.
     */
    fun chargerConfig(context: Context): BrandingConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return BrandingConfig(
            nomApp = prefs.getString(KEY_NOM_APP, NOM_APP_DEFAUT) ?: NOM_APP_DEFAUT,
            slogan = prefs.getString(KEY_SLOGAN, SLOGAN_DEFAUT) ?: SLOGAN_DEFAUT,
            logoPath = prefs.getString(KEY_LOGO_PATH, null),
            themeId = prefs.getString(KEY_THEME_ID, THEME_ID_DEFAUT) ?: THEME_ID_DEFAUT,
            couleurVertPrincipal = prefs.getString(KEY_COULEUR_VERT_PRINCIPAL, COULEUR_VERT_PRINCIPAL_DEFAUT) ?: COULEUR_VERT_PRINCIPAL_DEFAUT,
            couleurTerracotta = prefs.getString(KEY_COULEUR_TERRACOTTA, COULEUR_TERRACOTTA_DEFAUT) ?: COULEUR_TERRACOTTA_DEFAUT,
            couleurBrunDoux = prefs.getString(KEY_COULEUR_BRUN_DOUX, COULEUR_BRUN_DOUX_DEFAUT) ?: COULEUR_BRUN_DOUX_DEFAUT,
            couleurVertClair = prefs.getString(KEY_COULEUR_VERT_CLAIR, COULEUR_VERT_CLAIR_DEFAUT) ?: COULEUR_VERT_CLAIR_DEFAUT,
            pinCode = prefs.getString(KEY_PIN_CODE, PIN_CODE_DEFAUT) ?: PIN_CODE_DEFAUT
        )
    }
    
    // ========== ÉCRITURE ==========
    
    /**
     * Sauvegarde la configuration complète.
     * Écrase TOUTES les valeurs existantes (utiliser les setters individuels pour modifier partiellement).
     */
    fun sauvegarderConfig(context: Context, config: BrandingConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_NOM_APP, config.nomApp)
            .putString(KEY_SLOGAN, config.slogan)
            .putString(KEY_LOGO_PATH, config.logoPath)
            .putString(KEY_THEME_ID, config.themeId)
            .putString(KEY_COULEUR_VERT_PRINCIPAL, config.couleurVertPrincipal)
            .putString(KEY_COULEUR_TERRACOTTA, config.couleurTerracotta)
            .putString(KEY_COULEUR_BRUN_DOUX, config.couleurBrunDoux)
            .putString(KEY_COULEUR_VERT_CLAIR, config.couleurVertClair)
            .putString(KEY_PIN_CODE, config.pinCode)
            .apply()
    }
    
    // ========== SETTERS INDIVIDUELS (plus pratiques) ==========
    
    fun setNomApp(context: Context, nom: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NOM_APP, nom).apply()
    }
    
    fun setSlogan(context: Context, slogan: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SLOGAN, slogan).apply()
    }
    
    fun setLogoPath(context: Context, path: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (path == null) {
            prefs.edit().remove(KEY_LOGO_PATH).apply()
        } else {
            prefs.edit().putString(KEY_LOGO_PATH, path).apply()
        }
    }
    
    /**
     * Applique un thème prédéfini et met à jour les 4 couleurs en conséquence.
     * Ne touche PAS aux couleurs personnalisées si themeId == "custom".
     */
    fun setTheme(context: Context, themeId: String) {
        if (themeId == "custom") {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_THEME_ID, "custom").apply()
            return
        }
        
        val theme = getThemeParId(themeId)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_THEME_ID, theme.id)
            .putString(KEY_COULEUR_VERT_PRINCIPAL, theme.vertPrincipal)
            .putString(KEY_COULEUR_TERRACOTTA, theme.terracotta)
            .putString(KEY_COULEUR_BRUN_DOUX, theme.brunDoux)
            .putString(KEY_COULEUR_VERT_CLAIR, theme.vertClair)
            .apply()
    }
    
    fun setCouleurVertPrincipal(context: Context, hex: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_COULEUR_VERT_PRINCIPAL, hex)
            .putString(KEY_THEME_ID, "custom")
            .apply()
    }
    
    fun setCouleurTerracotta(context: Context, hex: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_COULEUR_TERRACOTTA, hex)
            .putString(KEY_THEME_ID, "custom")
            .apply()
    }
    
    fun setCouleurBrunDoux(context: Context, hex: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_COULEUR_BRUN_DOUX, hex)
            .putString(KEY_THEME_ID, "custom")
            .apply()
    }
    
    fun setCouleurVertClair(context: Context, hex: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_COULEUR_VERT_CLAIR, hex)
            .putString(KEY_THEME_ID, "custom")
            .apply()
    }
    
    fun setPinCode(context: Context, pin: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PIN_CODE, pin).apply()
    }
    
    // ========== VÉRIFICATION DU PIN ==========
    
    fun verifierPin(context: Context, pin: String): Boolean {
        val pinEnregistre = chargerConfig(context).pinCode
        return pin == pinEnregistre
    }
    
    // ========== RÉINITIALISATION ==========
    
    /**
     * Réinitialise TOUTE la configuration aux valeurs par défaut.
     * Utile pour tester ou pour repartir de zéro.
     */
    fun reinitialiser(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

/**
 * État observable du branding pour Compose.
 * 
 * Chargé UNE FOIS au démarrage via `BrandingApp.initialiser(context)`.
 * Toute modification via `BrandingApp.recharger(context)` déclenche
 * une recomposition de tous les composables qui lisent ses propriétés.
 */
object BrandingApp {
    
    /** Configuration actuelle, observable par Compose. */
    var config: BrandingPreferences.BrandingConfig by mutableStateOf(
        BrandingPreferences.BrandingConfig()
    )
        private set
    
    /**
     * Charge la configuration depuis SharedPreferences.
     * À appeler dans MainActivity.onCreate() avant setContent {}.
     */
    fun initialiser(context: Context) {
        config = BrandingPreferences.chargerConfig(context)
    }
    
    /**
     * Recharge la configuration après une modification.
     * À appeler après chaque setter pour rafraîchir l'UI.
     */
    fun recharger(context: Context) {
        config = BrandingPreferences.chargerConfig(context)
    }
}
