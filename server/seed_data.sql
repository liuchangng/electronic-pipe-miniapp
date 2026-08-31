-- 曲谱数据（8首经典曲目）
INSERT INTO song (title, author, icon, color, description, image_url, score_image_url, category, status, sort_order, view_count, favorite_count) VALUES
('梅花三弄', '古曲', '梅', '#FF6B6B', '经典古曲，旋律优美，适合电吹管入门练习', '', '', 'hot', 1, 1, 128, 56),
('高山流水', '古曲', '高', '#4ECDC4', '中国十大古曲之一，意境深远', '', '', 'hot', 1, 2, 95, 42),
('渔舟唱晚', '古曲', '渔', '#45B7D1', '经典古筝曲改编，电吹管演绎别有韵味', '', '', 'hot', 1, 3, 110, 48),
('茉莉花', '江苏民歌', '茉', '#96CEB4', '经典民歌，广为流传，旋律优美', '', '', 'favorite', 1, 4, 200, 80),
('沧海一声笑', '黄霑', '沧', '#FF9FF3', '武侠经典主题曲，气势磅礴', '', '', 'hot', 1, 5, 150, 70),
('月亮代表我的心', '邓丽君', '月', '#F368E0', '经典情歌，适合电吹管深情演绎', '', '', 'favorite', 1, 6, 180, 90),
('送别', '李叔同', '送', '#54A0FF', '经典歌曲，旋律优美动人', '', '', 'new', 1, 7, 175, 75),
('但愿人长久', '邓丽君', '但', '#FF6348', '经典歌曲，苏轼词，适合电吹管演奏', '', '', 'new', 1, 8, 160, 65);

-- 视频数据（6个教学视频，关联曲谱）
INSERT INTO video (title, description, song_id, video_url, thumbnail_url, duration, transcode_status, sort_order, play_count) VALUES
('梅花三弄-入门教学', '适合初学者的梅花三弄教学视频，讲解基础指法和气息控制', 2, '', '', 600, 'done', 1, 35),
('高山流水-演奏示范', '高山流水完整演奏示范，感受古曲意境', 3, '', '', 480, 'done', 2, 28),
('渔舟唱晚-技巧讲解', '渔舟唱晚演奏技巧详解，包含气息和颤音技巧', 4, '', '', 720, 'done', 3, 42),
('茉莉花-完整教学', '茉莉花完整演奏教学，从入门到熟练', 5, '', '', 900, 'done', 4, 56),
('沧海一声笑-进阶教学', '沧海一声笑进阶演奏技巧，包含颤音和滑音', 6, '', '', 540, 'done', 5, 38),
('月亮代表我的心-深情演绎', '月亮代表我的心电吹管演奏教学', 7, '', '', 660, 'done', 6, 45);

-- Banner数据（5条）
INSERT INTO banner (title, image_url, link_url, sort_order, status) VALUES
('电吹管入门教程', 'https://picsum.photos/seed/pipe1/750/300', '', 1, 1),
('热门曲谱推荐', 'https://picsum.photos/seed/pipe2/750/300', '', 2, 1),
('经典古曲专区', 'https://picsum.photos/seed/pipe3/750/300', '', 3, 1),
('新曲上线', 'https://picsum.photos/seed/pipe4/750/300', '', 4, 1),
('电吹管社区', 'https://picsum.photos/seed/pipe5/750/300', '', 5, 1);