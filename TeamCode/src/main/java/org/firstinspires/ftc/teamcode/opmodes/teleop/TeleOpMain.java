package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.subsystems.RobotHardware;
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeModule;

@TeleOp(name = "TeleOpMain", group = "TeleOp")
public class TeleOpMain extends LinearOpMode {

    private final RobotHardware robot = new RobotHardware();
    private ShooterSubsystem shooter;
    private IntakeModule intake;

    @Override
    public void runOpMode() {
        robot.init(hardwareMap);
        try {
            shooter = new ShooterSubsystem(hardwareMap);
        } catch (RuntimeException shooterNotConfigured) {
            telemetry.addData("Shooter pair", "Not configured: %s", shooterNotConfigured.getMessage());
        }
        try {
            intake = new IntakeModule();
            intake.init(hardwareMap);
        } catch (RuntimeException intakeNotConfigured) {
            intake = null;
            telemetry.addData("Intake", "Not configured: %s", intakeNotConfigured.getMessage());
        }

        telemetry.addLine("Mecanum drivetrain ready");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            double axial = -gamepad1.left_stick_y;
            double lateral = gamepad1.left_stick_x;
            double yaw = gamepad1.right_stick_x;

            robot.driveSubsystem.drive(axial, lateral, yaw);

            // Operator gamepad 2: right bumper runs both shooter motors.
            if (shooter != null) {
                if (gamepad2.right_bumper) {
                    shooter.setShooting(true);
                } else {
                    shooter.setPowers(gamepad2.x ? ShooterSubsystem.SHOOT_POWER : 0.0,
                            gamepad2.b ? ShooterSubsystem.SHOOT_POWER : 0.0);
                }
            }

            // Left bumper intakes; D-pad down briefly reverses it to clear a jam.
            if (intake != null) {
                if (gamepad2.left_bumper) {
                    intake.intakeIn();
                } else if (gamepad2.dpad_down) {
                    intake.intakeOut();
                } else {
                    intake.stop();
                }
            }

            telemetry.addData("Axial", axial);
            telemetry.addData("Lateral", lateral);
            telemetry.addData("Yaw", yaw);
            telemetry.addData("Shooters", shooter == null ? "not configured"
                    : gamepad2.right_bumper ? "both running"
                    : String.format("left %s / right %s", gamepad2.x ? "running" : "stopped",
                    gamepad2.b ? "running" : "stopped"));
            telemetry.addData("Intake", intake == null ? "not configured"
                    : gamepad2.left_bumper ? "in" : gamepad2.dpad_down ? "reverse" : "stopped");
            telemetry.update();
        }
        if (shooter != null) shooter.stop();
        if (intake != null) intake.stop();
        robot.driveSubsystem.stop();
    }
}
