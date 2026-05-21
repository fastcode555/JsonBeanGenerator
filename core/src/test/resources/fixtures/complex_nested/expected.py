import json


class User(object):
  def __init__(self,user_id: int = None,user_name: str = None,is_vip: bool = None,balance: float = None,tags: List[str] = None,scores: list = None,empty_list = None,address = None,orders: list = None):
    self.user_id = user_id
    self.user_name = user_name
    self.is_vip = is_vip
    self.balance = balance
    self.tags = tags
    self.scores = scores
    self.empty_list = empty_list
    self.address = address
    self.orders = orders

  @classmethod
  def fromJson(cls, *args):
    if len(args) == 0:
      return
    _dict = json.loads(args[0]) if isinstance(args[0], str) else args[0]
    if isinstance(_dict, tuple):
      return cls(
        user_id = _dict[0] if len(_dict) > 0 else None,
        user_name = _dict[1] if len(_dict) > 1 else None,
        is_vip = _dict[2] if len(_dict) > 2 else None,
        balance = _dict[3] if len(_dict) > 3 else None,
        tags = json.loads(_dict[4]) if len(_dict) > 4 else None,
        scores = json.loads(_dict[5]) if len(_dict) > 5 else None,
        empty_list = [EmptyList.fromJson(element) for element in json.loads(_dict[6]) if element]
         if len(_dict) > 6 and _dict[6] and isinstance(_dict[6], str) else [],
        address = Address.fromJson(json.loads(_dict[7])) if len(_dict) > 7 and isinstance(_dict[7], str) and len(_dict[7]) > 0 else None,
        orders = [Orders.fromJson(element) for element in json.loads(_dict[8]) if element]
         if len(_dict) > 8 and _dict[8] and isinstance(_dict[8], str) else [],
      )
    else:
      return cls(
        user_id = _dict.get('user_id'),
        user_name = _dict.get('user_name'),
        is_vip = _dict.get('is_vip'),
        balance = _dict.get('balance'),
        tags = [element for element in _dict['tags'] if element],
        scores = _dict.get('scores'),
        empty_list = [EmptyList.fromJson(element) for element in _dict.get('empty_list',[]) if element],
        address = Address.fromJson(_dict['address']) if _dict.__contains__('address') else None,
        orders = [Orders.fromJson(element) for element in _dict.get('orders',[]) if element],
      )

  def toJson(self):
    return {
      'user_id': self.user_id,
      'user_name': self.user_name,
      'is_vip': self.is_vip,
      'balance': self.balance,
      'tags': self.tags,
      'scores': self.scores,
      'empty_list': [element.toJson() for element in self.empty_list if element],
      'address': self.address.toJson() if self.address is not None else None,
      'orders': [element.toJson() for element in self.orders if element],
    }

  def toString(self):
    return json.dumps(self.toJson(), indent=2, ensure_ascii=False)

class EmptyList(object):
  def __init__(self):
    pass

  @classmethod
  def fromJson(cls, *args):
    if len(args) == 0:
      return
    _dict = json.loads(args[0]) if isinstance(args[0], str) else args[0]
    if isinstance(_dict, tuple):
      return cls(
      )
    else:
      return cls(
      )

  def toJson(self):
    return {
    }

  def toString(self):
    return json.dumps(self.toJson(), indent=2, ensure_ascii=False)

class Geo(object):
  def __init__(self,lat: float = None,lng: float = None):
    self.lat = lat
    self.lng = lng

  @classmethod
  def fromJson(cls, *args):
    if len(args) == 0:
      return
    _dict = json.loads(args[0]) if isinstance(args[0], str) else args[0]
    if isinstance(_dict, tuple):
      return cls(
        lat = _dict[0] if len(_dict) > 0 else None,
        lng = _dict[1] if len(_dict) > 1 else None,
      )
    else:
      return cls(
        lat = _dict.get('lat'),
        lng = _dict.get('lng'),
      )

  def toJson(self):
    return {
      'lat': self.lat,
      'lng': self.lng,
    }

  def toString(self):
    return json.dumps(self.toJson(), indent=2, ensure_ascii=False)

class Address(object):
  def __init__(self,city: str = None,zip_code: str = None,geo = None):
    self.city = city
    self.zip_code = zip_code
    self.geo = geo

  @classmethod
  def fromJson(cls, *args):
    if len(args) == 0:
      return
    _dict = json.loads(args[0]) if isinstance(args[0], str) else args[0]
    if isinstance(_dict, tuple):
      return cls(
        city = _dict[0] if len(_dict) > 0 else None,
        zip_code = _dict[1] if len(_dict) > 1 else None,
        geo = Geo.fromJson(json.loads(_dict[2])) if len(_dict) > 2 and isinstance(_dict[2], str) and len(_dict[2]) > 0 else None,
      )
    else:
      return cls(
        city = _dict.get('city'),
        zip_code = _dict.get('zip_code'),
        geo = Geo.fromJson(_dict['geo']) if _dict.__contains__('geo') else None,
      )

  def toJson(self):
    return {
      'city': self.city,
      'zip_code': self.zip_code,
      'geo': self.geo.toJson() if self.geo is not None else None,
    }

  def toString(self):
    return json.dumps(self.toJson(), indent=2, ensure_ascii=False)

class Items(object):
  def __init__(self,sku: str = None,qty: int = None):
    self.sku = sku
    self.qty = qty

  @classmethod
  def fromJson(cls, *args):
    if len(args) == 0:
      return
    _dict = json.loads(args[0]) if isinstance(args[0], str) else args[0]
    if isinstance(_dict, tuple):
      return cls(
        sku = _dict[0] if len(_dict) > 0 else None,
        qty = _dict[1] if len(_dict) > 1 else None,
      )
    else:
      return cls(
        sku = _dict.get('sku'),
        qty = _dict.get('qty'),
      )

  def toJson(self):
    return {
      'sku': self.sku,
      'qty': self.qty,
    }

  def toString(self):
    return json.dumps(self.toJson(), indent=2, ensure_ascii=False)

class Orders(object):
  def __init__(self,order_id: str = None,total: float = None,items: list = None):
    self.order_id = order_id
    self.total = total
    self.items = items

  @classmethod
  def fromJson(cls, *args):
    if len(args) == 0:
      return
    _dict = json.loads(args[0]) if isinstance(args[0], str) else args[0]
    if isinstance(_dict, tuple):
      return cls(
        order_id = _dict[0] if len(_dict) > 0 else None,
        total = _dict[1] if len(_dict) > 1 else None,
        items = [Items.fromJson(element) for element in json.loads(_dict[2]) if element]
         if len(_dict) > 2 and _dict[2] and isinstance(_dict[2], str) else [],
      )
    else:
      return cls(
        order_id = _dict.get('order_id'),
        total = _dict.get('total'),
        items = [Items.fromJson(element) for element in _dict.get('items',[]) if element],
      )

  def toJson(self):
    return {
      'order_id': self.order_id,
      'total': self.total,
      'items': [element.toJson() for element in self.items if element],
    }

  def toString(self):
    return json.dumps(self.toJson(), indent=2, ensure_ascii=False)