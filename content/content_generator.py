#!/usr/bin/env python3
import json
import os
import uuid
import re
from collections import defaultdict

class ContentGenerator:
    def __init__(self, schema_path, category_definitions_path):
        # Load schema
        with open(schema_path, 'r') as f:
            self.schema = json.load(f)
        
        # Load category definitions
        with open(category_definitions_path, 'r') as f:
            self.category_definitions = json.load(f)
        
        # Create a mapping of category IDs to their definitions
        self.categories = {cat["id"]: cat for cat in self.category_definitions["categories"]}
        
        # Initialize content storage
        self.content = defaultdict(list)
        self.all_texts = set()  # For duplicate detection
    
    def generate_item(self, category_id, sub_option_id, item_type, text, intensity):
        """Generate a single truth or dare item"""
        # Validate category and sub-option
        if category_id not in self.categories:
            raise ValueError(f"Invalid category ID: {category_id}")
        
        category = self.categories[category_id]
        valid_sub_options = [opt["id"] for opt in category["subOptions"]]
        if sub_option_id not in valid_sub_options:
            raise ValueError(f"Invalid sub-option ID: {sub_option_id} for category {category_id}")
        
        # Validate type
        if item_type not in ["truth", "dare"]:
            raise ValueError(f"Invalid item type: {item_type}. Must be 'truth' or 'dare'")
        
        # Validate intensity
        if intensity not in ["soft", "medium", "hot"]:
            raise ValueError(f"Invalid intensity: {intensity}. Must be 'soft', 'medium', or 'hot'")
        
        # Check for duplicates
        normalized_text = self.normalize_text(text)
        if normalized_text in self.all_texts:
            raise ValueError(f"Duplicate item detected: {text}")
        
        # Create the item
        item = {
            "id": str(uuid.uuid4()),
            "type": item_type,
            "text": text,
            "category": category_id,
            "subOptions": [sub_option_id],
            "intensity": intensity,
            "ageRestricted": category.get("ageRestricted", False),
            "requiresOptIn": category.get("requiresOptIn", False)
        }
        
        # Add to content and track for duplicate detection
        self.content[category_id].append(item)
        self.all_texts.add(normalized_text)
        
        return item
    
    def normalize_text(self, text):
        """Normalize text for duplicate detection"""
        # Convert to lowercase
        text = text.lower()
        # Remove punctuation and extra whitespace
        text = re.sub(r'[^\w\s]', '', text)
        # Remove extra spaces
        text = re.sub(r'\s+', ' ', text)
        return text.strip()
    
    def bulk_add_items(self, items):
        """Add multiple items at once"""
        for item in items:
            self.generate_item(
                item["category"],
                item["subOptions"][0],
                item["type"],
                item["text"],
                item["intensity"]
            )
    
    def get_category_stats(self, category_id):
        """Get statistics for a specific category"""
        if category_id not in self.categories:
            raise ValueError(f"Invalid category ID: {category_id}")
        
        items = self.content[category_id]
        
        # Count by type
        truth_count = sum(1 for item in items if item["type"] == "truth")
        dare_count = sum(1 for item in items if item["type"] == "dare")
        
        # Count by sub-option
        sub_option_counts = defaultdict(int)
        for item in items:
            for sub_opt in item["subOptions"]:
                sub_option_counts[sub_opt] += 1
        
        # Count by intensity
        intensity_counts = defaultdict(int)
        for item in items:
            intensity_counts[item["intensity"]] += 1
        
        return {
            "total": len(items),
            "truth_count": truth_count,
            "dare_count": dare_count,
            "truth_percentage": (truth_count / len(items)) * 100 if items else 0,
            "dare_percentage": (dare_count / len(items)) * 100 if items else 0,
            "sub_option_counts": dict(sub_option_counts),
            "intensity_counts": dict(intensity_counts)
        }
    
    def get_all_stats(self):
        """Get statistics for all categories"""
        stats = {}
        for category_id in self.categories:
            stats[category_id] = self.get_category_stats(category_id)
        return stats
    
    def validate_category_requirements(self, category_id):
        """Validate that a category meets all requirements"""
        if category_id not in self.categories:
            raise ValueError(f"Invalid category ID: {category_id}")
        
        stats = self.get_category_stats(category_id)
        category = self.categories[category_id]
        
        issues = []
        
        # Check total count (≥500 items)
        if stats["total"] < 500:
            issues.append(f"Category {category_id} has only {stats['total']} items. Minimum required: 500")
        
        # Check sub-option counts (≥120 items each)
        for sub_opt in category["subOptions"]:
            sub_opt_id = sub_opt["id"]
            count = stats["sub_option_counts"].get(sub_opt_id, 0)
            if count < 120:
                issues.append(f"Sub-option {sub_opt_id} in category {category_id} has only {count} items. Minimum required: 120")
        
        # Check truth/dare balance (50/50 ±10%)
        if abs(stats["truth_percentage"] - 50) > 10:
            issues.append(f"Truth/Dare balance in category {category_id} is off: {stats['truth_percentage']:.1f}% truths, {stats['dare_percentage']:.1f}% dares. Should be 50/50 ±10%")
        
        return {
            "valid": len(issues) == 0,
            "issues": issues,
            "stats": stats
        }
    
    def validate_all_categories(self):
        """Validate all categories against requirements"""
        results = {}
        for category_id in self.categories:
            results[category_id] = self.validate_category_requirements(category_id)
        return results
    
    def save_category(self, category_id, output_dir):
        """Save a category's content to a JSON file"""
        if category_id not in self.categories:
            raise ValueError(f"Invalid category ID: {category_id}")
        
        # Ensure the output directory exists
        os.makedirs(output_dir, exist_ok=True)
        
        # Save the content
        output_path = os.path.join(output_dir, f"{category_id}.json")
        with open(output_path, 'w') as f:
            json.dump(self.content[category_id], f, indent=2)
        
        return output_path
    
    def save_all_categories(self, output_dir):
        """Save all categories to JSON files"""
        paths = {}
        for category_id in self.categories:
            paths[category_id] = self.save_category(category_id, output_dir)
        return paths
    
    def load_category(self, category_id, input_path):
        """Load a category's content from a JSON file"""
        if category_id not in self.categories:
            raise ValueError(f"Invalid category ID: {category_id}")
        
        # Load the content
        with open(input_path, 'r') as f:
            items = json.load(f)
        
        # Clear existing content for this category
        self.content[category_id] = []
        
        # Add each item
        for item in items:
            # Skip duplicate detection for loaded items
            normalized_text = self.normalize_text(item["text"])
            self.all_texts.add(normalized_text)
            self.content[category_id].append(item)
        
        return len(items)
    
    def generate_sample_items(self, category_id, count=5):
        """Generate sample items for a category for preview purposes"""
        if category_id not in self.categories:
            raise ValueError(f"Invalid category ID: {category_id}")
        
        category = self.categories[category_id]
        samples = []
        
        # Generate sample truths and dares for each sub-option
        for sub_opt in category["subOptions"]:
            sub_opt_id = sub_opt["id"]
            
            # Add sample truths
            for i in range(count):
                samples.append({
                    "id": f"sample-{category_id}-{sub_opt_id}-truth-{i}",
                    "type": "truth",
                    "text": f"Sample truth for {category['name']} - {sub_opt['name']} #{i+1}",
                    "category": category_id,
                    "subOptions": [sub_opt_id],
                    "intensity": "medium",
                    "ageRestricted": category.get("ageRestricted", False),
                    "requiresOptIn": category.get("requiresOptIn", False)
                })
            
            # Add sample dares
            for i in range(count):
                samples.append({
                    "id": f"sample-{category_id}-{sub_opt_id}-dare-{i}",
                    "type": "dare",
                    "text": f"Sample dare for {category['name']} - {sub_opt['name']} #{i+1}",
                    "category": category_id,
                    "subOptions": [sub_opt_id],
                    "intensity": "medium",
                    "ageRestricted": category.get("ageRestricted", False),
                    "requiresOptIn": category.get("requiresOptIn", False)
                })
        
        return samples

if __name__ == "__main__":
    # Example usage
    generator = ContentGenerator("schema.json", "category_definitions.json")
    
    # Generate some sample items
    generator.generate_item("soft", "icebreaker", "truth", "What's your favorite ice cream flavor?", "soft")
    generator.generate_item("soft", "icebreaker", "dare", "Do your best impression of a celebrity", "soft")
    
    # Save to file
    generator.save_category("soft", "categories")
    
    # Validate
    validation = generator.validate_category_requirements("soft")
    print(json.dumps(validation, indent=2))