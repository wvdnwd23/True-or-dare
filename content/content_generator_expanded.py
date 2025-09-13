#!/usr/bin/env python3
import json
import os
import uuid
import re
from collections import defaultdict
import random

class ContentGeneratorExpanded:
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
        
        # Load templates for content generation
        self.truth_templates = self._load_templates("truth")
        self.dare_templates = self._load_templates("dare")
    
    def _load_templates(self, item_type):
        """Load templates for content generation"""
        templates = {
            "truth": {
                "soft": [
                    "What's your favorite {noun}?",
                    "If you could {verb} anything, what would it be?",
                    "What's the most {adjective} thing you've ever done?",
                    "Who is your favorite {person_type}?",
                    "What's your go-to {activity} when you're {feeling}?",
                    "If you could live in any {place}, where would it be?",
                    "What's the best {event} you've ever attended?",
                    "What {hobby} would you like to try?",
                    "What's your favorite way to {relax_activity}?",
                    "If you could have any {superpower}, what would it be?"
                ],
                "medium": [
                    "What's something you've never told anyone about your {topic}?",
                    "What's the most {adjective} thing you've done that no one knows about?",
                    "What's your biggest {fear} related to {topic}?",
                    "What's a {adjective} opinion you have that might be controversial?",
                    "What's something you've done that you hope your {relation} never finds out about?",
                    "What's the biggest {mistake} you've made in a {situation}?",
                    "What's something about your {topic} that would surprise people?",
                    "What's a {secret} you've kept from your {relation}?",
                    "What's the most embarrassing thing that's happened to you during {activity}?",
                    "What's something you've always wanted to {verb} but haven't had the courage to?"
                ],
                "hot": [
                    "What's your biggest {turn_on}?",
                    "What's the most {adjective} fantasy you've had?",
                    "What's something {intimate} you've always wanted to try?",
                    "What's the most {adjective} place you've ever been {intimate_verb}?",
                    "What's a {body_part} that you find most attractive on others?",
                    "What's the most {adjective} thing someone has done that immediately attracted you to them?",
                    "What's something that always puts you in the mood for {intimate_activity}?",
                    "What's your favorite form of {intimate_touch}?",
                    "What's the boldest move you've ever made on someone you were attracted to?",
                    "What's something you find {adjective} that most people might not?"
                ]
            },
            "dare": {
                "soft": [
                    "Do your best impression of {celebrity} for 30 seconds.",
                    "Show everyone your most recent {item} on your phone.",
                    "Tell a joke about {topic}.",
                    "Sing the chorus of {song_type}.",
                    "Strike a pose like you're {activity}.",
                    "Give a compliment to {person}.",
                    "Do your best {animal} impression.",
                    "Take a selfie with {person} and set it as your phone wallpaper for the next hour.",
                    "Create a short commercial for {item}.",
                    "Make up a short story that includes {random_elements}."
                ],
                "medium": [
                    "Text the {number} person in your contacts something {adjective} (show the group before sending).",
                    "Let someone go through your {personal_item} for 1 minute.",
                    "Call {person} and {activity} without explaining why.",
                    "Post an {adjective} selfie on your social media of the group's choosing.",
                    "Let the group create a new {profile} for you.",
                    "Show the last {number} {items} you {verb}.",
                    "Let someone {action} your {body_part} with a washable marker.",
                    "Eat a spoonful of {food_item}.",
                    "Speak in {accent} until your next turn.",
                    "Let the group pose you for a new {photo_type}."
                ],
                "hot": [
                    "Give a {adjective} compliment to {person}.",
                    "Show the group your best {flirty_action}.",
                    "Demonstrate your most {adjective} {action}.",
                    "Send a {adjective} text to {person} (show the group first).",
                    "Whisper something {adjective} in the ear of {person}.",
                    "Feed a small bite of food to {person}.",
                    "Give a brief {body_part} massage to {person}.",
                    "Demonstrate what you consider to be the most attractive way to {action}.",
                    "Use only body language to show how you'd {approach_action} someone you're attracted to.",
                    "Remove an article of clothing (nothing essential) for {number} rounds."
                ]
            }
        }
        
        return templates.get(item_type, {})
    
    def _fill_template(self, template, category_id, sub_option_id, intensity):
        """Fill a template with appropriate content based on category and intensity"""
        # Define replacements based on category and intensity
        replacements = {
            "soft": {
                "icebreaker": {
                    "noun": ["way to spend a weekend", "ice cream flavor", "movie", "book", "vacation spot", "holiday tradition"],
                    "verb": ["travel to", "learn", "master", "experience", "discover", "create"],
                    "adjective": ["fun", "exciting", "memorable", "surprising", "interesting", "unusual"],
                    "person_type": ["celebrity", "fictional character", "historical figure", "athlete", "musician", "actor"],
                    "activity": ["activity", "game", "hobby", "pastime", "sport"],
                    "feeling": ["happy", "sad", "bored", "excited", "relaxed", "stressed"],
                    "place": ["country", "city", "fictional world", "historical period", "type of home"],
                    "event": ["concert", "party", "trip", "celebration", "festival", "game"],
                    "hobby": ["hobby", "skill", "language", "instrument", "sport", "craft"],
                    "relax_activity": ["relax", "unwind", "spend free time", "spend a day off"],
                    "superpower": ["superpower", "magical ability", "special talent", "extraordinary skill"]
                },
                "silly": {
                    "noun": ["silly habit", "childhood toy", "cartoon character", "dance move", "made-up word"],
                    "verb": ["dance like", "pretend to be", "impersonate", "mimic", "act like"],
                    "adjective": ["silly", "ridiculous", "absurd", "goofy", "wacky", "bizarre"],
                    "person_type": ["cartoon character", "comedy character", "sitcom character", "animated character"],
                    "activity": ["silly game", "childhood game", "make-believe scenario"],
                    "feeling": ["giggly", "playful", "mischievous", "goofy", "silly"],
                    "place": ["imaginary place", "cartoon world", "fantasy land", "made-up country"],
                    "event": ["costume party", "comedy show", "carnival", "circus", "themed party"],
                    "hobby": ["unusual hobby", "quirky interest", "strange collection", "weird talent"],
                    "relax_activity": ["do something silly", "play a childish game", "watch cartoons"],
                    "superpower": ["ridiculous superpower", "useless magical ability", "silly special talent"]
                },
                "creative": {
                    "noun": ["art form", "creative project", "invention", "design", "creation"],
                    "verb": ["create", "design", "invent", "compose", "craft", "build"],
                    "adjective": ["creative", "innovative", "artistic", "imaginative", "original", "inventive"],
                    "person_type": ["artist", "inventor", "creator", "designer", "musician", "writer"],
                    "activity": ["creative activity", "art project", "craft", "design challenge"],
                    "feeling": ["inspired", "creative", "imaginative", "artistic", "expressive"],
                    "place": ["artist's studio", "creative space", "design workshop", "innovation hub"],
                    "event": ["art exhibition", "creative workshop", "design showcase", "innovation conference"],
                    "hobby": ["artistic hobby", "creative pursuit", "craft", "design skill"],
                    "relax_activity": ["create art", "work on a creative project", "design something"],
                    "superpower": ["creative ability", "artistic talent", "design skill", "innovative thinking"]
                }
            },
            "medium": {
                "challenging": {
                    "topic": ["past", "childhood", "relationship", "career", "family", "education"],
                    "adjective": ["rebellious", "risky", "daring", "bold", "challenging", "questionable"],
                    "fear": ["fear", "worry", "concern", "anxiety", "apprehension"],
                    "relation": ["parents", "best friend", "partner", "family", "colleagues"],
                    "mistake": ["mistake", "error", "misjudgment", "poor decision", "regret"],
                    "situation": ["relationship", "job", "friendship", "family gathering", "social situation"],
                    "secret": ["secret", "hidden truth", "private matter", "confidential information"],
                    "activity": ["a date", "a job interview", "a presentation", "a social event"],
                    "verb": ["confess", "admit", "reveal", "share", "disclose"]
                },
                "entertaining": {
                    "topic": ["funny story", "embarrassing moment", "prank", "joke", "misunderstanding"],
                    "adjective": ["hilarious", "entertaining", "amusing", "comical", "funny", "ridiculous"],
                    "fear": ["embarrassing situation", "public speaking mishap", "awkward encounter"],
                    "relation": ["friends", "audience", "crowd", "group", "party guests"],
                    "mistake": ["embarrassing mistake", "funny error", "comical misunderstanding"],
                    "situation": ["party", "social gathering", "public event", "group activity"],
                    "secret": ["funny secret", "amusing confession", "entertaining story"],
                    "activity": ["performing", "public speaking", "dancing", "singing"],
                    "verb": ["entertain", "amuse", "make people laugh", "perform"]
                },
                "surprising": {
                    "topic": ["unexpected talent", "hidden skill", "surprising fact", "secret ability"],
                    "adjective": ["unexpected", "surprising", "shocking", "astonishing", "unbelievable"],
                    "fear": ["unexpected outcome", "surprising result", "unforeseen consequence"],
                    "relation": ["acquaintances", "new friends", "strangers", "people who just met you"],
                    "mistake": ["unexpected error", "surprising misstep", "unforeseen problem"],
                    "situation": ["unexpected encounter", "surprising event", "chance meeting"],
                    "secret": ["surprising revelation", "unexpected truth", "astonishing fact"],
                    "activity": ["trying something new", "unexpected challenge", "surprising opportunity"],
                    "verb": ["surprise others", "shock people", "amaze an audience", "astonish friends"]
                }
            },
            "hot": {
                "flirty": {
                    "turn_on": ["turn-on", "attraction", "flirtatious gesture", "romantic quality"],
                    "adjective": ["flirty", "attractive", "charming", "alluring", "enticing"],
                    "intimate": ["romantic", "flirtatious", "charming", "attractive"],
                    "intimate_verb": ["flirting", "charming someone", "attracting attention"],
                    "body_part": ["smile", "eyes", "laugh", "voice", "style"],
                    "intimate_activity": ["romance", "flirtation", "attraction"],
                    "intimate_touch": ["touch", "gesture", "glance", "compliment"],
                    "approach_action": ["approach", "flirt with", "charm", "attract"]
                },
                "sensual": {
                    "turn_on": ["sensual experience", "physical attraction", "intimate moment"],
                    "adjective": ["sensual", "tantalizing", "seductive", "provocative", "enticing"],
                    "intimate": ["sensual", "provocative", "seductive", "enticing"],
                    "intimate_verb": ["being intimate", "sharing a sensual moment", "experiencing attraction"],
                    "body_part": ["physical feature", "attractive quality", "sensual attribute"],
                    "intimate_activity": ["intimacy", "sensuality", "physical connection"],
                    "intimate_touch": ["physical touch", "sensual contact", "intimate gesture"],
                    "approach_action": ["seduce", "entice", "attract", "tantalize"]
                },
                "daring": {
                    "turn_on": ["bold move", "daring action", "adventurous experience"],
                    "adjective": ["daring", "bold", "adventurous", "uninhibited", "spontaneous"],
                    "intimate": ["adventurous", "bold", "daring", "uninhibited"],
                    "intimate_verb": ["being adventurous", "taking a risk", "being spontaneous"],
                    "body_part": ["feature", "attribute", "characteristic", "quality"],
                    "intimate_activity": ["adventure", "spontaneity", "risk-taking"],
                    "intimate_touch": ["bold gesture", "daring move", "adventurous action"],
                    "approach_action": ["boldly approach", "daringly pursue", "adventurously engage with"]
                }
            }
        }
        
        # Get the appropriate replacements based on category and sub-option
        category_replacements = replacements.get(intensity, {}).get(sub_option_id, {})
        
        # If no specific replacements found, use generic ones
        if not category_replacements:
            category_replacements = {
                "noun": ["thing", "item", "object", "concept", "idea"],
                "verb": ["do", "try", "experience", "attempt", "accomplish"],
                "adjective": ["interesting", "notable", "significant", "memorable", "important"],
                "person_type": ["person", "individual", "character", "figure", "personality"],
                "activity": ["activity", "action", "task", "endeavor", "pursuit"],
                "feeling": ["feeling", "emotion", "mood", "state", "condition"],
                "place": ["place", "location", "spot", "area", "region"],
                "event": ["event", "occasion", "happening", "occurrence", "incident"],
                "hobby": ["hobby", "interest", "pastime", "activity", "pursuit"],
                "relax_activity": ["relax", "unwind", "rest", "take it easy", "chill"],
                "superpower": ["ability", "power", "capability", "skill", "talent"],
                "topic": ["topic", "subject", "matter", "issue", "theme"],
                "fear": ["fear", "concern", "worry", "anxiety", "apprehension"],
                "relation": ["person", "individual", "acquaintance", "contact", "connection"],
                "mistake": ["mistake", "error", "misstep", "slip-up", "blunder"],
                "situation": ["situation", "circumstance", "scenario", "context", "setting"],
                "secret": ["secret", "private matter", "confidential information", "hidden fact"],
                "turn_on": ["preference", "attraction", "interest", "appeal", "draw"],
                "intimate": ["personal", "private", "close", "special", "meaningful"],
                "intimate_verb": ["sharing", "experiencing", "enjoying", "participating in"],
                "body_part": ["feature", "attribute", "characteristic", "quality", "trait"],
                "intimate_activity": ["activity", "experience", "moment", "interaction"],
                "intimate_touch": ["interaction", "connection", "contact", "engagement"],
                "approach_action": ["approach", "engage with", "interact with", "connect with"]
            }
        
        # Replace placeholders in the template
        result = template
        for placeholder, options in category_replacements.items():
            if "{" + placeholder + "}" in result:
                result = result.replace("{" + placeholder + "}", random.choice(options))
        
        # Replace any remaining placeholders with generic terms
        for placeholder in re.findall(r'\{([^}]+)\}', result):
            if placeholder in ["person", "number", "item", "celebrity", "song_type", "animal", "random_elements", 
                              "personal_item", "activity", "profile", "items", "food_item", "accent", "photo_type",
                              "flirty_action", "action"]:
                replacements = {
                    "person": ["someone in the room", "the person to your right", "the person to your left", "the person across from you", "everyone"],
                    "number": ["3rd", "5th", "10th", "last", "first"],
                    "item": ["object", "thing", "item", "product", "possession"],
                    "celebrity": ["a celebrity", "a famous person", "a movie star", "a singer", "a public figure"],
                    "song_type": ["your favorite song", "a popular song", "a song everyone knows", "a song from your childhood"],
                    "animal": ["dog", "cat", "chicken", "monkey", "elephant", "lion"],
                    "random_elements": ["three random words chosen by the group", "objects in this room", "people in the group", "recent events"],
                    "personal_item": ["phone", "social media", "photo gallery", "music playlist", "recent calls"],
                    "activity": ["sing to them", "tell them a joke", "speak in a funny accent", "pretend you're calling about a survey"],
                    "profile": ["dating profile bio", "social media profile", "professional bio", "personal description"],
                    "items": ["photos", "messages", "social media posts", "emails", "apps"],
                    "verb": ["shared", "posted", "saved", "liked", "commented on"],
                    "food_item": ["hot sauce", "mustard", "ketchup", "honey", "lemon juice", "soy sauce"],
                    "accent": ["a British accent", "a Southern accent", "an Australian accent", "a made-up accent"],
                    "photo_type": ["profile picture", "social media post", "photo pose", "selfie"],
                    "flirty_action": ["flirtatious look", "pick-up line", "flirty walk", "charming gesture"],
                    "action": ["dance move", "walk", "facial expression", "gesture", "pose"],
                    "body_part": ["hand", "arm", "cheek", "forehead", "shoulder"]
                }
                result = result.replace("{" + placeholder + "}", random.choice(replacements.get(placeholder, ["thing"])))
        
        return result
    
    def generate_content_from_templates(self, category_id, count_per_sub_option=120):
        """Generate content for a category using templates"""
        if category_id not in self.categories:
            raise ValueError(f"Invalid category ID: {category_id}")
        
        category = self.categories[category_id]
        items = []
        
        # Determine if category is age restricted or requires opt-in
        age_restricted = category.get("ageRestricted", False)
        requires_opt_in = category.get("requiresOptIn", False)
        
        # For each sub-option, generate truths and dares
        for sub_option in category["subOptions"]:
            sub_option_id = sub_option["id"]
            
            # Determine how many truths and dares to generate (roughly 50/50 split)
            truth_count = count_per_sub_option // 2
            dare_count = count_per_sub_option - truth_count
            
            # Generate truths
            for i in range(truth_count):
                # Determine intensity (distribute evenly among soft, medium, hot)
                if i < truth_count // 3:
                    intensity = "soft"
                elif i < (truth_count * 2) // 3:
                    intensity = "medium"
                else:
                    intensity = "hot"
                
                # Skip hot intensity if category is not age restricted
                if intensity == "hot" and not age_restricted:
                    intensity = "medium"
                
                # Get templates for this intensity
                templates = self.truth_templates.get(intensity, [])
                if not templates:
                    continue
                
                # Select a template and fill it
                template = random.choice(templates)
                text = self._fill_template(template, category_id, sub_option_id, intensity)
                
                # Create the item
                item = {
                    "id": f"{category_id}-truth-{sub_option_id}-{i+1}",
                    "type": "truth",
                    "text": text,
                    "category": category_id,
                    "subOptions": [sub_option_id],
                    "intensity": intensity,
                    "ageRestricted": age_restricted,
                    "requiresOptIn": requires_opt_in
                }
                
                # Add to items list
                items.append(item)
            
            # Generate dares
            for i in range(dare_count):
                # Determine intensity (distribute evenly among soft, medium, hot)
                if i < dare_count // 3:
                    intensity = "soft"
                elif i < (dare_count * 2) // 3:
                    intensity = "medium"
                else:
                    intensity = "hot"
                
                # Skip hot intensity if category is not age restricted
                if intensity == "hot" and not age_restricted:
                    intensity = "medium"
                
                # Get templates for this intensity
                templates = self.dare_templates.get(intensity, [])
                if not templates:
                    continue
                
                # Select a template and fill it
                template = random.choice(templates)
                text = self._fill_template(template, category_id, sub_option_id, intensity)
                
                # Create the item
                item = {
                    "id": f"{category_id}-dare-{sub_option_id}-{i+1}",
                    "type": "dare",
                    "text": text,
                    "category": category_id,
                    "subOptions": [sub_option_id],
                    "intensity": intensity,
                    "ageRestricted": age_restricted,
                    "requiresOptIn": requires_opt_in
                }
                
                # Add to items list
                items.append(item)
        
        return items
    
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
        added_items = []
        for item in items:
            try:
                added_item = self.generate_item(
                    item["category"],
                    item["subOptions"][0],
                    item["type"],
                    item["text"],
                    item["intensity"]
                )
                added_items.append(added_item)
            except ValueError as e:
                # Skip duplicates
                print(f"Skipping item: {e}")
        
        return added_items
    
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
    
    def generate_full_category_content(self, category_id, output_dir):
        """Generate full content for a category and save it"""
        if category_id not in self.categories:
            raise ValueError(f"Invalid category ID: {category_id}")
        
        print(f"Generating content for {category_id} category...")
        
        # Generate content from templates
        items = self.generate_content_from_templates(category_id, count_per_sub_option=170)  # Generate more than needed to account for duplicates
        
        # Add items to content
        added_items = self.bulk_add_items(items)
        
        # Validate category requirements
        validation = self.validate_category_requirements(category_id)
        if not validation["valid"]:
            print(f"WARNING: {category_id} category does not meet requirements:")
            for issue in validation["issues"]:
                print(f"  - {issue}")
            
            # If we don't have enough items, generate more
            while not validation["valid"] and "has only" in validation["issues"][0]:
                print("Generating additional items to meet requirements...")
                more_items = self.generate_content_from_templates(category_id, count_per_sub_option=50)
                self.bulk_add_items(more_items)
                validation = self.validate_category_requirements(category_id)
        
        # Save category content
        output_path = self.save_category(category_id, output_dir)
        print(f"Saved {category_id} content to {output_path}")
        
        # Print statistics
        stats = validation["stats"]
        print(f"Stats for {category_id}:")
        print(f"  Total items: {stats['total']}")
        print(f"  Truth: {stats['truth_count']} ({stats['truth_percentage']:.1f}%)")
        print(f"  Dare: {stats['dare_count']} ({stats['dare_percentage']:.1f}%)")
        print(f"  Sub-option counts: {stats['sub_option_counts']}")
        print(f"  Intensity counts: {stats['intensity_counts']}")
        
        return len(added_items)

if __name__ == "__main__":
    # Example usage
    generator = ContentGeneratorExpanded("content/schema.json", "content/category_definitions.json")
    
    # Generate content for all categories
    for category_id in generator.categories:
        generator.generate_full_category_content(category_id, "content/categories")
    
    # Validate all categories
    validation_results = generator.validate_all_categories()
    with open("content/validation_results.json", "w") as f:
        json.dump(validation_results, f, indent=2)
    
    print("Content generation complete!")