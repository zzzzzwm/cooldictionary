package com.zzw.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import android.os.Handler;
import android.os.Message;

public class MyThread implements Runnable {
	private String mServerAddr;			// 服务器地址
	private int mServerPort;			// 服务器端口
	private Selector mSelector=null;	// 选择器
	private SocketChannel mChannel=null;// 网络通道
	private SelectionKey mKey=null;		// 连接句柄
	private Semaphore mSemaphore;		// 信号量
	private Handler mHandler;			// 接收输入
	private Handler yHandler;			// 传递输出
	
	public MyThread(String addr, int port){
		mServerAddr=addr;
		mServerPort=port;
		mSemaphore=new Semaphore(0);
		mHandler=new Handler(){
			@Override
			public void handleMessage(Message msg){
				final String req=(String)(msg.obj);
				new Thread(){
					@Override
					public void run(){
						write(req);
					}
				}.start();
			}
		};
	}

	public void setOuterHandler(Handler h){
		yHandler=h;
	}
	public Handler getInnerHandler(){
		return mHandler;
	}
	
	@Override
	public void run() {
		try {
			mSelector=Selector.open();
			mChannel=SocketChannel.open();
			mChannel.configureBlocking(false);
			mChannel.register(mSelector, SelectionKey.OP_CONNECT);
			mChannel.connect(new InetSocketAddress(mServerAddr, mServerPort));
			
			while(true){
				if(mSelector.select()<=0)
					continue;
				Iterator<SelectionKey> iterator=mSelector.
						selectedKeys().iterator();
				while(iterator.hasNext()){
					SelectionKey key=iterator.next();
					iterator.remove();
					if(key.isConnectable()){
						connect(key);
					}
					else if(key.isReadable()){
						String res=read(key);
						if(res!=null && yHandler!=null){
							Message msg=Message.obtain();
							msg.what=1;
							msg.obj=res;
							yHandler.sendMessage(msg);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void connect(SelectionKey key){
		SocketChannel sc=(SocketChannel)key.channel();
		try {
			if(sc.isConnectionPending())
				sc.finishConnect();
			sc.configureBlocking(false);
			mKey=sc.register(mSelector, SelectionKey.OP_READ);
			mSemaphore.release();
		} catch (IOException e) {
			e.printStackTrace();
			key.cancel();
		}
	}
	
	synchronized String read(SelectionKey key){
		SocketChannel sc=(SocketChannel)key.channel();
		ByteBuffer bb=ByteBuffer.allocate(10240);
		try {
			sc.read(bb);
			bb.flip();
			CharBuffer cb=Charset.forName("UTF-8").decode(bb);
			return cb.toString().trim();
		} catch (IOException e) {
			e.printStackTrace();
			if(yHandler!=null){
				Message msg=Message.obtain();
				msg.what=0;
				yHandler.sendMessage(msg);
			}
			key.cancel();
			return null;
		}
	}
	
	synchronized void write(String req){
		try {
			if(mKey==null)
				mSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		SocketChannel sc=(SocketChannel)mKey.channel();
		try {
			sc.write(ByteBuffer.wrap(req.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
			if(yHandler!=null){
				Message msg=Message.obtain();
				msg.what=0;
				yHandler.sendMessage(msg);
			}
			mKey.cancel();
		}
	}

}
