package com.zaaach.citypicker.model;

public class HotCity extends City {

    public HotCity(String name,boolean isHistory, String sc) {
        super(name, "", isHistory?"历史车站":"热门车站", sc);
    }
}
