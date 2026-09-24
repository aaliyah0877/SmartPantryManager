package com.tagari.smartpantry;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tagari.smartpantry.databinding.ItemPantryBinding;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {
    private List<PantryItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    public PantryAdapter(List<PantryItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<PantryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPantryBinding binding = ItemPantryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PantryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.binding.itemName.setText(item.name);
        String info = item.quantity + " " + item.unit + "  ·  " + item.category;
        if (item.expiry != null && !item.expiry.isEmpty()) {
            info += "  ·  Use by " + item.expiry;
        }
        holder.binding.itemInfo.setText(info);
        holder.binding.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        ItemPantryBinding binding;
        PantryViewHolder(ItemPantryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
