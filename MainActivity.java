package com.tagari.smartpantry;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
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
    
    private enum ViewMode { PANTRY, RECIPES, ALMOST_THERE, FAVORITES }
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

        binding.navIngredients.setOnClickListener(v -> {
            currentMode = ViewMode.PANTRY;
            refreshDisplay();
        });

        binding.navRecipes.setOnClickListener(v -> {
            currentMode = ViewMode.RECIPES;
            refreshDisplay();
        });

        binding.navAlmostThere.setOnClickListener(v -> {
            currentMode = ViewMode.ALMOST_THERE;
            refreshDisplay();
        });

        binding.navSaved.setOnClickListener(v -> {
            currentMode = ViewMode.FAVORITES;
            refreshDisplay();
        });

        binding.fabAdd.setOnClickListener(v -> showItemDialog(null));

        binding.btnQuickAdd.setOnClickListener(v -> {
            String name = (binding.quickAddEditText.getText() != null) ? 
                    binding.quickAddEditText.getText().toString().trim() : "";
            if (name.isEmpty()) {
                binding.quickAddEditText.setError(getString(R.string.error_name));
                
                View alertView = getLayoutInflater().inflate(R.layout.dialog_custom_alert, null);
                AlertDialog alert = new MaterialAlertDialogBuilder(this)
                        .setView(alertView)
                        .setCancelable(true)
                        .create();
                
                View btnClose = alertView.findViewById(R.id.btnCloseAlert);
                if (btnClose != null) {
                    btnClose.setOnClickListener(view -> alert.dismiss());
                }
                alert.show();
                return;
            }
            PantryItem item = new PantryItem(0, name, getString(R.string.default_category), 1, getString(R.string.default_unit), "");
            database.saveItem(item);
            binding.quickAddEditText.setText("");
            refreshDisplay();
            Snackbar.make(binding.getRoot(), "Item added: " + name, Snackbar.LENGTH_SHORT).show();
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

    private boolean isIngredientAvailable(String req, Set<String> available) {
        req = req.trim().toLowerCase();
        String reqSingular = singularize(req);
        
        for (String have : available) {
            have = have.trim().toLowerCase();
            if (have.equals(req) || have.contains(req) || req.contains(have)) {
                return true;
            }
            String haveSingular = singularize(have);
            if (haveSingular.equals(reqSingular) || haveSingular.contains(reqSingular) || reqSingular.contains(haveSingular)) {
                return true;
            }
        }
        return false;
    }

    private String singularize(String word) {
        if (word.endsWith("es") && word.length() > 3) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("s") && word.length() > 2 && !word.endsWith("ss")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
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
            
            List<RecipeAdapter.RecipeItem> items = new ArrayList<>();
            Set<String> available = database.availableIngredients();

            if (currentMode == ViewMode.FAVORITES) {
                List<Recipe> allRecipes = database.getRecipes(true);
                for (Recipe recipe : allRecipes) {
                    List<String> missing = new ArrayList<>();
                    for (String req : recipe.ingredientSet()) {
                        if (!isIngredientAvailable(req, available)) {
                            missing.add(req);
                        }
                    }
                    if (!missing.isEmpty()) {
                        recipe.description = "Missing: " + String.join(", ", missing);
                    }
                    items.add(new RecipeAdapter.RecipeItem(recipe));
                }
                recipeAdapter.updateItems(items);
                binding.recyclerView.setAdapter(recipeAdapter);
                toggleEmptyState(items.isEmpty(), getString(R.string.empty_favorites));
                binding.toolbar.setTitle(R.string.recipes_favorites_title);
            } else if (currentMode == ViewMode.RECIPES) {
                List<Recipe> allRecipes = database.getRecipes(false);
                for (Recipe recipe : allRecipes) {
                    List<String> missing = new ArrayList<>();
                    for (String req : recipe.ingredientSet()) {
                        if (!isIngredientAvailable(req, available)) {
                            missing.add(req);
                        }
                    }
                    if (missing.isEmpty()) {
                        items.add(new RecipeAdapter.RecipeItem(recipe));
                    }
                }
                recipeAdapter.updateItems(items);
                binding.recyclerView.setAdapter(recipeAdapter);
                toggleEmptyState(items.isEmpty(), getString(R.string.empty_ideas));
                binding.toolbar.setTitle(R.string.recipes_ideas_title);
            } else if (currentMode == ViewMode.ALMOST_THERE) {
                List<Recipe> allRecipes = database.getRecipes(false);
                for (Recipe recipe : allRecipes) {
                    List<String> missing = new ArrayList<>();
                    for (String req : recipe.ingredientSet()) {
                        if (!isIngredientAvailable(req, available)) {
                            missing.add(req);
                        }
                    }
                    if (missing.size() == 1) {
                        recipe.description = "⚠️ Almost There! Missing: " + missing.get(0);
                        items.add(new RecipeAdapter.RecipeItem(recipe));
                    }
                }
                recipeAdapter.updateItems(items);
                binding.recyclerView.setAdapter(recipeAdapter);
                toggleEmptyState(items.isEmpty(), getString(R.string.empty_almost_there));
                binding.toolbar.setTitle(R.string.almost_there_title);
            }
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
                    String category = dialogBinding.editCategory.getText() != null ? dialogBinding.editCategory.getText().toString().trim() : getString(R.string.default_category);
                    int quantity = 1;
                    try {
                        if (dialogBinding.editQuantity.getText() != null) {
                            quantity = Integer.parseInt(dialogBinding.editQuantity.getText().toString().trim());
                        }
                    } catch (NumberFormatException ignored) {}
                    String unit = dialogBinding.editUnit.getText() != null ? dialogBinding.editUnit.getText().toString().trim() : getString(R.string.default_unit);
                    String expiry = dialogBinding.editExpiry.getText() != null ? dialogBinding.editExpiry.getText().toString().trim() : "";

                    if (name.isEmpty()) {
                        dialogBinding.editName.setError(getString(R.string.error_name));
                        return;
                    }

                    PantryItem item = new PantryItem(existing == null ? 0 : existing.id, name, category, quantity, unit, expiry);
                    database.saveItem(item);
                    refreshDisplay();
                    Snackbar.make(binding.getRoot(), R.string.toast_updated, Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }
}
