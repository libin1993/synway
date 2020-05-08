/*
 * Copyright (C) 2011-2013 dshine.com
 * All rights reserved.
 * ShangHai Dshine - http://www.dshine.com
 */
package com.doit.net.Constant;


/**
 * @author 杨维(Wiker Yong) Email:<a href="mailto:yangwei@dshine.com">yangwei@dshine.com</a>
 * @date 2013-12-30 上午11:49:43
 * @version 1.0
 */
public class PayConstant {
    
    /** 计费用的常量 */
    public final static String CONTENT_TYPE = "race";
    
    public static boolean INIT_ACTIVITY = false;
    
    /** 钻石转换为万的单位需要乘的常量 */
    public final static int DIAMOND_UNIT = 10000;
    
    /** 金额单位分转换为元需要乘的常量，100 */
    public final static int MONEY_UNIT = 100;
    
    /** 车辆磨损度需要的money*/
    public final static int CAR_MAINTAIN_MONEY_1 = 200000;
    
    /** 车辆磨损度需要的money*/
    public final static int CAR_MAINTAIN_MONEY_2 = 500000;
    
    /** 车辆磨损度需要的money*/
    public final static int CAR_MAINTAIN_MONEY_3 = 1200000; 
    
    /** 车辆磨损度需要的金币*/
    public final static int CAR_MAINTAIN_GOLD = 500; 
    
    /** 购买钻石*/
    public final static int SHOP_DAIMOND = 0;
    
    /** 兌换金币*/
    public final static int SHOP_GOLD_COIN = 1;
    
    /** 兌换码*/
    public final static int SHOP_EX_CODE = 2; 
    
    
    /** 是否启用支付宝 */
    public static boolean ALIPAY_ENABLE = true;
    /**
     * 道具相关
     * 
     * @author Wiker
     */
    public static class Props{
        /** 购买防护需要花费的金币 */
        public final static long GOLD_SAFE_COST = 20000;
        
        /** 购买火箭需要花费的金币 */
        public final static long GOLD_ROCKET_COST = 25000;
        
        /** 购买加速道具花费的金币 */
        public final static long GOLD_SPEED_COST = 30000;
        /** 购买道具最大数量 */
        public final static int MAX_PROPS_NUMBER = 100;
        /** 允许购买道具最大数量 */
        public final static int MAX_PROPS_TOTAL = 9999;
        /** 购买道具默认数量*/
        public static long DEFAULT_PROPS_10 = 10;
        /** 默认开宝箱使用的金币 */
        public final static long OPEN_CHEST_GOLD = 100000;
    }
    
    /**
     * 充值的入口,每个计费点都有一个运营商计费入口(1~98),自己定义,每个入口还有支付宝方式(101~198),后台根据这个值返回对应的计费策略
     * 有两个比较的特殊
     * 99  为账号解除绑定手机号码,即会取消userid与手机号码之间的对应关系
     * 100 为账号绑定手机号码,即会建立userid与手机号码之间的对应关系
     * 
     * @author Wiker
     */
    public final class Point{
        
