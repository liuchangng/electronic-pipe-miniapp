package com.score.config;

import com.score.entity.*;
import com.score.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 示例数据初始化
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private BannerMapper bannerMapper;
    
    @Autowired
    private SongMapper songMapper;
    
    @Autowired
    private KnowledgeMapper knowledgeMapper;
    
    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private HotSearchMapper hotSearchMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Override
    public void run(String... args) {
        // 数据库迁移：为已有video表添加新字段
        migrateVideoTable();
        // 数据库迁移：创建feedback表（如果不存在）
        migrateFeedbackTable();
        migrateVideoPlayCount();
        migrateVideoInteractionTables();
        migrateVideoCountColumns();
        migrateCommentTable();
        migratePointTables();
        migrateUserPointColumns();
        migrateFavoriteStatusColumn();
        
        // 仅初始化管理员账号和系统配置，不再插入演示数据
        // 数据通过admin后台管理
        initAdmin();
        initSystemConfigIfNeeded();
        initKnowledgeIfNeeded();
        initHotSearchesIfNeeded();
        System.out.println("✅ 基础数据初始化完成（管理员+系统配置+知识数据）");
    }

    private void initBanners() {
        Banner b1 = new Banner();
        b1.setTitle("电吹管入门教程");
        b1.setImageUrl("/images/banner1.jpg");
        b1.setSortOrder(1);
        b1.setStatus(1);
        bannerMapper.insert(b1);

        Banner b2 = new Banner();
        b2.setTitle("热门曲谱推荐");
        b2.setImageUrl("/images/banner2.jpg");
        b2.setSortOrder(2);
        b2.setStatus(1);
        bannerMapper.insert(b2);
    }

    private void initSongs() {
        String[][] songs = {
            // 经典古曲
            {"梅花三弄", "古曲", "经典古曲，适合入门练习", "#FF6B6B", "100", "50"},
            {"高山流水", "古曲", "中国十大古曲之一", "#4ECDC4", "80", "30"},
            {"渔舟唱晚", "古曲", "经典古筝曲，电吹管改编版", "#45B7D1", "90", "40"},
            
            // 民歌
            {"茉莉花", "江苏民歌", "经典民歌，广为流传", "#96CEB4", "200", "80"},
            {"彩云追月", "广东音乐", "轻快优美的广东音乐", "#FFEAA7", "150", "60"},
            {"半个月亮爬上来", "青海民歌", "优美动听的民歌", "#DDA0DD", "120", "45"},
            
            // 流行歌曲
            {"沧海一声笑", "黄霑", "武侠经典主题曲", "#FF9FF3", "150", "70"},
            {"月亮代表我的心", "邓丽君", "经典情歌", "#F368E0", "180", "90"},
            {"但愿人长久", "邓丽君", "经典歌曲，适合电吹管演奏", "#FF6348", "160", "65"},
            
            // 经典歌曲
            {"送别", "李叔同", "经典歌曲，旋律优美", "#54A0FF", "180", "75"},
            {"同一首歌", "孟卫东", "经典歌曲", "#5F27CD", "120", "55"},
            {"难忘今宵", "王酩", "春晚经典歌曲", "#00D2D3", "90", "40"},
            {"我的祖国", "刘炽", "经典爱国歌曲", "#FF9F43", "110", "50"},
            {"我和我的祖国", "李谷一", "经典爱国歌曲", "#EE5A24", "130", "55"},
            
            // 入门必学
            {"小星星", "莫扎特", "入门必学曲目", "#FFC312", "300", "120"},
            {"欢乐颂", "贝多芬", "经典入门曲目", "#C4E538", "250", "100"},
            {"生日快乐", "传统", "简单易学", "#FDA7DF", "280", "110"},
            {"两只老虎", "儿歌", "适合初学者练习", "#D980FA", "260", "95"},
            
            // 影视金曲
            {"枉凝眉", "王立平", "红楼梦主题曲", "#B33771", "140", "60"},
            {"女儿情", "许镜清", "西游记插曲", "#6C5CE7", "135", "55"},
            {"敢问路在何方", "许镜清", "西游记主题曲", "#00B894", "145", "65"},
        };

        for (int i = 0; i < songs.length; i++) {
            String[] s = songs[i];
            Song song = new Song();
            song.setTitle(s[0]);
            song.setAuthor(s[1]);
            song.setDescription(s[2]);
            song.setColor(s[3]);
            song.setIcon(s[0].charAt(0) + "");
            song.setStatus(1);
            song.setViewCount(Integer.parseInt(s[4]));
            song.setFavoriteCount(Integer.parseInt(s[5]));
            song.setSortOrder(i + 1);
            songMapper.insert(song);
        }
    }

    private void initKnowledge() {
        // 电吹管介绍
        Knowledge k1 = new Knowledge();
        k1.setTitle("什么是电吹管？");
        k1.setSubtitle("了解这种神奇的电子管乐器");
        k1.setCategory("introduction");
        k1.setContent("<h3>电吹管简介</h3><p>电吹管（Electronic Wind Instrument，简称EWI）是一种电子管乐器，通过吹气和按键来控制音高和音量。它结合了传统管乐器的演奏方式和现代电子音乐技术。</p><h3>主要特点</h3><ul><li>体积小巧，便于携带</li><li>音色丰富，可模拟多种乐器</li><li>音量可调，适合练习</li><li>无需调音，使用方便</li></ul><h3>适合人群</h3><p>电吹管适合各个年龄段的音乐爱好者，特别适合：</p><ul><li>想学习管乐器但觉得传统乐器太难的初学者</li><li>已有乐器基础，想拓展音色的音乐人</li><li>中老年音乐爱好者</li></ul>");
        k1.setSortOrder(1);
        knowledgeMapper.insert(k1);

        Knowledge k2 = new Knowledge();
        k2.setTitle("电吹管的种类");
        k2.setSubtitle("选择最适合你的电吹管类型");
        k2.setCategory("introduction");
        k2.setContent("<h3>按吹嘴类型分类</h3><p><strong>1. 萨克斯吹嘴型</strong><br/>最常见的类型，使用萨克斯吹嘴，适合有萨克斯基础的演奏者。</p><p><strong>2. 单簧管吹嘴型</strong><br/>使用单簧管吹嘴，音色更接近木管乐器。</p><p><strong>3. 咬嘴型</strong><br/>直接用牙齿咬住吹嘴，更像口琴的演奏方式。</p><h3>按功能分类</h3><p><strong>1. 入门级</strong><br/>功能简单，价格便宜，适合初学者。</p><p><strong>2. 专业级</strong><br/>功能丰富，音色多样，适合专业演奏。</p>");
        k2.setSortOrder(2);
        knowledgeMapper.insert(k2);

        // 发展历史
        Knowledge k3 = new Knowledge();
        k3.setTitle("电吹管的起源");
        k3.setSubtitle("从1970年代到现代的发展历程");
        k3.setCategory("history");
        k3.setContent("<h3>1970年代：诞生</h3><p>1970年代，日本Yamaha公司发明了世界上第一台电吹管EWI-1000。它的诞生源于一个简单的想法：让任何人都能轻松演奏管乐器。</p><h3>1980年代：发展</h3><p>Akai公司推出了具有里程碑意义的EWI系列，引入了更先进的传感器技术，使演奏更加灵敏和自然。</p><h3>1990年代：成熟</h3><p>电吹管技术逐渐成熟，音色库不断扩大，开始被专业音乐人接受和使用。</p><h3>2000年代至今：普及</h3><p>随着价格下降和技术进步，电吹管开始走进普通音乐爱好者的生活，成为一种流行的音乐学习工具。</p>");
        k3.setSortOrder(1);
        knowledgeMapper.insert(k3);

        Knowledge k4 = new Knowledge();
        k4.setTitle("电吹管在中国的发展");
        k4.setSubtitle("从专业圈到大众普及的历程");
        k4.setCategory("history");
        k4.setContent("<h3>引入中国</h3><p>电吹管于1990年代引入中国，最初主要在专业音乐圈内使用。</p><h3>民间普及</h3><p>2010年代开始，随着网络教程的普及和价格的下降，电吹管逐渐在民间流行起来。许多中老年音乐爱好者开始学习电吹管。</p><h3>现状</h3><p>目前中国已经成为全球最大的电吹管消费市场之一，涌现出许多优秀的电吹管演奏家和教育者。</p>");
        k4.setSortOrder(2);
        knowledgeMapper.insert(k4);

        // 品牌详解
        Knowledge k5 = new Knowledge();
        k5.setTitle("Yamaha");
        k5.setSubtitle("电吹管的发明者与行业领导者");
        k5.setCategory("brand");
        k5.setContent("<h3>品牌介绍</h3><p>Yamaha是电吹管的发明者，也是目前市场上的领导品牌之一。</p><h3>主要产品</h3><ul><li><strong>EWI4000s</strong>：经典款，性能稳定</li><li><strong>EWI5000</strong>：高端款，音色丰富</li><li><strong>EWI USB</strong>：入门款，性价比高</li></ul><h3>特点</h3><ul><li>音色质量优秀</li><li>做工精良，耐用性强</li><li>售后服务完善</li><li>价格相对较高</li></ul>");
        k5.setSortOrder(1);
        knowledgeMapper.insert(k5);

        Knowledge k6 = new Knowledge();
        k6.setTitle("Akai");
        k6.setSubtitle("专业EWI系列的缔造者");
        k6.setCategory("brand");
        k6.setContent("<h3>品牌介绍</h3><p>Akai是专业音频设备制造商，其EWI系列电吹管在专业领域享有盛誉。</p><h3>主要产品</h3><ul><li><strong>EWI4000s</strong>：经典型号</li><li><strong>EWI5000m</strong>：MIDI控制器</li></ul><h3>特点</h3><ul><li>MIDI功能强大</li><li>可编程性强</li><li>适合电子音乐制作</li><li>价格较高</li></ul>");
        k6.setSortOrder(2);
        knowledgeMapper.insert(k6);

        Knowledge k7 = new Knowledge();
        k7.setTitle("国产品牌");
        k7.setSubtitle("高性价比的国产电吹管选择");
        k7.setCategory("brand");
        k7.setContent("<h3>国产品牌概述</h3><p>近年来，国产电吹管品牌发展迅速，以高性价比获得了市场认可。</p><h3>代表品牌</h3><ul><li><strong>Alto</strong>：专注电吹管，产品线丰富</li><li><strong>NUX</strong>：综合乐器品牌，性价比高</li><li><strong>其他</strong>：众多新兴品牌</li></ul><h3>特点</h3><ul><li>价格实惠</li><li>功能实用</li><li>本土化服务好</li><li>适合入门和练习</li></ul>");
        k7.setSortOrder(3);
        knowledgeMapper.insert(k7);

        // 通用指法
        Knowledge k8 = new Knowledge();
        k8.setTitle("基础指法");
        k8.setSubtitle("C调音阶指法入门必学");
        k8.setCategory("fingering");
        k8.setContent("<h3>C调音阶指法</h3><p>以下是电吹管C调音阶的基础指法（从低到高）：</p><table border='1' cellpadding='8' cellspacing='0'><tr><th>音符</th><th>指法</th></tr><tr><td>Do (C)</td><td>全部按键按下</td></tr><tr><td>Re (D)</td><td>松开右手小指</td></tr><tr><td>Mi (E)</td><td>松开右手无名指</td></tr><tr><td>Fa (F)</td><td>松开右手中指</td></tr><tr><td>Sol (G)</td><td>松开右手食指</td></tr><tr><td>La (A)</td><td>松开左手中指</td></tr><tr><td>Si (B)</td><td>松开左手食指</td></tr><tr><td>Do (高音C)</td><td>全部按键松开</td></tr></table><h3>练习建议</h3><ul><li>每天练习音阶15-30分钟</li><li>注意气息控制，保持音色稳定</li><li>先慢后快，循序渐进</li></ul>");
        k8.setSortOrder(1);
        knowledgeMapper.insert(k8);

        Knowledge k9 = new Knowledge();
        k9.setTitle("进阶技巧");
        k9.setSubtitle("颤音、滑音、吐音等演奏技巧");
        k9.setCategory("fingering");
        k9.setContent("<h3>气息控制</h3><p>电吹管的音量和音色很大程度上取决于气息控制：</p><ul><li><strong>轻吹</strong>：音量小，音色柔和</li><li><strong>重吹</strong>：音量大，音色明亮</li><li><strong>渐强渐弱</strong>：通过气息变化实现</li></ul><h3>常用技巧</h3><ul><li><strong>颤音</strong>：通过气息或手指快速抖动产生</li><li><strong>滑音</strong>：按键时逐渐过渡</li><li><strong>吐音</strong>：用舌头控制气流</li></ul><h3>表情记号</h3><ul><li>p（弱）：轻吹</li><li>f（强）：重吹</li><li>mf（中强）：适中</li><li> cresc.（渐强）：逐渐加重</li><li>dim.（渐弱）：逐渐减轻</li></ul>");
        k9.setSortOrder(2);
        knowledgeMapper.insert(k9);
    }

    private void initVideos() {
        Video v1 = new Video();
        v1.setSongId(1L);
        v1.setTitle("梅花三弄-入门教学");
        v1.setDescription("适合初学者的梅花三弄教学视频，讲解基础指法和气息控制");
        v1.setVideoUrl("/videos/tutorial1.m3u8");
        v1.setThumbnailUrl("/images/video1.jpg");
        v1.setDuration(600);
        v1.setTranscodeStatus("done");
        v1.setSortOrder(1);
        videoMapper.insert(v1);

        Video v2 = new Video();
        v2.setSongId(4L);
        v2.setTitle("茉莉花-完整教学");
        v2.setDescription("茉莉花完整演奏教学，包含技巧讲解和示范");
        v2.setVideoUrl("/videos/tutorial2.m3u8");
        v2.setThumbnailUrl("/images/video2.jpg");
        v2.setDuration(900);
        v2.setTranscodeStatus("done");
        v2.setSortOrder(1);
        videoMapper.insert(v2);

        Video v3 = new Video();
        v3.setSongId(16L);
        v3.setTitle("小星星-零基础入门");
        v3.setDescription("零基础也能学会的小星星教学视频");
        v3.setVideoUrl("/videos/tutorial3.m3u8");
        v3.setThumbnailUrl("/images/video3.jpg");
        v3.setDuration(480);
        v3.setTranscodeStatus("done");
        v3.setSortOrder(1);
        videoMapper.insert(v3);

        Video v4 = new Video();
        v4.setSongId(7L);
        v4.setTitle("沧海一声笑-进阶教学");
        v4.setDescription("沧海一声笑进阶演奏技巧，包含颤音和滑音");
        v4.setVideoUrl("/videos/tutorial4.m3u8");
        v4.setThumbnailUrl("/images/video4.jpg");
        v4.setDuration(720);
        v4.setTranscodeStatus("done");
        v4.setSortOrder(1);
        videoMapper.insert(v4);
    }

    private void initHotSearches() {
        String[] keywords = {"小星星", "茉莉花", "沧海一声笑", "月亮代表我的心", "送别", "欢乐颂", "生日快乐", "但愿人长久"};
        for (int i = 0; i < keywords.length; i++) {
            HotSearch hs = new HotSearch();
            hs.setKeyword(keywords[i]);
            hs.setSearchCount(100 - i * 10);
            hs.setIsHot(1);
            hs.setSortOrder(i + 1);
            hs.setDeleted(0);
            hotSearchMapper.insert(hs);
        }
    }

    /**
     * 知识内容检查，如果数据不完整则重新初始化种子数据
     * 检查4个分类(introduction/history/brand/fingering)是否都有数据
     */
    private void initKnowledgeIfNeeded() {
        try {
            // 检查4个分类是否都有数据
            String[] categories = {"introduction", "history", "brand", "fingering"};
            boolean needInit = false;
            
            Integer totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM knowledge", Integer.class);
            if (totalCount == null || totalCount < 9) {
                // 总数不足9条（完整种子数据有9条），需要初始化
                needInit = true;
            } else {
                // 检查每个分类是否都有数据
                for (String cat : categories) {
                    Integer catCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM knowledge WHERE category = ?", Integer.class, cat);
                    if (catCount == null || catCount == 0) {
                        needInit = true;
                        break;
                    }
                }
            }
            
            if (needInit) {
                System.out.println("⚠️ 知识内容不完整，开始初始化种子数据");
                // 清除已有数据，重新插入完整种子数据
                jdbcTemplate.execute("DELETE FROM knowledge");
                initKnowledge();
                System.out.println("✅ 知识内容种子数据初始化完成");
            } else {
                System.out.println("✅ 知识内容已存在（" + totalCount + "条），跳过初始化");
            }
        } catch (Exception e) {
            System.out.println("⚠️ knowledge表检查失败: " + e.getMessage());
        }
    }

    /**
     * 热门搜索单独检查，如果为空则插入种子数据
     */
    private void initHotSearchesIfNeeded() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hot_search", Integer.class);
            if (count == null || count == 0) {
                System.out.println("⚠️ 热门搜索为空，开始初始化种子数据");
                initHotSearches();
                System.out.println("✅ 热门搜索种子数据初始化完成");
            }
        } catch (Exception e) {
            System.out.println("⚠️ 热门搜索表检查失败: " + e.getMessage());
        }
    }

    /**
     * 初始化管理员账号
     */
    private void initAdmin() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin", Integer.class);
            if (count != null && count > 0) {
                System.out.println("✅ 管理员账号已存在（" + count + "个），跳过初始化");
                return;
            }
        } catch (Exception e) {
            System.out.println("⚠️ admin表不存在，跳过管理员初始化: " + e.getMessage());
            return;
        }

        Admin admin = Admin.builder()
                .username("admin")
                .password("admin123")
                .nickname("超级管理员")
                .role("super_admin")
                .status(1)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        adminMapper.insert(admin);
        System.out.println("✅ 默认管理员账号初始化完成（admin / admin123）");
    }

    /**
     * 初始化系统配置（首次启动时插入默认值）
     */
    private void initSystemConfig() {
        initConfigIfAbsent("system_name", "电子Pipe曲谱小程序", "系统名称");
        initConfigIfAbsent("system_version", "1.0.0", "系统版本");
        initConfigIfAbsent("system_logo", "", "系统Logo URL");
        initConfigIfAbsent("system_subtitle", "曲谱管理系统", "系统副标题");
        System.out.println("✅ 系统配置初始化完成");
    }

    /**
     * 已有数据时检查并补充缺失的配置项
     */
    private void initSystemConfigIfNeeded() {
        initSystemConfig();
    }

    private void initConfigIfAbsent(String key, String value, String desc) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM system_config WHERE config_key = ?", Integer.class, key);
            if (count == null || count == 0) {
                jdbcTemplate.update(
                    "INSERT INTO system_config (config_key, config_value, description, updated_at) VALUES (?, ?, ?, datetime('now'))",
                    key, value, desc);
            }
        } catch (Exception e) {
            System.out.println("⚠️ system_config表可能不存在，跳过配置初始化: " + e.getMessage());
        }
    }

    /**
     * 数据库迁移：为已有video表添加transcode_status和transcode_error字段
     */
    private void migrateVideoTable() {
        try {
            // 检查transcode_status列是否存在
            Integer colCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pragma_table_info('video') WHERE name = 'transcode_status'",
                Integer.class);
            if (colCount == null || colCount == 0) {
                jdbcTemplate.execute("ALTER TABLE video ADD COLUMN transcode_status VARCHAR(32) DEFAULT 'pending'");
                System.out.println("✅ video表添加transcode_status字段");
            }
            // 检查transcode_error列是否存在
            Integer errColCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pragma_table_info('video') WHERE name = 'transcode_error'",
                Integer.class);
            if (errColCount == null || errColCount == 0) {
                jdbcTemplate.execute("ALTER TABLE video ADD COLUMN transcode_error VARCHAR(512)");
                System.out.println("✅ video表添加transcode_error字段");
            }
            // 已有m3u8 URL的视频标记为已完成转码
            int updated = jdbcTemplate.update(
                "UPDATE video SET transcode_status = 'done' WHERE video_url LIKE '%.m3u8' AND (transcode_status IS NULL OR transcode_status = 'pending')");
            if (updated > 0) {
                System.out.println("✅ 已将" + updated + "个m3u8视频标记为转码完成");
            }
        } catch (Exception e) {
            System.out.println("⚠️ video表迁移失败: " + e.getMessage());
        }
    }

    /**
     * 数据库迁移：创建feedback表（如果不存在）
     */
    private void migrateFeedbackTable() {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS feedback (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "type VARCHAR(32) NOT NULL DEFAULT 'suggestion', " +
                "content TEXT NOT NULL, " +
                "contact VARCHAR(128), " +
                "status VARCHAR(32) DEFAULT 'pending', " +
                "reply TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
            System.out.println("✅ feedback表就绪");
        } catch (Exception e) {
            System.out.println("⚠️ feedback表迁移失败: " + e.getMessage());
        }
    }

    /**
     * 数据库迁移：为video表添加play_count列
     */
    private void migrateVideoPlayCount() {
        try {
            var columns = jdbcTemplate.queryForList(
                "SELECT name FROM pragma_table_info('video') WHERE name='play_count'", String.class);
            if (columns.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE video ADD COLUMN play_count INTEGER DEFAULT 0");
                System.out.println("✅ video表添加play_count列");
            } else {
                System.out.println("✅ video表play_count列已存在");
            }
        } catch (Exception e) {
            System.out.println("⚠️ video表play_count迁移失败: " + e.getMessage());
        }
    }

    /**
     * 数据库迁移：创建视频点赞和收藏表
     */
    private void migrateVideoInteractionTables() {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS video_like (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "video_id INTEGER NOT NULL, " +
                "status VARCHAR(16) DEFAULT 'active', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(user_id, video_id))");
            System.out.println("✅ video_like表已就绪");

            // 为已有video_like表添加status和updated_at列
            try {
                var statusCol = jdbcTemplate.queryForList(
                    "SELECT name FROM pragma_table_info('video_like') WHERE name='status'", String.class);
                if (statusCol.isEmpty()) {
                    jdbcTemplate.execute("ALTER TABLE video_like ADD COLUMN status VARCHAR(16) DEFAULT 'active'");
                    System.out.println("✅ video_like表添加status列");
                }
                var updatedAtCol = jdbcTemplate.queryForList(
                    "SELECT name FROM pragma_table_info('video_like') WHERE name='updated_at'", String.class);
                if (updatedAtCol.isEmpty()) {
                    jdbcTemplate.execute("ALTER TABLE video_like ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    System.out.println("✅ video_like表添加updated_at列");
                }
            } catch (Exception e) {
                System.out.println("⚠️ video_like表列迁移失败: " + e.getMessage());
            }

            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS video_favorite (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "video_id INTEGER NOT NULL, " +
                "status VARCHAR(16) DEFAULT 'active', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(user_id, video_id))");
            System.out.println("✅ video_favorite表已就绪");

            // 为已有video_favorite表添加status和updated_at列
            try {
                var statusCol = jdbcTemplate.queryForList(
                    "SELECT name FROM pragma_table_info('video_favorite') WHERE name='status'", String.class);
                if (statusCol.isEmpty()) {
                    jdbcTemplate.execute("ALTER TABLE video_favorite ADD COLUMN status VARCHAR(16) DEFAULT 'active'");
                    System.out.println("✅ video_favorite表添加status列");
                }
                var updatedAtCol = jdbcTemplate.queryForList(
                    "SELECT name FROM pragma_table_info('video_favorite') WHERE name='updated_at'", String.class);
                if (updatedAtCol.isEmpty()) {
                    jdbcTemplate.execute("ALTER TABLE video_favorite ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    System.out.println("✅ video_favorite表添加updated_at列");
                }
            } catch (Exception e) {
                System.out.println("⚠️ video_favorite表列迁移失败: " + e.getMessage());
            }

            // 创建视频分享记录表
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS video_share (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "video_id INTEGER NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            System.out.println("✅ video_share表已就绪");
        } catch (Exception e) {
            System.out.println("⚠️ 视频互动表迁移失败: " + e.getMessage());
        }
    }

    /**
     * 数据库迁移：为video表添加like_count和favorite_count列
     */
    private void migrateVideoCountColumns() {
        try {
            var likeColumns = jdbcTemplate.queryForList(
                "SELECT name FROM pragma_table_info('video') WHERE name='like_count'", String.class);
            if (likeColumns.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE video ADD COLUMN like_count INTEGER DEFAULT 0");
                System.out.println("✅ video表添加like_count列");
            }

            var favColumns = jdbcTemplate.queryForList(
                "SELECT name FROM pragma_table_info('video') WHERE name='favorite_count'", String.class);
            if (favColumns.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE video ADD COLUMN favorite_count INTEGER DEFAULT 0");
                System.out.println("✅ video表添加favorite_count列");
            }

            var commentCountColumns = jdbcTemplate.queryForList(
                "SELECT name FROM pragma_table_info('video') WHERE name='comment_count'", String.class);
            if (commentCountColumns.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE video ADD COLUMN comment_count INTEGER DEFAULT 0");
                System.out.println("✅ video表添加comment_count列");
            }

            var shareCountColumns = jdbcTemplate.queryForList(
                "SELECT name FROM pragma_table_info('video') WHERE name='share_count'", String.class);
            if (shareCountColumns.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE video ADD COLUMN share_count INTEGER DEFAULT 0");
                System.out.println("✅ video表添加share_count列");
            }
        } catch (Exception e) {
            System.out.println("⚠️ video表计数列迁移失败: " + e.getMessage());
        }
    }

    /**
     * 数据库迁移：创建评论表
     */
    private void migrateCommentTable() {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS comment (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "video_id INTEGER NOT NULL, " +
                "content TEXT NOT NULL, " +
                "parent_id INTEGER, " +
                "status VARCHAR(32) DEFAULT 'approved', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "deleted INTEGER DEFAULT 0)");
            System.out.println("✅ comment表已就绪");
        } catch (Exception e) {
            System.out.println("⚠️ comment表迁移失败: " + e.getMessage());
        }
    }

    /**
     * 数据库迁移：创建积分流水表
     */
    private void migratePointTables() {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS point_log (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "type VARCHAR(32) NOT NULL, " +
                "amount INTEGER NOT NULL, " +
                "description VARCHAR(128), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            System.out.println("✅ point_log表已就绪");
        } catch (Exception e) {
            System.out.println("⚠️ point_log表迁移失败: " + e.getMessage());
        }
    }

    /**
     * 数据库迁移：为user表添加point和level列
     */
    private void migrateUserPointColumns() {
        try {
            var pointColumns = jdbcTemplate.queryForList(
                "SELECT name FROM pragma_table_info('user') WHERE name='point'", String.class);
            if (pointColumns.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE user ADD COLUMN point INTEGER DEFAULT 0");
                System.out.println("✅ user表添加point列");
            }

            var levelColumns = jdbcTemplate.queryForList(
                "SELECT name FROM pragma_table_info('user') WHERE name='level'", String.class);
            if (levelColumns.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE user ADD COLUMN level INTEGER DEFAULT 1");
                System.out.println("✅ user表添加level列");
            }
        } catch (Exception e) {
            System.out.println("⚠️ user表积分列迁移失败: " + e.getMessage());
        }
    }

    /**
     * 数据库迁移：为favorite表添加status和updated_at列
     */
    private void migrateFavoriteStatusColumn() {
        try {
            var statusCol = jdbcTemplate.queryForList(
                "SELECT name FROM pragma_table_info('favorite') WHERE name='status'", String.class);
            if (statusCol.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE favorite ADD COLUMN status VARCHAR(16) DEFAULT 'active'");
                System.out.println("✅ favorite表添加status列");
            }
            var updatedAtCol = jdbcTemplate.queryForList(
                "SELECT name FROM pragma_table_info('favorite') WHERE name='updated_at'", String.class);
            if (updatedAtCol.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE favorite ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                System.out.println("✅ favorite表添加updated_at列");
            }
        } catch (Exception e) {
            System.out.println("⚠️ favorite表列迁移失败: " + e.getMessage());
        }
    }
}
