package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

/** Single-motor shooter/flywheel. Hold the driver control while shooting. */
public final class ShooterSubsystem {
    public static final String CONFIG_NAME = "shooter";
    public static final double SHOOT_POWER = 0.85;

    private final DcMotorEx motor;

    public ShooterSubsystem(HardwareMap hardwareMap) {
        motor = hardwareMap.get(DcMotorEx.class, CONFIG_NAME);
        // If the flywheel spins the wrong way, change this to REVERSE after a safe test.
        motor.setDirection(DcMotor.Direction.FORWARD);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setPower(0.0);
    }

    public void setShooting(boolean enabled) {
        motor.setPower(enabled ? SHOOT_POWER : 0.0);
    }

    public void stop() {
        motor.setPower(0.0);
    }
}
