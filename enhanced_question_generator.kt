package com.wes.truthdare.buildtasks

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.math.min

/**
 * Enhanced engine for generating question packs with more questions
 */
class EnhancedQuestionTemplateEngine {
    companion object {
        // Categories for questions
        private val CATEGORIES = listOf(
            "casual", "party", "deep", "romantic", "family", "friends",
            "funny", "challenge", "personal", "childhood", "future", "hypothetical"
        )
        
        // Game modes
        private val GAME_MODES = listOf(
            "classic", "couples", "party", "drink"
        )
        
        // Question types
        private val QUESTION_TYPES = mapOf(
            "casual" to Pair(0.6, 0.4),      // 60% truth, 40% dare
            "party" to Pair(0.4, 0.6),       // 40% truth, 60% dare
            "deep" to Pair(0.9, 0.1),        // 90% truth, 10% dare
            "romantic" to Pair(0.7, 0.3),    // 70% truth, 30% dare
            "family" to Pair(0.8, 0.2),      // 80% truth, 20% dare
            "friends" to Pair(0.5, 0.5),     // 50% truth, 50% dare
            "funny" to Pair(0.3, 0.7),       // 30% truth, 70% dare
            "challenge" to Pair(0.2, 0.8),   // 20% truth, 80% dare
            "personal" to Pair(0.9, 0.1),    // 90% truth, 10% dare
            "childhood" to Pair(0.8, 0.2),   // 80% truth, 20% dare
            "future" to Pair(0.7, 0.3),      // 70% truth, 30% dare
            "hypothetical" to Pair(0.6, 0.4) // 60% truth, 40% dare
        )
        
        // Tags for questions
        private val TAGS = mapOf(
            "casual" to listOf("alledaags", "licht", "simpel", "dagelijks", "gewoon"),
            "party" to listOf("feest", "plezier", "sociaal", "groep", "spel"),
            "deep" to listOf("diep", "filosofisch", "betekenisvol", "reflectie", "inzicht"),
            "romantic" to listOf("romantisch", "liefde", "relatie", "intimiteit", "passie"),
            "family" to listOf("familie", "gezin", "ouders", "broers", "zussen"),
            "friends" to listOf("vrienden", "vriendschap", "vertrouwen", "loyaliteit", "band"),
            "funny" to listOf("grappig", "humor", "lachen", "grap", "komisch"),
            "challenge" to listOf("uitdaging", "moeilijk", "durf", "lef", "moed"),
            "personal" to listOf("persoonlijk", "privé", "intiem", "eigen", "zelf"),
            "childhood" to listOf("jeugd", "kindertijd", "vroeger", "herinnering", "nostalgie"),
            "future" to listOf("toekomst", "plan", "droom", "ambitie", "hoop"),
            "hypothetical" to listOf("hypothetisch", "wat-als", "scenario", "denkbeeldig", "fantasie")
        )
        
        // Additional tags for game modes
        private val MODE_TAGS = mapOf(
            "classic" to listOf("klassiek", "traditioneel", "standaard", "basis", "normaal"),
            "couples" to listOf("koppel", "relatie", "partner", "liefde", "samen"),
            "party" to listOf("feest", "groep", "sociaal", "plezier", "spel"),
            "drink" to listOf("drank", "alcohol", "shots", "spel", "uitdaging")
        )
        
        // Depth levels for deep talk questions
        private val DEPTH_LEVELS = mapOf(
            "casual" to null,
            "party" to null,
            "deep" to (1..5),
            "romantic" to (1..4),
            "family" to (1..3),
            "friends" to (1..3),
            "funny" to null,
            "challenge" to null,
            "personal" to (1..4),
            "childhood" to (1..3),
            "future" to (1..4),
            "hypothetical" to (1..3)
        )
        
        // Target types
        private val TARGETS = listOf("single", "group", "pair", "self")
        
        // Drink-specific templates
        private val DRINK_TRUTH_TEMPLATES = listOf(
            "Wanneer was de laatste keer dat je {ACTIVITEIT} deed terwijl je dronken was?",
            "Wat is je {BIJVOEGLIJK} drankje en waarom?",
            "Wat is het {BIJVOEGLIJK} dat je ooit hebt gedaan onder invloed?",
            "Wie zou je het liefst zien {ACTIVITEIT} van de mensen hier?",
            "Wat is je {BIJVOEGLIJK} drinkspel?",
            "Heb je ooit spijt gehad van iets dat je deed terwijl je dronken was?",
            "Wat is het {BIJVOEGLIJK} excuus dat je ooit hebt gebruikt om niet te drinken?",
            "Wie is volgens jou de {BIJVOEGLIJK} drinker in deze groep?",
            "Wat is je {BIJVOEGLIJK} herinnering aan een avond uit?",
            "Als je één drankje voor de rest van je leven moest kiezen, wat zou het zijn?"
        )
        
        private val DRINK_DARE_TEMPLATES = listOf(
            "Neem een shot {DING} (alcoholvrij mag ook).",
            "Doe een {BIJVOEGLIJK} dansmove of neem een shot.",
            "Vertel een {BIJVOEGLIJK} verhaal over de persoon rechts van je of neem een shot.",
            "Doe een {BIJVOEGLIJK} imitatie van een beroemdheid of neem een shot.",
            "Laat iemand een {BIJVOEGLIJK} bericht sturen vanaf je telefoon of neem een shot.",
            "Doe een shotje met de persoon tegenover je.",
            "Vertel je {BIJVOEGLIJK} dronken verhaal of neem een shot.",
            "Doe een handstand tegen de muur of neem een shot.",
            "Zing het refrein van je {BIJVOEGLIJK} lied of neem een shot.",
            "Wissel van drankje met de persoon links van je."
        )
    }
    
