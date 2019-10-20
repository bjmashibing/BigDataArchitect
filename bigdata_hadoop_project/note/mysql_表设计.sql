# Host: localhost  (Version: 5.5.40)
# Date: 2015-12-06 22:29:08
# Generator: MySQL-Front 5.3  (Build 4.120)

/*!40101 SET NAMES utf8 */;

#
# Structure for table "dimension_browser"
#

DROP TABLE IF EXISTS `dimension_browser`;
CREATE TABLE `dimension_browser` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `browser_name` varchar(45) NOT NULL DEFAULT '' COMMENT '浏览器名称',
  `browser_version` varchar(255) NOT NULL DEFAULT '' COMMENT '浏览器版本号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='浏览器维度信息表';

#
# Structure for table "dimension_currency_type"
#

DROP TABLE IF EXISTS `dimension_currency_type`;
CREATE TABLE `dimension_currency_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `currency_name` varchar(10) DEFAULT NULL COMMENT '货币名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='支付货币类型维度信息表';

#
# Structure for table "dimension_date"
#

DROP TABLE IF EXISTS `dimension_date`;
CREATE TABLE `dimension_date` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `year` int(11) DEFAULT NULL,
  `season` int(11) DEFAULT NULL,
  `month` int(11) DEFAULT NULL,
  `week` int(11) DEFAULT NULL,
  `day` int(11) DEFAULT NULL,
  `calendar` date DEFAULT NULL,
  `type` enum('year','season','month','week','day') DEFAULT NULL COMMENT '日期格式',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='时间维度信息表';

#
# Structure for table "dimension_event"
#

DROP TABLE IF EXISTS `dimension_event`;
CREATE TABLE `dimension_event` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `category` varchar(255) DEFAULT NULL COMMENT '事件种类category',
  `action` varchar(255) DEFAULT NULL COMMENT '事件action名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='事件维度信息表';

#
# Structure for table "dimension_inbound"
#

DROP TABLE IF EXISTS `dimension_inbound`;
CREATE TABLE `dimension_inbound` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_id` int(11) DEFAULT NULL COMMENT '父级外链id',
  `name` varchar(45) DEFAULT NULL COMMENT '外链名称',
  `url` varchar(255) DEFAULT NULL COMMENT '外链url',
  `type` int(11) DEFAULT NULL COMMENT '外链类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='外链源数据维度信息表';

#
# Structure for table "dimension_kpi"
#

DROP TABLE IF EXISTS `dimension_kpi`;
CREATE TABLE `dimension_kpi` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `kpi_name` varchar(45) DEFAULT NULL COMMENT 'kpi维度名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='kpi维度相关信息表';

#
# Structure for table "dimension_location"
#

DROP TABLE IF EXISTS `dimension_location`;
CREATE TABLE `dimension_location` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `country` varchar(45) DEFAULT NULL COMMENT '国家名称',
  `province` varchar(45) DEFAULT NULL COMMENT '省份名称',
  `city` varchar(45) DEFAULT NULL COMMENT '城市名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='地域信息维度表';

#
# Structure for table "dimension_os"
#

DROP TABLE IF EXISTS `dimension_os`;
CREATE TABLE `dimension_os` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `os_name` varchar(45) NOT NULL DEFAULT '' COMMENT '操作系统名称',
  `os_version` varchar(45) NOT NULL DEFAULT '' COMMENT '操作系统版本号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='操作系统信息维度表';

#
# Structure for table "dimension_payment_type"
#

DROP TABLE IF EXISTS `dimension_payment_type`;
CREATE TABLE `dimension_payment_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `payment_type` varchar(255) DEFAULT NULL COMMENT '支付方式名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='支付方式维度信息表';

#
# Structure for table "dimension_platform"
#

DROP TABLE IF EXISTS `dimension_platform`;
CREATE TABLE `dimension_platform` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `platform_name` varchar(45) DEFAULT NULL COMMENT '平台名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='平台维度信息表';

#
# Structure for table "event_info"
#

DROP TABLE IF EXISTS `event_info`;
CREATE TABLE `event_info` (
  `event_dimension_id` int(11) NOT NULL DEFAULT '0',
  `key` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `times` int(11) DEFAULT '0' COMMENT '触发次数',
  PRIMARY KEY (`event_dimension_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='描述event的属性信息，在本次项目中不会用到';

#
# Structure for table "order_info"
#

DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
  `order_id` varchar(50) NOT NULL DEFAULT '',
  `date_dimension_id` int(11) NOT NULL DEFAULT '0',
  `amount` int(11) NOT NULL DEFAULT '0' COMMENT '订单金额',
  `is_pay` int(1) DEFAULT '0' COMMENT '表示是否支付，0表示未支付，1表示支付',
  `is_refund` int(1) DEFAULT '0' COMMENT '表示是否退款，0表示未退款，1表示退款',
  PRIMARY KEY (`order_id`,`date_dimension_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='描述订单的相关信息，该table在本次项目中的主要目标就是为了去重数据';

#
# Structure for table "stats_device_browser"
#

DROP TABLE IF EXISTS `stats_device_browser`;
CREATE TABLE `stats_device_browser` (
  `date_dimension_id` int(11) NOT NULL,
  `platform_dimension_id` int(11) NOT NULL,
  `browser_dimension_id` int(11) NOT NULL DEFAULT '0',
  `active_users` int(11) DEFAULT '0' COMMENT '活跃用户数',
  `new_install_users` int(11) DEFAULT '0' COMMENT '新增用户数',
  `total_install_users` int(11) DEFAULT '0' COMMENT '总用户数',
  `sessions` int(11) DEFAULT '0' COMMENT '会话个数',
  `sessions_length` int(11) DEFAULT '0' COMMENT '会话长度',
  `total_members` int(11) unsigned DEFAULT '0' COMMENT '总会员数',
  `active_members` int(11) unsigned DEFAULT '0' COMMENT '活跃会员数',
  `new_members` int(11) unsigned DEFAULT '0' COMMENT '新增会员数',
  `pv` int(11) DEFAULT '0' COMMENT 'pv数',
  `created` date DEFAULT NULL,
  PRIMARY KEY (`platform_dimension_id`,`date_dimension_id`,`browser_dimension_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='统计浏览器相关分析数据的统计表';

#
# Structure for table "stats_device_location"
#

DROP TABLE IF EXISTS `stats_device_location`;
CREATE TABLE `stats_device_location` (
  `date_dimension_id` int(11) NOT NULL,
  `platform_dimension_id` int(11) NOT NULL,
  `location_dimension_id` int(11) NOT NULL DEFAULT '0',
  `active_users` int(11) DEFAULT '0' COMMENT '活跃用户数',
  `sessions` int(11) DEFAULT '0' COMMENT '会话个数',
  `bounce_sessions` int(11) DEFAULT '0' COMMENT '跳出会话个数',
  `created` date DEFAULT NULL,
  PRIMARY KEY (`platform_dimension_id`,`date_dimension_id`,`location_dimension_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='统计地域相关分析数据的统计表';

#
# Structure for table "stats_event"
#

DROP TABLE IF EXISTS `stats_event`;
CREATE TABLE `stats_event` (
  `platform_dimension_id` int(11) NOT NULL DEFAULT '0',
  `date_dimension_id` int(11) NOT NULL DEFAULT '0',
  `event_dimension_id` int(11) NOT NULL DEFAULT '0',
  `times` int(11) DEFAULT '0' COMMENT '触发次数',
  `created` date DEFAULT NULL,
  PRIMARY KEY (`platform_dimension_id`,`date_dimension_id`,`event_dimension_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='统计事件相关分析数据的统计表';

#
# Structure for table "stats_hourly"
#

DROP TABLE IF EXISTS `stats_hourly`;
CREATE TABLE `stats_hourly` (
  `platform_dimension_id` int(11) NOT NULL,
  `date_dimension_id` int(11) NOT NULL,
  `kpi_dimension_id` int(11) NOT NULL,
  `hour_00` int(11) DEFAULT '0',
  `hour_01` int(11) DEFAULT '0',
  `hour_02` int(11) DEFAULT '0',
  `hour_03` int(11) DEFAULT '0',
  `hour_04` int(11) DEFAULT '0',
  `hour_05` int(11) DEFAULT '0',
  `hour_06` int(11) DEFAULT '0',
  `hour_07` int(11) DEFAULT '0',
  `hour_08` int(11) DEFAULT '0',
  `hour_09` int(11) DEFAULT '0',
  `hour_10` int(11) DEFAULT '0',
  `hour_11` int(11) DEFAULT '0',
  `hour_12` int(11) DEFAULT '0',
  `hour_13` int(11) DEFAULT '0',
  `hour_14` int(11) DEFAULT '0',
  `hour_15` int(11) DEFAULT '0',
  `hour_16` int(11) DEFAULT '0',
  `hour_17` int(11) DEFAULT '0',
  `hour_18` int(11) DEFAULT '0',
  `hour_19` int(11) DEFAULT '0',
  `hour_20` int(11) DEFAULT '0',
  `hour_21` int(11) DEFAULT '0',
  `hour_22` int(11) DEFAULT '0',
  `hour_23` int(11) DEFAULT '0',
  `created` date DEFAULT NULL,
  PRIMARY KEY (`platform_dimension_id`,`date_dimension_id`,`kpi_dimension_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='按小时统计信息的统计表';

#
# Structure for table "stats_inbound"
#

DROP TABLE IF EXISTS `stats_inbound`;
CREATE TABLE `stats_inbound` (
  `platform_dimension_id` int(11) NOT NULL DEFAULT '0',
  `date_dimension_id` int(11) NOT NULL,
  `inbound_dimension_id` int(11) NOT NULL,
  `active_users` int(11) DEFAULT '0' COMMENT '活跃用户数',
  `sessions` int(11) DEFAULT '0' COMMENT '会话个数',
  `bounce_sessions` int(11) DEFAULT '0' COMMENT '跳出会话个数',
  `created` date DEFAULT NULL,
  PRIMARY KEY (`platform_dimension_id`,`date_dimension_id`,`inbound_dimension_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='统计外链信息的统计表';

#
# Structure for table "stats_order"
#

DROP TABLE IF EXISTS `stats_order`;
CREATE TABLE `stats_order` (
  `platform_dimension_id` int(11) NOT NULL DEFAULT '0',
  `date_dimension_id` int(11) NOT NULL DEFAULT '0',
  `currency_type_dimension_id` int(11) NOT NULL DEFAULT '0',
  `payment_type_dimension_id` int(11) NOT NULL DEFAULT '0',
  `orders` int(11) DEFAULT '0' COMMENT '订单个数',
  `success_orders` int(11) DEFAULT '0' COMMENT '成功支付的订单个数',
  `refund_orders` int(11) DEFAULT '0' COMMENT '退款订单个数',
  `order_amount` int(11) DEFAULT '0' COMMENT '订单金额',
  `revenue_amount` int(11) DEFAULT '0' COMMENT '收入金额，也就是成功支付过的金额',
  `refund_amount` int(11) DEFAULT '0' COMMENT '退款金额',
  `total_revenue_amount` int(11) DEFAULT '0' COMMENT '迄今为止，总的订单交易额',
  `total_refund_amount` int(11) DEFAULT '0' COMMENT '迄今为止，总的退款金额',
  `created` date DEFAULT NULL,
  PRIMARY KEY (`platform_dimension_id`,`date_dimension_id`,`currency_type_dimension_id`,`payment_type_dimension_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='统计订单信息的统计表';

#
# Structure for table "stats_user"
#

DROP TABLE IF EXISTS `stats_user`;
CREATE TABLE `stats_user` (
  `date_dimension_id` int(11) NOT NULL,
  `platform_dimension_id` int(11) NOT NULL,
  `active_users` int(11) DEFAULT '0' COMMENT '活跃用户数',
  `new_install_users` int(11) DEFAULT '0' COMMENT '新增用户数',
  `total_install_users` int(11) DEFAULT '0' COMMENT '总用户数',
  `sessions` int(11) DEFAULT '0' COMMENT '会话个数',
  `sessions_length` int(11) DEFAULT '0' COMMENT '会话长度',
  `total_members` int(11) unsigned DEFAULT '0' COMMENT '总会员数',
  `active_members` int(11) unsigned DEFAULT '0' COMMENT '活跃会员数',
  `new_members` int(11) unsigned DEFAULT '0' COMMENT '新增会员数',
  `created` date DEFAULT NULL,
  PRIMARY KEY (`platform_dimension_id`,`date_dimension_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='统计用户基本信息的统计表';

#
# Structure for table "stats_view_depth"
#

DROP TABLE IF EXISTS `stats_view_depth`;
CREATE TABLE `stats_view_depth` (
  `platform_dimension_id` bigint(20) NOT NULL DEFAULT '0',
  `date_dimension_id` bigint(20) NOT NULL DEFAULT '0',
  `kpi_dimension_id` bigint(20) NOT NULL DEFAULT '0',
  `pv1` bigint(20) DEFAULT NULL,
  `pv2` bigint(20) DEFAULT NULL,
  `pv3` bigint(20) DEFAULT NULL,
  `pv4` bigint(20) DEFAULT NULL,
  `pv5_10` bigint(20) DEFAULT NULL,
  `pv10_30` bigint(20) DEFAULT NULL,
  `pv30_60` bigint(20) DEFAULT NULL,
  `pv60_plus` bigint(20) DEFAULT NULL,
  `created` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`platform_dimension_id`,`date_dimension_id`,`kpi_dimension_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='统计用户浏览深度相关分析数据的统计表';
