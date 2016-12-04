package com.zzw.activity;

import com.zzw.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

public class ReceiveActivity extends Activity {
	private ImageButton receBack;
	private TextView receHead;
	private TextView receTitle1;
	private TextView receTitle2;
	private TextView receTitle3;
	private TextView receTrans1;
	private TextView receTrans2;
	private TextView receTrans3;
	private ImageButton receEnjoy1;
	private ImageButton receEnjoy2;
	private ImageButton receEnjoy3;
	private TextView receNum1;
	private TextView receNum2;
	private TextView receNum3;
	private TextView receBottom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_receive);
		initView();
		initEvent();
		setContent(getIntent().getStringExtra("RECEIVE_CONTENT"));
	}

	private void initView(){
		receBack=(ImageButton)findViewById(R.id.rece_back);
		receHead=(TextView)findViewById(R.id.rece_head);
		receTitle1=(TextView)findViewById(R.id.rece_title1);
		receTitle2=(TextView)findViewById(R.id.rece_title2);
		receTitle3=(TextView)findViewById(R.id.rece_title3);
		receTrans1=(TextView)findViewById(R.id.rece_trans1);
		receTrans2=(TextView)findViewById(R.id.rece_trans2);
		receTrans3=(TextView)findViewById(R.id.rece_trans3);
		receEnjoy1=(ImageButton)findViewById(R.id.rece_enjoy1);
		receEnjoy2=(ImageButton)findViewById(R.id.rece_enjoy2);
		receEnjoy3=(ImageButton)findViewById(R.id.rece_enjoy3);
		receNum1=(TextView)findViewById(R.id.rece_num1);
		receNum2=(TextView)findViewById(R.id.rece_num2);
		receNum3=(TextView)findViewById(R.id.rece_num3);
		receBottom=(TextView)findViewById(R.id.rece_bottom);
	}
	
	private void initEvent(){
		receBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void setContent(String rece){
		//Log.d("TAG", rece);
		String[] info=rece.split("\\^");
		//Log.d("TAG", info.length+"");
		if(info==null || info.length!=15)
			return;
		// 发送者
		String sender=info[0];
		// 单词
		receHead.setText(info[1]);
		// 翻译标题
		receTitle1.setText(info[2]);
		receTitle2.setText(info[6]);
		receTitle3.setText(info[10]);
		// 翻译内容
		receTrans1.setText(info[3]);
		receTrans2.setText(info[7]);
		receTrans3.setText(info[11]);
		// 是否点赞
		if(Boolean.parseBoolean(info[4]))
			receEnjoy1.setImageResource(R.drawable.enjoy);
		if(Boolean.parseBoolean(info[8]))
			receEnjoy2.setImageResource(R.drawable.enjoy);
		if(Boolean.parseBoolean(info[12]))
			receEnjoy3.setImageResource(R.drawable.enjoy);
		// 点赞数
		receNum1.setText(info[5]);
		receNum2.setText(info[9]);
		receNum3.setText(info[13]);
		// 发送时间
		String date=info[14];
		receBottom.setText(sender+"\t"+date);
	}
}
