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

 Date: 03/03/2026 11:20:52
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
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of asset
-- ----------------------------
INSERT INTO `asset` VALUES (10, 'ASSET-20260203-1077', '小米摄像头', NULL, '8.	小米（MI） 摄像头云台版2K1296P智能摄像机wifi监控器家用300万像素手机远程室内夜视 小米智能摄像机云台版2K 129', '/api/files/assets/10/8.avif', 6, '2026-02-02', 129.00, 129.28, 103.42, 1, NULL, b'0', '已处置', '2026-02-03 23:28:36', '2026-02-19 17:00:16');
INSERT INTO `asset` VALUES (11, 'ASSET-20260203-7963', '三星（SAMSUNG）Watch8 /Watch8Classic 蓝牙通话智能手表/运动手表/电话手表', NULL, '二手手表 Watch8Classic46mmBT 月陨黑准新', '/api/files/assets/11/2.jpg,/api/files/assets/11/2.1.jpg', 6, '2024-02-05', 1788.00, 1109.49, 887.59, 1, NULL, b'0', '拍卖中', '2026-02-03 23:55:53', '2026-02-28 11:16:22');
INSERT INTO `asset` VALUES (12, 'ASSET-20260203-3746', '3海尔空调', NULL, '大1.5匹超一级能效冷暖变频挂机 双排铜管蒸发器 家电国家补贴以旧换新KFR-35GW/E1-1.', '/api/files/assets/12/3.avif,/api/files/assets/12/70af0e35-0363-4dbf-a4b4-cd10f19b0939.avif', 6, '2025-02-03', 2294.00, 2021.59, 1617.27, 1, NULL, b'0', '已处置', '2026-02-03 23:57:02', '2026-03-03 10:59:52');
INSERT INTO `asset` VALUES (13, 'ASSET-20260203-5130', '华为Mate70Pro Mate60Rs', NULL, '非凡大师 超凡大师 二手华为手机 【70Rs保时捷非凡大师】皓白 99新 16+512G（电池效率100%+大礼包）', '/api/files/assets/13/c5283cdd-6717-4744-8c57-18ec15511c16.avif', 6, '2023-02-14', 6838.00, 2975.94, 2380.75, 1, NULL, b'0', '待拍卖', '2026-02-03 23:57:43', '2026-02-24 20:28:20');
INSERT INTO `asset` VALUES (14, 'ASSET-20260203-3871', '三星（SAMSUNG）', NULL, 'Samsung/三星980PRO990PROPM981APM9A1970EVO PLUS PM9C1 512G1T2T NVME m2 拆机二手固态硬盘（99成新） 三星980 PRO(4.0) 1T', '/api/files/assets/14/1.avif', 6, '2026-01-31', 939.00, 937.53, 750.02, 1, NULL, b'0', '已处置', '2026-02-03 23:58:25', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (16, 'ASSET-20260204-9420', '森海塞尔（Sennheiser）MOMENTUM 4 ', NULL, '特别设计版 蓝牙头戴耳机 曜金黑 ', '/api/files/assets/16/f967985a-9c33-42a0-b57e-677553dd69be.jpg', 6, '2022-02-08', 1387.00, 335.35, 268.28, 1, NULL, b'0', '拍卖中', '2026-02-04 13:50:11', '2026-02-19 16:59:22');
INSERT INTO `asset` VALUES (22, 'ASSET-20260204-8765', '华硕全家桶14代I5', NULL, '4400F/RTX5060/16G/1T/WiF6/畅玩三角洲游戏电脑主机组装电脑台式电脑整机', '/api/files/assets/22/5.avif,/api/files/assets/22/3.avif,/api/files/assets/22/4.avif,/api/files/assets/22/1.avif', 6, '2025-05-14', 6089.00, 5247.20, 4197.76, 1, NULL, b'0', '拍卖中', '2026-02-04 14:00:04', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (23, 'ASSET-20260205-3019', '电动车', NULL, '九成新', '/api/files/assets/23/ef10eec4-0a22-4ff9-ae2c-f7d09a1bce65.avif,/api/files/assets/23/f9b4fb1d-facb-4efc-ac09-306317ed99fc.avif', 1, '2026-02-03', 2595.00, 2593.80, 2075.04, 1, NULL, b'0', '已处置', '2026-02-05 18:35:58', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (24, 'ASSET-20260209-5082', 'AKKOMetaKey苹果iPhone17pro Max手机键盘背夹一体手机壳 ', NULL, '99成', '/api/files/assets/24/a3fb2ab8-dfe6-440a-bddb-b2a6426dd221.avif,/api/files/assets/24/849c7ec9-b34f-49d1-8a24-5f94ca2b7857.avif,/api/files/assets/24/1a9e829b-a130-4ec4-ba1e-d2c325436651.avif', NULL, '2025-02-03', 251.10, 220.83, 176.66, 1, NULL, b'0', '已处置', '2026-02-09 14:06:26', '2026-03-03 10:59:05');
INSERT INTO `asset` VALUES (25, 'ASSET-20260209-7995', 'vivo X Flip 5G折叠屏手机 vivoxflip 智能5G折叠屏 全网通 二手手机 菱紫【送3C超级快充】 12G+256G【建议购买碎屏险】', NULL, ' 99新', '/api/files/assets/25/fce59b0a-7c24-48e6-a553-def2a00b40c5.avif,/api/files/assets/25/4e6403eb-91b6-4c45-8bad-d4ccde8f9bcc.avif,/api/files/assets/25/0eb1c975-8224-4c7f-a9ef-16d877d129c6.png', NULL, '2025-10-15', 1655.00, 1554.56, 1243.65, 1, NULL, b'0', '待拍卖', '2026-02-09 14:09:04', '2026-02-28 17:01:40');
INSERT INTO `asset` VALUES (26, 'ASSET-20260209-7375', '格力出品 晶弘空调 小凉神 1.5匹 新一级能效变频 纯铜管卧室省电挂机 国家补贴 KFR-35GW/JHFNhAa1Bj', NULL, '8成新', '/api/files/assets/26/10d7b604-e4f9-4f9d-b330-e2c2d8ac9f74.avif,/api/files/assets/26/bd9d6123-dd84-4922-9f8e-ae2814b767e4.avif,/api/files/assets/26/0d95518e-b320-4dfa-8c46-2ef76955a8e7.avif,/api/files/assets/26/7380d36e-8d07-4935-8c2f-67a205181884.avif', NULL, '2024-02-14', 2039.00, 1557.66, 1246.13, 1, NULL, b'0', '待处置', '2026-02-09 14:11:23', '2026-02-19 16:59:16');
INSERT INTO `asset` VALUES (27, 'ASSET-20260209-0371', '贝瑞佳（BeRica）阿斯顿马丁授权F1儿童电动车卡丁车遥控玩具汽车3-6岁生日礼物', NULL, '95成新', '/api/files/assets/27/896a4773-3e11-4436-997f-f8426a819460.avif,/api/files/assets/27/57e954fa-b511-405f-b961-930f28364583.avif,/api/files/assets/27/6e678ff1-ec1a-4116-b4d2-2991e7724f39.avif,/api/files/assets/27/8d206a8f-e91c-4ddd-94bc-67b7fe5e7bab.avif,/api/files/assets/27/d6d4b7e3-4040-46b7-94a2-8e61fa31d4d4.avif', NULL, '2025-11-12', 1091.55, 1066.38, 853.10, 1, NULL, b'0', '待拍卖', '2026-02-09 14:13:01', '2026-02-25 12:35:28');
INSERT INTO `asset` VALUES (28, 'ASSET-20260209-1849', '爱玛（AIMA）【门店自提】卫士铅酸长续航大动力大电池外卖时尚成人高速电动摩托车 ', NULL, '九成新', '/api/files/assets/28/45ec755e-4622-4dfc-99da-9eaf7964ac94.avif,/api/files/assets/28/b78c331a-67b1-4c58-8a66-7c3f81419b24.avif', NULL, '2025-08-13', 2699.00, 2572.84, 2058.27, 1, NULL, b'0', '已处置', '2026-02-09 14:14:38', '2026-03-03 10:59:45');
INSERT INTO `asset` VALUES (29, 'ASSET-20260209-3291', '华硕（ASUS）台式电脑主机i5 14600KF/5060Ti/5070电竞游戏三角洲打瓦直播整机组装电脑ROG雪舞战姬海景房主机 i5 13400TEF丨RTX5060 8G丨五', NULL, '99成新', '/api/files/assets/29/5e9a6a9e-f2e3-45a0-95dd-363fcebb7950.avif,/api/files/assets/29/d1b54769-a368-4c0a-b02c-35802b86fbdd.avif,/api/files/assets/29/70eaa2ba-c376-4d76-a912-dbad60d45a36.avif,/api/files/assets/29/3c42bcb0-9f74-405a-92b1-9b5ab0dc8f3c.avif,/api/files/assets/29/96ea6618-3062-45d9-945b-10aa1e6b0f3d.avif', NULL, '2026-01-22', 5444.25, 5394.39, 4315.51, 1, 5394.39, b'1', '待拍卖', '2026-02-09 14:17:54', '2026-02-20 13:26:30');
INSERT INTO `asset` VALUES (30, 'ASSET-20260209-5408', '机械师（MACHENIKE）曙光S 国家补贴 高性能台式机游戏台式电脑主机（i7-13650HX 32G DDR5 1TSSD RTX5060）三角洲', NULL, '基本完好无损', '/api/files/assets/30/5e3f1551-1619-4e97-9311-50e11e94c225.avif,/api/files/assets/30/3d98c7d9-8b7f-405a-92be-80e52f0b05dd.avif,/api/files/assets/30/e24c4367-bbf8-4ef8-a0d5-19a7192ff662.avif', NULL, '2026-02-08', 7299.00, 7296.73, 5837.38, 1, 7296.73, b'1', '待拍卖', '2026-02-09 14:19:58', '2026-03-03 10:00:16');
INSERT INTO `asset` VALUES (31, 'ASSET-20260219-8055', '电动车', NULL, '9成新', '/api/files/assets/31/36e68c67-d05d-4d45-87f8-4c959d297e0f.avif', NULL, '2025-12-23', 2566.00, 2527.88, 2022.30, 3, NULL, b'0', '拍卖中', '2026-02-19 01:41:33', '2026-02-19 02:04:27');
INSERT INTO `asset` VALUES (32, 'ASSET-20260219-6391', '玩具车', NULL, '九成新', '/api/files/assets/32/ab1e56e0-0479-47b0-9f9f-725abf38442f.avif', 3, '2025-12-16', 562.00, 552.63, 442.10, 3, NULL, b'0', '已处置', '2026-02-19 02:05:13', '2026-02-28 01:23:06');
INSERT INTO `asset` VALUES (33, 'ASSET-20260227-6560', '电动车', NULL, '99成新', '/api/files/assets/33/888cdd47-1b14-49f7-977e-97ed36c8cdb5.avif,/api/files/assets/33/2ca4c895-22d6-4ea2-9b8c-b7a16abd2e26.avif,/api/files/assets/33/e10f8d48-902b-4af0-ba8f-596a034c5130.avif', 1, '2026-02-09', 1999.00, 1989.64, 1591.80, 3, NULL, b'0', '待拍卖', '2026-02-27 18:30:12', '2026-02-27 18:30:50');
INSERT INTO `asset` VALUES (34, 'ASSET-20260227-5610', '电动车', NULL, '8成新', '/api/files/assets/34/3e50f206-7aa2-487b-b7ca-e15efb8b6f30.avif,/api/files/assets/34/26ff7f6b-f5ae-4e9e-ac42-537ba8dcebd0.avif,/api/files/assets/34/a13f29a9-3626-400a-9ba4-8c8b40440d1c.avif', 1, '2024-02-12', 1955.00, 1575.52, 1260.42, 3, NULL, b'0', '待拍卖', '2026-02-27 18:52:55', '2026-02-27 18:53:17');
INSERT INTO `asset` VALUES (35, 'ASSET-20260228-5009', '玩具車', NULL, '九成新', '/api/files/assets/35/564d6581-3186-48f9-a0e9-588995cba243.avif,/api/files/assets/35/6a163cfa-6e04-4383-b876-8c747fbb28a1.avif', 1, '2025-02-09', 1922.00, 1730.04, 1384.30, 3, NULL, b'0', '待拍卖', '2026-02-28 01:20:12', '2026-02-28 01:20:51');
INSERT INTO `asset` VALUES (36, 'ASSET-20260228-0517', '玩具车11', NULL, '九成新', '/api/files/assets/36/c7a2a7ee-7961-4ae3-adc9-9584648fe036.avif,/api/files/assets/36/474020f5-5663-40b8-a847-164cf8f8eca4.avif', 3, '2025-02-17', 1922.00, 1734.19, 1387.35, 3, NULL, b'0', '已处置', '2026-02-28 10:23:44', '2026-03-03 10:59:21');
INSERT INTO `asset` VALUES (37, 'ASSET-20260228-0751', '电动车九成新', NULL, '九成新', '/api/files/assets/37/582e060a-989c-4c40-9186-6a17d6d037aa.avif,/api/files/assets/37/40773674-ba19-4253-b604-181b5ada42a6.avif', 3, '2025-03-12', 1888.00, 1714.80, 1371.84, 3, NULL, b'0', '已处置', '2026-02-28 11:20:07', '2026-03-03 10:59:11');
INSERT INTO `asset` VALUES (38, 'ASSET-20260303-5069', '科沃斯扫地机器人X11PRO扫拖洗烘一体自动清洗滚筒洗地机器人智能黑银水箱版', NULL, '九成新', '/api/files/assets/38/3e643b59-96cf-4350-9358-6f5e611c21b9.avif,/api/files/assets/38/eba1e52d-a9ec-4583-bb59-f5c3a6f62fa9.avif,/api/files/assets/38/61306aff-db5b-4e5a-a979-12ef5f1be803.avif', 3, '2026-01-12', 5881.00, 5729.60, 4583.68, 1, 5729.60, b'1', '拍卖中', '2026-03-03 10:56:52', '2026-03-03 10:58:07');
INSERT INTO `asset` VALUES (39, 'ASSET-20260303-7063', '90分行李箱20英寸莱茵河经典款 拉杆箱登机旅行箱万向轮密码箱钛金灰', NULL, '九成新', '/api/files/assets/39/b65da0ca-38d3-4c11-8b90-3082a197e623.avif,/api/files/assets/39/5922f99e-e2ee-4647-a3d0-c63493f9fd32.avif,/api/files/assets/39/bb4e4a4d-8d74-4103-b08d-81f534712d8b.avif', 2, '2025-03-18', 215.00, 190.56, 152.45, 2, NULL, b'0', '待审核', '2026-03-03 11:02:25', '2026-03-03 11:02:25');
INSERT INTO `asset` VALUES (40, 'ASSET-20260303-7303', '大艺手电钻打孔家用无刷16V电动螺丝刀工具套装', NULL, '大功率手电钻3305SEA 8成新', '/api/files/assets/40/65853224-6029-42b7-b773-68d4475eeee6.avif,/api/files/assets/40/e6d414a7-032f-4712-9731-5d2b6110e4ff.avif,/api/files/assets/40/00a017b9-9862-4782-9c59-65c05106ed9e.avif', 7, '2025-08-12', 219.05, 204.62, 163.70, 2, NULL, b'0', '待审核', '2026-03-03 11:04:13', '2026-03-03 11:04:13');
INSERT INTO `asset` VALUES (41, 'ASSET-20260303-4268', '四轮代步车电动汽车成人油电两用全封闭新能源', NULL, '节能款(60V55A电池) 8成新', '/api/files/assets/41/bf407f43-90d8-4cb0-a7d5-ec608a319a0f.avif,/api/files/assets/41/37d55504-0516-46e0-b605-5b0009620e84.avif,/api/files/assets/41/dc6298a9-113a-4cd1-87d9-79a48c4042c1.avif,/api/files/assets/41/cb6fbc3b-c94b-41c5-bb32-4c3875cc7c8d.avif', 5, '2025-03-18', 22906.91, 20823.39, 16658.71, 3, NULL, b'0', '待审核', '2026-03-03 11:06:09', '2026-03-03 11:06:10');
INSERT INTO `asset` VALUES (42, 'ASSET-20260303-8372', '乐威普奔驰授权儿童电动车', NULL, '95成新', '/api/files/assets/42/58b045f9-d2a9-4768-8193-96c90b4bcd0e.avif,/api/files/assets/42/a5407881-652c-49af-93da-e8d17f2d58ea.avif', 3, '2026-01-22', 496.80, 491.70, 393.36, 3, NULL, b'0', '待审核', '2026-03-03 11:07:09', '2026-03-03 11:07:10');
INSERT INTO `asset` VALUES (43, 'ASSET-20260303-0507', 'AGVPISTA机车头盔冰蓝', NULL, '全盔摩托车碳纤维四季防摔变色龙蝴蝶结 AGV Pista【蝴蝶结】 M', '/api/files/assets/43/6c6a7988-baf9-4c4a-9d77-cf9b931eb481.avif', 4, '2025-11-12', 12439.00, 11991.96, 9593.57, 2, NULL, b'0', '待审核', '2026-03-03 11:08:27', '2026-03-03 11:08:28');
INSERT INTO `asset` VALUES (44, 'ASSET-20260303-2425', '暴风骑士青少年电动摩托车', NULL, '9成新', '/api/files/assets/44/14759a65-1542-4b1a-ac4f-d4f3c54c0f13.avif,/api/files/assets/44/751aa797-3b7a-4fff-8470-d7282306c778.avif,/api/files/assets/44/f75da119-2531-4f08-b436-053b3c2da601.avif', 5, '2025-09-24', 2899.00, 2778.68, 2222.94, 3, NULL, b'0', '待审核', '2026-03-03 11:09:53', '2026-03-03 11:09:54');
INSERT INTO `asset` VALUES (45, 'ASSET-20260303-8538', '华硕天选6Pro/5Pro游戏本笔记本电脑', NULL, '16英寸独显5060电竞满功耗 天选4 I7-13620H RTX4050 16GB+1TB', '/api/files/assets/45/9ae0771f-a1da-4e35-8c09-d0fa79bbf8e5.avif', 7, '2025-06-05', 5477.52, 4706.34, 3765.07, 1, NULL, b'0', '待审核', '2026-03-03 11:11:13', '2026-03-03 11:11:13');
INSERT INTO `asset` VALUES (46, 'ASSET-20260303-8737', '魔霸新锐 锐龙9 16英寸 游戏本笔记本电脑', NULL, 'R9 8940HX 16G 1T RTX5060 2.5K 165Hz', '/api/files/assets/46/dbde336c-e33d-42f2-ad8d-bdadce912936.avif,/api/files/assets/46/b298b93c-b234-4cf4-8289-d3983d073802.avif,/api/files/assets/46/4e42ed37-8d3c-4e8a-8358-619c39859dd2.avif,/api/files/assets/46/ec02ac01-171d-4c36-afb5-459a655be678.avif', 2, '2026-01-20', 9699.00, 9489.64, 7591.71, 1, NULL, b'0', '待审核', '2026-03-03 11:12:31', '2026-03-03 11:12:31');
INSERT INTO `asset` VALUES (47, 'ASSET-20260303-1081', '联想拯救者Y7000P/Y9000P游戏本笔记本电脑', NULL, 'Y9000P I7-11800H/RTX3060 16GB+1TB', '/api/files/assets/47/ee69c32c-9e80-479d-98b5-06c4fb523b72.avif,/api/files/assets/47/6501a1e4-42ab-4600-bfa8-47388710b1f2.avif', 6, '2026-01-28', 5132.32, 5042.91, 4034.33, 1, NULL, b'0', '待审核', '2026-03-03 11:13:49', '2026-03-03 11:13:50');
INSERT INTO `asset` VALUES (48, 'ASSET-20260303-9355', '联想拯救者Y7000P', NULL, '5060笔记本电脑GTX4090显卡 9-y7000P-i7-32G-1TB独立6G', '/api/files/assets/48/6b224192-199f-4721-8757-12aa6df6b023.avif,/api/files/assets/48/d6878640-60fa-46fc-8821-129badd4aa2f.avif,/api/files/assets/48/e96e34d2-35db-4a72-9fe9-1817c3d82cd1.avif', 3, '2025-07-25', 4870.00, 4311.10, 3448.88, 1, NULL, b'0', '待审核', '2026-03-03 11:16:54', '2026-03-03 11:16:54');
INSERT INTO `asset` VALUES (49, 'ASSET-20260303-2802', '联想笔记本电脑ThinkBook16+ 2026全能本 锐龙', NULL, ' 32G 1T 3.2K 16英寸高刷 星耀白', '/api/files/assets/49/9a137d05-e2bc-4691-871c-cca6423a9b4d.avif,/api/files/assets/49/1a20cf13-8149-40e3-99ec-2b0880b67089.avif,/api/files/assets/49/96cc488d-f2fa-41c8-be43-813fc48fbffb.avif', 4, '2026-01-21', 6099.00, 5970.51, 4776.41, 1, NULL, b'0', '待审核', '2026-03-03 11:18:22', '2026-03-03 11:18:22');

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
) ENGINE = InnoDB AUTO_INCREMENT = 168 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of asset_history
-- ----------------------------
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
INSERT INTO `asset_history` VALUES (19, 16, '资产创建', '2026-02-04 13:50:11.429974', '创建', NULL);
INSERT INTO `asset_history` VALUES (25, 22, '资产创建', '2026-02-04 14:00:04.087678', '创建', NULL);
INSERT INTO `asset_history` VALUES (26, 22, '审核通过，起拍价：4197.76，未设置保留价', '2026-02-04 14:03:26.842846', '财务审核', 13);
INSERT INTO `asset_history` VALUES (31, 14, '交易单审核通过：TXN1770202406677', '2026-02-04 19:19:41.720003', '确认收款', 13);
INSERT INTO `asset_history` VALUES (32, 14, '确认处置完成，备注：111', '2026-02-04 19:31:28.681300', '确认处置', 11);
INSERT INTO `asset_history` VALUES (34, 12, '拍卖结束：流拍，拍卖名称：海尔空调，无人出价', '2026-02-05 17:13:51.786172', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (35, 22, '拍卖结束：流拍，拍卖名称：华硕全家桶，无人出价', '2026-02-05 17:13:51.790470', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (36, 23, '资产创建', '2026-02-05 18:35:58.327113', '创建', NULL);
INSERT INTO `asset_history` VALUES (37, 23, '审核通过，起拍价：2075.04，未设置保留价', '2026-02-05 18:36:30.632830', '财务审核', 13);
INSERT INTO `asset_history` VALUES (38, 23, '拍卖结束：成交，拍卖名称：电动车，中标者：庄采润，成交价：2175.04', '2026-02-05 18:55:08.057040', '拍卖结果', NULL);
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
INSERT INTO `asset_history` VALUES (95, 13, '资产信息更新', '2026-02-24 19:32:26.502683', '更新', NULL);
INSERT INTO `asset_history` VALUES (96, 13, '资产信息更新', '2026-02-24 20:06:57.107107', '更新', NULL);
INSERT INTO `asset_history` VALUES (97, 13, '资产信息更新', '2026-02-24 20:22:34.901564', '更新', NULL);
INSERT INTO `asset_history` VALUES (98, 13, '资产信息更新', '2026-02-24 20:22:50.267223', '更新', NULL);
INSERT INTO `asset_history` VALUES (99, 13, '资产信息更新', '2026-02-24 20:28:20.414618', '更新', NULL);
INSERT INTO `asset_history` VALUES (101, 11, '拍卖结束：成交，拍卖名称：三星手表，中标者：星见雅，成交价：1087.59', '2026-02-25 12:34:16.324152', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (102, 27, '审核通过，起拍价：853.1，未设置保留价', '2026-02-25 12:35:27.756799', '财务审核', 1);
INSERT INTO `asset_history` VALUES (103, 12, '拍卖结束：成交，拍卖名称：海尔空调，中标者：甘雨，成交价：1717.27', '2026-02-27 18:22:45.127549', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (104, 32, '拍卖结束：成交，拍卖名称：玩具车，中标者：甘雨，成交价：642.10', '2026-02-27 18:22:45.174052', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (105, 29, '拍卖结束：流拍，拍卖名称：华硕台式主机，未达保留价', '2026-02-27 18:22:45.179112', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (106, 33, '资产创建', '2026-02-27 18:30:11.760920', '创建', 1);
INSERT INTO `asset_history` VALUES (107, 33, '资产审核通过，状态变更为待拍卖', '2026-02-27 18:30:43.217300', '审核通过', 1);
INSERT INTO `asset_history` VALUES (108, 33, '资产价值重新计算', '2026-02-27 18:30:46.497283', '重新计算', NULL);
INSERT INTO `asset_history` VALUES (109, 33, '资产审核通过，状态变更为待拍卖', '2026-02-27 18:30:49.511759', '审核通过', 1);
INSERT INTO `asset_history` VALUES (110, 34, '资产创建', '2026-02-27 18:52:55.085332', '创建', 1);
INSERT INTO `asset_history` VALUES (111, 34, '资产审核通过，状态变更为待拍卖', '2026-02-27 18:53:03.594713', '审核通过', 1);
INSERT INTO `asset_history` VALUES (112, 34, '资产信息更新', '2026-02-27 18:53:16.621008', '更新', NULL);
INSERT INTO `asset_history` VALUES (113, 30, '拍卖结束：成交，拍卖名称：机械师，中标者：星见雅，成交价：6137.38', '2026-02-28 01:12:25.494385', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (114, 28, '拍卖结束：成交，拍卖名称：爱玛电动车，中标者：李毅，成交价：2458.27', '2026-02-28 01:12:25.562728', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (115, 34, '拍卖结束：流拍，拍卖名称：电动车，无人出价', '2026-02-28 01:12:25.569724', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (117, 32, '交易单审核通过：TXN1772187765176', '2026-02-28 01:16:50.494301', '确认收款', 1);
INSERT INTO `asset_history` VALUES (118, 12, '交易单审核通过：TXN1772187765164', '2026-02-28 01:16:58.433649', '确认收款', 1);
INSERT INTO `asset_history` VALUES (119, 35, '资产创建', '2026-02-28 01:20:11.808949', '创建', 1);
INSERT INTO `asset_history` VALUES (120, 35, '资产价值重新计算', '2026-02-28 01:20:32.785252', '重新计算', NULL);
INSERT INTO `asset_history` VALUES (121, 35, '资产审核通过，状态变更为待拍卖', '2026-02-28 01:20:45.754339', '审核通过', 1);
INSERT INTO `asset_history` VALUES (122, 35, '资产价值重新计算', '2026-02-28 01:20:48.330218', '重新计算', NULL);
INSERT INTO `asset_history` VALUES (123, 35, '资产审核通过，状态变更为待拍卖', '2026-02-28 01:20:50.521721', '审核通过', 1);
INSERT INTO `asset_history` VALUES (124, 32, '确认处置完成，备注：111111', '2026-02-28 01:23:06.091757', '确认处置', 1);
INSERT INTO `asset_history` VALUES (125, 36, '资产创建', '2026-02-28 10:23:44.269435', '创建', 17);
INSERT INTO `asset_history` VALUES (126, 36, '审核通过，起拍价：1387.35，未设置保留价', '2026-02-28 10:24:21.738322', '财务审核', 18);
INSERT INTO `asset_history` VALUES (136, 11, '中标者超时未确认，资产退回待拍卖', '2026-02-28 11:16:21.741754', '拍卖结果', 21);
INSERT INTO `asset_history` VALUES (137, 37, '资产创建', '2026-02-28 11:20:06.890386', '创建', 17);
INSERT INTO `asset_history` VALUES (138, 37, '审核通过，起拍价：1371.84，未设置保留价', '2026-02-28 11:20:46.086520', '财务审核', 18);
INSERT INTO `asset_history` VALUES (139, 28, '交易单审核通过：TXN1772212345565', '2026-02-28 11:24:09.128179', '确认收款', 18);
INSERT INTO `asset_history` VALUES (140, 25, '审核通过，起拍价：1243.65，未设置保留价', '2026-02-28 17:01:40.066496', '财务审核', 18);
INSERT INTO `asset_history` VALUES (141, 26, '拍卖结束：成交，拍卖名称：格力空调，中标者：王辉，成交价：1546.13', '2026-03-02 15:40:53.038605', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (142, 36, '拍卖结束：成交，拍卖名称：玩具车11，中标者：甘雨，成交价：1687.35', '2026-03-02 15:40:53.102131', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (143, 37, '拍卖结束：成交，拍卖名称：电动车九成新，中标者：李毅，成交价：1871.84', '2026-03-02 15:40:53.121426', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (144, 35, '拍卖结束：流拍，拍卖名称：玩具車，无人出价', '2026-03-02 15:40:53.130549', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (145, 24, '拍卖结束：成交，拍卖名称：苹果17pro max手机壳，中标者：星见雅，成交价：276.66', '2026-03-03 09:26:57.460775', '拍卖结果', NULL);
INSERT INTO `asset_history` VALUES (146, 30, '中标者超时未确认，资产退回待拍卖', '2026-03-03 10:00:16.052066', '拍卖结果', 21);
INSERT INTO `asset_history` VALUES (147, 24, '交易单审核通过：TXN1772501217520', '2026-03-03 10:01:37.509884', '确认收款', 18);
INSERT INTO `asset_history` VALUES (148, 37, '交易单审核通过：TXN1772437253124', '2026-03-03 10:01:52.958762', '确认收款', 18);
INSERT INTO `asset_history` VALUES (149, 36, '交易单审核通过：TXN1772437253105', '2026-03-03 10:14:00.445790', '确认收款', 18);
INSERT INTO `asset_history` VALUES (150, 38, '资产创建', '2026-03-03 10:56:52.442799', '创建', 1);
INSERT INTO `asset_history` VALUES (151, 38, '审核通过，起拍价：4583.68，保留价：5729.6', '2026-03-03 10:58:06.628767', '财务审核', 1);
INSERT INTO `asset_history` VALUES (152, 24, '确认处置完成，备注：2026/3/5 早上9点到晚上6点期间 都可以到仓库领取物品', '2026-03-03 10:59:05.246020', '确认处置', 1);
INSERT INTO `asset_history` VALUES (153, 37, '确认处置完成，备注：2026/3/5 早上9点到晚上6点期间 都可以到仓库领取物品', '2026-03-03 10:59:11.182781', '确认处置', 1);
INSERT INTO `asset_history` VALUES (154, 36, '确认处置完成，备注：2026/3/5 早上9点到晚上6点期间 都可以到仓库领取物品', '2026-03-03 10:59:21.125803', '确认处置', 1);
INSERT INTO `asset_history` VALUES (155, 28, '确认处置完成，备注：2026/3 早上9点到晚上6点期间 都可以到仓库领取物品', '2026-03-03 10:59:44.830910', '确认处置', 1);
INSERT INTO `asset_history` VALUES (156, 12, '确认处置完成，备注：2026/3 早上9点到晚上6点期间 都可以到仓库领取物品', '2026-03-03 10:59:52.014878', '确认处置', 1);
INSERT INTO `asset_history` VALUES (157, 39, '资产创建', '2026-03-03 11:02:24.964787', '创建', 1);
INSERT INTO `asset_history` VALUES (158, 40, '资产创建', '2026-03-03 11:04:12.843663', '创建', 1);
INSERT INTO `asset_history` VALUES (159, 41, '资产创建', '2026-03-03 11:06:09.368032', '创建', 1);
INSERT INTO `asset_history` VALUES (160, 42, '资产创建', '2026-03-03 11:07:09.247209', '创建', 1);
INSERT INTO `asset_history` VALUES (161, 43, '资产创建', '2026-03-03 11:08:27.254609', '创建', 1);
INSERT INTO `asset_history` VALUES (162, 44, '资产创建', '2026-03-03 11:09:53.305440', '创建', 1);
INSERT INTO `asset_history` VALUES (163, 45, '资产创建', '2026-03-03 11:11:12.667660', '创建', 1);
INSERT INTO `asset_history` VALUES (164, 46, '资产创建', '2026-03-03 11:12:31.176772', '创建', 1);
INSERT INTO `asset_history` VALUES (165, 47, '资产创建', '2026-03-03 11:13:49.421753', '创建', 1);
INSERT INTO `asset_history` VALUES (166, 48, '资产创建', '2026-03-03 11:16:53.855929', '创建', 1);
INSERT INTO `asset_history` VALUES (167, 49, '资产创建', '2026-03-03 11:18:21.782778', '创建', 1);

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
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction
-- ----------------------------
INSERT INTO `auction` VALUES (3, 'test', NULL, 10, 103.42, 103.42, 100.00, b'0', NULL, '2026-02-03 15:34:06', '2026-02-03 16:05:00', 'ended', 0, '九成新', 'all', NULL, NULL, '2026-02-03 23:34:29', '2026-02-20 13:55:36');
INSERT INTO `auction` VALUES (6, '三星手表', NULL, 11, 887.59, 1287.59, 100.00, b'1', 1109.49, '2026-02-04 06:01:20', '2026-02-08 16:00:00', 'ended', 1, '不可反悔', 'all', 12, 1287.59, '2026-02-04 14:01:32', '2026-02-20 13:55:36');
INSERT INTO `auction` VALUES (7, '海尔空调', NULL, 12, 1617.27, 1617.27, 100.00, b'1', 2021.59, '2026-02-04 06:01:50', '2026-02-05 16:00:00', 'ended', 0, '111', 'all', NULL, NULL, '2026-02-04 14:02:04', '2026-02-05 17:13:52');
INSERT INTO `auction` VALUES (8, '华为手机', NULL, 13, 2380.75, 2380.75, 100.00, b'1', 2975.94, '2026-02-04 06:02:24', '2026-02-04 16:00:00', 'ended', 0, '价高者得', 'all', NULL, NULL, '2026-02-04 14:02:39', '2026-02-04 16:00:57');
INSERT INTO `auction` VALUES (12, '华硕全家桶', NULL, 22, 4197.76, 4197.76, 100.00, b'0', NULL, '2026-02-04 16:00:00', '2026-02-05 16:00:00', 'ended', 0, '价高者得', 'all', NULL, NULL, '2026-02-04 15:39:21', '2026-02-05 17:13:52');
INSERT INTO `auction` VALUES (13, '三星', NULL, 14, 750.02, 1050.02, 100.00, b'0', NULL, '2026-02-04 15:50:52', '2026-02-04 15:58:54', 'ended', 2, '价高者得', 'all', 12, 1050.02, '2026-02-04 15:51:07', '2026-02-20 13:55:36');
INSERT INTO `auction` VALUES (14, '电动车', NULL, 23, 2075.04, 2175.04, 100.00, b'0', NULL, '2026-02-05 18:42:58', '2026-02-05 18:55:02', 'ended', 1, '222', 'all', 12, 2175.04, '2026-02-05 18:43:12', '2026-02-05 18:55:08');
INSERT INTO `auction` VALUES (15, '机械师', NULL, 30, 5837.38, 5937.38, 100.00, b'1', 7296.73, '2026-02-20 13:28:42', '2026-02-21 06:00:09', 'ended', 1, '价高者得', 'all', 16, 5937.38, '2026-02-20 13:31:50', '2026-02-22 16:30:43');
INSERT INTO `auction` VALUES (20, '三星手表', NULL, 11, 887.59, 1087.59, 100.00, b'0', NULL, '2026-02-24 12:34:50', '2026-02-25 00:00:00', 'ended', 2, '1111', 'all', 21, 1087.59, '2026-02-24 12:35:07', '2026-02-25 12:34:16');
INSERT INTO `auction` VALUES (21, '海尔空调', NULL, 12, 1617.27, 1717.27, 100.00, b'0', NULL, '2026-02-24 12:35:22', '2026-02-26 00:00:00', 'ended', 1, '2222', 'all', 19, 1717.27, '2026-02-24 12:35:43', '2026-02-27 18:22:45');
INSERT INTO `auction` VALUES (22, '玩具车', NULL, 32, 442.10, 642.10, 200.00, b'0', NULL, '2026-02-24 12:35:54', '2026-02-27 00:00:00', 'ended', 1, '2026.2.27结束', 'all', 19, 642.10, '2026-02-24 12:36:32', '2026-02-27 18:22:45');
INSERT INTO `auction` VALUES (23, '机械师', NULL, 30, 5837.38, 6137.38, 100.00, b'0', NULL, '2026-02-24 12:36:45', '2026-02-28 00:00:00', 'ended', 3, '3333', 'all', 21, 6137.38, '2026-02-24 12:37:04', '2026-02-28 01:12:25');
INSERT INTO `auction` VALUES (24, '电动车', NULL, 31, 2022.30, 2322.30, 100.00, b'0', NULL, '2026-02-24 12:37:25', '2026-03-29 00:00:00', 'in_progress', 3, '2026-03-29 00:00:00结束', 'all', 20, 2322.30, '2026-02-24 12:37:41', '2026-03-03 11:20:38');
INSERT INTO `auction` VALUES (30, '华硕台式主机', NULL, 29, 4315.51, 4415.51, 100.00, b'1', 5394.39, '2026-02-24 18:41:52', '2026-02-27 00:00:00', 'ended', 1, '中标后无故放弃、逾期未确认、逾期未付款或其他违约行为，平台将按规则进行处理，包括但不限于取消成交资格、恢复资产为待拍卖状态，并执行竞拍资格限制（如限制3个月内参与竞拍）。', 'all', 21, 4415.51, '2026-02-24 18:42:00', '2026-02-27 18:22:45');
INSERT INTO `auction` VALUES (31, '苹果17pro max手机壳', NULL, 24, 176.66, 276.66, 100.00, b'0', NULL, '2026-02-24 18:42:29', '2026-03-03 00:00:00', 'ended', 1, '中标后无故放弃、逾期未确认、逾期未付款或其他违约行为，平台将按规则进行处理，包括但不限于取消成交资格、恢复资产为待拍卖状态，并执行竞拍资格限制（如限制3个月内参与竞拍）。', 'all', 21, 276.66, '2026-02-24 18:42:41', '2026-03-03 09:26:57');
INSERT INTO `auction` VALUES (32, '格力空调', NULL, 26, 1246.13, 1546.13, 100.00, b'0', NULL, '2026-02-24 18:42:56', '2026-03-01 00:00:00', 'ended', 3, '中标后无故放弃、逾期未确认、逾期未付款或其他违约行为，平台将按规则进行处理，包括但不限于取消成交资格、恢复资产为待拍卖状态，并执行竞拍资格限制（如限制3个月内参与竞拍）。', 'all', 16, 1546.13, '2026-02-24 18:43:06', '2026-03-02 15:40:53');
INSERT INTO `auction` VALUES (33, '爱玛电动车', NULL, 28, 2058.27, 2458.27, 100.00, b'0', NULL, '2026-02-24 18:45:20', '2026-02-28 00:00:00', 'ended', 3, '中标后无故放弃、逾期未确认、逾期未付款或其他违约行为，平台将按规则进行处理，包括但不限于取消成交资格、恢复资产为待拍卖状态，并执行竞拍资格限制（如限制3个月内参与竞拍）。', 'all', 25, 2458.27, '2026-02-24 18:45:42', '2026-02-28 01:12:26');
INSERT INTO `auction` VALUES (35, '电动车', NULL, 34, 1260.42, 1260.42, 100.00, b'0', NULL, '2026-02-27 18:54:16', '2026-02-28 00:00:00', 'ended', 0, '价高者得', 'all', NULL, NULL, '2026-02-27 18:54:27', '2026-02-28 01:12:26');
INSERT INTO `auction` VALUES (36, '玩具車', NULL, 35, 1384.30, 1384.30, 100.00, b'0', NULL, '2026-02-28 01:22:04', '2026-03-02 00:00:00', 'ended', 0, '價高者得', 'all', NULL, NULL, '2026-02-28 01:22:21', '2026-03-02 15:40:53');
INSERT INTO `auction` VALUES (37, '玩具车11', NULL, 36, 1387.35, 1687.35, 100.00, b'0', NULL, '2026-02-28 10:24:51', '2026-03-01 00:00:00', 'ended', 3, '价高者得', 'all', 19, 1687.35, '2026-02-28 10:25:16', '2026-03-02 15:40:53');
INSERT INTO `auction` VALUES (38, '电动车九成新', NULL, 37, 1371.84, 1871.84, 100.00, b'0', NULL, '2026-02-28 11:21:17', '2026-03-01 00:00:00', 'ended', 2, '九成新', 'all', 25, 1871.84, '2026-02-28 11:21:44', '2026-03-02 15:40:53');
INSERT INTO `auction` VALUES (39, '三星手表', NULL, 11, 887.59, 987.59, 100.00, b'0', NULL, '2026-03-03 10:49:10', '2026-03-05 00:00:00', 'in_progress', 1, '价高者得', 'all', 20, 987.59, '2026-03-03 10:49:20', '2026-03-03 11:20:22');
INSERT INTO `auction` VALUES (40, '华硕全家桶', NULL, 22, 4197.76, 4297.76, 100.00, b'0', NULL, '2026-03-03 11:18:50', '2026-03-05 00:00:00', 'in_progress', 1, '价高者得', 'all', 20, 4297.76, '2026-03-03 11:19:00', '2026-03-03 11:20:29');
INSERT INTO `auction` VALUES (41, '科沃斯扫地机器人', NULL, 38, 4583.68, 4683.68, 100.00, b'0', NULL, '2026-03-03 11:19:15', '2026-03-05 00:00:00', 'in_progress', 1, '价高者得', 'all', 20, 4683.68, '2026-03-03 11:19:33', '2026-03-03 11:20:33');

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
) ENGINE = InnoDB AUTO_INCREMENT = 70 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bid
-- ----------------------------
INSERT INTO `bid` VALUES (3, 3, 13, 203.42, '2026-02-04 14:19:11.528552', b'1', '2026-02-04 14:19:11.528552');
INSERT INTO `bid` VALUES (4, 13, 12, 850.02, '2026-02-04 15:51:46.853358', b'1', '2026-02-04 15:51:46.853358');
INSERT INTO `bid` VALUES (5, 13, 13, 950.02, '2026-02-04 15:52:06.423814', b'1', '2026-02-04 15:52:06.423814');
INSERT INTO `bid` VALUES (6, 13, 12, 1050.02, '2026-02-04 15:52:27.926406', b'1', '2026-02-04 15:52:27.926406');
INSERT INTO `bid` VALUES (7, 14, 12, 2175.04, '2026-02-05 18:46:13.967446', b'1', '2026-02-05 18:46:13.967446');
INSERT INTO `bid` VALUES (9, 6, 13, 1187.59, '2026-02-07 16:36:11.177837', b'0', '2026-02-07 16:36:11.177837');
INSERT INTO `bid` VALUES (10, 6, 12, 1287.59, '2026-02-07 16:36:30.699868', b'1', '2026-02-07 16:36:30.699868');
INSERT INTO `bid` VALUES (11, 15, 16, 5937.38, '2026-02-20 14:34:21.097764', b'1', '2026-02-20 14:34:21.097764');
INSERT INTO `bid` VALUES (42, 20, 19, 987.59, '2026-02-24 12:38:35.899206', b'0', '2026-02-24 12:38:35.899206');
INSERT INTO `bid` VALUES (43, 21, 19, 1717.27, '2026-02-24 12:38:41.329365', b'1', '2026-02-24 12:38:41.329365');
INSERT INTO `bid` VALUES (44, 22, 19, 642.10, '2026-02-24 12:38:46.050259', b'1', '2026-02-24 12:38:46.050259');
INSERT INTO `bid` VALUES (45, 23, 19, 5937.38, '2026-02-24 12:38:51.808958', b'0', '2026-02-24 12:38:51.808958');
INSERT INTO `bid` VALUES (46, 23, 19, 6037.38, '2026-02-24 12:38:57.585031', b'0', '2026-02-24 12:38:57.585031');
INSERT INTO `bid` VALUES (47, 24, 19, 2122.30, '2026-02-24 12:39:02.249101', b'0', '2026-02-24 12:39:02.249101');
INSERT INTO `bid` VALUES (49, 20, 21, 1087.59, '2026-02-24 12:39:46.626583', b'1', '2026-02-24 12:39:46.626583');
INSERT INTO `bid` VALUES (50, 23, 21, 6137.38, '2026-02-24 12:39:51.337045', b'1', '2026-02-24 12:39:51.337045');
INSERT INTO `bid` VALUES (52, 30, 21, 4415.51, '2026-02-24 18:48:05.159492', b'1', '2026-02-24 18:48:05.159492');
INSERT INTO `bid` VALUES (53, 31, 21, 276.66, '2026-02-24 18:50:05.893891', b'1', '2026-02-24 18:50:05.893891');
INSERT INTO `bid` VALUES (54, 32, 21, 1346.13, '2026-02-24 18:50:49.654049', b'0', '2026-02-24 18:50:49.654049');
INSERT INTO `bid` VALUES (55, 33, 21, 2158.27, '2026-02-24 18:52:01.245010', b'0', '2026-02-24 18:52:01.245010');
INSERT INTO `bid` VALUES (56, 33, 25, 2258.27, '2026-02-27 18:32:52.377965', b'0', '2026-02-27 18:32:52.377965');
INSERT INTO `bid` VALUES (57, 33, 25, 2458.27, '2026-02-27 18:32:59.558200', b'1', '2026-02-27 18:32:59.558200');
INSERT INTO `bid` VALUES (58, 24, 19, 2222.30, '2026-02-28 01:13:49.262428', b'0', '2026-02-28 01:13:49.262428');
INSERT INTO `bid` VALUES (59, 37, 19, 1487.35, '2026-02-28 10:26:05.823807', b'0', '2026-02-28 10:26:05.823807');
INSERT INTO `bid` VALUES (60, 37, 19, 1587.35, '2026-02-28 10:26:16.143897', b'0', '2026-02-28 10:26:16.143897');
INSERT INTO `bid` VALUES (61, 32, 19, 1446.13, '2026-02-28 10:31:34.273573', b'0', '2026-02-28 10:31:34.273573');
INSERT INTO `bid` VALUES (62, 37, 19, 1687.35, '2026-02-28 10:52:23.601777', b'1', '2026-02-28 10:52:23.601777');
INSERT INTO `bid` VALUES (63, 32, 16, 1546.13, '2026-02-28 10:52:54.832072', b'1', '2026-02-28 10:52:54.832072');
INSERT INTO `bid` VALUES (64, 38, 25, 1471.84, '2026-02-28 11:22:20.163989', b'0', '2026-02-28 11:22:20.163989');
INSERT INTO `bid` VALUES (65, 38, 25, 1871.84, '2026-02-28 11:22:33.921520', b'1', '2026-02-28 11:22:33.921520');
INSERT INTO `bid` VALUES (66, 39, 20, 987.59, '2026-03-03 11:20:21.656136', b'1', '2026-03-03 11:20:21.656136');
INSERT INTO `bid` VALUES (67, 40, 20, 4297.76, '2026-03-03 11:20:28.843436', b'1', '2026-03-03 11:20:28.843436');
INSERT INTO `bid` VALUES (68, 41, 20, 4683.68, '2026-03-03 11:20:33.068289', b'1', '2026-03-03 11:20:33.068289');
INSERT INTO `bid` VALUES (69, 24, 20, 2322.30, '2026-03-03 11:20:37.725135', b'1', '2026-03-03 11:20:37.725135');

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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES (1, '技术部', '2026-02-01 11:05:16', '2026-02-27 18:50:58', '', '庄采润', '17728840461');
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
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of depreciation_rule
-- ----------------------------
INSERT INTO `depreciation_rule` VALUES (1, 0, '2026-02-01 11:33:27.926727', '适用于电脑、打印机等电子设备', '电子设备折旧规则', 5, b'1', '2026-02-01 11:33:27.926727', 5);
INSERT INTO `depreciation_rule` VALUES (2, 0, '2026-02-01 11:33:27.929724', '适用于桌椅、柜子等办公设备', '办公设备折旧规则', 5, b'1', '2026-02-01 11:33:27.929724', 8);
INSERT INTO `depreciation_rule` VALUES (3, 0, '2026-02-01 11:33:27.931246', '适用于公司车辆', '车辆折旧规则', 5, b'1', '2026-02-28 01:21:35.673235', 10);

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
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of transaction
-- ----------------------------
INSERT INTO `transaction` VALUES (1, 13, 14, 12, 'TXN1770202406677', 1050.02, '2026-02-06 18:53:26.675505', 'confirmed', '2026-02-04 18:53:26.675505', '2026-02-04 18:53:26.675505', '111', 'completed', '2026-02-04 19:31:28.630215', '/uploads/vouchers/disposal/20260204/4430a5e9-5739-45e0-8dd6-4e1728b06938.jpg', NULL, 'approved', '2026-02-04 19:19:41.709004', '/uploads/vouchers/payment/20260204/8f16663f-ad13-4067-afad-337132af8c40.jpg', '2026-02-04 19:31:28.630215', '2026-02-04 18:53:27');
INSERT INTO `transaction` VALUES (2, 3, 10, 13, 'TXN1770208312337', 203.42, '2026-02-06 20:31:52.336798', 'confirmed', '2026-02-04 20:31:52.336798', '2026-02-04 20:31:52.336798', '公司楼下交接', 'completed', '2026-02-19 17:00:16.104513', '/uploads/vouchers/disposal/20260219/979cd2a4-0eaa-44fb-aaab-591b3544915c.jpg', NULL, 'approved', '2026-02-14 22:28:20.425380', '/uploads/vouchers/payment/20260214/4bdac7bd-ad07-4d23-9083-eaefca4714df.jpg', '2026-02-19 17:00:16.104513', '2026-02-04 20:31:52');
INSERT INTO `transaction` VALUES (3, 14, 23, 12, 'TXN1770288908090', 2175.04, '2026-02-08 13:15:51.921035', 'confirmed', '2026-02-06 13:15:51.921035', '2026-02-05 18:55:08.090912', '123', 'completed', '2026-02-09 13:50:18.317885', '/uploads/vouchers/disposal/20260209/ebfc19bf-aace-4caa-b203-1f83bb1a882a.jpg', NULL, 'approved', '2026-02-09 13:07:32.463236', '/uploads/vouchers/payment/20260209/0f3f6eb4-df0b-49c8-8de5-8a32a802b49a.jpg', '2026-02-09 13:50:18.317885', '2026-02-06 18:55:08');
INSERT INTO `transaction` VALUES (4, 6, 11, 12, 'TXN1770613594904', 1287.59, '2026-02-11 13:06:34.904663', 'expired', NULL, '2026-02-09 13:06:34.904663', NULL, 'PENDING', NULL, NULL, NULL, 'pending', NULL, NULL, '2026-02-19 02:00:00.021267', '2026-02-10 13:06:35');
INSERT INTO `transaction` VALUES (6, 15, 30, 16, 'TXN1771749122712', 5937.38, '2026-02-24 16:32:02.709522', 'confirmed', '2026-02-22 16:32:02.709522', '2026-02-22 16:32:02.709522', '111', 'completed', '2026-02-22 16:34:34.851273', '/uploads/vouchers/disposal/20260222/9d87fb80-310f-4425-baf6-fe3570555de2.jpg', NULL, 'approved', '2026-02-22 16:34:08.741895', '/uploads/vouchers/payment/20260222/6b1a1236-b1ae-4861-9fec-bea1a4ae292a.jpg', '2026-02-22 16:34:34.851273', '2026-02-22 16:32:03');
INSERT INTO `transaction` VALUES (11, 20, 11, 21, 'TXN1771994056325', 1087.59, '2026-02-27 12:34:16.325152', 'expired', NULL, '2026-02-25 12:34:16.325152', NULL, 'PENDING', NULL, NULL, NULL, 'pending', NULL, NULL, '2026-02-28 11:16:21.741754', '2026-02-26 12:34:16');
INSERT INTO `transaction` VALUES (12, 21, 12, 19, 'TXN1772187765164', 1717.27, '2026-03-02 01:14:54.421581', 'confirmed', '2026-02-28 01:14:54.421581', '2026-02-27 18:22:45.164546', '2026/3 早上9点到晚上6点期间 都可以到仓库领取物品', 'completed', '2026-03-03 10:59:52.012882', '/uploads/vouchers/disposal/20260303/b3d5e1fb-3f08-416d-b976-818e20ed0351.jpg', NULL, 'approved', '2026-02-28 01:16:58.431652', '/uploads/vouchers/payment/20260228/1cf1f14a-e17d-4617-9852-6091a3d46d21.jpg', '2026-03-03 10:59:52.012882', '2026-02-28 18:22:45');
INSERT INTO `transaction` VALUES (13, 22, 32, 19, 'TXN1772187765176', 642.10, '2026-03-02 01:14:37.898462', 'confirmed', '2026-02-28 01:14:37.898462', '2026-02-27 18:22:45.176052', '111111', 'completed', '2026-02-28 01:23:06.084235', '/uploads/vouchers/disposal/20260228/4f7dab27-0052-4742-adb6-aefd16af9ec2.jpg', NULL, 'approved', '2026-02-28 01:16:50.491772', '/uploads/vouchers/payment/20260228/4d1c9518-321f-4e7f-b4e6-706084a1e4dc.jpg', '2026-02-28 01:23:06.084235', '2026-02-28 18:22:45');
INSERT INTO `transaction` VALUES (14, 23, 30, 21, 'TXN1772212345548', 6137.38, '2026-03-02 01:12:25.549725', 'expired', NULL, '2026-02-28 01:12:25.549725', NULL, 'PENDING', NULL, NULL, NULL, 'pending', NULL, NULL, '2026-03-03 10:00:16.049140', '2026-03-01 01:12:26');
INSERT INTO `transaction` VALUES (15, 33, 28, 25, 'TXN1772212345565', 2458.27, '2026-03-02 11:23:16.515089', 'confirmed', '2026-02-28 11:23:16.515089', '2026-02-28 01:12:25.565726', '2026/3 早上9点到晚上6点期间 都可以到仓库领取物品', 'completed', '2026-03-03 10:59:44.828907', '/uploads/vouchers/disposal/20260303/976a0a7b-0171-4e8e-9395-c52e6c396c79.jpg', NULL, 'approved', '2026-02-28 11:24:09.125181', '/uploads/vouchers/payment/20260228/b860d923-ed9a-4af3-9166-5d4384cc13f6.jpg', '2026-03-03 10:59:44.828907', '2026-03-01 01:12:26');
INSERT INTO `transaction` VALUES (16, 32, 26, 16, 'TXN1772437253088', 1546.13, '2026-03-04 15:40:53.088797', 'pending', NULL, '2026-03-02 15:40:53.088797', NULL, 'PENDING', NULL, NULL, NULL, 'pending', NULL, NULL, '2026-03-02 15:40:53.088797', '2026-03-03 15:40:53');
INSERT INTO `transaction` VALUES (17, 37, 36, 19, 'TXN1772437253105', 1687.35, '2026-03-05 09:58:59.503133', 'confirmed', '2026-03-03 09:58:59.503133', '2026-03-02 15:40:53.105255', '2026/3/5 早上9点到晚上6点期间 都可以到仓库领取物品', 'completed', '2026-03-03 10:59:21.123803', '/uploads/vouchers/disposal/20260303/c4183cb3-ef79-40ed-b785-a7d5f4cb2ac3.jpg', NULL, 'approved', '2026-03-03 10:14:00.440785', '/uploads/vouchers/payment/20260303/178e0574-2470-490c-bb54-1071b9c7500c.jpg', '2026-03-03 10:59:21.123803', '2026-03-03 15:40:53');
INSERT INTO `transaction` VALUES (18, 38, 37, 25, 'TXN1772437253124', 1871.84, '2026-03-05 10:00:59.604353', 'confirmed', '2026-03-03 10:00:59.604353', '2026-03-02 15:40:53.124558', '2026/3/5 早上9点到晚上6点期间 都可以到仓库领取物品', 'completed', '2026-03-03 10:59:11.180784', '/uploads/vouchers/disposal/20260303/6f7e0aa3-c92d-4acd-beb8-f394ac2c0d6b.jpg', NULL, 'approved', '2026-03-03 10:01:52.952754', '/uploads/vouchers/payment/20260303/55da13ba-cede-48c4-8b75-d91a0282e192.jpg', '2026-03-03 10:59:11.180784', '2026-03-03 15:40:53');
INSERT INTO `transaction` VALUES (19, 31, 24, 21, 'TXN1772501217520', 276.66, '2026-03-05 10:00:13.989882', 'confirmed', '2026-03-03 10:00:13.989882', '2026-03-03 09:26:57.520733', '2026/3/5 早上9点到晚上6点期间 都可以到仓库领取物品', 'completed', '2026-03-03 10:59:05.241024', '/uploads/vouchers/disposal/20260303/c2420e21-363b-4405-b5e1-96689690542d.jpg', NULL, 'approved', '2026-03-03 10:01:37.507885', '/uploads/vouchers/payment/20260303/3347eb48-a452-45a6-842a-049adf71fb2b.jpg', '2026-03-03 10:59:05.241024', '2026-03-04 09:26:58');

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
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$w/gdqmz.YY1CbI72EzUBrONV3hIjjyKHRqY0q21uxLjYdT3sN5.cG', 'admin@example.com', '15815321902', '王阳明', '/uploads/avatars/20260214/9c0d2d25-c36d-4f74-9f48-2e7b55f829d3.png', 6, 'ADMIN', 1, '2026-02-01 11:05:16', '2026-02-14 19:17:10', NULL);
INSERT INTO `user` VALUES (11, '17728840466', '$2a$10$CGysH3nG5V5TPRjCK2EjlOJuPT7C41HtYRZ35d3CoobTe8P4miVKm', NULL, '17728840466', '爱丽丝', '/uploads/avatars/20260220/4fc1610c-babd-4c1f-b616-450e2ded5cd0.png', 6, 'asset_specialist', 1, '2026-02-02 23:44:59', '2026-02-28 11:53:45', NULL);
INSERT INTO `user` VALUES (12, ' 17728840461', '$2a$10$MvggPPK1gBw1UldhEtdG9uqWDa14eBJfPJNcbcV60dPGRNDRMmblO', NULL, '17728840461', '庄采润', NULL, 1, 'NORMAL_USER', 1, '2026-02-02 23:55:39', '2026-02-28 11:05:15', '2026-05-19 02:00:00');
INSERT INTO `user` VALUES (13, '17728840455', '$2a$10$Rhzq0A5QifMwR9ZNyMb/8eFOeVisf8Bz2GSFLsF4CoWl9aU5ga0km', NULL, '17728840455', '小雪', '/uploads/avatars/20260214/dad23f62-87c2-4f53-93db-75b94a71798d.png', 2, 'finance_specialist', 1, '2026-02-03 00:19:08', '2026-02-22 16:33:41', NULL);
INSERT INTO `user` VALUES (16, '17728840499', '$2a$10$3BBBbIcmN0I/LMU29PLgUOXtU0b01.zJitDNeB/CF.LW18iPxPOYW', NULL, '17728840499', '王辉', '/uploads/avatars/20260220/7cbada8b-bd6e-4103-b1d3-86fb07ed5479.png', 2, 'NORMAL_USER', 1, '2026-02-20 13:48:22', '2026-02-28 11:05:15', NULL);
INSERT INTO `user` VALUES (17, '17728840555', '$2a$10$w6XHFuIDHtlBtmi8SkQnneB/3ncwdU/Cu1.38YQKLz8QslcGUPrZK', NULL, '17728840555', '空', NULL, 6, 'asset_specialist', 1, '2026-02-22 18:30:46', '2026-02-28 12:39:51', NULL);
INSERT INTO `user` VALUES (18, '17728840666', '$2a$10$iriRjACF9sKll5wVkYIwQ.6T84X6Wn6135kfSGhVK0tfJ32FaEDj.', NULL, '17728840666', '荧', '/uploads/avatars/20260222/7a2b74bc-2e0f-4b3a-8b24-3a2e8cfd33a2.jpg', 2, 'finance_specialist', 1, '2026-02-22 18:32:40', '2026-02-22 19:17:54', NULL);
INSERT INTO `user` VALUES (19, '18851832622', '$2a$10$hb4yTDLO/DsZgh3MuH5ETeGsWkXMwFI7L5zXBZBCP8xSIODcKej2G', NULL, '18851832622', '甘雨', '/uploads/avatars/20260222/03325896-12bf-4b86-b4eb-b2caffe074e9.jpg', 3, 'NORMAL_USER', 1, '2026-02-22 19:02:40', '2026-02-28 11:05:15', '2026-05-28 11:15:13');
INSERT INTO `user` VALUES (20, '18851832633', '$2a$10$JkFzsS07i63S3YhqAAkYduwO/lRcJWNnibK1kvqRsb.xRuYJ7IcWK', NULL, '18851832633', '千夏', '/uploads/avatars/20260222/982764bd-97c7-46a6-ad52-edf3c9d5421e.png', 5, 'NORMAL_USER', 1, '2026-02-22 19:09:07', '2026-02-28 11:05:15', NULL);
INSERT INTO `user` VALUES (21, '18851832644', '$2a$10$2LLrOpNaTaY4oT6TE2oZs.lSZL9.HB1ha7aZrhqnpbqvUk/5abCKm', NULL, '18851832644', '星见雅', '/uploads/avatars/20260222/e2a78cf9-6b58-44b7-a132-20ec14871bd5.jpg', 1, 'NORMAL_USER', 1, '2026-02-22 19:09:47', '2026-02-28 11:05:15', '2026-06-03 10:00:16');
INSERT INTO `user` VALUES (22, '15815321903', '$2a$10$rowAhZiAb/WiEUg2hOO6DOO1XlfpJS96ExAco7IZUA6w/Rwyz7g56', NULL, '15815321903', '徐翀', NULL, 4, 'NORMAL_USER', 1, '2026-02-24 19:09:47', '2026-02-28 11:05:15', NULL);
INSERT INTO `user` VALUES (23, '15815321955', '$2a$10$QJ.781kk7YdrL.aSYSQMKe5NAvHfHK1clPBUofMUsby.7.LGK1O4O', NULL, '15815321955', '李艾', NULL, 7, 'NORMAL_USER', 1, '2026-02-24 19:10:48', '2026-02-28 11:05:15', NULL);
INSERT INTO `user` VALUES (25, '17728840444', '$2a$10$0auUlkOUEAAFGNVQh/EN9u/B4byg97ZXM6Egx6uPZBmItL28XXQj2', NULL, '17728840444', '李毅', NULL, 6, 'NORMAL_USER', 1, '2026-02-27 18:32:31', '2026-02-28 11:05:15', NULL);

-- ----------------------------
-- View structure for v_disposal_archive
-- ----------------------------
DROP VIEW IF EXISTS `v_disposal_archive`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_disposal_archive` AS select `a`.`id` AS `asset_id`,`a`.`code` AS `asset_code`,`a`.`name` AS `asset_name`,`a`.`category` AS `asset_category`,`a`.`status` AS `asset_status`,`t`.`id` AS `transaction_id`,`t`.`code` AS `transaction_code`,`t`.`final_price` AS `final_price`,`t`.`confirm_status` AS `confirm_status`,`t`.`confirm_time` AS `confirm_time`,`t`.`payment_status` AS `payment_status`,`t`.`payment_time` AS `payment_time`,`t`.`payment_voucher` AS `payment_voucher`,`t`.`payment_remark` AS `payment_remark`,`t`.`disposal_status` AS `disposal_status`,`t`.`disposal_time` AS `disposal_time`,`t`.`disposal_voucher` AS `disposal_voucher`,`t`.`disposal_remark` AS `disposal_remark`,`u`.`id` AS `winner_id`,`u`.`real_name` AS `winner_name`,`u`.`phone` AS `winner_phone`,`au`.`id` AS `auction_id`,`au`.`name` AS `auction_name` from ((((`transaction` `t` join (select `transaction`.`asset_id` AS `asset_id`,max(`transaction`.`id`) AS `latest_tx_id` from `transaction` where (lower(trim(coalesce(`transaction`.`disposal_status`,''))) = 'completed') group by `transaction`.`asset_id`) `lt` on((`lt`.`latest_tx_id` = `t`.`id`))) join `asset` `a` on((`a`.`id` = `t`.`asset_id`))) left join `user` `u` on((`u`.`id` = `t`.`winner_id`))) left join `auction` `au` on((`au`.`id` = `t`.`auction_id`)));

SET FOREIGN_KEY_CHECKS = 1;
