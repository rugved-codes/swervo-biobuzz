package org.firstinspires.ftc.teamcode.opmodes.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.subsystems.RobotHardware;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

/**
 * Conservative BIOBUZZ autonomous for the currently documented drivetrain-only robot.
 * Scores LEAVE, then returns to the alliance Loading Zone to score PARK.
 */
@Autonomous(name = "BIOBUZZ Leave + Park", group = "BIOBUZZ")
public class BioBuzzLeaveParkAuto extends LinearOpMode {
    // Relative Pinpoint targets, inches from the pose reset during INIT.
    private static final double CLEAR_WALL_IN = 20.0;
    private static final double PARK_OFFSET_IN = 8.0;
    private static final double POSITION_TOLERANCE_IN = 1.5;
    private static final double MAX_TRANSLATION_POWER = 0.35;
    private static final double MIN_TRANSLATION_POWER = 0.12;
    private static final double HEADING_KP = 0.8;

    private final RobotHardware robot = new RobotHardware();
    private final ElapsedTime matchTimer = new ElapsedTime();
    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;

    @Override
    public void runOpMode() {
        try {
            robot.init(hardwareMap);
            initializeVision();

            telemetry.addLine("BIOBUZZ: Leave + Park");
            telemetry.addLine("Start in your alliance Loading Zone, touching its wall,");
            telemetry.addLine("with the robot facing straight into the field.");
            telemetry.addLine("Pinpoint must be still while its IMU calibrates.");
            showPoseAndTags();
            telemetry.update();

            waitForStart();
            if (isStopRequested()) return;

            matchTimer.reset();
            boolean clearedWall = driveToX(CLEAR_WALL_IN, 5.0);
            boolean parked = false;
            if (clearedWall && opModeIsActive()) {
                // Return toward the start wall, stopping inside the 11-inch-deep Loading Zone.
                parked = driveToX(PARK_OFFSET_IN, 5.0);
            }

            robot.driveSubsystem.stop();
            telemetry.addData("AUTO time", "%.1f s", matchTimer.seconds());
            telemetry.addLine(!clearedWall
                    ? "Wall-clear move timed out; robot stopped for safety."
                    : parked ? "Route complete: clear wall and return to Loading Zone."
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
            if (visionPortal != null) visionPortal.close();
        }
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
            // Vision reports field-tag observations only; odometry safely runs this route alone.
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
        if (aprilTagProcessor == null) return;
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        StringBuilder ids = new StringBuilder();
        int count = Math.min(detections.size(), 6);
        for (int i = 0; i < count; i++) {
            if (i > 0) ids.append(", ");
            ids.append(detections.get(i).id);
        }
        telemetry.addData("AprilTags", detections.isEmpty() ? "none" : ids.toString());
    }
}
