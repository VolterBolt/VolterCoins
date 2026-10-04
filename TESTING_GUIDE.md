# VolterCoins Paper Test Server Setup Guide

This guide will help you set up a complete local Paper test server with VolterCoins, Vault, EssentialsX, and other useful plugins for testing.

## Prerequisites

- Java 17+ installed ([download here](https://adoptopenjdk.net/))
- Maven installed (for building VolterCoins)
- ~2GB free disk space
- A Minecraft client (Java Edition)

## Step 1: Create Server Directory Structure

```bash
# Create a new folder for the test server
mkdir PaperTestServer
cd PaperTestServer
```

## Step 2: Download Paper Server JAR

Visit https://papermc.io/downloads/paper and download the latest stable version (1.20.4 recommended).

Place the JAR in your `PaperTestServer` folder and rename it to `paper.jar`.

## Step 3: Initial Server Run

```bash
# Run the server for the first time
java -Xmx2G -Xms1G -jar paper.jar nogui
```

The server will generate files and then stop. This is normal.

## Step 4: Accept EULA

Open `eula.txt` in the `PaperTestServer` folder and change:
```
eula=false
```

to:

```
eula=true
```

Save and close.

## Step 5: Create Plugins Directory and Download Plugins

```bash
mkdir plugins
cd plugins
```

Download these plugins and place them in the `plugins` folder:

### Required Plugins

1. **Vault** (Economy Bridge)
   - Download from: https://www.spigotmc.org/resources/vault.41918/
   - Latest version works with Paper 1.20.4

2. **EssentialsX** (Admin Tools & Player Management)
   - Download from: https://essentialsx.net/downloads.html
   - Download both `EssentialsX` and `EssentialsXSpawn`

3. **VolterCoins** (Your Plugin)
   - Build locally: `mvn clean package` from your VolterCoins project
   - Copy the JAR from `target/VolterCoins-1.0.0.jar` to the `plugins` folder

### Optional Plugins (Recommended)

4. **PlaceholderAPI** (Placeholder Support)
   - Download from: https://www.spigotmc.org/resources/placeholderapi.6245/

5. **WorldEdit** (World Manipulation - useful for testing)
   - Download from: https://dev.bukkit.org/projects/worldedit

6. **LiteBans** (Ban/Mute Management)
   - Download from: https://www.spigotmc.org/resources/litebans.3715/

Your `plugins` folder should look like:
```
plugins/
├── EssentialsX-2.x.x.jar
├── EssentialsXSpawn-2.x.x.jar
├── Vault-1.x.x.jar
├── VolterCoins-1.0.0.jar
├── PlaceholderAPI-5.x.x.jar
└── WorldEdit-7.x.x.jar
```

## Step 6: Configure Server Properties (Optional)

Open `server.properties` and adjust these for testing:

```properties
# Allow creative mode for testing
gamemode=survival
difficulty=peaceful

# Enable PvP if needed
pvp=false

# Server port (default is 25565)
server-port=25565

# Disable autosave for faster iteration
auto-save-interval=0
```

## Step 7: Create Basic Server Configs

Create a `server-icon.png` (optional) or leave as is.

## Step 8: Start the Server

```bash
# From PaperTestServer folder
java -Xmx2G -Xms1G -jar paper.jar nogui
```

Wait for the message:
```
[Server thread/INFO]: Done (X.XXs)! For help, type "help"
```

## Step 9: Connect with Minecraft Client

1. Launch Minecraft Java Edition
2. Multiplayer → Direct Connection
3. Server address: `localhost` or `127.0.0.1`
4. Join

You should spawn in the default world.

## Step 10: Test VolterCoins

### Create Test Players

In the server console, run:
```
op [your-username]
```

### Basic Command Testing

In-game, run these commands:

1. **Check your balance:**
   ```
   /balance
   ```

2. **Give yourself coins (admin):**
   ```
   /coinsadmin give @s 1000
   ```

3. **Check balance again:**
   ```
   /balance
   ```

4. **View leaderboard:**
   ```
   /coins top
   ```

5. **Transfer to another player:**
   - Create a second player account or invite a friend
   ```
   /pay [playername] 100
   ```

6. **Check player stats (admin):**
   ```
   /coinsadmin check [playername]
   ```

### Permission Testing

Test permission enforcement:

```
# Give a player a permission
pex user [playername] add voltercoin.pay.send

# Remove a permission
pex user [playername] remove voltercoin.pay.send
```

## Step 11: Monitor Logs and Database

### Server Console Output

Watch the console for errors:
- Database connection issues
- Permission errors
- Transaction failures

### Check Database

The SQLite database is at:
```
plugins/VolterCoins/data.db
```

You can inspect it with:
- SQLite Browser (GUI): https://sqlitebrowser.org/
- Command line: `sqlite3 plugins/VolterCoins/data.db`

Query balances:
```sql
SELECT uuid, balance FROM player_balances;
SELECT uuid, type, amount, description, timestamp FROM transaction_history;
```

## Step 12: Rebuild and Redeploy

When you make code changes:

1. Stop the server: type `stop` in console
2. Rebuild VolterCoins: `mvn clean package`
3. Copy new JAR to `plugins/`
4. Delete the old JAR (optional)
5. Restart server

## Troubleshooting

### Plugin Won't Load

- Check server console for errors
- Ensure JAR is in `plugins/` folder
- Verify Java version is 17+
- Check `logs/latest.log` for detailed errors

### Database Errors

- Delete `plugins/VolterCoins/` to reset
- Restart server to recreate schema
- Check file permissions on the folder

### Permission Issues

- Confirm player has the permission node
- Use `/pex user [name] list` to check permissions
- Restart server if permissions changed

### Can't Connect

- Verify server is running (check console)
- Ensure firewall isn't blocking port 25565
- Try `127.0.0.1` instead of `localhost`

## Quick Start Script (Optional)

Create `start-server.sh` (Linux/Mac) or `start-server.bat` (Windows):

**Linux/Mac (start-server.sh):**
```bash
#!/bin/bash
cd "$(dirname "$0")"
java -Xmx2G -Xms1G -jar paper.jar nogui
```

**Windows (start-server.bat):**
```batch
@echo off
cd /d "%~dp0"
java -Xmx2G -Xms1G -jar paper.jar nogui
pause
```

Then just double-click or run to start the server.

## Next Steps After Testing

1. Run through the full economy flow
2. Test admin commands
3. Verify permissions work correctly
4. Check database integrity
5. Review console logs for warnings
6. Test edge cases (negative amounts, self-transfer, etc.)
7. Validate PlaceholderAPI integration (if using)
8. Test Vault integration with another economy plugin

## Useful Server Commands

```
/say [message]           - Broadcast message
/give @s [item] [amount] - Give items
/gamemode creative       - Switch to creative mode
/difficulty peaceful     - Disable damage
/time set day            - Set time to day
/weather clear           - Clear weather
/status                  - Server info
op [player]              - Make player admin
deop [player]            - Remove admin
stop                     - Graceful shutdown
```

## File Structure

After setup, your folder should look like:

```
PaperTestServer/
├── paper.jar
├── eula.txt
├── server.properties
├── start-server.sh (or .bat)
├── world/
├── plugins/
│   ├── Vault.jar
│   ├── EssentialsX.jar
│   ├── EssentialsXSpawn.jar
│   ├── VolterCoins-1.0.0.jar
│   ├── PlaceholderAPI.jar
│   ├── config/
│   │   ├── EssentialsX/
│   │   └── VolterCoins/
│   └── VolterCoins/
│       └── data.db
└── logs/
    └── latest.log
```

## Support & Documentation

- Paper Docs: https://docs.papermc.io/
- Vault Docs: https://github.com/milkbowl/Vault/wiki
- EssentialsX Docs: https://essentialsx.net/wiki/
- PlaceholderAPI: https://wiki.placeholderapi.com/

---

**Happy testing! Report any issues or crashes in the console logs.**
