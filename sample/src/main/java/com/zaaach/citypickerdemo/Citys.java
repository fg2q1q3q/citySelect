package com.zaaach.citypickerdemo;

import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

import java.util.List;

public class Citys {
    public String init;//首字母——大写
    public List<InfoBean> info;//字母下的所有城市
    @Table("citys")
    public static class InfoBean {
        @PrimaryKey(AssignType.BY_MYSELF)
        public String sc;//VAP
        public String szm;//bjb
        public String zm;//bjb
        public String py;//beijingbei
        public String sn;//北京北
        public int selected;//1表示热门 2表示曾经选过
        public int ishot;
        public long updatetime;
    }
}
