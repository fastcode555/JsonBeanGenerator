part of 'User.dart';

EmptyList _$EmptyListClone(EmptyList i) => EmptyList();

Map<String, dynamic> _$EmptyListToJson(EmptyList i) => {};

EmptyList _$EmptyListFromJson(Map json) => EmptyList();

Geo _$GeoClone(Geo i) => Geo(
        lat: i.lat,
        lng: i.lng,
    );

Map<String, dynamic> _$GeoToJson(Geo i) => {
        'lat': i.lat,
        'lng': i.lng,
    };

Geo _$GeoFromJson(Map json) => Geo(
      lat: json.asDouble('lat'),
      lng: json.asDouble('lng'),
    );

Address _$AddressClone(Address i) => Address(
        city: i.city,
        zipCode: i.zipCode,
        geo: i.geo?.clone(),
    );

Map<String, dynamic> _$AddressToJson(Address i) => {
        'city': i.city,
        'zip_code': i.zipCode,
        'geo': i.geo?.toJson(),
    };

Address _$AddressFromJson(Map json) => Address(
      city: json.asString('city'),
      zipCode: json.asString('zip_code'),
      geo: json.asBean('geo', Geo.fromJson),
    );

Items _$ItemsClone(Items i) => Items(
        sku: i.sku,
        qty: i.qty,
    );

Map<String, dynamic> _$ItemsToJson(Items i) => {
        'sku': i.sku,
        'qty': i.qty,
    };

Items _$ItemsFromJson(Map json) => Items(
      sku: json.asString('sku'),
      qty: json.asInt('qty'),
    );

Orders _$OrdersClone(Orders i) => Orders(
        orderId: i.orderId,
        total: i.total,
        items: i.items?.map((v) => v.clone()).toList(),
    );

Map<String, dynamic> _$OrdersToJson(Orders i) => {
        'order_id': i.orderId,
        'total': i.total,
        'items': i.items?.map((v) => v.toJson()).toList(),
    };

Orders _$OrdersFromJson(Map json) => Orders(
      orderId: json.asString('order_id'),
      total: json.asDouble('total'),
      items: json.asList<Items>('items', Items.fromJson),
    );

User _$UserClone(User i) => User(
        userId: i.userId,
        userName: i.userName,
        isVip: i.isVip,
        balance: i.balance,
        tags: List<String>.from(i.tags??[]),
        scores: i.scores?.map((e) => List<int>.from(e)).toList(),
        emptyList: i.emptyList?.map((v) => v.clone()).toList(),
        address: i.address?.clone(),
        orders: i.orders?.map((v) => v.clone()).toList(),
    );

Map<String, dynamic> _$UserToJson(User i) => {
        'user_id': i.userId,
        'user_name': i.userName,
        'is_vip': i.isVip,
        'balance': i.balance,
        'tags': i.tags,
        'scores': i.scores,
        'empty_list': i.emptyList?.map((v) => v.toJson()).toList(),
        'address': i.address?.toJson(),
        'orders': i.orders?.map((v) => v.toJson()).toList(),
    };

User _$UserFromJson(Map json) => User(
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
