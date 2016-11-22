package com.zzw.zwm.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import com.zzw.zwm.network.HttpConnectionHelper;
import com.zzw.zwm.network.HttpConnectionHelper.ContentReader;
import com.zzw.zwm.util.ContentReaderFactory;

public class HttpConnectionHelperTest {
	public static void main(String[] args){
		// 准备文件输入输出
		BufferedReader in=null;
		PrintWriter youdao=null, bing=null, baidu=null;
		
		try {
			in=new BufferedReader(
					new InputStreamReader(
							new FileInputStream("words.txt")));
			youdao=new PrintWriter(
					new FileOutputStream("youdao.txt"));
			bing=new PrintWriter(
					new FileOutputStream("bing.txt"));
			baidu=new PrintWriter(
					new FileOutputStream("baidu.txt"));
		} catch (FileNotFoundException e) {
			System.err.println("file not find!");
			e.printStackTrace();
		}
		
		// 准备内容读取器
		ContentReader youdaocr=ContentReaderFactory.
				getYoudaoContentReader(
						"<h2 class=\"wordbook-js\">", 2);
		ContentReader bingcr=ContentReaderFactory.
				getBingContentReader(
						"<div class=\"hd_area\">", 2);
		ContentReader baiducr=ContentReaderFactory.
				getBaiduContentReader(
						"<div id=\"english-header\"", 3);
		LinkedHashMap<String,String> query=
				new LinkedHashMap<String,String>();
		
		// 开始测试
		System.out.println("TEST STARTING!");
		String word;
		try {
			while((word=in.readLine())!=null){
				System.out.println("TEST - "+word);
				// 查询有道词典
				query.clear();
				query.put("q", word);
				youdaocr.reset();
				HttpConnectionHelper.get("http://dict.youdao.com", 
						"/search", query, "UTF-8", youdaocr);
				youdao.write(youdaocr.getCount()+"\t"+word+"\n");
				youdao.write(youdaocr.getContent()+"\n\n");
				// 查询必应词典
				bingcr.reset();
				HttpConnectionHelper.get("http://cn.bing.com", 
						"/dict/search", query, "UTF-8", bingcr);
				bing.write(bingcr.getCount()+"\t"+word+"\n");
				bing.write(bingcr.getContent()+"\n\n");
				// 查询百度词典
				query.clear();
				query.put("wd", word);
				baiducr.reset();
				HttpConnectionHelper.get("http://dict.baidu.com", 
						"/s", query, "UTF-8", baiducr);
				baidu.write(baiducr.getCount()+"\t"+word+"\n");
				baidu.write(baiducr.getContent()+"\n\n");
			}
			System.out.println("TEST FINISHED!");
		} catch (IOException e) {
			System.err.println("reader I/O exception!");
			e.printStackTrace();
		} finally {
			try {
				if(in!=null)
					in.close();
				if(youdao!=null)
					youdao.close();
				if(bing!=null)
					bing.close();
				if(baidu!=null)
					baidu.close();
				} catch (IOException e) {
					System.err.println("file close failed!");
					e.printStackTrace();
				}
		}
		
	}
}
