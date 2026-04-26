# 白族非遗展示系统 - E-R图设计信息

## 📌 文档说明
本文档包含绘制系统 E-R 图（实体-关系图）所需的全部信息，基于实际数据库表结构提取，专门针对**白族非遗智能展示与管理平台**的核心业务表。

> **说明**：若依框架自带的系统管理表（菜单、角色、字典、日志等）仅保留与非遗系统**强关联的用户表**，其他系统表不在本 E-R 图范围内。

---

## 一、核心业务实体清单

###  非遗业务表（9张核心表）

| 序号 | 表名 | 实体名称 | 说明 | 所属模块 |
|------|------|---------|------|---------|
| 1 | `heritage_item` | 非遗展品 | 核心业务实体，展示非遗项目 | heritage |
| 2 | `heritage_category` | 非遗分类 | 展品分类（树形结构） | heritage |
| 3 | `heritage_video` | 视频档案 | 展品关联的视频资源 | heritage |
| 4 | `heritage_audio` | 音频档案 | 展品关联的音频资源 | heritage |
| 5 | `heritage_comment` | 评论 | 用户对展品的评论 | heritage |
| 6 | `heritage_user_action` | 用户行为 | 用户点赞/收藏记录 | heritage |
| 7 | `heritage_inheritor` | 传承人 | 非遗传承人信息 | heritage |
| 8 | `heritage_gallery` | 图集主表 | 展品图册（主表） | heritage |
| 9 | `heritage_gallery_image` | 图集从表 | 图册图片明细（从表） | heritage |

### 👤 系统用户表（1张强关联表）

| 序号 | 表名 | 实体名称 | 说明 | 关联说明 |
|------|------|---------|------|---------|
| 10 | `sys_user` | 系统用户 | 若依框架用户表 | 评论、用户行为关联 |

---

## 二、各表详细结构

### 📊 1. heritage_item（非遗展品表）

**表说明**：存储非遗展品的核心信息，包括展品名称、分类、3D模型、统计数据等

**主键**：`item_id`（BIGINT，自增）

| 字段名 | 数据类型 | 是否主键 | 是否外键 | 允许空 | 默认值 | 说明 |
|--------|---------|---------|---------|--------|--------|------|
| `item_id` | BIGINT | ✅ 是 | - | 否 | 自增 | 展品ID（主键） |
| `item_name` | VARCHAR(200) | - | - | 否 | - | 展品名称 |
| `category_id` | BIGINT | - | ✅ 外键 | 否 | - | 分类ID（关联 heritage_category.category_id） |
| `cover_image` | VARCHAR(500) | - | - | 是 | NULL | 封面图片URL（相对路径） |
| `model_file` | VARCHAR(500) | - | - | 是 | NULL | 3D模型文件路径（.glb/.gltf） |
| `description` | TEXT | - | - | 是 | NULL | 展品简介（展示用） |
| `history` | TEXT | - | - | 是 | NULL | 历史渊源 |
| `craft` | TEXT | - | - | 是 | NULL | 制作工艺 |
| `view_count` | INT UNSIGNED | - | - | 否 | 0 | 浏览次数 |
| `like_count` | INT UNSIGNED | - | - | 否 | 0 | 点赞数 |
| `favorite_count` | INT UNSIGNED | - | - | 否 | 0 | 收藏数 |
| `comment_count` | BIGINT | - | - | 是 | 0 | 评论数量 |
| `status` | TINYINT(1) | - | - | 是 | 0 | 状态（0正常,1下架） |
| `del_flag` | CHAR(1) | - | - | 是 | '0' | 删除标志（0=未删,2=已删） |
| `sort_order` | INT | - | - | 是 | 0 | 显示顺序 |
| `remark` | TEXT | - | - | 是 | NULL | 备注 |
| `create_by` | VARCHAR(64) | - | - | 是 | '' | 创建者 |
| `create_time` | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | VARCHAR(64) | - | - | 是 | '' | 更新者 |
| `update_time` | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间 |
| `reject_reason` | VARCHAR(500) | - | - | 是 | NULL | 审核驳回原因 |

**索引**：
- `PRIMARY KEY (item_id)`
- `INDEX idx_category_id (category_id)` - 分类查询优化
- `INDEX idx_cat_status_sort (category_id, status, sort_order)` - 复合索引
- `INDEX idx_status (status)` - 状态查询
- `INDEX idx_view_count (view_count)` - 浏览量排序
- `INDEX idx_favorite_count (favorite_count)` - 收藏数排序

---

### 📊 2. heritage_category（非遗分类表）

**表说明**：非遗分类信息，支持树形结构（自关联）

**主键**：`category_id`（BIGINT，自增）

