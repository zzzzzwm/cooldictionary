package com.zzw.zwm.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * 服务器，采用NIO方式实现的单线程多客户的服务器端，
 * 避免了开启、维护和切换线程的额外开销，另外实现了
 * 非阻塞通信和I/O。
 * @author zzw
 * @version 1.0
 */
public class MyServer implements MyProcesser.MyDevelopmentKit {
	private Selector mSelector;				// 选择子
	private ServerSocketChannel mChannel;	// 通道
	private MyProcesser mProcesser;			// 处理器
	private static MyServer mInstance;		// 静态实例
	
	public static MyServer getInstance() throws IOException {
		if(mInstance==null){
			synchronized(MyServer.class){
				if(mInstance==null)
					mInstance=new MyServer();
			}
		}
		return mInstance;
	}
	
	/**
	 * MyServer类的构造方法，在此方法中打开选择子和通道，
	 * 并将通道设置成非阻塞态，绑定端口和注册ACCEPT事件；
	 * 另外还需对处理器进行初始化。
	 * @throws IOException
	 */
	private MyServer() throws IOException {
		mSelector=Selector.open();
		mChannel=ServerSocketChannel.open();
		mChannel.socket().setReuseAddress(true);
		mChannel.bind(new InetSocketAddress(7999));
		mChannel.configureBlocking(false);
		mChannel.register(mSelector, SelectionKey.OP_ACCEPT);
		mProcesser=new MyProcesser(this);
	}
	
	/**
	 * 开始服务，在此过程中，服务器不断监听来自客户的请求，并将请求送至
	 * 处理器进行处理；在每次循环开始阶段，服务器分发所有可以分发的词条。
	 */
	public void start(){
		SelectionKey key=null;
		try {
			while(true){
				mProcesser.dispatchItem(mSelector);
				if(mSelector.select()<=0)
					continue;
				Iterator<SelectionKey> iterator=mSelector.
						selectedKeys().iterator();
				while(iterator.hasNext()){
					key=iterator.next();
					iterator.remove();
					// 网络连接建立
					if(key.isAcceptable()){
						accept(key);
					}
					// 用户请求到达
					else if(key.isReadable()){
						String require=read(key);
						/**
						 * 这里真正处理用户请求，用户请求被抽取出来交由处理器处理。
						 * 不同的处理器定义C/S交互的数据格式，提供不同的处理流程，
						 * 所有的处理细节都被封装在处理器中，处理器只对外提供
						 * process()方法。
						 */
						if(require!=null)
							mProcesser.process(key, require);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				mSelector.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * 网络连接建立，此阶段还未真正接收用户的请求，而只是建立起TCP连接，
	 * 需要获取通信通道，并设置其为非阻塞态；最后需要在此通道上注册READ
	 * 事件来监听用户请求。
	 * @param key
	 */
	public void accept(SelectionKey key){
		SocketChannel sc=null;
		try {
			sc=((ServerSocketChannel)(key.channel())).accept();
			String remoteAddress=sc.getRemoteAddress().toString();
			mProcesser.log("CONNECT", remoteAddress);
			sc.configureBlocking(false);
			sc.register(mSelector, SelectionKey.OP_READ).attach(remoteAddress);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
			try {
				if(sc!=null)
					sc.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			key.cancel();
		}
	}
	
	/**
	 * 从客户端读取数据
	 */
	synchronized public String read(SelectionKey key){
		SocketChannel sc=(SocketChannel)key.channel();
		ByteBuffer bb=ByteBuffer.allocate(10240);
		try {
			sc.read(bb);
			bb.flip();
			CharBuffer cb=Charset.forName("UTF-8").decode(bb);
			//System.out.println("*"+cb.toString().trim()+"*");
			return cb.toString().trim();
			//return new String(bb.array()).trim();
		} catch (IOException e) {
			try {
				if(!key.attachment().toString().contains(".")){
					mProcesser.execute("update user set online=false where name='"+
							key.attachment()+"';");
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			mProcesser.log(" BREAK ", key.attachment().toString());
			key.cancel();
			return null;
		}
	}
	
	/**
	 * 向客户端发送数据
	 */
	public synchronized void write(SelectionKey key, String msg){
		SocketChannel sc=(SocketChannel)key.channel();
		try {
			sc.write(ByteBuffer.wrap(msg.getBytes("UTF-8")));
		} catch (IOException e) {
			try {
				if(!key.attachment().toString().contains(".")){
					mProcesser.execute("update user set online=false where name='"+
							key.attachment()+"';");
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			mProcesser.log(" BREAK ", key.attachment().toString());
			key.cancel();
		}
	}
	
	
	public static class Test{
		public static void main(String[] args){
			try {
				MyServer.getInstance().start();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("服务器启动失败！");
			}
		}
	}

}
