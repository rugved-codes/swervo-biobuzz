package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.subsystems.RobotHardware;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

/**
 * SWERVO 26256 | AprilTag Camera Diagnostic (FTC SDK 11.1.0)
 *
 * Purpose: Verify the webcam, detect individual AprilTags, and report useful
 *          diagnostic information to the Driver Station.
 *
 * WARNING: This test does not control motors, servos, or the launcher.
 * WARNING: SDK 11.1 cannot detect BIOBUZZ four-tag clusters as one target.
 *          Upgrade to SDK 12.0+ for reliable CELL-opening pose estimation.
 * WARNING: Camera-relative measurements do not represent launcher calibration.
 */
@TeleOp(name = "SWERVO AprilTag Vision Test", group = "Vision")
public class AprilTagVisionTest extends LinearOpMode {

    // Diagnostic settings. The webcam name is defined in RobotHardware.java.
    private static final double CENTER_TOLERANCE_DEG = 2.0;
    private static final int MAX_TAGS_TO_DISPLAY = 4;

    @Override
    public void runOpMode() {
        VisionPortal visionPortal = null;

        try {
            // 1. Find the configured webcam and initialize the AprilTag processor.
            //    The name must match the active Robot Controller configuration exactly.
            WebcamName webcam = hardwareMap.get(
                    WebcamName.class, RobotHardware.WEBCAM_NAME);
            AprilTagProcessor aprilTag = AprilTagProcessor.easyCreateWithDefaults();
            visionPortal = new VisionPortal.Builder()
                    .setCamera(webcam)
                    .addProcessor(aprilTag)
                    .build();

            // 2. Show the camera state during INIT, before the driver presses START.
            while (!isStarted() && !isStopRequested()) {
                telemetry.addLine("SWERVO APRILTAG | INITIALIZING");
                telemetry.addData("Webcam", RobotHardware.WEBCAM_NAME);
                telemetry.addData("Camera state", visionPortal.getCameraState());
                if (visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
                    telemetry.addLine("WAIT: Camera is not streaming yet.");
                }
                telemetry.addLine("VISION ONLY - Robot motors are not controlled.");
                telemetry.update();
                sleep(150);
            }

            if (isStopRequested()) {
                return;
            }

            // 3. Read AprilTag results and send diagnostics to the Driver Station.
            while (opModeIsActive()) {
                boolean cameraReady =
                        visionPortal.getCameraState() == VisionPortal.CameraState.STREAMING;

                telemetry.addLine("SWERVO APRILTAG | RUNNING");
                telemetry.addData("Camera state", visionPortal.getCameraState());
                telemetry.addData("Webcam", RobotHardware.WEBCAM_NAME);

                if (!cameraReady) {
                    telemetry.addLine("WARNING: CAMERA NOT STREAMING.");
                    telemetry.addLine("Check USB cable and active robot configuration.");
                } else {
                    List<AprilTagDetection> detections = aprilTag.getDetections();
                    telemetry.addData("Tags detected", detections.size());

                    if (detections.isEmpty()) {
                        telemetry.addLine("WARNING: NO APRILTAGS DETECTED.");
                        telemetry.addLine("Check tag visibility, lighting and distance.");
                    }

                    int displayed = 0;
                    for (AprilTagDetection detection : detections) {
                        if (displayed >= MAX_TAGS_TO_DISPLAY) {
                            telemetry.addLine("WARNING: More tags found than displayed.");
                            break;
                        }
                        displayed++;

                        // 4. Every detection has an ID. Metadata and pose are
                        //    available only when the current tag library knows that ID.
                        telemetry.addLine("------------------------------");
                        telemetry.addData("Tag ID", detection.id);
                        String bioBuzzHint = bioBuzzIdHint(detection.id);
                        if (bioBuzzHint != null) {
                            telemetry.addData("BIOBUZZ ID hint", bioBuzzHint);
                        }

                        if (detection.metadata == null || detection.ftcPose == null) {
                            telemetry.addLine("WARNING: Tag has no known pose metadata.");
                            telemetry.addLine("SDK 11.1 may recognize its ID only.");
                            if (detection.center != null) {
                                telemetry.addData("Image center (px)", "%.0f, %.0f",
                                        detection.center.x, detection.center.y);
                            }
                            continue;
                        }

                        // 5. Report camera-relative position for recognized tags.
                        telemetry.addData("Tag name", detection.metadata.name);
                        telemetry.addData("Range", "%.1f in", detection.ftcPose.range);
                        telemetry.addData("Bearing", "%.1f deg", detection.ftcPose.bearing);
                        telemetry.addData("Sideways X", "%.1f in", detection.ftcPose.x);
                        telemetry.addData("Elevation", "%.1f deg", detection.ftcPose.elevation);
                        telemetry.addData("Alignment", alignmentLabel(detection.ftcPose.bearing));
                    }
                }

                // 6. Keep the limitations visible so measurements are not mistaken
                //    for 2026-27 HIVE CELL aiming data.
                telemetry.addLine("SDK 11.1: INDIVIDUAL TAGS ONLY.");
                telemetry.addLine("CELL cluster pose requires SDK 12.0+.");
                telemetry.update();
                sleep(100);
            }
        } catch (RuntimeException error) {
            // 7. Keep the error visible on the Driver Station until STOP.
            //    Runtime errors are different from Gradle compilation errors.
            while (!isStopRequested()) {
                telemetry.addLine("ERROR: APRILTAG VISION FAILURE.");
                telemetry.addData("Expected webcam", RobotHardware.WEBCAM_NAME);
                telemetry.addData("Reason", error.getMessage() == null
                        ? error.getClass().getSimpleName() : error.getMessage());
                telemetry.addLine("Check webcam spelling, USB and configuration.");
                telemetry.update();
                sleep(250);
            }
        } finally {
            // 8. Release the webcam for other OpModes when this test ends.
            if (visionPortal != null) {
                visionPortal.close();
            }
        }
    }

    /** Human-readable location of an INDIVIDUAL tag in the camera image. */
    private static String alignmentLabel(double bearingDegrees) {
        if (Math.abs(bearingDegrees) <= CENTER_TOLERANCE_DEG) {
            return "TAG CENTERED (not launcher-calibrated)";
        }
        return bearingDegrees > 0.0 ? "TAG LEFT" : "TAG RIGHT";
    }

    /** ID-range hint only; SDK 11.1 cannot calculate a BIOBUZZ cluster pose. */
    private static String bioBuzzIdHint(int id) {
        if (id >= 30 && id <= 33) return "RED far-side CELL (ID only)";
        if (id >= 34 && id <= 37) return "RED audience CELL (ID only)";
        if (id >= 38 && id <= 41) return "BLUE audience CELL (ID only)";
        if (id >= 42 && id <= 45) return "BLUE far-side CELL (ID only)";
        return null;
    }
}
