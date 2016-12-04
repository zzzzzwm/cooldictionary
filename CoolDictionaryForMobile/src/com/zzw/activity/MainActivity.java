package com.zzw.activity;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zzw.R;
import com.zzw.network.MyNetworkThread;
import com.zzw.util.MyThread;
import com.zzw.util.QueryUserPopupWindow;
import com.zzw.util.QueryUserPopupWindow.OnClickQueryListener;
import com.zzw.util.QueryUserPopupWindow.UserInfo;
import com.zzw.util.RegisterPopupWindow;
import com.zzw.util.RegisterPopupWindow.OnClickRegisterListener;
import com.zzw.util.SettingPopupWindow;
import com.zzw.util.SettingPopupWindow.OnClickSettingListener;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.GridLayoutManager.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements 
OnClickSettingListener, OnClickRegisterListener, 
OnClickQueryListener {
	private MyThread mServerThread;		// 服务器连接线程
	private Handler mHandler;			// 处理服务器应答
	// 翻译相关
	private String word;				// 单词
	private String youdaoTrans;			// 有道翻译
	private String bingTrans;			// 必应翻译
	private String jinshanTrans;		// 金山翻译
	private ProgressDialog loading;

	private boolean isOnline=false;		// 是否在线
	private String user=null;			// 用户
	
	private NotificationManager manager;// 通知管理器
	private int notificationId=0;		// 通知ID

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mHandler=new Handler(){
			@Override
			public void handleMessage(Message msg){
				if(msg.what==0){		// 与服务器断开连接
					showToast("与服务器的连接已中断");
					isOnline=false;
					mServerThread=null;
				}
				else if(msg.what==1){	// 处理服务器的响应
					String respond=(String)(msg.obj);
					//Log.d("TAG", respond);
					String[] info=respond.split("#");
					int requireCode=Integer.parseInt(info[0]);
					int respondCode=Integer.parseInt(info[1]);
					switch(requireCode){
					case 0:		// 用户注册
					case 1:		// 用户登录
						respondRegister(respondCode, info[2]);
						break;
					case 2:		// 查询点赞
						respondQueryCount(respondCode, info[2]);
						break;
					case 3:		// 查询用户
						respondQueryUser(respondCode, info[2]);
						break;
					case 4:		// 用户点赞
						respondUpdateCount(respondCode, info[2]);
						break;
					case 5:		// 分享词条
						respondShareItem(respondCode, info[2]);
						break;
					case 6:		// 接收词条
						respondReceiveItem(respondCode, info[2]);
						break;
					default:break;
					}
				}
				else if(msg.what==2){
					obtainTranslation(msg);
				}
				else
					showToast("无效的应答");
			}
		};
		
		initView();
		initEvent();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode!=RESULT_OK)
			return;
		switch(requestCode){
		case 1:
			if(mServerThread==null){
				String addr=data.getStringExtra("SERVER_ADDR");
				int port=data.getIntExtra("SERVER_PORT", 7999);
				mServerThread=new MyThread(addr, port);
				mServerThread.setOuterHandler(mHandler);
				new Thread(mServerThread).start();
			}
			break;
		default:
			break;
		}
	}
	
	// 对来自服务器的不同应答做相应处理
	// -------------------------------------------------------
	// 登录/注册处理
	private void respondRegister(int respondCode, String msg){
		if(respondCode==1){
			isOnline=true;
			registerWindow.dismiss();
			String[] info=msg.split("&");
			onlineText.setText(user=info[1]);
			showToast(info[0]);
		}
		else
			showToast(msg);
	}
	// 点赞查询处理
	private void respondQueryCount(int respondCode, String msg){
		if(respondCode==1){
			String[] num=msg.split("&");
			int youdaoNum=Integer.parseInt(num[0]);
			int bingNum=Integer.parseInt(num[1]);
			int jinshanNum=Integer.parseInt(num[2]);
			if(youdaoNum>=bingNum && youdaoNum>=jinshanNum){
				setContent(1, "有道", youdaoTrans, youdaoNum);
				if(bingNum>=jinshanNum){
					setContent(2, "必应", bingTrans, bingNum);
					setContent(3, "金山", jinshanTrans, jinshanNum);
				}
				else{
					setContent(2, "金山", jinshanTrans, jinshanNum);
					setContent(3, "必应", bingTrans, bingNum);
				}
			}
			else if(bingNum>=youdaoNum && bingNum>=jinshanNum){
				setContent(1, "必应", bingTrans, bingNum);
				if(youdaoNum>=jinshanNum){
					setContent(2, "有道", youdaoTrans, youdaoNum);
					setContent(3, "金山", jinshanTrans, jinshanNum);
				}
				else{
					setContent(2, "金山", jinshanTrans, jinshanNum);
					setContent(3, "有道", youdaoTrans, youdaoNum);
				}
			}
			else{
				setContent(1, "金山", jinshanTrans, jinshanNum);
				if(youdaoNum>=bingNum){
					setContent(2, "有道", youdaoTrans, youdaoNum);
					setContent(3, "必应", bingTrans, bingNum);
				}
				else{
					setContent(2, "必应", bingTrans, bingNum);
					setContent(3, "有道", youdaoTrans, youdaoNum);
				}
			}
			loading.dismiss();
		}
	}
	// 用户查询处理
	private void respondQueryUser(int respondCode, String msg){
		if(respondCode==1){
			String[] info=msg.split("&");
			ArrayList<UserInfo> users=new ArrayList<UserInfo>();
			for(String s:info){
				String[] user=s.split(",");
				if(user[1].equals("1"))
					users.add(new UserInfo(user[0], true));
				else
					users.add(new UserInfo(user[0], false));
			}
			queryUserWindow=new QueryUserPopupWindow(MainActivity.this, 
					LayoutInflater.from(MainActivity.this).
					inflate(R.layout.layout_queryuser, null), 
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, users);
			queryUserWindow.setOnClickQueryListener(this);
			queryUserWindow.setAnimationStyle(R.style.anim_pop);
			queryUserWindow.showAtBottom(rootView);
			loading.dismiss();
		}
		else
			showToast(msg);
	}
	// 点赞处理
	private void respondUpdateCount(int respondCode, String msg){
		showToast(msg);
	}
	// 分享词条
	private void respondShareItem(int respondCode, String msg){
		if(respondCode==1)
			showToast(msg);
		else
			showToast(msg+"，请先点赞");
		queryUserWindow.dismiss();
	}
	// 接收词条
	private void respondReceiveItem(int respondCode, String msg){
		NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.log);
		builder.setContentTitle("收到一条新的分享");
		builder.setContentText("点击查看详情");
		builder.setTicker("收到一条新的分享");
		builder.setNumber(1);
		builder.setAutoCancel(true);
		String[] items=msg.split("&");
		for(String item:items){
			Intent intent=new Intent(this, ReceiveActivity.class);
			intent.putExtra("RECEIVE_CONTENT", item);
			PendingIntent pi=PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(pi);
			manager.notify(notificationId++, builder.build());
		}
	}
	// 获得翻译
	private void obtainTranslation(Message msg){
		//Log.d("TAG", (String)(msg.obj));
		String[] info=((String)(msg.obj)).split("&");
		if(info==null || info.length<4){
			showToast("搜索失败");
			loading.dismiss();
			return;
		}
		word=info[0].toLowerCase();
		youdaoTrans=info[1];
		bingTrans=info[2];
		jinshanTrans=info[3];
		wordHead.setText(word);
		wordEnjoy1.setImageResource(R.drawable.notenjoy);
		wordEnjoy2.setImageResource(R.drawable.notenjoy);
		wordEnjoy3.setImageResource(R.drawable.notenjoy);
		wordIsEnjoy1=wordIsEnjoy2=wordIsEnjoy3=false;
		if(isOnline && mServerThread!=null){
			Message message=new Message();
			message.obj="2#"+word;
			mServerThread.getInnerHandler().sendMessage(message);
		}
		else{
			wordHead.setText(word);
			setContent(1, "有道", youdaoTrans, 0);
			setContent(2, "必应", bingTrans, 0);
			setContent(3, "金山", jinshanTrans, 0);
			loading.dismiss();
		}
	}
	private void setContent(int id, String title, String trans, int num){
		TextView wordTitle, wordTrans, wordNum;
		if(id==1){
			wordTitle=wordTitle1;
			wordTrans=wordTrans1;
			wordNum=wordNum1;
		}
		else if(id==2){
			wordTitle=wordTitle2;
			wordTrans=wordTrans2;
			wordNum=wordNum2;
		}
		else{
			wordTitle=wordTitle3;
			wordTrans=wordTrans3;
			wordNum=wordNum3;
		}
		wordTitle.setText(title);
		wordTrans.setText(trans);
		wordNum.setText(num+"");
	}
	private void sendEnjoyMessage(int id){
		TextView wordTitle;
		ImageButton wordEnjoy;
		TextView wordNum;
		if(id==1){
			wordTitle=wordTitle1;
			wordEnjoy=wordEnjoy1;
			wordNum=wordNum1;
			wordIsEnjoy1=true;
		}
		else if(id==2){
			wordTitle=wordTitle2;
			wordEnjoy=wordEnjoy2;
			wordNum=wordNum2;
			wordIsEnjoy2=true;
		}
		else{
			wordTitle=wordTitle3;
			wordEnjoy=wordEnjoy3;
			wordNum=wordNum3;
			wordIsEnjoy3=true;
		}
		
		Message msg=new Message();
		String title=wordTitle.getText().toString();
		if(title.equals("有道"))
			msg.obj="4#"+word+"&1&0&0";
		else if(title.equals("必应"))
			msg.obj="4#"+word+"&0&1&0";
		else
			msg.obj="4#"+word+"&0&0&1";
		mServerThread.getInnerHandler().sendMessage(msg);
		wordEnjoy.setImageResource(R.drawable.enjoy);
		wordNum.setText(""+(Integer.parseInt(wordNum.getText().toString())+1));
	}
	// -------------------------------------------------------

	// 初始化相关
	// -------------------------------------------------------
	private View rootView;
	private ImageButton userImage;
	private TextView onlineText;
	private ImageButton setting;
	private SettingPopupWindow settingWindow;
	private RegisterPopupWindow registerWindow;
	private QueryUserPopupWindow queryUserWindow;
	private EditText wordEdit;
	private Button queryBt;
	private TextView wordHead;		// 单词头
	private ImageButton shareBt;	// 分享
	private TextView wordTitle1;	// 翻译头1
	private TextView wordTitle2;	// 翻译头2
	private TextView wordTitle3;	// 翻译头3
	private TextView wordTrans1;	// 翻译1
	private TextView wordTrans2;	// 翻译2
	private TextView wordTrans3;	// 翻译3
	private ImageButton wordEnjoy1;	// 点赞1
	private ImageButton wordEnjoy2;	// 点赞2
	private ImageButton wordEnjoy3;	// 点赞3
	private TextView wordNum1;		// 点赞数1
	private TextView wordNum2;		// 点赞数2
	private TextView wordNum3;		// 点赞数3
	private boolean wordIsEnjoy1;	// 是否点赞1
	private boolean wordIsEnjoy2;	// 是否点赞2
	private boolean wordIsEnjoy3;	// 是否点赞3
	
	private void initView(){
		loading=new ProgressDialog(this);
		loading.setTitle("正在加载");
		loading.setCancelable(false);
		rootView=findViewById(R.id.root_layout);
		userImage=(ImageButton)findViewById(R.id.user_state);
		onlineText=(TextView)findViewById(R.id.online);
		setting=(ImageButton)findViewById(R.id.setting);
		
		ArrayList<String> datas=new ArrayList<String>();
		datas.add("配置"); datas.add("其他");
		settingWindow=new SettingPopupWindow(this, 
				LayoutInflater.from(getApplicationContext()).inflate(
						R.layout.list_setting, null), 
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, datas);
		settingWindow.setOnDismissListener(new OnDismissListener(){
			@Override
			public void onDismiss() {
				WindowManager.LayoutParams lp=getWindow().getAttributes();
				lp.alpha=1.0F;
				getWindow().setAttributes(lp);
			}
		});
		settingWindow.setOnClickSettingListener(this);
		
		registerWindow=new RegisterPopupWindow(this, 
				LayoutInflater.from(getApplicationContext()).
				inflate(R.layout.layout_register, null), 
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		registerWindow.setOnClickRegisterListener(this);
		
		wordEdit=(EditText)findViewById(R.id.word_edit);
		queryBt=(Button)findViewById(R.id.start_query);
		wordHead=(TextView)findViewById(R.id.word_head);
		shareBt=(ImageButton)findViewById(R.id.share_bt);
		wordTitle1=(TextView)findViewById(R.id.word_title1);
		wordTitle2=(TextView)findViewById(R.id.word_title2);
		wordTitle3=(TextView)findViewById(R.id.word_title3);
		wordTrans1=(TextView)findViewById(R.id.word_trans1);
		wordTrans2=(TextView)findViewById(R.id.word_trans2);
		wordTrans3=(TextView)findViewById(R.id.word_trans3);
		wordEnjoy1=(ImageButton)findViewById(R.id.word_enjoy1);
		wordEnjoy2=(ImageButton)findViewById(R.id.word_enjoy2);
		wordEnjoy3=(ImageButton)findViewById(R.id.word_enjoy3);
		wordNum1=(TextView)findViewById(R.id.word_num1);
		wordNum2=(TextView)findViewById(R.id.word_num2);
		wordNum3=(TextView)findViewById(R.id.word_num3);
	}
	
	private void initEvent(){
		// 弹出配置窗口
		setting.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				settingWindow.setAnimationStyle(R.style.anim_pop);
				settingWindow.showAtBottom(rootView);
				WindowManager.LayoutParams lp=getWindow().getAttributes();
				lp.alpha=.3F;
				getWindow().setAttributes(lp);
			}
		});
		// 弹出登录窗口
		userImage.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(isOnline){
					showToast("已登录");
					return;
				}
				registerWindow.setAnimationStyle(R.style.anim_pop);
				registerWindow.showAtBottom(rootView);
			}
		});
		// 开始查询单词
		queryBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String word=wordEdit.getText().toString().trim().toLowerCase();
				if(word==null || word.length()<=0){
					showToast("请输入单词");
					return;
				}
				Matcher m=Pattern.compile("[^(a-zA-Z\\u4e00-\\u9fa5 \\-')]").
						matcher(word);
				if(m.find()){
					showToast("格式错误");
					return;
				}
				new Thread(new MyNetworkThread(word, mHandler)).start();
				loading.show();
			}
		});
		// 分享词条，弹出用户选择窗口
		shareBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(isOnline && mServerThread!=null && word!=null){
					Message msg=new Message();
					msg.what=1;
					msg.obj="3#all";
					mServerThread.getInnerHandler().sendMessage(msg);
					loading.show();
				}
				else
					showToast("请先登录或查询单词");
			}
		});
		// 点赞
		wordEnjoy1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(word!=null && isOnline && mServerThread!=null)
					sendEnjoyMessage(1);
				else
					showToast("请先登录");
			}
		});
		wordEnjoy2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(word!=null && isOnline && mServerThread!=null)
					sendEnjoyMessage(2);
				else
					showToast("请先登录");
			}
		});
		wordEnjoy3.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(word!=null && isOnline && mServerThread!=null)
					sendEnjoyMessage(3);
				else
					showToast("请先登录");
			}
		});
	}
	// -------------------------------------------------------

	// 回调方法相关
	// -------------------------------------------------------
	// 连接服务器
	@Override
	public void onSetting(int position) {
		switch(position){
		case 0:		// 配置
			Intent intent=new Intent(this, SettingActivity.class);
			startActivityForResult(intent, 1);
			break;
		case 1:
			break;
		default:
			break;
		}
	}

	@Override
	public void onSignIn(String name, String password) {
		if(mServerThread==null){
			showToast("未连接服务器");
			return;
		}
		Message msg=new Message();
		msg.obj="1#"+name+"&"+password;
		mServerThread.getInnerHandler().sendMessage(msg);
	}

	@Override
	public void onRegister(String name, String password) {
		if(mServerThread==null){
			showToast("未连接服务器");
			return;
		}
		Message msg=new Message();
		msg.obj="0#"+name+"&"+password;
		mServerThread.getInnerHandler().sendMessage(msg);
	}

	@Override
	public void onQuery(ArrayList<String> selectedUsers) {
		if(selectedUsers==null || selectedUsers.size()<=0){
			showToast("请选择用户");
			return;
		}
		StringBuilder sb=new StringBuilder("5#");				// 请求号
		sb.append(user).append("&").append(word).append("&").	// 发送者&单词
		append(wordTitle1.getText()).append("^").				// 1 单词头&翻译&
		append(wordTrans1.getText()).append("^").				// 是否点赞&点赞数
		append(wordIsEnjoy1).append("^").
		append(wordNum1.getText()).append("^").
		append(wordTitle2.getText()).append("^").				// 2 单词头&翻译&
		append(wordTrans2.getText()).append("^").				// 是否点赞&点赞数
		append(wordIsEnjoy2).append("^").
		append(wordNum2.getText()).append("^").
		append(wordTitle3.getText()).append("^").				// 3 单词头&翻译&
		append(wordTrans3.getText()).append("^").				// 是否点赞&点赞数
		append(wordIsEnjoy3).append("^").
		append(wordNum3.getText()).append("&");
		for(String s:selectedUsers)								// 接收者
			sb.append(s).append(",");
		sb.deleteCharAt(sb.length()-1);
		Message msg=new Message();
		msg.obj=sb.toString();
		//Log.d("TAG", sb.toString());
		mServerThread.getInnerHandler().sendMessage(msg);
	}

	private void showToast(String text){
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

}
