# FieldForge

**FieldForge** is a Spigot 1.21.1 plugin that allows players to create and manage vector fields in Minecraft, manipulating entity movement with physics-based forces. Built with industrial-level code, it leverages Net Minecraft Server (NMS) for performance, offers a rich API, and includes a user-friendly GUI and command system.

## Features
- **Vector Field Types**:
  - **Radial**: Applies forces based on the inverse square law (attraction/repulsion).
  - **Linear**: Pushes entities in a configurable direction.
  - **Vortex**: Creates swirling motion with optional environmental effects (e.g., leaf particles).
- **Customization**:
  - Configurable particle effects, sounds, and force limits via `config.yml`.
  - Toggleable visuals and active states.
- **Field Management**:
  - Create fields with optional duration (e.g., `/fieldforge create radial 2 5 10s`).
  - Remove, list, and modify fields (strength) via commands or GUI.
  - Persistence across server restarts in `fields.yml`.
- **Ownership & Permissions**:
  - Fields tied to players with creator UUIDs; admin override available.
  - Granular permission nodes (e.g., `fieldforge.use`, `fieldforge.admin`).
- **Performance**:
  - NMS-powered entity movement and particle rendering for efficiency.
  - Collision detection with a maximum force cap to prevent excessive effects.
- **Events**:
  - `FieldEnterEvent` and `FieldExitEvent` for integration with other plugins.
- **GUI**:
  - Interactive inventory interface (`/fieldforge gui`) to toggle, remove, or adjust field strength.
- **API**:
  - Extensible `FieldForgeAPI` for programmatic field creation and management.

## Requirements
- **Java**: 17
- **Server**: Spigot 1.21.1
- **Build Tool**: Maven

## Installation
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/4K1D3V/FieldForge.git
   cd FieldForge
   ```
2. **Build the Plugin**:
   ```bash
   mvn clean package
   ```
   - Output: `target/FieldForge-1.0.0-SNAPSHOT.jar`
3. **Install on Server**:
   - Copy the JAR to your Spigot server's `plugins/` folder.
   - Start the server: `java -jar spigot-1.21.1.jar`.
4. **Verify**:
   - Check `latest.log` for "FieldForge enabled successfully."

## Usage
### Commands
- **`/fieldforge create <type> <strength> <range> [x y z] [duration]`**  
  Creates a field (e.g., `/fieldforge create linear 1 10 0 1 0 5s` for a 5-second upward push).
- **`/fieldforge remove <index>`**  
  Removes a field by its index.
- **`/fieldforge list`**  
  Lists your fields (all fields for admins).
- **`/fieldforge reload`**  
  Reloads `config.yml`.
- **`/fieldforge modify strength <index> <value>`**  
  Adjusts field strength (e.g., `/fieldforge modify strength 0 3`).
- **`/fieldforge toggle <index>`**  
  Toggles field visuals.
- **`/fieldforge activate|deactivate <index>`**  
  Toggles field active state.
- **`/fieldforge gui`**  
  Opens the management GUI (requires `fieldforge.gui` permission).

### Permissions
- `fieldforge.use`: Basic command access (default: op).
- `fieldforge.admin`: Manage all fields (default: op).
- `fieldforge.gui`: Use the GUI (default: op).

### GUI
- **Open**: `/fieldforge gui`
- **Actions**:
  - **Left-click**: Toggle active state.
  - **Right-click**: Remove field.
  - **Shift+Left**: Increase strength by 1.
  - **Shift+Right**: Decrease strength by 1.

## Configuration
Edit `plugins/FieldForge/config.yml`:
```yaml
vector-fields:
  max-per-player: 3          # Max fields per player
  default-range: 10          # Default range in blocks
  particle-density: 0.5      # Particle spacing (lower = denser)
  max-force: 5.0             # Max total force per entity
  sound-effects:
    radial: "minecraft:entity.ender_eye.ambient"
    linear: "minecraft:entity.ghast.shoot"
    vortex: "minecraft:entity.wither.shoot"
  particle-types:
    radial: "ELECTRIC_SPARK"
    linear: "SWEEP_ATTACK"
    vortex: "SMOKE_NORMAL"
  environmental-effects:
    vortex-leaves: true       # Vortex spawns leaf particles
```

## API
Integrate with FieldForge using the `FieldForgeAPI`:
```java
import pro.akii.ks.core.fieldforge.api.FieldForgeAPI;

public class ExamplePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        FieldForgeAPI api = getServer().getPluginManager().getPlugin("FieldForge").getAPI();
        // Create a 10-second radial field
        api.createRadialField(getServer().getWorld("world").getSpawnLocation(), 2, 5, 200, null);
    }
}
```
Full API methods are documented in `FieldForgeAPI.java`.

## Troubleshooting
- **No Effects**: Ensure you’re opped (`fieldforge.use`) and check `latest.log` for errors.
- **NMS Errors**: Verify Spigot 1.21.1; adjust `NMSUtil.NMS_VERSION` if needed (e.g., `v1_21_R1`).
- **Persistence Issues**: Ensure `fields.yml` world names match server worlds.
- **Performance**: If TPS drops with many fields, increase particle render interval in `ParticleManager` (e.g., `% 10`).

## Contributing
We welcome contributions! Follow these steps:
1. **Fork the Repository**: Click "Fork" on GitHub.
2. **Clone Your Fork**:
   ```bash
   git clone https://github.com/4K1D3V/FieldForge.git
   ```
3. **Create a Branch**:
   ```bash
   git checkout -b feature/your-feature
   ```
4. **Make Changes**: Implement your feature or fix.
5. **Test**: Ensure functionality on Spigot 1.21.1.
6. **Commit**:
   ```bash
   git commit -m "Add your feature description"
   ```
7. **Push**:
   ```bash
   git push origin feature/your-feature
   ```
8. **Pull Request**: Open a PR on GitHub with a detailed description.

### Code Style
- Use Javadoc for all public methods.
- Follow Java naming conventions.
- Maintain modularity and error handling.

## License
This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

## Credits
Developed by [Kit](https://github.com/4K1D3V). Special thanks to the Spigot community for inspiration and support.