    // Random number generator
    private val random = Random()
    
    /**
     * Generate all question packs for all game modes
     * @param outDir The output directory for the generated JSON files
     */
    fun generateAllQuestionPacks(outDir: File) {
        // Create mode-specific directories
        for (mode in GAME_MODES) {
            val modeDir = File(outDir, mode)
            modeDir.mkdirs()
            
            // Generate questions for each category in this mode
            for (category in CATEGORIES) {
                val count = if (mode == "drink") 100 else 1000
                val questions = generateQuestionsForCategoryAndMode(category, mode, count)
                saveQuestionsToJson(questions, File(modeDir, "$category.json"))
                println("Generated $count questions for $category in $mode mode")
            }
        }
    }
    
    /**
     * Generate questions for a specific category and game mode
     * @param category The category to generate questions for
     * @param mode The game mode
     * @param count The number of questions to generate
     * @return A list of generated questions
     */
    private fun generateQuestionsForCategoryAndMode(category: String, mode: String, count: Int): List<Question> {
        val (truthRatio, dareRatio) = QUESTION_TYPES[category] ?: Pair(0.5, 0.5)
        val truthCount = (count * truthRatio).toInt()
        val dareCount = count - truthCount
        
        val questions = mutableListOf<Question>()
        
        // Generate truth questions
        questions.addAll(generateQuestionsByTypeAndMode(category, mode, "truth", truthCount))
        
        // Generate dare questions
        questions.addAll(generateQuestionsByTypeAndMode(category, mode, "dare", dareCount))
        
        return questions
    }
    
