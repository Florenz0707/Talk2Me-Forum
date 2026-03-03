# Talk2Me Forum API 设计文档

## 数据结构

### 分区（Section）

- id: 分区ID
- name: 分区名称
- description: 分区描述
- postCount: 帖子数量
- sortOrder: 排序顺序

### 帖子（Post）

- id: 帖子ID
- sectionId: 所属分区ID
- userId: 发帖人ID
- title: 标题
- content: 内容
- viewCount: 浏览数
- likeCount: 点赞数
- replyCount: 回复数
- status: 状态（0:正常 1:删除 2:隐藏）

### 回复（Reply）

- id: 回复ID
- postId: 所属帖子ID
- userId: 回复人ID
- content: 内容
- floorNumber: 楼层号
- likeCount: 点赞数
- status: 状态（0:正常 1:删除）

### 点赞（Like）

- targetType: 目标类型（POST/REPLY）
- targetId: 目标ID

## API 接口

### 1. 分区相关

#### 1.1 获取分区列表

```
GET /api/v1/sections
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "技术讨论",
      "description": "技术相关话题",
      "postCount": 100,
      "sortOrder": 1
    }
  ]
}
```

#### 1.2 获取分区详情

```
GET /api/v1/sections/{id}
```

响应：同上单个对象

### 2. 帖子相关

#### 2.1 创建帖子

```
POST /api/v1/posts
Content-Type: application/json
Authorization: Bearer {token}
```

请求体：

```json
{
  "sectionId": 1,
  "title": "帖子标题",
  "content": "帖子内容"
}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "sectionId": 1,
    "userId": 1,
    "title": "帖子标题",
    "content": "帖子内容",
    "viewCount": 0,
    "likeCount": 0,
    "replyCount": 0,
    "createTime": "2026-03-03T14:00:00"
  }
}
```

#### 2.2 获取帖子详情

```
GET /api/v1/posts/{id}
```

响应：同上

#### 2.3 更新帖子

```
PUT /api/v1/posts/{id}
Content-Type: application/json
Authorization: Bearer {token}
```

请求体：

```json
{
  "title": "新标题",
  "content": "新内容"
}
```

#### 2.4 删除帖子

```
DELETE /api/v1/posts/{id}
Authorization: Bearer {token}
```

#### 2.5 获取帖子列表

```
GET /api/v1/posts?sectionId={sectionId}&page={page}&size={size}
```

参数：

- sectionId: 分区ID（可选）
- page: 页码（默认1）
- size: 每页数量（默认20）

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "list": [...]
  }
}
```

### 3. 回复相关

#### 3.1 创建回复

```
POST /api/v1/posts/{postId}/replies
Content-Type: application/json
Authorization: Bearer {token}
```

请求体：

```json
{
  "content": "回复内容"
}
```

#### 3.2 获取回复列表

```
GET /api/v1/posts/{postId}/replies?page={page}&size={size}
```

#### 3.3 删除回复

```
DELETE /api/v1/replies/{id}
Authorization: Bearer {token}
```

### 4. 点赞相关

#### 4.1 点赞

```
POST /api/v1/likes
Content-Type: application/json
Authorization: Bearer {token}
```

请求体：

```json
{
  "targetType": "POST",
  "targetId": 1
}
```

#### 4.2 取消点赞

```
DELETE /api/v1/likes?targetType={targetType}&targetId={targetId}
Authorization: Bearer {token}
```

## 扩展空间

1. 帖子可扩展字段：
   - tags: 标签
   - images: 图片附件
   - isTop: 是否置顶
   - isEssence: 是否精华

2. 回复可扩展为树形结构：
   - parentId: 父回复ID
   - replyToUserId: 回复给谁

3. 用户关系：
   - 关注/粉丝
   - 收藏帖子

4. 通知系统：
   - 回复通知
   - 点赞通知
   - @提醒
