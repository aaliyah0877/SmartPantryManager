package com.tagari.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PantryDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 5;

    public PantryDbHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, category TEXT NOT NULL, quantity INTEGER NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, description TEXT NOT NULL, ingredients TEXT NOT NULL)");
        db.execSQL("CREATE TABLE favorites (recipe_id INTEGER PRIMARY KEY, FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)");
        seedRecipes(db);
    }

    private void seedRecipes(SQLiteDatabase db) {
        // Essential Recipes from Assignment logic
        addRecipe(db, "Chocolate Cupcakes", 
            "Beat eggs and sugar until light. Mix oil, cocoa, water and vanilla. Sift in flour and baking powder. Combine and bake at 180°C for 20 mins.", 
            "eggs, sugar, flour, cocoa, oil");

        addRecipe(db, "Lentil Curry", 
            "Sauté onion and garlic. Add lentils, coconut milk, and curry powder. Simmer until thick. Serve with rice.", 
            "lentils, onion, garlic, coconut milk, curry powder");

        addRecipe(db, "Spinach & Ricotta Lasagna", 
            "Layer lasagna sheets with spinach, ricotta, tomato sauce, and mozzarella. Bake until golden and bubbly.", 
            "spinach, ricotta, tomato sauce, mozzarella, lasagna sheets");

        addRecipe(db, "Mushroom Alfredo", 
            "Boil tagliatelle. Sauté mushrooms, onion and garlic. Add butter, cream and cheese. Toss with pasta.", 
            "mushrooms, onion, garlic, tagliatelle, cream, cheese, butter");

        addRecipe(db, "Tomato Pasta", 
            "Cook pasta. Sauté garlic in olive oil, add crushed tomatoes and herbs. Combine and serve.", 
            "pasta, tomato, garlic, olive oil");

        addRecipe(db, "Garden Omelette", 
            "Whisk eggs. Sauté onion and tomato. Pour eggs over and top with cheese. Fold and serve.", 
            "eggs, onion, tomato, cheese");

        addRecipe(db, "Quinoa Salad", 
            "Cook quinoa. Toss with cucumber, tomato, feta, and lemon juice. Add a drizzle of olive oil.", 
            "quinoa, cucumber, tomato, feta cheese, lemon juice, olive oil");
            
        addRecipe(db, "Grilled Lemon Chicken",
            "Marinate chicken in lemon juice and garlic. Grill until tender. Garnish with fresh herbs.",
            "chicken, lemon juice, garlic, olive oil");

        addRecipe(db, "Butterscotch Pecan Ice Cream",
            "Make a paste with cornflour, custard powder, and milk powder. Add to boiling milk. Add condensed milk and cream, cool. Mix in butterscotch pecan sauce and churn.",
            "full cream milk, cornflour, custard powder, milk powder, condensed milk, nestle cream, fresh cream, butter, brown sugar, pecan nuts, dark brown sugar");

        addRecipe(db, "Garlic Butter Crayfish",
            "Bake crayfish with garlic butter and herbs. Serve with a creamy peppercorn sauce and warm rolls.",
            "crayfish, garlic butter, black peppercorns, italian mixed herbs, fresh cream, butter, flour, salt, sugar");

        addRecipe(db, "Broth Noodles with Prawns",
            "Boil broth with spices for 15 mins. Add noodles and vegetables. Sauté prawns in butter and serve over noodles.",
            "red chillies, soy sauce, fish spice, worcestershire sauce, garlic, ginger, basil, coriander, onion, beef stock, shiitake mushrooms, carrots, mange tout, baby corn, egg noodles, prawns, salt, pepper, butter");

        addRecipe(db, "Hot Chocolate",
            "Mix cocoa, sugar, and milk. Boil while stirring. Add dark chocolate until smooth. Garnish and serve.",
            "cocoa powder, sugar, cornflour, salt, milk, dark chocolate");

        addRecipe(db, "Energade Mocktail",
            "Add energade concentrate to glass, top with ice and sprite. Garnish with blueberries.",
            "energade concentrate, ice cubes, sprite, blueberries");

        addRecipe(db, "Nutella Ferrero Milkshake",
            "Blend milk, ice cream, sugar, and nutella. Add chopped Ferreros and blend again. Top with whipped cream.",
            "milk, chocolate ice cream, sugar, nutella, ferrero chocolates");

        addRecipe(db, "Classic Pancakes",
            "Mix flour, sugar, baking powder, salt, milk, oil, and egg. Cook on griddle until golden.",
            "flour, sugar, baking powder, salt, milk, vegetable oil, egg");

        addRecipe(db, "Homemade Chicken Soup",
            "Boil chicken and vegetables until tender. Chop meat, return to pot, season, and serve hot.",
            "whole chicken, carrots, celery, onion, salt, pepper, bouillon granules");

        addRecipe(db, "Teriyaki Chicken",
            "Make teriyaki sauce. Bake chicken thighs with sauce until glazed and cooked through.",
            "sugar, soy sauce, apple cider vinegar, cornstarch, garlic, ginger, black pepper, chicken thighs");

        addRecipe(db, "Spicy Salmon Sushi Bites",
            "Prepare sushi rice. Air fry rice squares. Top with salmon mix, avocado, and unagi sauce.",
            "cooked rice, rice vinegar, honey, salt, canned salmon, ponzu sauce, sesame oil, japanese mayo, sriracha, green onions, avocado, unagi sauce, furikake");
    }

    private void addRecipe(SQLiteDatabase db, String name, String description, String ingredients) {
        ContentValues values = new ContentValues();
        values.put("name", name); values.put("description", description); values.put("ingredients", ingredients);
        db.insert("recipes", null, values);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { db.execSQL("DROP TABLE IF EXISTS favorites"); db.execSQL("DROP TABLE IF EXISTS recipes"); db.execSQL("DROP TABLE IF EXISTS pantry"); onCreate(db); }

    public List<PantryItem> getPantry(String search) {
        List<PantryItem> items = new ArrayList<>();
        String selection = search == null || search.trim().isEmpty() ? null : "name LIKE ?";
        String[] args = selection == null ? null : new String[]{"%" + search.trim() + "%"};
        try (Cursor cursor = getReadableDatabase().query("pantry", null, selection, args, null, null, "expiry ASC, name ASC")) {
            while (cursor.moveToNext()) items.add(new PantryItem(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4), cursor.getString(5)));
        }
        return items;
    }

    public long saveItem(PantryItem item) {
        ContentValues values = new ContentValues(); values.put("name", item.name); values.put("category", item.category); values.put("quantity", item.quantity); values.put("unit", item.unit); values.put("expiry", item.expiry);
        if (item.id == 0) return getWritableDatabase().insert("pantry", null, values);
        getWritableDatabase().update("pantry", values, "id=?", new String[]{String.valueOf(item.id)}); return item.id;
    }

    public void deleteItem(long id) { getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)}); }

    public List<Recipe> getRecipes(boolean favoritesOnly) {
        List<Recipe> recipes = new ArrayList<>();
        String query = "SELECT r.id, r.name, r.description, r.ingredients, CASE WHEN f.recipe_id IS NULL THEN 0 ELSE 1 END FROM recipes r LEFT JOIN favorites f ON r.id=f.recipe_id" + (favoritesOnly ? " WHERE f.recipe_id IS NOT NULL" : "") + " ORDER BY r.name";
        try (Cursor cursor = getReadableDatabase().rawQuery(query, null)) {
            while (cursor.moveToNext()) recipes.add(new Recipe(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4) == 1));
        }
        return recipes;
    }

    public void setFavorite(long recipeId, boolean favorite) {
        if (favorite) { ContentValues values = new ContentValues(); values.put("recipe_id", recipeId); getWritableDatabase().insertWithOnConflict("favorites", null, values, SQLiteDatabase.CONFLICT_IGNORE); }
        else getWritableDatabase().delete("favorites", "recipe_id=?", new String[]{String.valueOf(recipeId)});
    }

    public Set<String> availableIngredients() {
        Set<String> ingredients = new HashSet<>();
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT name FROM pantry WHERE quantity > 0", null)) { while (cursor.moveToNext()) ingredients.add(cursor.getString(0).trim().toLowerCase()); }
        return ingredients;
    }
}
