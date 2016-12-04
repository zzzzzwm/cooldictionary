package com.zzw.util;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class CommonAdapter<T> extends BaseAdapter {
	protected Context mContext;
	protected List<T> mDatas;
	protected int mItemLayoutId;
	
	public CommonAdapter(
			Context context, List<T> datas, int itemLayoutId){
		mContext=context;
		mDatas=datas;
		mItemLayoutId=itemLayoutId;
	}
	
	@Override
	public int getCount(){
		return mDatas.size();
	}

	@Override
	public T getItem(int position){
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder viewHolder=ViewHolder.getViewHolder(mContext, 
				mItemLayoutId, position, convertView, parent);
		convert(viewHolder, position);
		return viewHolder.getConvertView();
	}
	
	public abstract void convert(ViewHolder helper, int position);
	
	public static class ViewHolder {
		private SparseArray<View> mViews=new SparseArray<View>();
		private View mConvertView;
		private int mPosition;

		private ViewHolder(Context context, int layoutId,
				int position, ViewGroup parent){
			mConvertView=LayoutInflater.from(context).inflate(
					layoutId, parent, false);
			mConvertView.setTag(this);
			mPosition=position;
		}
		
		public static ViewHolder getViewHolder(
				Context context, int layoutId, int position, 
				View convertView, ViewGroup parent){
			ViewHolder holder=null;
			if(convertView==null)
				holder=new ViewHolder(
						context, layoutId, position, parent);
			else{
				holder=(ViewHolder)convertView.getTag();
				holder.mPosition=position;
			}
			return holder;
		}
		
		public View getConvertView(){
			return mConvertView;
		}
		
		public int getPosition(){
			return mPosition;
		}
		
		public View getView(int viewId){
			View view=mViews.get(viewId);
			if (view==null){
				view=mConvertView.findViewById(viewId);
				mViews.put(viewId, view);
			}
			return view;
		}

		public ViewHolder setText(int viewId, String text){
			TextView view=(TextView)getView(viewId);
			view.setText(text);
			return this;
		}

		public ViewHolder setImageResource(int viewId, int drawableId){
			ImageView view=(ImageView)getView(viewId);
			view.setImageResource(drawableId);
			return this;
		}

		public ViewHolder setImageBitmap(int viewId, Bitmap bm){
			ImageView view=(ImageView)getView(viewId);
			view.setImageBitmap(bm);
			return this;
		}

	}
}