        /** 道具界面购买飞弹金币不够  */
        public final static int POINT_1 = 1;
        /** 道具界面购买防护金币不够  */
        public final static int POINT_2 = 2;
        /** 道具界面购买加速金币不够  */
        public final static int POINT_3 = 3;
        /** 道具界面开宝箱金币不够  */
        public final static int POINT_4 = 4;
        /** 商城购买钻石 */
        public final static int POINT_5 = 5;
        /** 商城兑换金币钻石不够 */
        public final static int POINT_6 = 6;        
        /** 改装赛车加速度时金币不够  */
        public final static int POINT_7 = 7;
        /** 改装赛车操控指数时金币不够  */
        public final static int POINT_8 = 8;
        /** 改装赛车最大速度时金币不够  */
        public final static int POINT_9 = 9;
        /** 改装赛车加速度时钻石不够  */
        public final static int POINT_10 = 10;
        /** 改装赛车操控指数时钻石不够  */
        public final static int POINT_11 = 11;
        /** 改装赛车最大速度时钻石不够  */
        public final static int POINT_12 = 12;
        /** 改装赛车  */
        public final static int POINT_13 = 13;
        /** 选赛道界面，解锁赛道金币不够 */
        public final static int POINT_15 = 15;
        /** 选赛道界面，解锁赛道钻石不够 */
        public final static int POINT_16 = 16;
        /** 比赛界面购买飞弹金币不够 */
        public final static int POINT_17 = 17;
        /** 比赛界面购买防护金币不够 */
        public final static int POINT_18 = 18;
        /** 比赛界面购买加速金币不够 */
        public final static int POINT_19 = 19;
        /** 维修界面金币不够 */
        public final static int POINT_20 = 20;
        /** 维修界面钻石不够 */
        public final static int POINT_21 = 21;
        /** 第一次购买钻石成功，跳转到兑换 */
        public final static int POINT_22 = 22;
        /** 升级界面维修赛车金币不够(赛车升级) */
        public final static int POINT_23 = 23;
        /** 升级界面维修赛车钻石不够 */
        public final static int POINT_24 = 24;
        /** 车库界面金币点击 */
        public final static int POINT_25 = 25;
        /** 车库界面钻石点击 */
        public final static int POINT_26 = 26;
        /** 道具界面金币点击 */
        public final static int POINT_27 = 27;
        /** 道具界面钻石点击 */
        public final static int POINT_28 = 28;
        /** 赛道界面金币点击 */
        public final static int POINT_29 = 29;
        /** 赛道界面钻石点击 */
        public final static int POINT_30 = 30;
        /** 主界面商城 */
        public final static int POINT_31 = 31;
        /** 商城内兑换金币钻石不足 */
        public final static int POINT_32 = 32;
        /** 赛车改装（单货币） */
        public final static int POINT_33 = 33;
        /** 解锁赛道(单货币) */
        public final static int POINT_34 = 34;
        /** 5元充值120万金币 */
        public final static int POINT_35 = 35;
        /** 0.1元充值480金币 */
        public final static int POINT_36 = 36;
        /** 任务界面钻石点击 */
        public final static int POINT_37 = 37;
        /** 金币翻倍可重复购买  游戏中翻倍*/
        public final static int POINT_38 = 38;
        /** 黄金赛门票可重复购买 */
        public final static int POINT_39 = 39;
        /** 延时道具可重复购买 */
        public final static int POINT_40 = 40;
        /** 吸附道具可重复购买 */
        public final static int POINT_41 = 41;
        /** 金币翻倍可重复购买 结束时翻倍 */
        public final static int POINT_42 = 42;
        /** 赛车手升级金币不足 */
        public final static int POINT_43 = 43;
        /** 赛车手解锁金币不足 */
        public final static int POINT_44 = 44;
        /** 从提示跳转到升级界面维修赛车金币不够(赛车升级) */
        public final static int POINT_45 = 45;
        /** 新手购买礼包*/
        public final static int POINT_46 = 46;
        /** 土豪购买礼包*/
        public final static int POINT_47 = 47;
        /** 15元周卡*/
        public final static int POINT_48 = 48;
        /** 30元月卡*/
        public final static int POINT_49 = 49;
        /** 其它金额解锁赛道*/
        public final static int POINT_50 = 50;
        /** 选车界面，解锁赛车金币不够 */
        public final static int POINT_70 = 70;
        /** 选车界面，解锁赛车钻石不够 */
        public final static int POINT_80 = 80;
        /** 优惠点购买(兰博基尼Gallardo 2013) */
        public final static int POINT_90 = 90;
        /** 首充双倍 */
        public final static int POINT_91 = 91;
        /** 优惠点购买(科尼赛克 Agera) */
        public final static int POINT_92 = 92;
        /** 优惠点购买(布加迪) */
        public final static int POINT_93 = 93;
        
    }
}
