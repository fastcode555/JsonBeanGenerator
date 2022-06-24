import 'dart:convert';

import 'package:json2dart_safe/json2dart.dart';

class AudioBookTest {
  Condition? condition;
  Albuminfo? albumInfo;
  List<Tracklist>? trackList;

  AudioBookTest({
    this.condition,
    this.albumInfo,
    this.trackList,
  });

  Map<String, dynamic> toJson() => <String, dynamic>{}
    ..put('condition', condition?.toJson())
    ..put('albumInfo', albumInfo?.toJson())
    ..put('trackList', trackList?.map((v) => v.toJson()).toList());

  AudioBookTest.fromJson(Map json) {
    condition = json.asBean('condition', (v) => Condition.fromJson(v));
    albumInfo = json.asBean('albumInfo', (v) => Albuminfo.fromJson(v));
    trackList = json.asList<Tracklist>('trackList', (v) => Tracklist.fromJson(v));
  }

  static AudioBookTest toBean(Map json) => AudioBookTest.fromJson(json);

  @override
  String toString() => jsonEncode(toJson());
}

class Condition {
  int? start;
  int? length;
  String? id;

  Condition({
    this.start,
    this.length,
    this.id,
  });

  Map<String, dynamic> toJson() => <String, dynamic>{}
    ..put('start', start)
    ..put('length', length)
    ..put('id', id);

  Condition.fromJson(Map json) {
    start = json.asInt('start');
    length = json.asInt('length');
    id = json.asString('id');
  }

  @override
  String toString() => jsonEncode(toJson());
}

class Albuminfo {
  String? authorName;
  int? isHide;
  String? createdTime;
  String? coverPath;
  String? updateDate;
  String? resourceSourceId;
  String? CoverPath;
  int? sumCount;
  String? authorFk;
  String? CoverHost;
  String? language;
  String? resourceSource;
  int? isFinished;
  int? trackCount;
  List<String>? tags;
  String? detailRichIntro;
  int? playCount;
  String? updateTime;
  String? albumImage;
  String? languageText;
  int? id;
  int? resourceSourcePoll;
  String? createDate;
  String? albumTitle;

  Albuminfo({
    this.authorName,
    this.isHide,
    this.createdTime,
    this.coverPath,
    this.updateDate,
    this.resourceSourceId,
    this.CoverPath,
    this.sumCount,
    this.authorFk,
    this.CoverHost,
    this.language,
    this.resourceSource,
    this.isFinished,
    this.trackCount,
    this.tags,
    this.detailRichIntro,
    this.playCount,
    this.updateTime,
    this.albumImage,
    this.languageText,
    this.id,
    this.resourceSourcePoll,
    this.createDate,
    this.albumTitle,
  });

  Map<String, dynamic> toJson() => <String, dynamic>{}
    ..put('author_name', authorName)
    ..put('is_hide', isHide)
    ..put('created_time', createdTime)
    ..put('coverPath', coverPath)
    ..put('updateDate', updateDate)
    ..put('resource_source_id', resourceSourceId)
    ..put('_cover_path', CoverPath)
    ..put('sum_count', sumCount)
    ..put('author_fk', authorFk)
    ..put('_cover_host', CoverHost)
    ..put('language', language)
    ..put('resource_source', resourceSource)
    ..put('isFinished', isFinished)
    ..put('track_count', trackCount)
    ..put('tags', tags)
    ..put('detailRichIntro', detailRichIntro)
    ..put('playCount', playCount)
    ..put('update_time', updateTime)
    ..put('album_image', albumImage)
    ..put('language_text', languageText)
    ..put('id', id)
    ..put('resource_source_poll', resourceSourcePoll)
    ..put('createDate', createDate)
    ..put('album_title', albumTitle);

  Albuminfo.fromJson(Map json) {
    authorName = json.asString('author_name');
    isHide = json.asInt('is_hide');
    createdTime = json.asString('created_time');
    coverPath = json.asString('coverPath');
    updateDate = json.asString('updateDate');
    resourceSourceId = json.asString('resource_source_id');
    CoverPath = json.asString('_cover_path');
    sumCount = json.asInt('sum_count');
    authorFk = json.asString('author_fk');
    CoverHost = json.asString('_cover_host');
    language = json.asString('language');
    resourceSource = json.asString('resource_source');
    isFinished = json.asInt('isFinished');
    trackCount = json.asInt('track_count');
    tags = json.asList<String>('tags');
    detailRichIntro = json.asString('detailRichIntro');
    playCount = json.asInt('playCount');
    updateTime = json.asString('update_time');
    albumImage = json.asString('album_image');
    languageText = json.asString('language_text');
    id = json.asInt('id');
    resourceSourcePoll = json.asInt('resource_source_poll');
    createDate = json.asString('createDate');
    albumTitle = json.asString('album_title');
  }

  @override
  String toString() => jsonEncode(toJson());
}

class Tracklist {
  String? createdTime;
  int? playCount;
  String? updateTime;
  int? trackPoll;
  String? resourceSourceId;
  String? albumFk;
  String? trackTitle;
  String? trackOriginalSource;
  String? authorFk;
  int? indexNo;
  String? resourceSource;
  String? downLoadUrl;

  Tracklist({
    this.createdTime,
    this.playCount,
    this.updateTime,
    this.trackPoll,
    this.resourceSourceId,
    this.albumFk,
    this.trackTitle,
    this.trackOriginalSource,
    this.authorFk,
    this.indexNo,
    this.resourceSource,
    this.downLoadUrl,
  });

  Map<String, dynamic> toJson() => <String, dynamic>{}
    ..put('created_time', createdTime)
    ..put('playCount', playCount)
    ..put('update_time', updateTime)
    ..put('track_poll', trackPoll)
    ..put('resource_source_id', resourceSourceId)
    ..put('album_fk', albumFk)
    ..put('track_title', trackTitle)
    ..put('track_original_source', trackOriginalSource)
    ..put('author_fk', authorFk)
    ..put('index_no', indexNo)
    ..put('resource_source', resourceSource)
    ..put('down_load_url', downLoadUrl);

  Tracklist.fromJson(Map json) {
    createdTime = json.asString('created_time');
    playCount = json.asInt('playCount');
    updateTime = json.asString('update_time');
    trackPoll = json.asInt('track_poll');
    resourceSourceId = json.asString('resource_source_id');
    albumFk = json.asString('album_fk');
    trackTitle = json.asString('track_title');
    trackOriginalSource = json.asString('track_original_source');
    authorFk = json.asString('author_fk');
    indexNo = json.asInt('index_no');
    resourceSource = json.asString('resource_source');
    downLoadUrl = json.asString('down_load_url');
  }

  @override
  String toString() => jsonEncode(toJson());
}
