/**
 * Truth or Dare - Multi-Select Implementation
 * 
 * This file contains the JavaScript implementation for the multi-select
 * category and sub-option functionality.
 */

// State management
const gameState = {
  selectedCategories: [],
  selectedSubOptions: {},
  intensityLevel: 'medium', // 'soft', 'medium', or 'hot'
  ageVerified: false,
  optedInCategories: [],
  currentItems: [],
  currentItemIndex: 0,
  mixMode: false,
  progressiveMode: false,
  roundCount: 0
};

// Category definitions (would be loaded from JSON in production)
let categoryDefinitions = {};
let allContent = {};

// DOM Elements
let categoryContainer;
let subOptionContainer;
let selectionSummary;
let intensitySlider;
let previewPanel;
let startButton;

/**
 * Initialize the application
 */
function initApp() {
  // Load category definitions
  loadCategoryDefinitions()
    .then(() => {
      // Load content samples for preview
      return loadContentSamples();
    })
    .then(() => {
      // Set up UI elements
      setupUIElements();
      
      // Render categories
      renderCategories();
      
      // Set up event listeners
      setupEventListeners();
      
      // Update UI state
      updateSelectionSummary();
      updatePreviewPanel();
    })
    .catch(error => {
      console.error('Error initializing app:', error);
    });
}

/**
 * Load category definitions from JSON
 */
async function loadCategoryDefinitions() {
  try {
    const response = await fetch('../category_definitions.json');
    const data = await response.json();
    categoryDefinitions = data;
    return data;
  } catch (error) {
    console.error('Error loading category definitions:', error);
    throw error;
  }
}

/**
 * Load content samples for preview
 */
async function loadContentSamples() {
  try {
    // In a real implementation, this would load from the content files
    // For now, we'll use placeholder content
    allContent = {
      soft: [
        {
          id: "soft-truth-1",
          type: "truth",
          text: "What's your favorite way to spend a lazy Sunday?",
          category: "soft",
          subOptions: ["icebreaker"],
          intensity: "soft"
        },
        {
          id: "soft-dare-1",
          type: "dare",
          text: "Do your best impression of a celebrity for 30 seconds.",
          category: "soft",
          subOptions: ["icebreaker"],
          intensity: "soft"
        }
      ],
      medium: [
        {
          id: "medium-truth-1",
          type: "truth",
          text: "What's something you've never told anyone in this room?",
          category: "medium",
          subOptions: ["challenging"],
          intensity: "medium"
        },
        {
          id: "medium-dare-1",
          type: "dare",
          text: "Text the 10th person in your contacts something embarrassing (show the group before sending).",
          category: "medium",
          subOptions: ["challenging"],
          intensity: "medium"
        }
      ],
      hot: [
        {
          id: "hot-truth-1",
          type: "truth",
          text: "What's your biggest turn-on?",
          category: "hot",
          subOptions: ["flirty"],
          intensity: "hot",
          ageRestricted: true
        },
        {
          id: "hot-dare-1",
          type: "dare",
          text: "Give a flirty compliment to each person in the room.",
          category: "hot",
          subOptions: ["flirty"],
          intensity: "medium",
          ageRestricted: true
        }
      ]
    };
    return allContent;
  } catch (error) {
    console.error('Error loading content samples:', error);
    throw error;
  }
}

/**
 * Set up UI elements
 */
function setupUIElements() {
  categoryContainer = document.getElementById('categoryContainer');
  subOptionContainer = document.getElementById('subOptionContainer');
  selectionSummary = document.getElementById('selectionSummary');
  intensitySlider = document.getElementById('intensitySlider');
  previewPanel = document.getElementById('previewPanel');
  startButton = document.getElementById('startButton');
}

/**
 * Render categories
 */
