package com.zzw.zwm.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库协助者，此类将协助使用者完成建表（包括user、enjoy、emailbox）以及
 * 插入、删除、更新、查询等基本操作。如果想使用复杂的SQL语句，可以使用execute方法。
 * @author zzw
 * @version 1.0
 */
public class MyDataBaseHelper {
	private MyDataBase db;
	// user(id,name,password,online,date);
	private static final String CREATE_USER_TABLE=
			"create table if not exists user("+
			"id int unsigned auto_increment,"+
			"name varchar(20) primary key,"+
			"password varchar(20) not null,"+
			"online boolean,"+
			"date varchar(20) not null,"+
			"key(id));";
	// enjoy(word,youdao,bing,baidu);
	private static final String CREATE_ENJOY_TABLE=
			"create table if not exists enjoy("+
			"word varchar(20) primary key,"+
			"youdao integer not null,"+
			"bing integer not null,"+
			"baidu integer not null);";
	// emailbox(send,receive,word,date);
	private static final String CREATE_EMAILBOX_TABLE=
			"create table if not exists emailbox("+
			"send varchar(20) not null,"+
			"receive varchar(20) not null,"+
			"word varchar(20) not null,"+
			"date varchar(20) not null,"+
			"index(send),index(receive),index(word),"+
			"foreign key(send) references user(name) on delete cascade,"+
			"foreign key(receive) references user(name) on delete cascade,"+
			"foreign key(word) references enjoy(word) on delete cascade"+
			");";
	
	public MyDataBaseHelper(){
		try {
			db=MyDataBase.getInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			for(Throwable t : e)
				t.printStackTrace();
		}
	}
	
	public void createUserTable(){
		try {
			db.execute(CREATE_USER_TABLE);
		} catch (SQLException e) {
			for(Throwable t : e)
				t.printStackTrace();
		}
	}
	public void createEnjoyTable(){
		try {
			db.execute(CREATE_ENJOY_TABLE);
		} catch (SQLException e) {
			for(Throwable t : e)
				t.printStackTrace();
		}
	}
	public void createEmailboxTable(){
		try {
			db.execute(CREATE_EMAILBOX_TABLE);
		} catch (SQLException e) {
			for(Throwable t : e)
				t.printStackTrace();
		}
	}
	
	public int insert(String table, String[] values){
		if(values==null || values.length<=0)
			return 0;
		StringBuilder sb=new StringBuilder("insert into ");
		sb.append(table).append(" ").append("values(");
		int i;
		for(i=0;i<values.length-1;i++)
			sb.append(values[i]).append(",");
		sb.append(values[i]).append(");");
		try {
			return db.executeReturnCount(sb.toString());
		} catch (SQLException e) {
			for(Throwable t : e)
				t.printStackTrace();
			return 0;
		}
	}

	public int delete(String table, String condition, String[] parameter) 
			throws ParameterException{
		StringBuilder sb=new StringBuilder("delete from ");
		sb.append(table).append(getCondition(condition, parameter));
		try {
			return db.executeReturnCount(sb.toString());
		} catch (SQLException e) {
			for(Throwable t : e)
				t.printStackTrace();
			return 0;
		}
	}
	
	public int update(String table, String key, String value, 
			String condition, String[] parameter) throws ParameterException{
		if(key==null || value==null)
			return 0;
		StringBuilder sb=new StringBuilder("update ");
		sb.append(table).append(" set ");
		sb.append(key).append("=").append(value).append(getCondition(condition, parameter));
		try {
			return db.executeReturnCount(sb.toString());
		} catch (SQLException e) {
			for(Throwable t : e)
				t.printStackTrace();
			return 0;
		}
	}
	
	public ResultSet query(String table, String[] selection, 
			String condition, String[] parameter) throws ParameterException{
		StringBuilder sb=new StringBuilder("select ");
		if(selection!=null && selection.length>0){
			int i;
			for(i=0;i<selection.length-1;i++)
				sb.append(selection[i]).append(",");
			sb.append(selection[i]).append(" ");
		}
		else
			sb.append("* ");
		sb.append("from ").append(table).append(getCondition(condition, parameter));
		try {
			return db.query(sb.toString());
		} catch (SQLException e) {
			for(Throwable t : e)
				t.printStackTrace();
			return null;
		}
	}
	
	public boolean execute(String sql) throws SQLException{
		return db.execute(sql);
	}
	
	public int executeReturnCount(String sql) throws SQLException{
		return db.executeReturnCount(sql);
	}
	
	public ResultSet executeReturnResult(String sql) throws SQLException{
		return db.executeReturnResult(sql);
	}
	
	private String getCondition(String condition, String[] parameter) 
			throws ParameterException{
		StringBuilder sb=new StringBuilder();
		if(condition!=null){
			sb.append(" where ");
			if(!condition.contains("?")){
				sb.append(condition);
			}
			else{
				if(parameter==null)
					throw new ParameterException("parameter is null!");
				Matcher m=Pattern.compile("\\?").matcher(condition);
				
				int start=0, end=0, count=0;
				while(m.find()){
					start=m.start();
					sb.append(condition.substring(end, start));
					if(count>=parameter.length)
						throw new ParameterException("parameter's length is not match!");
					sb.append(parameter[count++]);
					end=m.end();
				}
				if(count<parameter.length)
					throw new ParameterException("parameter's length is not match!");
				sb.append(condition.substring(end));
			}
		}
		sb.append(";");
		return sb.toString();
	}
	
	@SuppressWarnings("serial")
	public static class ParameterException extends Exception {
		public ParameterException(){
			super();
		}
		public ParameterException(String s){
			super(s);
		}
	}
}
