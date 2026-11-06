# 简体中文修订清单

以本次任务开始时的中文资源为基准。新增数量按资源 key 统计，复数各 quantity 和数组各 item 另由审计逐项核验。

| 资源文件 | 新增 key | 修订已有 key |
| --- | ---: | ---: |
| `app/commonApp/src/androidMain/res/values-zh-rCN/strings.xml` | 0 | 1 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 400 | 325 |
| `app/core/ui/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 109 | 1 |
| `app/feature/albums/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 67 | 0 |
| `app/feature/classifier/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 88 | 0 |
| `app/feature/editor/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 9 | 7 |
| `app/feature/exif/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 59 | 0 |
| `app/feature/help/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 1473 | 0 |
| `app/feature/ignored/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 70 | 0 |
| `app/feature/library/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 32 | 0 |
| `app/feature/picker/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 19 | 0 |
| `app/feature/search/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 11 | 0 |
| `app/feature/settings/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 625 | 0 |
| `app/feature/setup/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 97 | 0 |
| `app/feature/timeline/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 24 | 0 |
| `app/feature/vault/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 89 | 1 |
| `app/feature/viewer/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 97 | 1 |

合计新增 **3269** 个 key，修订已有 **336** 个 key。`app_name` 已核对保持不变。

## 已有译文逐条修订

| 文件 / key | 修订前 | 修订后 |
| --- | --- | --- |
| `app/commonApp/src/androidMain/res/values-zh-rCN/strings.xml` / `app_name_gallery` | 图库 | 手写相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `app_name_gallery` | 图库 | 手写相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `no_media_title` | 这里似乎是空的 | 这里还没有照片或视频 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `empty_trash_title` | 空空如也 | 回收站已清空 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `empty_trash_cd` | 回收站是空的 | 回收站为空 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `error_title` | 出了点差错 :( | 出了点问题 :( |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `metadata` | 元数据 | 照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `settings_trash_summary` | 删除的图片和视频 30 天内将暂存在回收站中。\n如果禁用，删除操作将无法撤回。 | 将支持恢复的已删除照片和视频移入回收站。保留时间由 Android 或媒体服务决定。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `nav_timeline` | 时间轴 | 时间线 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `trash_deletion_warning` | 您删除的照片和视频将在 30 天后被彻底移除。 | 你删除的照片和视频将在 30 天后被彻底移除。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `monthly_timeline_title` | 月度时间轴 | 月度时间线 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `monthly_timeline_summary` | 按月而非具体日期来进行时间轴分组（需要重启应用） | 按月而非按日对时间线分组（需要重启应用） |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `timeline_layout_type` | 时间轴布局 | 时间线布局 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `timeline_layout_type_summary` | 选择网格或马赛克布局 | 选择网格或拼贴布局 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `timeline_layout_mosaic` | 马赛克 | 拼贴 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `group_similar_media_summary` | 在时间轴和相册中将 RAW+JPG 成对文件、编辑后的照片和连拍照片合并在一起 | 在时间线和相册中将 RAW+JPG 成对文件、编辑后的照片和连拍照片合并在一起 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `allow_gif_animation_summary` | 在时间轴和相册网格中播放 GIF 动画。关闭此功能以减轻浏览包含大量 GIF 的文件夹时的卡顿 | 在时间线和相册网格中播放 GIF 动画。关闭此功能以减轻浏览包含大量 GIF 的文件夹时的卡顿 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `use_system_font_description` | 启用此功能后，应用将使用设备的系统字体，而非内置的 Google Sans 字体。如果您使用自定义系统字体，这将有助于提高可读性。 | 开启此功能后，应用将使用设备的系统字体，而非内置的 Google Sans 字体。如果你使用自定义系统字体，这将有助于提高可读性。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `color_palette_preview_description` | 图标、文字等元素的颜色将与您的壁纸相匹配 | 图标、文字等元素的颜色将与你的壁纸相匹配 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `edit_metadata` | 编辑元数据 | 编辑照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `s_items` | %1$s 项目 | %1$s 项 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `trashing_items` | 正在回收 %1$s 个项目.. | 正在将 %1$s 项移入回收站… |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `deleting_items` | 正在删除 %1$s 个项目.. | 正在删除 %1$s 项… |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `restoring_items` | 正在恢复 %1$s 个项目.. | 正在恢复 %1$s 项… |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `permission_manage_media_summary` | "当你要求对媒体进行多项更改时（切换收藏、管理垃圾箱、编辑元数据等），系统会自动发出确认对话框。" | "批量收藏、移入回收站或编辑时，跳过额外的系统确认对话框。" |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `read_media_images_summary` | 允许应用程序显示您的图像 | 允许应用显示你的图片 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `read_media_videos_summary` | 允许应用程序显示您的视频 | 允许应用显示你的视频 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `access_media_location_summary` | <![CDATA[允许应用程序读取图像和视频的位置数据]]> | <![CDATA[允许应用读取图片和视频的位置数据]]> |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `internet_summary` | <![CDATA[用于显示图像和视频的位置预览]]> | <![CDATA[用于显示图片和视频的位置预览]]> |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `auto_contrast_summary` | 根据图像亮度调整按钮对比度 | 根据图片亮度调整按钮对比度 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `auto_contrast_description` | 持续采集屏幕以检测图像亮度，并据此调整媒体查看器的界面颜色。在暗色区域，按钮和图标会显示为白色，并带有深色背景；在亮色区域，它们则会切换为黑色，并带有浅色背景。该功能在浅色和深色主题下均可正常工作。状态栏图标也会随之调整，以匹配其后方图像的亮度。 当您对媒体内容进行缩放、平移和滑动时，界面会实时更新。 | 持续采集屏幕以检测图片亮度，并据此调整媒体查看器的界面颜色。在暗色区域，按钮和图标会显示为白色，并带有深色背景；在亮色区域，它们则会切换为黑色，并带有浅色背景。该功能在浅色和深色主题下均可正常工作。状态栏图标也会随之调整，以匹配其后方图片的亮度。 当你对媒体内容进行缩放、平移和滑动时，界面会实时更新。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `hide_timeline_for_albums` | 隐藏相册的时间轴 | 隐藏相册日期标题 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `revert_to_original` | 恢复至原始图像 | 恢复至原始图片 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `revert_to_original_confirmation` | 这将撤销所有的编辑操作并还原原始图像。此操作无法撤销。 | 这将撤销所有的编辑操作并还原原始图片。此操作无法撤销。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `edit_backups_summary` | 管理已编辑照片的原始图像备份 | 管理已编辑照片的原始图片备份 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `edit_backups_no_backups_summary` | 当您覆盖已编辑的图片时，原始图片将被备份 | 当你覆盖已编辑的图片时，原始图片将被备份 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `edit_backups_delete_all_summary` | 永久删除所有原始图像备份 | 永久删除所有原始图片备份 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `edit_backups_delete_all_confirmation` | 这将永久删除所有原始图像备份。您将无法将任何已编辑的照片还原到原始照片。 | 这将永久删除所有原始图片备份。你将无法将任何已编辑的照片还原到原始照片。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `timeline` | 时间轴 | 时间线 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `launch_on_timeline` | 启动时打开时间轴页面 | 启动时打开时间线 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `permission_manage_files_summary` | "允许应用程序创建、修改和删除之前受系统限制的文件和相册。（包括图片和 DCIM 文件夹以外的相册以及 SD 卡中的相册）" | "允许应用创建、修改和删除原先受系统限制的文件和相册（包括 Pictures、DCIM 之外及 SD 卡中的相册），并显示系统不识别的照片格式，例如 JPEG XL、JPEG 2000 和 Photoshop 文件。" |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault` | 保险箱 | 私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `library` | 库 | 资料库 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `add_media_to_vault_cd` | 将媒体添加到保险箱 | 将媒体添加到私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_icon_cd` | 保险箱 | 私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_locked_cd` | 锁定的保险箱 | 锁定的私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `create_vault_cd` | 创建新的保险箱 | 创建新的私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `switch_vault_cd` | 切换保险箱 | 切换私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `new_vault` | 新建保险箱 | 新建私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `delete_vault` | 删除保险箱 | 删除私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `unknown_vault` | 未知保险箱 | 未知私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ignored_albums_text` | 选择您不想在主时间线、专辑选项卡或两者中看到的相册。\n\n您可以选择单个相册，也可以创建通配符模式以自动隐藏与其匹配的任何相册。 | 选择你不想在主时间线、专辑选项卡或两者中看到的相册。\n\n你可以选择单个相册，也可以创建通配符模式以自动隐藏与其匹配的任何相册。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_location_options_timeline` | 时间轴 | 时间线 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_location_type_subtitle` | 您可以选择单个相册、多个相册或者创建正则表达式模式以匹配多个 | 你可以选择单个相册、多个相册或者创建正则表达式模式以匹配多个 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_type_selection_subtitle` | 选择您要隐藏的相册 | 选择你要隐藏的相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_type_regex_summary` | 使用正则表达式模式匹配自动隐藏您的相册。\n\n继续之前您的模式必须有效。 | 使用正则表达式模式匹配自动隐藏你的相册。\n\n继续之前你的模式必须有效。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_setup_title` | 设置你的保险箱 | 设置私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_setup_subtitle` | <![CDATA[加密您最私密的照片和视频]]> | <![CDATA[加密你最私密的照片和视频]]> |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_setup_name` | 保险箱名称 | 私密相册名称 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_setup_security_error` | 请在设置保险箱前设置手机安全措施。 | 请先设置设备锁屏保护，再创建私密相册。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_setup_summary2` | 保险箱通过您的手机安全机制（如密码或生物识别）进行访问。\n\n其存储内容将采用 AES-256 加密算法进行加密存储。\n温馨提示：加密密钥不会存储在保险箱中，因此您无法访问密钥，也无法将保险箱转移至其它设备。 | 私密相册受设备锁屏（PIN、密码或生物识别）保护。\n\n文件使用 AES-256 加密，验证身份后才能访问。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_setup_decryption_info` | 您的文件是在访问时即时处理的，且绝不会以解密状态存储在设备上。 | 你的文件是在访问时即时处理的，且绝不会以解密状态存储在设备上。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_already_exists` | 保险箱“%1$s”已存在 | 私密相册 \"%1$s\" 已存在 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `click_to_copy` | 点击复制 | 轻点复制 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `delete_metadata` | 删除元数据 | 删除照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `view_all_metadata` | 查看所有元数据 | 查看所有照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `all_metadata` | 所有元数据 | 所有照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `no_metadata_found` | 未找到元数据 | 未找到照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `loading_metadata` | 正在加载元数据…… | 正在加载照片信息…… |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `disclaimer_classification` | 您的媒体不会上传到任何服务器，所有分类工作均在您的设备上本地完成，使用的是 ONNX 运行时和应用程序资源中提供的预训练模型。 | 你的媒体不会上传到任何服务器，所有分类工作均在你的设备上本地完成，使用的是 ONNX 运行时和应用资源中提供的预训练模型。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_download_summary` | 下载图像搜索和分类的 AI 模型（约 147 MB） | 下载图片搜索和分类的 AI 模型（约 147 MB） |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_delete_confirm` | 您确定要删除这些 AI 模型吗？若要继续使用 AI 功能，您需要重新下载这些模型。 | 你确定要删除这些 AI 模型吗？若要继续使用 AI 功能，你需要重新下载这些模型。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_description` | AI 模型支持智能图片搜索和自动媒体分类。本应用采用 ONNX 格式的 CLIP（对比式语言-图像预训练）模型来理解照片的内容。 | AI 模型支持智能图片搜索和自动媒体分类。本应用采用 ONNX 格式的 CLIP（对比式语言-图片预训练）模型来理解照片的内容。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_privacy_description` | 所有 AI 处理均在您的设备上完成。图片、搜索查询或分类数据绝不会离开您的手机。模型通过 ONNX 运行时在本地运行，下载后无需网络连接。您的媒体内容始终完全私密。 | 所有 AI 处理均在你的设备上完成。图片、搜索查询或分类数据绝不会离开你的手机。模型通过 ONNX 运行时在本地运行，下载后无需网络连接。你的媒体内容始终完全私密。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_feature_search` | 按详情搜索 | 按描述搜索 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `delete_category_confirmation` | 您确定要删除此分类吗？此操作无法撤销。 | 你确定要删除此分类吗？此操作无法撤销。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `category_helper_text` | 输入名称、搜索词并调整精确度，以创建您的分类 | 输入名称、搜索词并调整精确度，以创建你的分类 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `quick_start_subtitle` | 点击建议以开始 | 轻点建议以开始 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `date_header_summary` | 设置您自己的自定义日期格式 | 设置你自己的自定义日期格式 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `exif_date` | Exif 日期 | 照片信息日期 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `shared_elements` | 为媒体项目应用动画 | 照片和视频动画 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `shared_elements_summary` | 用共同元素过渡来作为媒体项目的点击/返回动画 | 打开或关闭查看器时，使用共享元素过渡动画 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_notification_channel_name` | 保险箱操作 | 私密相册操作 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `decrypt_vault` | 解密保险箱 | 解密私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `restore_vault_summary` | 当前保险箱及其内容将被解密并恢复。\n此操作将使您的内容在图库中再次可用。 | 当前私密相册及其内容将被解密并恢复。\n此操作将使你的内容在图库中再次可用。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_deletion_summary` | 当前保险箱及其内容将被永久删除。\n此操作无法撤销。 | 当前私密相册及其内容将被永久删除。\n此操作无法撤销。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `create_new_vault_title` | 创建新的保险箱？ | 创建新的私密相册？ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `create_new_vault_summary` | 保险箱将通过您的手机安全措施（密码或生物识别）进行访问。\n\n加密密钥可以在保险箱内部获取，并用于恢复任何保险箱。 | 将创建新的私密相册，受设备锁屏（PIN、密码或生物识别）保护。\n\n文件使用 AES-256 加密，验证身份后才能访问。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `select_a_vault` | 请选择保险箱 | 请选择私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_select_subtitle` | 选择要打开的保险箱 | 选择要打开的私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_add_to_vault_title` | 添加到保险箱 | 添加到私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_encrypt_behavior` | 保险箱加密行为 | 私密相册加密行为 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_encrypt_behavior_summary` | 选择添加媒体到保险箱时原文件的处理方式 | 选择添加媒体到私密相册时原文件的处理方式 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_custom_password_summary` | 为该保险箱设置独立密码，而非使用设备安全设置 | 为该私密相册设置独立密码，而非使用设备安全设置 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_remove_custom_password_summary` | 这将删除自定义密码，并恢复使用设备安全设置来解锁此保险箱 | 这将删除自定义密码，并恢复使用设备安全设置来解锁此私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_actions` | 保险箱操作 | 私密相册操作 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_enter_password` | 输入保险箱密码 | 输入私密相册密码 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_password_set` | 保险箱密码已成功设置 | 私密相册密码已成功设置 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_password_removed` | 已移除保险箱密码，使用设备安全设置 | 已移除私密相册密码，使用设备安全设置 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_unlock` | 解锁保险箱 | 解锁私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_create_first` | 目前还没有保险箱。创建以隐藏您的媒体文件。 | 还没有私密相册。创建一个来隐藏照片和视频。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_create_vault` | 创建保险箱 | 创建私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `verify_identity` | 验证您的身份 | 验证你的身份 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_hide_no_local_items` | 无法将云端项目添加到保险箱中 | 无法将云端项目添加到私密相册中 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_setup_password_prompt` | 您想为这个保险箱设置自定义密码吗？ | 你想为这个私密相册设置自定义密码吗？ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_deletion_title_named` | 删除“%1$s”？ | 删除 \"%1$s\"？ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_deletion_summary_named` | 保险箱“%1$s”的内容将被永久删除。\n此操作无法撤销。 | 私密相册 \"%1$s\" 及其全部内容将被永久删除。\n此操作无法撤销。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_confirm_restore_summary` | 选中的媒体文件将被解密并还原到您的图库中。\n该文件将从保险箱中移除。 | 选中的媒体文件将被解密并还原到你的图库中。\n该文件将从私密相册中移除。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_confirm_delete_items_title` | 从保险箱中删除？ | 从私密相册中删除？ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_confirm_delete_items_summary` | 所选项目将从保险箱中永久删除。\n此操作无法撤销。 | 所选项目将从私密相册中永久删除。\n此操作无法撤销。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_gate_setup_title` | 保险箱选择页面安全性 | 私密相册入口安全设置 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_gate_setup_subtitle` | 您想保护保险箱选择页面吗？ | 保护私密相册选择页面？ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_gate_setup_summary` | 在您的保险箱列表可见之前增加一层额外的安全层。 | 在显示私密相册列表前增加身份验证。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `collecting_metadata` | 正在收集元数据… | 正在收集照片信息… |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `refresh_metadata` | 刷新元数据 | 刷新照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_category_summary` | 控制您想使用的智能功能 | 控制你想使用的智能功能 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `monthly_timeline_description` | 将您的所有照片和视频按月而非按具体日期进行分组。这样在浏览大型图库时能提供更简洁的概览，但您将无法查看按日分类的内容。\n\n更改此设置后，应用将重新启动。 | 将你的所有照片和视频按月而非按具体日期进行分组。这样在浏览大型图库时能提供更简洁的概览，但你将无法查看按日分类的内容。\n\n更改此设置后，应用将重新启动。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `group_similar_media_description` | 自动将相关照片合并 — 包括 RAW+JPG 成对文件、编辑后的副本以及连拍照片序列。启用此功能后，网格中仅显示主照片；合并项目将在媒体查看器中显示。 | 自动将相关照片合并 — 包括 RAW+JPG 成对文件、编辑后的副本以及连拍照片序列。开启此功能后，网格中仅显示主照片；合并项目将在媒体查看器中显示。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `allow_gif_animation_description` | 启用时，GIF 图片的动画将直接在时间轴和相册网格中播放。关闭此功能以显示静态缩略图，减轻浏览包含大量 GIF 的文件夹时的卡顿。 | 开启时，GIF 图片的动画将直接在时间线和相册网格中播放。关闭此功能以显示静态缩略图，减轻浏览包含大量 GIF 的文件夹时的卡顿。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `merge_albums_by_name_description` | 将不同存储位置中名称相同的相册合并为一个相册。如果您将媒体文件分散存储在内部存储和SD卡中，此功能将非常有用。 | 将不同存储位置中名称相同的相册合并为一个相册。如果你将媒体文件分散存储在内部存储和SD卡中，此功能将非常有用。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `show_favorite_button_description` | 在媒体查看器工具栏中显示一个心形按钮，可让您快速将当前照片添加到收藏夹或从收藏夹中移除。 | 在媒体查看器工具栏显示心形按钮，方便收藏或取消收藏当前照片。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `auto_play_video_description` | 在媒体查看器中滑动到视频时自动播放。禁用此功能后，需手动点击才能播放。 | 在媒体查看器中滑动到视频时自动播放。关闭此功能后，需手动轻点才能播放。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `video_surface_rebind_description` | 在某些设备上，当隐藏或显示播放控件时，视频画面可能会黑屏，尽管音频仍在播放。此功能会在每次操作变化时重新绑定视频区域，以确保其保持可见。在已知会出现此问题的设备上，该功能默认启用。 | 在某些设备上，当隐藏或显示播放控件时，视频画面可能会黑屏，尽管音频仍在播放。此功能会在每次操作变化时重新绑定视频区域，以确保其保持可见。在已知会出现此问题的设备上，该功能默认开启。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `shared_elements_description` | 启用网格缩略图与全屏媒体查看器之间共享元素的平滑过渡。这使得项目的缩放在视觉上更加连贯。 | 在网格缩略图和全屏查看器之间使用平滑的共享元素过渡，让展开和收起更连贯。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `show_selection_titles_description` | 在底部导航栏的每个项目下方显示文本标签（时间轴、相册、库）。关闭此选项以切换为仅显示图标的简洁导航栏。 | 在底部选择栏的各项下显示文字（时间线、相册、资料库）。关闭后使用更紧凑的纯图标导航。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `secure_mode_description` | 防止应用内容出现在截图、最近使用的应用列表以及屏幕录制中。当您的图库中包含敏感内容时，此功能有助于保护隐私。 | 防止应用内容出现在截图、最近使用的应用列表以及屏幕录制中。当你的图库中包含敏感内容时，此功能有助于保护隐私。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `trash_enabled_description` | 启用时，已删除的照片和视频将被移至回收站，并在永久删除前保留 30 天。禁用时，删除操作将立即生效且无法撤销。 | 开启时，已删除的照片和视频将被移至回收站，并在永久删除前保留 30 天。关闭时，删除操作将立即生效且无法撤销。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `follow_system_theme_description` | 启用后，应用将自动遵循设备的深色/浅色模式设置。在下方禁用此功能以手动选择深色或浅色模式。 | 开启后，应用将自动遵循设备的深色/浅色模式设置。在下方关闭此功能以手动选择深色或浅色模式。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `dark_mode_description` | 强制应用使用深色模式并忽略系统设置。仅在“遵循系统主题”功能被禁用时可用。 | 强制应用使用深色模式并忽略系统设置。仅在“遵循系统主题”功能被关闭时可用。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `timeline_layout_description` | 选择照片在时间轴网格中的排列方式。网格布局会显示大小一致的缩略图，而马赛克布局则显示不同大小的缩略图以呈现更具动态感的视觉效果。 | 选择时间线中照片的排列方式。网格布局使用大小一致的缩略图，拼贴布局根据照片比例使用不同大小的图块。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `default_editor_description` | 选择点击“编辑”按钮时打开的应用程序。您可以使用内置编辑器，或者设备上已安装的任何兼容的外部照片编辑器。 | 选择轻点“编辑”按钮时打开的应用。你可以使用内置编辑器，或者设备上已安装的任何兼容的外部照片编辑器。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `launch_screen_description` | 选择打开应用时显示的页面。您可以选择特定的页面，或者让应用记住您上次使用的页面。 | 选择打开应用时显示的页面。你可以选择特定的页面，或者让应用记住你上次使用的页面。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `metadata_collecting` | 正在收集元数据… | 正在收集照片信息… |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `metadata_collecting_progress` | 正在收集元数据… %1$d%% | 正在收集照片信息… %1$d%% |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `metadata_idle` | 点击以刷新媒体库中的元数据 | 轻点刷新资料库中的照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `show_selection_titles` | 显示选择项的标题 | 显示选择操作名称 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `action_desc_info` | 查看所选项目的元数据和详细信息 | 查看所选项目的照片信息和详细信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `action_desc_collection` | 将选中的项目添加到收藏夹或从收藏夹中移除 | 将选中项目添加到合集或从合集中移除 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `action_desc_hide` | 将选中项目加密并隐藏至保险箱 | 将选中项目加密并隐藏至私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `action_desc_download` | 将云端媒体下载到您的设备上 | 将云端媒体下载到你的设备上 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `lock_album_disclaimer` | 锁定相册仅会在本应用内将其隐藏在身份验证之后。内容不会被加密，其他应用和文件管理器仍可访问。\n\n如需加密文件，请改用保险箱功能。 | 锁定相册仅会在本应用内将其隐藏在身份验证之后。内容不会被加密，其他应用和文件管理器仍可访问。\n\n如需加密文件，请改用私密相册功能。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `settings_trash_confirmation_summary` | 开启以在将您的媒体移动到回收站之前需求额外确认 | 开启以在将你的媒体移动到回收站之前需求额外确认 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `item_count` | <item quantity="other">%s 个项目</item> | <item quantity="one">%s 项</item>         <item quantity="other">%s 项</item> |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `album_count` | <item quantity="other">%d 个相册</item> | <item quantity="one">%d 个相册</item>         <item quantity="other">%d 个相册</item> |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `editor_save_subtitle` | 您可以随时撤销这些更改 | 你可以随时撤销这些更改 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `editor_save_copy_subtitle` | 您的原图捕获被修改 | 原图不会改变 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `delete_group_confirm` | 您确定要删除这个分组吗？其中的相册不会被删除。 | 你确定要删除这个分组吗？其中的相册不会被删除。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `delete_section_confirm` | 您确定要删除这个分区吗？其中的相册不会被删除。 | 你确定要删除这个分区吗？其中的相册不会被删除。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `album_sections_detail_description` | 将相册按“常用”、“应用”和自定义类别等分区排列。通过拖拽调整顺序、切换显示状态，或创建自己的分类。 | 将相册按“常用”、“应用”和自定义分类等分区排列。通过拖拽调整顺序、切换显示状态，或创建自己的分类。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `remove_image` | 移除图像 | 移除图片 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `find_similar` | 查找类似图像 | 查找类似图片 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `delete_collection_confirm` | 您确定要删除这个合集吗？其中的媒体不会被删除。 | 你确定要删除这个合集吗？其中的媒体不会被删除。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `help_summary` | 教程、提示和更新内容 | 教程、技巧和更新内容 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `help_whats_new_subtitle` | 探索“图库”的新增功能 | 了解手写相册的新功能 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `help_whats_new_in_version` | v%s 中的更新内容 | v%s 更新内容 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `help_explore_whats_new` | 探索更新内容 | 查看更新内容 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `help_make_most` | 充分利用“图库”的功能 | 更好地使用手写相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `help_cat_timeline_albums` | 时间轴 &amp; 相册设置 | 时间线 &amp; 相册设置 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `help_cat_vault` | 保险箱 | 私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `help_cat_metadata` | 元数据 &amp; EXIF | 照片信息 &amp; 拍摄参数 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `settings_backup_summary` | 导出和导入设置、收藏夹及保险箱 | 导出和导入设置、收藏及私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_include_cloud_configs` | 云服务帐号 | 云服务账号 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_include_vaults` | 保险箱 | 私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_include_vaults_summary` | 保险箱中的全部内容已解密并进入备份 | 将私密相册全部内容解密后纳入备份 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_warning` | 备份文件可能包含已解密的保险箱和帐号凭据。请将其存放在安全的地方。 | 备份文件可能包含已解密的私密相册和账号凭据。请将其存放在安全的地方。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_export_done` | 已导出 %1$d 设置、 %2$d 收藏、 %3$d 云端帐号、 %4$d 保险箱（%5$d 项目）。 | 已导出 %1$d 项设置、%2$d 个收藏、%3$d 个云端账号和 %4$d 个私密相册（%5$d 项内容）。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_import_done` | 已导入 %1$d 设置、 %2$d 云端收藏、 %3$d 云服务帐号、 %4$d 保险箱（%5$d 项目）。 %6$d 本地收藏待确认。 | 已导入 %1$d 项设置、%2$d 个云端收藏、%3$d 个云端账号和 %4$d 个私密相册（%5$d 项内容）。%6$d 个本地收藏待确认。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_export_vault_notice` | 保险箱中的媒体文件将被解密并以二进制形式备份。在导出前，系统会提示您进行保险箱的安全验证。 | 私密相册中的媒体文件将被解密并以二进制形式备份。在导出前，系统会提示你进行私密相册的安全验证。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_export_password_notice` | 为了增加安全性，您可以使用密码对整个备份进行加密。您稍后导入备份时需要该密码 — 该密码无法恢复。 | 为了增加安全性，你可以使用密码对整个备份进行加密。你稍后导入备份时需要该密码 — 该密码无法恢复。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_verify_export_subtitle` | 请验证您的身份以导出保险箱内容 | 请验证你的身份以导出私密相册内容 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_unlock_vault_subtitle` | 解锁“%1$s”以便将其纳入备份 | 解锁 \"%1$s\"，将其纳入备份 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_unlock_vaults_gate_subtitle` | 请进行保险箱安全验证，以便选择要备份的保险箱 | 请进行私密相册安全验证，以便选择要备份的私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_import_pending_favorites` | %1$d 本地收藏等待您确认。 | %1$d 本地收藏等待你确认。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_intro` | 备份您的设置、收藏、云服务帐号和保险箱，或从备份文件中恢复它们。 | 备份你的设置、收藏、云服务账号和私密相册，或从备份文件中恢复它们。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `backup_made_with_version` | 由 ReFra %1$s 创建 | 由 手写相册 %1$s 创建 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `security_metadata_isolation` | 元数据隔离模式 | 照片信息隔离模式 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `security_metadata_isolation_summary` | 控制不同文件之间元数据解析的隔离方式。在所有模式下，解析均在无权限的独立进程中运行 — 区别仅在于该进程的回收频率。\n\n共享进程 — 所有文件均通过单个长期运行的隔离进程进行解析。 这是扫描库时开销最小且速度最快的选项。如果恶意文件导致解析器崩溃，进程会自动重启。建议大多数用户使用。\n\n混合 — 对于在查看器中打开的文件使用独立的隔离进程；对于后台索引则复用共享进程。正在查看的文件间彼此完全隔离，同时批处理操作仍保持高速。 建议在偶尔接收不可信的文件时使用。\n\n按文件 — 对每个文件都使用独立的隔离进程，并在解析完成后立即销毁。提供最强的隔离性，完全避免交叉污染，但会显著拖慢大型库的扫描，且增加耗电。建议在经常处理不可信文件时使用。 | 控制不同文件之间照片信息解析的隔离方式。在所有模式下，解析均在无权限的独立进程中运行 — 区别仅在于该进程的回收频率。\n\n共享进程 — 所有文件均通过单个长期运行的隔离进程进行解析。 这是扫描库时开销最小且速度最快的选项。如果恶意文件导致解析器崩溃，进程会自动重启。建议大多数用户使用。\n\n混合 — 对于在查看器中打开的文件使用独立的隔离进程；对于后台索引则复用共享进程。正在查看的文件间彼此完全隔离，同时批处理操作仍保持高速。 建议在偶尔接收不可信的文件时使用。\n\n按文件 — 对每个文件都使用独立的隔离进程，并在解析完成后立即销毁。提供最强的隔离性，完全避免交叉污染，但会显著拖慢大型库的扫描，且增加耗电。建议在经常处理不可信文件时使用。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `security_sandboxed_decode` | 沙盒化图像解码 | 沙盒化图片解码 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `security_sandboxed_decode_description` | 将复杂图片格式（HEIF, AVIF, JPEG XL）的解码移至一个独立的无权限隔离进程中。 如果解码器遇到格式错误的文件，也不会影响主应用。\n\n这会给图像的初始加载带来少量开销（约 5–15%），但对缓存的缩略图加载没有影响。 | 将复杂图片格式（HEIF, AVIF, JPEG XL）的解码移至一个独立的无权限隔离进程中。 如果解码器遇到格式错误的文件，也不会影响主应用。\n\n这会给图片的初始加载带来少量开销（约 5–15%），但对缓存的缩略图加载没有影响。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `security_enforced_by_advanced_protection` | 由于本设备已启用 Android 高级保护模式，因此已强制应用此设置。选项将在关闭 Android 高级保护模式后恢复可用。 | 由于本设备已开启 Android 高级保护模式，因此已强制应用此设置。选项将在关闭 Android 高级保护模式后恢复可用。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `security_enforced_by_advanced_protection_short` | 由高级保护模式强制启用 | 由高级保护模式强制开启 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `security_private_folder_description` | 指定设备上一个仅能通过本应用访问的文件夹。其内容会完全从普通时间线、相册和搜索结果中隐藏。\n\n工作原理 — 该文件夹通过存储访问框架（SAF）访问，这意味着其内容不会被系统媒体扫描器索引，也不会出现在依赖 MediaStore 的其他图库应用或文件管理器中。当您选择一个文件夹时，其内部会自动创建一个 .nomedia 文件，以防止 Android 扫描其内容。这可确保文件夹中的照片和视频始终对所有其他应用隐藏。\n\n在哪里查看 — 配置后，您的相册列表中会出现一个“私密文件夹”条目。如果您启用了应用锁，打开它时需要身份验证。\n\n您能做什么 — 浏览、查看和删除私密文件夹内的照片和视频。文件在专用查看器中显示，具备与主图库相同的功能（缩放、滑动、元数据）。\n\n选择文件夹 — 您可以选择设备允许的任何文件夹，包括内部存储、SD 卡或 USB 驱动器。应用会在重启后记住您的选择。您可以随时更改或移除该文件夹，而不会影响文件本身。\n\n重要 — 请勿删除私密文件夹内的 .nomedia 文件。若被删除，Android 将扫描该文件夹，其内容可能会出现在其他图库应用中。下次您打开私密文件夹设置时，应用会自动重新创建它。 | 指定设备上一个仅能通过本应用访问的文件夹。其内容会完全从普通时间线、相册和搜索结果中隐藏。\n\n工作原理 — 该文件夹通过存储访问框架（SAF）访问，这意味着其内容不会被系统媒体扫描器索引，也不会出现在依赖 MediaStore 的其他图库应用或文件管理器中。当你选择一个文件夹时，其内部会自动创建一个 .nomedia 文件，以防止 Android 扫描其内容。这可确保文件夹中的照片和视频始终对所有其他应用隐藏。\n\n在哪里查看 — 配置后，你的相册列表中会出现一个\"私密文件夹\"条目。如果你开启了应用锁，打开它时需要身份验证。\n\n你能做什么 — 浏览、查看和删除私密文件夹内的照片和视频。文件在专用查看器中显示，具备与主图库相同的功能（缩放、滑动、照片信息）。\n\n选择文件夹 — 你可以选择设备允许的任何文件夹，包括内部存储、SD 卡或 USB 驱动器。应用会在重启后记住你的选择。你可以随时更改或移除该文件夹，而不会影响文件本身。\n\n重要 — 请勿删除私密文件夹内的 .nomedia 文件。若被删除，Android 将扫描该文件夹，其内容可能会出现在其他图库应用中。下次你打开私密文件夹设置时，应用会自动重新创建它。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `private_folder_security_subtitle` | 您想如何保护私密文件夹？ | 你想如何保护私密文件夹？ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `private_folder_unlock_subtitle` | 请验证身份以访问您的私密文件夹 | 请验证身份以访问你的私密文件夹 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `show_filter_button_description` | 在时间轴页面的搜索栏内显示筛选图标。点击即可打开窗口以按媒体类型、收藏状态、年份或相册筛选您的图库。 | 在时间线页面的搜索栏内显示筛选图标。轻点即可打开窗口以按媒体类型、收藏状态、年份或相册筛选你的图库。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `show_searchbar_favorite_button_description` | 时间轴的搜索栏旁边显示一个心形按钮。点击它即可快速打开您的收藏集。 | 时间线的搜索栏旁边显示一个心形按钮。轻点它即可快速打开你的收藏。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `favorites_info_title` | 您的收藏 | 你的收藏 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `favorites_info_summary` | 长按选择任意照片或视频，点击心形图标将其添加到收藏。添加到此处的项目仅作为快捷方式，不会将其从原相册中移除。 | 长按选择任意照片或视频，轻点心形图标将其添加到收藏。添加到此处的项目仅作为快捷方式，不会将其从原相册中移除。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `date_headers_description` | 日期分隔符按日期将您的媒体内容分组。您可以针对应用的各个模块启用或禁用此功能。 | 日期分隔符按日期将你的媒体内容分组。你可以针对应用的各个模块开启或关闭此功能。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `date_headers_timeline` | 时间轴 | 时间线 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `date_headers_vault` | 保险箱 | 私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `group_method_timeline` | 时间轴 | 时间线 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `group_method_vault` | 保险箱 | 私密相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `story_cards_settings_summary` | 配置时间轴上方的故事卡片 | 配置时间线上方的故事卡片 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `story_cards_enabled_summary` | 在时间轴上方显示故事卡片 | 在时间线上方显示故事卡片 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `settings_cloud_accounts` | 云服务帐号 | 云服务账号 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_no_accounts` | 未配置云服务账户 | 未配置云服务账号 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_no_accounts_description` | 添加一个云端媒体服务器，以便同步和浏览您远程存储的照片和视频。 | 添加一个云端媒体服务器，以便同步和浏览你远程存储的照片和视频。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_api_key_hint` | 输入您的 API 密钥 | 输入你的 API 密钥 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_delete_confirm` | 您确定要移除此服务器吗？本地缓存将被清除。 | 你确定要移除此服务器吗？本地缓存将被清除。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_sync_enabled` | 已启用自动同步 | 已开启自动同步 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_sync_synced_media` | 已同步 %d 媒体项目… | 已同步 %d 项… |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_sync_enabled_toast` | 该相册已启用同步功能 | 该相册已开启同步功能 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_sync_disabled_toast` | 该相册已禁用同步功能 | 该相册已关闭同步功能 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_sync_enable` | 启用同步 | 开启同步 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_sync_disable` | 禁用同步 | 关闭同步 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_no_providers_description` | 在设置 → 云服务账户中添加云服务器以浏览您的远程媒体。 | 在设置 → 云服务账号中添加云服务器以浏览你的远程媒体。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_empty_description` | 您的云服务器中没有照片或视频，或者同步尚未完成。 | 你的云服务器中没有照片或视频，或者同步尚未完成。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_provider_not_available` | 提供商不可用。您是否在构建中启用了它？ | 提供商不可用。你是否在构建中开启了它？ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_archive_info_summary` | 已归档的照片和视频将在您的时间轴上隐藏，但仍可在此处找到 | 已归档的照片和视频将在你的时间线上隐藏，但仍可在此处找到 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_shared_links_delete_message` | 您确定要删除此分享链接吗？ | 你确定要删除此分享链接吗？ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_shared_links_edit_show_metadata` | 显示元数据 | 显示照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_shared_links_edit_show_metadata_badge` | EXIF | 照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_profile_cached_count` | %1$d 本地存储的项目 | %1$d 项已缓存在本地 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_adv_verbose_summary` | 启用调试用的详细日志 | 开启调试用的详细日志 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_adv_prefer_remote` | 偏好远程图像 | 偏好远程图片 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_adv_readonly_summary` | 禁用所有写入操作（选择、共享、删除） | 关闭所有写入操作（选择、共享、删除） |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_adv_clear_cache` | 清除图像缓存 | 清除图片缓存 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_viewer_load_preview` | 加载预览图像 | 加载预览图片 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_accounts` | 云服务帐号 | 云服务账号 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_credentials_summary` | 更改或更新您的服务器凭据。\n更新凭据将导致缓存失效。 | 更改或更新你的服务器凭据。\n更新凭据将导致缓存失效。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `enabled` | 已启用 | 已开启 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `disabled` | 已禁用 | 已关闭 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `trash_confirmation_requires_trash_description` | 请先启用回收站。仅当删除的项目移入回收站时才需要确认。 | 请先开启回收站。仅当删除的项目移入回收站时才需要确认。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `settings_trash_confirmation_requires_trash` | 启用回收站以配置此确认 | 开启回收站以配置此确认 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_offline_clear_message` | 这将移除浏览时产生的自动缓存和已下载的离线副本。您的账号和时间轴不会受到影响。 | 这将移除浏览时产生的自动缓存和已下载的离线副本。你的账号和时间线不会受到影响。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_reason_media_location_optional` | <![CDATA[可选。允许 ReFra 读取照片和视频中嵌入的 GPS 数据，用于地图和位置详情。即使跳过此步骤，图库仍可正常工作。]]> | <![CDATA[可选。允许手写相册读取照片和视频中的 GPS 数据，用于地图和位置详情。跳过后仍可正常使用相册。]]> |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `trash_empty_state_title` | 垃圾箱为空 | 回收站为空 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `action_move_to_trash` | 移动到垃圾箱 | 移动到回收站 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `settings_open_trash` | 打开垃圾箱 | 打开回收站 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `album_trash_confirm_title` | 将相册移动到垃圾箱？ | 将相册移动到回收站？ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `album_trash_confirm_summary` | 此相册中的所有支持项目将移至垃圾箱，可在以后恢复。 | 此相册中的所有支持项目将移至回收站，可在以后恢复。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `map_appearance_description` | 选择地图是跟随 ReFra 的外观还是始终使用明亮或黑暗的地图颜色。 | 选择地图是跟随 手写相册 的外观还是始终使用明亮或黑暗的地图颜色。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `edit_load_failed` | 无法在编辑器中打开此图像。 | 无法在编辑器中打开此图片。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `edit_load_failed_restore_title` | 无法打开图像 | 无法打开图片 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `reencode_quality_summary` | 编辑和旋转的图像保存时如何重新编码 | 编辑和旋转的图片保存时如何重新编码 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_develop_demosaic` | 去马赛克 | 去拼贴 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_export_tiff_unavailable` | TIFF保留完整位深，但无法包含裁剪、滤镜或标记 — 需移除它们以导出TIFF。 | TIFF 可保留完整位深，但无法包含裁剪、滤镜或标注。请移除这些编辑后再导出 TIFF。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `disable_smoothing_title` | 禁用平滑 | 关闭平滑 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `disable_smoothing_summary` | 在放大时显示清晰、方形的像素，而不是模糊、平滑的图像 | 在放大时显示清晰、方形的像素，而不是模糊、平滑的图片 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `disable_smoothing_description` | 启用后，图像将使用最近邻缩放绘制，因此在放大像素艺术时保持清晰锐利的像素，而不会模糊它们。关闭以实现照片的平滑缩放。 | 开启后，图片将使用最近邻缩放绘制，因此在放大像素艺术时保持清晰锐利的像素，而不会模糊它们。关闭以实现照片的平滑缩放。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `remove_metadata` | 移除元数据 | 移除照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `remove_metadata_warning` | 在验证无损清理版本后，原始文件才会被替换。如果替换失败，ReFra 将恢复原文件。 | 在验证无损清理版本后，原始文件才会被替换。如果替换失败，手写相册 将恢复原文件。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `metadata_mode_everything_description` | 在保留渲染或播放媒体所需数据的同时，删除所有可选元数据。 | 在保留渲染或播放媒体所需数据的同时，删除所有可选照片信息。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `remove_metadata_success` | 元数据已删除 | 照片信息已删除 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `remove_metadata_failed` | 无法删除元数据 | 无法删除照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `remove_metadata_rolled_back` | 元数据删除失败。已恢复原始文件。 | 照片信息删除失败。已恢复原始文件。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_feature_faces` | 人脸与人物 | 人脸 &amp; 人物 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_group_search_title` | 搜索与分类 | 搜索 &amp; 分类 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_group_face_detect_desc` | UltraFace RFB-320，一款轻量级的人脸检测器，可驱动编辑器的自动“模糊人脸”操作。 | UltraFace RFB-320 是轻量人脸检测器，用于编辑器的自动 \"模糊人脸\" 功能。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_group_face_recognition_desc` | ArcFace 面部嵌入模型，可在您的照片中将同一个人分组，全程在设备上运行。需要面部检测。 | ArcFace 面部嵌入模型，可在你的照片中将同一个人分组，全程在设备上运行。需要面部检测。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `refresh_all_smart_features_summary` | 顺序运行元数据、搜索、类别和人物 | 顺序运行照片信息、搜索、分类和人物 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `smart_scan_phase_metadata` | 读取元数据 | 读取照片信息 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `smart_scan_phase_categories` | 更新类别 | 更新分类 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `smart_search_index_progress` | 已建立索引 %1$d / %2$d 图像 · 阶段 %3$d / %4$d | 已建立索引 %1$d / %2$d 图片 · 阶段 %3$d / %4$d |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `smart_scan_manager_summary` | 刷新并修复元数据、搜索、类别和人物 | 刷新并修复照片信息、搜索、分类和人物 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `smart_scan_metadata_detail` | 刷新本地和缓存的云元数据而不重建无关的智能功能 | 刷新本地和缓存的云照片信息而不重建无关的智能功能 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `smart_scan_full_refresh_summary` | 即使当前智能功能数据已完整，也重新处理每个项目 | 即使当前智能功能数据已完整，也重新处理每项 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `action_desc_move_to_private_folder` | 将选定项目移入私人文件夹 | 将选定项目移入私密文件夹 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `move_to_private_folder` | 私人文件夹 | 私密文件夹 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `frame_picker_selection_limit` | 您最多可以选择 50 帧 | 你最多可以选择 50 帧 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `pinned_albums_as_grid_description` | 启用时，固定专辑会以与其他专辑相同的网格方格显示（换行显示多行），排列在其他专辑之前。禁用时，它们会显示为宽幅、可水平滚动的横幅轮播。 | 开启时，固定专辑会以与其他专辑相同的网格方格显示（换行显示多行），排列在其他专辑之前。关闭时，它们会显示为宽幅、可水平滚动的横幅轮播。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `private_folder_move_confirm_title` | 移动到私人文件夹？ | 移动到私密文件夹？ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `private_folder_move_confirm_summary` | 所选的项目将被复制到您的私人文件夹中，原件将从您的设备中移除。私人文件夹中的文件对其他应用和媒体库是隐藏的。 | 所选的项目将被复制到你的私密文件夹中，原件将从你的设备中移除。私密文件夹中的文件对其他应用和媒体库是隐藏的。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `private_folder_moving` | 正在移动到私人文件夹… | 正在移动到私密文件夹… |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `private_folder_move_failed` | 无法将项目移动到私人文件夹 | 无法将项目移动到私密文件夹 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `private_folder_move_out_confirm_title` | 要移出私人文件夹吗？ | 要移出私密文件夹吗？ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `private_folder_move_out_confirm_summary` | 所选的项目将恢复到您的图库中，并从私人文件夹中移除。 | 所选的项目将恢复到你的图库中，并从私密文件夹中移除。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `private_folder_moving_out` | 正在从私人文件夹移出… | 正在从私密文件夹移出… |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `private_folder_move_out_failed` | 无法将项目从私人文件夹中移出 | 无法将项目从私密文件夹中移出 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `private_folder_delete_confirm_summary` | 所选项目将被永久从私人文件夹中删除。此操作无法撤销。 | 所选项目将被永久从私密文件夹中删除。此操作无法撤销。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `private_folder_delete_failed` | 无法从私人文件夹中删除项目 | 无法从私密文件夹中删除项目 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_net_ssid_optional` | Wi-Fi 名称（留空以禁用切换） | Wi-Fi 名称（留空以关闭切换） |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_help_immich` | 输入您的 Immich 服务器 URL（包含 http:// 或 https://）。使用您的电子邮件和密码登录，或从账户设置 → API 密钥中粘贴 API 密钥。 | 输入你的 Immich 服务器 URL（包含 http:// 或 https://）。使用你的电子邮件和密码登录，或从账号设置 → API 密钥中粘贴 API 密钥。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_help_owncloud` | 输入您的 ownCloud 服务器 URL（例如 https://cloud.example.com）。使用您的账户用户名和密码登录，如果启用了两步验证，请使用应用密码。 | 输入你的 ownCloud 服务器 URL（例如 https://cloud.example.com）。使用你的账号用户名和密码登录，如果开启了两步验证，请使用应用密码。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_help_nextcloud` | 输入您的 Nextcloud 服务器 URL（例如 https://cloud.example.com）。在浏览器中安全登录，包括两步验证，或者手动使用用户名和应用密码。 | 输入你的 Nextcloud 服务器 URL（例如 https://cloud.example.com）。在浏览器中安全登录，包括两步验证，或者手动使用用户名和应用密码。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_nextcloud_verifying` | 正在验证 Nextcloud 账户… | 正在验证 Nextcloud 账号… |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_nextcloud_connected` | Nextcloud 账户已验证 | Nextcloud 账号已验证 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_help_webdav` | 输入您的主机提供的完整 WebDAV 端点 URL，然后输入您的 WebDAV 用户名和密码。 | 输入你的主机提供的完整 WebDAV 端点 URL，然后输入你的 WebDAV 用户名和密码。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_help_smb` | 使用 smb://host/share/folder。用户名留空以进行访客访问，或输入 user / DOMAIN\\user 及您的密码。共享必须与此设备在同一局域网内。需要 SMB2/3（不支持旧版 SMB1）。 | 使用 smb://host/share/folder。用户名留空以进行访客访问，或输入 user / DOMAIN\\user 及你的密码。共享必须与此设备在同一局域网内。需要 SMB2/3（不支持旧版 SMB1）。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_backed_up_tap_hint` | 点击以管理备份 | 轻点以管理备份 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_http_warning_message` | 该服务器使用未加密的HTTP连接。您的登录信息和照片可能会被网络上的其他人读取或篡改。仅在您信任此网络时继续——例如VPN或Tailscale/WireGuard隧道。仍要继续吗？ | 该服务器使用未加密的HTTP连接。你的登录信息和照片可能会被网络上的其他人读取或篡改。仅在你信任此网络时继续——例如VPN或Tailscale/WireGuard隧道。仍要继续吗？ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_backup_per_account` | 每个账户 | 每个账号 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_backup_dashboard_title` | 备份与同步 | 备份 &amp; 同步 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_backup_overall_caption` | %1$d 个项目，共 %2$d 个项目，跨 %3$d 个服务 | 已处理 %1$d 项，共 %2$d 项，涉及 %3$d 个服务 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_destinations_album_summary` | %1$d 个项目 · %2$d 云端 | %1$d 项 \u00b7 %2$d 个云端目标 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_destinations_album_none` | %1$d 个项目 · 未备份 | %1$d 项 \u00b7 未备份 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_destinations_delete_local_summary` | 只有在每个项目已备份到所有选定的云端后，本地副本才会被删除。 | 只有在每项已备份到所有选定的云端后，本地副本才会被删除。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_destinations_album_backing_up` | %1$d 项 · 正在备份 | %1$d 项 \u00b7 正在备份 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_offline_title` | 离线与缓存 | 离线 &amp; 缓存 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_backup_uploading_short` | 上传中… | 上传中\u2026 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_backup_failed_text` | %1$d 个项目上传失败 | %1$d 项上传失败 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_index_progress_text` | %1$s · %2$d 项目已索引 | %1$s · 已索引 %2$d 项 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_index_progress_multi` | %1$d 个帐户 · %2$d 项目已索引 | %1$d 个账号 · 已索引 %2$d 项 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_upload_group_summary` | %1$d 项 · %2$s | %1$d 项 \u00b7 %2$s |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_upload_pending_total` | %1$d 项 · %2$s 待备份 | %1$d 项 \u00b7 %2$s 待备份 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_net_wifi_sheet_desc` | 选择已连接的 Wi-Fi 或输入名称。留空以禁用网络切换。 | 选择已连接的 Wi-Fi 或输入名称。留空以关闭网络切换。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_net_wifi_disable` | 禁用切换 | 关闭切换 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_net_wifi_blank_summary` | 无网络 — 已禁用切换 | 无网络 — 已关闭切换 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `change_app_logo_summary` | 在 ReFra 和经典图库启动器图标之间切换 | 切换手写相册或经典相册桌面图标 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_welcome_subtitle` | 您的私人本地设备图库 | 你的私人本地相册 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_permissions_subtitle` | ReFra 仅使用显示和管理您的媒体所需的权限。点击权限了解原因。 | 手写相册 仅使用显示和管理你的媒体所需的权限。轻点权限了解原因。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_reason_local_network` | 可选。仅当ReFra直接连接到您当前网络上的服务器时授予此权限。 | 可选。仅当ReFra直接连接到你当前网络上的服务器时授予此权限。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `local_network_permission_dialog_message` | 此服务器解析为您当前本地网络上的设备。Android 在ReFra可以直接连接之前需要权限。 | 此服务器解析为你当前本地网络上的设备。Android 在ReFra可以直接连接之前需要权限。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_reason_read_images` | <![CDATA[在时间轴、相册和查看器中显示你的照片。没有这个，图库将保持为空。]]> | <![CDATA[在时间线、相册和查看器中显示照片。未授权时，相册将为空。]]> |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_reason_read_videos` | <![CDATA[在时间轴、相册和查看器中显示和播放你的视频。]]> | <![CDATA[在时间线、相册和查看器中显示并播放视频。]]> |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_reason_internet` | <![CDATA[正在下载位置预览的地图瓦片，将媒体投射到附近设备，下载可选的AI模型并与云服务器同步。除非您设置了云同步，否则媒体永远不会离开您的设备。]]> | <![CDATA[正在下载位置预览的地图瓦片，将媒体投射到附近设备，下载可选的AI模型并与云服务器同步。除非你设置了云同步，否则媒体永远不会离开你的设备。]]> |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_reason_manage_media` | <![CDATA[在您一次收藏、删除或编辑多个项目时跳过额外的系统确认对话框。]]> | <![CDATA[批量收藏、移入回收站或编辑时，跳过额外的系统确认对话框。]]> |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_reason_manage_files` | <![CDATA[在标准的图片和DCIM文件夹之外创建相册，在您的存储中管理任何位置的文件，并显示系统无法识别的额外照片格式（例如JPEG XL、JPEG 2000和Photoshop文件）。]]> | <![CDATA[在标准 Pictures 和 DCIM 之外创建相册，管理存储中的任意文件，并显示系统不识别的 JPEG XL、JPEG 2000、Photoshop 等照片格式。]]> |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_looks_title` | 外观与感觉 | 外观 &amp; 体验 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_cloud_subtitle` | 可选择连接服务器以备份和同步您的媒体。一切都是可选的，并且在您的控制下。 | 可选择连接服务器以备份和同步你的媒体。一切都是可选的，并且在你的控制下。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_cloud_none` | 您尚未连接云账户。您可以随时从设置中进行此操作。 | 你尚未连接云账号。你可以随时从设置中进行此操作。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_cap_sync` | 备份与同步 | 备份 &amp; 同步 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_cap_people` | 人物与面孔 | 人物 &amp; 人脸 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_cap_map` | 地图与地点 | 地图 &amp; 地点 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_ai_subtitle` | 启用设备上的智能搜索和自动分类。模型完全在您的设备上运行。 | 开启设备上的智能搜索和自动分类。模型完全在你的设备上运行。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_ai_features_title` | 您将获得的内容 | 可用功能 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_ai_feature_private_desc` | 所有操作都在本地进行。您的照片不会离开设备。 | 所有操作都在本地进行。你的照片不会离开设备。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_ai_extra_cutout_desc` | 点击以隔离主体并复制、分享或保存——由 MobileSAM 提供支持。 | 轻点以隔离主体并复制、分享或保存——由 MobileSAM 提供支持。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_ai_faces_title` | 面孔和人物 | 人脸 &amp; 人物 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_ai_faces_people_desc` | 根据照片中的人物对照片进行分组，全部在您的设备上完成。 | 根据照片中的人物对照片进行分组，全部在你的设备上完成。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_tips_title` | 提示与技巧 | 提示 &amp; 技巧 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_tips_subtitle` | 您已准备就绪！ | 准备就绪！ |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `setup_tips_body` | 任何时候都可以从设置中打开帮助页面，发现每个功能的提示和技巧，阅读每个版本的新内容，并充分利用该应用程序。 | 随时从设置打开帮助页面，了解各项功能的技巧和版本更新内容，更好地使用应用。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `library_customize_hint` | 拖动瓷砖以重新排序，拖动侧边手柄调整大小，点击 × 删除。 | 拖动瓷砖以重新排序，拖动侧边手柄调整大小，轻点 × 删除。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `long_press_cutout_description` | 选择在查看器中长按图像时的操作。启用后，长按将启动设备上的主题剪切功能，旋转操作将移到屏幕按钮上。禁用时（默认），长按会旋转图像。你仍然可以从内置编辑器的剪切工具中剪出主题。 | 选择在查看器中长按图片时的操作。开启后，长按会在设备上开始主体抠图，旋转操作改由屏幕按钮完成。关闭时（默认），长按会旋转图片。也可随时使用内置编辑器的抠图工具。 |
| `app/core/ui/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `overwrite_unavailable_summary` | 此图像格式无法在您的设备上重新编码，因此无法覆盖原文件。将改为创建新的 PNG 复制。 | 此图片格式无法在你的设备上重新编码，因此无法覆盖原文件。将改为创建新的 PNG 复制。 |
| `app/feature/editor/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `filter_style_airy` | Airy | 轻盈 |
| `app/feature/editor/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `filter_style_ember` | Ember | 余烬 |
| `app/feature/editor/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `filter_style_luminous` | Luminous | 柔光 |
| `app/feature/editor/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `filter_style_radiant` | Radiant | 明亮 |
| `app/feature/editor/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `filter_style_vivid` | Vivid | 鲜艳 |
| `app/feature/editor/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `tool_black_white` | 黑白 | 黑 &amp; 白 |
| `app/feature/editor/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `type_mosaic` | 马赛克 | 拼贴 |
| `app/feature/vault/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_item_deleted` | 项目已从保险箱中删除 | 项目已从私密相册中删除 |
| `app/feature/viewer/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `decrypt_failed_tap_to_retry` | 解密失败。点击重试。 | 解密失败。轻点重试。 |

## 保留原文的项目

这些项目仍有对应中文资源；没有把它们当作缺失翻译忽略。

| 文件 / key | 原文 | 理由 |
| --- | --- | --- |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_nfs_url_hint` | nfs://host/export/path | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ppi_resolution_string` | " • %1$.1f × %2$.1f %3$s" | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ppcm` | PPCM | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_demosaic_dcb` | DCB | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `resolution_with_mp` | %1$d × %2$d • %3$s MP | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_color_adobe` | Adobe RGB | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `security_private_folder_summary` | %s | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_server_url_hint` | https://your-server.com | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `app_name` | ReFra | 按要求保留品牌资源 app_name，不参与改名。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_source_cutout_url` | huggingface.co/Acly/MobileSAM | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `person_stats_range` | %1$s · %2$s – %3$s | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `frame_picker_timestamp_ms` | %1$d ms | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `mb` | MB | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_source_face_recognition_url` | huggingface.co/garavv/arcface-onnx | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `support_revolut` | Revolut | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_demosaic_ppg` | PPG | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_source_face_detect_url` | github.com/onnx/models · UltraFace | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `github_url` | https://github.com/IacobIonut01/Gallery | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `video_format_dolby_vision` | Dolby Vision | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `kb` | KB | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ppi` | PPI | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `version_build_format` | v%1$s (%2$d) | 版本格式及格式占位符，保持原样。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_smb_url_hint` | smb://host/share/folder | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `video_format_hdr10` | HDR10 | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_type_pin` | PIN | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_offline_megabytes` | %1$d MB | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `settings_navigation_auto_symbol` | A | 自动导航选项的单字符图标标记。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_source_url` | github.com/IacobIonut01/ReFra | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `person_stats_single` | %1$s · %2$s | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `frame_picker_format_jpeg` | JPEG | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `support_paypal` | PayPal | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_demosaic_ahd` | AHD | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_net_external_hint` | https://immich.example.com | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cloud_cap_ocr` | OCR | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `story_counter` | %1$d / %2$d | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `gb` | GB | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_export_jpeg` | JPEG | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `video_format_hlg` | HLG | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_export_png` | PNG | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `version_name_format` | v%1$s | 版本格式及格式占位符，保持原样。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `video_fps` | %.1f fps | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `cast_ip_hint` | 192.168.1.100 | 仅含格式占位符、数值或分隔符，无需翻译。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_color_srgb` | sRGB | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_demosaic_vng` | VNG | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_color_prophoto` | ProPhoto | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `map_attribution` | © OpenStreetMap | 地图来源署名，保留 OpenStreetMap 名称。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `frame_picker_format_png` | PNG | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `frame_picker_index_label` | #%1$d | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/commonApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `app_dev_name` | IacobIonut01 | 原作者署名，保留原文。 |
| `app/core/ui/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `app_name` | ReFra | 按要求保留品牌资源 app_name，不参与改名。 |
| `app/core/ui/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `github_url` | https://github.com/IacobIonut01/Gallery | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/core/ui/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `version_build_format` | v%1$s (%2$d) | 版本格式及格式占位符，保持原样。 |
| `app/core/ui/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `app_dev_name` | IacobIonut01 | 原作者署名，保留原文。 |
| `app/feature/editor/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_export_png` | PNG | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/feature/editor/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `raw_export_jpeg` | JPEG | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/feature/help/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `support_revolut` | Revolut | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/feature/help/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `support_paypal` | PayPal | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/feature/help/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `version_name_format` | v%1$s | 版本格式及格式占位符，保持原样。 |
| `app/feature/ignored/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `continue_with_count` | %1$s (%2$d) | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/feature/search/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `group_type_raw_jpg` | RAW + JPG | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/feature/settings/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `settings_navigation_auto_symbol` | A | 自动导航选项的单字符图标标记。 |
| `app/feature/settings/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_source_url` | github.com/IacobIonut01/ReFra | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/feature/settings/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_source_face_detect_url` | github.com/onnx/models · UltraFace | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/feature/settings/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `security_private_folder_summary` | %1$s | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/feature/settings/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_source_cutout_url` | huggingface.co/Acly/MobileSAM | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/feature/settings/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ai_models_source_face_recognition_url` | huggingface.co/garavv/arcface-onnx | 实际网址或协议路径示例，修改会影响链接或示例含义。 |
| `app/feature/setup/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `version_name_format` | v%1$s | 版本格式及格式占位符，保持原样。 |
| `app/feature/setup/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `app_name` | ReFra | 按要求保留品牌资源 app_name，不参与改名。 |
| `app/feature/vault/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `vault_type_pin` | PIN | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/feature/viewer/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ppi` | PPI | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/feature/viewer/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `video_fps` | %1$.1f fps | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/feature/viewer/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ppi_resolution_string` | " • %1$.1f × %2$.1f %3$s" | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/feature/viewer/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `ppcm` | PPCM | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
| `app/feature/viewer/src/commonMain/composeResources/values-zh-rCN/strings.xml` / `resolution_with_mp` | %1$d × %2$d • %3$s MP | 产品名、标准格式、算法缩写或通用技术单位，保留英文。 |
