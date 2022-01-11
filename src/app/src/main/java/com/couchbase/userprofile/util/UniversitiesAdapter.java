package com.couchbase.userprofile.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.couchbase.userprofile.R;

import java.util.List;
import java.util.Map;

public class UniversitiesAdapter
        extends RecyclerView.Adapter<UniversitiesAdapter.ViewHolder> {

    private List<Map<String, Object>> mUniversities;
    private OnItemListener mOnItemListener;

    public interface OnItemListener {
        void OnClick(View view, int position);
    }

    public UniversitiesAdapter(List<Map<String, Object>> universities) {
        this.mUniversities = universities;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.university_list_item, parent, false);
        view.setPadding(20, 20, 20, 20);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        holder.title.setText(mUniversities.get(adapterPosition).get("name").toString());
        holder.subtitle.setText(mUniversities.get(adapterPosition).get("country").toString());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            mOnItemListener.OnClick(view, adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUniversities.size();
    }

    public void setOnItemClickListener(OnItemListener onItemClickListener) {
        mOnItemListener = onItemClickListener;
    }
}
