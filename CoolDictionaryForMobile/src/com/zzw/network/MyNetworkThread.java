package com.zzw.network;

import java.util.LinkedHashMap;

import com.zzw.network.ContentReaderFactory.SimpleContentReader;
import android.os.Handler;
import android.os.Message;

public class MyNetworkThread implements Runnable {
	private String word;
	private SimpleContentReader youdaocr;
	private SimpleContentReader bingcr;
	private SimpleContentReader jinshancr;
	private LinkedHashMap<String,String> query;
	private Handler handler;

	public MyNetworkThread(String word, Handler handler){
		this.word=word;
		this.handler=handler;
		youdaocr=(SimpleContentReader)ContentReaderFactory.
				getYoudaoContentReader("<h2 class=\"wordbook-js\">", 2);
		bingcr=(SimpleContentReader)ContentReaderFactory.
				getBingContentReader("<div class=\"hd_area\">", 2);
		jinshancr=(SimpleContentReader)ContentReaderFactory.
				getJinshanContentReader("<div class=\"in-base-top clearfix\" class=\"clearfix\">", 2);
		query=new LinkedHashMap<String,String>();
	}
	
	@Override
	public void run() {
		// 查询有道词典
		query.put("q", word);
		HttpConnectionHelper.get("http://dict.youdao.com", 
				"/search", query, "UTF-8", youdaocr);
		// 查询必应词典
		HttpConnectionHelper.get("http://cn.bing.com", 
				"/dict/search", query, "UTF-8", bingcr);
		// 查询金山词典
		HttpConnectionHelper.get("http://www.iciba.com", 
				word, null, "UTF-8", jinshancr);
		Message msg=new Message();
		msg.what=2;
		msg.obj=youdaocr.getKeyword()+"&"+youdaocr.getTranslation()+"&"+
				bingcr.getTranslation()+"&"+jinshancr.getTranslation();
		//Log.d("TAG", "translation: "+msg.obj);
		handler.sendMessage(msg);
	}

}
