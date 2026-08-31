package com.score.service;

import com.score.entity.Video;
import com.score.mapper.VideoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 视频转码服务 - 数据库驱动的串行转码队列
 * 从数据库查询pending状态的视频，逐个串行转码
 * 使用 StorageService 生成完整URL
 *
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class VideoTranscodeService {

    private static final Logger log = LoggerFactory.getLogger(VideoTranscodeService.class);

    @Value("${video.transcode.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoService videoService;

    @Autowired
    private StorageService storageService;

    /** 单线程执行器，保证串行转码 */
    private final ExecutorService transcodeExecutor = Executors.newSingleThreadExecutor();

    /** 是否已提交轮询任务 */
    private volatile boolean pollingStarted = false;

    /** 服务运行标志，用于优雅停机 */
    private volatile boolean running = true;

    /** 当前正在运行的FFmpeg进程（用于优雅停机时销毁） */
    private volatile Process currentProcess;

    /**
     * 服务启动时自动开始轮询，处理重启前遗留的pending视频
     */
    @PostConstruct
    public void init() {
        startPollingIfNeeded();
        log.info("视频转码服务已启动，轮询线程已激活");
    }

    /**
     * 优雅停机：停止轮询，中断当前FFmpeg进程，等待转码任务完成
     */
    @PreDestroy
    public void shutdown() {
        log.info("视频转码服务开始优雅停机...");
        running = false;

        // 销毁当前正在运行的FFmpeg进程
        Process proc = currentProcess;
        if (proc != null && proc.isAlive()) {
            log.info("销毁正在运行的FFmpeg进程...");
            proc.destroyForcibly();
        }

        // 关闭执行器并等待任务完成
        transcodeExecutor.shutdown();
        try {
            if (!transcodeExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("转码执行器未在30秒内终止，强制关闭");
                transcodeExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            transcodeExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("视频转码服务已停机");
    }

    /**
     * 提交转码请求：将视频状态设为pending，触发队列轮询
     */
    public void submitTranscode(Long videoId, String sourcePath) {
        // 更新状态为pending
        Video video = videoMapper.selectOneById(videoId);
        if (video != null) {
            video.setTranscodeStatus("pending");
            video.setTranscodeError(null);
            video.setUpdatedAt(java.time.LocalDateTime.now());
            videoMapper.update(video);
        }
        // 确保轮询任务已启动
        startPollingIfNeeded();
        log.info("视频转码已提交: videoId={}, sourcePath={}", videoId, sourcePath);
    }

    /**
     * 启动轮询任务（仅启动一次）
     */
    private synchronized void startPollingIfNeeded() {
        if (!pollingStarted) {
            pollingStarted = true;
            transcodeExecutor.submit(this::pollAndProcess);
        }
    }

    /**
     * 轮询数据库，串行处理pending的视频
     */
    private void pollAndProcess() {
        while (running) {
            try {
                // 查询最早的pending视频
                List<Video> pendingVideos = videoService.listPendingTranscode();
                if (pendingVideos.isEmpty()) {
                    // 没有待处理的，分批等待（每100ms检查一次running标志，便于快速响应停机）
                    for (int i = 0; i < 50 && running; i++) {
                        Thread.sleep(100);
                    }
                    continue;
                }

                // 串行处理每个视频
                for (Video video : pendingVideos) {
                    if (!running) break;
                    try {
                        processOne(video);
                    } catch (Exception e) {
                        log.error("转码失败: videoId={}, error={}", video.getId(), e.getMessage(), e);
                        updateTranscodeStatus(video.getId(), "failed", e.getMessage());
                    }
                }
            } catch (InterruptedException e) {
                log.info("转码轮询线程被中断，退出");
                break;
            } catch (Exception e) {
                log.error("轮询异常: {}", e.getMessage(), e);
                try {
                    for (int i = 0; i < 50 && running; i++) {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException ignored) { break; }
            }
        }
    }

    /**
     * 处理单个视频转码
     */
    private void processOne(Video video) throws Exception {
        String videoUrl = video.getVideoUrl();
        if (videoUrl == null || videoUrl.isEmpty()) {
            updateTranscodeStatus(video.getId(), "failed", "视频URL为空");
            return;
        }

        // 将完整URL或相对路径转换为本地文件路径
        String localPath = storageService.toLocalPath(videoUrl);

        if (!Files.exists(Path.of(localPath))) {
            updateTranscodeStatus(video.getId(), "failed", "源文件不存在: " + localPath);
            return;
        }

        // 更新状态为processing
        updateTranscodeStatus(video.getId(), "processing", null);

        // 执行FFmpeg转码
        transcodeToHls(video.getId(), localPath);

        // 转码成功
        updateTranscodeStatus(video.getId(), "done", null);
        log.info("视频转码完成: videoId={}", video.getId());
    }

    /**
     * 执行FFmpeg转码为HLS
     */
    private void transcodeToHls(Long videoId, String sourcePath) throws Exception {
        Path source = Paths.get(sourcePath);
        if (!Files.exists(source)) {
            throw new RuntimeException("源文件不存在: " + sourcePath);
        }

        // 创建HLS输出目录（本地路径）
        Path hlsDir = Paths.get(storageService.getLocalStoragePath())
                .resolve("hls").resolve(String.valueOf(videoId));
        Files.createDirectories(hlsDir);

        String m3u8Path = hlsDir.resolve("index.m3u8").toString();
        String tsPattern = hlsDir.resolve("segment_%03d.ts").toString();

        // FFmpeg命令
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", sourcePath,
                "-c:v", "libx264",
                "-c:a", "aac",
                "-f", "hls",
                "-hls_time", "10",
                "-hls_list_size", "0",
                "-hls_segment_filename", tsPattern,
                "-hls_flags", "delete_segments",
                m3u8Path
        );
        pb.redirectErrorStream(true);

        log.info("开始转码: videoId={}, cmd={}", videoId, String.join(" ", pb.command()));

        Process process = pb.start();
        currentProcess = process;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("FFmpeg: {}", line);
            }
        } finally {
            currentProcess = null;
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg转码失败, exitCode=" + exitCode);
        }

        // 获取视频时长
        int duration = getVideoDuration(sourcePath);

        // 使用 StorageService 生成完整URL
        String m3u8Url = storageService.getFullUrl("/uploads/hls/" + videoId + "/index.m3u8");

        // 更新视频记录
        Video video = videoMapper.selectOneById(videoId);
        if (video != null) {
            video.setVideoUrl(m3u8Url);  // 完整URL
            video.setDuration(duration);
            video.setUpdatedAt(java.time.LocalDateTime.now());
            videoMapper.update(video);
        }

        // 转码成功后删除原始文件
        try {
            Files.deleteIfExists(source);
        } catch (Exception e) {
            log.warn("删除原始文件失败: {}", sourcePath);
        }
    }

    /**
     * 获取视频时长（秒）
     */
    private int getVideoDuration(String filePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-i", filePath,
                    "-show_entries", "format=duration",
                    "-v", "quiet",
                    "-of", "csv=p=0"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    return (int) Double.parseDouble(line.trim());
                }
            }
            process.waitFor();
        } catch (Exception e) {
            log.warn("获取视频时长失败: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * 更新转码状态
     */
    private void updateTranscodeStatus(Long videoId, String status, String error) {
        Video video = videoMapper.selectOneById(videoId);
        if (video != null) {
            video.setTranscodeStatus(status);
            video.setTranscodeError(error);
            video.setUpdatedAt(java.time.LocalDateTime.now());
            videoMapper.update(video);
        }
    }

    /**
     * 获取转码状态（兼容旧接口）
     */
    public String getTranscodeStatus(Long videoId) {
        Video video = videoMapper.selectOneById(videoId);
        if (video != null) {
            return video.getTranscodeStatus() != null ? video.getTranscodeStatus() : "none";
        }
        return "none";
    }
}