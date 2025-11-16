package com.collab.productivity.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

/**
 * Helper class for managing mood emojis and types for notes
 */
public class MoodHelper {

    public static class Mood {
        private String emoji;
        private String type;
        private String description;
        private int intensity;

        public Mood(String emoji, String type, String description, int intensity) {
            this.emoji = emoji;
            this.type = type;
            this.description = description;
            this.intensity = intensity;
        }

        public String getEmoji() { return emoji; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public int getIntensity() { return intensity; }

        public String getDisplayText() {
            return emoji + " " + description;
        }
    }

    // Predefined mood categories
    public static final Map<String, List<Mood>> MOOD_CATEGORIES = new HashMap<>();

    static {
        // Happy moods
        List<Mood> happyMoods = new ArrayList<>();
        happyMoods.add(new Mood("😊", "happy", "Happy", 4));
        happyMoods.add(new Mood("😄", "joyful", "Joyful", 5));
        happyMoods.add(new Mood("🥰", "loved", "Loved", 5));
        happyMoods.add(new Mood("😎", "cool", "Cool", 4));
        happyMoods.add(new Mood("🤗", "grateful", "Grateful", 4));
        happyMoods.add(new Mood("😇", "blessed", "Blessed", 5));
        MOOD_CATEGORIES.put("Happy", happyMoods);

        // Sad moods
        List<Mood> sadMoods = new ArrayList<>();
        sadMoods.add(new Mood("😢", "sad", "Sad", 2));
        sadMoods.add(new Mood("😔", "disappointed", "Disappointed", 2));
        sadMoods.add(new Mood("😞", "dejected", "Dejected", 1));
        sadMoods.add(new Mood("😭", "crying", "Crying", 1));
        sadMoods.add(new Mood("💔", "heartbroken", "Heartbroken", 1));
        sadMoods.add(new Mood("😿", "sorry", "Sorry", 2));
        MOOD_CATEGORIES.put("Sad", sadMoods);

        // Angry moods
        List<Mood> angryMoods = new ArrayList<>();
        angryMoods.add(new Mood("😠", "angry", "Angry", 2));
        angryMoods.add(new Mood("😡", "furious", "Furious", 1));
        angryMoods.add(new Mood("🤬", "enraged", "Enraged", 1));
        angryMoods.add(new Mood("😤", "huffing", "Frustrated", 2));
        angryMoods.add(new Mood("👿", "devil", "Devil", 1));
        angryMoods.add(new Mood("🔥", "heated", "Heated", 2));
        MOOD_CATEGORIES.put("Angry", angryMoods);

        // Anxious moods
        List<Mood> anxiousMoods = new ArrayList<>();
        anxiousMoods.add(new Mood("😰", "anxious", "Anxious", 2));
        anxiousMoods.add(new Mood("😨", "fearful", "Fearful", 1));
        anxiousMoods.add(new Mood("😱", "shocked", "Shocked", 1));
        anxiousMoods.add(new Mood("🤯", "mindblown", "Mind Blown", 3));
        anxiousMoods.add(new Mood("😬", "grimacing", "Nervous", 2));
        anxiousMoods.add(new Mood("🫨", "shaking", "Shaking", 1));
        MOOD_CATEGORIES.put("Anxious", anxiousMoods);

        // Calm moods
        List<Mood> calmMoods = new ArrayList<>();
        calmMoods.add(new Mood("😌", "relieved", "Relieved", 4));
        calmMoods.add(new Mood("🧘", "meditative", "Meditative", 5));
        calmMoods.add(new Mood("😴", "sleepy", "Sleepy", 3));
        calmMoods.add(new Mood("🕊️", "peaceful", "Peaceful", 5));
        calmMoods.add(new Mood("🌸", "serene", "Serene", 4));
        calmMoods.add(new Mood("💤", "resting", "Resting", 3));
        MOOD_CATEGORIES.put("Calm", calmMoods);

        // Excited moods
        List<Mood> excitedMoods = new ArrayList<>();
        excitedMoods.add(new Mood("🤩", "starstruck", "Star Struck", 5));
        excitedMoods.add(new Mood("🎉", "celebrating", "Celebrating", 5));
        excitedMoods.add(new Mood("🚀", "rocket", "Rocket", 5));
        excitedMoods.add(new Mood("⚡", "energetic", "Energetic", 4));
        excitedMoods.add(new Mood("🔥", "fire", "On Fire", 5));
        excitedMoods.add(new Mood("💫", "sparkles", "Sparkles", 4));
        MOOD_CATEGORIES.put("Excited", excitedMoods);
    }

    /**
     * Get all available moods as a flat list
     */
    public static List<Mood> getAllMoods() {
        List<Mood> allMoods = new ArrayList<>();
        for (List<Mood> categoryMoods : MOOD_CATEGORIES.values()) {
            allMoods.addAll(categoryMoods);
        }
        return allMoods;
    }

    /**
     * Find a mood by its emoji
     */
    public static Mood findMoodByEmoji(String emoji) {
        for (List<Mood> categoryMoods : MOOD_CATEGORIES.values()) {
            for (Mood mood : categoryMoods) {
                if (mood.getEmoji().equals(emoji)) {
                    return mood;
                }
            }
        }
        return null;
    }

    /**
     * Get mood recommendations based on time of day
     */
    public static List<Mood> getMoodRecommendations() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        List<Mood> recommendations = new ArrayList<>();

        if (hour >= 6 && hour < 12) {
            // Morning - energetic and positive moods
            recommendations.add(new Mood("☀️", "morning", "Morning Energy", 4));
            recommendations.add(new Mood("🌅", "sunrise", "Fresh Start", 4));
            recommendations.add(new Mood("☕", "coffee", "Coffee Time", 3));
        } else if (hour >= 12 && hour < 17) {
            // Afternoon - productive and focused moods
            recommendations.add(new Mood("💪", "productive", "Productive", 4));
            recommendations.add(new Mood("🎯", "focused", "Focused", 4));
            recommendations.add(new Mood("⚡", "energetic", "Energetic", 4));
        } else if (hour >= 17 && hour < 21) {
            // Evening - relaxed and social moods
            recommendations.add(new Mood("🍽️", "dinner", "Dinner Time", 3));
            recommendations.add(new Mood("👥", "social", "Social", 4));
            recommendations.add(new Mood("🏠", "home", "Home Sweet Home", 4));
        } else {
            // Night - calm and reflective moods
            recommendations.add(new Mood("🌙", "night", "Night Owl", 3));
            recommendations.add(new Mood("⭐", "starry", "Starry Night", 4));
            recommendations.add(new Mood("😴", "sleepy", "Sleepy", 3));
        }

        return recommendations;
    }

    /**
     * Get mood category names
     */
    public static List<String> getMoodCategories() {
        return new ArrayList<>(MOOD_CATEGORIES.keySet());
    }

    /**
     * Get moods for a specific category
     */
    public static List<Mood> getMoodsForCategory(String category) {
        return MOOD_CATEGORIES.getOrDefault(category, new ArrayList<>());
    }
}
