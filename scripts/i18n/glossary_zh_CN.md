# 手写相册简体中文术语表

面向中国大陆用户。界面短句简洁直接，按钮优先使用短动词；正文统一称“你”。

| 英文 | 简体中文 | 说明 |
| --- | --- | --- |
| Album / Albums | 相册 | 对应文件夹的相册 |
| Timeline | 时间线 | 不使用“时间轴” |
| Trash | 回收站 | Move to Trash＝移至回收站；Empty＝清空 |
| Vault | 私密相册 | 不使用“保险箱”；与未加密的锁定相册区分 |
| Favorites | 收藏 | Favorite＝收藏；Unfavorite＝取消收藏 |
| Library | 资料库 | 页面和功能入口统一用此名称 |
| Metadata / EXIF | 照片信息 | 照片的拍摄参数、位置等；不将其误写为图片内容 |
| Collection | 合集 | 跨相册的虚拟分组，不是收藏 |
| Smart search | 智能搜索 | CLIP 内容搜索 |
| Smart Scan | 智能扫描 | 分阶段扫描和索引 |
| Smart features | 智能功能 | 设置分类名称 |
| Settings | 设置 | 不使用“设定” |
| Visual search | 以图搜图 | 发送图片至 Google Lens 等外部应用 |
| Vision search | 视觉搜索 | AI 根据图片内容匹配文字描述 |
| Category | 分类 | 自动或自定义分类 |
| People | 人物 | Face＝人脸 |
| Private folder | 私密文件夹 | 不与加密私密相册混用 |
| Capture time / date | 拍摄时间 / 拍摄日期 | 与修改时间区分 |
| Modified time / date | 修改时间 / 修改日期 | 文件的修改时间 |
| Mosaic | 拼贴 | 与整齐网格布局区分 |
| Motion Photo | 动态照片 | 静态照片内含短视频 |
| Story cards | 故事卡片 | 时间线上方的回忆卡片 |
| Backup / Sync | 备份 / 同步 | Upload＝上传；Download＝下载 |
| Account | 账号 | 不混用“帐号”“帐户”“账户” |
| Subject cutout | 主体抠图 | subject 指照片主体，不是“主题” |
| Remove metadata | 移除照片信息 | 不表示删除照片文件 |
| Save a copy | 另存副本 | 明确保留原文件 |
| Delete permanently | 永久删除 | 与可恢复的移入回收站区分 |
| Select all | 全选 | Deselect all＝取消全选 |
| Share | 分享 | 相册成员访问权限使用“共享” |
| ReFra / Refra / Gallery（应用名称） | 手写相册 | `app_name` 按任务要求保持原值；网址和原作者署名不改 |

数量按语境写“项”“张”“个相册”“位人物”，不使用“items”或“个项目”作机械单位。复数资源保留英文已有的全部 quantity，即使中文各项内容相同。

Immich、ownCloud、Nextcloud、WebDAV、SMB、NFS、OpenStreetMap、CARTO、Google Lens 等产品、服务和协议名称保留英文。JPEG、PNG、RAW、HDR10 等标准格式名，以及 URL、版本格式和作者署名保持原样。逐 key 的保留理由见 `changes_zh_CN.md`。

格式占位符逐叶节点保留原有数量、顺序、编号和精度，例如 `%s`、`%1$s`、`%d`、`%.1f`、`%%`。HTML 标签、CDATA 边界、XML 实体和换行、Unicode 等转义由 `zh_cn_audit.py` 校验。
