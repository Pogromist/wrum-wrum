package com.openway.square.wrumwrum.ui.history;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openway.square.wrumwrum.R;
import com.openway.square.wrumwrum.data.model.Operation;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.HistoryViewHolder> {

    private ArrayList<Operation> operations = new ArrayList<>();

    public HistoryRecyclerAdapter(@NonNull List<Operation> operations) {
        this.operations.addAll(operations);
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivOperationIcon;
        public TextView tvOperationType;
        public TextView tvOperationDuration;
        public TextView tvOperationStartDate;
        public TextView tvOperationAmount;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            this.ivOperationIcon = itemView.findViewById(R.id.iv_operation_icon);
            this.tvOperationType = itemView.findViewById(R.id.tv_operation_type);
            this.tvOperationDuration = itemView.findViewById(R.id.tv_operation_duration);
            this.tvOperationStartDate = itemView.findViewById(R.id.tv_operation_start_date);
            this.tvOperationAmount = itemView.findViewById(R.id.tv_operation_amount);
        }
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_history, parent, false));
    }

    private String convertTimeToString(final long timeInMilliSeconds) {
        long seconds = timeInMilliSeconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return days + " d " + hours % 24 + " h " + minutes % 60 + " m " + seconds % 60 + " s";
    }

    @Override
    public void onBindViewHolder(@NonNull final HistoryViewHolder holder, int position) {
        Resources resources = holder.itemView.getContext().getResources();
        Operation operation = operations.get(position);
        int operIconId;
        String type;
        switch (operation.getType()) {
            case "replenishment":
                operIconId = R.mipmap.ic_top_up;
                type = "Top up";
                holder.tvOperationDuration.setText("");
                break;
            case "withdrawal":
                operIconId = R.mipmap.ic_withdraw;
                type = "Withdrawal";
                holder.tvOperationDuration.setText("");
                break;
            case "rent":
                operIconId = R.mipmap.ic_rental;
                type = "Rental";
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                dateFormatter.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
                Date startDate = null;
                try {
                    startDate = dateFormatter.parse(operation.getStartDate());
                } catch (ParseException e) {
                    // ignore
                }
                Date endDate = null;
                try {
                    endDate = operation.getEndDate() == null ? new Date(System.currentTimeMillis()) : dateFormatter.parse(operation.getEndDate());
                } catch (ParseException e) {
                    // ignore
                }
                final long delta = endDate.getTime() - startDate.getTime();
                holder.tvOperationDuration.setText(convertTimeToString(delta));
                break;
            case "bonus":
                operIconId = R.mipmap.ic_bonus;
                type = "Bonus";
                holder.tvOperationDuration.setText("");
                break;
            default:
                operIconId = R.mipmap.ic_rental;
                type = "Unknown";
                holder.tvOperationDuration.setText("");
        }
        holder.tvOperationType.setText(type);
        holder.tvOperationStartDate.setText(operation.getStartDate());
        holder.ivOperationIcon.setImageDrawable(resources.getDrawable(operIconId));
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        final String currency = format.format(operation.getAmount());
        holder.tvOperationAmount.setText(currency);
    }

    @Override
    public int getItemCount() {
        return operations.size();
    }

    public void clear() {
        operations.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Operation> list) {
        operations.addAll(list);
        notifyDataSetChanged();
    }

}
