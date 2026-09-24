package com.tagari.smartpantry;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.tagari.smartpantry.databinding.ActivityMainBinding;
import com.tagari.smartpantry.databinding.DialogItemBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PantryDbHelper database;
    private PantryAdapter pantryAdapter;
    private RecipeAdapter recipeAdapter;
    
    private enum ViewMode { PANTRY, RECIPES, FAVORITES }
    private ViewMode currentMode = ViewMode.PANTRY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        database = new PantryDbHelper(this);
        setupUI();
        refreshDisplay();
    }

    private void setupUI() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        pantryAdapter = new PantryAdapter(new ArrayList<>(), new PantryAdapter.OnItemClickListener() {
            @Override
            public void onEdit(PantryItem item) {
                showItemDialog(item);
            }

            @Override
            public void onDelete(PantryItem item) {
                database.deleteItem(item.id);
                refreshDisplay();
                Snackbar.make(binding.getRoot(), "Item deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            database.saveItem(item);
                            refreshDisplay();
                        }).show();
            }
        });

        recipeAdapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            database.setFavorite(recipe.id, !recipe.favorite);
            refreshDisplay();
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                currentMode = ViewMode.PANTRY;
            } else if (id == R.id.nav_recipes) {
                currentMode = ViewMode.RECIPES;
            } else if (id == R.id.nav_favorites) {
                currentMode = ViewMode.FAVORITES;
            }
            refreshDisplay();
            return true;
        });

        binding.fabAdd.setOnClickListener(v -> showItemDialog(null));

        binding.btnQuickAdd.setOnClickListener(v -> {
            String name = (binding.quickAddEditText.getText() != null) ? 
                    binding.quickAddEditText.getText().toString().trim() : "";
            if (!name.isEmpty()) {
                PantryItem item = new PantryItem(0, name, getString(R.string.default_category), 1, getString(R.string.default_unit), "");
                database.saveItem(item);
                binding.quickAddEditText.setText("");
                refreshDisplay();
                Snackbar.make(binding.getRoot(), "Item added: " + name, Snackbar.LENGTH_SHORT).show();
            }
        });

        binding.quickAddEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (currentMode == ViewMode.PANTRY) refreshDisplay();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void refreshDisplay() {
        String searchQuery = (currentMode == ViewMode.PANTRY && binding.quickAddEditText.getText() != null) ? 
                binding.quickAddEditText.getText().toString() : "";
        
        if (currentMode == ViewMode.PANTRY) {
            binding.fabAdd.show();
            binding.quickAddContainer.setVisibility(View.VISIBLE);
            binding.quickAddEditText.setHint(R.string.quick_add_hint);
            
            List<PantryItem> items = database.getPantry(searchQuery);
            pantryAdapter.updateItems(items);
            binding.recyclerView.setAdapter(pantryAdapter);
            toggleEmptyState(items.isEmpty(), getString(R.string.empty_pantry));
            binding.toolbar.setTitle(R.string.pantry_title);
        } else {
            binding.fabAdd.hide();
            binding.quickAddContainer.setVisibility(View.GONE);
            boolean favoritesOnly = (currentMode == ViewMode.FAVORITES);
            
            List<Recipe> allRecipes = database.getRecipes(favoritesOnly);
            List<Recipe> resultRecipes = new ArrayList<>();
            Set<String> available = database.availableIngredients();
            
            for (Recipe recipe : allRecipes) {
                // Fuzzy Match Logic:
                // For each ingredient required by the recipe, check if any pantry item name 
                // contains it OR if the ingredient name contains any pantry item name.
                List<String> missing = new ArrayList<>();
                for (String req : recipe.ingredientSet()) {
                    boolean found = false;
                    for (String have : available) {
                        if (have.contains(req) || req.contains(have)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) missing.add(req);
                }

                // If in favorites, always show.
                // If in recipes, show if no ingredients are missing (Strict) 
                // OR show if user requested to see "Recipe ideas".
                // I'll show full matches first, then partial matches.
                if (favoritesOnly || missing.isEmpty()) {
                    // Update recipe description to show missing items if any (for favorites)
                    if (!missing.isEmpty()) {
                        recipe.description = "Missing: " + String.join(", ", missing);
                    }
                    resultRecipes.add(recipe);
                }
            }
            
            recipeAdapter.updateRecipes(resultRecipes);
            binding.recyclerView.setAdapter(recipeAdapter);
            toggleEmptyState(resultRecipes.isEmpty(), 
                favoritesOnly ? getString(R.string.empty_favorites) : getString(R.string.empty_ideas));
            binding.toolbar.setTitle(favoritesOnly ? R.string.recipes_favorites_title : R.string.recipes_ideas_title);
        }
    }

    private void toggleEmptyState(boolean empty, String message) {
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.emptyState.setText(message);
        binding.recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showItemDialog(PantryItem existing) {
        DialogItemBinding dialogBinding = DialogItemBinding.inflate(getLayoutInflater());
        
        if (existing != null) {
            dialogBinding.editName.setText(existing.name);
            dialogBinding.editCategory.setText(existing.category);
            dialogBinding.editQuantity.setText(String.valueOf(existing.quantity));
            dialogBinding.editUnit.setText(existing.unit);
            dialogBinding.editExpiry.setText(existing.expiry);
        } else {
            dialogBinding.editCategory.setText(R.string.default_category);
            dialogBinding.editQuantity.setText("1");
            dialogBinding.editUnit.setText(R.string.default_unit);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(existing == null ? R.string.dialog_add_title : R.string.dialog_edit_title)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_save, (dialog, which) -> {
                    Editable nameText = dialogBinding.editName.getText();
                    String name = (nameText != null) ? nameText.toString().trim() : "";
                    
                    Editable categoryText = dialogBinding.editCategory.getText();
                    String category = (categoryText != null) ? categoryText.toString().trim() : "";
                    
                    Editable quantityText = dialogBinding.editQuantity.getText();
                    String quantityStr = (quantityText != null) ? quantityText.toString().trim() : "";
                    
                    Editable unitText = dialogBinding.editUnit.getText();
                    String unit = (unitText != null) ? unitText.toString().trim() : "";
                    
                    Editable expiryText = dialogBinding.editExpiry.getText();
                    String expiry = (expiryText != null) ? expiryText.toString().trim() : "";

                    if (name.isEmpty()) {
                        Snackbar.make(binding.getRoot(), R.string.error_name, Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    int quantity;
                    try {
                        quantity = Integer.parseInt(quantityStr);
                    } catch (NumberFormatException e) {
                        Snackbar.make(binding.getRoot(), R.string.error_quantity_number, Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    PantryItem newItem = new PantryItem(
                            existing == null ? 0 : existing.id,
                            name, category, quantity, unit, expiry
                    );
                    database.saveItem(newItem);
                    refreshDisplay();
                    Snackbar.make(binding.getRoot(), R.string.toast_updated, Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }
}
