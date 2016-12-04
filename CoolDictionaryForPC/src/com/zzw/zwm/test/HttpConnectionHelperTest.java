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
import com.zzw.zwm.util.ContentReaderFactory;
import com.zzw.zwm.util.ContentReaderFactory.SimpleContentReader;

public class HttpConnectionHelperTest {
	public static void main(String[] args){
		// 准备文件输入输出
		BufferedReader in=null;
		PrintWriter youdao=null, bing=null, baidu=null, jinshan=null;
		
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
			jinshan=new PrintWriter(
					new FileOutputStream("jinshan.txt"));
		} catch (FileNotFoundException e) {
			System.err.println("file not find!");
			e.printStackTrace();
		}
		
		// 准备内容读取器
		SimpleContentReader youdaocr=(SimpleContentReader)ContentReaderFactory.
				getYoudaoContentReader("<h2 class=\"wordbook-js\">", 2);
		SimpleContentReader bingcr=(SimpleContentReader)ContentReaderFactory.
				getBingContentReader("<div class=\"hd_area\">", 2);
		SimpleContentReader baiducr=(SimpleContentReader)ContentReaderFactory.
				getBaiduContentReader("<div id=\"simple_means\"", 2);
		SimpleContentReader jinshancr=(SimpleContentReader)ContentReaderFactory.
				getJinshanContentReader("<div class=\"in-base-top clearfix\" class=\"clearfix\">", 2);
		LinkedHashMap<String,String> query=
				new LinkedHashMap<String,String>();
		
		// 开始测试
		System.out.println("TEST STARTING!");
		String word;
		try {
			while((word=in.readLine())!=null){
				System.out.println("TEST - "+word);
				/*// 查询有道词典
				query.clear();
				query.put("q", word);
				youdaocr.reset();
				HttpConnectionHelper.get("http://dict.youdao.com", 
						"/search", query, "UTF-8", youdaocr);
				//youdao.write(youdaocr.getCount()+"\t"+word+"\n");
				youdao.write(youdaocr.getKeyword()+"\n"+youdaocr.getTranslation()+"\n\n");
				// 查询必应词典
				bingcr.reset();
				HttpConnectionHelper.get("http://cn.bing.com", 
						"/dict/search", query, "UTF-8", bingcr);
				//bing.write(bingcr.getCount()+"\t"+word+"\n");
				bing.write(bingcr.getKeyword()+"\n"+bingcr.getTranslation()+"\n\n");
				// 查询百度词典
				query.clear();
				query.put("wd", word);
				baiducr.reset();
				HttpConnectionHelper.get("http://dict.baidu.com", 
						"/s", query, "UTF-8", baiducr);
				//baidu.write(baiducr.getCount()+"\t"+word+"\n");
				baidu.write(baiducr.getTranslation()+"\n\n");
				*/// 查询金山
				jinshancr.reset();
				HttpConnectionHelper.get("http://www.iciba.com", 
						word, null, "UTF-8", jinshancr);
				jinshan.write(jinshancr.getContent()+"\n\n");
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
