(function() {
	var CookieUtil = {
		// get the cookie of the key is name
		get : function(name) {
			var cookieName = encodeURIComponent(name) + "=", cookieStart = document.cookie
					.indexOf(cookieName), cookieValue = null;
			if (cookieStart > -1) {
				var cookieEnd = document.cookie.indexOf(";", cookieStart);
				if (cookieEnd == -1) {
					cookieEnd = document.cookie.length;
				}
				cookieValue = decodeURIComponent(document.cookie.substring(
						cookieStart + cookieName.length, cookieEnd));
			}
			return cookieValue;
		},
		// set the name/value pair to browser cookie
		set : function(name, value, expires, path, domain, secure) {
			var cookieText = encodeURIComponent(name) + "="
					+ encodeURIComponent(value);

			if (expires) {
				// set the expires time
				var expiresTime = new Date();
				expiresTime.setTime(expires);
				cookieText += ";expires=" + expiresTime.toGMTString();
			}

			if (path) {
				cookieText += ";path=" + path;
			}

			if (domain) {
				cookieText += ";domain=" + domain;
			}

			if (secure) {
				cookieText += ";secure";
			}

			document.cookie = cookieText;
		},
		setExt : function(name, value) {
			this.set(name, value, new Date().getTime() + 315360000000, "/");
		}
	};

	// 主体，其实就是tracker js
	var tracker = {
		// config
		clientConfig : {
			serverUrl : "http://node2/log.gif",
			sessionTimeout : 360, // 360s -> 6min
			maxWaitTime : 3600, // 3600s -> 60min -> 1h
			ver : "1"
		},

		cookieExpiresTime : 315360000000, // cookie过期时间，10年

		columns : {
			// 发送到服务器的列名称
			eventName : "en",
			version : "ver",
			platform : "pl",
			sdk : "sdk",
			uuid : "u_ud",
			memberId : "u_mid",
			sessionId : "u_sd",
			clientTime : "c_time",
			language : "l",
			userAgent : "b_iev",
			resolution : "b_rst",
			currentUrl : "p_url",
			referrerUrl : "p_ref",
			title : "tt",
			orderId : "oid",
			orderName : "on",
			currencyAmount : "cua",
			currencyType : "cut",
			paymentType : "pt",
			category : "ca",
			action : "ac",
			kv : "kv_",
			duration : "du"
		},

		keys : {
			pageView : "e_pv",
			chargeRequestEvent : "e_crt",
			launch : "e_l",
			eventDurationEvent : "e_e",
			sid : "bftrack_sid",
			uuid : "bftrack_uuid",
			mid : "bftrack_mid",
			preVisitTime : "bftrack_previsit",

		},

		/**
		 * 获取会话id
		 */
		getSid : function() {
			return CookieUtil.get(this.keys.sid);
		},

		/**
		 * 保存会话id到cookie
		 */
		setSid : function(sid) {
			if (sid) {
				CookieUtil.setExt(this.keys.sid, sid);
			}
		},

		/**
		 * 获取uuid，从cookie中
		 */
		getUuid : function() {
			return CookieUtil.get(this.keys.uuid);
		},

		/**
		 * 保存uuid到cookie
		 */
		setUuid : function(uuid) {
			if (uuid) {
				CookieUtil.setExt(this.keys.uuid, uuid);
			}
		},

		/**
		 * 获取memberID
		 */
		getMemberId : function() {
			return CookieUtil.get(this.keys.mid);
		},

		/**
		 * 设置mid
		 */
		setMemberId : function(mid) {
			if (mid) {
				CookieUtil.setExt(this.keys.mid, mid);
			}
		},

		startSession : function() {
			// 加载js就触发的方法
			if (this.getSid()) {
				// 会话id存在，表示uuid也存在
				if (this.isSessionTimeout()) {
					// 会话过期,产生新的会话
					this.createNewSession();
				} else {
					// 会话没有过期，更新最近访问时间
					this.updatePreVisitTime(new Date().getTime());
				}
			} else {
				// 会话id不存在，表示uuid也不存在
				this.createNewSession();
			}
			this.onPageView();
		},

		onLaunch : function() {
			// 触发launch事件
			var launch = {};
			launch[this.columns.eventName] = this.keys.launch; // 设置事件名称
			this.setCommonColumns(launch); // 设置公用columns
			this.sendDataToServer(this.parseParam(launch)); // 最终发送编码后的数据
		},

		onPageView : function() {
			// 触发page view事件
			if (this.preCallApi()) {
				var time = new Date().getTime();
				var pageviewEvent = {};
				pageviewEvent[this.columns.eventName] = this.keys.pageView;
				pageviewEvent[this.columns.currentUrl] = window.location.href; // 设置当前url
				pageviewEvent[this.columns.referrerUrl] = document.referrer; // 设置前一个页面的url
				pageviewEvent[this.columns.title] = document.title; // 设置title
				this.setCommonColumns(pageviewEvent); // 设置公用columns
				this.sendDataToServer(this.parseParam(pageviewEvent)); // 最终发送编码后的数据
				this.updatePreVisitTime(time);
			}
		},

		onChargeRequest : function(orderId, name, currencyAmount, currencyType, paymentType) {
			// 触发订单产生事件
			if (this.preCallApi()) {
				if (!orderId || !currencyType || !paymentType) {
					this.log("订单id、货币类型以及支付方式不能为空");
					return;
				}

				if (typeof (currencyAmount) == "number") {
					// 金额必须是数字
					var time = new Date().getTime();
					var chargeRequestEvent = {};
					chargeRequestEvent[this.columns.eventName] = this.keys.chargeRequestEvent;
					chargeRequestEvent[this.columns.orderId] = orderId;
					chargeRequestEvent[this.columns.orderName] = name;
					chargeRequestEvent[this.columns.currencyAmount] = currencyAmount;
					chargeRequestEvent[this.columns.currencyType] = currencyType;
					chargeRequestEvent[this.columns.paymentType] = paymentType;
					this.setCommonColumns(chargeRequestEvent); // 设置公用columns
					this.sendDataToServer(this.parseParam(chargeRequestEvent)); // 最终发送编码后的数据
					this.updatePreVisitTime(time);
				} else {
					this.log("订单金额必须是数字");
					return;
				}
			}
		},

		onEventDuration : function(category, action, map, duration) {
			// 触发event事件
			if (this.preCallApi()) {
				if (category && action) {
					var time = new Date().getTime();
					var event = {};
					event[this.columns.eventName] = this.keys.eventDurationEvent;
					event[this.columns.category] = category;
					event[this.columns.action] = action;
					if (map) {
						for ( var k in map) {
							if (k && map[k]) {
								event[this.columns.kv + k] = map[k];
							}
						}
					}
					if (duration) {
						event[this.columns.duration] = duration;
					}
					this.setCommonColumns(event); // 设置公用columns
					this.sendDataToServer(this.parseParam(event)); // 最终发送编码后的数据
					this.updatePreVisitTime(time);
				} else {
					this.log("category和action不能为空");
				}
			}
		},

		/**
		 * 执行对外方法前必须执行的方法
		 */
		preCallApi : function() {
			if (this.isSessionTimeout()) {
				// 如果为true，表示需要新建
				this.startSession();
			} else {
				this.updatePreVisitTime(new Date().getTime());
			}
			return true;
		},

		sendDataToServer : function(data) {
			
			alert(data);
			
			// 发送数据data到服务器，其中data是一个字符串
			var that = this;
			var i2 = new Image(1, 1);// <img src="url"></img>
			i2.onerror = function() {
				// 这里可以进行重试操作
			};
			i2.src = this.clientConfig.serverUrl + "?" + data;
		},

		/**
		 * 往data中添加发送到日志收集服务器的公用部分
		 */
		setCommonColumns : function(data) {
			data[this.columns.version] = this.clientConfig.ver;
			data[this.columns.platform] = "website";
			data[this.columns.sdk] = "js";
			data[this.columns.uuid] = this.getUuid(); // 设置用户id
			data[this.columns.memberId] = this.getMemberId(); // 设置会员id
			data[this.columns.sessionId] = this.getSid(); // 设置sid
			data[this.columns.clientTime] = new Date().getTime(); // 设置客户端时间
			data[this.columns.language] = window.navigator.language; // 设置浏览器语言
			data[this.columns.userAgent] = window.navigator.userAgent; // 设置浏览器类型
			data[this.columns.resolution] = screen.width + "*" + screen.height; // 设置浏览器分辨率
		},

		/**
		 * 创建新的会员，并判断是否是第一次访问页面，如果是，进行launch事件的发送。
		 */
		createNewSession : function() {
			var time = new Date().getTime(); // 获取当前操作时间
			// 1. 进行会话更新操作
			var sid = this.generateId(); // 产生一个session id
			this.setSid(sid);
			this.updatePreVisitTime(time); // 更新最近访问时间
			// 2. 进行uuid查看操作
			if (!this.getUuid()) {
				// uuid不存在，先创建uuid，然后保存到cookie，最后触发launch事件
				var uuid = this.generateId(); // 产品uuid
				this.setUuid(uuid);
				this.onLaunch();
			}
		},

		/**
		 * 参数编码返回字符串
		 */
		parseParam : function(data) {
			var params = "";
			for ( var e in data) {
				if (e && data[e]) {
					params += encodeURIComponent(e) + "="
							+ encodeURIComponent(data[e]) + "&";
				}
			}
			if (params) {
				return params.substring(0, params.length - 1);
			} else {
				return params;
			}
		},

		/**
		 * 产生uuid
		 */
		generateId : function() {
			var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
			var tmpid = [];
			var r;
			tmpid[8] = tmpid[13] = tmpid[18] = tmpid[23] = '-';
			tmpid[14] = '4';

			for (i = 0; i < 36; i++) {
				if (!tmpid[i]) {
					r = 0 | Math.random() * 16;
					tmpid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
				}
			}
			return tmpid.join('');
		},

		/**
		 * 判断这个会话是否过期，查看当前时间和最近访问时间间隔时间是否小于this.clientConfig.sessionTimeout<br/>
		 * 如果是小于，返回false;否则返回true。
		 */
		isSessionTimeout : function() {
			var time = new Date().getTime();
			var preTime = CookieUtil.get(this.keys.preVisitTime);
			if (preTime) {
				// 最近访问时间存在,那么进行区间判断
				return time - preTime > this.clientConfig.sessionTimeout * 1000;
			}
			return true;
		},

		/**
		 * 更新最近访问时间
		 */
		updatePreVisitTime : function(time) {
			CookieUtil.setExt(this.keys.preVisitTime, time);
		},

		/**
		 * 打印日志
		 */
		log : function(msg) {
			console.log(msg);
		},

	};

	// 对外暴露的方法名称
	window.__AE__ = {
		startSession : function() {
			tracker.startSession();
		},
		onPageView : function() {
			tracker.onPageView();
		},
		onChargeRequest : function(orderId, name, currencyAmount, currencyType, paymentType) {
			tracker.onChargeRequest(orderId, name, currencyAmount, currencyType, paymentType);
		},
		onEventDuration : function(category, action, map, duration) {
			tracker.onEventDuration(category, action, map, duration);
		},
		setMemberId : function(mid) {
			tracker.setMemberId(mid);
		}
	};

	// 自动加载方法
	var autoLoad = function() {
		// 进行参数设置
		var _aelog_ = _aelog_ || window._aelog_ || [];
		var memberId = null;
		for (i = 0; i < _aelog_.length; i++) {
			_aelog_[i][0] === "memberId" && (memberId = _aelog_[i][1]);
		}
		// 根据是给定memberid，设置memberid的值
		memberId && __AE__.setMemberId(memberId);
		// 启动session
		__AE__.startSession();
	};

	autoLoad();
})();