package com.mashibing.common;

/**
 * 定义日志收集客户端收集得到的用户数据参数的name名称<br/>
 * 以及event_logs这张hbase表的结构信息<br/>
 * 用户数据参数的name名称就是event_logs的列名
 * 
 * @author 马士兵教育
 *
 */
public class EventLogConstants {
	
	/**
	 * 事件枚举类。指定事件的名称
	 * 
	 * @author root
	 *
	 */
	public static enum EventEnum {
		LAUNCH(1, "launch event", "e_l"), // launch事件，表示第一次访问
		PAGEVIEW(2, "page view event", "e_pv"), // 页面浏览事件
		CHARGEREQUEST(3, "charge request event", "e_crt"), // 订单生产事件
		CHARGESUCCESS(4, "charge success event", "e_cs"), // 订单成功支付事件
		CHARGEREFUND(5, "charge refund event", "e_cr"), // 订单退款事件
		EVENT(6, "event duration event", "e_e") // 事件
		;

		public final int id; // id 唯一标识
		public final String name; // 名称
		public final String alias; // 别名，用于数据收集的简写

		private EventEnum(int id, String name, String alias) {
			this.id = id;
			this.name = name;
			this.alias = alias;
		}

		/**
		 * 获取匹配别名的event枚举对象，如果最终还是没有匹配的值，那么直接返回null。
		 * 
		 * @param alias
		 * @return
		 */
		public static EventEnum valueOfAlias(String alias) {
			for (EventEnum event : values()) {
				if (event.alias.equals(alias)) {
					return event;
				}
			}
			return null;
		}
	}

	/**
	 * 表名称
	 */
	public static final String HBASE_NAME_EVENT_LOGS = "eventlog";

	/**
	 * event_logs表的列簇名称
	 */
	public static final String EVENT_LOGS_FAMILY_NAME = "log";

	/**
	 * 日志分隔符
	 */
	public static final String LOG_SEPARTIOR = "\\^A";

	/**
	 * 用户ip地址
	 */
	public static final String LOG_COLUMN_NAME_IP = "ip";

	/**
	 * 服务器时间
	 */
	public static final String LOG_COLUMN_NAME_SERVER_TIME = "s_time";

	/**
	 * 事件名称
	 */
	public static final String LOG_COLUMN_NAME_EVENT_NAME = "en";

	/**
	 * 数据收集端的版本信息
	 */
	public static final String LOG_COLUMN_NAME_VERSION = "ver";

	/**
	 * 用户唯一标识符
	 */
	public static final String LOG_COLUMN_NAME_UUID = "u_ud";

	/**
	 * 会员唯一标识符
	 */
	public static final String LOG_COLUMN_NAME_MEMBER_ID = "u_mid";

	/**
	 * 会话id
	 */
	public static final String LOG_COLUMN_NAME_SESSION_ID = "u_sd";
	/**
	 * 客户端时间
	 */
	public static final String LOG_COLUMN_NAME_CLIENT_TIME = "c_time";
	/**
	 * 语言
	 */
	public static final String LOG_COLUMN_NAME_LANGUAGE = "l";
	/**
	 * 浏览器user agent参数
	 */
	public static final String LOG_COLUMN_NAME_USER_AGENT = "b_iev";
	/**
	 * 浏览器分辨率大小
	 */
	public static final String LOG_COLUMN_NAME_RESOLUTION = "b_rst";

	/**
	 * 定义platform
	 */
	public static final String LOG_COLUMN_NAME_PLATFORM = "pl";
	/**
	 * 当前url
	 */
	public static final String LOG_COLUMN_NAME_CURRENT_URL = "p_url";
	/**
	 * 前一个页面的url
	 */
	public static final String LOG_COLUMN_NAME_REFERRER_URL = "p_ref";
	/**
	 * 当前页面的title
	 */
	public static final String LOG_COLUMN_NAME_TITLE = "tt";
	/**
	 * 订单id
	 */
	public static final String LOG_COLUMN_NAME_ORDER_ID = "oid";
	/**
	 * 订单名称
	 */
	public static final String LOG_COLUMN_NAME_ORDER_NAME = "on";
	/**
	 * 订单金额
	 */
	public static final String LOG_COLUMN_NAME_ORDER_CURRENCY_AMOUNT = "cua";
	/**
	 * 订单货币类型
	 */
	public static final String LOG_COLUMN_NAME_ORDER_CURRENCY_TYPE = "cut";
	/**
	 * 订单支付金额
	 */
	public static final String LOG_COLUMN_NAME_ORDER_PAYMENT_TYPE = "pt";
	/**
	 * category名称
	 */
	public static final String LOG_COLUMN_NAME_EVENT_CATEGORY = "ca";
	/**
	 * action名称
	 */
	public static final String LOG_COLUMN_NAME_EVENT_ACTION = "ac";
	/**
	 * kv前缀
	 */
	public static final String LOG_COLUMN_NAME_EVENT_KV_START = "kv_";
	/**
	 * duration持续时间
	 */
	public static final String LOG_COLUMN_NAME_EVENT_DURATION = "du";
	/**
	 * 操作系统名称
	 */
	public static final String LOG_COLUMN_NAME_OS_NAME = "os";
	/**
	 * 操作系统版本
	 */
	public static final String LOG_COLUMN_NAME_OS_VERSION = "os_v";
	/**
	 * 浏览器名称
	 */
	public static final String LOG_COLUMN_NAME_BROWSER_NAME = "browser";
	/**
	 * 浏览器版本
	 */
	public static final String LOG_COLUMN_NAME_BROWSER_VERSION = "browser_v";
	/**
	 * ip地址解析的所属国家
	 */
	public static final String LOG_COLUMN_NAME_COUNTRY = "country";
	/**
	 * ip地址解析的所属省份
	 */
	public static final String LOG_COLUMN_NAME_PROVINCE = "province";
	/**
	 * ip地址解析的所属城市
	 */
	public static final String LOG_COLUMN_NAME_CITY = "city";
}
