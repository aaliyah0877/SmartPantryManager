package com.tagari.smartpantry;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tagari.smartpantry.databinding.ItemRecipeBinding;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;
    private final OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onToggleFavorite(Recipe recipe);
    }

    public RecipeAdapter(List<Recipe> recipes, OnFavoriteClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecipeBinding binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RecipeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.binding.recipeName.setText(recipe.name);
        holder.binding.recipeDescription.setText(recipe.description);
        holder.binding.recipeIngredients.setText(recipe.ingredients);
        
        int starIcon = recipe.favorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off;
        holder.binding.btnFavorite.setIconResource(starIcon);
        
        holder.binding.btnFavorite.setOnClickListener(v -> listener.onToggleFavorite(recipe));

        // Design: Set Recipe Icon based on name
        String name = recipe.name.toLowerCase();
        if (name.contains("cupcake") || name.contains("pancake")) {
            holder.binding.recipeIcon.setImageResource(R.drawable.ic_cupcake);
        } else if (name.contains("ice cream") || name.contains("panna cotta")) {
            holder.binding.recipeIcon.setImageResource(R.drawable.ic_ice_cream);
        } else {
            holder.binding.recipeIcon.setImageResource(R.drawable.ic_restaurant);
        }

        // Design: Set camera distance for 3D effect
        float scale = holder.itemView.getContext().getResources().getDisplayMetrics().density;
        holder.binding.recipeFront.setCameraDistance(10000 * scale);
        holder.binding.recipeBack.setCameraDistance(10000 * scale);

        // Design: Start Animations
        holder.startFancyAnimations();

        holder.itemView.setOnClickListener(v -> holder.flipCard());
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ItemRecipeBinding binding;
        private boolean isFront = true;
        private ObjectAnimator rotationAnim;

        RecipeViewHolder(ItemRecipeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void startFancyAnimations() {
            // 1. Rotating Glow (Back)
            if (rotationAnim == null) {
                rotationAnim = ObjectAnimator.ofFloat(binding.glowView, View.ROTATION, 0f, 360f);
                rotationAnim.setDuration(4000);
                rotationAnim.setRepeatCount(ValueAnimator.INFINITE);
                rotationAnim.setInterpolator(new LinearInterpolator());
                rotationAnim.start();
            }

            // 2. Floating Circles (Front)
            animateFloating(binding.circle1, 2600, 15f);
            animateFloating(binding.circle2, 3200, -20f);
            animateFloating(binding.circle3, 2000, 10f);
            animateFloating(binding.circle4, 2800, -15f);
            animateFloating(binding.circle5, 2400, 12f);
        }

        private void animateFloating(View view, int duration, float translation) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, translation);
            anim.setDuration(duration);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.setRepeatMode(ValueAnimator.REVERSE);
            anim.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
            anim.start();
        }

        void flipCard() {
            AnimatorSet frontAnim = (AnimatorSet) AnimatorInflater.loadAnimator(itemView.getContext(), R.animator.front_animator);
            AnimatorSet backAnim = (AnimatorSet) AnimatorInflater.loadAnimator(itemView.getContext(), R.animator.back_animator);

            if (isFront) {
                frontAnim.setTarget(binding.recipeFront);
                backAnim.setTarget(binding.recipeBack);
                frontAnim.start();
                backAnim.start();
                isFront = false;
            } else {
                frontAnim.setTarget(binding.recipeBack);
                backAnim.setTarget(binding.recipeFront);
                frontAnim.start();
                backAnim.start();
                isFront = true;
            }
        }
    }
}
