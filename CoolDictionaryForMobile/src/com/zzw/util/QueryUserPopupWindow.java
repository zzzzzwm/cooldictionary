package com.zzw.util;

import java.util.ArrayList;
import java.util.List;

import com.zzw.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

public class QueryUserPopupWindow extends MyAbstractPopupWindow {
	private ImageButton queryBack;	// 返回
	private Button queryOK;			// 完成
	private EditText queryEdit;		// 搜索编辑
	private Button queryStart;		// 搜索开始
	private ListView userList;		// 用户列表
	private List<UserInfo> users;			// 用户信息
	private List<UserInfo> visbleUsers;		// 可见用户
	private ArrayList<String> selectedUsers;// 选中用户

	public QueryUserPopupWindow(Context context, View contentView, 
			int width, int height, List<UserInfo> datas) {
		super(context, contentView, width, height, true);
		users=datas;
		visbleUsers=new ArrayList<UserInfo>();
		for(UserInfo user:users)
			visbleUsers.add(user);
		selectedUsers=new ArrayList<String>();
		initView();
		initEvent();
		init();
	}

	@Override
	public void initView() {
		queryBack=(ImageButton)contentView.findViewById(R.id.query_back);
		queryOK=(Button)contentView.findViewById(R.id.query_ok);
		queryEdit=(EditText)contentView.findViewById(R.id.query_edit);
		queryStart=(Button)contentView.findViewById(R.id.query_inner);
		userList=(ListView)contentView.findViewById(R.id.user_list);
		userList.setAdapter(new UserAdapter(
				context, visbleUsers, R.layout.item_queryuser));
	}

	@Override
	public void initEvent() {
		queryBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		queryOK.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(listener!=null)
					listener.onQuery(selectedUsers);
			}
		});
		
		queryStart.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String cond=queryEdit.getText().toString().trim();
				if(cond==null || cond.length()<=0){
					visbleUsers.clear();
					for(UserInfo user:users)
						visbleUsers.add(user);
					Log.d("TAG", "space");
				}
				else{
					visbleUsers.clear();
					for(UserInfo user:users)
						if(user.name.contains(cond))
							visbleUsers.add(user);
					Log.d("TAG", visbleUsers.toString());
				}
				userList.setAdapter(new UserAdapter(
						context, visbleUsers, R.layout.item_queryuser));
			}
		});
	}

	@Override
	public void init() {}

	public static class UserInfo {
		public final String name;
		public final Boolean isOnline;
		public UserInfo(String n, Boolean online){
			name=n;
			isOnline=online;
		}
	}
	
	private class UserAdapter extends CommonAdapter<UserInfo> {
		public UserAdapter(Context context, List<UserInfo> datas, int itemLayoutId) {
			super(context, datas, itemLayoutId);
		}
		@Override
		public void convert(ViewHolder helper, final int position) {
			final UserInfo userInfo=getItem(position);
			// 设置头像
			ImageView userImage=(ImageView)helper.getView(R.id.user_state);
			if(userInfo.isOnline)
				userImage.setColorFilter(null);
			else
				userImage.setColorFilter(Color.parseColor("#77000000"));
			// 设置用户
			helper.setText(R.id.user_name, userInfo.name);
			// 设置选择
			final ImageView userSelect=(ImageView)helper.getView(R.id.user_select);
			if(selectedUsers.contains(userInfo.name))
				userSelect.setVisibility(View.VISIBLE);
			else
				userSelect.setVisibility(View.GONE);
			// 设置点击
			View userItem=helper.getView(R.id.user_item);
			userItem.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					if(selectedUsers.contains(userInfo.name)){
						selectedUsers.remove(userInfo.name);
						userSelect.setVisibility(View.GONE);
					}
					else{
						selectedUsers.add(userInfo.name);
						userSelect.setVisibility(View.VISIBLE);
					}
				}
			});
		}
	}
	
	public interface OnClickQueryListener {
		void onQuery(ArrayList<String> selectedUsers);
	}
	
	private OnClickQueryListener listener;
	
	public void setOnClickQueryListener(OnClickQueryListener l){
		listener=l;
	}
}
