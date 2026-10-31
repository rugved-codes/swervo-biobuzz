package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Proper mecanum drive subsystem for a standard 4-wheel chassis.
 * Supports dynamic speed scaling for Precision and Turbo modes.
 */
public class MecanumDrive {

    private DcMotorEx frontLeft;
    private DcMotorEx frontRight;
    private DcMotorEx backLeft;
    private DcMotorEx backRight;

    public void init(HardwareMap hwMap) {
        frontLeft = hwMap.get(DcMotorEx.class, RobotHardware.FL_MOTOR);
        frontRight = hwMap.get(DcMotorEx.class, RobotHardware.FR_MOTOR);
        backLeft = hwMap.get(DcMotorEx.class, RobotHardware.BL_MOTOR);
        backRight = hwMap.get(DcMotorEx.class, RobotHardware.BR_MOTOR);

        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    /**
     * Robot-Centric Mecanum Drive with Speed Scaling.
     * @param axial Forward/Backward power (-1.0 to 1.0)
     * @param lateral Left/Right strafe power (-1.0 to 1.0)
     * @param yaw Rotation power (-1.0 to 1.0)
     * @param speedScale Multiplier (e.g., 0.5 for 50% precision, 1.0 for 100% turbo)
     */
    public void drive(double axial, double lateral, double yaw, double speedScale) {
        // Apply speed scale directly to joystick inputs
        axial *= speedScale;
        lateral *= speedScale;
        yaw *= speedScale;

        double fl = axial + lateral + yaw;
        double fr = axial - lateral - yaw;
        double bl = axial - lateral + yaw;
        double br = axial + lateral - yaw;

        double max = Math.max(Math.abs(fl), Math.abs(fr));
        max = Math.max(max, Math.abs(bl));
        max = Math.max(max, Math.abs(br));

        if (max > 1.0) {
            fl /= max;
            fr /= max;
            bl /= max;
            br /= max;
        }

        setPowers(fl, fr, bl, br);
    }

    /** Drive with full output scale. */
    public void drive(double axial, double lateral, double yaw) {
        drive(axial, lateral, yaw, 1.0);
    }

    public void stop() {
        setPowers(0.0, 0.0, 0.0, 0.0);
    }

    public void setPowers(double frontLeftPower, double frontRightPower,
                          double backLeftPower, double backRightPower) {
        frontLeft.setPower(frontLeftPower);
        frontRight.setPower(frontRightPower);
        backLeft.setPower(backLeftPower);
        backRight.setPower(backRightPower);
    }

    public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
        frontLeft.setZeroPowerBehavior(behavior);
        frontRight.setZeroPowerBehavior(behavior);
        backLeft.setZeroPowerBehavior(behavior);
        backRight.setZeroPowerBehavior(behavior);
    }

    public void setMode(DcMotor.RunMode mode) {
        frontLeft.setMode(mode);
        frontRight.setMode(mode);
        backLeft.setMode(mode);
        backRight.setMode(mode);
    }
}
