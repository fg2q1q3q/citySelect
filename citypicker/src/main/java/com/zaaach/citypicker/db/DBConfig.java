package com.zaaach.citypicker.db;

public class DBConfig {
    public static final String DB_NAME_V2 = "stations_v2.db";
    public static final String LATEST_DB_NAME = DB_NAME_V2;

    public static final String TABLE_NAME = "citys";

    public static final String COLUMN_C_NAME = "sn";
    public static final String COLUMN_C_PROVINCE = "zm";
    public static final String COLUMN_C_PINYIN = "py";
    public static final String COLUMN_C_CODE = "sc";
    public static final String COLUMN_C_ISHOT = "ishot";//1表示热门 2表示已选
    public static final String COLUMN_C_SELECTED= "selected";//1表示热门 2表示已选
    public static final String COLUMN_C_UPDATE = "updatetime";
}
