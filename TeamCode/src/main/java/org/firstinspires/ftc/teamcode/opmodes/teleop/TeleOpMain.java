package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.subsystems.RobotHardware;
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;

@TeleOp(name = "TeleOpMain", group = "TeleOp")
public class TeleOpMain extends LinearOpMode {

    private final RobotHardware robot = new RobotHardware();
    private ShooterSubsystem shooter;

    @Override
    public void runOpMode() {
        robot.init(hardwareMap);
        try {
            shooter = new ShooterSubsystem(hardwareMap);
        } catch (RuntimeException shooterNotConfigured) {
            telemetry.addData("Shooter", "Not configured: %s", shooterNotConfigured.getMessage());
        }

        telemetry.addLine("Mecanum drivetrain ready");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            double axial = -gamepad1.left_stick_y;
            double lateral = gamepad1.left_stick_x;
            double yaw = gamepad1.right_stick_x;

            robot.driveSubsystem.drive(axial, lateral, yaw);

            // Hold right bumper to run the flywheel; releasing it immediately stops the motor.
            if (shooter != null) {
                shooter.setShooting(gamepad1.right_bumper);
            }

            telemetry.addData("Axial", axial);
            telemetry.addData("Lateral", lateral);
            telemetry.addData("Yaw", yaw);
            telemetry.addData("Shooter", shooter == null ? "not configured"
                    : gamepad1.right_bumper ? "running" : "stopped (hold right bumper)");
            telemetry.update();
        }
        if (shooter != null) shooter.stop();
        robot.driveSubsystem.stop();
    }
}
