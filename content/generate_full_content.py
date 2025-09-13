#!/usr/bin/env python3
import os
import json
import sys
from content_generator_expanded import ContentGeneratorExpanded

def main():
    print("Truth or Dare - Full Content Generation")
    print("======================================")
    
    # Get the current directory
    current_dir = os.path.dirname(os.path.abspath(__file__))
    
    # Initialize the expanded content generator with absolute paths
    schema_path = os.path.join(current_dir, "schema.json")
    category_definitions_path = os.path.join(current_dir, "category_definitions.json")
    
    print(f"Using schema path: {schema_path}")
    print(f"Using category definitions path: {category_definitions_path}")
    
    # Check if files exist
    if not os.path.exists(schema_path):
        print(f"ERROR: Schema file not found at {schema_path}")
        return
    
    if not os.path.exists(category_definitions_path):
        print(f"ERROR: Category definitions file not found at {category_definitions_path}")
        return
    
    # Initialize the generator
    generator = ContentGeneratorExpanded(schema_path, category_definitions_path)
    
    # Create output directory structure
    for category_id in generator.categories:
        category_dir = os.path.join(current_dir, "categories", category_id)
        os.makedirs(category_dir, exist_ok=True)
    
    # Generate content for all categories
    total_items = 0
    for category_id in generator.categories:
        category_name = generator.categories[category_id]["name"]
        print(f"\nGenerating content for {category_name} ({category_id}) category...")
        
        # Generate full content for this category
        output_dir = os.path.join(current_dir, "categories", category_id)
        item_count = generator.generate_full_category_content(category_id, output_dir)
        total_items += item_count
        
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
    
    # Generate summary
    print("\nContent Generation Summary:")
    all_stats = generator.get_all_stats()
    total_items = sum(stats["total"] for stats in all_stats.values())
    print(f"Total items across all categories: {total_items}")
    
    # Save all validation results
    validation_results = generator.validate_all_categories()
    validation_path = os.path.join(current_dir, "validation_results.json")
    with open(validation_path, "w") as f:
        json.dump(validation_results, f, indent=2)
    
    print(f"\nValidation results saved to {validation_path}")
    
    # Create a combined content file for easy import
    combined_content = {}
    for category_id in generator.categories:
        combined_content[category_id] = generator.content[category_id]
    
    all_content_path = os.path.join(current_dir, "all_content.json")
    with open(all_content_path, "w") as f:
        json.dump(combined_content, f, indent=2)
    
    print(f"All content saved to {all_content_path}")
    
    # Check if all requirements are met
    all_valid = all(result["valid"] for result in validation_results.values())
    if all_valid:
        print("\nSUCCESS: All categories meet the requirements!")
    else:
        print("\nWARNING: Some categories do not meet requirements. Check validation_results.json for details.")
    
    # Print final statistics
    print("\nFinal Statistics:")
    print(f"Total categories: {len(generator.categories)}")
    print(f"Total items: {total_items}")
    print(f"Average items per category: {total_items / len(generator.categories):.1f}")
    
    truth_count = sum(stats["truth_count"] for stats in all_stats.values())
    dare_count = sum(stats["dare_count"] for stats in all_stats.values())
    print(f"Total truths: {truth_count} ({truth_count / total_items * 100:.1f}%)")
    print(f"Total dares: {dare_count} ({dare_count / total_items * 100:.1f}%)")
    
    # Count by intensity
    intensity_counts = {"soft": 0, "medium": 0, "hot": 0}
    for category_id, stats in all_stats.items():
        for intensity, count in stats["intensity_counts"].items():
            intensity_counts[intensity] += count
    
    print("Intensity distribution:")
    for intensity, count in intensity_counts.items():
        print(f"  {intensity}: {count} ({count / total_items * 100:.1f}%)")

if __name__ == "__main__":
    main()