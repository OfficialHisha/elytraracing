# Race Configuration

The Setup Dashboard (`/er setup`) is the central hub for configuring the rules and behavior of your races. Many settings are contextual and apply to the race you are currently editing with the Elytra Racing Tool.

## Global Actions

*   **Create Race:** Triggers the race creation prompt in chat.
*   **Delete Race:** Triggers the race deletion prompt in chat.
*   **Set Spawn:**
    *   If you are editing a race: Sets the teleport point for that race.
    *   Otherwise: Sets the global world spawn point.

## Race-Specific Settings

These options appear only when you have an active editing session (holding a tool or having recently used one).

### Laps
*   Configure the number of laps required to complete the race (1-10).
*   The scoreboard will automatically track the current lap and display the best lap time for races with more than 1 lap.

### Reset Delay
*   Sets a delay (in seconds) for automatically resetting the race after the last player finishes.
*   Cycles through: 0, 5, 10, 30, 60 seconds.

### DNF Timer
*   Determines how long racers have to finish the course after the first player crosses the finish line.
*   Racers who do not finish in time will be marked as **DNF**.
*   Cycles through: 10, 30, 60, 120, 300 seconds.

### Rocket Cooldown
*   Adjusts the replenishment speed of firework rockets given to racers.
*   Cycles through: Default (5s), 1, 2, 3, 5, 10 seconds.
