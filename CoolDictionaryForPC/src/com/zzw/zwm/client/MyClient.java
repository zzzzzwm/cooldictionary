package com.zzw.zwm.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class MyClient {
	// 服务器地址和端口
	private static final String SERVER_ADDRESS="localhost";
	private static final int PORT=7999;
	
	private Selector mSelector=null;	// 选择器
	private SocketChannel mChannel=null;// 网络通道
	private Thread mServerThread=null;	// 服务器连接线程
	
	private SelectionKey mKey=null;		// 连接句柄
	private Semaphore mSemaphore=new Semaphore(0);
	
	public MyClient() throws IOException {
		// TODO 定义并显示图形用户界面
		
		// 服务器连接
		mSelector=Selector.open();
		mChannel=SocketChannel.open();
		mChannel.configureBlocking(false);
		mChannel.register(mSelector, SelectionKey.OP_CONNECT);
		mChannel.connect(new InetSocketAddress(SERVER_ADDRESS, PORT));

		mServerThread=new Thread(){
			@Override
			public void run(){
				try {
					while(!isInterrupted()){
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
								String msg=read(key);
								// TODO 这里接收来自服务器的数据，
								// 根据这些数据更新图形用户界面。
								if(msg!=null)
									System.out.println(msg);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					try {
						mChannel.close();
						mSelector.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					interrupt();
				}
			}
		};
		mServerThread.start();
		
		// TODO 用户输入，客户端和服务器通信
		Scanner in=new Scanner(System.in);
		while(in.hasNext()){
			String msg=in.nextLine();
			if(msg!=null)
				write(msg);
		}
		in.close();
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
			try {
				mChannel.close();
				mSelector.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			mServerThread.interrupt();
		}
	}
	
	String read(SelectionKey key){
		SocketChannel sc=(SocketChannel)key.channel();
		ByteBuffer bb=ByteBuffer.allocate(1024);
		try {
			sc.read(bb);
			bb.flip();
			CharBuffer cb=Charset.forName("UTF-8").decode(bb);
			//System.out.println("*"+cb.toString().trim()+"*");
			return cb.toString().trim();
			//return new String(bb.array()).trim();
		} catch (IOException e) {
			// TODO 通知用户连接断开，在图形用户界面显示
			System.err.println("[ BREAK ] 与服务器的连接已断开！");
			e.printStackTrace();
			key.cancel();
			try {
				mChannel.close();
				mSelector.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			mServerThread.interrupt();
			return null;
		}
	}
	
	void write(String msg){
		// 保证在向服务器传递数据之前，已经建立与服务器之间的连接。
		try {
			if(mKey==null)
				mSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		SocketChannel sc=(SocketChannel)mKey.channel();
		try {
			sc.write(ByteBuffer.wrap(msg.getBytes("UTF-8")));
		} catch (IOException e) {
			// TODO 通知用户与服务器的连接已断开，在图形用户界面显示
			System.err.println("[ BREAK ] 与服务器的连接已断开！");
			e.printStackTrace();
			mKey.cancel();
			try {
				mChannel.close();
				mSelector.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			mServerThread.interrupt();
		}
	}
	
	public static class Test{
		public static void main(String[] args){
			try {
				new MyClient();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("连接服务器失败！");
			}
		}
	}
}
