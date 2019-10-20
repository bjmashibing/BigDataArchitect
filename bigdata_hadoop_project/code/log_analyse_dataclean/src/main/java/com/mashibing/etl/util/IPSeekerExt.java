package com.mashibing.etl.util;

import com.mashibing.common.GlobalConstants;
import com.mashibing.etl.util.ip.IPSeeker;

/**
 * 定义具体的ip解析的类，最终调用IpSeeker类(父类)<br/>
 * 解析ip最终的返回时：国家名称 省份名称 城市名称<br/>
 * 如果是国外的ip，那么直接设置为unknown unknown unknown<br/>
 * 如果是国内ip，如果没法进行解析，那么就设置为中国 unknown unknown<br/>
 * 
 * @author 马士兵教育
 *
 */
public class IPSeekerExt extends IPSeeker {
	private RegionInfo DEFAULT_INFO = new RegionInfo();

	/**
	 * 解析ip地址，返回该ip地址对应的国家省份信息<br/>
	 * 如果该ip解析失败，那么直接返回默认值
	 * 
	 * @param ip
	 *            要解析的ip地址，格式为: 120.197.87.216
	 * @return
	 */
	public RegionInfo analyticIp(String ip) {
		if (ip == null || ip.trim().isEmpty()) {
			return DEFAULT_INFO;
		}

		RegionInfo info = new RegionInfo();
		try {
			String country = super.getCountry(ip);
			if ("局域网".equals(country)) {
				info.setCountry("中国");
				info.setProvince("上海市");
			} else if (country != null && !country.trim().isEmpty()) {
				// 表示该ip还一个可以解析的ip
				country = country.trim();
				int length = country.length();
				int index = country.indexOf('省');
				if (index > 0) {
					// 当前ip属于23个省之间的一个，country的格式为：xxx省(xxx市)(xxx县/区)
					info.setCountry("中国");
					if (index == length - 1) {
						info.setProvince(country); // 设置省份，格式列入： 广东省
					} else {
						// 格式为：广东省广州市
						info.setProvince(country.substring(0, index + 1)); // 设置省份
						int index2 = country.indexOf('市', index); // 查看下一个出现市的位置
						if (index2 > 0) {
							country.substring(1, 1);
							info.setCity(country.substring(index + 1,
									Math.min(index2 + 1, length))); // 设置city
						}
					}
				} else {
					// 其他的五个自治区 四个直辖市 2个特别行政区
					String flag = country.substring(0, 2); // 拿字符串前两位
					switch (flag) {
					case "内蒙":
						info.setCountry("中国");
						info.setProvince("内蒙古自治区");
						country = country.substring(3);
						if (country != null && !country.isEmpty()) {
							index = country.indexOf('市');
							if (index > 0) {
								info.setCity(country.substring(0,
										Math.min(index + 1, country.length()))); // 设置市
							}
						}
						break;
					case "广西":
					case "西藏":
					case "宁夏":
					case "新疆":
						info.setCountry("中国");
						info.setProvince(flag);
						country = country.substring(2);
						if (country != null && !country.isEmpty()) {
							index = country.indexOf('市');
							if (index > 0) {
								info.setCity(country.substring(0,
										Math.min(index + 1, country.length()))); // 设置市
							}
						}
						break;
					case "上海":
					case "北京":
					case "天津":
					case "重庆":
						info.setCountry("中国");
						info.setProvince(flag + "市");
						country = country.substring(3); // 去除这个省份/直辖市
						if (country != null && !country.isEmpty()) {
							index = country.indexOf('区');
							if (index > 0) {
								char ch = country.charAt(index - 1);
								if (ch != '校' || ch != '小') {
									info.setCity(country.substring(
											0,
											Math.min(index + 1,
													country.length()))); // 设置区
								}
							}

							if (RegionInfo.DEFAULT_VALUE.equals(info.getCity())) {
								// city还是默认值
								index = country.indexOf('县');
								if (index > 0) {
									info.setCity(country.substring(
											0,
											Math.min(index + 1,
													country.length()))); // 设置区
								}
							}
						}
						break;
					case "香港":
					case "澳门":
						info.setCountry("中国");
						info.setProvince(flag + "特别行政区");
						break;
					default:
						break;
					}
				}
			}
		} catch (Exception e) {
			// 解析过程中出现异常
			e.printStackTrace();
		}
		return info;
	}

	/**
	 * ip地域相关的一个model
	 * 
	 * @author root
	 *
	 */
	public static class RegionInfo {
		public static final String DEFAULT_VALUE = GlobalConstants.DEFAULT_VALUE; // 默认值
		private String country = DEFAULT_VALUE; // 国家
		private String province = DEFAULT_VALUE; // 省份
		private String city = DEFAULT_VALUE; // 城市

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getProvince() {
			return province;
		}

		public void setProvince(String province) {
			this.province = province;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		@Override
		public String toString() {
			return "RegionInfo [country=" + country + ", province=" + province
					+ ", city=" + city + "]";
		}
	}
}
