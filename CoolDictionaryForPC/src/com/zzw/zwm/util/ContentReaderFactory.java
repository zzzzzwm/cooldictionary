package com.zzw.zwm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zzw.zwm.network.HttpConnectionHelper.ContentReader;

public class ContentReaderFactory {
	private ContentReaderFactory(){}
	
	// "<h2 class=\"wordbook-js\">"
	public static ContentReader getYoudaoContentReader(
			String tab, int seg){
		return new SimpleContentReader(tab, seg){
			@Override
			public String readNext(BufferedReader reader) {
				return readLine(reader);
			}
			@Override
			public long skip(BufferedReader reader) {
				try {
					return reader.skip(5500);
				} catch (IOException e) {
					System.err.println("reader skip failed!");
					e.printStackTrace();
					return -1;
				}
				//return 0;
			}
		};
	}
	
	// "<div class=\"hd_area\">"
	public static ContentReader getBingContentReader(
			String tab, int seg){
		return new SimpleContentReader(tab, seg){
			@Override
			public String readNext(BufferedReader reader) {
				return readLine(reader);
			}
			@Override
			public long skip(BufferedReader reader) {
				try {
					return reader.skip(66000);
				} catch (IOException e) {
					System.err.println("reader skip failed!");
					e.printStackTrace();
					return -1;
				}
				//return 0;
			}
		};
	}
	
	public static ContentReader getBaiduContentReader(
			String tab, int seg){
		return new SimpleContentReader(tab, seg){
			@Override
			public String readNext(BufferedReader reader) {
				return readLine(reader);
			}
			@Override
			public long skip(BufferedReader reader) {
				try {
					return reader.skip(3500);
				} catch (IOException e) {
					System.err.println("reader skip failed!");
					e.printStackTrace();
					return -1;
				}
				//return 0;
			}
		};
	}
}

abstract class SimpleContentReader extends ContentReader {
	private boolean isStart=false;	// 是否开始读取
	private int headNum=0;			// HTML头标记数
	private int tailNum=0;			// HTML尾标记数
	private int segnNum=0;			// HTML段标记数
	private String startTab;		// 开始读取标记
	private int segment;			// 需要读取段数
	private long count;

	public SimpleContentReader(String tab, int seg){
		startTab=tab;
		segment=seg;
	}
	
	@Override
	public void append(String s) {
		if(s.contains(startTab)){
			Matcher m=Pattern.compile(startTab).matcher(s);
			m.find();
			s=s.substring(m.start());
			isStart=true;
		}
		else
			count+=s.getBytes().length;
		if(isStart){
			Matcher mhead=head.matcher(s);
			Matcher mtail=tail.matcher(s);
			int offset=0;
			
			while(true){
				int hp=-1, tp=-1;
				if(mhead.find(offset)){ hp=mhead.end(); }
				if(mtail.find(offset)){ tp=mtail.end(); }
				
				if(hp>-1 || tp>-1){
					if((tp>hp && hp>-1) || tp==-1){
						++headNum;
						offset=hp;
					}
					else{
						++tailNum;
						offset=tp;
					}
					
					if(headNum==tailNum){
						if(++segnNum>=segment){
							content.append(s.substring(0, offset));
							isFinish=true;
							return;
						}
						headNum=tailNum=0;
					}
				}
				else{
					content.append(s);
					return;
				}
			}
		}
	}
	
	@Override
	public long getCount(){
		return count;
	}
	
	@Override
	public void reset(){
		super.reset();
		isStart=false;
		headNum=tailNum=segnNum=0;
		count=0;
	}
}