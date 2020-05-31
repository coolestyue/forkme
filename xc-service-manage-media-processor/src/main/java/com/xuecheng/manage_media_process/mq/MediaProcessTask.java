package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessTask {
    //ffmpeg绝对路径
    @Value("${xc‐service‐manage‐media.ffmpeg‐path}")
    String ffmpeg_path;
    //上传文件根目录
    @Value("${xc‐service‐manage‐media.upload‐location}")
    String serverPath;

    @Autowired
    MediaFileRepository mediaFileRepository;
    @RabbitListener(queues="${xc‐service‐manage‐media.mq.queue‐media‐processtask}",
    containerFactory = "customContainerFactory"
    )
    public void receiveMediaProcessTask(String msg){
        Map msgMap = JSON.parseObject(msg,Map.class);
        String mediaId = (String) msgMap.get("mediaId");
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(mediaId);
        if(!mediaFileOptional.isPresent()){
            //返回
        }
        MediaFile mediaFile = mediaFileOptional.get();
        String fileType = mediaFile.getFileType();
        if(fileType==null || !fileType.equals("avi")){
            //无需处理
            mediaFile.setProcessStatus("无需处理");
            mediaFileRepository.save(mediaFile);
            return ;
        }else{
            mediaFile.setProcessStatus("正在处理");
            mediaFileRepository.save(mediaFile);
        }
        //接下来需要生成mp4文件,
        //生成mp4
        String video_path = serverPath + mediaFile.getFilePath()+mediaFile.getFileName();
        String mp4_name = mediaFile.getFileId()+".mp4";
        String mp4folder_path = serverPath + mediaFile.getFilePath();
        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4folder_path);
        String result = videoUtil.generateMp4();
        //生成m3u8
        video_path = serverPath + mediaFile.getFilePath()+mp4_name;//此地址为mp4的地址
        String m3u8_name = mediaFile.getFileId()+".m3u8";
        String m3u8folder_path = serverPath + mediaFile.getFilePath()+"hls/";
        HlsVideoUtil hlsVideoUtil = new
                HlsVideoUtil(ffmpeg_path,video_path,m3u8_name,m3u8folder_path);
        result = hlsVideoUtil.generateM3u8();
        if(result == null || !result.equals("success")){
            //操作失败写入处理日志
            mediaFile.setProcessStatus("303003");//处理状态为处理失败
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return ;
        }
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        mediaFile.setProcessStatus("成果");
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        mediaFile.setFileUrl(mediaFile.getFilePath()+"hls/"+m3u8_name);
        mediaFileRepository.save(mediaFile);
    }
}