| 字段名 | 数据类型 | 是否主键 | 是否外键 | 允许空 | 默认值 | 说明 |
|--------|---------|---------|---------|--------|--------|------|
| `category_id` | BIGINT | ✅ 是 | - | 否 | 自增 | 分类ID（主键） |
| `parent_id` | BIGINT | - | ✅ 自关联 | 是 | 0 | 父分类ID（0为顶级大类） |
| `category_name` | VARCHAR(100) | - | - | 否 | - | 分类名称 |
| `category_code` | VARCHAR(100) | - | - | 是 | NULL | 分类编码（可用于前端/接口过滤） |
| `sort_order` | INT | - | - | 是 | 0 | 显示顺序（越小越靠前） |
| `status` | TINYINT(1) | - | - | 是 | 0 | 状态（0=正常,1=停用） |
| `del_flag` | CHAR(1) | - | - | 是 | '0' | 删除标志（0=未删,2=已删） |
| `remark` | TEXT | - | - | 是 | NULL | 备注 |
| `create_by` | VARCHAR(64) | - | - | 是 | '' | 创建者 |
| `create_time` | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | VARCHAR(64) | - | - | 是 | '' | 更新者 |
| `update_time` | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间 |

**约束**：
- `PRIMARY KEY (category_id)`
- `UNIQUE (category_code)` - 分类编码唯一
- `INDEX idx_parent_id (parent_id)` - 父ID索引

---

### 📊 3. heritage_video（视频档案表）

**表说明**：展品关联的视频资源，一个展品可有多个视频

**主键**：`video_id`（BIGINT，自增）

| 字段名 | 数据类型 | 是否主键 | 是否外键 | 允许空 | 默认值 | 说明 |
|--------|---------|---------|---------|--------|--------|------|
| `video_id` | BIGINT | ✅ 是 | - | 否 | 自增 | 视频ID（主键） |
| `title` | VARCHAR(100) | - | - | 否 | - | 视频标题 |
| `video_url` | VARCHAR(500) | - | - | 否 | - | 视频文件路径 |
| `cover_image` | VARCHAR(500) | - | - | 是 | NULL | 封面图片URL |
| `item_id` | BIGINT | - | ✅ 外键 | 否 | - | 关联展品ID（外键） |
| `duration` | INT | - | - | 是 | NULL | 视频时长（秒） |
| `description` | TEXT | - | - | 是 | NULL | 视频简介/解说词 |
| `status` | CHAR(1) | - | - | 是 | '0' | 状态（0正常 1待审核 2下架） |
| `del_flag` | CHAR(1) | - | - | 是 | '0' | 删除标志 |
| `create_by` | VARCHAR(64) | - | - | 是 | '' | 创建者 |
| `create_time` | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | VARCHAR(64) | - | - | 是 | '' | 更新者 |
| `update_time` | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间 |

**索引**：
- `PRIMARY KEY (video_id)`
- `INDEX idx_item_id (item_id)` - 展品关联查询优化

---

### 📊 4. heritage_audio（音频档案表）

**表说明**：展品关联的音频资源（如白族调、唱腔等）

**主键**：`audio_id`（BIGINT，自增）

| 字段名 | 数据类型 | 是否主键 | 是否外键 | 允许空 | 默认值 | 说明 |
|--------|---------|---------|---------|--------|--------|------|
| `audio_id` | BIGINT | ✅ 是 | - | 否 | 自增 | 音频ID（主键） |
| `title` | VARCHAR(100) | - | - | 否 | - | 音频标题 |
| `audio_url` | VARCHAR(500) | - | - | 否 | - | 音频文件路径 |
| `item_id` | BIGINT | - | ✅ 外键 | 否 | - | 关联展品ID（外键） |
| `waveform_data` | TEXT | - | - | 是 | NULL | 预生成的波形数据（JSON字符串） |
| `description` | TEXT | - | - | 是 | NULL | 内容解析/唱词 |
| `status` | CHAR(1) | - | - | 是 | '0' | 状态（0正常 1待审核 2下架） |
| `del_flag` | CHAR(1) | - | - | 是 | '0' | 删除标志 |
| `create_by` | VARCHAR(64) | - | - | 是 | '' | 创建者 |
| `create_time` | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | VARCHAR(64) | - | - | 是 | '' | 更新者 |
| `update_time` | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间 |

**索引**：
- `PRIMARY KEY (audio_id)`
- `INDEX idx_item_id (item_id)` - 展品关联查询优化

---

### 📊 5. heritage_comment（评论表）

**表说明**：用户对非遗展品的评论，支持回复功能（预留）

**主键**：`comment_id`（BIGINT，自增）

