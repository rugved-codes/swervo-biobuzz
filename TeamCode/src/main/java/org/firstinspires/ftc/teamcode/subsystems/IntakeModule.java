package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * One-motor rubber-band intake.
 */
public class IntakeModule {

    private DcMotorEx intakeMotor;

    // Power constants for intake states
    private static final double INTAKE_IN_POWER = 0.75;
    private static final double INTAKE_OUT_POWER = -0.50;
    private static final double INTAKE_STOP_POWER = 0.0;

    public void init(HardwareMap hwMap) {
        intakeMotor = hwMap.get(DcMotorEx.class, RobotHardware.INTAKE_NAME);
        intakeMotor.setDirection(DcMotor.Direction.FORWARD);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intakeMotor.setPower(0.0);
    }

    /**
     * Spin the intake inward to grab elements.
     */
    public void intakeIn() {
        intakeMotor.setPower(INTAKE_IN_POWER);
    }

    /**
     * Spit elements outward.
     */
    public void intakeOut() {
        intakeMotor.setPower(INTAKE_OUT_POWER);
    }

    /**
     * Stop the intake.
     */
    public void stop() {
        intakeMotor.setPower(INTAKE_STOP_POWER);
    }
}