    /**
     * Generate questions of a specific type for a category and mode
     * @param category The category to generate questions for
     * @param mode The game mode
     * @param type The type of questions to generate ("truth" or "dare")
     * @param count The number of questions to generate
     * @return A list of generated questions
     */
    private fun generateQuestionsByTypeAndMode(category: String, mode: String, type: String, count: Int): List<Question> {
        val questions = mutableListOf<Question>()
        val templates = getTemplatesForTypeAndMode(type, category, mode)
        val synonyms = getSynonymsForCategoryAndMode(category, mode)
        
        // Generate unique questions
        val generatedTexts = mutableSetOf<String>()
        var attempts = 0
        val maxAttempts = count * 10 // Limit attempts to avoid infinite loops
        
        while (questions.size < count && attempts < maxAttempts) {
            attempts++
            
            // Select a random template
            val template = templates.random()
            
            // Replace placeholders with synonyms
            val text = replacePlaceholders(template, synonyms)
            
            // Ensure the question is unique and not too long
            if (text.length <= 100 && text.split(" ").size <= 18 && !generatedTexts.contains(text)) {
                generatedTexts.add(text)
                
                // Create question object
                val question = Question(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    category = category,
                    targets = TARGETS.random(),
                    depthLevel = if (DEPTH_LEVELS[category] != null) {
                        DEPTH_LEVELS[category]!!.random()
                    } else {
                        null
                    },
                    tags = getTagsForQuestionAndMode(category, mode),
                    text = text,
                    mode = mode
                )
                
                questions.add(question)
            }
        }
        
        // If we couldn't generate enough unique questions, fill with variations
        if (questions.size < count) {
            val remaining = count - questions.size
            for (i in 0 until remaining) {
                // Create a variation of an existing question
                val baseQuestion = questions[i % questions.size]
                val variation = baseQuestion.copy(
                    id = UUID.randomUUID().toString(),
                    text = createVariation(baseQuestion.text, synonyms)
                )
                questions.add(variation)
            }
        }
        
        return questions
    }
    
