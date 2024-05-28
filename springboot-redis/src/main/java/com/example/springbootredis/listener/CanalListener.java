package com.example.springbootredis.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import org.springframework.stereotype.Service;



public class CanalListener {
//    @CanalListener(databaseName = "open_md",tableName = "o_user")
//    public void consumerUser(User user, CanalEntry.EventType eventType){
//        log.info("user={}，操作类型={}",JSON.toJSONString(user),eventType.name());
//
//        boolean b = userRepository.existsById(user.getId());
//        switch (eventType){
//            case INSERT:
//            case UPDATE:
//                System.out.println("=========正在添加或者修改文档===========");
//                userRepository.save(user);
//                break;
//            case DELETE:
//                System.out.println("=========正在删除文档===========");
//                if(b){
//                    userRepository.delete(user);
//                }
//                break;
//            default:
//                System.out.println("=========操作类型不匹配===========");
//                System.out.println(user);
//        }
//    }
}
