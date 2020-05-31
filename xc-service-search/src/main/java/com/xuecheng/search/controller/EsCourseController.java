package com.xuecheng.search.controller;

import com.xuecheng.api.search.EsCourseControllerApi;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.search.service.EsCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 **/
@RestController
@RequestMapping("/search/course")
public class EsCourseController implements EsCourseControllerApi {
    @Autowired
    EsCourseService esCourseService;

    @Override
    @GetMapping(value="/list/{page}/{size}")
    public QueryResponseResult<CoursePub> list(@PathVariable("page") int page,@PathVariable("size") int size, CourseSearchParam courseSearchParam) {
        return esCourseService.list(page,size,courseSearchParam);
    }

    @Override
    public Map<String, CoursePub> getall(String courseId) {
        return esCourseService.getall(courseId);
    }

    @Override
    public TeachplanMediaPub getmedia(String teachplanId) {
        String[] ids  = new String[]{teachplanId};
        QueryResponseResult<TeachplanMediaPub> getmedia = esCourseService.getmedia(ids);
        QueryResult<TeachplanMediaPub> queryResult = getmedia.getQueryResult();
        if(queryResult!=null &&queryResult.getList()!=null&& queryResult.getList().size()>0){
            return queryResult.getList().get(0);
        }
        return null;
    }
}
