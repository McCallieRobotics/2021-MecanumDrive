// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.CAN;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;

import java.util.Map;

import com.kauailabs.navx.frc.AHRS;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import edu.wpi.first.wpilibj.SPI;

/**
 * This is a demo program showing how to use Mecanum control with the RobotDrive
 * class.
 */
public class Robot extends TimedRobot {
    private static final int frontLeftDeviceID = 4;
    private static final int rearLeftDeviceID = 3;
    private static final int frontRightDeviceID = 1;
    private static final int rearRightDeviceID = 2;

    private static final int leftLauncherDeviceID = 5;
    private static final int rightLauncherDeviceID = 6;

    private static final int kJoystickChannel = 0;

    private MecanumDrive m_robotDrive;
    private XboxController m_controller;

    private NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");

    private NetworkTableEntry speedSlider;

    private double lXTimerMs = 0;
    private double lYTimerMs = 0;
    private double rXTimerMs = 0;

    CANSparkMax frontLeft;
    CANSparkMax rearLeft;
    CANSparkMax frontRight;
    CANSparkMax rearRight;

    CANSparkMax leftLauncher;
    CANSparkMax rightLauncher;

    Compressor compressor;

    DoubleSolenoid solenoid;

    private int rampUpMs = 3000;

    @Override
    public void robotInit() {
        table = NetworkTableInstance.getDefault().getTable("limelight");

        compressor = new Compressor(0);

        // boolean enabled = compressor.enabled();

        solenoid = new DoubleSolenoid(0, 1);

        frontLeft = new CANSparkMax(frontLeftDeviceID, MotorType.kBrushless);
        rearLeft = new CANSparkMax(rearLeftDeviceID, MotorType.kBrushless);
        frontRight = new CANSparkMax(frontRightDeviceID, MotorType.kBrushless);
        rearRight = new CANSparkMax(rearRightDeviceID, MotorType.kBrushless);

        leftLauncher = new CANSparkMax(leftLauncherDeviceID, MotorType.kBrushless);
        rightLauncher = new CANSparkMax(rightLauncherDeviceID, MotorType.kBrushless);

        m_robotDrive = new MecanumDrive(frontLeft, rearLeft, frontRight, rearRight);

        m_controller = new XboxController(kJoystickChannel);

        speedSlider = Shuffleboard.getTab("Drive").add("Max Speed", 1).withWidget(BuiltInWidgets.kNumberSlider)
                .withProperties(Map.of("min", 0, "max", 1)) // specify widget properties here
                .getEntry();

        Shuffleboard.getTab("Drive").add("Drivebase", m_robotDrive).withWidget(BuiltInWidgets.kMecanumDrive);

    }

    /**
     * This function is called periodically during teleoperated
     */
    @Override
    public void teleopPeriodic() {

        double l_x_value = 0;
        double l_y_value = 0;
        double r_x_value = 0;

        double l_stick_x = m_controller.getX(Hand.kLeft);
        double l_stick_y = m_controller.getY(Hand.kLeft);
        double r_stick_x = m_controller.getX(Hand.kRight);

        double speedMultiplier = speedSlider.getDouble(0);

        if ((l_stick_x < .1 && l_stick_x > 0) || (l_stick_x > -.1 && l_stick_x < 0)) {
            l_x_value = 0;
            lXTimerMs = 0;
        } else {
            if (lXTimerMs < rampUpMs) {
                lXTimerMs += 20;
            }

            l_x_value = l_stick_x * speedMultiplier * (lXTimerMs / rampUpMs);
        }

        if ((l_stick_y < .1 && l_stick_y > 0) || (l_stick_y > -.1 && l_stick_y < 0)) {
            l_y_value = 0;
            lYTimerMs = 0;
        } else {
            if (lYTimerMs < rampUpMs) {
                lYTimerMs += 20;
            }
            l_y_value = l_stick_y * speedMultiplier * (lYTimerMs / rampUpMs);
        }

        if ((r_stick_x < .1 && r_stick_x > 0) || (r_stick_x > -.1 && r_stick_x < 0)) {
            r_x_value = 0;
            rXTimerMs = 0;
        } else {
            if (rXTimerMs < rampUpMs) {
                rXTimerMs += 20;
            }
            r_x_value = r_stick_x * speedMultiplier * (rXTimerMs / rampUpMs);
        }

        // frontLeft.set(l_x_value * -1);
        // rearLeft.set(l_x_value);

        m_robotDrive.driveCartesian(l_x_value, (-1 * l_y_value), r_x_value, 0.0);

        boolean pressureSwitch = compressor.getPressureSwitchValue();
        double current = compressor.getCompressorCurrent();

        System.out.println("pressureSwitch: " + pressureSwitch);
        System.out.println("current: " + current);

        boolean a_pressed = m_controller.getAButtonPressed();
        boolean b_pressed = m_controller.getBButtonPressed();
        boolean x_pressed = m_controller.getXButtonPressed();

        if (a_pressed) {
            solenoid.set(Value.kOff);
        }
        /*
         * if (b_pressed) { solenoid.set(Value.kForward); }
         */
        if (x_pressed) {
            solenoid.set(Value.kReverse);
        }

        double r_trigger = m_controller.getTriggerAxis(Hand.kRight);
        double l_trigger = m_controller.getTriggerAxis(Hand.kLeft);

        double launcherValue = 0;

        if (r_trigger > 0 && l_trigger == 0) {
            launcherValue = r_trigger;
        }

        if (l_trigger > 0 && r_trigger == 0) {
            launcherValue = l_trigger * -1 * .25;
        }

        leftLauncher.set(launcherValue * -1);
        rightLauncher.set(launcherValue);

        double tv = table.getEntry("tv").getDouble(0.0);

        double tx = table.getEntry("tx").getDouble(0.0);

        double ty = table.getEntry("ty").getDouble(0.0);

        double ta = table.getEntry("ta").getDouble(0.0);

        double tl = table.getEntry("tl").getDouble(0.0);

        if ((tx > -1 && tx < 1 && launcherValue == 1 && tv == 1) || (b_pressed && launcherValue == 1)) {
            solenoid.set(Value.kForward);
        }
    }

    /**
     * This function is called periodically during autonomous
     */
    @Override
    public void autonomousPeriodic() {
        double auto_y_value = 0;
        double auto_x_value = 0;

        double tv = table.getEntry("tv").getDouble(0.0);

        double tx = table.getEntry("tx").getDouble(0.0);

        double ty = table.getEntry("ty").getDouble(0.0);

        double ta = table.getEntry("ta").getDouble(0.0);

        // System.out.println("tv: " + tv);
        // System.out.println("tx: " + tx);
        // System.out.println("ty: " + ty);
        // System.out.println("ta: " + ta);

        if (tv == 0) {
            m_robotDrive.driveCartesian(0, 0, .05, 0.0);
        } else {
            if (ta > .4) {
                auto_y_value = -.1;
            } else if (ta < .2) {
                auto_y_value = .1;
            }

            /*
             * if (tx > .6) { auto_y_value = -.1; } else if (tx < .4) { auto_y_value = .1; }
             */

            m_robotDrive.driveCartesian(0, auto_y_value, (tx * .01), 0.0);
        }

        // System.out.println("angle: " + angle);

        // Drives forward continuously at half speed, using the gyro to stabilize the
        // heading
        // m_robotDrive.driveCartesian(0, 0, 0);
    }
}