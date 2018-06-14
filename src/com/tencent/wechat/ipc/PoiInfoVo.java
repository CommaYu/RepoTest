package com.tencent.wechat.ipc;
/**
 * Author: congqin<br>
 * Data: 2016/12/14.<br>
 * Description: 从助理获取的poi实体类<br>
 * Note: <br>
 */
public class PoiInfoVo {

    private String province;

    private String city;

    private String area;

    private String street;

    private String number;

    private String name;

    private String poiname;

    private String longitude;

    private String latitude;

    /*坐标系统，取值   bdl（百度墨卡托）、bd9（百度09）、gcj（国测局02）、wgs （国际标准）*/
    private String coord_type;


    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoiname() {
        return poiname;
    }

    public void setPoiname(String poiname) {
        this.poiname = poiname;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getCoord_type() {
        return coord_type;
    }

    public void setCoord_type(String coord_type) {
        this.coord_type = coord_type;
    }


    public PoiInfoVo(String province, String city, String area, String street, String number, String name, String
            poiname, String longitude, String latitude, String coord_type) {
        this.province = province;
        this.city = city;
        this.area = area;
        this.street = street;
        this.number = number;
        this.name = name;
        this.poiname = poiname;
        this.longitude = longitude;
        this.latitude = latitude;
        this.coord_type = coord_type;
    }

    public PoiInfoVo() {
    }
}
