package com.awesome.utils

object FlutterTailwind {
    val colors = listOf(
        "transparent",
        "black",
        "black87",
        "black54",
        "black45",
        "black38",
        "black26",
        "black12",
        "white",
        "white70",
        "white60",
        "white54",
        "white38",
        "white30",
        "white12",
        "red",
        "redAccent",
        "green",
        "greenAccent",
        "lightGreen",
        "lime",
        "blue",
        "blueAccent",
        "lightBlue",
        "blueGrey",
        "indigoAccent",
        "yellow",
        "yellowAccent",
        "amber",
        "amberAccent",
        "purple",
        "purpleAccent",
        "orange",
        "orangeAccent",
        "brown",
        "pink",
        "grey",
        "cyan",
        "deepOrange",
        "teal",
        "tealAccent",
        "deepPurple",
        "indigo"
    )

    val fluterTailWindConst = "import 'package:flutter/material.dart';\n" +
            "import 'package:flutter_tailwind/flutter_tailwind.dart';\n" +
            "\n" +
            "import 'colours.dart';\n" +
            "\n" +
            "part 'tailwind_ext.g.dart';\n" +
            "\n" +
            "/// define the custom size here\n" +
            "extension SizeExt<T extends SizeBuilder> on T {\n" +
            "  T get h210 => this..height = 210.h;\n" +
            "\n" +
            "  T get w210 => this..width = 210.w;\n" +
            "\n" +
            "  T get s210 => this..size = 210.r;\n" +
            "}\n" +
            "\n" +
            "/// define the custom text feature here\n" +
            "extension TextFeatureExt<T extends TextFeature> on T {\n" +
            "  ///T get fontExample => this..fontFamily = 'font family,just define family here';\n" +
            "}\n\n" +
            "/// define the custom text size here\n" +
            "extension FontSizeExt<T extends FontSizeBuilder> on T {\n" +
            "  ///T get f120 => this..font(120.csp);\n" +
            "}\n\n" +
            "/// define the custom icon here\n" +
            "extension IconExt<T extends IconBuilder> on T {\n" +
            "  ///T get icDefAvatar => this..icon(R.icDefAvatar);\n" +
            "}\n\n" +
            "/// define the custom text style here,text feature just single feature,but style is completed style,can directly use it\n" +
            "extension TextStyleExt<T extends CompletedTextStyleBuilder> on T {\n" +
            "  T get styleMain => this..style = ts.redAccent.f16.bold.underline.mk;\n" +
            "\n\n" +
            "  /// use flutter tailwind style\n" +
            "  T get styleAccent => this..style = ts.greenAccent.f20.bold.underline.mk;\n" +
            "\n\n" +
            "  /// use flutter normal style to describe text style\n" +
            "  T get styleTradition => this\n" +
            "    ..style = TextStyle(\n" +
            "      color: Colors.greenAccent,\n" +
            "      fontSize: 20.sp,\n" +
            "      fontWeight: FontWeight.bold,\n" +
            "      decoration: TextDecoration.underline,\n" +
            "    );\n" +
            "}\n" +
            "\n" +
            "/// define the shadow\n" +
            "extension ShadowExt<T extends ShadowBuilder> on T {\n" +
            "  T get customShadow => this\n" +
            "    ..boxShadow = const [\n" +
            "      BoxShadow(\n" +
            "        color: Color(0x78000000),\n" +
            "        offset: Offset(0, 4),\n" +
            "        blurRadius: 4,\n" +
            "      )\n" +
            "    ];\n" +
            "}\n" +
            "\n" +
            "/// You can define the default style which just explain you how to define the style with flutter tailwind\n" +
            "extension DecorationExt<T extends CompleteDecoration> on T {\n" +
            "  /// use flutter tailwind BoxDecoration\n" +
            "  T get decorMain => this..decoration = bd.greenAccent.circle.borderBrown.rounded8.customShadow.border5.mk;\n" +
            "\n" +
            "  /// Use flutter normal style to describe BoxDecoration\n" +
            "  T get decorTradition => this\n" +
            "    ..decoration = BoxDecoration(\n" +
            "        color: Colors.greenAccent,\n" +
            "        border: Border.all(color: Colors.brown, width: 5.r),\n" +
            "        borderRadius: BorderRadius.circular(8.r),\n" +
            "        boxShadow: const [\n" +
            "          BoxShadow(\n" +
            "            color: Color(0x78000000),\n" +
            "            offset: Offset(0, 4),\n" +
            "            blurRadius: 4,\n" +
            "          ),\n" +
            "        ]);\n" +
            "}"

