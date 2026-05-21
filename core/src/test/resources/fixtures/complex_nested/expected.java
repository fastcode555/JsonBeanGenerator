import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {
    @SerializedName("user_id")
    public Integer userId;

    @SerializedName("user_name")
    public String userName;

    @SerializedName("is_vip")
    public Boolean isVip;

    @SerializedName("balance")
    public Double balance;

    @SerializedName("tags")
    public List<String> tags;

    @SerializedName("scores")
    public List<List<Integer>> scores;

    @SerializedName("empty_list")
    public List<EmptyList> emptyList;

    @SerializedName("address")
    public Address address;

    @SerializedName("orders")
    public List<Orders> orders;
}

class EmptyList {
}

class Geo {
    @SerializedName("lat")
    public Double lat;

    @SerializedName("lng")
    public Double lng;
}

class Address {
    @SerializedName("city")
    public String city;

    @SerializedName("zip_code")
    public String zipCode;

    @SerializedName("geo")
    public Geo geo;
}

class Items {
    @SerializedName("sku")
    public String sku;

    @SerializedName("qty")
    public Integer qty;
}

class Orders {
    @SerializedName("order_id")
    public String orderId;

    @SerializedName("total")
    public Double total;

    @SerializedName("items")
    public List<Items> items;
}
