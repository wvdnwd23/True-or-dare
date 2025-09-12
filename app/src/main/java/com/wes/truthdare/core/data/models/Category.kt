package com.wes.truthdare.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity representing a category in the game
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val description: String,
    val isAgeRestricted: Boolean = false,
    val requiresOptIn: Boolean = false,
    val order: Int = 0,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // Factory methods for predefined categories
        fun soft() = Category(
            id = "soft",
            name = "Soft",
            emoji = "🙂",
            description = "Light and fun questions to break the ice",
            isAgeRestricted = false,
            requiresOptIn = false,
            order = 1
        )
        
        fun medium() = Category(
            id = "medium",
            name = "Medium",
            emoji = "🔶",
            description = "Moderately challenging questions and dares",
            isAgeRestricted = false,
            requiresOptIn = false,
            order = 2
        )
        
        fun hot() = Category(
            id = "hot",
            name = "Hot 18+",
            emoji = "🔥",
            description = "Flirty and spicy content for adults",
            isAgeRestricted = true,
            requiresOptIn = false,
            order = 3
        )
        
        fun extreme() = Category(
            id = "extreme",
            name = "Extreme 18+",
            emoji = "🚨",
            description = "Intense and daring adult content",
            isAgeRestricted = true,
            requiresOptIn = false,
            order = 4
        )
        
        fun couples() = Category(
            id = "couples",
            name = "Couples",
            emoji = "💑",
            description = "Questions to strengthen your relationship",
            isAgeRestricted = false,
            requiresOptIn = false,
            order = 5
        )
        
        fun couplesHot() = Category(
            id = "couples_hot",
            name = "Couples Hot",
            emoji = "💑🔥",
            description = "Intimate questions and dares for couples",
            isAgeRestricted = true,
            requiresOptIn = false,
            order = 6
        )
        
        fun dirty() = Category(
            id = "dirty",
            name = "Dirty",
            emoji = "😏",
            description = "Explicit adult content and challenges",
            isAgeRestricted = true,
            requiresOptIn = false,
            order = 7
        )
        
        fun deepPast() = Category(
            id = "deep_past",
            name = "Deep Past",
            emoji = "🌌",
            description = "Reflective questions about your past experiences",
            isAgeRestricted = false,
            requiresOptIn = false,
            order = 8
        )
        
        fun mentalHealth() = Category(
            id = "mental_health",
            name = "Mental Health",
            emoji = "🧠",
            description = "Questions about wellbeing and emotional health",
            isAgeRestricted = false,
            requiresOptIn = true,
            order = 9
        )
        
        fun childhood() = Category(
            id = "childhood",
            name = "Childhood",
            emoji = "👶",
            description = "Nostalgic questions about growing up",
            isAgeRestricted = false,
            requiresOptIn = false,
            order = 10
        )
        
        fun selfGrowth() = Category(
            id = "self_growth",
            name = "Self-Growth",
            emoji = "🌱",
            description = "Questions to inspire personal development",
            isAgeRestricted = false,
            requiresOptIn = false,
            order = 11
        )
        
        fun traumaHealing() = Category(
            id = "trauma_healing",
            name = "Trauma & Healing",
            emoji = "❤️‍🩹",
            description = "Supportive questions about difficult experiences",
            isAgeRestricted = false,
            requiresOptIn = true,
            order = 12
        )
        
        fun personal() = Category(
            id = "personal",
            name = "Personal",
            emoji = "🧍",
            description = "Deep questions about your inner self",
            isAgeRestricted = false,
            requiresOptIn = false,
            order = 13
        )
        
        fun selfDiscovery() = Category(
            id = "self_discovery",
            name = "Self-Discovery",
            emoji = "🌟",
            description = "Questions to help you understand yourself better",
            isAgeRestricted = false,
            requiresOptIn = false,
            order = 14
        )
        
        // Get all predefined categories
        fun getAllPredefinedCategories(): List<Category> {
            return listOf(
                soft(),
                medium(),
                hot(),
                extreme(),
                couples(),
                couplesHot(),
                dirty(),
                deepPast(),
                mentalHealth(),
                childhood(),
                selfGrowth(),
                traumaHealing(),
                personal(),
                selfDiscovery()
            )
        }
    }
}