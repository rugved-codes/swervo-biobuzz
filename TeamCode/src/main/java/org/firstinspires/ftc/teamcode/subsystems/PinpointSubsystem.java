package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

/** Configuration and access wrapper for a goBILDA Pinpoint with two dead-wheel pods. */
public class PinpointSubsystem {
    // Measure these from the robot's tracking center to each pod's wheel contact point.
    // X pod is forward-facing; Y pod is the lateral/strafe wheel.
    public static final double X_POD_OFFSET_MM = 0.0;
    public static final double Y_POD_OFFSET_MM = 0.0;

    private final GoBildaPinpointDriver pinpoint;

    public PinpointSubsystem(HardwareMap hardwareMap) {
        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, RobotHardware.PINPOINT_NAME);
        pinpoint.setOffsets(X_POD_OFFSET_MM, Y_POD_OFFSET_MM, DistanceUnit.MM);
        pinpoint.setEncoderResolution(
                GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        pinpoint.setEncoderDirections(
                GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.FORWARD);
        // The robot must remain still while the Pinpoint IMU recalibrates.
        pinpoint.resetPosAndIMU();
    }

    public void update() {
        pinpoint.update();
    }

    public Pose2D getPose() {
        return pinpoint.getPosition();
    }

    public double getXInches() {
        return getPose().getX(DistanceUnit.INCH);
    }

    public double getYInches() {
        return getPose().getY(DistanceUnit.INCH);
    }

    public double getHeadingRadians() {
        return getPose().getHeading(AngleUnit.RADIANS);
    }
}
