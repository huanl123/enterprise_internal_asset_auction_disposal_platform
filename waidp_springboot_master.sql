/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80100 (8.1.0)
 Source Host           : localhost:3306
 Source Schema         : waidp_springboot_master

 Target Server Type    : MySQL
 Target Server Version : 80100 (8.1.0)
 File Encoding         : 65001

 Date: 24/02/2026 20:29:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for asset
-- ----------------------------
DROP TABLE IF EXISTS `asset`;
CREATE TABLE `asset`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `specification` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `department_id` bigint NULL DEFAULT NULL,
  `purchase_date` date NOT NULL,
  `original_value` decimal(12, 2) NOT NULL,
  `current_value` decimal(12, 2) NOT NULL,
  `start_price` decimal(12, 2) NULL DEFAULT NULL,
  `depreciation_rule_id` bigint NULL DEFAULT NULL,
  `reserve_price` decimal(12, 2) NULL DEFAULT NULL,
  `has_reserve_price` bit(1) NULL DEFAULT b'0',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '待审核',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `FKnpe7x7oel7k7arlms8bb94rnx`(`department_id` ASC) USING BTREE,
  INDEX `FK25uffdu780j2g6ek8b1qxi5kw`(`depreciation_rule_id` ASC) USING BTREE,
  INDEX `idx_asset_status`(`status` ASC) USING BTREE,
  INDEX `idx_asset_name`(`name` ASC) USING BTREE,
  CONSTRAINT `asset_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK25uffdu780j2g6ek8b1qxi5kw` FOREIGN KEY (`depreciation_rule_id`) REFERENCES `depreciation_rule` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_asset_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of asset
