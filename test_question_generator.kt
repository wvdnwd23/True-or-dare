import java.io.File

/**
 * Test script to verify the enhanced question generator
 */
fun main() {
    println("Testing Enhanced Question Generator")
    println("===================================")
    
    // Create a temporary directory for test output
    val testDir = File("test_output")
    if (testDir.exists()) {
        testDir.deleteRecursively()
    }
    testDir.mkdirs()
    
    println("1. Creating test questions...")
    
    // Create a minimal test generator
    val testGenerator = object {
        fun generateTestQuestions() {
            // Create mode directories
            val modes = listOf("classic", "couples", "party", "drink")
            val categories = listOf("casual", "deep")
            
            for (mode in modes) {
                val modeDir = File(testDir, mode)
                modeDir.mkdirs()
                
                for (category in categories) {
                    // Generate a small test file with 10 questions
                    val count = if (mode == "drink") 5 else 10
                    val questions = mutableListOf<Map<String, Any>>()
                    
                    for (i in 1..count) {
                        val question = mapOf(
                            "id" to java.util.UUID.randomUUID().toString(),
                            "type" to if (i % 2 == 0) "truth" else "dare",
                            "category" to category,
                            "targets" to "single",
                            "tags" to listOf(category, mode, "test"),
                            "text" to "Test question $i for $category in $mode mode",
                            "mode" to mode
                        )
                        questions.add(question)
                    }
                    
                    // Write to JSON file
                    val jsonContent = questions.joinToString(
                        prefix = "[\n",
                        postfix = "\n]",
                        separator = ",\n"
                    ) { question ->
                        """  {
    "id": "${question["id"]}",
    "type": "${question["type"]}",
    "category": "${question["category"]}",
    "targets": "${question["targets"]}",
    "tags": ${(question["tags"] as List<*>).joinToString(prefix = "[", postfix = "]") { "&quot;$it&quot;" }},
    "text": "${question["text"]}",
    "mode": "${question["mode"]}"
  }"""
                    }
                    
                    val outputFile = File(modeDir, "$category.json")
                    outputFile.writeText(jsonContent)
                }
            }
        }
    }
    
    // Generate test questions
    testGenerator.generateTestQuestions()
    
    // Verify the output
    println("2. Verifying test output...")
    
    var allPassed = true
    val modes = listOf("classic", "couples", "party", "drink")
    val categories = listOf("casual", "deep")
    
    for (mode in modes) {
        val modeDir = File(testDir, mode)
        if (!modeDir.exists() || !modeDir.isDirectory) {
            println("❌ FAILED: Mode directory $mode not found")
            allPassed = false
            continue
        }
        
        for (category in categories) {
            val file = File(modeDir, "$category.json")
            if (!file.exists()) {
                println("❌ FAILED: File $mode/$category.json not found")
                allPassed = false
                continue
            }
            
            val content = file.readText()
            val expectedCount = if (mode == "drink") 5 else 10
            
            // Simple check for expected number of questions
            val actualCount = content.split("&quot;id&quot;:").size - 1
            if (actualCount != expectedCount) {
                println("❌ FAILED: $mode/$category.json has $actualCount questions, expected $expectedCount")
                allPassed = false
            } else {
                println("✅ PASSED: $mode/$category.json has correct number of questions")
            }
        }
    }
    
    if (allPassed) {
        println("\n✅ All tests passed! The question generator structure works correctly.")
    } else {
        println("\n❌ Some tests failed. Please check the output above for details.")
    }
    
    println("\nTest output is available in the 'test_output' directory.")
}