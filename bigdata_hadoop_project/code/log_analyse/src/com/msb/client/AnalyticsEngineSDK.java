package com.msb.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 分析引擎sdk java服务器端数据收集
 */
public class AnalyticsEngineSDK {
	// 日志打印对象
	private static final Logger log = Logger.getGlobal();
	// 请求url的主体部分
	public static final String accessUrl = "http://node2/log.gif";
	private static final String platformName = "java_server";
	private static final String sdkName = "jdk";
	private static final String version = "1";

	/**
	 * 触发订单支付成功事件，发送事件数据到服务器
	 * 
	 * @param orderId
	 *            订单支付id
	 * @param memberId
	 *            订单支付会员id
	 * @return 如果发送数据成功(加入到发送队列中)，那么返回true；否则返回false(参数异常&添加到发送队列失败).
	 */
	public static boolean onChargeSuccess(String orderId, String memberId) {
		try {
			if (isEmpty(orderId) || isEmpty(memberId)) {
				// 订单id或者memberid为空
				log.log(Level.WARNING, "订单id和会员id不能为空");
				return false;
			}
			// 代码执行到这儿，表示订单id和会员id都不为空。
			Map<String, String> data = new HashMap<String, String>();
			data.put("u_mid", memberId);
			data.put("oid", orderId);
			data.put("c_time", String.valueOf(System.currentTimeMillis()));
			data.put("ver", version);
			data.put("en", "e_cs");
			data.put("pl", platformName);
			data.put("sdk", sdkName);
			// 创建url
			String url = buildUrl(data);
			// 发送url&将url加入到队列
			SendDataMonitor.addSendUrl(url);
			return true;
		} catch (Throwable e) {
			log.log(Level.WARNING, "发送数据异常", e);
		}
		return false;
	}

	/**
	 * 触发订单退款事件，发送退款数据到服务器
	 * 
	 * @param orderId
	 *            退款订单id
	 * @param memberId
	 *            退款会员id
	 * @return 如果发送数据成功，返回true。否则返回false。
	 */
	public static boolean onChargeRefund(String orderId, String memberId) {
		try {
			if (isEmpty(orderId) || isEmpty(memberId)) {
				// 订单id或者memberid为空
				log.log(Level.WARNING, "订单id和会员id不能为空");
				return false;
			}
			// 代码执行到这儿，表示订单id和会员id都不为空。
			Map<String, String> data = new HashMap<String, String>();
			data.put("u_mid", memberId);
			data.put("oid", orderId);
			data.put("c_time", String.valueOf(System.currentTimeMillis()));
			data.put("ver", version);
			data.put("en", "e_cr");
			data.put("pl", platformName);
			data.put("sdk", sdkName);
			// 构建url
			String url = buildUrl(data);
			// 发送url&将url添加到队列中
			SendDataMonitor.addSendUrl(url);
			return true;
		} catch (Throwable e) {
			log.log(Level.WARNING, "发送数据异常", e);
		}
		return false;
	}

	/**
	 * 根据传入的参数构建url
	 * 
	 * @param data
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static String buildUrl(Map<String, String> data)
			throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append(accessUrl).append("?");
		for (Map.Entry<String, String> entry : data.entrySet()) {
			if (isNotEmpty(entry.getKey()) && isNotEmpty(entry.getValue())) {
				sb.append(entry.getKey().trim())
						.append("=")
						.append(URLEncoder.encode(entry.getValue().trim(), "utf-8"))
						.append("&");
			}
		}
		return sb.substring(0, sb.length() - 1);// 去掉最后&
	}

	/**
	 * 判断字符串是否为空，如果为空，返回true。否则返回false。
	 * 
	 * @param value
	 * @return
	 */
	private static boolean isEmpty(String value) {
		return value == null || value.trim().isEmpty();
	}

	/**
	 * 判断字符串是否非空，如果不是空，返回true。如果是空，返回false。
	 * 
	 * @param value
	 * @return
	 */
	private static boolean isNotEmpty(String value) {
		return !isEmpty(value);
	}
}
