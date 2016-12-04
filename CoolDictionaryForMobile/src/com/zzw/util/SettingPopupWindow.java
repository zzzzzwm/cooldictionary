package com.zzw.util;

import java.util.List;

import com.zzw.R;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SettingPopupWindow extends MyAbstractPopupWindow {
	private List<String> datas;
	private ListView listView;
	
	public SettingPopupWindow(Context context, View contentView, 
			int width, int height, List<String> datas) {
		super(context, contentView, width, height, true);
		this.datas=datas;
		initView();
		initEvent();
		init();
	}
	
	@Override
	public void initView() {
		listView=(ListView)contentView.findViewById(
				R.id.setting_list);
		listView.setAdapter(new CommonAdapter<String>(
				context, datas, R.layout.item_setting){
			@Override
			public void convert(ViewHolder helper, int position) {
				helper.setText(R.id.setting_item, datas.get(position));
			}
		});
	}
	
	@Override
	public void initEvent() {
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, 
					View view, int position, long id) {
				if(listener!=null)
					listener.onSetting(position);
				dismiss();
			}
		});
	}
	
	@Override
	public void init() {}

	public interface OnClickSettingListener {
		public void onSetting(int position);
	}
	
	private OnClickSettingListener listener;
	
	public void setOnClickSettingListener(OnClickSettingListener l){
		listener=l;
	}
	
}
