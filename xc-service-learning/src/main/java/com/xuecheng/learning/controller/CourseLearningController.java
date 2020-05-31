package com.xuecheng.learning.controller;

import com.xuecheng.api.learning.CourseLearningControllerApi;
import com.xuecheng.framework.domain.media.response.GetMediaResult;
import com.xuecheng.learning.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;

public class CourseLearningController implements CourseLearningControllerApi {
    @Autowired
    LearningService learningService;


    @Override
    public GetMediaResult getmedia(String courseId, String teachplanId) {
        return learningService.getmedia(courseId,teachplanId);
    }
}