| 字段名 | 数据类型 | 是否主键 | 是否外键 | 允许空 | 默认值 | 说明 |
|--------|---------|---------|---------|--------|--------|------|
| `comment_id` | BIGINT | ✅ 是 | - | 否 | 自增 | 评论ID（主键） |
| `item_id` | BIGINT | - | ✅ 外键 | 否 | - | 非遗展品ID（外键） |
| `user_id` | BIGINT | - | ✅ 外键 | 否 | - | 用户ID（关联 sys_user.user_id） |
| `parent_id` | BIGINT | - | ✅ 自关联 | 是 | NULL | 父评论ID（用于回复，预留） |
| `content` | VARCHAR(500) | - | - | 是 | NULL | 评论内容 |
| `create_time` | DATETIME | - | - | 是 | NULL | 创建时间 |
| `del_flag` | CHAR(1) | - | - | 是 | '0' | 删除标志（0代表存在 2代表删除） |

**索引**：
- `PRIMARY KEY (comment_id)`

---

### 📊 6. heritage_user_action（用户行为表）

**表说明**：记录用户对展品的点赞和收藏行为，防止重复操作

**主键**：`action_id`（BIGINT，自增）

| 字段名 | 数据类型 | 是否主键 | 是否外键 | 允许空 | 默认值 | 说明 |
|--------|---------|---------|---------|--------|--------|------|
| `action_id` | BIGINT | ✅ 是 | - | 否 | 自增 | 主键ID |
| `user_id` | BIGINT | - | ✅ 外键 | 否 | - | 用户ID（关联 sys_user.user_id） |
| `item_id` | BIGINT | - | ✅ 外键 | 否 | - | 展品ID（关联 heritage_item.item_id） |
| `action_type` | TINYINT(1) | - | - | 否 | - | 动作类型：1-点赞, 2-收藏 |
| `create_time` | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 操作时间 |

**约束**：
- `PRIMARY KEY (action_id)`
- `UNIQUE (user_id, item_id, action_type)` - 联合唯一约束（防止同一用户对同一展品重复点赞/收藏）

---

### 📊 7. heritage_inheritor（传承人表）

**表说明**：非遗传承人信息，关联所属技艺分类

**主键**：`inheritor_id`（BIGINT，自增）

| 字段名 | 数据类型 | 是否主键 | 是否外键 | 允许空 | 默认值 | 说明 |
|--------|---------|---------|---------|--------|--------|------|
| `inheritor_id` | BIGINT | ✅ 是 | - | 否 | 自增 | 传承人ID（主键） |
| `name` | VARCHAR(100) | - | - | 否 | - | 姓名 |
| `avatar` | VARCHAR(500) | - | - | 是 | NULL | 个人照片 |
| `level` | CHAR(1) | - | - | 否 | - | 级别（1国家级 2省级 3市级 4县级） |
| `category_id` | BIGINT | - | ✅ 外键 | 否 | - | 所属技艺分类ID（关联 heritage_category.category_id） |
| `introduction` | TEXT | - | - | 是 | NULL | 个人简介 |
| `status` | CHAR(1) | - | - | 是 | '0' | 状态（0在世 1去世） |
| `del_flag` | CHAR(1) | - | - | 是 | '0' | 删除标志 |
| `create_by` | VARCHAR(64) | - | - | 是 | '' | 创建者 |
| `create_time` | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间 |

**索引**：
- `PRIMARY KEY (inheritor_id)`
- `INDEX idx_category_id (category_id)` - 分类关联查询优化

---

### 📊 8. heritage_gallery（图集主表）

**表说明**：展品图册主表，一个展品对应一个图册

**主键**：`gallery_id`（BIGINT，自增）

| 字段名 | 数据类型 | 是否主键 | 是否外键 | 允许空 | 默认值 | 说明 |
|--------|---------|---------|---------|--------|--------|------|
| `gallery_id` | BIGINT | ✅ 是 | - | 否 | 自增 | 主键ID |
| `title` | VARCHAR(100) | - | - | 否 | - | 图集标题 |
| `cover_url` | VARCHAR(500) | - | - | 否 | - | 封面图 |
| `description` | TEXT | - | - | 是 | NULL | 导语 |
| `status` | CHAR(1) | - | - | 是 | '0' | 状态 |
| `del_flag` | CHAR(1) | - | - | 是 | '0' | 删除标志 |
| `create_by` | VARCHAR(64) | - | - | 是 | '' | 创建者 |
| `create_time` | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |

**注意**：该表**没有直接关联 item_id**，实际关联通过 `heritage_item.item_name` 或业务逻辑实现

---

### 📊 9. heritage_gallery_image（图集从表）

**表说明**：图册图片明细，一个图册包含多张图片

**主键**：`image_id`（BIGINT，自增）

