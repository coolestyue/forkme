package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MyServiceTest {
    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String upload_location;
    private String getFileFolderPath(String fileMd5) {
        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/";
    }
    private String getFilePath(String fileMd5,String fileExt) {
        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileMd5+"."+fileExt;
    }
    private String getChunkFileFolderPath(String fileMd5) {
        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/chunk/";
    }

    public ResponseResult regist(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt){
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        String filePath = this.getFilePath(fileMd5,fileExt);
        File file = new File(filePath);
        boolean fileIsExists = file.exists();
        Optional<MediaFile> byId = mediaFileRepository.findById(fileMd5);
        if(fileIsExists && byId.isPresent()){
            //文件已经存在 抛出异常
//            ExceptionCast.cast();
        }
        File fileFolder = new File(fileFolderPath);
        if(!fileFolder.exists()){
            fileFolder.mkdirs();
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize){
        String chunkfolder = this.getChunkFileFolderPath(fileMd5);
        String chunkFilePath =   chunkfolder+chunk;
        File chunkfile = new File(chunkFilePath);
        if(chunkfile.exists()){
            //块文件存在
            return new CheckChunkResult(CommonCode.SUCCESS,true);
        }else{
            //块文件不存在
            return new CheckChunkResult(CommonCode.SUCCESS,false);
        }
    }
    //上传块文件
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk){
        //上传过来的块文件存放路径
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if(!chunkFileFolder.exists()){
             chunkFileFolder.mkdirs();
        }
        String chunkFilePath = chunkFileFolderPath+chunk;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream= file.getInputStream();
            fileOutputStream =  new FileOutputStream(chunkFilePath);
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //合并块文件
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt){
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        File[] files = chunkFileFolder.listFiles();
        List<File> filesList = Arrays.asList(files);
        //合并后的文件路径
        String filePath  = this.getFilePath(fileMd5,fileExt);
        File mergeFile = new File(filePath);
        mergeFile = this.mergeFile(filesList,mergeFile);
        if(mergeFile==null){
            //执行合并失败 抛出异常
        }
        //检验合并后的md5值和前端传过来的是否一致
       boolean checkFileMd5 = this.checkMd5(mergeFile,fileMd5);
        if(!checkFileMd5){
            //校验文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //3、将文件的信息写入mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5 + "." +fileExt);
        //文件路径保存相对路径
        String filePath1 = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" + fileMd5 + "." +fileExt;
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    private boolean checkMd5(File mergeFile, String fileMd5) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(mergeFile);
            String s = IOUtils.toString(fileInputStream, "utf-8");
            String md5Hex = DigestUtils.md5Hex(s);
            if(fileMd5.equalsIgnoreCase(md5Hex)){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private File mergeFile(List<File> filesList, File mergeFile) {
        try {
            if(mergeFile.exists()){
                mergeFile.delete();
            }else {
                mergeFile.createNewFile();
            }
            Collections.sort(filesList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if(Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                        return 1;
                    }
                    return -1;
                }
            });
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
            byte[] b = new byte[1024];
            for(File chunkFile:filesList){
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                while((len = raf_read.read(b))!=-1){
                    raf_write.write(b,0,len);
                }
                raf_read.close();
            }
            raf_write.close();
            return mergeFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


    }
}
