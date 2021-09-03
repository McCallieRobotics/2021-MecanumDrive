// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
 
package frc.robot;
 
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
 
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
 
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
 
/**
 * This is a demo program showing how to use Mecanum control with the RobotDrive
 * class.
 */
public class Robot extends TimedRobot {
    private static final int frontLeftDeviceID = 4;
    private static final int rearLeftDeviceID = 3;
    private static final int frontRightDeviceID = 1;
    private static final int rearRightDeviceID = 2;
 
    private static final int kJoystickChannel = 0;
 
    private MecanumDrive m_robotDrive;
    private XboxController m_controller;
 
    @Override
    public void robotInit() {
        NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");

        CANSparkMax frontLeft = new CANSparkMax(frontLeftDeviceID, MotorType.kBrushless);
        CANSparkMax rearLeft = new CANSparkMax(rearLeftDeviceID, MotorType.kBrushless);
        CANSparkMax frontRight = new CANSparkMax(frontRightDeviceID, MotorType.kBrushless);
        CANSparkMax rearRight = new CANSparkMax(rearRightDeviceID, MotorType.kBrushless);     
 
        m_robotDrive = new MecanumDrive(frontLeft, rearLeft, frontRight, rearRight);
 
        m_controller = new XboxController(kJoystickChannel);
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
 
        if ((l_stick_x < .1 && l_stick_x > 0) || (l_stick_x > -.1 && l_stick_x < 0)) {
            l_x_value = 0;
        } else {
            l_x_value = l_stick_x * .25;
        }
 
        if ((l_stick_y < .1 && l_stick_y > 0) || (l_stick_y > -.1 && l_stick_y < 0)) {
            l_y_value = 0;
        } else {
            l_y_value = l_stick_y * .25;
        }
 
        if ((r_stick_x < .1 && r_stick_x > 0) || (r_stick_x > -.1 && r_stick_x < 0)) {
            r_x_value = 0;
        } else {
            r_x_value = r_stick_x * .25;
        }
 
        m_robotDrive.driveCartesian(l_x_value, l_y_value, r_x_value, 0.0);

    }
   
  /**
   * This function is called periodically during autonomous
   */
  @Override
  public void autonomousPeriodic() {

    double auto_y_value;
 
    double tv = table.getEntry("tv").getDouble(0.0);

    double tx = table.getEntry("tx").getDouble(0.0);

    double ty = table.getEntry("ty").getDouble(0.0);

    double ta = table.getEntry("ta").getDouble(0.0);

    System.out.println("tv: " + tv);
    System.out.println("tx: " + tx);
    System.out.println("ty: " + ty);
    System.out.println("ta: " + ta);

    if (ta > .5) {
      auto_y_value = -.1;
    } else {
      auto_y_value = .1;
    }

    m_robotDrive.driveCartesian((tx * .01), teauto_y_valuemp, 0, 0.0);
  }
}