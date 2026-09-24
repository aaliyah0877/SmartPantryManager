package com.tagari.smartpantry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Recipe {
    public long id;
    public String name;
    public String description;
    public String ingredients;
    public boolean favorite;

    public Recipe(long id, String name, String description, String ingredients, boolean favorite) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ingredients = ingredients;
        this.favorite = favorite;
    }

    public Set<String> ingredientSet() {
        Set<String> result = new HashSet<>();
        for (String ingredient : ingredients.split(",")) {
            result.add(ingredient.trim().toLowerCase());
        }
        return result;
    }
}
