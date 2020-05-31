package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    @Autowired
    XcTaskRepository xcTaskRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;
    public List<XcTask> findTaskList(Date updateTime , int n){

        Pageable pageable = new PageRequest(0,n);
        Page<XcTask> byUpdateTimeBefore = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        List<XcTask> content = byUpdateTimeBefore.getContent();
        return  content;


    }
    @Transactional
    public void publish(XcTask xcTask,String ex,String routingKey){
        Optional<XcTask> byId = xcTaskRepository.findById(xcTask.getId());
        if(byId.isPresent()){
            XcTask xcTask1 = byId.get();
            rabbitTemplate.convertAndSend(ex,routingKey,xcTask1);
            //更新任务时间为当前时间
            xcTask.setUpdateTime(new  Date());
            xcTaskRepository.save(xcTask);
        }
    }
    @Transactional
    public int getTask(String id,int version){
        int i = xcTaskRepository.updateVersion(id, version);
        return i;

    }

}