function renderCategories() {
  if (!categoryContainer || !categoryDefinitions.categories) return;
  
  categoryContainer.innerHTML = '';
  
  categoryDefinitions.categories.forEach(category => {
    const categoryCard = document.createElement('div');
    categoryCard.className = 'category-card';
    categoryCard.dataset.category = category.id;
    
    // Check if category is selected
    if (gameState.selectedCategories.includes(category.id)) {
      categoryCard.classList.add('selected');
    }
    
    // Create category content
    categoryCard.innerHTML = `
      <div class="category-icon">${category.icon || '📝'}</div>
      <div class="category-info">
        <div class="category-name">${category.name}</div>
        <div class="category-description">${category.description}</div>
        <div class="sub-options" id="subOptions-${category.id}">
          ${renderSubOptions(category)}
        </div>
      </div>
      ${category.ageRestricted ? '<div class="category-badge">18+</div>' : ''}
      ${category.requiresOptIn ? '<div class="category-badge opt-in">Opt-In</div>' : ''}
    `;
    
    categoryContainer.appendChild(categoryCard);
  });
}

/**
 * Render sub-options for a category
 */
function renderSubOptions(category) {
  if (!category.subOptions) return '';
  
  return category.subOptions.map(subOption => {
    const isSelected = gameState.selectedSubOptions[category.id]?.includes(subOption.id);
    return `
      <div class="sub-option-chip ${isSelected ? 'selected' : ''}" 
           data-category="${category.id}" 
           data-sub-option="${subOption.id}">
        ${subOption.name}
      </div>
    `;
  }).join('');
}

/**
 * Set up event listeners
 */
function setupEventListeners() {
  // Category selection
  document.addEventListener('click', function(event) {
    const categoryCard = event.target.closest('.category-card');
    if (categoryCard) {
      const categoryId = categoryCard.dataset.category;
      toggleCategory(categoryId);
    }
  });
  
  // Sub-option selection
  document.addEventListener('click', function(event) {
    const subOptionChip = event.target.closest('.sub-option-chip');
    if (subOptionChip) {
      event.stopPropagation(); // Prevent triggering category selection
      const categoryId = subOptionChip.dataset.category;
      const subOptionId = subOptionChip.dataset.subOption;
      toggleSubOption(categoryId, subOptionId);
    }
  });
  
  // Clear all button
  document.getElementById('clearAllButton').addEventListener('click', function() {
    clearAllSelections();
  });
  
  // Quick picks
  document.querySelectorAll('.quick-pick-button').forEach((button, index) => {
    button.addEventListener('click', function() {
      applyQuickPick(index);
    });
  });
  
  // Intensity slider
  intensitySlider.addEventListener('input', function() {
    updateIntensity();
  });
  
  // Start button
  startButton.addEventListener('click', function() {
    startGame();
  });
}

/**
 * Toggle category selection
 */
function toggleCategory(categoryId) {
  const category = categoryDefinitions.categories.find(c => c.id === categoryId);
  if (!category) return;
  
  // Check if category requires age verification
  if (category.ageRestricted && !gameState.ageVerified) {
    showAgeVerificationModal(categoryId);
    return;
  }
  
  // Check if category requires opt-in
  if (category.requiresOptIn && !gameState.optedInCategories.includes(categoryId)) {
    showOptInModal(categoryId, category.explicitOptIn);
    return;
  }
  
  // Toggle category selection
  const index = gameState.selectedCategories.indexOf(categoryId);
  if (index === -1) {
    // Add category
    gameState.selectedCategories.push(categoryId);
    
    // Initialize sub-options array for this category
    if (!gameState.selectedSubOptions[categoryId]) {
      gameState.selectedSubOptions[categoryId] = [];
    }
  } else {
    // Remove category
    gameState.selectedCategories.splice(index, 1);
    
    // Remove sub-options for this category
    delete gameState.selectedSubOptions[categoryId];
  }
  
  // Update UI
  renderCategories();
  updateSelectionSummary();
  updatePreviewPanel();
}

/**
 * Toggle sub-option selection
 */
function toggleSubOption(categoryId, subOptionId) {
  // Ensure category is selected
  if (!gameState.selectedCategories.includes(categoryId)) {
    toggleCategory(categoryId);
    return;
  }
  
  // Initialize sub-options array if needed
  if (!gameState.selectedSubOptions[categoryId]) {
    gameState.selectedSubOptions[categoryId] = [];
  }
  
  // Toggle sub-option
  const index = gameState.selectedSubOptions[categoryId].indexOf(subOptionId);
  if (index === -1) {
    // Add sub-option
    gameState.selectedSubOptions[categoryId].push(subOptionId);
  } else {
    // Remove sub-option
    gameState.selectedSubOptions[categoryId].splice(index, 1);
  }
  
  // Update UI
  renderCategories();
  updateSelectionSummary();
  updatePreviewPanel();
}

