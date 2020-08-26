package fi.neskola.kartta.ui.views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import fi.neskola.kartta.R;
import fi.neskola.kartta.models.IRecord;

public class RecordListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<IRecord> records;

    public RecordListRecyclerViewAdapter(Context context, ArrayList<IRecord> userArrayList) {
        this.context = context;
        this.records = userArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.record_list_item,parent,false);
        return new RecyclerViewViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        IRecord record = records.get(position);
        RecyclerViewViewHolder viewHolder= (RecyclerViewViewHolder) holder;
        viewHolder.txtView_title.setText(record.getName());
        switch (record.getType()) {
            case TARGET:
                viewHolder.txtView_type.setText("Target");
            case ROUTE:
            case AREA:
                break;
        }

    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView_icon;
        TextView txtView_title;
        TextView txtView_type;

        public RecyclerViewViewHolder(@NonNull View itemView) {
            super(itemView);
            txtView_type = itemView.findViewById(R.id.txtView_type);
            imgView_icon = itemView.findViewById(R.id.imgView_icon);
            txtView_title = itemView.findViewById(R.id.txtView_title);
        }
    }
}
