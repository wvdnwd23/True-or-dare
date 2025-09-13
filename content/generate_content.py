#!/usr/bin/env python3
import os
import json
import sys
from content_generator import ContentGenerator

def generate_soft_category(generator):
    """Generate content for the Soft category"""
    print("Generating Soft category content...")
    
    # Sub-option: Icebreaker
    icebreaker_truths = [
        {"category": "soft", "subOptions": ["icebreaker"], "type": "truth", "text": "What's your favorite way to spend a lazy Sunday?", "intensity": "soft"},
        {"category": "soft", "subOptions": ["icebreaker"], "type": "truth", "text": "If you could have any animal as a pet, what would it be and why?", "intensity": "soft"},
        {"category": "soft", "subOptions": ["icebreaker"], "type": "truth", "text": "What's your go-to karaoke song?", "intensity": "soft"},
        {"category": "soft", "subOptions": ["icebreaker"], "type": "truth", "text": "What's the best piece of advice you've ever received?", "intensity": "soft"},
        {"category": "soft", "subOptions": ["icebreaker"], "type": "truth", "text": "What's your favorite season and why?", "intensity": "soft"}
    ]
    
    icebreaker_dares = [
        {"category": "soft", "subOptions": ["icebreaker"], "type": "dare", "text": "Do your best impression of a celebrity for 30 seconds.", "intensity": "soft"},
        {"category": "soft", "subOptions": ["icebreaker"], "type": "dare", "text": "Show everyone your most recent photo on your phone.", "intensity": "soft"},
        {"category": "soft", "subOptions": ["icebreaker"], "type": "dare", "text": "Tell a joke that will make everyone laugh.", "intensity": "soft"},
        {"category": "soft", "subOptions": ["icebreaker"], "type": "dare", "text": "Sing the chorus of your favorite song.", "intensity": "soft"},
        {"category": "soft", "subOptions": ["icebreaker"], "type": "dare", "text": "Strike a pose like you're on the cover of a magazine.", "intensity": "soft"}
    ]
    
    # Sub-option: Silly
    silly_truths = [
        {"category": "soft", "subOptions": ["silly"], "type": "truth", "text": "What's the silliest thing you've ever done in public?", "intensity": "soft"},
        {"category": "soft", "subOptions": ["silly"], "type": "truth", "text": "What's your funniest childhood memory?", "intensity": "soft"},
        {"category": "soft", "subOptions": ["silly"], "type": "truth", "text": "If you were a cartoon character, who would you be and why?", "intensity": "soft"},
        {"category": "soft", "subOptions": ["silly"], "type": "truth", "text": "What's the weirdest food combination you actually enjoy?", "intensity": "soft"},
        {"category": "soft", "subOptions": ["silly"], "type": "truth", "text": "What's the most embarrassing song on your playlist?", "intensity": "soft"}
    ]
    
    silly_dares = [
        {"category": "soft", "subOptions": ["silly"], "type": "dare", "text": "Do your best chicken impression for 15 seconds.", "intensity": "soft"},
        {"category": "soft", "subOptions": ["silly"], "type": "dare", "text": "Put as many socks on one foot as you can in 30 seconds.", "intensity": "soft"},
        {"category": "soft", "subOptions": ["silly"], "type": "dare", "text": "Speak in an accent of the group's choosing until your next turn.", "intensity": "soft"},
        {"category": "soft", "subOptions": ["silly"], "type": "dare", "text": "Make up a short rap about the person to your left.", "intensity": "soft"},
        {"category": "soft", "subOptions": ["silly"], "type": "dare", "text": "Do your best impression of a runway model walking down the catwalk.", "intensity": "soft"}
    ]
    
    # Sub-option: Creative
    creative_truths = [
        {"category": "soft", "subOptions": ["creative"], "type": "truth", "text": "If you could invent anything, what would it be?", "intensity": "soft"},
        {"category": "soft", "subOptions": ["creative"], "type": "truth", "text": "What's a creative talent you wish you had?", "intensity": "soft"},
        {"category": "soft", "subOptions": ["creative"], "type": "truth", "text": "If you could design your dream house, what unique feature would it have?", "intensity": "soft"},
        {"category": "soft", "subOptions": ["creative"], "type": "truth", "text": "If you wrote a book, what would it be about?", "intensity": "soft"},
        {"category": "soft", "subOptions": ["creative"], "type": "truth", "text": "What's the most creative excuse you've ever used?", "intensity": "soft"}
    ]
    
    creative_dares = [
        {"category": "soft", "subOptions": ["creative"], "type": "dare", "text": "Draw a portrait of someone in the room with your non-dominant hand.", "intensity": "soft"},
        {"category": "soft", "subOptions": ["creative"], "type": "dare", "text": "Create a short commercial for an everyday object near you.", "intensity": "soft"},
        {"category": "soft", "subOptions": ["creative"], "type": "dare", "text": "Make up a short story that includes three random words chosen by the group.", "intensity": "soft"},
        {"category": "soft", "subOptions": ["creative"], "type": "dare", "text": "Create a new dance move and teach it to everyone.", "intensity": "soft"},
        {"category": "soft", "subOptions": ["creative"], "type": "dare", "text": "Design a superhero logo using only items in the room.", "intensity": "soft"}
    ]
    
    # Add sample items to generator
    sample_items = icebreaker_truths + icebreaker_dares + silly_truths + silly_dares + creative_truths + creative_dares
    generator.bulk_add_items(sample_items)
    
    # In a real implementation, we would generate 500+ items here
    # This is just a demonstration with a few sample items