/**
 * Clear all selections
 */
function clearAllSelections() {
  gameState.selectedCategories = [];
  gameState.selectedSubOptions = {};
  
  // Update UI
  renderCategories();
  updateSelectionSummary();
  updatePreviewPanel();
}

/**
 * Apply quick pick
 */
function applyQuickPick(index) {
  // Clear current selections
  clearAllSelections();
  
  // Apply quick pick based on index
  if (index === 0) { // Party
    applyPartyQuickPick();
  } else if (index === 1) { // Deep
    applyDeepQuickPick();
  } else if (index === 2) { // Couples
    applyCouplesQuickPick();
  }
}

/**
 * Apply Party quick pick
 */
function applyPartyQuickPick() {
  const partyCategories = categoryDefinitions.quickPicks.party;
  
  // Add each category
  partyCategories.forEach(categoryId => {
    if (!gameState.selectedCategories.includes(categoryId)) {
      gameState.selectedCategories.push(categoryId);
      gameState.selectedSubOptions[categoryId] = [];
    }
  });
  
  // Update UI
  renderCategories();
  updateSelectionSummary();
  updatePreviewPanel();
}

/**
 * Apply Deep quick pick
 */
function applyDeepQuickPick() {
  const deepCategories = categoryDefinitions.quickPicks.deep;
  
  // Add each category
  deepCategories.forEach(categoryId => {
    if (!gameState.selectedCategories.includes(categoryId)) {
      gameState.selectedCategories.push(categoryId);
      gameState.selectedSubOptions[categoryId] = [];
    }
  });
  
  // Update UI
  renderCategories();
  updateSelectionSummary();
  updatePreviewPanel();
}

/**
 * Apply Couples quick pick
 */
function applyCouplesQuickPick() {
  const couplesCategories = categoryDefinitions.quickPicks.couples;
  
  // Add each category
  couplesCategories.forEach(categoryId => {
    if (!gameState.selectedCategories.includes(categoryId)) {
      gameState.selectedCategories.push(categoryId);
      gameState.selectedSubOptions[categoryId] = [];
    }
  });
  
  // Update UI
  renderCategories();
  updateSelectionSummary();
  updatePreviewPanel();
}

/**
 * Update intensity based on slider
 */
function updateIntensity() {
  const value = intensitySlider.value;
  
  if (value == 1) {
    gameState.intensityLevel = 'soft';
  } else if (value == 2) {
    gameState.intensityLevel = 'medium';
  } else {
    gameState.intensityLevel = 'hot';
  }
  
  // Update UI
  updatePreviewPanel();
}

/**
 * Update selection summary
 */
function updateSelectionSummary() {
  if (!selectionSummary) return;
  
  const categoryCount = gameState.selectedCategories.length;
  
  // Count sub-options
  let subOptionCount = 0;
  Object.values(gameState.selectedSubOptions).forEach(subOptions => {
    subOptionCount += subOptions.length;
  });
  
  selectionSummary.textContent = `Selected: ${categoryCount} categories • ${subOptionCount} sub-options`;
}

/**
 * Update preview panel with sample items
 */
function updatePreviewPanel() {
  if (!previewPanel) return;
  
  // Clear current preview
  previewPanel.innerHTML = '<h3>Preview</h3>';
  
  // If no categories selected, show message
  if (gameState.selectedCategories.length === 0) {
    previewPanel.innerHTML += '<div class="preview-empty">Select categories to see preview items</div>';
    return;
  }
  
  // Get filtered items
  const filteredItems = getFilteredItems();
  
  // If no items match filters, show message
  if (filteredItems.length === 0) {
    previewPanel.innerHTML += '<div class="preview-empty">No items match your current selections</div>';
    return;
  }
  
  // Show up to 3 preview items
  const previewItems = filteredItems.slice(0, 3);
  
  previewItems.forEach(item => {
    const previewItem = document.createElement('div');
    previewItem.className = 'preview-item';
    
    const typeSpan = document.createElement('span');
    typeSpan.className = `preview-type ${item.type}`;
    typeSpan.textContent = item.type.charAt(0).toUpperCase() + item.type.slice(1);
    
    const textDiv = document.createElement('div');
    textDiv.className = 'preview-text';
    textDiv.textContent = item.text;
    
    previewItem.appendChild(typeSpan);
    previewItem.appendChild(textDiv);
    
    previewPanel.appendChild(previewItem);
  });
}

