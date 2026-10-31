// ==========================================
// AUTONOMOUS ROUTINE: BIOBUZZ GAME
// ==========================================

// 1. Initialization Phase
// - Reset all encoders on drivetrain and subsystem motors.
// - Initialize VisionPortal and calibrate AprilTag detection for field localization.
// - Set initial servo positions (index gate CLOSED, hood angle to default starting position).

// 2. Start Signal Received (OpMode Active)
// - Read localized starting position from AprilTag / telemetry.
// - Spin up flywheel motor to pre-calculated scoring RPM.

// 3. Navigation & Scoring Sequence
// - Drive forward/strafe off the starting tile using PID-controlled Mecanum drive vectors.
// - Locate target HIVE structure using OpenCV vision pipeline / AprilTag pose estimation.
// - Adjust drivetrain alignment and hood angle dynamically based on target distance.

// 4. Element Delivery / Intake Interaction
// - Actuate index gate servo to feed stored POLLEN/NECTAR into the spinning flywheel.
// - Run intake rollers to sweep any supplementary field elements if applicable.
// - Confirm scoring completion via current spike or vision confirmation.

// 5. Park & Safe Shutdown
// - Navigate to designated parking zone.
// - Power down flywheel and intake motors to conserve battery and comply with safety rules.