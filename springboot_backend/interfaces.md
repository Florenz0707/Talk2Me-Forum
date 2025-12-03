---
title: 默认模块
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# 默认模块

Base URLs:

# Authentication

- HTTP Authentication, scheme: bearer

# 认证

## POST 登录

POST /api/v1/login

通过用户名和密码登录。
- 如果正确，则发放access_token和refresh_token
- 否则，返回错误

> Body 请求参数

```json
{
  "username": "string",
  "password": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[LoginDto](#schemalogindto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "access_token": "string",
  "refresh_token": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» access_token|string|true|none||none|
|» refresh_token|string|true|none||none|

## POST 注册

POST /api/v1/register

通过用户名和密码注册，用户名必须唯一不重复。

> Body 请求参数

```json
{
  "username": "string",
  "password": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[LoginDto](#schemalogindto)| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## POST 刷新Token

POST /api/v1/refresh

通过refresh_token进行access_token的续期。

> Body 请求参数

```json
{
  "refresh_token": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 否 |none|
|» refresh_token|body|string| 是 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 聊天

## GET 获取所有屋子

GET /api/v1/house/list

获取所有屋子的ID和名称。返回一个列表。

> 返回示例

> 200 Response

```json
{
  "house_list": [
    {
      "id": 0,
      "name": "string"
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» house_list|[[House](#schemahouse)]|true|none||none|
|»» id|integer|true|none||none|
|»» name|string|true|none||none|

## GET 获取屋子内的房间

GET /api/v1/room/list

获取一个屋子里的所有房间，返回一个列表。
*（默认方式按最新排序）*

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|house_id|query|integer| 否 |none|

> 返回示例

> 200 Response

```json
{
  "room_list": [
    "string"
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» room_list|[string]|true|none||none|

## GET 获取房间内的谈话

GET /api/v1/talk/list

获取一个房间内的所有谈话，返回一个列表。
*（默认方式按最旧）*

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|room|query|integer| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## POST 建立一个房间

POST /api/v1/room/new

建立一个房间。

> Body 请求参数

```json
{
  "house_id": 0,
  "room_title": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 否 |none|
|» house_id|body|integer| 是 |none|
|» room_title|body|string| 是 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 数据模型

<h2 id="tocS_LoginDto">LoginDto</h2>

<a id="schemalogindto"></a>
<a id="schema_LoginDto"></a>
<a id="tocSlogindto"></a>
<a id="tocslogindto"></a>

```json
{
  "username": "string",
  "password": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|username|string|true|none||none|
|password|string|true|none||none|

<h2 id="tocS_House">House</h2>

<a id="schemahouse"></a>
<a id="schema_House"></a>
<a id="tocShouse"></a>
<a id="tocshouse"></a>

```json
{
  "id": 0,
  "name": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer|true|none||none|
|name|string|true|none||none|

<h2 id="tocS_User">User</h2>

<a id="schemauser"></a>
<a id="schema_User"></a>
<a id="tocSuser"></a>
<a id="tocsuser"></a>

```json
{
  "username": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|username|string|true|none||none|

<h2 id="tocS_Room">Room</h2>

<a id="schemaroom"></a>
<a id="schema_Room"></a>
<a id="tocSroom"></a>
<a id="tocsroom"></a>

```json
{
  "id": 0,
  "title": "string",
  "creator_id": 0,
  "create_time": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer|true|none||none|
|title|string|true|none||none|
|creator_id|integer|true|none||none|
|create_time|string|true|none||none|

<h2 id="tocS_Talk">Talk</h2>

<a id="schematalk"></a>
<a id="schema_Talk"></a>
<a id="tocStalk"></a>
<a id="tocstalk"></a>

```json
{
  "id": 0,
  "content": "string",
  "creator_id": 0,
  "create_time": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer|true|none||none|
|content|string|true|none||none|
|creator_id|integer|true|none||none|
|create_time|string|true|none||none|
