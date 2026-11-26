package org.firstinspires.ftc.teamcode.opmodes.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.subsystems.IntakeModule;
import org.firstinspires.ftc.teamcode.subsystems.RobotHardware;
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

/**
 * Fires the four preloaded POLLEN, clears the wall, then drives to a tunable return point.
 */
@Autonomous(name = "BIOBUZZ Preload Shot + Leave", group = "BIOBUZZ")
public class BioBuzzLeaveParkAuto extends LinearOpMode {
    // Relative Pinpoint targets, inches from the pose reset during INIT.
    private static final double CLEAR_WALL_IN = 20.0;
    private static final double PARK_OFFSET_IN = 8.0;
    private static final double POSITION_TOLERANCE_IN = 1.5;
    private static final double MAX_TRANSLATION_POWER = 0.35;
    private static final double MIN_TRANSLATION_POWER = 0.12;
    private static final double HEADING_KP = 0.8;
    // Starting values for the preload burst; tune on the real field.
    private static final double SHOOTER_SPINUP_SECONDS = 0.65;
    private static final double SHOOTER_FEED_END_SECONDS = 2.0;

    private final RobotHardware robot = new RobotHardware();
    private final ElapsedTime matchTimer = new ElapsedTime();
    private ShooterSubsystem shooter;
    private IntakeModule intake;
    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;

    @Override
    public void runOpMode() {
        try {
            robot.init(hardwareMap);
            shooter = new ShooterSubsystem(hardwareMap);
            intake = new IntakeModule();
            intake.init(hardwareMap);
            initializeVision();

            telemetry.addLine("BIOBUZZ: Preload Shot + Leave");
            telemetry.addLine("Start on your alliance side, touching the wall, outside the Loading Zone.");
            telemetry.addLine("Aim the robot at the upward-facing CELL.");
            telemetry.addLine("Load the 4 preloaded POLLEN into the shooter feed path.");
            telemetry.addLine("Auto fires a short burst, then clears the wall and returns to X=8 in.");
            telemetry.addLine("Pinpoint must be still while its IMU calibrates.");
            showPoseAndTags();
            telemetry.update();

            waitForStart();
            if (isStopRequested()) return;

            matchTimer.reset();
            firePreloadBurst();
            if (!opModeIsActive()) return;

            boolean clearedWall = driveToX(CLEAR_WALL_IN, 5.0);
            boolean parked = false;
            if (clearedWall && opModeIsActive()) {
                // Return near the start wall. Calibrate this target for the actual Loading Zone.
                parked = driveToX(PARK_OFFSET_IN, 5.0);
            }

            robot.driveSubsystem.stop();
            telemetry.addData("AUTO time", "%.1f s", matchTimer.seconds());
            telemetry.addLine(!clearedWall
                    ? "Wall-clear move timed out; robot stopped for safety."
                    : parked ? "Shot and drive complete: returned to configured X target."
                    : "Wall cleared; return move timed out. Check Pinpoint calibration.");
            showPoseAndTags();
            telemetry.update();

            // Keep vision diagnostics visible until the end of the autonomous period.
            while (opModeIsActive() && matchTimer.seconds() < 29.5) {
                robot.pinpointSubsystem.update();
                showPoseAndTags();
                telemetry.addData("AUTO time", "%.1f s", matchTimer.seconds());
                telemetry.update();
                sleep(50);
            }
        } finally {
            if (robot.driveSubsystem != null) robot.driveSubsystem.stop();
            if (intake != null) intake.stop();
            if (shooter != null) shooter.stop();
            if (visionPortal != null) visionPortal.close();
        }
    }

    /** Briefly spins both shooter motors, then feeds the preloaded scoring elements. */
    private void firePreloadBurst() {
        ElapsedTime burstTimer = new ElapsedTime();
        shooter.setShooting(true);
        while (opModeIsActive() && burstTimer.seconds() < SHOOTER_SPINUP_SECONDS) {
            telemetry.addLine("AUTO: spinning both shooters");
            showPoseAndTags();
            telemetry.update();
            sleep(20);
        }

        if (opModeIsActive()) {
            intake.intakeIn();
            while (opModeIsActive() && burstTimer.seconds() < SHOOTER_FEED_END_SECONDS) {
                telemetry.addLine("AUTO: feeding preload into upward-facing CELL");
                showPoseAndTags();
                telemetry.update();
                sleep(20);
            }
        }

        intake.stop();
        shooter.stop();
    }

