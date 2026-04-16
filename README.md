# 🎥 ExtralyCinematic

*A lightweight, high-performance, and feature-rich cinematic plugin for Minecraft (Paper/Bukkit) servers.*

[**🇻🇳 Xem phiên bản Tiếng Việt ở bên dưới (Scroll down for Vietnamese version)**](#phiên-bản-tiếng-việt-vietnamese-version)

---

## 🇬🇧 ENGLISH VERSION

**ExtralyCinematic** is a professional cutscene and camera movement plugin. Built from scratch using native `BukkitRunnable` and Catmull-Rom Spline interpolation, it provides buttery-smooth camera movements without relying on heavy external libraries like TaskChain.

### ✨ Key Features
* **High Performance:** 100% native Bukkit API, lag-free, and YAML-based storage.
* **Dual Recording Modes:** Supports Free Record (live flying) and Keyframe/Waypoint mode (connecting specific points).
* **Cinematic Effects:** Dolly Zoom (FOV manipulation), Camera Shake, and Target Focus.
* **Immersive Media:** Play Background Music (BGM - Vanilla or custom Resource Pack IDs) and display fully customizable Titles/Subtitles using **MiniMessage** formatting.
* **Action Injection:** Execute custom commands as the player at specific frames.
* **GUI Dashboard:** An intuitive in-game menu to edit, visualize paths, and manage cinematics (`/cinematic edit`).
* **Soft-depends & Hooks:**
  * `PlaceholderAPI`: Full placeholder support in titles, subtitles, and commands.
  * `MythicMobs`: Custom `playcinematic` mechanic for bosses and skills.
  * `WorldGuard`: Trigger cinematics automatically upon entering specific regions.

### 🚀 Quick Start Guide

**Method 1: Free Record (Live Movement)**
1. Type `/cinematic record start <name>`.
2. Fly around your build to capture frames automatically.
3. Type `/cinematic record stop` to save.
4. Type `/cinematic play <player_name> <name>` to view it.

**Method 2: Keyframe / Waypoint (Smooth Spline)**
1. Stand at Point A and look in your desired direction -> Type `/cinematic addframe <name>`.
2. Move to Point B, change your pitch/yaw -> Type `/cinematic addframe <name>`.
3. Add as many frames as you want.
4. Set the total playback duration (e.g., 15 seconds): `/cinematic duration <name> 15`.
5. Play the cinematic. The plugin will automatically calculate a smooth curved path between your frames!

### ⌨️ Commands & Permissions
**Permission for all commands:** `cinematic.cmd`

| Command | Description |
| :--- | :--- |
| `/cinematic edit` | Open the GUI Dashboard. |
| `/cinematic list` | List all available cinematics. |
| `/cinematic play <player> <name>` | Play a cinematic for a specific player. |
| `/cinematic stop <player>` | Force stop a cinematic for a player. |
| `/cinematic delete <name>` | Delete a cinematic permanently. |
| `/cinematic path <name>` | Visualize the camera path with particles. |
| `/cinematic rec <name> <sec>` | Record with a 3-second countdown. |
| `/cinematic record start/stop` | Start/Stop free recording mode. |
| `/cinematic addframe <name>` | Add a manual keyframe at your location. |
| `/cinematic duration <name> <sec>` | Set total duration for Waypoint mode. |
| `/cinematic focus <name> set/clear` | Lock camera to a specific location. |
| `/cinematic shake <name> <intensity>`| Add camera shake effect (0 to disable). |
| `/cinematic zoom <name> <start> <end>`| Add Dolly Zoom FOV effect (-10 to 10). |
| `/cinematic bgm <name> <sound_id/clear>`| Set background music (e.g., `minecraft:music_disc.pigstep`). |
| `/cinematic title/subtitle <name> <frame> <text>` | Add MiniMessage text to a specific frame. |
| `/cinematic addcmd <name> <frame> <cmd>` | Execute a command at a specific frame (supports `%player%`). |

### ⚙️ Configuration

#### 1. `messages.yml`
Customize 100% of the plugin's messages. Fully supports **MiniMessage** formatting.
```yaml
prefix: "<gold>[ExtralyCinematic]</gold> "
play.finished: "<green>Cinematic finished.</green>"
```

#### 2. `worldguard.yml`
Link WorldGuard regions to cinematics. Players will be forced to watch the cutscene once upon entering the region.
```yaml
regions:
  boss_arena_region: BossIntroCinematic
  spawn_city_region: WelcomeCinematic
```

#### 3. `cinematics/` Folder
All cinematics are saved here as individual YAML files for easy manual editing.

### 🔗 Hooks Integration

**MythicMobs Mechanic**
Use the `playcinematic` mechanic in your MythicMobs skills:
```yaml
Boss_Ultimate:
  Skills:
  - playcinematic{cine=MeteorStrike} @PlayersInRadius{r=20} ~onSpawn
```

---
---

## 🇻🇳 PHIÊN BẢN TIẾNG VIỆT (VIETNAMESE VERSION)

**ExtralyCinematic** là plugin tạo cutscene và luồng camera chuyên nghiệp dành cho server Minecraft (Paper/Bukkit). Plugin được code tối ưu hóa từ đầu bằng `BukkitRunnable` và thuật toán nội suy Catmull-Rom Spline, mang đến những thước phim mượt mà chuẩn Hollywood mà không làm lag server.

### ✨ Tính năng nổi bật
* **Hiệu suất cao:** Dùng 100% API bản địa, lưu trữ bằng YAML dễ dàng config.
* **Hai chế độ quay:** Hỗ trợ quay tự do (Free Record) và quay theo điểm chốt (Keyframe/Waypoint).
* **Hiệu ứng Điện ảnh:** Hỗ trợ Dolly Zoom (FOV), Camera Shake (Rung lắc) và Target Focus (Khóa mục tiêu).
* **Âm thanh & Phụ đề:** Tích hợp nhạc nền BGM (Vanilla hoặc Custom Resource Pack) và hệ thống Phụ đề (Title/Subtitle) chuẩn **MiniMessage**.
* **Thực thi Lệnh:** Chạy lệnh tự động (có placeholder `%player%`) tại từng frame cụ thể.
* **GUI Dashboard:** Quản lý toàn bộ tính năng in-game cực tiện lợi (`/cinematic edit`).
* **Tích hợp mạnh mẽ (Soft-depends):** * `PlaceholderAPI`: Hỗ trợ dịch mọi biến PAPI trong Title, Subtitle và Command.
    * `MythicMobs`: Cung cấp mechanic ép người chơi xem phim khi đánh Boss.
    * `WorldGuard`: Tự động kích hoạt phim khi đi vào Region.

### 🚀 Hướng dẫn sử dụng

**Cách 1: Quay tự do (Free Record)**
1. Gõ `/cinematic record start <tên>`.
2. Bay lượn quanh công trình để plugin bắt frame tự động.
3. Gõ `/cinematic record stop` để lưu lại.
4. Gõ `/cinematic play <tên_người_chơi> <tên>` để xem.

**Cách 2: Quay theo điểm chốt (Keyframe / Waypoint)**
1. Đứng tại vị trí A, căn góc nhìn -> Gõ `/cinematic addframe <tên>`.
2. Bay sang vị trí B, xoay mặt đi hướng khác -> Gõ `/cinematic addframe <tên>`.
3. Add bao nhiêu frame tùy thích.
4. Chốt tổng thời gian camera bay qua các điểm (ví dụ 15 giây): `/cinematic duration <tên> 15`.
5. Play! Plugin sẽ tự tính toán vẽ ra một đường cong mượt mà đi qua các frame của bạn.

### ⌨️ Danh sách Lệnh & Phân quyền
**Quyền (Permission) mặc định:** `cinematic.cmd`

| Lệnh | Chức năng |
| :--- | :--- |
| `/cinematic edit` | Mở GUI Dashboard quản lý tổng. |
| `/cinematic list` | Danh sách toàn bộ Cinematic. |
| `/cinematic play <player> <name>` | Phát Cinematic cho người chơi. |
| `/cinematic stop <player>` | Ép dừng Cinematic của một người chơi. |
| `/cinematic delete <name>` | Xóa Cinematic. |
| `/cinematic path <name>` | Hiển thị quỹ đạo bay bằng Particle. |
| `/cinematic rec <name> <sec>` | Quay phim đếm ngược 3 giây. |
| `/cinematic record start/stop` | Bắt đầu/Dừng quay tự do. |
| `/cinematic addframe <name>` | Chấm 1 frame thủ công tại vị trí đang đứng. |
| `/cinematic duration <name> <sec>` | Chỉnh thời lượng tổng (Dành cho Waypoint). |
| `/cinematic focus <name> set/clear` | Khóa camera luôn nhìn vào vị trí bạn đang đứng. |
| `/cinematic shake <name> <cường_độ>`| Chỉnh độ rung lắc (0 = tắt). |
| `/cinematic zoom <name> <start> <end>`| Chỉnh hiệu ứng Dolly Zoom FOV (-10 đến 10). |
| `/cinematic bgm <name> <ID_nhạc/clear>`| Đặt nhạc nền (VD: `minecraft:music_disc.pigstep`). |
| `/cinematic title/subtitle <name> <frame> <text>` | Set Title/Subtitle bằng MiniMessage cho 1 frame. |
| `/cinematic addcmd <name> <frame> <cmd>` | Gắn lệnh chạy tự động vào 1 frame. |

### ⚙️ Hướng dẫn Config

#### 1. File `messages.yml`
File này chứa 100% text phản hồi của plugin. Hỗ trợ **MiniMessage**.
```yaml
prefix: "<gold>[ExtralyCinematic]</gold> "
play.finished: "<green>Đã xem xong Cinematic.</green>"
```

#### 2. File `worldguard.yml`
Chỉ định Cinematic sẽ chạy khi người chơi bước vào một Region của WorldGuard.
```yaml
regions:
  boss_arena: BossIntroCine
  spawn_city: WelcomeCine
```

#### 3. Thư mục `cinematics/`
Nơi chứa file data YAML của từng Cinematic. Bạn có thể mở ra để chỉnh sửa config frame, tọa độ bằng tay rất dễ dàng.

### 🔗 Tích hợp Plugins (Hooks)

**Dùng chung với MythicMobs**
Sử dụng mechanic `playcinematic` trong file kỹ năng của Boss để ép người chơi xung quanh xem Cutscene (rất hợp làm Intro Boss hoặc Ulti).
```yaml
Boss_Chi_Mang:
  Skills:
  - playcinematic{cine=ThienThach} @PlayersInRadius{r=20} ~onSpawn
```
```