/**
 * Get filtered items based on current selections
 */
function getFilteredItems() {
  let filteredItems = [];
  
  // For each selected category
  gameState.selectedCategories.forEach(categoryId => {
    // Get items for this category
    const categoryItems = allContent[categoryId] || [];
    
    // Filter by sub-options if any are selected
    let subOptionFiltered = categoryItems;
    if (gameState.selectedSubOptions[categoryId]?.length > 0) {
      subOptionFiltered = categoryItems.filter(item => {
        return item.subOptions.some(subOption => 
          gameState.selectedSubOptions[categoryId].includes(subOption)
        );
      });
    }
    
    // Filter by intensity
    const intensityFiltered = subOptionFiltered.filter(item => {
      if (gameState.intensityLevel === 'soft') {
        return item.intensity === 'soft';
      } else if (gameState.intensityLevel === 'medium') {
        return ['soft', 'medium'].includes(item.intensity);
      } else {
        return ['soft', 'medium', 'hot'].includes(item.intensity);
      }
    });
    
    // Filter by age restriction
    const ageFiltered = intensityFiltered.filter(item => {
      return !item.ageRestricted || gameState.ageVerified;
    });
    
    // Filter by opt-in requirement
    const optInFiltered = ageFiltered.filter(item => {
      return !item.requiresOptIn || gameState.optedInCategories.includes(item.category);
    });
    
    // Add to filtered items
    filteredItems = filteredItems.concat(optInFiltered);
  });
  
  return filteredItems;
}

/**
 * Show age verification modal
 */
function showAgeVerificationModal(categoryId) {
  const modal = document.getElementById('ageVerificationModal');
  if (!modal) return;
  
  // Show modal
  modal.style.display = 'flex';
  
  // Set up event listeners
  const cancelButton = modal.querySelector('.cancel');
  const confirmButton = modal.querySelector('.confirm');
  
  cancelButton.onclick = function() {
    modal.style.display = 'none';
  };
  
  confirmButton.onclick = function() {
    // Set age verified
    gameState.ageVerified = true;
    
    // Hide modal
    modal.style.display = 'none';
    
    // Toggle category
    toggleCategory(categoryId);
  };
}

/**
 * Show opt-in modal
 */
function showOptInModal(categoryId, isExplicit) {
  const modalId = isExplicit ? 'explicitOptInModal' : 'optInModal';
  const modal = document.getElementById(modalId);
  if (!modal) return;
  
  // Show modal
  modal.style.display = 'flex';
  
  // Set up event listeners
  const cancelButton = modal.querySelector('.cancel');
  const confirmButton = modal.querySelector('.confirm');
  
  cancelButton.onclick = function() {
    modal.style.display = 'none';
  };
  
  confirmButton.onclick = function() {
    // Add to opted-in categories
    if (!gameState.optedInCategories.includes(categoryId)) {
      gameState.optedInCategories.push(categoryId);
    }
    
    // Hide modal
    modal.style.display = 'none';
    
    // Toggle category
    toggleCategory(categoryId);
  };
}

/**
 * Start the game
 */
function startGame() {
  // Check if any categories are selected
  if (gameState.selectedCategories.length === 0) {
    alert('Please select at least one category to start the game.');
    return;
  }
  
  // Get filtered items
  gameState.currentItems = getFilteredItems();
  
  // Check if we have enough items
  if (gameState.currentItems.length < 10) {
    alert('Not enough items match your current selections. Please adjust your selections.');
    return;
  }
  
  // Shuffle items
  shuffleArray(gameState.currentItems);
  
  // Reset game state
  gameState.currentItemIndex = 0;
  gameState.roundCount = 0;
  
  // In a real implementation, this would navigate to the game screen
  alert('Game starting with ' + gameState.currentItems.length + ' items!');
}

/**
 * Shuffle array in place
 */
function shuffleArray(array) {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]];
  }
}

// Initialize the app when the DOM is loaded
document.addEventListener('DOMContentLoaded', initApp);