| 字段名 | 数据类型 | 是否主键 | 是否外键 | 允许空 | 默认值 | 说明 |
|--------|---------|---------|---------|--------|--------|------|
| `image_id` | BIGINT | ✅ 是 | - | 否 | 自增 | 主键ID |
| `gallery_id` | BIGINT | - | ✅ 外键 | 否 | - | 关联主表ID（外键） |
| `image_url` | VARCHAR(500) | - | - | 否 | - | 图片路径 |
| `caption` | VARCHAR(200) | - | - | 是 | NULL | 图片说明 |
| `sort_order` | INT | - | - | 是 | 0 | 排序 |

**索引**：
- `PRIMARY KEY (image_id)`

---

### 📊 10. sys_user（系统用户表）

**表说明**：若依框架自带的用户表，与非遗系统的评论、用户行为强关联

**主键**：`user_id`（BIGINT，自增）

| 字段名 | 数据类型 | 是否主键 | 是否外键 | 允许空 | 默认值 | 说明 |
|--------|---------|---------|---------|--------|--------|------|
| `user_id` | BIGINT | ✅ 是 | - | 否 | 自增 | 用户ID（主键） |
| `dept_id` | BIGINT | - | 外键 | 是 | NULL | 部门ID |
| `user_name` | VARCHAR(30) | - | - | 否 | - | 用户账号 |
| `nick_name` | VARCHAR(30) | - | - | 否 | - | 用户昵称 |
| `user_type` | VARCHAR(2) | - | - | 是 | '00' | 用户类型（00系统用户） |
| `email` | VARCHAR(50) | - | - | 是 | '' | 用户邮箱 |
| `phonenumber` | VARCHAR(11) | - | - | 是 | '' | 手机号码 |
| `sex` | CHAR(1) | - | - | 是 | '0' | 用户性别（0男 1女 2未知） |
| `avatar` | VARCHAR(100) | - | - | 是 | '' | 头像地址 |
| `password` | VARCHAR(100) | - | - | 是 | '' | 密码 |
| `status` | CHAR(1) | - | - | 是 | '0' | 账号状态（0正常 1停用） |
| `del_flag` | CHAR(1) | - | - | 是 | '0' | 删除标志（0代表存在 2代表删除） |
| `login_ip` | VARCHAR(128) | - | - | 是 | '' | 最后登录IP |
| `login_date` | DATETIME | - | - | 是 | NULL | 最后登录时间 |
| `pwd_update_date` | DATETIME | - | - | 是 | NULL | 密码最后更新时间 |
| `create_by` | VARCHAR(64) | - | - | 是 | '' | 创建者 |
| `create_time` | DATETIME | - | - | 是 | NULL | 创建时间 |
| `update_by` | VARCHAR(64) | - | - | 是 | '' | 更新者 |
| `update_time` | DATETIME | - | - | 是 | NULL | 更新时间 |
| `remark` | VARCHAR(500) | - | - | 是 | NULL | 备注 |

**索引**：
- `PRIMARY KEY (user_id)`

---

## 三、表之间的关系（核心重点）

### 关系类型说明

| E-R关系类型 | 图形表示 | 说明 | 数据库实现方式 |
|------------|---------|------|---------------|
| **一对多（1:N）** | 一条线 + 末端分叉（乌鸦脚） | 一个实体对应多个另一实体 | 外键关联 |
| **一对一（1:1）** | 一条直线 | 一个实体对应一个另一实体 | 外键 + 唯一约束 |
| **多对多（M:N）** | 两端都分叉 | 多对多关系（需中间表） | 中间表（关联表） |
| **自关联** | 自己指向自己 | 同一表内的层级关系 | 外键指向自身主键 |

---

### 详细关系列表

#### 1️⃣ 一对多关系（1:N）- 共7条

| 序号 | 主表（1端） | 从表（N端） | 关系说明 | 外键字段 | 业务含义 |
|------|------------|------------|---------|---------|---------|
| 1 | `heritage_category` | `heritage_item` | 一个分类包含多个展品 | `heritage_item.category_id` → `heritage_category.category_id` | 分类下可有多个展品，展品属于一个分类 |
| 2 | `heritage_item` | `heritage_video` | 一个展品包含多个视频 | `heritage_video.item_id` → `heritage_item.item_id` | 展品可关联多个视频资源 |
| 3 | `heritage_item` | `heritage_audio` | 一个展品包含多个音频 | `heritage_audio.item_id` → `heritage_item.item_id` | 展品可关联多个音频资源 |
| 4 | `heritage_item` | `heritage_comment` | 一个展品有多个评论 | `heritage_comment.item_id` → `heritage_item.item_id` | 展品可被多个用户评论 |
| 5 | `heritage_item` | `heritage_user_action` | 一个展品有多个用户行为 | `heritage_user_action.item_id` → `heritage_item.item_id` | 展品可被多个用户点赞/收藏 |
| 6 | `heritage_category` | `heritage_inheritor` | 一个分类有多个传承人 | `heritage_inheritor.category_id` → `heritage_category.category_id` | 分类下可有多个传承人 |
| 7 | `heritage_gallery` | `heritage_gallery_image` | 一个图册包含多张图片 | `heritage_gallery_image.gallery_id` → `heritage_gallery.gallery_id` | 图册包含多张明细图片 |

