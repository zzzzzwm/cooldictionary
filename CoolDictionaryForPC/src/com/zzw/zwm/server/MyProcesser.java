package com.zzw.zwm.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zzw.zwm.database.MyDataBaseHelper;

/**
 * 处理器类，这里提供了注册、登录、查询（用户/点赞）、点赞、分享这些功能。
 * 猿们可以参照此类编写自己的处理器。
 * @author zzw
 * @version 1.0
 */
public class MyProcesser {
	private MyDevelopmentKit mdk;			// 开发工具包
	private MyDataBaseHelper dbHelper;		// 数据库连接
	private SimpleDateFormat mSDF;			// 日期格式
	
	private static final int REGISTER=0;	// 注册
	private static final int SIGN_IN=1;		// 登录
	private static final int QUERY_COUNT=2;	// 查询点赞数
	private static final int QUERY_USER=3;	// 查询用户
	private static final int UPDATE_COUNT=4;// 点赞
	private static final int SHARE_ITEM=5;	// 分享词条
	
	public MyProcesser(MyDevelopmentKit kit){
		mdk=kit;
		dbHelper=new MyDataBaseHelper();
		dbHelper.createUserTable();
		dbHelper.createEnjoyTable();
		dbHelper.createEmailboxTable();
		mSDF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public interface MyDevelopmentKit {
		String read(SelectionKey key);
		void write(SelectionKey key, String msg);
	}

	private String getDate(){
		return mSDF.format(new Date());
	}

	public void log(String tag, String msg){
		System.out.println("["+tag+"]["+getDate()+"] "+msg);
	}
	
	public void execute(String sql) throws SQLException{
		dbHelper.execute(sql);
	}
	
	/**
	 * 对用户请求进行处理，用户请求包括状态码和消息体，
	 * 状态码对用户请求进行标识，例如：注册、登录、点赞、查询用户等等；
	 * 消息体是用户请求的具体内容，例如：注册、登录需要用户名和密码，
	 * 点赞需要单词等等。状态码和消息体之间用"#"分隔，消息之间用"&"分隔。
	 * @param require
	 * @throws RequireException 
	 * @throws IOException 
	 */
	public void process(SelectionKey key, String require){
		Matcher m=Pattern.compile("#").matcher(require);
		if(!m.find()){
			mdk.write(key, "0#fail");
			return;
		}
		String stateCode=require.substring(0, m.start());
		String msgBody=require.substring(m.end());
		switch(Integer.parseInt(stateCode)){
		case REGISTER:
			register(key, msgBody);
			break;
		case SIGN_IN:
			signIn(key, msgBody);
			break;
		case QUERY_COUNT:
			queryCount(key, msgBody);
			break;
		case QUERY_USER:
			queryUser(key, msgBody);
			break;
		case UPDATE_COUNT:
			updateCount(key, msgBody);
			break;
		case SHARE_ITEM:
			shareItem(key, msgBody);
			break;
		default:break;
		}
	}
	
	/**
	 * 注册流程，首先查询数据库中是否存在相同用户，若存在，则直接返回"0#fail"；
	 * 否则将用户加入数据库，并返回"1#pass"。
	 * @param key
	 * @param msgBody
	 */
	private void register(SelectionKey key, String msgBody){
		String[] info=msgBody.split("&");
		try {
			ResultSet result=dbHelper.executeReturnResult(
					"select name from user where name='"+info[0]+"';");
			if(!result.next()){
				dbHelper.execute("insert into user values(null,'"+info[0]+"','"+
						info[1]+"',true,'"+getDate()+"');");
				mdk.write(key, "1#pass");
				key.attach(info[0]);
			}
			else{
				mdk.write(key, "0#fail");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 登录流程，首先查询用户是否存在，若不存在，则直接返回"0#null"；
	 * 否则检查密码，若不匹配，则直接返回"0#fail"，否则更新在线状态和登录时间，
	 * 并返回"1#pass"。
	 * @param key
	 * @param msgBody
	 */
	private void signIn(SelectionKey key, String msgBody){
		String[] info=msgBody.split("&");
		try {
			ResultSet result=dbHelper.executeReturnResult(
					"select password,online,date from user "+
							"where name='"+info[0]+"';");
			if(!result.next()){
				mdk.write(key, "0#null");
			}
			else if(info[1].equals(result.getString(1))){
				dbHelper.execute("update user set online=true,date='"+
						getDate()+"' where name='"+info[0]+"';");
				mdk.write(key, "1#pass");
				key.attach(info[0]);
			}
			else{
				mdk.write(key, "0#fail");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询点赞数，首先查询单词是否存在，若不存在直接返回"1#0&0&0"；
	 * 否则直接返回"1#number&number&number"。
	 * @param key
	 * @param msgBody
	 */
	private void queryCount(SelectionKey key, String msgBody){
		String[] info=msgBody.split("&");
		try {
			ResultSet result=dbHelper.executeReturnResult(
					"select * from enjoy where word='"+info[0]+"';");
			if(!result.next()){
				mdk.write(key, "1#0&0&0");
			}
			else{
				mdk.write(key, "1#"+result.getString(2)+"&"+
						result.getString(3)+"&"+result.getString(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 查询用户，分为随机查询和特定查询，随机查询根据用户提供的随机数进行查询，
	 * 特定查询根据用户提供的用户名进行查询。随机查询时，若成功则返回
	 * "1#seed&name,online&...&name,online"，否则返回"0#null"或"0#fail"。
	 * 特定查询时，若成功则返回"1#online"，否则返回"0#null"。
	 * @param key
	 * @param msgBody
	 */
	private void queryUser(SelectionKey key, String msgBody){
		String[] info=msgBody.split("&");
		try {
			if(info[0].equals("id")){
				ResultSet result=dbHelper.executeReturnResult(
						"select max(id) from user;");
				if(!result.next()){
					mdk.write(key, "0#null");
					return;
				}
				int next=new Random(Integer.parseInt(info[1])).
						nextInt(result.getInt(1))+1;
				result=dbHelper.executeReturnResult("select name,online from user "+
						"where id between "+next+" and "+(next+10)+";");
				if(!result.next()){
					mdk.write(key, "0#fail");
				}
				else{
					StringBuilder sb=new StringBuilder("1#");
					sb.append(next);
					do{
						sb.append("&").append(result.getString(1)).
						append(",").append(result.getString(2));
					}while(result.next());
					mdk.write(key, sb.toString());
				}
			}
			else if(info[0].equals("name")){
				ResultSet result=dbHelper.executeReturnResult(
						"select online from user where name='"+info[1]+"';");
				if(!result.next()){
					mdk.write(key, "0#null");
				}
				else{
					mdk.write(key, "1#"+result.getString(1));
				}
			}
			else{
				mdk.write(key, "0#fail");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 点赞流程，首先查询单词是否存在，若存在则更新；否则加入数据库。
	 * 成功返回"1#pass"，失败返回"0#fail"。
	 * @param key
	 * @param msgBody
	 */
	private void updateCount(SelectionKey key, String msgBody){
		String[] info=msgBody.split("&");
		try {
			ResultSet result=dbHelper.executeReturnResult(
					"select word from enjoy where word='"+info[0]+"';");
			if(!result.next()){
				dbHelper.execute("insert into enjoy values('"+
						info[0]+"',"+info[1]+","+info[2]+","+info[3]+");");
			}
			else{
				dbHelper.execute("update enjoy set youdao=youdao+"+info[1]+
						",bing=bing+"+info[2]+",baidu=baidu+"+info[3]+
						" where word='"+info[0]+"';");
			}
			mdk.write(key, "1#pass");
		} catch (SQLException e) {
			e.printStackTrace();
			mdk.write(key, "0#fail");
		}
	}
	
	/**
	 * 分享词条，首先将词条加入数据库，在下次select时，检测每个用户是否有待接收的词条。
	 * 若有则发送，若无则跳过。发送成功将提醒发送者发送成功，失败则返回"0#fail&error"。
	 * @param key
	 * @param msgBody
	 */
	private void shareItem(SelectionKey key, String msgBody){
		String[] info=msgBody.split("&");
		try {
			dbHelper.execute("insert into emailbox values('"+info[0]+"','"+info[1]+
					"','"+info[2]+"','"+getDate()+"');");
		} catch (SQLException e) {
			e.printStackTrace();
			mdk.write(key, "0#fail&"+e.getMessage());
		}
	}
	
	/**
	 * 对词条进行分发，首先对在线用户进行识别，如果他/她为接收者，
	 * 则向他/她发送词条，发送完毕后从数据库中删除所有已发送的词条。
	 * @param selector
	 */
	public void dispatchItem(Selector selector){
		Set<SelectionKey> set=selector.keys();
		if(set==null || set.size()<=0)
			return;
		for(SelectionKey sk : set){
			if(!sk.isValid())
				continue;
			Object o=sk.attachment();
			if(o==null || o.toString().contains("."))
				continue;
			try {
				ResultSet res=dbHelper.executeReturnResult(
						"select send,word,date from emailbox "+
								"where receive='"+o+"';");
				if(res.next()){
					StringBuilder sb=new StringBuilder("1#mail");
					do{
						sb.append("&").append(res.getString(1)).
						append(",").append(res.getString(2)).
						append(",").append(res.getString(3));
					}while(res.next());
					mdk.write(sk, sb.toString());
					dbHelper.execute("delete from emailbox "+
							"where receive='"+o+"';");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
