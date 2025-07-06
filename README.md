# EmperorVanish

A modern, feature-rich vanish plugin for Minecraft servers, designed for staff and moderation teams. Includes a dynamic scoreboard, PlaceholderAPI support, and extensive customization.

## Features

- **Vanish Command**: Toggle your visibility to regular players.
- **Scoreboard**: Real-time sidebar showing online staff, vanished players, and player count.
- **Staff List**: Automatically detects and displays staff members based on permission.
- **Vanished List**: Shows who is currently vanished.
- **PlaceholderAPI Support**: Use placeholders in scoreboard and messages.
- **Hex Color Support**: Use `<#RRGGBB>` for custom colors and `&` for Minecraft color codes.
- **Configurable Messages**: All messages and formats are customizable.
- **Join/Quit Handling**: Optionally hides join/quit messages for vanished players.
- **Tab List/Effects**: Configurable hiding from tab list and visual effects.

## Commands

| Command            | Aliases         | Description                | Permission            |
|--------------------|----------------|----------------------------|-----------------------|
| `/emperorvanish`   | `/v`, `/vanish`| Toggle vanish mode         | `emperorvanish.use`   |

## Permissions

| Permission              | Description                                 | Default |
|------------------------|---------------------------------------------|---------|
| `emperorvanish.use`    | Allows use of vanish command                | op      |
| `emperorvanish.see`    | Allows seeing vanished players and staff    | op      |

## Configuration

Edit `src/main/resources/config.yml` to customize plugin behavior:

```yaml
scoreboard:
  enabled: true
  update-interval: 20 # ticks (20 ticks = 1 second)

staff:
  permission: "emperorvanish.see"

vanish:
  hide-from-tab: true
  disable-interactions: true
  hide-messages: true
  hide-effects: true
```

### Messages
Edit `src/main/resources/messages.yml` to customize all plugin messages, scoreboard lines, and formats. Supports color codes and hex colors.

## Dependencies
- [PaperMC 1.18.2+](https://papermc.io/) (or compatible Spigot server)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (optional, for additional placeholders)

## Installation
1. Build the plugin with Maven:
   ```sh
   mvn clean package
   ```
2. Place the generated `.jar` file from `target/` into your server's `plugins/` folder.
3. Restart or reload your server.
4. Edit `config.yml` and `messages.yml` as needed.

## Usage
- Use `/emperorvanish` (or `/v`, `/vanish`) to toggle vanish mode.
- Staff with `emperorvanish.see` can see vanished players and appear in the staff list.
- The scoreboard updates automatically for all players.

## Customization
- Change scoreboard appearance and lines in `messages.yml`.
- Adjust permissions and staff detection in `config.yml`.
- Use internal placeholders or PlaceholderAPI placeholders in messages and scoreboard lines.

### Internal Placeholders
The plugin includes built-in placeholders that work without PlaceholderAPI:

**Player Placeholders:**
- `{player}` - Player name
- `{player_uuid}` - Player UUID
- `{player_health}` - Current health
- `{player_max_health}` - Maximum health
- `{player_food}` - Food level
- `{player_exp}` - Experience points
- `{player_level}` - Experience level
- `{player_gamemode}` - Current gamemode
- `{player_world}` - Current world name
- `{player_x}`, `{player_y}`, `{player_z}` - Player coordinates

**Server Placeholders:**
- `{server_online}` - Online player count
- `{server_max_online}` - Maximum player capacity
- `{server_tps}` - Server TPS (placeholder value)
- `{server_time}` - Current server time
- `{staff_count}` - Number of online staff
- `{vanished_count}` - Number of vanished players

**Vanish-Specific Placeholders:**
- `{staff_list}` - List of online staff members
- `{vanished_list}` - List of vanished players
- `{online_players}` - Total online player count

## License
This project is provided as-is. Please add a LICENSE file if you wish to specify usage terms.

---

**EmperorVanish** by brofan11 — Modern vanish and staff management for Minecraft servers. 