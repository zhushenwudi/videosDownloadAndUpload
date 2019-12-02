package com.ilab.testysy.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ilab.testysy.R;
import com.ilab.testysy.entity.TaskEnty;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    //数据源
    private List<TaskEnty> mList;

    public MyAdapter(List<TaskEnty> list) {
        mList = list;
    }

    //返回item个数
    @Override
    public int getItemCount() {
        return mList.size();
    }

    //创建ViewHolder
    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv, parent, false);
        return new ViewHolder(convertView);
    }

    //填充视图
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyAdapter.ViewHolder holder, final int position) {
        holder.tvId.setText((mList.get(position).getId() + 1) + "");
        holder.tvCount.setText(mList.get(position).getCount() + "");
    }

    /*
     * 内部类实现视图接收
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId;
        TextView tvCount;

        ViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_id);//item 的id
            tvCount = itemView.findViewById(R.id.tv_count);//item 的id
        }
    }

}
