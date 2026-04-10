# Rings and Borders

Effective track management is key to creating engaging races. All track modifications are handled via the **Elytra Racing Tool**.

## Managing Rings

Rings are the checkpoints of your race. They must be passed in numerical order (Index).

### Creating and Moving
1.  **Right-Click (Air):** Creates a ghost ring in front of you.
2.  **Left-Click (Air/Block):** Teleports the ring you are currently configuring to your target location.
3.  **Right-Click (Existing Ring):** Opens the Ring Configuration menu.

### Configuration Menu
*   **Radius:** Adjust the size of the ring.
*   **Orientation:** Cycle between Horizontal, Vertical X, and Vertical Z.
*   **Material:** Choose the block type for the ring. (See [[Special Rings]] for interactive materials).
*   **Index:** Set the passing order. The plugin will warn you if indices conflict.
*   **Delete/Save:** Manage the ring's lifecycle.

## Defining Borders

Borders define the boundaries of your race. If a racer leaves these bounds, they are automatically teleported back to their last checkpoint.

### Creating Borders
1.  **Sneak + Left Click:** Set Position 1 for a new border.
2.  **Sneak + Right Click:** Set Position 2 for a new border.
3.  **Right-Click (Air/Block):** If a selection is active, opens the Border Configuration GUI to add the new border.

### Managing Existing Borders
1.  Target a border edge with your crosshair while holding the tool.
2.  **Right-Click:** Opens the configuration menu for that specific border, allowing you to delete it or clear your current selection.

### Visualization
*   **Lime Glass:** Represents existing saved borders.
*   **Orange Glass:** Represents your current active selection.
*   **Joined Borders:** Borders that overlap will automatically hide their shared edges to create a seamless volume.
