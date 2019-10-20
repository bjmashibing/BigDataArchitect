<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>测试页面3</title>
<script type="text/javascript" src="./js/analytics.js"></script>
</head>
<body>
	测试页面3<br/>
	<label>category: event的category名称</label><br/>
	<label>action: event的action名称</label><br/>
	<label>map: {"key1":"value1", "key2":"value2"}</label><br/>
	<label>duration: 1245</label><br/>
	<button onclick="__AE__.onEventDuration('event的category名称','event的action名称', {'key1':'value1','key2':'value2'}, 1245)">触发带map和duration的事件</button><br/>
	<button onclick="__AE__.onEventDuration('event的category名称','event的action名称')">触发不带map和duration的事件</button><br/>
	跳转到:
	<a href="demo.jsp">demo</a>
	<a href="demo2.jsp">demo2</a>
	<a href="demo3.jsp">demo3</a>
	<a href="demo4.jsp">demo4</a>
</body>
</html>