package com.example.administrator.justfortest2.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.justfortest2.AddCity;
import com.example.administrator.justfortest2.bean.DiyCity;
import com.example.administrator.justfortest2.R;
import com.example.administrator.justfortest2.db.FavouriteCity;

import org.litepal.LitePal;

import java.util.List;

/**
 * Created by Administrator on 2017/5/3.
 */

public class DiyCityAdapter extends RecyclerView.Adapter<DiyCityAdapter.ViewHolder> {

    public Context mContext;
    static private List<DiyCity> mDiyCityList;
    static AddCity addcity;
    RecyItemOnClick recyItemOnClick;
    LayoutInflater layoutInflater;
    int flag;//判断是否显示红色按钮

    public void setRecyItemOnClick(RecyItemOnClick recyItemOnClick) {
        this.recyItemOnClick = recyItemOnClick;
    }

    public DiyCityAdapter(List<DiyCity> diyCityList, Context context, int flag,AddCity addCity) {
        super();
        mDiyCityList = diyCityList;
        mContext = context;
        this.addcity = addCity;
        layoutInflater = LayoutInflater.from(context);
        this.flag = flag;
    }

    /**
     * 自定义点击接口
     */
    public interface RecyItemOnClick {
        //item点击
        void onItemOnClick(View view, int index);
    }

    /**
     * 重写自定义ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardView;
        TextView cityName;
        Button button;
        RecyItemOnClick recyItemOnClick;

        public ViewHolder(View view, RecyItemOnClick recyitemonclick) {
            super(view);
            cardView = (CardView) view;
            cityName = (TextView) view.findViewById(R.id.city_name);
            button = (Button) view.findViewById(R.id.btn_del);
            this.recyItemOnClick = recyitemonclick;
            view.setOnClickListener(this);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("MyFault", "getAdapterPosition: " + getAdapterPosition());
                    LitePal.deleteAll(FavouriteCity.class,"name = ?",mDiyCityList.get(getAdapterPosition()).getCityName());
                    addcity.diyCityList.remove(getAdapterPosition());
                    for (int i = 0; i < addcity.diyCityList.size(); i++) {
                        Log.d("MyFault", addcity.diyCityList.get(i).getCityName());
                    }
                    Log.d("MyFault", "-------------");
                    addcity.adapter.notifyDataSetChanged();

                }
            });
        }


        public void show() {
            button.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClick(View v) {
            if (recyItemOnClick != null) {
                //这里ViewHolder中提供了getPosition()
                int position = getAdapterPosition();
                recyItemOnClick.onItemOnClick(v, position);
            }
        }
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);

    }

    /**
     * 给ViewHolder设置布局元素
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View parentView = layoutInflater.inflate(R.layout.city_item, parent, false);
        return new ViewHolder(parentView, recyItemOnClick);
    }

    /**
     * ViewHolder设置元素
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        DiyCity diyCity = mDiyCityList.get(position);
        holder.cityName.setText(diyCity.getCityName());
        if (flag == 1) {
            holder.show();
        }
    }

    @Override
    public int getItemCount() {
        return mDiyCityList.size();
    }
}
