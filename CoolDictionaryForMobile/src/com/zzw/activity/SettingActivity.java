package com.zzw.activity;

import com.zzw.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class SettingActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);
		initView();
		initEvent();
	}
	
	private ImageButton settingBack;
	private EditText serverAddr;
	private EditText serverPort;
	private Button settingOK;
	
	private void initView(){
		settingBack=(ImageButton)findViewById(R.id.setting_back);
		serverAddr=(EditText)findViewById(R.id.server_addr);
		serverPort=(EditText)findViewById(R.id.server_port);
		settingOK=(Button)findViewById(R.id.setting_ok);
	}
	private void initEvent(){
		settingBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		settingOK.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String addr=serverAddr.getText().toString().trim();
				int port=Integer.parseInt(serverPort.getText().toString().trim());
				Intent intent=new Intent();
				intent.putExtra("SERVER_ADDR", addr);
				intent.putExtra("SERVER_PORT", port);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
}
