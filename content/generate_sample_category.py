#!/usr/bin/env python3
import os
import json
import random
import uuid
from collections import defaultdict

def normalize_text(text):
    """Normalize text for duplicate detection"""
    # Convert to lowercase
    text = text.lower()
    # Remove punctuation and extra whitespace
    import re
    text = re.sub(r'[^\w\s]', '', text)
    # Remove extra spaces
    text = re.sub(r'\s+', ' ', text)
    return text.strip()

def generate_sample_category(category_id, category_name, sub_options, is_age_restricted=False, requires_opt_in=False):
    """Generate a sample category with truths and dares"""
    items = []
    all_texts = set()
    
    # For each sub-option, generate truths and dares
    for sub_option in sub_options:
        sub_option_id = sub_option["id"]
        sub_option_name = sub_option["name"]
        
        # Generate truths (60 per sub-option)
        for i in range(60):
            # Determine intensity (distribute evenly)
            if i < 20:
                intensity = "soft"
            elif i < 40:
                intensity = "medium"
            else:
                intensity = "hot"
            
            # Skip hot intensity if category is not age restricted
            if intensity == "hot" and not is_age_restricted:
                intensity = "medium"
            
            # Generate truth text based on sub-option and intensity
            text = generate_truth_text(category_id, sub_option_id, intensity)
            
            # Check for duplicates
            normalized_text = normalize_text(text)
            if normalized_text in all_texts:
                # Try again with a different text
                for _ in range(5):
                    text = generate_truth_text(category_id, sub_option_id, intensity)
                    normalized_text = normalize_text(text)
                    if normalized_text not in all_texts:
                        break
            
            # Add to set of texts
            all_texts.add(normalized_text)
            
            # Create the item
            item = {
                "id": f"{category_id}-truth-{sub_option_id}-{i+1}",
                "type": "truth",
                "text": text,
                "category": category_id,
                "subOptions": [sub_option_id],
                "intensity": intensity,
                "ageRestricted": is_age_restricted,
                "requiresOptIn": requires_opt_in
            }
            
            # Add to items list
            items.append(item)
        
        # Generate dares (60 per sub-option)
        for i in range(60):
            # Determine intensity (distribute evenly)
            if i < 20:
                intensity = "soft"
            elif i < 40:
                intensity = "medium"
            else:
                intensity = "hot"
            
            # Skip hot intensity if category is not age restricted
            if intensity == "hot" and not is_age_restricted:
                intensity = "medium"
            
            # Generate dare text based on sub-option and intensity
            text = generate_dare_text(category_id, sub_option_id, intensity)
            
            # Check for duplicates
            normalized_text = normalize_text(text)
            if normalized_text in all_texts:
                # Try again with a different text
                for _ in range(5):
                    text = generate_dare_text(category_id, sub_option_id, intensity)
                    normalized_text = normalize_text(text)
                    if normalized_text not in all_texts:
                        break
            
            # Add to set of texts
            all_texts.add(normalized_text)
            
            # Create the item
            item = {
                "id": f"{category_id}-dare-{sub_option_id}-{i+1}",
                "type": "dare",
                "text": text,
                "category": category_id,
                "subOptions": [sub_option_id],
                "intensity": intensity,
                "ageRestricted": is_age_restricted,
                "requiresOptIn": requires_opt_in
            }
            
            # Add to items list
            items.append(item)
    
    return items