-- ----------------------------
INSERT INTO `asset` VALUES (1, 'SRV-001', '服务器', 'IT设备', NULL, NULL, 1, '2025-01-01', 50000.00, 30000.00, 200.00, 1, NULL, b'0', '已处置', '2026-02-01 11:05:16', '2026-02-24 19:19:54');
INSERT INTO `asset` VALUES (2, 'PC-002', '办公电脑', '办公设备', NULL, NULL, 1, '2025-01-01', 8000.00, 4000.00, 100.00, 1, NULL, b'0', '待处置', '2026-02-01 11:05:16', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (3, 'PRN-003', '打印机', '办公设备', NULL, NULL, 2, '2025-01-01', 5000.00, 2000.00, 100.00, 1, NULL, b'0', '已处置', '2026-02-01 11:05:16', '2026-02-24 11:30:39');
INSERT INTO `asset` VALUES (4, 'FUR-004', '办公桌椅', '家具', NULL, NULL, 3, '2025-01-01', 2000.00, 1000.00, NULL, 2, NULL, b'0', '待审核', '2026-02-01 11:05:16', '2026-02-19 02:03:21');
INSERT INTO `asset` VALUES (5, 'PRO-005', '投影仪', 'IT设备', NULL, NULL, 4, '2025-01-01', 3000.00, 1500.00, NULL, 2, NULL, b'0', '已拒绝', '2026-02-01 11:05:16', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (6, 'ASSET-20260202-1328', '1', NULL, '1', '', 6, '2026-02-12', 1000.00, 978.04, 782.43, 1, NULL, b'0', '拍卖中', '2026-02-02 23:32:31', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (9, 'ASSET-20260202-7526', '1', NULL, '1', '/api/files/assets/9/da75d6b8-f13b-410d-892f-5fbab4072f98.jpg', 6, '2026-01-25', 1000.00, 1017.51, 814.01, 1, NULL, b'0', '拍卖中', '2026-02-02 23:43:43', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (10, 'ASSET-20260203-1077', '小米摄像头', NULL, '8.	小米（MI） 摄像头云台版2K1296P智能摄像机wifi监控器家用300万像素手机远程室内夜视 小米智能摄像机云台版2K 129', '/api/files/assets/10/8.avif', 6, '2026-02-02', 129.00, 129.28, 103.42, 1, NULL, b'0', '已处置', '2026-02-03 23:28:36', '2026-02-19 17:00:16');
INSERT INTO `asset` VALUES (11, 'ASSET-20260203-7963', '三星（SAMSUNG）Watch8 /Watch8Classic 蓝牙通话智能手表/运动手表/电话手表', NULL, '二手手表 Watch8Classic46mmBT 月陨黑准新', '/api/files/assets/11/2.jpg,/api/files/assets/11/2.1.jpg', 6, '2024-02-05', 1788.00, 1109.49, 887.59, 1, NULL, b'0', '拍卖中', '2026-02-03 23:55:53', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (12, 'ASSET-20260203-3746', '3海尔空调', NULL, '大1.5匹超一级能效冷暖变频挂机 双排铜管蒸发器 家电国家补贴以旧换新KFR-35GW/E1-1.', '/api/files/assets/12/3.avif,/api/files/assets/12/70af0e35-0363-4dbf-a4b4-cd10f19b0939.avif', 6, '2025-02-03', 2294.00, 2021.59, 1617.27, 1, NULL, b'0', '拍卖中', '2026-02-03 23:57:02', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (13, 'ASSET-20260203-5130', '华为Mate70Pro Mate60Rs', NULL, '非凡大师 超凡大师 二手华为手机 【70Rs保时捷非凡大师】皓白 99新 16+512G（电池效率100%+大礼包）', '/api/files/assets/13/c5283cdd-6717-4744-8c57-18ec15511c16.avif', 6, '2023-02-14', 6838.00, 2975.94, 2380.75, 1, NULL, b'0', '待拍卖', '2026-02-03 23:57:43', '2026-02-24 20:28:20');
INSERT INTO `asset` VALUES (14, 'ASSET-20260203-3871', '三星（SAMSUNG）', NULL, 'Samsung/三星980PRO990PROPM981APM9A1970EVO PLUS PM9C1 512G1T2T NVME m2 拆机二手固态硬盘（99成新） 三星980 PRO(4.0) 1T', '/api/files/assets/14/1.avif', 6, '2026-01-31', 939.00, 937.53, 750.02, 1, NULL, b'0', '已处置', '2026-02-03 23:58:25', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (15, 'ASSET-20260204-2484', 'Fujits扫描仪', NULL, '6130扫描机彩色a4连续快速扫描机自动批量文件票据高速双面再制造 富士通fi-6130（30张/分）', '/api/files/assets/15/9aa3f373-1c11-4d5b-ba8f-edbe7e38531b.avif', 6, '2022-02-14', 475.00, 250.83, 200.66, 1, NULL, b'0', '已拒绝', '2026-02-04 13:45:19', '2026-02-24 19:02:21');
INSERT INTO `asset` VALUES (16, 'ASSET-20260204-9420', '森海塞尔（Sennheiser）MOMENTUM 4 ', NULL, '特别设计版 蓝牙头戴耳机 曜金黑 ', '/api/files/assets/16/f967985a-9c33-42a0-b57e-677553dd69be.jpg', 6, '2022-02-08', 1387.00, 335.35, 268.28, 1, NULL, b'0', '拍卖中', '2026-02-04 13:50:11', '2026-02-19 16:59:22');
INSERT INTO `asset` VALUES (17, 'ASSET-20260204-2673', '8.	小米（MI） 摄像头云台版2K1296P', NULL, '家用300万像素手机远程室内夜视 小米智能摄像机云台版2K', '/api/files/assets/17/fb2a70fb-3bac-4432-bf5e-64f51c6c6ef4.jpg', 6, '2024-02-13', 129.00, 98.72, 78.98, 1, 98.72, b'1', '拍卖中', '2026-02-04 13:51:22', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (18, 'ASSET-20260204-2707', '萤石CB60小魔方摄像头灰色', NULL, '智能家居4G电池 家用监控 2600mAh可充锂电池 AOV全天录像 360°无极旋转  ', '/api/files/assets/18/0a69404f-c553-44b7-a9e0-4ea582a74281.avif', 6, '2025-02-11', 369.00, 326.07, 260.86, 1, 326.07, b'1', '拍卖中', '2026-02-04 13:52:19', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (19, 'ASSET-20260204-4019', '雅鹿新疆棉花被子', NULL, '加厚12斤冬被宿舍冬季被芯家用被褥盖被 蓝大格-新疆棉花填充 加厚保暖 150*200cm-可拆洗棉花被-约4斤', '/api/files/assets/19/93d4daa8-8974-43ed-bc0c-1a6ed684d949.avif', 6, '2025-12-31', 88.00, 87.01, 69.61, 1, 87.01, b'1', '拍卖中', '2026-02-04 13:53:27', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (20, 'ASSET-20260204-6650', '爱玛电动摩托车 ', NULL, '辉腾长续航大动力智能APP操控72V电池轻便高速电瓶车大牌极酷爱玛电动车电摩 极地灰', '/api/files/assets/20/8c97179b-0295-4327-bd03-87849e52d844.avif,/api/files/assets/20/74cfa34c-bbc4-4b9d-a3e6-5abbb2ed0f49.avif,/api/files/assets/20/70c0c970-00a3-4050-83e9-9f903ddf1447.avif', 6, '2025-12-17', 2749.00, 2714.24, 2171.39, 3, NULL, b'0', '拍卖中', '2026-02-04 13:55:46', '2026-02-19 02:03:21');
INSERT INTO `asset` VALUES (21, 'ASSET-20260204-8566', 'HIMEILI奔驰大g儿童电动车', NULL, '可坐大人遥控玩具车六轮越野亲子车小孩宝礼物 四驱黑+双油门+双皮座+可坐大人', '/api/files/assets/20/9f903ddf1447.avif,/api/files/assets/20/084c7b5e42c74362.jpg.avif,/api/files/assets/20/aad36919-a6fb-46ae-8f59-e3ec5a883b7b.avif,/api/files/assets/20/f8b830d54b93df33.png.avif', 6, '2025-02-11', 707.90, 642.02, 513.62, 1, NULL, b'0', '拍卖中', '2026-02-04 13:57:12', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (22, 'ASSET-20260204-8765', '华硕全家桶14代I5', NULL, '4400F/RTX5060/16G/1T/WiF6/畅玩三角洲游戏电脑主机组装电脑台式电脑整机', '/api/files/assets/22/5.avif,/api/files/assets/22/3.avif,/api/files/assets/22/4.avif,/api/files/assets/22/1.avif', 6, '2025-05-14', 6089.00, 5247.20, 4197.76, 1, NULL, b'0', '待拍卖', '2026-02-04 14:00:04', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (23, 'ASSET-20260205-3019', '电动车', NULL, '九成新', '/api/files/assets/23/ef10eec4-0a22-4ff9-ae2c-f7d09a1bce65.avif,/api/files/assets/23/f9b4fb1d-facb-4efc-ac09-306317ed99fc.avif', 1, '2026-02-03', 2595.00, 2593.80, 2075.04, 1, NULL, b'0', '已处置', '2026-02-05 18:35:58', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (24, 'ASSET-20260209-5082', 'AKKOMetaKey苹果iPhone17pro Max手机键盘背夹一体手机壳 ', NULL, '99成', '/api/files/assets/24/a3fb2ab8-dfe6-440a-bddb-b2a6426dd221.avif,/api/files/assets/24/849c7ec9-b34f-49d1-8a24-5f94ca2b7857.avif,/api/files/assets/24/1a9e829b-a130-4ec4-ba1e-d2c325436651.avif', NULL, '2025-02-03', 251.10, 220.83, 176.66, 1, NULL, b'0', '拍卖中', '2026-02-09 14:06:26', '2026-02-19 16:59:20');
INSERT INTO `asset` VALUES (25, 'ASSET-20260209-7995', 'vivo X Flip 5G折叠屏手机 vivoxflip 智能5G折叠屏 全网通 二手手机 菱紫【送3C超级快充】 12G+256G【建议购买碎屏险】', NULL, ' 99新', '/api/files/assets/25/fce59b0a-7c24-48e6-a553-def2a00b40c5.avif,/api/files/assets/25/4e6403eb-91b6-4c45-8bad-d4ccde8f9bcc.avif,/api/files/assets/25/0eb1c975-8224-4c7f-a9ef-16d877d129c6.png', NULL, '2025-10-15', 1655.00, 1554.56, 1243.65, 1, NULL, b'0', '待审核', '2026-02-09 14:09:04', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (26, 'ASSET-20260209-7375', '格力出品 晶弘空调 小凉神 1.5匹 新一级能效变频 纯铜管卧室省电挂机 国家补贴 KFR-35GW/JHFNhAa1Bj', NULL, '8成新', '/api/files/assets/26/10d7b604-e4f9-4f9d-b330-e2c2d8ac9f74.avif,/api/files/assets/26/bd9d6123-dd84-4922-9f8e-ae2814b767e4.avif,/api/files/assets/26/0d95518e-b320-4dfa-8c46-2ef76955a8e7.avif,/api/files/assets/26/7380d36e-8d07-4935-8c2f-67a205181884.avif', NULL, '2024-02-14', 2039.00, 1557.66, 1246.13, 1, NULL, b'0', '拍卖中', '2026-02-09 14:11:23', '2026-02-19 16:59:16');
INSERT INTO `asset` VALUES (27, 'ASSET-20260209-0371', '贝瑞佳（BeRica）阿斯顿马丁授权F1儿童电动车卡丁车遥控玩具汽车3-6岁生日礼物', NULL, '95成新', '/api/files/assets/27/896a4773-3e11-4436-997f-f8426a819460.avif,/api/files/assets/27/57e954fa-b511-405f-b961-930f28364583.avif,/api/files/assets/27/6e678ff1-ec1a-4116-b4d2-2991e7724f39.avif,/api/files/assets/27/8d206a8f-e91c-4ddd-94bc-67b7fe5e7bab.avif,/api/files/assets/27/d6d4b7e3-4040-46b7-94a2-8e61fa31d4d4.avif', NULL, '2025-11-12', 1091.55, 1066.38, 853.10, 1, NULL, b'0', '待审核', '2026-02-09 14:13:01', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (28, 'ASSET-20260209-1849', '爱玛（AIMA）【门店自提】卫士铅酸长续航大动力大电池外卖时尚成人高速电动摩托车 ', NULL, '九成新', '/api/files/assets/28/45ec755e-4622-4dfc-99da-9eaf7964ac94.avif,/api/files/assets/28/b78c331a-67b1-4c58-8a66-7c3f81419b24.avif', NULL, '2025-08-13', 2699.00, 2572.84, 2058.27, 1, NULL, b'0', '拍卖中', '2026-02-09 14:14:38', '2026-02-22 18:12:06');
INSERT INTO `asset` VALUES (29, 'ASSET-20260209-3291', '华硕（ASUS）台式电脑主机i5 14600KF/5060Ti/5070电竞游戏三角洲打瓦直播整机组装电脑ROG雪舞战姬海景房主机 i5 13400TEF丨RTX5060 8G丨五', NULL, '99成新', '/api/files/assets/29/5e9a6a9e-f2e3-45a0-95dd-363fcebb7950.avif,/api/files/assets/29/d1b54769-a368-4c0a-b02c-35802b86fbdd.avif,/api/files/assets/29/70eaa2ba-c376-4d76-a912-dbad60d45a36.avif,/api/files/assets/29/3c42bcb0-9f74-405a-92b1-9b5ab0dc8f3c.avif,/api/files/assets/29/96ea6618-3062-45d9-945b-10aa1e6b0f3d.avif', NULL, '2026-01-22', 5444.25, 5394.39, 4315.51, 1, 5394.39, b'1', '拍卖中', '2026-02-09 14:17:54', '2026-02-20 13:26:30');
INSERT INTO `asset` VALUES (30, 'ASSET-20260209-5408', '机械师（MACHENIKE）曙光S 国家补贴 高性能台式机游戏台式电脑主机（i7-13650HX 32G DDR5 1TSSD RTX5060）三角洲', NULL, '基本完好无损', '/api/files/assets/30/5e3f1551-1619-4e97-9311-50e11e94c225.avif,/api/files/assets/30/3d98c7d9-8b7f-405a-92be-80e52f0b05dd.avif,/api/files/assets/30/e24c4367-bbf8-4ef8-a0d5-19a7192ff662.avif', NULL, '2026-02-08', 7299.00, 7296.73, 5837.38, 1, 7296.73, b'1', '拍卖中', '2026-02-09 14:19:58', '2026-02-22 18:21:57');
INSERT INTO `asset` VALUES (31, 'ASSET-20260219-8055', '电动车', NULL, '9成新', '/api/files/assets/31/36e68c67-d05d-4d45-87f8-4c959d297e0f.avif', NULL, '2025-12-23', 2566.00, 2527.88, 2022.30, 3, NULL, b'0', '拍卖中', '2026-02-19 01:41:33', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (32, 'ASSET-20260219-6391', '玩具车', NULL, '九成新', '/api/files/assets/32/ab1e56e0-0479-47b0-9f9f-725abf38442f.avif', 3, '2025-12-16', 562.00, 552.63, 442.10, 3, NULL, b'0', '拍卖中', '2026-02-19 02:05:13', '2026-02-19 02:05:21');

-- ----------------------------
-- Table structure for asset_history
-- ----------------------------
DROP TABLE IF EXISTS `asset_history`;
CREATE TABLE `asset_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `asset_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `operation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `operator_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK322cepv7u4b2ch4r0g7ciehbj`(`asset_id` ASC) USING BTREE,
  INDEX `FKbqskcx1rmn52asexr4iue6xx5`(`operator_id` ASC) USING BTREE,
  INDEX `idx_asset_history_asset_time`(`asset_id` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `FK322cepv7u4b2ch4r0g7ciehbj` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKbqskcx1rmn52asexr4iue6xx5` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of asset_history
-- ----------------------------
INSERT INTO `asset_history` VALUES (1, 6, '资产创建', '2026-02-02 23:32:31.417550', '创建', NULL);
INSERT INTO `asset_history` VALUES (2, 9, '资产创建', '2026-02-02 23:43:43.012136', '创建', NULL);
INSERT INTO `asset_history` VALUES (3, 9, '审核通过，起拍价：814.01，未设置保留价', '2026-02-02 23:44:14.538356', '财务审核', NULL);
INSERT INTO `asset_history` VALUES (4, 6, '审核通过，起拍价：782.43，未设置保留价', '2026-02-02 23:54:15.050554', '财务审核', NULL);
INSERT INTO `asset_history` VALUES (5, 1, '审核通过，起拍价：200，未设置保留价，备注：1111', '2026-02-03 00:19:51.885206', '财务审核', 13);
INSERT INTO `asset_history` VALUES (6, 2, '审核通过，起拍价：100，未设置保留价', '2026-02-03 08:40:04.635445', '财务审核', 13);
INSERT INTO `asset_history` VALUES (7, 3, '审核通过，起拍价：100，未设置保留价', '2026-02-03 23:24:06.696552', '财务审核', 1);
INSERT INTO `asset_history` VALUES (8, 10, '资产创建', '2026-02-03 23:28:36.239257', '创建', NULL);
INSERT INTO `asset_history` VALUES (9, 10, '审核通过，起拍价：103.42，未设置保留价', '2026-02-03 23:32:59.037970', '财务审核', 13);
INSERT INTO `asset_history` VALUES (10, 11, '资产创建', '2026-02-03 23:55:52.942716', '创建', NULL);
INSERT INTO `asset_history` VALUES (11, 12, '资产创建', '2026-02-03 23:57:01.748533', '创建', NULL);
INSERT INTO `asset_history` VALUES (12, 13, '资产创建', '2026-02-03 23:57:42.963900', '创建', NULL);
INSERT INTO `asset_history` VALUES (13, 14, '资产创建', '2026-02-03 23:58:24.953973', '创建', NULL);
INSERT INTO `asset_history` VALUES (14, 14, '审核通过，起拍价：750.02，未设置保留价', '2026-02-04 13:39:20.574132', '财务审核', 13);
INSERT INTO `asset_history` VALUES (15, 13, '审核通过，起拍价：2380.75，未设置保留价', '2026-02-04 13:39:22.519818', '财务审核', 13);
INSERT INTO `asset_history` VALUES (16, 12, '审核通过，起拍价：1617.27，未设置保留价', '2026-02-04 13:39:24.414994', '财务审核', 13);
INSERT INTO `asset_history` VALUES (17, 11, '审核通过，起拍价：887.59，未设置保留价', '2026-02-04 13:39:30.984007', '财务审核', 13);
INSERT INTO `asset_history` VALUES (18, 15, '资产创建', '2026-02-04 13:45:18.698634', '创建', NULL);
INSERT INTO `asset_history` VALUES (19, 16, '资产创建', '2026-02-04 13:50:11.429974', '创建', NULL);
INSERT INTO `asset_history` VALUES (20, 17, '资产创建', '2026-02-04 13:51:22.494431', '创建', NULL);
INSERT INTO `asset_history` VALUES (21, 18, '资产创建', '2026-02-04 13:52:19.484073', '创建', NULL);
INSERT INTO `asset_history` VALUES (22, 19, '资产创建', '2026-02-04 13:53:27.079149', '创建', NULL);
INSERT INTO `asset_history` VALUES (23, 20, '资产创建', '2026-02-04 13:55:45.704277', '创建', NULL);
INSERT INTO `asset_history` VALUES (24, 21, '资产创建', '2026-02-04 13:57:11.948030', '创建', NULL);
INSERT INTO `asset_history` VALUES (25, 22, '资产创建', '2026-02-04 14:00:04.087678', '创建', NULL);
INSERT INTO `asset_history` VALUES (26, 22, '审核通过，起拍价：4197.76，未设置保留价', '2026-02-04 14:03:26.842846', '财务审核', 13);
INSERT INTO `asset_history` VALUES (27, 21, '审核通过，起拍价：513.62，未设置保留价', '2026-02-04 14:03:29.744390', '财务审核', 13);
INSERT INTO `asset_history` VALUES (28, 5, '资产审核拒绝，原因：没有图片参考', '2026-02-04 14:14:36.992812', '审核拒绝', 13);
INSERT INTO `asset_history` VALUES (29, 20, '审核通过，起拍价：2171.39，未设置保留价', '2026-02-04 17:16:10.178142', '财务审核', 13);
INSERT INTO `asset_history` VALUES (30, 19, '审核通过，起拍价：69.61，保留价：87.01', '2026-02-04 17:16:14.972066', '财务审核', 13);
INSERT INTO `asset_history` VALUES (31, 14, '交易单审核通过：TXN1770202406677', '2026-02-04 19:19:41.720003', '确认收款', 13);
INSERT INTO `asset_history` VALUES (32, 14, '确认处置完成，备注：111', '2026-02-04 19:31:28.681300', '确认处置', 11);
INSERT INTO `asset_history` VALUES (33, 2, '拍卖结束：流拍，拍卖名称：办公电脑，无人出价', '2026-02-05 17:13:51.741504', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (34, 12, '拍卖结束：流拍，拍卖名称：海尔空调，无人出价', '2026-02-05 17:13:51.786172', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (35, 22, '拍卖结束：流拍，拍卖名称：华硕全家桶，无人出价', '2026-02-05 17:13:51.790470', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (36, 23, '资产创建', '2026-02-05 18:35:58.327113', '创建', NULL);
INSERT INTO `asset_history` VALUES (37, 23, '审核通过，起拍价：2075.04，未设置保留价', '2026-02-05 18:36:30.632830', '财务审核', 13);
INSERT INTO `asset_history` VALUES (38, 23, '拍卖结束：成交，拍卖名称：电动车，中标者：庄采润，成交价：2175.04', '2026-02-05 18:55:08.057040', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (39, 18, '审核通过，起拍价：260.86，保留价：326.07', '2026-02-06 14:46:44.579851', '财务审核', 13);
INSERT INTO `asset_history` VALUES (40, 17, '审核通过，起拍价：78.98，保留价：98.72', '2026-02-06 14:46:49.492328', '财务审核', 13);
INSERT INTO `asset_history` VALUES (41, 11, '拍卖结束：成交，拍卖名称：三星手表，中标者：庄采润，成交价：1287.59', '2026-02-09 13:06:34.864794', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (42, 23, '交易单审核通过：TXN1770288908090', '2026-02-09 13:07:32.466281', '确认收款', 13);
INSERT INTO `asset_history` VALUES (43, 23, '确认处置完成，备注：123', '2026-02-09 13:50:18.327200', '确认处置', 11);
INSERT INTO `asset_history` VALUES (44, 24, '资产创建', '2026-02-09 14:06:25.936479', '创建', NULL);
INSERT INTO `asset_history` VALUES (45, 25, '资产创建', '2026-02-09 14:09:03.971927', '创建', NULL);
INSERT INTO `asset_history` VALUES (46, 26, '资产创建', '2026-02-09 14:11:22.823247', '创建', NULL);
INSERT INTO `asset_history` VALUES (47, 27, '资产创建', '2026-02-09 14:13:00.666837', '创建', NULL);
INSERT INTO `asset_history` VALUES (48, 28, '资产创建', '2026-02-09 14:14:38.176614', '创建', NULL);
INSERT INTO `asset_history` VALUES (49, 29, '资产创建', '2026-02-09 14:17:53.583569', '创建', NULL);
INSERT INTO `asset_history` VALUES (50, 30, '资产创建', '2026-02-09 14:19:58.303847', '创建', NULL);
INSERT INTO `asset_history` VALUES (51, 1, '拍卖结束：成交，拍卖名称：服务器拍卖，中标者：庄采润，成交价：25300.00', '2026-02-14 15:38:20.461732', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (52, 10, '交易单审核通过：TXN1770208312337', '2026-02-14 22:28:20.434345', '确认收款', 13);
INSERT INTO `asset_history` VALUES (55, 12, '资产信息更新', '2026-02-19 01:24:44.160114', '更新', NULL);
INSERT INTO `asset_history` VALUES (56, 12, '资产信息更新', '2026-02-19 01:24:54.409551', '更新', NULL);
INSERT INTO `asset_history` VALUES (57, 12, '资产信息更新', '2026-02-19 01:29:40.326429', '更新', NULL);
INSERT INTO `asset_history` VALUES (58, 12, '资产信息更新', '2026-02-19 01:30:01.738418', '更新', NULL);
INSERT INTO `asset_history` VALUES (59, 12, '资产信息更新', '2026-02-19 01:30:09.977104', '更新', NULL);
INSERT INTO `asset_history` VALUES (60, 12, '资产信息更新', '2026-02-19 01:33:40.812368', '更新', NULL);
INSERT INTO `asset_history` VALUES (61, 12, '资产信息更新', '2026-02-19 01:33:44.824716', '更新', NULL);
INSERT INTO `asset_history` VALUES (62, 12, '资产信息更新', '2026-02-19 01:34:31.136729', '更新', NULL);
INSERT INTO `asset_history` VALUES (63, 12, '资产信息更新', '2026-02-19 01:34:51.243825', '更新', NULL);
INSERT INTO `asset_history` VALUES (64, 12, '资产信息更新', '2026-02-19 01:35:01.569854', '更新', NULL);
INSERT INTO `asset_history` VALUES (65, 12, '资产信息更新', '2026-02-19 01:37:08.775981', '更新', NULL);
INSERT INTO `asset_history` VALUES (66, 12, '资产信息更新', '2026-02-19 01:37:22.393239', '更新', NULL);
INSERT INTO `asset_history` VALUES (67, 12, '资产信息更新', '2026-02-19 01:37:33.077963', '更新', NULL);
INSERT INTO `asset_history` VALUES (68, 31, '资产创建', '2026-02-19 01:41:33.266060', '创建', NULL);
INSERT INTO `asset_history` VALUES (69, 31, '资产价值重新计算', '2026-02-19 01:45:35.751140', '重新计算', NULL);
INSERT INTO `asset_history` VALUES (70, 31, '资产信息更新', '2026-02-19 01:45:47.143128', '更新', NULL);
INSERT INTO `asset_history` VALUES (71, 31, '资产信息更新', '2026-02-19 01:46:22.304576', '更新', NULL);
INSERT INTO `asset_history` VALUES (72, 31, '资产审核通过，状态变更为待拍卖', '2026-02-19 01:46:28.815837', '审核通过', 1);
INSERT INTO `asset_history` VALUES (73, 11, '中标者超时未确认，资产退回待拍卖', '2026-02-19 02:00:00.037435', '拍卖结果', 12);
INSERT INTO `asset_history` VALUES (74, 1, '中标者超时未确认，资产退回待拍卖', '2026-02-19 02:00:00.087637', '拍卖结果', 12);
INSERT INTO `asset_history` VALUES (75, 32, '资产创建', '2026-02-19 02:05:13.159362', '创建', NULL);
INSERT INTO `asset_history` VALUES (76, 32, '资产审核通过，状态变更为待拍卖', '2026-02-19 02:05:20.784378', '审核通过', 1);
INSERT INTO `asset_history` VALUES (77, 26, '审核通过，起拍价：1246.13，未设置保留价', '2026-02-19 16:59:16.460485', '财务审核', 13);
INSERT INTO `asset_history` VALUES (78, 24, '审核通过，起拍价：176.66，未设置保留价', '2026-02-19 16:59:20.307606', '财务审核', 13);
INSERT INTO `asset_history` VALUES (79, 16, '审核通过，起拍价：268.28，未设置保留价', '2026-02-19 16:59:22.408241', '财务审核', 13);
INSERT INTO `asset_history` VALUES (80, 10, '确认处置完成，备注：公司楼下交接', '2026-02-19 17:00:16.108499', '确认处置', 11);
INSERT INTO `asset_history` VALUES (81, 29, '审核通过，起拍价：4315.51，保留价：5394.39', '2026-02-20 13:26:29.729566', '财务审核', 13);
INSERT INTO `asset_history` VALUES (82, 30, '审核通过，起拍价：5837.38，保留价：7296.73', '2026-02-20 13:26:36.027893', '财务审核', 13);
INSERT INTO `asset_history` VALUES (83, 30, '拍卖结束：流拍，拍卖名称：机械师，未达保留价', '2026-02-22 16:30:43.041147', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (84, 30, '交易单审核通过：TXN1771749122712', '2026-02-22 16:34:08.745951', '确认收款', 13);
INSERT INTO `asset_history` VALUES (85, 30, '确认处置完成，备注：111', '2026-02-22 16:34:34.854263', '确认处置', 11);
INSERT INTO `asset_history` VALUES (86, 28, '审核通过，起拍价：2058.27，未设置保留价', '2026-02-22 18:12:06.066885', '财务审核', 1);
INSERT INTO `asset_history` VALUES (87, 1, '拍卖结束：成交，拍卖名称：服务器，中标者：甘雨，成交价：31700.00', '2026-02-23 20:15:31.043110', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (88, 2, '拍卖结束：成交，拍卖名称：办公电脑，中标者：荧，成交价：4100.00', '2026-02-23 20:15:31.092940', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (89, 3, '拍卖结束：成交，拍卖名称：打印机，中标者：千夏，成交价：2000.00', '2026-02-24 00:00:02.593698', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (90, 3, '交易单审核通过：TXN1771862402623', '2026-02-24 11:27:23.999700', '确认收款', 18);
INSERT INTO `asset_history` VALUES (91, 3, '确认处置完成，备注：111111', '2026-02-24 11:30:38.779933', '确认处置', 1);
INSERT INTO `asset_history` VALUES (92, 1, '交易单审核通过：TXN1771848931080', '2026-02-24 12:06:50.667498', '确认收款', 18);
INSERT INTO `asset_history` VALUES (93, 15, '资产审核拒绝，原因：111', '2026-02-24 19:02:21.391928', '审核拒绝', 1);
INSERT INTO `asset_history` VALUES (94, 1, '确认处置完成，备注：1111111', '2026-02-24 19:19:54.147523', '确认处置', 1);
INSERT INTO `asset_history` VALUES (95, 13, '资产信息更新', '2026-02-24 19:32:26.502683', '更新', NULL);
INSERT INTO `asset_history` VALUES (96, 13, '资产信息更新', '2026-02-24 20:06:57.107107', '更新', NULL);
INSERT INTO `asset_history` VALUES (97, 13, '资产信息更新', '2026-02-24 20:22:34.901564', '更新', NULL);
INSERT INTO `asset_history` VALUES (98, 13, '资产信息更新', '2026-02-24 20:22:50.267223', '更新', NULL);
INSERT INTO `asset_history` VALUES (99, 13, '资产信息更新', '2026-02-24 20:28:20.414618', '更新', NULL);

-- ----------------------------
-- Table structure for auction
-- ----------------------------
DROP TABLE IF EXISTS `auction`;
CREATE TABLE `auction`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `asset_id` bigint NULL DEFAULT NULL,
  `start_price` decimal(12, 2) NOT NULL,
  `current_price` decimal(12, 2) NULL DEFAULT NULL,
  `increment_amount` decimal(12, 2) NOT NULL,
  `has_reserve_price` bit(1) NULL DEFAULT b'0',
  `reserve_price` decimal(12, 2) NULL DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'not_started',
  `bid_count` int NULL DEFAULT 0,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `department_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `winner_id` bigint NULL DEFAULT NULL,
  `final_price` decimal(12, 2) NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK3tpdtvsqiatd477om91g61ib0`(`asset_id` ASC) USING BTREE,
  INDEX `FKd9v475fs8ytaltayvh2i3lmqm`(`winner_id` ASC) USING BTREE,
  INDEX `idx_auction_status_end_time`(`status` ASC, `end_time` ASC) USING BTREE,
  CONSTRAINT `FK3tpdtvsqiatd477om91g61ib0` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKd9v475fs8ytaltayvh2i3lmqm` FOREIGN KEY (`winner_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction
-- ----------------------------
INSERT INTO `auction` VALUES (1, '服务器拍卖', 'IT设备', 1, 25000.00, 25300.00, 100.00, b'1', 20000.00, '2026-02-02 10:00:00', '2026-02-10 10:00:00', 'ended', 2, '拍卖服务器', '1,2,3', 12, 25300.00, '2026-02-01 11:05:16', '2026-02-20 13:55:36');
INSERT INTO `auction` VALUES (3, 'test', NULL, 10, 103.42, 103.42, 100.00, b'0', NULL, '2026-02-03 15:34:06', '2026-02-03 16:05:00', 'ended', 0, '九成新', 'all', NULL, NULL, '2026-02-03 23:34:29', '2026-02-20 13:55:36');
INSERT INTO `auction` VALUES (4, '打印机', NULL, 3, 100.00, 100.00, 100.00, b'0', NULL, '2026-02-04 05:32:49', '2026-02-04 16:00:00', 'ended', 0, '实物以现场为准，竞买人应自行勘验确认。逾期付款或弃标视为违约，按【{违约处理}】处理。', 'all', NULL, NULL, '2026-02-04 13:36:20', '2026-02-04 16:00:57');
INSERT INTO `auction` VALUES (5, '办公电脑', NULL, 2, 100.00, 100.00, 100.00, b'1', 4000.00, '2026-02-04 06:00:33', '2026-02-05 16:00:00', 'ended', 0, '不可反悔', 'all', NULL, NULL, '2026-02-04 14:01:02', '2026-02-05 17:13:52');
INSERT INTO `auction` VALUES (6, '三星手表', NULL, 11, 887.59, 1287.59, 100.00, b'1', 1109.49, '2026-02-04 06:01:20', '2026-02-08 16:00:00', 'ended', 1, '不可反悔', 'all', 12, 1287.59, '2026-02-04 14:01:32', '2026-02-20 13:55:36');
INSERT INTO `auction` VALUES (7, '海尔空调', NULL, 12, 1617.27, 1617.27, 100.00, b'1', 2021.59, '2026-02-04 06:01:50', '2026-02-05 16:00:00', 'ended', 0, '111', 'all', NULL, NULL, '2026-02-04 14:02:04', '2026-02-05 17:13:52');
INSERT INTO `auction` VALUES (8, '华为手机', NULL, 13, 2380.75, 2380.75, 100.00, b'1', 2975.94, '2026-02-04 06:02:24', '2026-02-04 16:00:00', 'ended', 0, '价高者得', 'all', NULL, NULL, '2026-02-04 14:02:39', '2026-02-04 16:00:57');
INSERT INTO `auction` VALUES (12, '华硕全家桶', NULL, 22, 4197.76, 4197.76, 100.00, b'0', NULL, '2026-02-04 16:00:00', '2026-02-05 16:00:00', 'ended', 0, '价高者得', 'all', NULL, NULL, '2026-02-04 15:39:21', '2026-02-05 17:13:52');
INSERT INTO `auction` VALUES (13, '三星', NULL, 14, 750.02, 1050.02, 100.00, b'0', NULL, '2026-02-04 15:50:52', '2026-02-04 15:58:54', 'ended', 2, '价高者得', 'all', 12, 1050.02, '2026-02-04 15:51:07', '2026-02-20 13:55:36');
INSERT INTO `auction` VALUES (14, '电动车', NULL, 23, 2075.04, 2175.04, 100.00, b'0', NULL, '2026-02-05 18:42:58', '2026-02-05 18:55:02', 'ended', 1, '222', 'all', 12, 2175.04, '2026-02-05 18:43:12', '2026-02-05 18:55:08');
INSERT INTO `auction` VALUES (15, '机械师', NULL, 30, 5837.38, 5937.38, 100.00, b'1', 7296.73, '2026-02-20 13:28:42', '2026-02-21 06:00:09', 'ended', 1, '价高者得', 'all', 16, 5937.38, '2026-02-20 13:31:50', '2026-02-22 16:30:43');
INSERT INTO `auction` VALUES (16, '服务器', NULL, 1, 200.00, 31700.00, 100.00, b'1', 30000.00, '2026-02-22 18:18:25', '2026-02-23 00:00:00', 'ended', 20, '保留价30，000', 'all', 19, 31700.00, '2026-02-22 18:19:05', '2026-02-23 20:15:31');
INSERT INTO `auction` VALUES (17, '办公电脑', NULL, 2, 100.00, 4100.00, 100.00, b'1', 4000.00, '2026-02-22 18:35:29', '2026-02-23 18:35:48', 'ended', 3, '保留价金额：¥4,000', 'all', 18, 4100.00, '2026-02-22 18:36:32', '2026-02-23 20:15:31');
INSERT INTO `auction` VALUES (18, '打印机', NULL, 3, 100.00, 2000.00, 100.00, b'1', 2000.00, '2026-02-22 18:40:01', '2026-02-24 00:00:00', 'ended', 2, '保留价金额：2，000', 'all', 20, 2000.00, '2026-02-22 18:40:34', '2026-02-24 00:00:03');
INSERT INTO `auction` VALUES (19, '1', NULL, 6, 782.43, 1578.04, 100.00, b'1', 978.04, '2026-02-22 18:42:07', '2026-02-25 00:00:00', 'in_progress', 7, '保留价金额\n978.04', 'all', 19, 1578.04, '2026-02-22 18:42:22', '2026-02-24 12:40:48');
INSERT INTO `auction` VALUES (20, '三星手表', NULL, 11, 887.59, 1087.59, 100.00, b'0', NULL, '2026-02-24 12:34:50', '2026-02-25 00:00:00', 'in_progress', 2, '1111', 'all', 21, 1087.59, '2026-02-24 12:35:07', '2026-02-24 12:39:47');
INSERT INTO `auction` VALUES (21, '海尔空调', NULL, 12, 1617.27, 1717.27, 100.00, b'0', NULL, '2026-02-24 12:35:22', '2026-02-26 00:00:00', 'in_progress', 1, '2222', 'all', 19, 1717.27, '2026-02-24 12:35:43', '2026-02-24 12:38:41');
INSERT INTO `auction` VALUES (22, '玩具车', NULL, 32, 442.10, 642.10, 200.00, b'0', NULL, '2026-02-24 12:35:54', '2026-02-27 00:00:00', 'in_progress', 1, '2026.2.27结束', 'all', 19, 642.10, '2026-02-24 12:36:32', '2026-02-24 12:38:46');
INSERT INTO `auction` VALUES (23, '机械师', NULL, 30, 5837.38, 6137.38, 100.00, b'0', NULL, '2026-02-24 12:36:45', '2026-02-28 00:00:00', 'in_progress', 3, '3333', 'all', 21, 6137.38, '2026-02-24 12:37:04', '2026-02-24 12:39:51');
INSERT INTO `auction` VALUES (24, '电动车', NULL, 31, 2022.30, 2122.30, 100.00, b'0', NULL, '2026-02-24 12:37:25', '2026-03-29 00:00:00', 'in_progress', 1, '2026-03-29 00:00:00结束', 'all', 19, 2122.30, '2026-02-24 12:37:41', '2026-02-24 12:39:02');
INSERT INTO `auction` VALUES (30, '华硕台式主机', NULL, 29, 4315.51, 4415.51, 100.00, b'1', 5394.39, '2026-02-24 18:41:52', '2026-02-27 00:00:00', 'in_progress', 1, '中标后无故放弃、逾期未确认、逾期未付款或其他违约行为，平台将按规则进行处理，包括但不限于取消成交资格、恢复资产为待拍卖状态，并执行竞拍资格限制（如限制3个月内参与竞拍）。', 'all', 21, 4415.51, '2026-02-24 18:42:00', '2026-02-24 18:48:05');
INSERT INTO `auction` VALUES (31, '苹果17pro max手机壳', NULL, 24, 176.66, 276.66, 100.00, b'0', NULL, '2026-02-24 18:42:29', '2026-03-03 00:00:00', 'in_progress', 1, '中标后无故放弃、逾期未确认、逾期未付款或其他违约行为，平台将按规则进行处理，包括但不限于取消成交资格、恢复资产为待拍卖状态，并执行竞拍资格限制（如限制3个月内参与竞拍）。', 'all', 21, 276.66, '2026-02-24 18:42:41', '2026-02-24 18:50:06');
INSERT INTO `auction` VALUES (32, '格力空调', NULL, 26, 1246.13, 1346.13, 100.00, b'0', NULL, '2026-02-24 18:42:56', '2026-03-01 00:00:00', 'in_progress', 1, '中标后无故放弃、逾期未确认、逾期未付款或其他违约行为，平台将按规则进行处理，包括但不限于取消成交资格、恢复资产为待拍卖状态，并执行竞拍资格限制（如限制3个月内参与竞拍）。', 'all', 21, 1346.13, '2026-02-24 18:43:06', '2026-02-24 18:50:50');
INSERT INTO `auction` VALUES (33, '爱玛电动车', NULL, 28, 2058.27, 2158.27, 100.00, b'0', NULL, '2026-02-24 18:45:20', '2026-02-28 00:00:00', 'in_progress', 1, '中标后无故放弃、逾期未确认、逾期未付款或其他违约行为，平台将按规则进行处理，包括但不限于取消成交资格、恢复资产为待拍卖状态，并执行竞拍资格限制（如限制3个月内参与竞拍）。', 'all', 21, 2158.27, '2026-02-24 18:45:42', '2026-02-24 18:52:01');

-- ----------------------------
-- Table structure for bid
-- ----------------------------
DROP TABLE IF EXISTS `bid`;
CREATE TABLE `bid`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `auction_id` bigint NOT NULL,
  `bidder_id` bigint NOT NULL,
  `price` decimal(12, 2) NOT NULL,
  `bid_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `is_highest` bit(1) NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKhexc6i4j8i0tmpt8bdulp6g3g`(`auction_id` ASC) USING BTREE,
  INDEX `FKlv3wvxwx62go8g98owtwcbf7k`(`bidder_id` ASC) USING BTREE,
  CONSTRAINT `FKhexc6i4j8i0tmpt8bdulp6g3g` FOREIGN KEY (`auction_id`) REFERENCES `auction` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKlv3wvxwx62go8g98owtwcbf7k` FOREIGN KEY (`bidder_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bid
-- ----------------------------
INSERT INTO `bid` VALUES (1, 1, 12, 25100.00, '2026-02-03 00:13:16.041083', b'0', '2026-02-03 00:13:16.041083');
INSERT INTO `bid` VALUES (2, 1, 13, 25200.00, '2026-02-03 08:39:35.268095', b'0', '2026-02-03 08:39:35.268095');
INSERT INTO `bid` VALUES (3, 3, 13, 203.42, '2026-02-04 14:19:11.528552', b'1', '2026-02-04 14:19:11.528552');
INSERT INTO `bid` VALUES (4, 13, 12, 850.02, '2026-02-04 15:51:46.853358', b'1', '2026-02-04 15:51:46.853358');
INSERT INTO `bid` VALUES (5, 13, 13, 950.02, '2026-02-04 15:52:06.423814', b'1', '2026-02-04 15:52:06.423814');
INSERT INTO `bid` VALUES (6, 13, 12, 1050.02, '2026-02-04 15:52:27.926406', b'1', '2026-02-04 15:52:27.926406');
INSERT INTO `bid` VALUES (7, 14, 12, 2175.04, '2026-02-05 18:46:13.967446', b'1', '2026-02-05 18:46:13.967446');
INSERT INTO `bid` VALUES (8, 1, 12, 25300.00, '2026-02-06 13:16:03.988317', b'1', '2026-02-06 13:16:03.988317');
INSERT INTO `bid` VALUES (9, 6, 13, 1187.59, '2026-02-07 16:36:11.177837', b'0', '2026-02-07 16:36:11.177837');
INSERT INTO `bid` VALUES (10, 6, 12, 1287.59, '2026-02-07 16:36:30.699868', b'1', '2026-02-07 16:36:30.699868');
INSERT INTO `bid` VALUES (11, 15, 16, 5937.38, '2026-02-20 14:34:21.097764', b'1', '2026-02-20 14:34:21.097764');
INSERT INTO `bid` VALUES (12, 16, 19, 300.00, '2026-02-22 19:03:42.101528', b'0', '2026-02-22 19:03:42.101528');
INSERT INTO `bid` VALUES (13, 16, 21, 2000.00, '2026-02-22 19:10:34.348678', b'0', '2026-02-22 19:10:34.348678');
INSERT INTO `bid` VALUES (14, 17, 21, 200.00, '2026-02-22 19:11:07.445803', b'0', '2026-02-22 19:11:07.445803');
INSERT INTO `bid` VALUES (15, 19, 21, 978.04, '2026-02-22 19:11:22.331586', b'0', '2026-02-22 19:11:22.331586');
INSERT INTO `bid` VALUES (16, 18, 21, 1500.00, '2026-02-22 19:11:34.869644', b'0', '2026-02-22 19:11:34.869644');
INSERT INTO `bid` VALUES (17, 16, 20, 30000.00, '2026-02-22 19:13:48.707701', b'0', '2026-02-22 19:13:48.707701');
INSERT INTO `bid` VALUES (18, 17, 20, 4000.00, '2026-02-22 19:14:06.061247', b'0', '2026-02-22 19:14:06.061247');
INSERT INTO `bid` VALUES (19, 18, 20, 2000.00, '2026-02-22 19:14:35.820347', b'1', '2026-02-22 19:14:35.820347');
INSERT INTO `bid` VALUES (20, 17, 18, 4100.00, '2026-02-22 19:20:11.950062', b'1', '2026-02-22 19:20:11.950062');
INSERT INTO `bid` VALUES (21, 16, 19, 30100.00, '2026-02-22 19:22:53.466823', b'0', '2026-02-22 19:22:53.466823');
INSERT INTO `bid` VALUES (22, 16, 19, 30200.00, '2026-02-22 19:29:47.484126', b'0', '2026-02-22 19:29:47.484126');
INSERT INTO `bid` VALUES (23, 16, 19, 30300.00, '2026-02-22 19:29:51.047238', b'0', '2026-02-22 19:29:51.047238');
INSERT INTO `bid` VALUES (24, 16, 19, 30400.00, '2026-02-22 19:30:03.350535', b'0', '2026-02-22 19:30:03.350535');
INSERT INTO `bid` VALUES (25, 16, 19, 30500.00, '2026-02-22 19:30:10.104940', b'0', '2026-02-22 19:30:10.104940');
INSERT INTO `bid` VALUES (26, 16, 19, 30600.00, '2026-02-22 19:30:13.679555', b'0', '2026-02-22 19:30:13.679555');
INSERT INTO `bid` VALUES (27, 16, 19, 30700.00, '2026-02-22 19:30:17.609555', b'0', '2026-02-22 19:30:17.609555');
INSERT INTO `bid` VALUES (28, 16, 19, 30800.00, '2026-02-22 19:30:19.284904', b'0', '2026-02-22 19:30:19.284904');
INSERT INTO `bid` VALUES (29, 16, 19, 30900.00, '2026-02-22 19:30:20.941577', b'0', '2026-02-22 19:30:20.941577');
INSERT INTO `bid` VALUES (30, 16, 19, 31000.00, '2026-02-22 19:30:23.168306', b'0', '2026-02-22 19:30:23.168306');
INSERT INTO `bid` VALUES (31, 16, 19, 31100.00, '2026-02-22 19:30:24.816029', b'0', '2026-02-22 19:30:24.816029');
INSERT INTO `bid` VALUES (32, 16, 19, 31200.00, '2026-02-22 19:30:26.613969', b'0', '2026-02-22 19:30:26.613969');
INSERT INTO `bid` VALUES (33, 16, 19, 31300.00, '2026-02-22 19:30:31.997035', b'0', '2026-02-22 19:30:31.997035');
INSERT INTO `bid` VALUES (34, 16, 19, 31400.00, '2026-02-22 19:30:35.259307', b'0', '2026-02-22 19:30:35.259307');
INSERT INTO `bid` VALUES (35, 16, 19, 31500.00, '2026-02-22 19:30:37.304623', b'0', '2026-02-22 19:30:37.304623');
INSERT INTO `bid` VALUES (36, 16, 19, 31600.00, '2026-02-22 19:30:39.488841', b'0', '2026-02-22 19:30:39.488841');
INSERT INTO `bid` VALUES (37, 16, 19, 31700.00, '2026-02-22 19:30:41.141249', b'1', '2026-02-22 19:30:41.141249');
INSERT INTO `bid` VALUES (38, 19, 21, 1078.04, '2026-02-22 20:11:03.323087', b'0', '2026-02-22 20:11:03.323087');
INSERT INTO `bid` VALUES (39, 19, 21, 1178.04, '2026-02-22 20:11:05.258213', b'0', '2026-02-22 20:11:05.258213');
INSERT INTO `bid` VALUES (40, 19, 21, 1278.04, '2026-02-22 20:11:06.949635', b'0', '2026-02-22 20:11:06.949635');
INSERT INTO `bid` VALUES (41, 19, 19, 1378.04, '2026-02-24 12:38:27.945989', b'0', '2026-02-24 12:38:27.945989');
INSERT INTO `bid` VALUES (42, 20, 19, 987.59, '2026-02-24 12:38:35.899206', b'0', '2026-02-24 12:38:35.899206');
INSERT INTO `bid` VALUES (43, 21, 19, 1717.27, '2026-02-24 12:38:41.329365', b'1', '2026-02-24 12:38:41.329365');
INSERT INTO `bid` VALUES (44, 22, 19, 642.10, '2026-02-24 12:38:46.050259', b'1', '2026-02-24 12:38:46.050259');
INSERT INTO `bid` VALUES (45, 23, 19, 5937.38, '2026-02-24 12:38:51.808958', b'0', '2026-02-24 12:38:51.808958');
INSERT INTO `bid` VALUES (46, 23, 19, 6037.38, '2026-02-24 12:38:57.585031', b'0', '2026-02-24 12:38:57.585031');
INSERT INTO `bid` VALUES (47, 24, 19, 2122.30, '2026-02-24 12:39:02.249101', b'1', '2026-02-24 12:39:02.249101');
INSERT INTO `bid` VALUES (48, 19, 21, 1478.04, '2026-02-24 12:39:23.743658', b'0', '2026-02-24 12:39:23.743658');
INSERT INTO `bid` VALUES (49, 20, 21, 1087.59, '2026-02-24 12:39:46.626583', b'1', '2026-02-24 12:39:46.626583');
INSERT INTO `bid` VALUES (50, 23, 21, 6137.38, '2026-02-24 12:39:51.337045', b'1', '2026-02-24 12:39:51.337045');
INSERT INTO `bid` VALUES (51, 19, 19, 1578.04, '2026-02-24 12:40:47.516384', b'1', '2026-02-24 12:40:47.516384');
INSERT INTO `bid` VALUES (52, 30, 21, 4415.51, '2026-02-24 18:48:05.159492', b'1', '2026-02-24 18:48:05.159492');
INSERT INTO `bid` VALUES (53, 31, 21, 276.66, '2026-02-24 18:50:05.893891', b'1', '2026-02-24 18:50:05.893891');
INSERT INTO `bid` VALUES (54, 32, 21, 1346.13, '2026-02-24 18:50:49.654049', b'1', '2026-02-24 18:50:49.654049');
INSERT INTO `bid` VALUES (55, 33, 21, 2158.27, '2026-02-24 18:52:01.245010', b'1', '2026-02-24 18:52:01.245010');

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `leader_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES (1, '技术部', '2026-02-01 11:05:16', '2026-02-14 18:16:13', '', '庄采润', '17728840461');
INSERT INTO `department` VALUES (2, '财务部', '2026-02-01 11:05:16', '2026-02-14 17:52:24', '', '小雪', '17728840455');
INSERT INTO `department` VALUES (3, '行政部', '2026-02-01 11:05:16', '2026-02-24 19:08:21', '', '甘雨', '18815321902');
INSERT INTO `department` VALUES (4, '市场部', '2026-02-01 11:05:16', '2026-02-24 19:11:14', '', '徐翀', '14815321902');
INSERT INTO `department` VALUES (5, '人力资源部', '2026-02-01 11:05:16', '2026-02-24 19:08:27', '', '千夏', '16815321902');
INSERT INTO `department` VALUES (6, '资产管理部', '2026-02-01 11:05:16', '2026-02-14 17:52:47', '', 'cc', '17728840466');
INSERT INTO `department` VALUES (7, '总经办', '2026-02-01 11:05:16', '2026-02-24 19:11:03', '', '李艾', '17728840442');

-- ----------------------------
-- Table structure for depreciation_rule
-- ----------------------------
DROP TABLE IF EXISTS `depreciation_rule`;
CREATE TABLE `depreciation_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `asset_count` int NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `salvage_rate` double NOT NULL,
  `status` bit(1) NOT NULL DEFAULT b'1',
  `update_time` datetime(6) NULL DEFAULT NULL,
  `useful_life` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of depreciation_rule
-- ----------------------------
INSERT INTO `depreciation_rule` VALUES (1, 0, '2026-02-01 11:33:27.926727', '适用于电脑、打印机等电子设备', '电子设备折旧规则', 5, b'1', '2026-02-01 11:33:27.926727', 5);
INSERT INTO `depreciation_rule` VALUES (2, 0, '2026-02-01 11:33:27.929724', '适用于桌椅、柜子等办公设备', '办公设备折旧规则', 5, b'1', '2026-02-01 11:33:27.929724', 8);
INSERT INTO `depreciation_rule` VALUES (3, 0, '2026-02-01 11:33:27.931246', '适用于公司车辆', '车辆折旧规则', 5, b'1', '2026-02-20 13:28:20.447284', 10);

-- ----------------------------
-- Table structure for disposal
-- ----------------------------
DROP TABLE IF EXISTS `disposal`;
CREATE TABLE `disposal`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `asset_id` bigint NOT NULL,
  `method` enum('AUCTION','DIRECT_SALE','SCRAP') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `status` enum('PENDING','APPROVED','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'PENDING',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_disposal_asset`(`asset_id` ASC) USING BTREE,
  CONSTRAINT `disposal_ibfk_1` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_disposal_asset` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of disposal
-- ----------------------------
INSERT INTO `disposal` VALUES (1, 3, 'AUCTION', 'PENDING', '2026-02-01 11:05:16', '2026-02-01 11:05:16');
INSERT INTO `disposal` VALUES (2, 4, 'DIRECT_SALE', 'APPROVED', '2026-02-01 11:05:16', '2026-02-01 11:05:16');
INSERT INTO `disposal` VALUES (3, 5, 'SCRAP', 'COMPLETED', '2026-02-01 11:05:16', '2026-02-01 11:05:16');

-- ----------------------------
-- Table structure for transaction
-- ----------------------------
DROP TABLE IF EXISTS `transaction`;
CREATE TABLE `transaction`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `auction_id` bigint NOT NULL,
  `asset_id` bigint NULL DEFAULT NULL,
  `winner_id` bigint NOT NULL,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `final_price` decimal(12, 2) NOT NULL,
  `payment_deadline` datetime(6) NOT NULL,
  `confirm_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `confirm_time` datetime(6) NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `disposal_remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `disposal_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `disposal_time` datetime(6) NULL DEFAULT NULL,
  `disposal_voucher` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `payment_remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `payment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `payment_time` datetime(6) NULL DEFAULT NULL,
  `payment_voucher` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `confirm_deadline` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `FKpy2y5q95x5wuftj2654j5y7uw`(`auction_id` ASC) USING BTREE,
  INDEX `FK7q9n8y7m19311rq48q4mxq42x`(`asset_id` ASC) USING BTREE,
  INDEX `FKj7y41ujpq683x4y7l3x0x6x3k`(`winner_id` ASC) USING BTREE,
  INDEX `idx_tx_confirm_payment`(`confirm_status` ASC, `payment_status` ASC) USING BTREE,
  INDEX `idx_tx_disposal_status_time`(`disposal_status` ASC, `disposal_time` ASC) USING BTREE,
  CONSTRAINT `FK7q9n8y7m19311rq48q4mxq42x` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKj7y41ujpq683x4y7l3x0x6x3k` FOREIGN KEY (`winner_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKpy2y5q95x5wuftj2654j5y7uw` FOREIGN KEY (`auction_id`) REFERENCES `auction` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of transaction
-- ----------------------------
INSERT INTO `transaction` VALUES (1, 13, 14, 12, 'TXN1770202406677', 1050.02, '2026-02-06 18:53:26.675505', 'confirmed', '2026-02-04 18:53:26.675505', '2026-02-04 18:53:26.675505', '111', 'completed', '2026-02-04 19:31:28.630215', '/uploads/vouchers/disposal/20260204/4430a5e9-5739-45e0-8dd6-4e1728b06938.jpg', NULL, 'approved', '2026-02-04 19:19:41.709004', '/uploads/vouchers/payment/20260204/8f16663f-ad13-4067-afad-337132af8c40.jpg', '2026-02-04 19:31:28.630215', '2026-02-04 18:53:27');
INSERT INTO `transaction` VALUES (2, 3, 10, 13, 'TXN1770208312337', 203.42, '2026-02-06 20:31:52.336798', 'confirmed', '2026-02-04 20:31:52.336798', '2026-02-04 20:31:52.336798', '公司楼下交接', 'completed', '2026-02-19 17:00:16.104513', '/uploads/vouchers/disposal/20260219/979cd2a4-0eaa-44fb-aaab-591b3544915c.jpg', NULL, 'approved', '2026-02-14 22:28:20.425380', '/uploads/vouchers/payment/20260214/4bdac7bd-ad07-4d23-9083-eaefca4714df.jpg', '2026-02-19 17:00:16.104513', '2026-02-04 20:31:52');
INSERT INTO `transaction` VALUES (3, 14, 23, 12, 'TXN1770288908090', 2175.04, '2026-02-08 13:15:51.921035', 'confirmed', '2026-02-06 13:15:51.921035', '2026-02-05 18:55:08.090912', '123', 'completed', '2026-02-09 13:50:18.317885', '/uploads/vouchers/disposal/20260209/ebfc19bf-aace-4caa-b203-1f83bb1a882a.jpg', NULL, 'approved', '2026-02-09 13:07:32.463236', '/uploads/vouchers/payment/20260209/0f3f6eb4-df0b-49c8-8de5-8a32a802b49a.jpg', '2026-02-09 13:50:18.317885', '2026-02-06 18:55:08');
INSERT INTO `transaction` VALUES (4, 6, 11, 12, 'TXN1770613594904', 1287.59, '2026-02-11 13:06:34.904663', 'expired', NULL, '2026-02-09 13:06:34.904663', NULL, 'PENDING', NULL, NULL, NULL, 'pending', NULL, NULL, '2026-02-19 02:00:00.021267', '2026-02-10 13:06:35');
INSERT INTO `transaction` VALUES (5, 1, 1, 12, 'TXN1771054700504', 25300.00, '2026-02-16 15:38:20.504680', 'expired', NULL, '2026-02-14 15:38:20.504680', NULL, 'PENDING', NULL, NULL, NULL, 'pending', NULL, NULL, '2026-02-19 02:00:00.086403', '2026-02-15 15:38:21');
INSERT INTO `transaction` VALUES (6, 15, 30, 16, 'TXN1771749122712', 5937.38, '2026-02-24 16:32:02.709522', 'confirmed', '2026-02-22 16:32:02.709522', '2026-02-22 16:32:02.709522', '111', 'completed', '2026-02-22 16:34:34.851273', '/uploads/vouchers/disposal/20260222/9d87fb80-310f-4425-baf6-fe3570555de2.jpg', NULL, 'approved', '2026-02-22 16:34:08.741895', '/uploads/vouchers/payment/20260222/6b1a1236-b1ae-4861-9fec-bea1a4ae292a.jpg', '2026-02-22 16:34:34.851273', '2026-02-22 16:32:03');
INSERT INTO `transaction` VALUES (7, 16, 1, 19, 'TXN1771848931080', 31700.00, '2026-02-25 20:15:31.080983', 'confirmed', NULL, '2026-02-23 20:15:31.080983', '1111111', 'completed', '2026-02-24 19:19:54.142514', '/uploads/vouchers/disposal/20260224/f2c71bc7-a735-4623-b8fc-b9699ae05e61.jpg', NULL, 'approved', '2026-02-24 12:06:50.664495', '/uploads/vouchers/payment/20260224/8a4326cb-e31d-4fdf-9545-0c279bad310d.jpg', '2026-02-24 19:19:54.142514', '2026-02-24 20:15:31');
INSERT INTO `transaction` VALUES (8, 17, 2, 18, 'TXN1771848931093', 4100.00, '2026-02-25 20:15:31.093936', 'pending', NULL, '2026-02-23 20:15:31.093936', NULL, 'PENDING', NULL, NULL, NULL, 'pending', NULL, NULL, '2026-02-23 20:15:31.093936', '2026-02-24 20:15:31');
INSERT INTO `transaction` VALUES (9, 18, 3, 20, 'TXN1771862402623', 2000.00, '2026-02-26 11:25:13.093947', 'confirmed', '2026-02-24 11:25:13.093947', '2026-02-24 00:00:02.623598', '111111', 'completed', '2026-02-24 11:30:38.775933', '/uploads/vouchers/disposal/20260224/6d855bae-bef4-4885-952f-80574936695a.jpg', NULL, 'approved', '2026-02-24 11:27:23.997701', '/uploads/vouchers/payment/20260224/b7cfde72-d63e-48e0-916e-1ed37416af8a.jpg', '2026-02-24 11:30:38.775933', '2026-02-25 00:00:03');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `department_id` bigint NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'USER',
  `status` tinyint(1) NOT NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `bid_ban_until` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `FK_user_department`(`department_id` ASC) USING BTREE,
  CONSTRAINT `FK_user_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$w/gdqmz.YY1CbI72EzUBrONV3hIjjyKHRqY0q21uxLjYdT3sN5.cG', 'admin@example.com', '15815321902', '王阳明', '/uploads/avatars/20260214/9c0d2d25-c36d-4f74-9f48-2e7b55f829d3.png', 6, 'ADMIN', 1, '2026-02-01 11:05:16', '2026-02-14 19:17:10', NULL);
INSERT INTO `user` VALUES (11, '17728840466', '$2a$10$CGysH3nG5V5TPRjCK2EjlOJuPT7C41HtYRZ35d3CoobTe8P4miVKm', NULL, '17728840466', 'cc', '/uploads/avatars/20260220/4fc1610c-babd-4c1f-b616-450e2ded5cd0.png', 6, 'asset_specialist', 1, '2026-02-02 23:44:59', '2026-02-20 13:35:46', NULL);
INSERT INTO `user` VALUES (12, ' 17728840461', '$2a$10$hwwDNr4F3Q.NIjloZywAjOs8MMP6mxYieRg7r/aWpAoYraoP1DsTG', NULL, '17728840461', '庄采润', NULL, 1, 'NORMAL_USER', 1, '2026-02-02 23:55:39', '2026-02-20 14:19:26', '2026-05-19 02:00:00');
INSERT INTO `user` VALUES (13, '17728840455', '$2a$10$Rhzq0A5QifMwR9ZNyMb/8eFOeVisf8Bz2GSFLsF4CoWl9aU5ga0km', NULL, '17728840455', '小雪', '/uploads/avatars/20260214/dad23f62-87c2-4f53-93db-75b94a71798d.png', 2, 'finance_specialist', 1, '2026-02-03 00:19:08', '2026-02-22 16:33:41', NULL);
INSERT INTO `user` VALUES (16, '17728840499', '$2a$10$3BBBbIcmN0I/LMU29PLgUOXtU0b01.zJitDNeB/CF.LW18iPxPOYW', NULL, '17728840499', '王辉', '/uploads/avatars/20260220/7cbada8b-bd6e-4103-b1d3-86fb07ed5479.png', 2, 'NORMAL_USER', 1, '2026-02-20 13:48:22', '2026-02-20 13:48:55', NULL);
INSERT INTO `user` VALUES (17, '17728840555', '$2a$10$w6XHFuIDHtlBtmi8SkQnneB/3ncwdU/Cu1.38YQKLz8QslcGUPrZK', NULL, '17728840555', '旅行者', NULL, 6, 'asset_specialist', 1, '2026-02-22 18:30:46', '2026-02-22 18:30:46', NULL);
INSERT INTO `user` VALUES (18, '17728840666', '$2a$10$iriRjACF9sKll5wVkYIwQ.6T84X6Wn6135kfSGhVK0tfJ32FaEDj.', NULL, '17728840666', '荧', '/uploads/avatars/20260222/7a2b74bc-2e0f-4b3a-8b24-3a2e8cfd33a2.jpg', 2, 'finance_specialist', 1, '2026-02-22 18:32:40', '2026-02-22 19:17:54', NULL);
INSERT INTO `user` VALUES (19, '18851832622', '$2a$10$hb4yTDLO/DsZgh3MuH5ETeGsWkXMwFI7L5zXBZBCP8xSIODcKej2G', NULL, '18851832622', '甘雨', '/uploads/avatars/20260222/03325896-12bf-4b86-b4eb-b2caffe074e9.jpg', 3, 'employee', 1, '2026-02-22 19:02:40', '2026-02-22 19:03:29', NULL);
INSERT INTO `user` VALUES (20, '18851832633', '$2a$10$JkFzsS07i63S3YhqAAkYduwO/lRcJWNnibK1kvqRsb.xRuYJ7IcWK', NULL, '18851832633', '千夏', '/uploads/avatars/20260222/982764bd-97c7-46a6-ad52-edf3c9d5421e.png', 5, 'employee', 1, '2026-02-22 19:09:07', '2026-02-22 19:13:30', NULL);
INSERT INTO `user` VALUES (21, '18851832644', '$2a$10$2LLrOpNaTaY4oT6TE2oZs.lSZL9.HB1ha7aZrhqnpbqvUk/5abCKm', NULL, '18851832644', '星见雅', '/uploads/avatars/20260222/e2a78cf9-6b58-44b7-a132-20ec14871bd5.jpg', 1, 'employee', 1, '2026-02-22 19:09:47', '2026-02-22 19:12:04', NULL);
INSERT INTO `user` VALUES (22, '15815321903', '$2a$10$rowAhZiAb/WiEUg2hOO6DOO1XlfpJS96ExAco7IZUA6w/Rwyz7g56', NULL, '15815321903', '徐翀', NULL, 4, 'employee', 1, '2026-02-24 19:09:47', '2026-02-24 19:09:47', NULL);
INSERT INTO `user` VALUES (23, '15815321955', '$2a$10$QJ.781kk7YdrL.aSYSQMKe5NAvHfHK1clPBUofMUsby.7.LGK1O4O', NULL, '15815321955', '李艾', NULL, 7, 'employee', 1, '2026-02-24 19:10:48', '2026-02-24 19:10:48', NULL);

-- ----------------------------
-- View structure for v_disposal_archive
-- ----------------------------
DROP VIEW IF EXISTS `v_disposal_archive`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_disposal_archive` AS select `a`.`id` AS `asset_id`,`a`.`code` AS `asset_code`,`a`.`name` AS `asset_name`,`a`.`category` AS `asset_category`,`a`.`status` AS `asset_status`,`t`.`id` AS `transaction_id`,`t`.`code` AS `transaction_code`,`t`.`final_price` AS `final_price`,`t`.`confirm_status` AS `confirm_status`,`t`.`confirm_time` AS `confirm_time`,`t`.`payment_status` AS `payment_status`,`t`.`payment_time` AS `payment_time`,`t`.`payment_voucher` AS `payment_voucher`,`t`.`payment_remark` AS `payment_remark`,`t`.`disposal_status` AS `disposal_status`,`t`.`disposal_time` AS `disposal_time`,`t`.`disposal_voucher` AS `disposal_voucher`,`t`.`disposal_remark` AS `disposal_remark`,`u`.`id` AS `winner_id`,`u`.`real_name` AS `winner_name`,`u`.`phone` AS `winner_phone`,`au`.`id` AS `auction_id`,`au`.`name` AS `auction_name` from ((((`transaction` `t` join (select `transaction`.`asset_id` AS `asset_id`,max(`transaction`.`id`) AS `latest_tx_id` from `transaction` where (lower(trim(coalesce(`transaction`.`disposal_status`,''))) = 'completed') group by `transaction`.`asset_id`) `lt` on((`lt`.`latest_tx_id` = `t`.`id`))) join `asset` `a` on((`a`.`id` = `t`.`asset_id`))) left join `user` `u` on((`u`.`id` = `t`.`winner_id`))) left join `auction` `au` on((`au`.`id` = `t`.`auction_id`)));

SET FOREIGN_KEY_CHECKS = 1;