    /**
     * Get templates for a specific question type, category, and mode
     * @param type The question type ("truth" or "dare")
     * @param category The question category
     * @param mode The game mode
     * @return A list of templates
     */
    private fun getTemplatesForTypeAndMode(type: String, category: String, mode: String): List<String> {
        // For drink mode, use drink-specific templates
        if (mode == "drink") {
            return when (type) {
                "truth" -> DRINK_TRUTH_TEMPLATES
                "dare" -> DRINK_DARE_TEMPLATES
                else -> emptyList()
            }
        }
        
        // For other modes, use category-specific templates
        return when {
            type == "truth" && category == "casual" -> listOf(
                "Wat is je favoriete {HOBBY} en waarom?",
                "Wanneer heb je voor het laatst {ACTIVITEIT} gedaan?",
                "Wat is het {BIJVOEGLIJK} ding dat je ooit hebt gedaan?",
                "Als je één {DING} kon veranderen aan jezelf, wat zou dat zijn?",
                "Wat is je {BIJVOEGLIJK} herinnering van de middelbare school?",
                "Welk {DING} zou je meenemen naar een onbewoond eiland?",
                "Wat is je {BIJVOEGLIJK} film aller tijden?",
                "Wanneer heb je voor het laatst {EMOTIE} gehad?",
                "Wat is je {BIJVOEGLIJK} vakantiebestemming?",
                "Als je één {PERSOON} kon ontmoeten, wie zou dat zijn?"
            )
            type == "truth" && category == "party" -> listOf(
                "Wat is het {BIJVOEGLIJK} feestje waar je ooit bent geweest?",
                "Heb je ooit {ACTIVITEIT} gedaan op een feestje?",
                "Wie zou je {ACTIVITEIT} willen zien doen van de mensen hier?",
                "Wat is je {BIJVOEGLIJK} drankje?",
                "Heb je ooit {ACTIVITEIT} gedaan terwijl je dronken was?",
                "Wat is het {BIJVOEGLIJK} excuus dat je ooit hebt gebruikt om een feestje te verlaten?",
                "Wie is de {BIJVOEGLIJK} persoon in deze ruimte?",
                "Wat is je {BIJVOEGLIJK} dansmove?",
                "Wanneer was je voor het laatst {EMOTIE} op een feestje?",
                "Als je iedereen hier kon laten {ACTIVITEIT}, wie zou je kiezen?"
            )
            type == "truth" && category == "deep" -> listOf(
                "Wat is je {BIJVOEGLIJK} angst in het leven?",
                "Wanneer voelde je je het meest {EMOTIE} in je leven?",
                "Welke {PERSOON} heeft de grootste invloed gehad op wie je nu bent?",
                "Wat is iets waar je {EMOTIE} over bent maar nooit over praat?",
                "Als je nog één dag te leven had, hoe zou je die doorbrengen?",
                "Wat is een {BIJVOEGLIJK} les die je hebt geleerd uit een moeilijke situatie?",
                "Wanneer heb je voor het laatst gehuild en waarom?",
                "Wat is iets waar je {EMOTIE} over bent maar waar anderen je om bekritiseren?",
                "Wat zou je willen dat mensen beter begrepen over jou?",
                "Welke {BIJVOEGLIJK} vraag zou je willen dat iemand je stelde?"
            )
            // Add more templates for other combinations...
            type == "dare" && category == "casual" -> listOf(
                "Doe een imitatie van {PERSOON} tot iemand raadt wie het is.",
                "Stuur een {BIJVOEGLIJK} bericht naar de {PERSOON} die je als laatste hebt gesproken.",
                "Doe {ACTIVITEIT} voor 30 seconden.",
                "Laat de groep je {DING} kiezen voor je profielfoto voor een dag.",
                "Vertel een {BIJVOEGLIJK} grap.",
                "Doe alsof je {PERSOON} bent voor de volgende 2 minuten.",
                "Laat iemand je haar {BIJVOEGLIJK} stylen.",
                "Zing het refrein van je {BIJVOEGLIJK} lied.",
                "Doe een {BIJVOEGLIJK} dans voor 30 seconden.",
                "Laat iemand een {BIJVOEGLIJK} tekening op je arm maken."
            )
            type == "dare" && category == "party" -> listOf(
                "Doe een {BIJVOEGLIJK} dansmove en laat iedereen het nadoen.",
                "Doe een shotje {DING} (alcoholvrij mag ook).",
                "Laat iemand je make-up doen met je ogen dicht.",
                "Doe {ACTIVITEIT} met de persoon links van je.",
                "Vertel een {BIJVOEGLIJK} verhaal over de persoon tegenover je.",
                "Doe een {BIJVOEGLIJK} imitatie van een beroemdheid.",
                "Laat iemand je telefoon ontgrendelen en een {BIJVOEGLIJK} bericht sturen naar wie zij kiezen.",
                "Doe een {BIJVOEGLIJK} catwalk door de kamer.",
                "Doe een {BIJVOEGLIJK} accent voor de rest van je beurt.",
                "Laat iemand een {BIJVOEGLIJK} foto van je maken en plaats deze op social media."
            )
            // Default templates for other combinations
            else -> listOf(
                "Wat vind je van {DING}?",
                "Heb je ooit {ACTIVITEIT} gedaan?",
                "Wat zou je doen als {SCENARIO}?",
                "Wie is je favoriete {PERSOON}?",
                "Doe {ACTIVITEIT} voor 30 seconden.",
                "Vertel een verhaal over {DING}.",
                "Deel een {BIJVOEGLIJK} herinnering.",
                "Wat is je mening over {DING}?",
                "Doe alsof je {PERSOON} bent.",
                "Wanneer was de laatste keer dat je {ACTIVITEIT} deed?"
            )
        }
    }
    
