# Special Rings

Special rings are optional checkpoints that trigger specific actions when a racer passes through them. Unlike regular rings, these do not count towards race completion and do not have to be passed in any specific order.

## Configuration in `config.yml`

Special rings are defined by their material. You can configure them in the `special-rings` section of your `config.yml`.

### Example Configuration

```yaml
special-rings:
  GOLD_BLOCK:
    command: "effect give %player% speed 5 2"
    cooldown: 1000
    global-cooldown: false
    enabled: false
  DIAMOND_BLOCK:
    command: "firework %player%"
    cooldown: 5000
    global-cooldown: true
    enabled: false
```

### Settings Breakdown

| Setting | Type | Description |
| :--- | :--- | :--- |
| `command` | String | The console command to execute. Use `%player%` as a placeholder for the racer's name. |
| `cooldown` | Number | Time in milliseconds before the same ring (based on ID) can be triggered again. |
| `global-cooldown`| Boolean| If `true`, the cooldown applies to *all* racers once the ring is triggered. If `false`, tracking is per-player. |
| `enabled` | Boolean| Quickly toggle the special ring logic for this material without deleting the config. |

## In-Game Behavior

*   **Optional:** Racers do not need to pass these to finish the race or advance to the next index.
*   **Visual Hiding:** While a special ring is on cooldown (either globally or for you specifically), it will be hidden from your view. It reappears once it can be triggered again.
*   **Identification:** In the Ring Configuration GUI, hovering over a material will display its special ring properties (Command, Cooldown, Status) in the tooltip if it's configured as a special ring.
*   **Disabled Rings:** If a special ring configuration is set to `enabled: false`, it will behave exactly like a normal required ring.
