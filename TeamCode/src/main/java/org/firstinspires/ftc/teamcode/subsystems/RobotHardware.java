package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

/** Hardware configuration names used by the drivetrain-only BIOBUZZ autonomous. */
public class RobotHardware {
    // These are Driver Station configuration names, not physical hub port numbers.
    public static final String FL_MOTOR = "frontLeft";
    public static final String FR_MOTOR = "frontRight";
    public static final String BL_MOTOR = "backLeft";
    public static final String BR_MOTOR = "backRight";
    public static final String INTAKE_NAME = "intake";
    public static final String SHOOTER_NAME = ShooterSubsystem.CONFIG_NAME;
    public static final String PINPOINT_NAME = "pinpoint";
    public static final String WEBCAM_NAME = "camera";

    public DcMotorEx frontLeft;
    public DcMotorEx frontRight;
    public DcMotorEx backLeft;
    public DcMotorEx backRight;
    public MecanumDrive driveSubsystem;
    public PinpointSubsystem pinpointSubsystem;

    public void init(HardwareMap hardwareMap) {
        frontLeft = hardwareMap.get(DcMotorEx.class, FL_MOTOR);
        frontRight = hardwareMap.get(DcMotorEx.class, FR_MOTOR);
        backLeft = hardwareMap.get(DcMotorEx.class, BL_MOTOR);
        backRight = hardwareMap.get(DcMotorEx.class, BR_MOTOR);

        driveSubsystem = new MecanumDrive();
        driveSubsystem.init(hardwareMap);
        pinpointSubsystem = new PinpointSubsystem(hardwareMap);
    }
}
