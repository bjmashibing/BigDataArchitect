package com.msb.test;

import com.msb.client.AnalyticsEngineSDK;
public class Test {
	public static void main(String[] args) {
		AnalyticsEngineSDK.onChargeSuccess("orderid123", "zhangsan");
		AnalyticsEngineSDK.onChargeRefund("orderid456", "lisi");
	}
}
