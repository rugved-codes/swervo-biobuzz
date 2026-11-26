package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

/** Two independently controlled rubber-band shooter motors. */
public final class ShooterSubsystem {
    public static final String LEFT_CONFIG_NAME = "shooterLeft";
    public static final String RIGHT_CONFIG_NAME = "shooterRight";
    public static final double SHOOT_POWER = 0.70;

    private final DcMotorEx leftMotor;
    private final DcMotorEx rightMotor;

    public ShooterSubsystem(HardwareMap hardwareMap) {
        leftMotor = hardwareMap.get(DcMotorEx.class, LEFT_CONFIG_NAME);
        rightMotor = hardwareMap.get(DcMotorEx.class, RIGHT_CONFIG_NAME);
        configure(leftMotor, DcMotor.Direction.FORWARD);
        // Mirrored rollers usually need opposite motor directions; verify on the built robot.
        configure(rightMotor, DcMotor.Direction.REVERSE);
    }

    private static void configure(DcMotorEx motor, DcMotor.Direction direction) {
        motor.setDirection(direction);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setPower(0.0);
    }

    public void setShooting(boolean enabled) {
        setPowers(enabled ? SHOOT_POWER : 0.0, enabled ? SHOOT_POWER : 0.0);
    }

    /** Allows a driver to test either motor individually. */
    public void setPowers(double leftPower, double rightPower) {
        leftMotor.setPower(leftPower);
        rightMotor.setPower(rightPower);
    }

    public void stop() {
        setPowers(0.0, 0.0);
    }
}
