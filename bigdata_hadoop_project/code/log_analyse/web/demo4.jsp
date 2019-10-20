<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>测试页面4</title>
<script type="text/javascript">
(function(){
	var _aelog_ = _aelog_ || window._aelog_ || [];
	// 设置_aelog_相关属性
	_aelog_.push(["memberId","zhangsan"]);
	window._aelog_ = _aelog_;
	(function(){
	    var aejs = document.createElement('script');
	    aejs.type = 'text/javascript';
	    aejs.async = true;
	    aejs.src = './js/analytics.js';
	    var script = document.getElementsByTagName('script')[0];
	    script.parentNode.insertBefore(aejs, script);
	})();
})();
</script>
</head>
<body>
	测试页面4<br/>
	在本页面设置memberid为zhangsan<br/>
	跳转到:
	<a href="demo.jsp">demo</a>
	<a href="demo2.jsp">demo2</a>
	<a href="demo3.jsp">demo3</a>
	<a href="demo4.jsp">demo4</a>
</body>
</html>