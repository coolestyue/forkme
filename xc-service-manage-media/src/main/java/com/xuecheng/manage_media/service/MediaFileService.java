package com.xuecheng.manage_media.service;


import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaFileService {
    @Autowired
    MediaFileRepository mediaFileRepository;
    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
        if(page<1){
            page = 1;
        }
        page = page -1;
        if(queryMediaFileRequest==null){
            queryMediaFileRequest =  new QueryMediaFileRequest();
        }
        ExampleMatcher matching = ExampleMatcher.matching();
        matching.withMatcher("tag",ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("fileOriginalName",ExampleMatcher.GenericPropertyMatchers.contains());
        MediaFile mediaFile = new MediaFile();
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getTag())){
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())){
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())){
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }
        Example<MediaFile> ex = Example.of(mediaFile, matching);
        Pageable pageable = new PageRequest(page,size);
        Page<MediaFile> mediaFiles = mediaFileRepository.findAll(ex, pageable);
        long totalElements = mediaFiles.getTotalElements();
        List<MediaFile> files = mediaFiles.getContent();
        QueryResult<MediaFile> mediaFileQueryResult = new QueryResult<>();
        mediaFileQueryResult.setList(files);
        mediaFileQueryResult.setTotal(totalElements);

        return new QueryResponseResult(CommonCode.SUCCESS,mediaFileQueryResult);
    }
}
