package com.xuecheng.api.media;


import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;

public interface MediaFileControllerApi {
    QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest);
}