    /**
     * Get synonyms for placeholders based on category and mode
     * @param category The question category
     * @param mode The game mode
     * @return A map of placeholder types to lists of possible values
     */
    private fun getSynonymsForCategoryAndMode(category: String, mode: String): Map<String, List<String>> {
        val commonSynonyms = mapOf(
            "BIJVOEGLIJK" to listOf("leukste", "grappigste", "gekste", "mooiste", "beste", "slechtste", "vreemdste", "interessantste", "verrassendste", "meest memorabele"),
            "EMOTIE" to listOf("blij", "verdrietig", "boos", "bang", "verrast", "teleurgesteld", "trots", "jaloers", "geïrriteerd", "opgewonden"),
            "PERSOON" to listOf("vriend", "familielid", "collega", "bekende", "beroemdheid", "leraar", "buurman", "ex", "crush", "baas"),
            "DING" to listOf("boek", "film", "lied", "eten", "drank", "hobby", "sport", "app", "website", "gadget"),
            "ACTIVITEIT" to listOf("dansen", "zingen", "sporten", "koken", "reizen", "gamen", "lezen", "schrijven", "tekenen", "zwemmen"),
            "SCENARIO" to listOf("je won de loterij", "je kon vliegen", "je was onzichtbaar", "je kon gedachten lezen", "je had één dag te leven", "je kon tijdreizen", "je was beroemd", "je was het andere geslacht", "je was 10 jaar jonger", "je was de baas van het land")
        )
        
        // Add mode-specific synonyms
        val modeSpecificSynonyms = when (mode) {
            "drink" -> mapOf(
                "ACTIVITEIT" to listOf("dansen", "drinken", "flirten", "zingen", "spelletjes spelen", "feesten", "kletsen", "lachen", "gek doen", "selfies maken"),
                "DING" to listOf("bier", "wijn", "cocktail", "shot", "mixdrankje", "sterke drank", "shotglas", "drinkspel", "kater", "bar"),
                "BIJVOEGLIJK" to listOf("wildste", "gekste", "meest beschamende", "grappigste", "meest gênante", "stoutste", "meest onverwachte", "meest memorabele", "meest hilarische", "meest bizarre")
            )
            "couples" -> mapOf(
                "PERSOON" to listOf("partner", "ex", "crush", "date", "eerste liefde", "schoonouder", "schoonfamilie", "vriend", "vriendin", "geliefde"),
                "ACTIVITEIT" to listOf("kussen", "knuffelen", "daten", "flirten", "samenwonen", "reizen", "koken", "slapen", "douchen", "ontbijten"),
                "EMOTIE" to listOf("verliefd", "jaloers", "gepassioneerd", "geïrriteerd", "gelukkig", "vertrouwd", "veilig", "kwetsbaar", "gewaardeerd", "geliefd")
            )
            "party" -> mapOf(
                "ACTIVITEIT" to listOf("dansen", "drinken", "flirten", "zingen", "spelletjes spelen", "feesten", "kletsen", "lachen", "gek doen", "selfies maken"),
                "DING" to listOf("drankje", "feestoutfit", "dansmove", "feesthoed", "shotje", "partytruc", "feestlied", "spelletje", "feestsnack", "feestlocatie")
            )
            else -> mapOf()
        }
        
        // Add category-specific synonyms
        val categorySpecificSynonyms = when (category) {
            "casual" -> mapOf<String, List<String>>()
            "party" -> mapOf(
                "ACTIVITEIT" to listOf("dansen", "drinken", "flirten", "zingen", "spelletjes spelen", "feesten", "kletsen", "lachen", "gek doen", "selfies maken"),
                "DING" to listOf("drankje", "feestoutfit", "dansmove", "feesthoed", "shotje", "partytruc", "feestlied", "spelletje", "feestsnack", "feestlocatie")
            )
            "deep" -> mapOf(
                "BIJVOEGLIJK" to listOf("diepste", "meest betekenisvolle", "meest invloedrijke", "meest emotionele", "meest waardevolle", "meest leerzame", "meest confronterende", "meest verhelderende", "meest transformerende", "meest intieme"),
                "EMOTIE" to listOf("kwetsbaar", "onzeker", "dankbaar", "schuldig", "hoopvol", "verloren", "vervuld", "leeg", "geïnspireerd", "overweldigd")
            )
            // Add more category-specific synonyms...
            else -> mapOf()
        }
        
        // Combine all synonyms, with mode-specific ones taking precedence
        return commonSynonyms + categorySpecificSynonyms + modeSpecificSynonyms
    }
    
    /**
     * Replace placeholders in a template with random synonyms
     * @param template The template string with placeholders
     * @param synonyms A map of placeholder types to lists of possible values
     * @return The template with placeholders replaced
     */
    private fun replacePlaceholders(template: String, synonyms: Map<String, List<String>>): String {
        var result = template
        
        // Find all placeholders in the template
        val placeholderRegex = Regex("\\{([A-Z]+)\\}")
        val matches = placeholderRegex.findAll(template)
        
        for (match in matches) {
            val placeholder = match.groupValues[1]
            val replacements = synonyms[placeholder]
            
            if (replacements != null) {
                val replacement = replacements.random()
                result = result.replace("{$placeholder}", replacement)
            }
        }
        
        return result
    }
    
