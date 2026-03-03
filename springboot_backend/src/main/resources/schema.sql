-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
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

-- 初始化分区数据
INSERT INTO sections (name, description, create_time, update_time) VALUES
('技术讨论', '技术相关话题', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('生活分享', '日常生活分享', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('问答求助', '提问和求助', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
