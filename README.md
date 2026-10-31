<p align="center">
  <img src="TeamCode\src\main\java\org\firstinspires\ftc\teamcode\assets\swervologo.jpg" alt="SWERVO 26256 Logo" width="240" height="auto">
</p>

# 🦾 SWERVO Team 26256 — 2026-2027 Season Codebase

<p align="left">
  <img src="https://img.shields.io/badge/Platform-FTC%20SDK%20v10.x-blue?style=for-the-badge&logo=android" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Java%2017-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Language">
  
  <a href="https://swervo26256.vercel.app/" target="_blank">
    <img src="https://img.shields.io/badge/Website--Vercel%20Live-000000?style=for-the-badge&logo=vercel&logoColor=white&labelColor=000000" alt="Website">
  </a>
  
  <a href="https://www.instagram.com/ftc26256/" target="_blank">
    <img src="https://img.shields.io/badge/Instagram--@ftc26256-E4405F?style=for-the-badge&logo=instagram&logoColor=white&labelColor=E4405F" alt="Instagram">
  </a>
</p>

Welcome to the official repository for FIRST Tech Challenge (FTC) Team 26256 (SWERVO) based out of Mississauga, Ontario. This repository manages our completely modular software architecture, sensor arrays, computer vision logic, and autonomous routines.

## BIOBUZZ Autonomous: Leave + Park

`BIOBUZZ Leave + Park` is a conservative 30-second autonomous for the currently documented mecanum drivetrain. It uses the goBILDA Pinpoint's two dead-wheel odometry and the Logitech webcam for AprilTag visibility telemetry. It drives 20 inches forward to clear the perimeter wall, then returns to an 8-inch relative offset so the robot remains partly inside the Loading Zone. The route targets the BIOBUZZ autonomous LEAVE (3 points) and PARK (5 points).

The autonomous routine currently performs LEAVE and PARK only; it does not run the shooter or attempt HIVE TIP. Test the route at low speed on a clear field and tune travel distances to the built robot before competition.

### Driver Station hardware configuration

These are the exact configuration names expected by the code. The repository does not include the Control Hub/Expansion Hub port map, which is stored in the robot's Driver Station configuration and must be matched to the wiring on the actual robot.

| Device | FTC device type | Configuration name |
| --- | --- | --- |
| Front-left drive motor | DcMotorEx | `frontLeft` |
| Front-right drive motor | DcMotorEx | `frontRight` |
| Back-left drive motor | DcMotorEx | `backLeft` |
| Back-right drive motor | DcMotorEx | `backRight` |
| goBILDA Pinpoint odometry computer | GoBildaPinpointDriver | `pinpoint` |
| Logitech USB webcam | WebcamName | `camera` |
| Shooter flywheel motor | DcMotorEx | `shooter` |

Configure the four motor names against the ports actually used by the robot. Connect the two odometry pods to the Pinpoint's X (forward) and Y (left/strafe) encoder inputs. In `PinpointSubsystem.java`, measure and set the pod offsets from the robot's tracking center, verify pod directions, and select the installed goBILDA pod type. The current zero offsets and forward directions are placeholders, so localization must be calibrated before running this autonomous. The robot must remain still during Pinpoint IMU calibration.

### One-motor shooter wiring and operation

The mecanum drivetrain already uses all four motor ports on a REV Control Hub. For a fifth motor, connect the shooter to a motor port on a REV Expansion Hub (suggested starting assignment: port 0, if free) and connect the Expansion Hub to the Control Hub using the REV-approved wiring. Do not try to connect a fifth motor to the Control Hub's four motor ports. In the Driver Station Robot Configuration, set that exact hub/port to a `DcMotor`/`DcMotorEx` named `shooter`. The code uses the configured name, not the numeric port, so update the DS config if the free port is different. The Control Hub's built-in motor encoder is used only if your motor has an encoder; this simple flywheel control does not require one.

