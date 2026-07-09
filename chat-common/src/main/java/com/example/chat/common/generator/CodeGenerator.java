//package com.example.chat.common.generator;
//
//import com.baomidou.mybatisplus.generator.FastAutoGenerator;
//import com.baomidou.mybatisplus.generator.config.OutputFile;
//import com.baomidou.mybatisplus.generator.config.rules.DateType;
//
//import java.util.Collections;
//
//public class CodeGenerator {
//
//    public static void main(String[] args) {
//        FastAutoGenerator.create(
//                "jdbc:mysql://localhost:3306/elovchat?useUnicode=true&useSSL=false&characterEncoding=utf8",
//                "root",
//                "123456"
//        )
//                .globalConfig(builder -> builder
//                        .outputDir("D:\\αchat\\chat-common\\src\\main\\java")
//                        .author("")
//                        .dateType(DateType.TIME_PACK)
//                        .disableOpenDir()
//                )
//                .packageConfig(builder -> builder
//                        .parent("com.example.chat.common")
//                        .entity("entity")
//                        .mapper("mapper")
//                        .service("service")
//                        .serviceImpl("service.impl")
//                        .pathInfo(Collections.singletonMap(
//                                OutputFile.xml,
//                                "D:\\αchat\\chat-common\\src\\main\\resources\\mapper"
//                        ))
//                )
//                .strategyConfig(builder -> builder
//                        .addInclude(
//                                "user",
//                                "friend_request",
//                                "friendship",
//                                "group_info",
//                                "group_member",
//                                "group_msg",
//                                "private_msg",
//                                "third_party_user",
//                                "user_space_post",
//                                "user_sticker",
//                                "voice_call_record"
//                        )
//                        .entityBuilder()
//                        .enableLombok()
//                        .enableTableFieldAnnotation()
//                        .enableFileOverride()
//                        .mapperBuilder()
//                        .enableBaseResultMap()
//                        .enableBaseColumnList()
//                        .enableFileOverride()
//                        .serviceBuilder()
//                        .enableFileOverride()
//                        .controllerBuilder()
//                        .disable()
//                )
//                .execute();
//    }
//}