**绘图方式**：
- 从主表（1端）画直线到从表（N端）
- 在主表端标注 `1`
- 在从表端绘制**乌鸦脚符号**（三条短线分叉）或标注 `N` / `*`

---

#### 2️⃣ 多对一关系（M:1）- 共2条

| 序号 | 主表（1端） | 从表（M端） | 关系说明 | 外键字段 | 业务含义 |
|------|------------|------------|---------|---------|---------|
| 8 | `sys_user` | `heritage_comment` | 一个用户可发布多条评论 | `heritage_comment.user_id` → `sys_user.user_id` | 用户可评论多个展品 |
| 9 | `sys_user` | `heritage_user_action` | 一个用户可产生多个行为 | `heritage_user_action.user_id` → `sys_user.user_id` | 用户可点赞/收藏多个展品 |

**绘图方式**：
- 从 `sys_user` 画直线到从表
- 在 `sys_user` 端标注 `1`
- 在从表端绘制**乌鸦脚符号**或标注 `N` / `*`

---

#### 3️⃣ 自关联关系 - 共2条

| 序号 | 表名 | 关系说明 | 外键字段 | 业务含义 |
|------|------|---------|---------|---------|
| 10 | `heritage_category` | 分类树形结构（父子关系） | `parent_id` → `category_id` | 一个分类可有多个子分类，一个子分类只有一个父分类 |
| 11 | `heritage_comment` | 评论回复功能（预留） | `parent_id` → `comment_id` | 一条评论可有多个回复 |

**绘图方式**：
- 从表顶部画线弯曲回到自身顶部
- 在箭头端绘制**乌鸦脚符号**（表示"多"）
- 在线旁标注 "父子关系" 或 "回复关系"

---

### 关系汇总表

| 序号 | 关系类型 | 表A | 表B | 关系描述 | 外键 |
|------|---------|-----|-----|---------|------|
| 1 | 1:N | heritage_category | heritage_item | 分类包含展品 | item.category_id → category.category_id |
| 2 | 1:N | heritage_item | heritage_video | 展品包含视频 | video.item_id → item.item_id |
| 3 | 1:N | heritage_item | heritage_audio | 展品包含音频 | audio.item_id → item.item_id |
| 4 | 1:N | heritage_item | heritage_comment | 展品包含评论 | comment.item_id → item.item_id |
| 5 | 1:N | heritage_item | heritage_user_action | 展品包含用户行为 | action.item_id → item.item_id |
| 6 | 1:N | heritage_category | heritage_inheritor | 分类包含传承人 | inheritor.category_id → category.category_id |
| 7 | 1:N | heritage_gallery | heritage_gallery_image | 图册包含图片 | image.gallery_id → gallery.gallery_id |
| 8 | 1:N | sys_user | heritage_comment | 用户发布评论 | comment.user_id → user.user_id |
| 9 | 1:N | sys_user | heritage_user_action | 用户产生行为 | action.user_id → user.user_id |
| 10 | 自关联 | heritage_category | heritage_category | 分类树形结构 | category.parent_id → category.category_id |
| 11 | 自关联 | heritage_comment | heritage_comment | 评论回复功能 | comment.parent_id → comment.comment_id |

---

## 四、E-R图布局建议

### 推荐布局（中心化设计）

```
                      ┌──────────────────┐
                      │ heritage_category│
                      │  (非遗分类)       │
                      └─────┬────────┬───┘
                            │1       │1
                    ┌───────┘        └────────┐
                    │N                        │N
         ┌──────────▼──────┐         ┌────────▼─────────┐
         │  heritage_item  │         │heritage_inheritor│
         │  (非遗展品)      │         │  (传承人)         │
         └──┬──┬──┬───────┘         └──────────────────┘
            │  │  │  │
        ┌───┘  │  │  └─────────────────────┐
        │N     │N │N                       │N
┌───────▼──┐ ┌─▼──┐ ┌───────┐     ┌───────▼──────────┐
│heritage_ │ │heritage│heritage│     │heritage_user_  │
│ video    │ │ audio  │comment│     │ action           │
│(视频)     │ │(音频)  │(评论)  │     │ (用户行为)        │
└──────────┘ └──────┘ └───┬───┘     └────────┬─────────┘
                          │                   │
                          │N                  │N
                   ┌──────▼──────┐      ┌─────▼──────┐
                   │  sys_user   │      │  sys_user  │
                   │  (用户)      │      │  (用户)     │
                   └─────────────┘      └────────────
                   
         ┌──────────────────┐
         │ heritage_gallery │
         │  (图集主表)       │
         └──────┬───────────┘
                │1
                │
                │N
         ┌──────▼──────────────────┐
         │heritage_gallery_image   │
         │  (图集从表)              │
         └─────────────────────────┘
```

