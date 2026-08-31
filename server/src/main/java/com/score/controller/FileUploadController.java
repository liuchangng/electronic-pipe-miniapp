package com.score.controller;

import com.score.common.R;
import com.score.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 * 使用 x-file-storage 统一存储抽象层
 *
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    @Autowired
    private StorageService storageService;

    /**
     * 上传文件（头像等）
     * 返回完整访问URL（如 http://localhost:8080/uploads/xxx.jpg）
     */
    @PostMapping("/upload")
    public R upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("文件不能为空");
        }

        try {
            String url = storageService.uploadFile(file);
            return R.ok((Object) url);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }
}