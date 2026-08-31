package com.score.service;

import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.score.entity.Banner;
import com.score.entity.Song;
import com.score.entity.User;
import com.score.entity.Video;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * 统一文件存储服务
 * 封装 x-file-storage，提供文件上传、URL生成等能力
 * 当前使用本地存储，后续切换MinIO/rustfs只需修改配置
 *
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /** 存储访问域名，从 x-file-storage 配置中读取，如 http://localhost:8080/uploads/ */
    @Value("${dromara.x-file-storage.local-plus[0].domain:}")
    private String storageDomain;

    /**
     * 上传文件（图片、头像等），返回完整访问URL
     *
     * @param file 上传的文件
     * @param path 存储子路径，如 "images/"、"avatars/"
     * @return 完整访问URL，如 http://localhost:8080/uploads/images/xxx.jpg
     */
    public String uploadFile(MultipartFile file, String path) {
        FileInfo fileInfo = fileStorageService.of(file)
                .setPath(path)
                .upload();
        if (fileInfo == null) {
            throw new RuntimeException("文件上传失败");
        }
        log.info("文件上传成功: url={}", fileInfo.getUrl());
        return fileInfo.getUrl();
    }

    /**
     * 上传文件到根路径，返回完整访问URL
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, "");
    }

    /**
     * 根据相对路径生成完整访问URL
     * 用于非x-file-storage上传的文件（如FFmpeg生成的HLS文件）
     *
     * @param relativePath 相对路径，如 /uploads/hls/1/index.m3u8 或 hls/1/index.m3u8
     * @return 完整访问URL
     */
    public String getFullUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }
        // 已经是完整URL则直接返回
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        // 去掉前导 /uploads/ 前缀，因为 domain 已包含
        String path = relativePath;
        if (path.startsWith("/uploads/")) {
            path = path.substring("/uploads/".length());
        } else if (path.startsWith("/uploads")) {
            path = path.substring("/uploads".length());
        }
        // 去掉前导斜杠，避免 domain 末尾斜杠与 path 前导斜杠重复
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        // 使用配置的 domain
        if (storageDomain != null && !storageDomain.isEmpty()) {
            return storageDomain + path;
        }
        // fallback: 拼接相对路径
        return "/uploads/" + path;
    }

    /**
     * 获取本地存储根路径（绝对路径）
     * 用于视频转码等需要本地文件路径的场景
     */
    public String getLocalStoragePath() {
        return Paths.get(uploadPath).toAbsolutePath().normalize().toString();
    }

    /**
     * 将完整URL或相对路径转换为本地绝对路径
     * 用于视频转码等需要读取本地文件的场景
     *
     * @param urlOrPath 完整URL或相对路径，如 http://localhost:8080/uploads/videos/xxx.mp4 或 /uploads/videos/xxx.mp4
     * @return 本地绝对路径
     */
    public String toLocalPath(String urlOrPath) {
        if (urlOrPath == null || urlOrPath.isEmpty()) {
            return "";
        }
        String path = urlOrPath;
        // 如果是完整URL，去掉domain部分
        if (path.startsWith("http://") || path.startsWith("https://")) {
            // 找到 /uploads/ 部分
            int idx = path.indexOf("/uploads/");
            if (idx >= 0) {
                path = path.substring(idx + "/uploads/".length());
            } else {
                idx = path.indexOf("/uploads");
                if (idx >= 0) {
                    path = path.substring(idx + "/uploads".length());
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }
                }
            }
        } else if (path.startsWith("/uploads/")) {
            path = path.substring("/uploads/".length());
        } else if (path.startsWith("/uploads")) {
            path = path.substring("/uploads".length());
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
        }
        return Paths.get(uploadPath).toAbsolutePath().normalize().resolve(path).toString();
    }

    /**
     * 保存输入流到本地指定子路径（用于分片合并等临时场景）
     * 合并后的文件仍需通过 x-file-storage 上传以获取完整URL
     *
     * @param is 输入流
     * @param subPath 子路径，如 videos/xxx.mp4
     * @return 本地文件路径
     */
    public String saveToLocal(InputStream is, String subPath) {
        try {
            Path filePath = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(subPath);
            Files.createDirectories(filePath.getParent());
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (Exception e) {
            throw new RuntimeException("保存文件到本地失败: " + e.getMessage(), e);
        }
    }

    /**
     * 填充Song实体的URL字段为完整URL
     */
    public void fillSongUrls(Song song) {
        if (song == null) return;
        song.setImageUrl(getFullUrl(song.getImageUrl()));
        song.setScoreImageUrl(getFullUrl(song.getScoreImageUrl()));
        song.setVideoUrl(getFullUrl(song.getVideoUrl()));
    }

    /**
     * 填充Song列表的URL字段
     */
    public void fillSongUrls(List<Song> songs) {
        if (songs == null) return;
        songs.forEach(this::fillSongUrls);
    }

    /**
     * 填充Video实体的URL字段为完整URL
     */
    public void fillVideoUrls(Video video) {
        if (video == null) return;
        video.setVideoUrl(getFullUrl(video.getVideoUrl()));
        video.setThumbnailUrl(getFullUrl(video.getThumbnailUrl()));
    }

    /**
     * 填充Video列表的URL字段
     */
    public void fillVideoUrls(List<Video> videos) {
        if (videos == null) return;
        videos.forEach(this::fillVideoUrls);
    }

    /**
     * 填充Banner实体的URL字段为完整URL
     */
    public void fillBannerUrls(Banner banner) {
        if (banner == null) return;
        banner.setImageUrl(getFullUrl(banner.getImageUrl()));
        banner.setLinkUrl(getFullUrl(banner.getLinkUrl()));
    }

    /**
     * 填充Banner列表的URL字段
     */
    public void fillBannerUrls(List<Banner> banners) {
        if (banners == null) return;
        banners.forEach(this::fillBannerUrls);
    }

    /**
     * 填充User实体的URL字段为完整URL
     */
    public void fillUserUrls(User user) {
        if (user == null) return;
        user.setAvatar(getFullUrl(user.getAvatar()));
    }
}