### 布局原则

1. **核心实体居中**：`heritage_item`（非遗展品）放在中央位置
2. **分类置顶**：`heritage_category`（非遗分类）放在顶部中央
3. **一对多关系**：主表在上，从表在下（或主表在左，从表在右）
4. **用户表置底**：`sys_user` 放在底部，表示基础支撑
5. **自关联弯曲**：树形结构用弯曲箭头表示自关联
6. **对齐美观**：保持表格之间的间距均匀，连线尽量水平或垂直

---

## 五、绘图工具推荐

### 在线工具
1. **Draw.io**（推荐）：https://app.diagrams.net/
   - 免费、功能强大
   - 支持导出 PNG、SVG、PDF
   - 内置 E-R 图符号库

2. **ProcessOn**：https://www.processon.com/
   - 中文界面，操作简单
   - 支持在线协作

3. **Lucidchart**：https://www.lucidchart.com/
   - 专业级工具
   - 模板丰富

### 桌面工具
1. **PowerDesigner**（推荐）
   - 专业数据库设计工具
   - 自动生成 E-R 图
   - 支持正向/逆向工程

2. **MySQL Workbench**
   - MySQL 官方工具
   - 可从现有数据库逆向生成 E-R 图
   - 操作：File → Reverse Engineer → 选择数据库

3. **Navicat**
   - 模型功能强大
   - 支持多种数据库

---

## 六、毕业论文 E-R 图规范要点

### 必须包含的元素

✅ **实体矩形框**：表名写在矩形框内（加粗）  
✅ **属性椭圆框**（可选）：重要属性可单独绘制  
✅ **主键标注**：主键字段加下划线或使用 **PK** 标识  
✅ **外键标注**：外键字段标注 **FK**  
✅ **关系线**：使用**乌鸦脚符号**（Crow's Foot）表示一对多  
✅ **基数标注**：在关系线两端标注 `1`、`N` 或 `*`  
✅ **图例说明**：在图的右下角或左下角添加图例  
✅ **实体名称**：使用中文名称（如"非遗展品"）  

### E-R 图符号说明

| 符号 | 含义 | 绘图方式 |
|------|------|---------|
| 矩形框 | 实体（表） | 长方形，内写表名 |
| 椭圆形 | 属性（字段） | 椭圆，内写字段名（可选绘制） |
| 菱形 | 关系 | 菱形，内写关系名（可选绘制） |
| 直线 | 实体与关系连接 | 实线连接 |
| 乌鸦脚 | 一对多（N端） | 三条短线分叉 |
| 单线 | 一端（1） | 普通直线 |
| 双菱形 | 多对多关系 | 两端都分叉 |

### 常见错误避免

❌ 忘记标注主键/外键  
❌ 关系线两端不标注基数（1、N）  
❌ 混淆一对多和多对多关系  
❌ 自关联关系未使用弯曲箭头  
❌ 实体名称不使用中文  
❌ 遗漏核心业务表  
❌ 包含过多系统表（菜单、日志等）  
❌ 关系线交叉过多（布局混乱）  

---

## 七、核心业务关系说明

### 1. 展品为核心（Center Entity）

`heritage_item`（非遗展品）是整个系统的核心实体，所有业务都围绕展品展开：

```
展品关联关系：
├── 属于某个分类（heritage_category）
├── 包含多个视频（heritage_video）
├── 包含多个音频（heritage_audio）
├── 包含多个评论（heritage_comment）
├── 包含多个用户行为（heritage_user_action）
└── 对应一个图册（heritage_gallery）
```

### 2. 分类树形结构

`heritage_category`（非遗分类）采用树形结构：

```
分类层级：
├── 传统技艺（parent_id = 0）
│   ├── 白族扎染（parent_id = 传统技艺ID）
│   ├── 剑川木雕（parent_id = 传统技艺ID）
│   └── 鹤庆银器（parent_id = 传统技艺ID）
├── 传统音乐（parent_id = 0）
│   └── 白族调（parent_id = 传统音乐ID）
└── 民俗活动（parent_id = 0）
    └── 三道茶（parent_id = 民俗活动ID）
```

### 3. 用户交互关系

用户通过 `sys_user` 表与非遗系统交互：

