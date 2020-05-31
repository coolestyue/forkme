package com.xuecheng.learning.service;

import com.alibaba.druid.util.StringUtils;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.media.response.GetMediaResult;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.client.CourseSearchClient;
import com.xuecheng.learning.dao.XcLearningCourseRepository;
import com.xuecheng.learning.dao.XcTaskHisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class LearningService {
    @Autowired
    CourseSearchClient courseSearchClient;
    @Autowired
    XcTaskHisRepository xcTaskHisRepository;
    @Autowired
    XcLearningCourseRepository xcLearningCourseRepository;
    public GetMediaResult getmedia(String courseId,String teachplanId){
        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getmedia(teachplanId);
                if(teachplanMediaPub == null || StringUtils.isEmpty(teachplanMediaPub.getMediaUrl())){
            //获取视频播放地址出错
           // ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        return new GetMediaResult(CommonCode.SUCCESS,teachplanMediaPub.getMediaUrl());

    }
    @Transactional
    public ResponseResult addCourse(String courseId, String userId, String valid, Date
            startTime, Date endTime, XcTask xcTask){
        XcLearningCourse byCourseIdAndUserId = xcLearningCourseRepository.findByCourseIdAndUserId(courseId, userId);
        if(byCourseIdAndUserId!=null){
            //更新
        }else{
            //没有记录就添加新的记录
            XcLearningCourse xcLearningCourse = new XcLearningCourse();
            //bulabula
        }

        Optional<XcTaskHis> byId = xcTaskHisRepository.findById(xcTask.getId());
        if(byId.isPresent()){

        }

    }
}
