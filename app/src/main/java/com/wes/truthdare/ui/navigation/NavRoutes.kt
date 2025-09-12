package com.wes.truthdare.ui.navigation

/**
 * Navigation routes for the app
 */
object NavRoutes {
    const val ONBOARDING = "onboarding"
    const val PLAYERS = "players"
    const val CATEGORIES = "categories"
    const val GAME = "game/{sessionId}"
    const val REFLECT = "reflect/{sessionId}"
    const val SETTINGS = "settings"
    const val REPORT = "report"
    const val REPORT_LIST = "report_list"
    const val REPORT_DETAIL = "report_detail/{reportId}"
    
    // Route with parameters
    fun game(sessionId: String) = "game/$sessionId"
    fun reflect(sessionId: String) = "reflect/$sessionId"
    fun reportDetail(reportId: String) = "report_detail/$reportId"
}