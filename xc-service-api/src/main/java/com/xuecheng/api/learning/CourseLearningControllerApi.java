package com.xuecheng.api.learning;

import com.xuecheng.framework.domain.media.response.GetMediaResult;

public interface CourseLearningControllerApi {
    GetMediaResult getmedia(String courseId,String teachplanId);
}