```
用户行为：
├── 发布评论（heritage_comment）
│   └── 可回复他人评论（自关联 parent_id）
└── 点赞/收藏（heritage_user_action）
    └── 唯一约束：同一用户对同一展品只能点赞/收藏一次
```

### 4. 主从表设计

`heritage_gallery` 和 `heritage_gallery_image` 采用主从表设计：

```
主从关系：
heritage_gallery（主表）
├── gallery_id（主键）
├── title（图集标题）
└── 包含多条记录
    └── heritage_gallery_image（从表）
        ├── image_id（主键）
        ├── gallery_id（外键）
        ├── image_url（图片路径）
        └── caption（图片说明）
```

---

## 八、数据库设计特点

### 1. 逻辑删除机制

所有业务表都包含 `del_flag` 字段：
- `'0'`：未删除（正常）
- `'2'`：已删除

**优点**：数据可恢复，便于审计

### 2. 审计字段

所有表都包含审计字段：
- `create_by` / `create_time`：创建者/创建时间
- `update_by` / `update_time`：更新者/更新时间

**优点**：数据追踪，责任明确

### 3. 唯一约束

关键业务表设置唯一约束：
- `heritage_category.category_code`：分类编码唯一
- `heritage_user_action (user_id, item_id, action_type)`：防止重复操作

**优点**：数据一致性保证

### 4. 索引优化

核心查询字段建立索引：
- `heritage_item.category_id`：分类查询
- `heritage_item.status`：状态过滤
- `heritage_video.item_id`：展品关联查询
- `heritage_category.parent_id`：树形查询

**优点**：查询性能优化

---

## 九、绘制步骤指南

### 步骤 1：准备工具
- 打开 Draw.io（https://app.diagrams.net/）
- 选择"创建新图表" → "数据库" → "实体关系"

### 步骤 2：绘制实体（表）
1. 从左侧面板拖拽"实体"形状到画布
2. 双击编辑实体名称（使用中文，如"非遗展品"）
3. 在实体内部添加字段（字段名 + 数据类型）
4. 标注主键（PK）和外键（FK）

### 步骤 3：绘制关系线
1. 选择"关系线"工具
2. 从主表拖拽到从表
3. 在关系线两端设置基数（1、N）
4. 在N端设置"乌鸦脚"符号

### 步骤 4：绘制自关联
1. 从实体顶部拖拽回到自身顶部
2. 设置弯曲路径
3. 在箭头端设置"乌鸦脚"符号
4. 添加文字说明（如"父子关系"）

### 步骤 5：调整布局
1. 移动实体位置，保持间距均匀
2. 调整连线，减少交叉
3. 添加标题（如"白族非遗展示系统 E-R 图"）

### 步骤 6：添加图例
1. 在右下角添加图例框
2. 说明各种符号的含义
3. 标注绘图日期、作者

### 步骤 7：导出图片
1. 文件 → 导出为 → PNG / SVG / PDF
2. 选择高分辨率（300 DPI）
3. 保存到论文目录

---

## 十、检查清单

绘制完成后，请逐项核对：

### 实体完整性
- [ ] 是否包含 9 张非遗业务表？
- [ ] 是否包含 `sys_user` 用户表？
- [ ] 是否排除若依系统表（菜单、角色、日志等）？
- [ ] 每个实体是否标注主键（PK）？
- [ ] 外键字段是否标注（FK）？

### 关系完整性
- [ ] 是否绘制 7 条一对多关系？
- [ ] 是否绘制 2 条用户关联关系？
- [ ] 是否绘制 2 条自关联关系？
- [ ] 关系线两端是否标注基数（1、N）？
- [ ] N端是否使用乌鸦脚符号？
- [ ] 自关联是否使用弯曲箭头？

### 规范性
- [ ] 实体名称是否使用中文？
- [ ] 是否包含图例说明？
- [ ] 是否包含标题？
- [ ] 布局是否清晰美观？
- [ ] 关系线是否尽量避免交叉？
- [ ] 是否标注外键关联字段？

### 业务逻辑
- [ ] `heritage_item` 是否处于中心位置？
- [ ] `heritage_category` 是否处于顶部？
- [ ] `sys_user` 是否处于底部？
- [ ] 主从表是否清晰区分（gallery → gallery_image）？
- [ ] 分类树形结构是否体现自关联？

---

## 十一、完整关系总结表

