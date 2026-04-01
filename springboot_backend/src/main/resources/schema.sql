-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    bio VARCHAR(500),
    avatar VARCHAR(255),
    birthday DATE,
    gender VARCHAR(10),
    occupation VARCHAR(100),
    preferences CLOB,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);

-- 用户统计表
CREATE TABLE IF NOT EXISTS user_stats (
    user_id BIGINT PRIMARY KEY,
    like_count INT NOT NULL DEFAULT 0,
    follower_count INT NOT NULL DEFAULT 0,
    following_count INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 分区表
CREATE TABLE IF NOT EXISTS sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);

-- 帖子表
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    reply_count INT NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_section_id ON posts(section_id);
CREATE INDEX IF NOT EXISTS idx_user_id ON posts(user_id);
CREATE INDEX IF NOT EXISTS idx_create_time ON posts(create_time);

-- 回复表
CREATE TABLE IF NOT EXISTS replies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    floor_number INT NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_post_id ON replies(post_id);
CREATE INDEX IF NOT EXISTS idx_reply_user_id ON replies(user_id);

-- 点赞表
CREATE TABLE IF NOT EXISTS likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_target ON likes(user_id, target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_target ON likes(target_type, target_id);

-- 帖子浏览记录表
CREATE TABLE IF NOT EXISTS post_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_deleted INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_post_view_user ON post_views(post_id, user_id);
CREATE INDEX IF NOT EXISTS idx_post_views_user_id ON post_views(user_id);

-- 关注关系表
CREATE TABLE IF NOT EXISTS user_follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_follow_relation ON user_follows(follower_id, followee_id);
CREATE INDEX IF NOT EXISTS idx_followee_id ON user_follows(followee_id);

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    deleted INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_notification_recipient ON notifications(recipient_id, is_read, create_time);

-- 初始化分区数据
INSERT INTO sections (name, description, create_time, update_time) VALUES
('技术讨论', '技术相关话题', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('生活分享', '日常生活分享', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('问答求助', '提问和求助', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('新手入门', '新手指南和入门教程', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('资源分享', '学习资源和工具分享', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('闲聊灌水', '轻松闲聊交流', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