def generate_truth_text(category_id, sub_option_id, intensity):
    """Generate a truth question based on category, sub-option, and intensity"""
    # Define templates based on category and sub-option
    templates = {
        "soft": {
            "icebreaker": [
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
            "silly": [
                "What's the silliest thing you've ever done in {place}?",
                "What's your funniest {memory_type}?",
                "If you were a {character_type}, who would you be and why?",
                "What's the weirdest {item} combination you actually enjoy?",
                "What's the most embarrassing {item} on your {list_type}?",
                "What's the strangest dream you've ever had?",
                "What's your silliest fear or phobia?",
                "What's the funniest joke you know by heart?",
                "If you had to eat one {food} for the rest of your life, what would it be?",
                "What's the most ridiculous thing you've ever {action}?"
            ],
            "creative": [
                "If you could invent anything, what would it be?",
                "What's a creative talent you wish you had?",
                "If you could design your dream {item}, what unique feature would it have?",
                "If you wrote a {creative_work}, what would it be about?",
                "What's the most creative excuse you've ever used?",
                "If you could create a new {event_type}, what would it celebrate?",
                "If you could create a new {food} flavor, what would it be?",
                "If you were to create a new {game_type}, what would it be about?",
                "If you could design a {place_type}, what would it be like?",
                "If you could create a new {activity_type}, what would the rules be?"
            ]
        },
        "medium": {
            "challenging": [
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
            ]
        },
        "hot": {
            "flirty": [
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
        }
    }
    
    # Get templates for this category, sub-option, and intensity
    category_templates = templates.get(intensity, {}).get(sub_option_id, [])
    
    # If no specific templates found, use generic ones
    if not category_templates:
        category_templates = [
            f"What's your favorite thing about {sub_option_id}?",
            f"What's something interesting related to {sub_option_id} that you've experienced?",
            f"What's your opinion on {sub_option_id}?",
            f"What's a memorable {sub_option_id} experience you've had?",
            f"How has {sub_option_id} influenced your life?"
        ]
    
    # Select a random template
    template = random.choice(category_templates)
    
    # Define replacements
    replacements = {
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
        "superpower": ["superpower", "magical ability", "special talent", "extraordinary skill"],
        "memory_type": ["childhood memory", "school memory", "family memory", "vacation memory"],
        "character_type": ["cartoon character", "movie character", "book character", "video game character"],
        "item": ["food", "clothing item", "gadget", "toy", "household item"],
        "list_type": ["playlist", "collection", "social media", "search history"],
        "food": ["food", "dessert", "snack", "meal", "cuisine"],
        "action": ["bought", "said", "done", "tried", "created"],
        "creative_work": ["book", "movie", "song", "play", "video game"],
        "event_type": ["holiday", "celebration", "festival", "tradition"],
        "game_type": ["board game", "card game", "video game", "party game"],
        "place_type": ["theme park ride", "museum exhibit", "restaurant concept", "vacation destination"],
        "activity_type": ["sport", "competition", "game", "hobby"],
        "topic": ["past", "childhood", "relationship", "career", "family", "education"],
        "fear": ["fear", "worry", "concern", "anxiety", "apprehension"],
        "relation": ["parents", "best friend", "partner", "family", "colleagues"],
        "mistake": ["mistake", "error", "misjudgment", "poor decision", "regret"],
        "situation": ["relationship", "job", "friendship", "family gathering", "social situation"],
        "secret": ["secret", "hidden truth", "private matter", "confidential information"],
        "turn_on": ["turn-on", "attraction", "flirtatious gesture", "romantic quality"],
        "intimate": ["romantic", "flirtatious", "charming", "attractive"],
        "intimate_verb": ["flirting", "charming someone", "attracting attention"],
        "body_part": ["smile", "eyes", "laugh", "voice", "style"],
        "intimate_activity": ["romance", "flirtation", "attraction"],
        "intimate_touch": ["touch", "gesture", "glance", "compliment"]
    }
    
    # Replace placeholders in the template
    result = template
    import re
    for placeholder in re.findall(r'\{([^}]+)\}', result):
        if placeholder in replacements:
            result = result.replace("{" + placeholder + "}", random.choice(replacements[placeholder]))
    
    return result

def generate_dare_text(category_id, sub_option_id, intensity):
    """Generate a dare challenge based on category, sub-option, and intensity"""
    # Define templates based on category and sub-option
    templates = {
        "soft": {
            "icebreaker": [
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
            "silly": [
                "Do your best {animal} impression for 15 seconds.",
                "Put as many {items} on one {body_part} as you can in 30 seconds.",
                "Speak in an {accent} until your next turn.",
                "Make up a short {creative_work} about {person}.",
                "Do your best impression of a {character_type}.",
                "Try to {impossible_action} for 10 seconds.",
                "Make a hat out of {materials} and wear it for the next three rounds.",
                "Pretend to be a {profession} and give a 30-second {performance_type}.",
                "Do your best impression of a {scene_type}.",
                "Switch {items} with {person} for one round."
            ],
            "creative": [
                "Draw a portrait of {person} with your non-dominant hand.",
                "Create a short commercial for {item}.",
                "Make up a short story that includes {random_elements}.",
                "Create a new dance move and teach it to everyone.",
                "Design a {design_item} using only {materials}.",
                "Create a 30-second jingle about {topic}.",
                "Make up a haiku (5-7-5 syllable poem) about {topic}.",
                "Create a new handshake with {person}.",
                "Using objects around you, create a {creative_item} and {action} a short {performance}.",
                "Create a fashion accessory using only {materials} and wear it for the next three rounds."
            ]
        },
        "medium": {
            "challenging": [
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
            ]
        },
        "hot": {
            "flirty": [
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
    
    # Get templates for this category, sub-option, and intensity
    category_templates = templates.get(intensity, {}).get(sub_option_id, [])
    
    # If no specific templates found, use generic ones
    if not category_templates:
        category_templates = [
            f"Show everyone your best {sub_option_id} face.",
            f"Act out a scenario related to {sub_option_id} for 30 seconds.",
            f"Share a personal story about {sub_option_id} with the group.",
            f"Demonstrate how you would handle a {sub_option_id} situation.",
            f"Create something that represents {sub_option_id} using objects around you."
        ]
    
    # Select a random template
    template = random.choice(category_templates)
    
    # Define replacements
    replacements = {
        "celebrity": ["a celebrity", "a famous person", "a movie star", "a singer", "a public figure"],
        "item": ["photo", "message", "app", "social media post", "saved image"],
        "topic": ["animals", "food", "technology", "movies", "sports", "travel"],
        "song_type": ["your favorite song", "a popular song", "a song everyone knows", "a song from your childhood"],
        "activity": ["on a runway", "in a movie", "at a fancy event", "winning a competition", "on a magazine cover"],
        "person": ["someone in the room", "the person to your right", "the person to your left", "the person across from you", "everyone"],
        "animal": ["dog", "cat", "chicken", "monkey", "elephant", "lion"],
        "random_elements": ["three random words chosen by the group", "objects in this room", "people in the group", "recent events"],
        "items": ["socks", "small objects", "pieces of paper", "clothing items", "accessories"],
        "body_part": ["foot", "hand", "arm", "head", "leg"],
        "accent": ["British accent", "Southern accent", "Australian accent", "made-up accent", "robot voice"],
        "creative_work": ["rap", "poem", "song", "story", "commercial"],
        "character_type": ["cartoon character", "movie villain", "superhero", "reality TV star", "game show host"],
        "impossible_action": ["lick your elbow", "touch your nose with your tongue", "wiggle your ears", "raise one eyebrow"],
        "materials": ["whatever materials are available", "paper", "napkins", "items in your pockets", "things on the table"],
        "profession": ["news reporter", "sports commentator", "tour guide", "cooking show host", "motivational speaker"],
        "performance_type": ["report", "presentation", "speech", "demonstration", "announcement"],
        "scene_type": ["famous movie scene", "TV commercial", "dramatic moment", "comedy sketch", "action sequence"],
        "design_item": ["superhero logo", "fashion accessory", "product packaging", "movie poster", "album cover"],
        "creative_item": ["musical instrument", "art piece", "invention", "game", "decoration"],
        "action": ["perform", "demonstrate", "show", "present", "display"],
        "performance": ["tune", "routine", "show", "presentation", "demonstration"],
        "number": ["3rd", "5th", "10th", "last", "first"],
        "adjective": ["funny", "silly", "creative", "unusual", "surprising"],
        "personal_item": ["phone", "social media", "photo gallery", "music playlist", "recent calls"],
        "profile": ["dating profile bio", "social media profile", "professional bio", "personal description"],
        "verb": ["shared", "posted", "saved", "liked", "commented on"],
        "food_item": ["hot sauce", "mustard", "ketchup", "honey", "lemon juice", "soy sauce"],
        "photo_type": ["profile picture", "social media post", "photo pose", "selfie"],
        "flirty_action": ["flirtatious look", "pick-up line", "flirty walk", "charming gesture"],
        "approach_action": ["approach", "flirt with", "charm", "attract"]
    }
    
    # Replace placeholders in the template
    result = template
    import re
    for placeholder in re.findall(r'\{([^}]+)\}', result):
        if placeholder in replacements:
            result = result.replace("{" + placeholder + "}", random.choice(replacements[placeholder]))
    
    return result

def get_category_stats(items):
    """Get statistics for a category"""
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

def main():
    print("Truth or Dare - Sample Category Generation")
    print("=========================================")
    
    # Define a sample category
    category_id = "soft"
    category_name = "Soft"
    sub_options = [
        {"id": "icebreaker", "name": "Icebreaker"},
        {"id": "silly", "name": "Silly"},
        {"id": "creative", "name": "Creative"}
    ]
    
    # Generate content for the sample category
    print(f"Generating content for {category_name} ({category_id}) category...")
    items = generate_sample_category(category_id, category_name, sub_options)
    
    # Save the content
    output_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "categories", category_id)
    os.makedirs(output_dir, exist_ok=True)
    output_path = os.path.join(output_dir, f"{category_id}_generated.json")
    
    with open(output_path, "w") as f:
        json.dump(items, f, indent=2)
    
    print(f"Saved {category_id} content to {output_path}")
    
    # Print statistics
    stats = get_category_stats(items)
    print(f"Stats for {category_id}:")
    print(f"  Total items: {stats['total']}")
    print(f"  Truth: {stats['truth_count']} ({stats['truth_percentage']:.1f}%)")
    print(f"  Dare: {stats['dare_count']} ({stats['dare_percentage']:.1f}%)")
    print(f"  Sub-option counts: {stats['sub_option_counts']}")
    print(f"  Intensity counts: {stats['intensity_counts']}")
    
    # Generate content for a hot category
    category_id = "hot"
    category_name = "Hot"
    sub_options = [
        {"id": "flirty", "name": "Flirty"},
        {"id": "sensual", "name": "Sensual"},
        {"id": "daring", "name": "Daring"}
    ]
    
    print(f"\nGenerating content for {category_name} ({category_id}) category...")
    items = generate_sample_category(category_id, category_name, sub_options, is_age_restricted=True)
    
    # Save the content
    output_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "categories", category_id)
    os.makedirs(output_dir, exist_ok=True)
    output_path = os.path.join(output_dir, f"{category_id}_generated.json")
    
    with open(output_path, "w") as f:
        json.dump(items, f, indent=2)
    
    print(f"Saved {category_id} content to {output_path}")
    
    # Print statistics
    stats = get_category_stats(items)
    print(f"Stats for {category_id}:")
    print(f"  Total items: {stats['total']}")
    print(f"  Truth: {stats['truth_count']} ({stats['truth_percentage']:.1f}%)")
    print(f"  Dare: {stats['dare_count']} ({stats['dare_percentage']:.1f}%)")
    print(f"  Sub-option counts: {stats['sub_option_counts']}")
    print(f"  Intensity counts: {stats['intensity_counts']}")
    
    print("\nSample category generation complete!")

if __name__ == "__main__":
    main()