package test;

import com.mashibing.etl.util.UserAgentUtil;
import com.mashibing.etl.util.UserAgentUtil.UserAgentInfo;


public class TestUserAgentUtil {
	public static void main(String[] args) {
		String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36";
		userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; GWX:QUALIFIED; rv:11.0) like Gecko";
		userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0";
		UserAgentInfo info = UserAgentUtil.analyticUserAgent(userAgent);
		System.out.println(info);
	}
}
