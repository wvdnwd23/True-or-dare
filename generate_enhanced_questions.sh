#!/bin/bash

# Make sure the output directory exists
mkdir -p app/src/main/assets/questions/modes

# Compile and run the enhanced question generator
kotlinc -script enhanced_question_generator.kt

echo "Question generation complete!"
echo "Generated questions can be found in app/src/main/assets/questions/modes/"