| 序号 | 关系类型 | 主表（1端） | 从表（N端） | 外键字段 | 关系说明 |
|------|---------|------------|------------|---------|---------|
| 1 | 1:N | heritage_category | heritage_item | item.category_id → category.category_id | 分类包含展品 |
| 2 | 1:N | heritage_item | heritage_video | video.item_id → item.item_id | 展品包含视频 |
| 3 | 1:N | heritage_item | heritage_audio | audio.item_id → item.item_id | 展品包含音频 |
| 4 | 1:N | heritage_item | heritage_comment | comment.item_id → item.item_id | 展品包含评论 |
| 5 | 1:N | heritage_item | heritage_user_action | action.item_id → item.item_id | 展品包含用户行为 |
| 6 | 1:N | heritage_category | heritage_inheritor | inheritor.category_id → category.category_id | 分类包含传承人 |
| 7 | 1:N | heritage_gallery | heritage_gallery_image | image.gallery_id → gallery.gallery_id | 图册包含图片 |
| 8 | 1:N | sys_user | heritage_comment | comment.user_id → user.user_id | 用户发布评论 |
| 9 | 1:N | sys_user | heritage_user_action | action.user_id → user.user_id | 用户产生行为 |
| 10 | 自关联 | heritage_category | heritage_category | category.parent_id → category.category_id | 分类树形结构 |
| 11 | 自关联 | heritage_comment | heritage_comment | comment.parent_id → comment.comment_id | 评论回复功能 |

---

## 十二、数据库表关系 SQL 验证

### 外键关系验证 SQL

```sql
-- 1. 展品与分类关联
SELECT i.item_name, c.category_name 
FROM heritage_item i 
JOIN heritage_category c ON i.category_id = c.category_id;

-- 2. 展品与视频关联
SELECT i.item_name, v.title 
FROM heritage_item i 
JOIN heritage_video v ON i.item_id = v.item_id;

-- 3. 展品与音频关联
SELECT i.item_name, a.title 
FROM heritage_item i 
JOIN heritage_audio a ON i.item_id = a.item_id;

-- 4. 展品与评论关联
SELECT i.item_name, c.content, u.nick_name 
FROM heritage_item i 
JOIN heritage_comment c ON i.item_id = c.item_id 
JOIN sys_user u ON c.user_id = u.user_id;

-- 5. 展品与用户行为关联
SELECT i.item_name, u.nick_name, ua.action_type 
FROM heritage_item i 
JOIN heritage_user_action ua ON i.item_id = ua.item_id 
JOIN sys_user u ON ua.user_id = u.user_id;

-- 6. 分类与传承人关联
SELECT c.category_name, inh.name, inh.level 
FROM heritage_category c 
JOIN heritage_inheritor inh ON c.category_id = inh.category_id;

-- 7. 图册主从表关联
SELECT g.title, gi.image_url, gi.caption 
FROM heritage_gallery g 
JOIN heritage_gallery_image gi ON g.gallery_id = gi.gallery_id;

-- 8. 分类树形结构
SELECT c1.category_name AS parent, c2.category_name AS child 
FROM heritage_category c1 
JOIN heritage_category c2 ON c1.category_id = c2.parent_id;
```

---

## 十三、参考资料

1. **实际数据库表结构**：`sql/all_table.sql`
2. **E-R 图绘制规范**：
   - 《数据库系统概念》（Silberschatz）
   - 《数据库设计指南》
3. **绘图工具官方文档**：
   - Draw.io：https://www.drawio.com/doc/
   - PowerDesigner：https://www.sap.com/products/powerdesigner.html

---

**文档版本**：v1.0  
**生成日期**：2026-04-19  
**适用系统**：白族非遗智能展示与管理平台  
**适用场景**：本科毕业设计论文 - 数据库设计章节  
**数据来源**：实际数据库表结构（sql/all_table.sql）

---

## 附录：快速参考卡片

### 🎯 核心实体（10个）
```
1. heritage_item        - 非遗展品（核心）
2. heritage_category    - 非遗分类（树形）
3. heritage_video       - 视频档案
4. heritage_audio       - 音频档案
5. heritage_comment     - 评论
6. heritage_user_action - 用户行为
7. heritage_inheritor   - 传承人
8. heritage_gallery     - 图集主表
9. heritage_gallery_image - 图集从表
10. sys_user            - 系统用户
```

### 🔗 关系汇总（11条）
```
一对多（9条）：
- category → item（分类包含展品）
- item → video（展品包含视频）
- item → audio（展品包含音频）
- item → comment（展品包含评论）
- item → user_action（展品包含用户行为）
- category → inheritor（分类包含传承人）
- gallery → gallery_image（图册包含图片）
- user → comment（用户发布评论）
- user → user_action（用户产生行为）

自关联（2条）：
- category → category（分类树形结构）
- comment → comment（评论回复功能）
```

### 📐 绘图符号
```
矩形框      → 实体（表）
直线        → 关系连接
乌鸦脚      → 一对多的"多"端（N）
单线        → 一端（1）
弯曲箭头    → 自关联
PK 标识     → 主键
FK 标识     → 外键
```
