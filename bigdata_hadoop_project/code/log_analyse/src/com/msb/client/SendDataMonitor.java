package com.msb.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 发送url数据的监控者，用于启动一个单独的线程来发送数据
 */
public class SendDataMonitor {
	// 日志记录对象
	private static final Logger log = Logger.getGlobal();
	// 队列，用户存储发送url
	private BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	// 用于单列的一个类对象
	private static SendDataMonitor monitor = null;

	private SendDataMonitor() {
		// 私有构造方法，进行单列模式的创建
	}

	/**
	 * 获取单列的monitor对象实例
	 * 
	 * @return
	 */
	public static SendDataMonitor getSendDataMonitor() {
		if (monitor == null) {
			synchronized (SendDataMonitor.class) {
				if (monitor == null) {
					monitor = new SendDataMonitor();

					Thread thread = new Thread(new Runnable() {

						@Override
						public void run() {
							// 线程中调用具体的处理方法
							SendDataMonitor.monitor.run();
						}
					});
					// 测试的时候，不设置为守护模式
					// thread.setDaemon(true);
					thread.start();
				}
			}
		}
		return monitor;
	}

	/**
	 * 添加一个url到队列中去
	 * 
	 * @param url
	 * @throws InterruptedException
	 */
	public static void addSendUrl(String url) throws InterruptedException {
		getSendDataMonitor().queue.put(url);
	}

	/**
	 * 具体执行发送url的方法
	 * 
	 */
	private void run() {
		while (true) {
			try {
				String url = this.queue.take();
				// 正式的发送url
				HttpRequestUtil.sendData(url);
			} catch (Throwable e) {
				log.log(Level.WARNING, "发送url异常", e);
			}
		}
	}

	/**
	 * 内部类，用户发送数据的http工具类
	 * 
	 * @author root
	 *
	 */
	public static class HttpRequestUtil {
		/**
		 * 具体发送url的方法
		 * 
		 * @param url
		 * @throws IOException
		 */
		public static void sendData(String url) throws IOException {
			HttpURLConnection con = null;
			BufferedReader in = null;

			try {
				URL obj = new URL(url); // 创建url对象
				con = (HttpURLConnection) obj.openConnection(); // 打开url连接
				// 设置连接参数
				con.setConnectTimeout(5000); // 连接过期时间
				con.setReadTimeout(5000); // 读取数据过期时间
				con.setRequestMethod("GET"); // 设置请求类型为get

				System.out.println("发送url:" + url);
				// 发送连接请求
//				in = new BufferedReader(new InputStreamReader(
//						con.getInputStream()));
				// TODO: 这里考虑是否可以
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (Throwable e) {
					// nothing
				}
				try {
					con.disconnect();
				} catch (Throwable e) {
					// nothing
				}
			}
		}
	}
}