    /**
     * Create a variation of a question by replacing some words with synonyms
     * @param text The original question text
     * @param synonyms A map of placeholder types to lists of possible values
     * @return A variation of the original question
     */
    private fun createVariation(text: String, synonyms: Map<String, List<String>>): String {
        val words = text.split(" ").toMutableList()
        
        // Replace 1-3 words with synonyms
        val replacementCount = random.nextInt(3) + 1
        for (i in 0 until min(replacementCount, words.size)) {
            val index = random.nextInt(words.size)
            val word = words[index]
            
            // Try to find a synonym for this word
            for ((_, synonymList) in synonyms) {
                if (synonymList.contains(word.lowercase().replace("[,.?!]".toRegex(), ""))) {
                    words[index] = synonymList.random() + if (word.endsWith(".") || word.endsWith(",") || word.endsWith("?") || word.endsWith("!")) {
                        word.last()
                    } else {
                        ""
                    }
                    break
                }
            }
        }
        
        return words.joinToString(" ")
    }
    
    /**
     * Get tags for a question based on its category and mode
     * @param category The question category
     * @param mode The game mode
     * @return A list of tags
     */
    private fun getTagsForQuestionAndMode(category: String, mode: String): List<String> {
        val result = mutableListOf<String>()
        
        // Always include the category itself as a tag
        result.add(category)
        
        // Add the mode as a tag
        result.add(mode)
        
        // Add 1-2 additional tags from the category's tag list
        val categoryTags = TAGS[category] ?: listOf()
        val additionalCategoryTagCount = random.nextInt(2) + 1
        val shuffledCategoryTags = categoryTags.shuffled()
        for (i in 0 until min(additionalCategoryTagCount, shuffledCategoryTags.size)) {
            result.add(shuffledCategoryTags[i])
        }
        
        // Add 1-2 additional tags from the mode's tag list
        val modeTags = MODE_TAGS[mode] ?: listOf()
        val additionalModeTagCount = random.nextInt(2) + 1
        val shuffledModeTags = modeTags.shuffled()
        for (i in 0 until min(additionalModeTagCount, shuffledModeTags.size)) {
            result.add(shuffledModeTags[i])
        }
        
        return result.distinct()
    }
    
    /**
     * Save questions to a JSON file
     * @param questions The list of questions to save
     * @param file The output file
     */
    private fun saveQuestionsToJson(questions: List<Question>, file: File) {
        val jsonArray = JSONArray()
        
        for (question in questions) {
            val jsonObject = JSONObject()
            jsonObject.put("id", question.id)
            jsonObject.put("type", question.type)
            jsonObject.put("category", question.category)
            jsonObject.put("targets", question.targets)
            if (question.depthLevel != null) {
                jsonObject.put("depthLevel", question.depthLevel)
            }
            
            val tagsArray = JSONArray()
            for (tag in question.tags) {
                tagsArray.put(tag)
            }
            jsonObject.put("tags", tagsArray)
            
            jsonObject.put("text", question.text)
            jsonObject.put("mode", question.mode)
            
            jsonArray.put(jsonObject)
        }
        
        file.writeText(jsonArray.toString())
    }
    
    /**
     * Data class representing a question
     */
    data class Question(
        val id: String,
        val type: String,
        val category: String,
        val targets: String,
        val depthLevel: Int?,
        val tags: List<String>,
        val text: String,
        val mode: String
    )
}

/**
 * Main function to generate question packs
 */
fun main() {
    val outDir = File("app/src/main/assets/questions/modes")
    outDir.mkdirs()
    
    val engine = EnhancedQuestionTemplateEngine()
    engine.generateAllQuestionPacks(outDir)
    
    println("Generated question packs in $outDir")
}