In `TeleOpMain`, hold gamepad 1's **right bumper** to run the shooter at 85% power; releasing it stops the motor. The current autonomous remains leave-and-park and does not shoot. The shooter direction and power are safe starting values only: with the robot secured and the flywheel clear, verify its rotation at low power before testing game pieces. Change `SHOOT_POWER` and the motor direction in `ShooterSubsystem.java` after a controlled test. A single flywheel motor also needs the robot's actual feed mechanism to deliver game pieces; this code does not invent a feeder or guarantee scoring.

### Match setup

Place the robot touching its alliance wall inside that alliance's Loading Zone, facing straight into the field. The routine is alliance-independent because it drives forward and then retraces its path; it does not cross toward the opposite alliance. The webcam reports visible tag IDs for diagnosis, while this conservative routine does not use camera detections to steer.

---

## 📊 System Architecture & Departmental Matrix

To ensure development efficiency, rapid prototyping, and clean version control, our operations are managed via a rigid 4-department matrix. This structure isolates tasks to eliminate overlapping repository drops during remote sprints:

| CAD Design | Mechanical Build | Autonomous Software | Business & Operations |
| :--- | :--- | :--- | :--- |
| • Lead CAD Designer | • Build Track Lead | • Software Architecture Lead | • Team Captain / Exec Director |
| • Mechanism Modeling | • Chassis Fabricator | • Lead Systems Developer | • Director of Business & Media |
| • Systems Integration | • Assembly Technician | • Java Controls Engineer | |
| • Vision Layout Prep | • Hardware Prototypers | • Vision Optimization Track | |

---

## 🚦 DevOps: Git Branching Protocol

  [feature/intake-logic] ----●-----●-----\\
                                          \\  (Pull Request & Peer Review)
  [master] --------------------------------●----------------------------●--> (Stable/Flashed)
                                          /
  [hotfix/sensor-patch] -----------------●/ 

### 1. The Core Branch: `master`

* **Status:** Protected. Direct pushes are restricted via organization rules.
* **Purpose:** Contains the absolute production-ready, verified stable code. The code sitting on the physical robot in the lab must always match this branch perfectly.

### 2. Isolated Workspace Branches: `feature/*`

* **Naming Standard:** `feature/mechanism-name` (e.g., `feature/drive-mecanum`, `feature/lift-pid`)
* **Purpose:** Where all remote development happens. Programmers work completely independently within their designated sub-folders.

### 3. Emergency Maintenance Branches: `hotfix/*`

* **Naming Standard:** `hotfix/issue-description` (e.g., `hotfix/camera-disconnect`)
* **Purpose:** Reserved for competition match-days or critical lab blocks. Used to patch game-breaking errors instantly on-site.

### 📝 The Pull Request (PR) & Code Review Procedure

Before any code is merged from a developer's `feature/*` branch into the main `master` line, the developer must submit a formal Pull Request and pass this criteria:

1. **Local Pre-Compilation:** The code must compile with zero errors on the developer's machine.
2. **Component Modularity Check:** Ensure all custom code lives strictly inside `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/[subsystem_folder]`. Subsystems must not write direct motor values inside OpModes.
3. **Peer Review:** At least one other software track team member or the Software Lead must review the line-by-line differences ("diff") on GitHub and approve the architecture.
4. **Conflict Resolution:** If a merge conflict occurs due to simultaneous edits on global files (like `Constants.java`), developers must meet synchronously to resolve the lines before forcing the merge.

---

## 💻 Lab Deployment: Flashing the REV Control Hub

Once a Pull Request is approved and merged into `master`, the team in the lab pulls the changes and deploys them directly to the robot via Android Studio using one of two methods:

### Method A: USB-C Physical Deploy (Recommended)

1. Connect a USB-C data tether directly from the lab station laptop to the REV Control Hub.
2. Select **"REV Control Hub"** from the Android Studio active device dropdown menu.
3. Click **Run** (`Shift + F10`) to compile the APK and install it directly onto the hardware.

### Method B: ADB Wireless Deploy (Field Tracking)

1. Ensure the laptop is connected directly to the Control Hub's broadcast Wi-Fi network.
2. Execute the bridge link command in the Android Studio terminal:

```bash
adb connect 192.168.43.1:5555

```

3. Once connected, deploy wirelessly using the top menu toolbar.