    private void initializeVision() {
        try {
            WebcamName webcam = hardwareMap.get(WebcamName.class, RobotHardware.WEBCAM_NAME);
            aprilTagProcessor = AprilTagProcessor.easyCreateWithDefaults();
            visionPortal = new VisionPortal.Builder()
                    .setCamera(webcam)
                    .addProcessor(aprilTagProcessor)
                    .build();
        } catch (RuntimeException cameraError) {
            // Vision reports field-tag observations; Pinpoint runs the route independently.
            telemetry.addData("Webcam", "Unavailable: %s", cameraError.getMessage());
        }
    }

    /** Closed-loop translation with Pinpoint position and heading feedback. */
    private boolean driveToX(double targetXInches, double timeoutSeconds) {
        ElapsedTime stepTimer = new ElapsedTime();
        while (opModeIsActive() && stepTimer.seconds() < timeoutSeconds) {
            robot.pinpointSubsystem.update();
            Pose2D pose = robot.pinpointSubsystem.getPose();
            double x = pose.getX(DistanceUnit.INCH);
            double y = pose.getY(DistanceUnit.INCH);
            double heading = pose.getHeading(org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.RADIANS);
            double error = targetXInches - x;

            if (Math.abs(error) <= POSITION_TOLERANCE_IN) {
                robot.driveSubsystem.stop();
                return true;
            }

            // Pinpoint X is forward and Y is left when heading is zero.
            double fieldForward = Range.clip(error * 0.06,
                    -MAX_TRANSLATION_POWER, MAX_TRANSLATION_POWER);
            if (Math.abs(fieldForward) < MIN_TRANSLATION_POWER) {
                fieldForward = Math.copySign(MIN_TRANSLATION_POWER, error);
            }
            double fieldLeft = Range.clip(-y * 0.04,
                    -MAX_TRANSLATION_POWER, MAX_TRANSLATION_POWER);

            // Convert field-axis error into robot-centric mecanum commands.
            double axial = fieldForward * Math.cos(heading) + fieldLeft * Math.sin(heading);
            double lateral = -fieldForward * Math.sin(heading) + fieldLeft * Math.cos(heading);
            double yaw = Range.clip(-heading * HEADING_KP, -0.25, 0.25);
            robot.driveSubsystem.drive(axial, lateral, yaw);

            telemetry.addData("Target X", "%.1f in", targetXInches);
            telemetry.addData("Pose", "x %.1f, y %.1f in, heading %.1f deg",
                    x, y, Math.toDegrees(heading));
            telemetry.addData("Move time", "%.1f / %.1f s", stepTimer.seconds(), timeoutSeconds);
            showTags();
            telemetry.update();
            idle();
        }
        robot.driveSubsystem.stop();
        return false;
    }

    private void showPoseAndTags() {
        robot.pinpointSubsystem.update();
        Pose2D pose = robot.pinpointSubsystem.getPose();
        telemetry.addData("Pinpoint", "x %.1f, y %.1f in, heading %.1f deg",
                pose.getX(DistanceUnit.INCH), pose.getY(DistanceUnit.INCH),
                pose.getHeading(org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES));
        telemetry.addData("Camera", visionPortal == null
                ? "not streaming" : visionPortal.getCameraState());
        showTags();
    }

    private void showTags() {
        if (aprilTagProcessor == null) {
            telemetry.addData("AprilTags", "camera unavailable");
            return;
        }
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        if (detections.isEmpty()) {
            telemetry.addData("AprilTags", "none visible (camera should look toward floor tags)");
            return;
        }
        int count = Math.min(detections.size(), 6);
        for (int i = 0; i < count; i++) {
            AprilTagDetection detection = detections.get(i);
            String label = bioBuzzTagLocation(detection.id);
            if (detection.ftcPose != null) {
                telemetry.addData("AprilTag " + detection.id,
                        "%s | range %.1f in, bearing %.1f deg",
                        label, detection.ftcPose.range, detection.ftcPose.bearing);
            } else {
                telemetry.addData("AprilTag " + detection.id, "%s | ID only", label);
            }
        }
        if (detections.size() > count) {
            telemetry.addData("AprilTags", "%d more visible", detections.size() - count);
        }
    }

    /** BIOBUZZ field tag ranges from the current FIRST competition manual. */
    private static String bioBuzzTagLocation(int id) {
        if (id >= 30 && id <= 33) return "red far-side CELL";
        if (id >= 34 && id <= 37) return "red audience-side CELL";
        if (id >= 38 && id <= 41) return "blue audience-side CELL";
        if (id >= 42 && id <= 45) return "blue far-side CELL";
        return "tag outside BIOBUZZ field IDs 30-45";
    }
}