def generate_medium_category(generator):
    """Generate content for the Medium category"""
    print("Generating Medium category content...")
    
    # Sub-option: Challenging
    challenging_truths = [
        {"category": "medium", "subOptions": ["challenging"], "type": "truth", "text": "What's something you've never told anyone in this room?", "intensity": "medium"},
        {"category": "medium", "subOptions": ["challenging"], "type": "truth", "text": "What's the most rebellious thing you've ever done?", "intensity": "medium"},
        {"category": "medium", "subOptions": ["challenging"], "type": "truth", "text": "What's a controversial opinion you have?", "intensity": "medium"},
        {"category": "medium", "subOptions": ["challenging"], "type": "truth", "text": "What's something you've done that you hope your parents never find out about?", "intensity": "medium"},
        {"category": "medium", "subOptions": ["challenging"], "type": "truth", "text": "What's the biggest lie you've ever told?", "intensity": "medium"}
    ]
    
    challenging_dares = [
        {"category": "medium", "subOptions": ["challenging"], "type": "dare", "text": "Text the 10th person in your contacts something embarrassing (show the group before sending).", "intensity": "medium"},
        {"category": "medium", "subOptions": ["challenging"], "type": "dare", "text": "Let someone go through your phone for 1 minute.", "intensity": "medium"},
        {"category": "medium", "subOptions": ["challenging"], "type": "dare", "text": "Call a friend and sing them happy birthday, regardless of whether it's their birthday.", "intensity": "medium"},
        {"category": "medium", "subOptions": ["challenging"], "type": "dare", "text": "Post an embarrassing selfie on your social media of the group's choosing.", "intensity": "medium"},
        {"category": "medium", "subOptions": ["challenging"], "type": "dare", "text": "Let the group create a new dating profile bio for you.", "intensity": "medium"}
    ]
    
    # Add sample items to generator
    sample_items = challenging_truths + challenging_dares
    generator.bulk_add_items(sample_items)
    
    # In a real implementation, we would generate 500+ items here
    # This is just a demonstration with a few sample items

def generate_category_content(generator, category_id):
    """Generate content for a specific category"""
    print(f"Generating {category_id} category content...")
    
    # This function would contain the logic to generate content for each category
    # For now, we'll just generate sample items
    samples = generator.generate_sample_items(category_id, count=10)
    
    # In a real implementation, we would generate 500+ items per category
    # with at least 120 items per sub-option
    
    # Add the sample items to the generator
    for item in samples:
        try:
            generator.generate_item(
                item["category"],
                item["subOptions"][0],
                item["type"],
                item["text"],
                item["intensity"]
            )
        except ValueError as e:
            # Skip duplicates
            print(f"Skipping item: {e}")
    
    return len(samples)

def main():
    # Initialize the content generator
    generator = ContentGenerator("content/schema.json", "content/category_definitions.json")
    
    # Create output directory
    os.makedirs("content/categories", exist_ok=True)
    
    # Generate content for each category
    categories = [cat["id"] for cat in generator.category_definitions["categories"]]
    
    for category_id in categories:
        # Generate content for this category
        generate_category_content(generator, category_id)
        
        # Save the category content
        output_path = generator.save_category(category_id, "content/categories")
        print(f"Saved {category_id} content to {output_path}")
        
        # Validate the category content
        validation = generator.validate_category_requirements(category_id)
        if not validation["valid"]:
            print(f"WARNING: {category_id} category does not meet requirements:")
            for issue in validation["issues"]:
                print(f"  - {issue}")
        else:
            print(f"{category_id} category meets all requirements!")
        
        print(f"Stats for {category_id}:")
        stats = validation["stats"]
        print(f"  Total items: {stats['total']}")
        print(f"  Truth: {stats['truth_count']} ({stats['truth_percentage']:.1f}%)")
        print(f"  Dare: {stats['dare_count']} ({stats['dare_percentage']:.1f}%)")
        print(f"  Sub-option counts: {stats['sub_option_counts']}")
        print(f"  Intensity counts: {stats['intensity_counts']}")
        print()
    
    # Generate summary
    print("Content Generation Summary:")
    all_stats = generator.get_all_stats()
    total_items = sum(stats["total"] for stats in all_stats.values())
    print(f"Total items across all categories: {total_items}")
    
    # Save all validation results
    validation_results = generator.validate_all_categories()
    with open("content/validation_results.json", "w") as f:
        json.dump(validation_results, f, indent=2)
    
    print("Validation results saved to content/validation_results.json")

if __name__ == "__main__":
    main()