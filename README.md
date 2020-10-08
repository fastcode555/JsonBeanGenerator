### JsonBeanGenerator
> 作为一位Android 开发,从早期Java做为Android开发,到Kotlin,再接着学习Flutter,而做业务开发始终绕不过生成Bean对象,在做Flutter开发的时候,发现其它生成Bean对象的工具都还不能够满足自己的需求,故手动撸一个转换插件,并决定可以兼容Java,Kotlin,和Dart的插件

- 1.由于目前先行开发了Dart的插件,故目前暂支持dart对象生成.

- 2.在使用dart的时候,存在类型不安全报错,特别由于后台不够严谨,返回类型出现经常性类型转换异常,**故接管数据的解析,使用本插件,需将此代码引入工程中,使用者可自行修改解析规则**:

  ```Dart
  /// @author Barry
  /// @date 2020/9/4
  /// describe:
  extension MapExt on Map {
    //单字段解析
    String asString(String key) {
      Object value = this[key];
      if (value == null) return "";
      if (value is String) return value;
      return value.toString();
    }
  
    //多字段解析
    String asStrings(List<String> keys) {
      for (String key in keys) {
        Object value = this[key];
        if (value == null) continue;
        if (value is String) {
          return value;
        }
      }
      return "";
    }
  
    double asDouble(String key) {
      Object value = this[key];
      if (value == null) return 0.0;
      if (value is double) return value;
      try {
        double result = double.parse(value.toString());
        return result;
      } catch (e) {
        print(e);
        print('json 解析异常,异常值:\"$key\":$value');
      }
      return 0.0;
    }
  
    double asDoubles(List<String> keys) {
      for (String key in keys) {
        Object value = this[key];
        if (value == null) continue;
        if (value is double) return value;
        try {
          double result = double.parse(value.toString());
          return result;
        } catch (e) {
          print(e);
          print('json 解析异常,异常值:\"$key\":$value');
        }
      }
      return 0.0;
    }
  
    int asInt(String key) {
      Object value = this[key];
      if (value == null) return 0;
      if (value is int) return value;
      try {
        int result = int.parse(value.toString());
        return result;
      } catch (e) {
        print(e);
        print('json 解析异常,异常值:\"$key\":$value');
      }
      return 0;
    }
  
    int asInts(List<String> keys) {
      for (String key in keys) {
        Object value = this[key];
        if (value == null) return 0;
        if (value is int) return value;
        try {
          int result = int.parse(value.toString());
          return result;
        } catch (e) {
          print(e);
          print('json 解析异常,异常值:\"$key\":$value');
        }
      }
      return 0;
    }
  
    num asNum(String key) {
      Object value = this[key];
      if (value == null) return 0;
      if (value is int || value is double) return value;
      try {
        if (value is String) {
          if (value.contains('.')) {
            return double.parse(value);
          } else {
            return int.parse(value);
          }
        }
      } catch (e) {
        print(e);
        print('json 解析异常,异常值:\"$key\":$value');
      }
      return 0;
    }
  
    List asList<T>(String key, T Function(Map<String, dynamic> json) toBean) {
      try {
        if (toBean != null) {
          return (this[key] as List).map((v) => toBean(v)).toList()?.cast<T>();
        } else {
          return List<T>.from(this[key]);
        }
      } catch (e) {
        print(e);
        print('json 解析异常,异常值:\"$key\":${this.toString()}');
      }
      return null;
    }
  
    List asLists<T>(List<String> keys, Function(Map<String, dynamic> json) toBean) {
      for (String key in keys) {
        try {
          if (this[key] != null) {
            if (toBean != null) {
              return (this[key] as List).map((v) => toBean(v)).toList()?.cast<T>();
            } else {
              return List<T>.from(this[key]);
            }
          }
        } catch (e) {
          print(e);
          print('json 解析异常,异常值:\"$key\":${this.toString()}');
        }
      }
  
      return null;
    }
  
    T asBeans<T>(List<String> keys, Function(Map<String, dynamic> json) toBean) {
      for (String key in keys) {
        try {
          if (this[key] != null && _isClassBean(this[key])) {
            return toBean(this[key]);
          }
        } catch (e) {
          print(e);
          print('json 解析异常,异常值:\"$key\":${this.toString()}');
        }
      }
  
      return null;
    }
  
    bool _isClassBean(Object obj) {
      bool isClassBean = true;
      if (obj is String || obj is num || obj is bool) {
        isClassBean = false;
      }
      return isClassBean;
    }
  }
  
  ```

  