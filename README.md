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

`BIOBUZZ Leave + Park` is a conservative 30-second autonomous for the mecanum drivetrain. It uses the goBILDA Pinpoint's two dead-wheel odometry to drive 20 inches forward to clear the perimeter wall, then returns to an 8-inch relative offset. The Logitech webcam detects BIOBUZZ field AprilTags during INIT and AUTO and reports the tag IDs, field-cell group, range, and bearing. Tags are diagnostic only; the route does not steer from tags.

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
| Rubber-band intake motor | DcMotorEx | `intake` |
| Left shooter motor | DcMotorEx | `shooterLeft` |
| Right shooter motor | DcMotorEx | `shooterRight` |

Configure the four motor names against the ports actually used by the robot. Connect the two odometry pods to the Pinpoint's X (forward) and Y (left/strafe) encoder inputs. In `PinpointSubsystem.java`, measure and set the pod offsets from the robot's tracking center, verify pod directions, and select the installed goBILDA pod type. The current zero offsets and forward directions are placeholders, so localization must be calibrated before running this autonomous. The robot must remain still during Pinpoint IMU calibration.

### Intake and two-motor shooter wiring and operation

The four drivetrain motors use all four motor ports on the REV Control Hub. The intake and two shooter motors need three more motor ports, so connect a REV Expansion Hub to the Control Hub. If its ports are free, use Expansion Hub port 0 for the intake, port 1 for the left shooter, and port 2 for the right shooter. In Driver Station Robot Configuration, set those motor ports to the exact names in the table. These names are software labels; the port assignment must match the physical wiring. The robot has no sorter configured or controlled by this code.

In `TeleOpMain`, gamepad 2's **right bumper** runs both shooter motors. **X** runs only the left motor and **B** runs only the right motor for separate checks. **Left bumper** runs the intake inward; **D-pad down** reverses it. Releasing the controls stops the motors. The motor power and direction values are starting points: keep the robot secured and hands clear while checking roller direction at low power, then tune `SHOOT_POWER` and the directions in the subsystem code. The autonomous currently does not run the intake or shooters.

BIOBUZZ AprilTags are IDs 30–45 from the 36h11 family and are mounted under the CELLS facing down toward the floor. The camera needs a clear, downward view of the tags for detection. The autonomous reports the official tag group (red/blue and audience/far side), but does not use the detections to aim or change its path. Verify detection with the `SWERVO AprilTag Vision Test` OpMode before a match. See the [current FIRST BIOBUZZ manual](https://ftc-resources.firstinspires.org/ftc/game/manual-09) and [field resources](https://ftc-resources.firstinspires.org/ftc/field).

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
