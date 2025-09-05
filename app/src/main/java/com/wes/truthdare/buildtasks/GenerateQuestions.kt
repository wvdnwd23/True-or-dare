package com.wes.truthdare.buildtasks

import java.io.File

/**
 * Helper script to generate question JSON files
 */
fun main() {
    val outDir = File("app/src/main/assets/questions")
    outDir.mkdirs()
    
    val engine = QuestionTemplateEngine()
    engine.generateAllQuestionPacks(outDir)
    
    println("Generated question packs in $outDir")
}