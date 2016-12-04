package com.zzw.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zzw.R;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterPopupWindow extends MyAbstractPopupWindow {
	private EditText nameEdit;
	private EditText passwordEdit;
	private Button signInBt;
	private Button registerBt;

	public RegisterPopupWindow(Context context, View contentView, int width, int height) {
		super(context, contentView, width, height, true);
		initView();
		initEvent();
		init();
	}

	@Override
	public void initView() {
		nameEdit=(EditText)contentView.findViewById(R.id.name_edit);
		passwordEdit=(EditText)contentView.findViewById(R.id.password_edit);
		signInBt=(Button)contentView.findViewById(R.id.sign_in_bt);
		registerBt=(Button)contentView.findViewById(R.id.register_bt);
	}

	@Override
	public void initEvent() {
		signInBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String name=nameEdit.getText().toString().trim();
				if(!checkName(name)){
					Toast.makeText(context, "用户名格式错误", Toast.LENGTH_SHORT).show();
					return;
				}
				String password=passwordEdit.getText().toString().trim();
				if(!checkPassword(password)){
					Toast.makeText(context, "密码格式错误", Toast.LENGTH_SHORT).show();
					return;
				}
				if(listener!=null)
					listener.onSignIn(name, password);
			}
		});
		registerBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String name=nameEdit.getText().toString().trim();
				if(!checkName(name)){
					Toast.makeText(context, "用户名格式错误", Toast.LENGTH_SHORT).show();
					return;
				}
				String password=passwordEdit.getText().toString().trim();
				if(!checkPassword(password)){
					Toast.makeText(context, "密码格式错误", Toast.LENGTH_SHORT).show();
					return;
				}
				if(listener!=null)
					listener.onRegister(name, password);
			}
		});
	}

	@Override
	public void init() {}

	public interface OnClickRegisterListener{
		void onSignIn(String name, String password);
		void onRegister(String name, String password);
	}
	
	private OnClickRegisterListener listener;
	
	public void setOnClickRegisterListener(OnClickRegisterListener l){
		listener=l;
	}
	
	private boolean checkName(String name){
		if(name==null || name.length()<=0)
			return false;
		Matcher m=Pattern.compile("[^(a-zA-Z0-9\\u4e00-\\u9fa5)]").matcher(name);
		return !m.find();
	}
	private boolean checkPassword(String password){
		if(password==null || password.length()<=0)
			return false;
		Matcher m=Pattern.compile("[^a-zA-Z0-9_]").matcher(password);
		return !m.find();
	}
}
