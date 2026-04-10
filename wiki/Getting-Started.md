# Getting Started

Setting up your first elytra race is a straightforward process. Follow these steps to get your track up and running.

## 1. Installation

1.  Place the `elytraracing.jar` file in your server's `plugins` folder.
2.  Restart your server to generate the configuration files.
3.  Ensure you have the required permissions (e.g., `elytraracing.admin.setup`).

## 2. Creating a Race

To create a new race, use the following command:
```
/er create <race_name>
```
Example: `/er create SkyDash`

Upon creation, you will automatically be given the **Elytra Racing Tool** for that race.

## 3. Defining the Spawn Point

The spawn point is where players will be teleported when the race starts.
1.  Stand at the desired location and face the direction you want racers to look.
2.  Use `/er setspawn`.

## 4. Building the Track

Use the **Elytra Racing Tool** to add rings to your race:
*   **Right-Click (Air):** Start configuring a new ring in front of you.
*   **Right-Click (Ring):** Open the Ring Configuration GUI for an existing ring.
*   **Left-Click (Air):** Relocate the ring you are currently configuring.

See [[Rings and Borders]] for detailed instructions on managing the track layout.

## 5. Joining and Starting

Once the track is built:
1.  Players can join using `/er join SkyDash`.
2.  Admins can start the countdown with `/er start SkyDash`.

The race will begin after a 5-second countdown, equipping all racers with Elytras and firework rockets.
