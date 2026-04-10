# Commands and Permissions

Elytra Racing uses a hierarchical permission system to manage access to race controls and participation.

## Admin Commands

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/er setup` | `elytraracing.admin.setup` | Opens the race management dashboard. |
| `/er tool <race>` | `elytraracing.admin.tool` | Gives the admin tool for a specific race. |
| `/er create <name>` | `elytraracing.admin.create` | Creates a new race. |
| `/er delete <name>` | `elytraracing.admin.delete` | Deletes an existing race. |
| `/er start <race>` | `elytraracing.admin.start` | Starts the 5-second countdown for a race. |
| `/er end <race>` | `elytraracing.admin.end` | Forcefully ends an active race. |
| `/er reset <race> [delay]` | `elytraracing.admin.reset` | Resets a race immediately or sets an auto-reset delay. |
| `/er setspawn` | `elytraracing.admin.setspawn` | Sets the spawn point for the race you are editing. |
| `/er list` | `elytraracing.admin.list` | Lists all created races and their associated worlds. |
| `/er enable <race>` | `elytraracing.admin.enable` | Enables a race for joining. |
| `/er disable <race>` | `elytraracing.admin.disable` | Disables a race. |
| `/er rings <race>` | `elytraracing.admin.rings` | Lists the locations and indices of all rings in a race. |
| `/er seespectators` | `elytraracing.admin.seespectators`| Toggles visibility of spectators for the admin. |

## Racer and Spectator Commands

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/er join <race>` | `elytraracing.racer.join` | Join a race as a participant. |
| `/er leave` | (None) | Leave the current race or spectating session. |
| `/er spectate <race>` | `elytraracing.admin.spectate` | Join a race as a spectator. |
| `/er tp` | `elytraracing.admin.tp` | Open the teleportation GUI while spectating. |

## Miscellaneous Permissions

*   `elytraracing.admin.*`: Grants access to all administrative commands.
*   `elytraracing.admin.stats`: Allows resetting race statistics (Note: command `/er resetstats` exists but is currently a placeholder).