    val fluterTailWindConstWithoutColor = "import 'package:flutter/material.dart';\n" +
            "import 'package:flutter_tailwind/flutter_tailwind.dart';\n" +
            "\n" +
            "/// define the custom size here\n" +
            "extension SizeExt<T extends SizeBuilder> on T {\n" +
            "  T get h210 => this..height = 210.h;\n" +
            "\n" +
            "  T get w210 => this..width = 210.w;\n" +
            "\n" +
            "  T get s210 => this..size = 210.r;\n" +
            "}\n" +
            "\n" +
            "/// define the custom text feature here\n" +
            "extension TextFeatureExt<T extends TextFeature> on T {\n" +
            "  ///T get fontExample => this..fontFamily = 'font family,just define family here';\n" +
            "}\n\n" +
            "/// define the custom text size here\n" +
            "extension FontSizeExt<T extends FontSizeBuilder> on T {\n" +
            "  ///T get f120 => this..font(120.csp);\n" +
            "}\n\n" +
            "/// define the custom icon here\n" +
            "extension IconExt<T extends IconBuilder> on T {\n" +
            "  ///T get icDefAvatar => this..icon(R.icDefAvatar);\n" +
            "}\n\n" +
            "/// define the custom text style here,text feature just single feature,but style is completed style,can directly use it\n" +
            "extension TextStyleExt<T extends CompletedTextStyleBuilder> on T {\n" +
            "  T get styleMain => this..style = ts.redAccent.f16.bold.underline.mk;\n" +
            "\n" +
            "  /// use flutter tailwind style\n" +
            "  T get styleAccent => this..style = ts.greenAccent.f20.bold.underline.mk;\n" +
            "\n" +
            "  /// use flutter normal style to describe text style\n" +
            "  T get styleTradition => this\n" +
            "    ..style = TextStyle(\n" +
            "      color: Colors.greenAccent,\n" +
            "      fontSize: 20.sp,\n" +
            "      fontWeight: FontWeight.bold,\n" +
            "      decoration: TextDecoration.underline,\n" +
            "    );\n" +
            "}\n" +
            "\n" +
            "/// define the shadow\n" +
            "extension ShadowExt<T extends ShadowBuilder> on T {\n" +
            "  T get customShadow => this\n" +
            "    ..boxShadow = const [\n" +
            "      BoxShadow(\n" +
            "        color: Color(0x78000000),\n" +
            "        offset: Offset(0, 4),\n" +
            "        blurRadius: 4,\n" +
            "      )\n" +
            "    ];\n" +
            "}\n" +
            "\n" +
            "/// You can define the default style which just explain you how to define the style with flutter tailwind\n" +
            "extension DecorationExt<T extends CompleteDecoration> on T {\n" +
            "  /// use flutter tailwind BoxDecoration\n" +
            "  T get decorMain => this..decoration = bd.greenAccent.circle.borderBrown.rounded8.customShadow.border5.mk;\n" +
            "\n" +
            "  /// Use flutter normal style to describe BoxDecoration\n" +
            "  T get decorTradition => this\n" +
            "    ..decoration = BoxDecoration(\n" +
            "        color: Colors.greenAccent,\n" +
            "        border: Border.all(color: Colors.brown, width: 5.r),\n" +
            "        borderRadius: BorderRadius.circular(8.r),\n" +
            "        boxShadow: const [\n" +
            "          BoxShadow(\n" +
            "            color: Color(0x78000000),\n" +
            "            offset: Offset(0, 4),\n" +
            "            blurRadius: 4,\n" +
            "          ),\n" +
            "        ]);\n" +
            "}"
}
