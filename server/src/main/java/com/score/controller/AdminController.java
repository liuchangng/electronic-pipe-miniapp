package com.score.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.score.common.R;
import com.score.dto.PageResult;
import com.score.entity.*;
import com.score.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 管理后台控制器
 *
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SongService songService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConfigService configService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private VideoTranscodeService transcodeService;

    @Autowired
    private SongRequestService songRequestService;

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private CommentService commentService;

    // ==================== 登录 ====================

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public R login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        if (username == null || password == null) {
            return R.fail("用户名和密码不能为空");
        }

        Admin admin = adminService.login(username, password);
        if (admin == null) {
            return R.fail("用户名或密码错误");
        }

        // Sa-Token 登录，使用 admin_ 前缀区分
        StpUtil.login("admin_" + admin.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        result.put("adminInfo", Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "nickname", admin.getNickname(),
                "role", admin.getRole()
        ));

        return R.ok(result);
    }

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/info")
    public R getAdminInfo() {
        String loginId = StpUtil.getLoginIdAsString();
        if (!loginId.startsWith("admin_")) {
            return R.fail("非管理员账号");
        }
        Long adminId = Long.parseLong(loginId.substring(6));
        Admin admin = adminService.getById(adminId);
        if (admin == null) {
            return R.fail("管理员不存在");
        }
        return R.ok(Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "nickname", admin.getNickname(),
                "role", admin.getRole()
        ));
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public R logout() {
        StpUtil.logout();
        return R.ok("退出成功");
    }

    // ==================== 仪表盘统计 ====================

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public R getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("songCount", songService.count());
            stats.put("videoCount", videoService.count());
            stats.put("userCount", userService.count());
            stats.put("bannerCount", bannerService.count());
            stats.put("totalViewCount", songService.totalViewCount());
            stats.put("totalPlayCount", videoService.totalPlayCount());
            return R.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("统计查询失败: " + e.getMessage());
        }
    }

    // ==================== 曲谱管理 ====================

    /**
     * 获取曲谱列表（管理端，含全部状态，分页）
     */
    @GetMapping("/song/list")
    public R<PageResult<Song>> getSongList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            PageResult<Song> result = songService.adminPageList(keyword, category, status, page, pageSize);
            storageService.fillSongUrls(result.getList());
            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取曲谱详情
     */
    @GetMapping("/song/detail/{id}")
    public R<Song> getSongDetail(@PathVariable Long id) {
        Song song = songService.getById(id);
        if (song == null) {
            return R.fail("曲谱不存在");
        }
        storageService.fillSongUrls(song);
        return R.ok(song);
    }

    /**
     * 新增曲谱
     */
    @PostMapping("/song/add")
    public R addSong(@RequestBody Song song) {
        songService.addSong(song);
        return R.ok("新增成功");
    }

    /**
     * 更新曲谱
     */
    @PostMapping("/song/update")
    public R updateSong(@RequestBody Song song) {
        songService.updateSong(song);
        return R.ok("更新成功");
    }

    /**
     * 删除曲谱（逻辑删除）
     */
    @PostMapping("/song/delete/{id}")
    public R deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return R.ok("删除成功");
    }

    // ==================== 视频管理 ====================

    /**
     * 获取视频列表（管理端，分页）
     */
    @GetMapping("/video/list")
    public R<PageResult<Video>> getVideoList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long songId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<Video> result = videoService.adminPageList(keyword, songId, page, pageSize);
        storageService.fillVideoUrls(result.getList());
        return R.ok(result);
    }

    /**
     * 获取视频详情（管理端，不增加播放计数）
     */
    @GetMapping("/video/detail/{id}")
    public R<Video> getVideoDetail(@PathVariable Long id) {
        Video video = videoService.getVideoDetail(id);
        if (video == null) {
            return R.fail("视频不存在");
        }
        storageService.fillVideoUrls(video);
        return R.ok(video);
    }

    /**
     * 新增视频
     */
    @PostMapping("/video/add")
    public R addVideo(@RequestBody Video video) {
        videoService.addVideo(video);
        return R.ok("新增成功");
    }

    /**
     * 更新视频
     */
    @PostMapping("/video/update")
    public R updateVideo(@RequestBody Video video) {
        videoService.updateVideo(video);
        return R.ok("更新成功");
    }

    /**
     * 删除视频（逻辑删除）
     */
    @PostMapping("/video/delete/{id}")
    public R deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return R.ok("删除成功");
    }

    /**
     * 上传视频文件并异步转码为HLS
     * 使用 x-file-storage 统一存储，返回完整URL
     */
    @PostMapping("/video/upload")
    public R uploadVideo(@RequestParam("file") MultipartFile file,
                         @RequestParam(required = false) String title,
                         @RequestParam(required = false) Long songId) {
        if (file.isEmpty()) {
            return R.fail("文件不能为空");
        }
        try {
            // 使用 StorageService 上传文件，获取完整URL
            String videoUrl = storageService.uploadFile(file, "videos/");

            // 获取本地路径用于转码
            String localPath = storageService.toLocalPath(videoUrl);

            // 创建视频记录
            Video video = Video.builder()
                    .title(title != null ? title : (file.getOriginalFilename() != null ? file.getOriginalFilename() : "未命名视频"))
                    .songId(songId)
                    .videoUrl(videoUrl)  // 完整URL
                    .duration(0)
                    .transcodeStatus("pending")
                    .sortOrder(0)
                    .deleted(0)
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();
            videoService.addVideo(video);

            // 提交转码任务（数据库驱动串行队列）
            transcodeService.submitTranscode(video.getId(), localPath);

            Map<String, Object> result = new HashMap<>();
            result.put("id", video.getId());
            result.put("title", video.getTitle());
            result.put("status", "pending");
            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("视频上传失败: " + e.getMessage());
        }
    }

    /**
     * 分片上传：上传单个分片
     * 分片为临时文件，仍使用本地存储，合并后通过StorageService获取完整URL
     */
    @PostMapping("/video/upload-chunk")
    public R uploadChunk(@RequestParam("file") MultipartFile file,
                         @RequestParam("uploadId") String uploadId,
                         @RequestParam("chunkIndex") int chunkIndex,
                         @RequestParam("totalChunks") int totalChunks) {
        if (file.isEmpty()) {
            return R.fail("分片文件不能为空");
        }
        try {
            // 分片临时文件保存在 chunks/ 子目录
            String subPath = "chunks/" + uploadId + "/" + String.format("%08d", chunkIndex);
            String localPath = storageService.saveToLocal(file.getInputStream(), subPath);

            // 统计已接收分片数
            Path chunkDir = Paths.get(localPath).getParent();
            int received = chunkDir.toFile().listFiles() != null ? chunkDir.toFile().listFiles().length : 0;

            Map<String, Object> result = new HashMap<>();
            result.put("uploadId", uploadId);
            result.put("chunkIndex", chunkIndex);
            result.put("received", received);
            result.put("totalChunks", totalChunks);
            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("分片上传失败: " + e.getMessage());
        }
    }

    /**
     * 分片上传：合并分片并触发转码
     * 合并后通过StorageService获取完整URL
     */
    @PostMapping("/video/merge-chunks")
    public R mergeChunks(@RequestBody Map<String, Object> params) {
        String uploadId = (String) params.get("uploadId");
        String fileName = (String) params.get("fileName");
        Integer totalChunks = (Integer) params.get("totalChunks");
        String title = (String) params.get("title");
        Long songId = params.get("songId") != null ? Long.valueOf(params.get("songId").toString()) : null;

        if (uploadId == null || fileName == null || totalChunks == null) {
            return R.fail("参数不完整");
        }

        try {
            // 分片目录
            Path chunkDir = Paths.get(storageService.getLocalStoragePath()).resolve("chunks").resolve(uploadId);
            if (!Files.exists(chunkDir)) {
                return R.fail("分片目录不存在");
            }

            // 检查分片是否全部上传
            String[] chunks = chunkDir.toFile().list();
            if (chunks == null || chunks.length < totalChunks) {
                return R.fail("分片未全部上传，已收到" + (chunks != null ? chunks.length : 0) + "/" + totalChunks);
            }

            // 合并分片到临时文件
            String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : ".mp4";
            String mergedFileName = UUID.randomUUID().toString().replace("-", "") + ext;
            String mergedSubPath = "videos/" + mergedFileName;
            Path mergedFile = Paths.get(storageService.getLocalStoragePath()).resolve("videos").resolve(mergedFileName);
            Files.createDirectories(mergedFile.getParent());

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(mergedFile.toFile())) {
                for (int i = 0; i < totalChunks; i++) {
                    Path chunk = chunkDir.resolve(String.format("%08d", i));
                    Files.copy(chunk, fos);
                }
            }

            // 删除分片临时目录
            try {
                for (File f : chunkDir.toFile().listFiles()) {
                    f.delete();
                }
                Files.delete(chunkDir);
            } catch (Exception ignored) {}

            // 生成完整URL
            String videoUrl = storageService.getFullUrl("/uploads/" + mergedSubPath);

            // 创建视频记录
            Video video = Video.builder()
                    .title(title != null ? title : fileName)
                    .songId(songId)
                    .videoUrl(videoUrl)  // 完整URL
                    .duration(0)
                    .transcodeStatus("pending")
                    .sortOrder(0)
                    .deleted(0)
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();
            videoService.addVideo(video);

            // 提交转码任务（数据库驱动串行队列）
            transcodeService.submitTranscode(video.getId(), mergedFile.toString());

            Map<String, Object> result = new HashMap<>();
            result.put("id", video.getId());
            result.put("title", video.getTitle());
            result.put("status", "pending");
            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("合并分片失败: " + e.getMessage());
        }
    }

    /**
     * 获取视频转码状态
     */
    @GetMapping("/video/transcode-status/{id}")
    public R getTranscodeStatus(@PathVariable Long id) {
        Video video = videoService.getVideoDetail(id);
        if (video == null) {
            return R.fail("视频不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("status", video.getTranscodeStatus() != null ? video.getTranscodeStatus() : "none");
        result.put("error", video.getTranscodeError());
        return R.ok(result);
    }

    /**
     * 重试视频转码
     */
    @PostMapping("/video/retry-transcode/{id}")
    public R retryTranscode(@PathVariable Long id) {
        Video video = videoService.getVideoDetail(id);
        if (video == null) {
            return R.fail("视频不存在");
        }
        if (!"failed".equals(video.getTranscodeStatus())) {
            return R.fail("只有转码失败的视频才能重试");
        }
        // 重置状态为pending，重新提交转码
        String localPath = storageService.toLocalPath(video.getVideoUrl());
        transcodeService.submitTranscode(id, localPath);
        return R.ok("已重新提交转码");
    }

    /**
     * 获取曲谱下拉列表（用于视频关联）
     */
    @GetMapping("/song/select-list")
    public R getSongSelectList() {
        List<Song> songs = songService.listAll();
        List<Map<String, Object>> selectList = new java.util.ArrayList<>();
        for (Song song : songs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", song.getId());
            item.put("title", song.getTitle());
            item.put("author", song.getAuthor());
            selectList.add(item);
        }
        return R.ok(selectList);
    }

    // ==================== Banner管理 ====================

    /**
     * 获取Banner列表（管理端，分页）
     */
    @GetMapping("/banner/list")
    public R<PageResult<Banner>> getBannerList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<Banner> result = bannerService.adminPageList(page, pageSize);
        storageService.fillBannerUrls(result.getList());
        return R.ok(result);
    }

    /**
     * 新增Banner
     */
    @PostMapping("/banner/add")
    public R addBanner(@RequestBody Banner banner) {
        bannerService.addBanner(banner);
        return R.ok("新增成功");
    }

    /**
     * 更新Banner
     */
    @PostMapping("/banner/update")
    public R updateBanner(@RequestBody Banner banner) {
        bannerService.updateBanner(banner);
        return R.ok("更新成功");
    }

    /**
     * 删除Banner（逻辑删除）
     */
    @PostMapping("/banner/delete/{id}")
    public R deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return R.ok("删除成功");
    }

    // ==================== 用户管理 ====================

    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/user/list")
    public R<PageResult<User>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<User> result = userService.adminPageList(keyword, page, pageSize);
        if (result.getList() != null) {
            result.getList().forEach(u -> storageService.fillUserUrls(u));
        }
        return R.ok(result);
    }

    /**
     * 更新用户状态
     */
    @PostMapping("/user/updateStatus")
    public R updateUserStatus(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("id").toString());
        Integer status = Integer.valueOf(request.get("status").toString());
        userService.updateStatus(userId, status);
        return R.ok("更新成功");
    }

    // ==================== 系统配置 ====================

    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public R getConfig() {
        return R.ok(configService.getAllConfigMap());
    }

    /**
     * 更新系统配置
     */
    @PostMapping("/config")
    public R updateConfig(@RequestBody Map<String, String> configMap) {
        configService.batchSetConfig(configMap);
        return R.ok("配置更新成功");
    }

    // ==================== 求谱管理 ====================

    /**
     * 获取求谱列表（管理端，支持状态筛选，分页）
     */
    @GetMapping("/song-request/list")
    public R<PageResult<SongRequest>> getSongRequestList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<SongRequest> result = songRequestService.getAllRequests(status, page, size);
        return R.ok(result);
    }

    /**
     * 处理求谱申请
     */
    @PostMapping("/song-request/handle")
    public R handleSongRequest(@RequestBody Map<String, Object> request) {
        Long id = Long.valueOf(request.get("id").toString());
        String status = (String) request.get("status");
        String reply = (String) request.get("reply");

        if (id == null) {
            return R.fail("求谱ID不能为空");
        }
        if (status == null || (!status.equals("accepted") && !status.equals("rejected"))) {
            return R.fail("状态参数无效");
        }

        songRequestService.handleRequest(id, status, reply);
        return R.ok("处理成功");
    }

    // ==================== 知识管理 ====================

    /**
     * 获取知识列表（分页，支持分类筛选）
     */
    @GetMapping("/knowledge/list")
    public R<PageResult<Knowledge>> getKnowledgeList(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<Knowledge> result = knowledgeService.adminPageList(category, page, pageSize);
        return R.ok(result);
    }

    /**
     * 新增知识
     */
    @PostMapping("/knowledge/add")
    public R addKnowledge(@RequestBody Knowledge knowledge) {
        knowledgeService.addKnowledge(knowledge);
        return R.ok("新增成功");
    }

    /**
     * 更新知识
     */
    @PostMapping("/knowledge/update")
    public R updateKnowledge(@RequestBody Knowledge knowledge) {
        knowledgeService.updateKnowledge(knowledge);
        return R.ok("更新成功");
    }

    /**
     * 删除知识（逻辑删除）
     */
    @PostMapping("/knowledge/delete/{id}")
    public R deleteKnowledge(@PathVariable Long id) {
        knowledgeService.deleteKnowledge(id);
        return R.ok("删除成功");
    }

    // ==================== 评论管理 ====================

    /**
     * 获取评论列表（分页，支持状态筛选）
     */
    @GetMapping("/comment/list")
    public R<Map<String, Object>> getCommentList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<Comment> comments = commentService.getAllComments(status, page, pageSize);
        long total = commentService.getAllCommentCount(status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", comments);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return R.ok(result);
    }

    /**
     * 审核评论
     */
    @PostMapping("/comment/review")
    public R reviewComment(@RequestBody Map<String, Object> request) {
        Long id = Long.valueOf(request.get("id").toString());
        String status = (String) request.get("status");
        if (id == null || status == null) {
            return R.fail("参数不能为空");
        }
        commentService.reviewComment(id, status);
        return R.ok("审核成功");
    }

    /**
     * 删除评论
     */
    @PostMapping("/comment/delete/{id}")
    public R deleteComment(@PathVariable Long id) {
        commentService.adminDeleteComment(id);
        return R.ok("删除成功");
    }

    // ==================== 互动记录管理 ====================

    @Autowired
    private VideoLikeService videoLikeService;

    @Autowired
    private VideoFavoriteService videoFavoriteService;

    @Autowired
    private VideoShareService videoShareService;

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 获取视频的点赞记录（分页，降序）
     */
    @GetMapping("/video/like/list")
    public R<Map<String, Object>> getVideoLikeList(
            @RequestParam Long videoId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<User> users = videoLikeService.getLikeUsers(videoId);
        // 手动分页
        int total = users.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<User> pagedUsers = fromIndex < total ? users.subList(fromIndex, toIndex) : java.util.Collections.emptyList();
        
        List<Map<String, Object>> list = pagedUsers.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("nickname", u.getNickname());
            m.put("avatar", u.getAvatar());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).collect(java.util.stream.Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return R.ok(result);
    }

    /**
     * 获取视频的收藏记录（分页，降序）
     */
    @GetMapping("/video/favorite/list")
    public R<Map<String, Object>> getVideoFavoriteList(
            @RequestParam Long videoId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<User> users = videoFavoriteService.getFavoriteUsers(videoId);
        int total = users.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<User> pagedUsers = fromIndex < total ? users.subList(fromIndex, toIndex) : java.util.Collections.emptyList();
        
        List<Map<String, Object>> list = pagedUsers.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("nickname", u.getNickname());
            m.put("avatar", u.getAvatar());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).collect(java.util.stream.Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return R.ok(result);
    }

    /**
     * 获取视频的分享记录（分页，降序）
     */
    @GetMapping("/video/share/list")
    public R<Map<String, Object>> getVideoShareList(
            @RequestParam Long videoId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<VideoShare> shares = videoShareService.getShareList(videoId);
        int total = shares.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<VideoShare> pagedShares = fromIndex < total ? shares.subList(fromIndex, toIndex) : java.util.Collections.emptyList();
        
        // 获取用户信息
        List<Long> userIds = pagedShares.stream()
                .map(VideoShare::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userService.listByIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }
        
        List<Map<String, Object>> list = pagedShares.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("userId", s.getUserId());
            User u = userMap.get(s.getUserId());
            m.put("nickname", u != null ? u.getNickname() : "匿名用户");
            m.put("avatar", u != null ? u.getAvatar() : "");
            m.put("createdAt", s.getCreatedAt());
            return m;
        }).collect(java.util.stream.Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return R.ok(result);
    }

    /**
     * 获取曲谱的收藏记录（分页，降序）
     */
    @GetMapping("/song/favorite/list")
    public R<Map<String, Object>> getSongFavoriteList(
            @RequestParam Long songId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<User> users = favoriteService.getFavoriteUsers(songId);
        int total = users.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<User> pagedUsers = fromIndex < total ? users.subList(fromIndex, toIndex) : java.util.Collections.emptyList();
        
        List<Map<String, Object>> list = pagedUsers.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("nickname", u.getNickname());
            m.put("avatar", u.getAvatar());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).collect(java.util.stream.Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return R.ok(result);
    }
}