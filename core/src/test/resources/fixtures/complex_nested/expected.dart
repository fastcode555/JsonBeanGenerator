import 'dart:convert';

import 'package:json2dart_safe/json2dart.dart';

class User {
  final int userId;
  final String userName;
  final bool isVip;
  final double balance;
  List<String>? tags;
  List<List<int>>? scores;
  List<EmptyList>? emptyList;
  Address? address;
  List<Orders>? orders;

  User({
    required this.userId,
    required this.userName,
    required this.isVip,
    required this.balance,
    required this.tags,
    required this.scores,
    this.emptyList,
    this.address,
    this.orders,
  });

  Map<String, dynamic> toJson() => {
        'user_id': userId,
        'user_name': userName,
        'is_vip': isVip,
        'balance': balance,
        'tags': tags,
        'scores': scores,
        'empty_list': emptyList?.map((v) => v.toJson()).toList(),
        'address': address?.toJson(),
        'orders': orders?.map((v) => v.toJson()).toList(),
      };

  factory User.fromJson(Map json) {
    return User(
      userId: json.asInt('user_id'),
      userName: json.asString('user_name'),
      isVip: json.asBool('is_vip'),
      balance: json.asDouble('balance'),
      tags: json.asList<String>('tags'),
      scores: json.asArray2d<int>('scores'),
      emptyList: json.asList<EmptyList>('empty_list', EmptyList.fromJson),
      address: json.asBean('address', Address.fromJson),
      orders: json.asList<Orders>('orders', Orders.fromJson),
    );
  }

  @override
  String toString() => jsonEncode(toJson());
}

class EmptyList {
  EmptyList();

  Map<String, dynamic> toJson() => {};

  factory EmptyList.fromJson(Map json) {
    return EmptyList();
  }

  @override
  String toString() => jsonEncode(toJson());
}

class Geo {
  final double lat;
  final double lng;

  Geo({
    required this.lat,
    required this.lng,
  });

  Map<String, dynamic> toJson() => {
        'lat': lat,
        'lng': lng,
      };

  factory Geo.fromJson(Map json) {
    return Geo(
      lat: json.asDouble('lat'),
      lng: json.asDouble('lng'),
    );
  }

  @override
  String toString() => jsonEncode(toJson());
}

class Address {
  final String city;
  final String zipCode;
  Geo? geo;

  Address({
    required this.city,
    required this.zipCode,
    this.geo,
  });

  Map<String, dynamic> toJson() => {
        'city': city,
        'zip_code': zipCode,
        'geo': geo?.toJson(),
      };

  factory Address.fromJson(Map json) {
    return Address(
      city: json.asString('city'),
      zipCode: json.asString('zip_code'),
      geo: json.asBean('geo', Geo.fromJson),
    );
  }

  @override
  String toString() => jsonEncode(toJson());
}

class Items {
  final String sku;
  final int qty;

  Items({
    required this.sku,
    required this.qty,
  });

  Map<String, dynamic> toJson() => {
        'sku': sku,
        'qty': qty,
      };

  factory Items.fromJson(Map json) {
    return Items(
      sku: json.asString('sku'),
      qty: json.asInt('qty'),
    );
  }

  @override
  String toString() => jsonEncode(toJson());
}

class Orders {
  final String orderId;
  final double total;
  List<Items>? items;

  Orders({
    required this.orderId,
    required this.total,
    this.items,
  });

  Map<String, dynamic> toJson() => {
        'order_id': orderId,
        'total': total,
        'items': items?.map((v) => v.toJson()).toList(),
      };

  factory Orders.fromJson(Map json) {
    return Orders(
      orderId: json.asString('order_id'),
      total: json.asDouble('total'),
      items: json.asList<Items>('items', Items.fromJson),
    );
  }

  @override
  String toString() => jsonEncode(toJson());
}