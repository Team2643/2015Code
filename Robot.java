
package org.usfirst.frc.team2643.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;

import org.usfirst.frc.team2643.robot.commands.ExampleCommand;
import org.usfirst.frc.team2643.robot.subsystems.ExampleSubsystem;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	
	Talon  frontRightMotor = new Talon (7);
	Talon frontLeftMotor = new Talon (5);
	Talon backLeftMotor = new Talon (6);
	Talon backRightMotor = new Talon (9);
	Talon strafe1 = new Talon( 1 );
	Talon strafe2 = new Talon( 2 );
	Talon lslideMotor1 = new Talon (3);
	Talon lslideMotor2 = new Talon(4);
	DigitalInput topSwitch = new DigitalInput( 6 );
	DigitalInput bottomSwitch = new DigitalInput( 7 );
	Joystick driveStickLeft = new Joystick( 0 );
	Joystick driveStickRight = new Joystick( 1 );
	Joystick operatorStick = new Joystick( 2 );
	
	Encoder leftEncoder = new Encoder( 5 , 4 );
	Encoder rightEncoder = new Encoder( 3 , 2 );
	Encoder lsEncoder = new Encoder( 1 , 0 ); //linear slide encoder
	
	boolean arcade = true;
	
	LEDController led = new LEDController( 48 );
	
	RobotDrive drive = new RobotDrive( frontLeftMotor , backLeftMotor , frontRightMotor , backRightMotor );
	
	Timer autoTimer = new Timer();
	
	double circumference = Math.PI * 6;
	
	boolean goDown = false;

	public static final ExampleSubsystem exampleSubsystem = new ExampleSubsystem();
	public static OI oi;

    Command autonomousCommand;

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
    	LedStrip allLEDs = new LedStrip( 64 , 1.0f );
    	allLEDs.allOff();
    	allLEDs.update();
    	led.initialize();
    	led.reset();
    	
    	drive.setInvertedMotor( RobotDrive.MotorType.kFrontLeft , true );
    	drive.setInvertedMotor( RobotDrive.MotorType.kFrontRight , true );
    	drive.setInvertedMotor( RobotDrive.MotorType.kRearLeft , true );
    	drive.setInvertedMotor( RobotDrive.MotorType.kRearRight , true );
    	
		oi = new OI();
        // instantiate the command used for the autonomous period
        autonomousCommand = new ExampleCommand();
    }
    
    public void controlDriving( double speed )
    {
    	backLeftMotor.set( speed ); 
        frontLeftMotor.set( speed ); 
    	
    	frontRightMotor.set( -speed );//right motors are switched
        backRightMotor.set( -speed );
    }
	
	public void disabledPeriodic() {
		led.bars();
		autonomousInit();
		Scheduler.getInstance().run();
	}

    public void autonomousInit() {
    	autoTimer.reset();
    	lsEncoder.reset();
    	leftEncoder.reset();
    	rightEncoder.reset();
        // schedule the autonomous command (example)
        if (autonomousCommand != null) autonomousCommand.start();
    }

    /**
     * This function is called periodically during autonomous
     */
    
    
    //max 3000
    public void autonomousPeriodic() {
    		led.bars();
    		if( lsEncoder.get() > 500 )//moves toward the yellow tote //Up to container org. value = 2250;
    		{
    			goDown= true;//set variable goDown to true
    		}
    		
    		if( goDown )//if goDown is true
    		{
    			if( ( leftEncoder.get() + rightEncoder.get() ) / 2 > 2900 )//drive 2900 ticks to the zone // Drive distance to the line; orig. value = 2300
            	{
    				lslideMotor1.set( 0.0 );//stop linear slide
        			lslideMotor2.set( 0.0 );
    				
            		controlDriving( 0.65 );//move forward
            	}
            	else
            	{
            		controlDriving( 0.0 );//stop moving
            		
            		if( bottomSwitch.get() ) //if lower switch is hit
            		{
            			lslideMotor1.set( 0.0 );//stop linear slide
            			lslideMotor2.set( 0.0 );
            		}
            		else//if not
            		{
            			lslideMotor1.set( -0.25 );//move slide down
            			lslideMotor2.set( -0.25 );
            		}
            	}
    		}
    		else //false
    		{
    			lslideMotor1.set( 0.5 );//slide goes up 0.5 speed
    			lslideMotor2.set( 0.5 );
    		}
        	
        Scheduler.getInstance().run();
    }

    public void teleopInit() {
    	lsEncoder.reset();
    	led.teleopInit();
    	arcade = true;
		// This makes sure that the autonomous stops running when
        // teleop starts running. If you want the autonomous to 
        // continue until interrupted by another command, remove
        // this line or comment it out.
        if (autonomousCommand != null) autonomousCommand.cancel();
    }

    /**
     * This function is called when the disabled button is hit.
     * You can use it to reset subsystems before shutting down.
     */
    public void disabledInit(){

    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
    	System.out.println(lsEncoder.get());
      	
    	led.hook( lsEncoder.get() );
    	if( arcade )
    	{
    		drive.arcadeDrive( driveStickLeft );
    	}
    	else
    	{
    		drive.tankDrive( driveStickLeft , driveStickRight );
    	}
    	
    	double strafeSpeed = ( 1 - driveStickLeft.getZ() ) / 2.0;
    	if( driveStickLeft.getRawButton( 4 ) )
    	{
    		strafe1.set( strafeSpeed );
    		strafe2.set( strafeSpeed );
    	}
    	else if( driveStickLeft.getRawButton( 5 ) )
    	{
    		strafe1.set( -strafeSpeed );
    		strafe2.set( -strafeSpeed );
    	}
    	else
    	{
    		strafe1.set( 0.0 );
    		strafe2.set( 0.0 );
    	}
    	
    	if( driveStickLeft.getRawButton( 10 ) )
    	{
    		arcade = true;
    	}
    	else if( driveStickLeft.getRawButton( 11 ) )
    	{
    		arcade = false;
    	}
    	
    	
    	if(	bottomSwitch.get() && operatorStick.getY() > 0 ||
    			topSwitch.get() && operatorStick.getY() < 0 )
    	{
    		lslideMotor1.set( 0.0 );
    		lslideMotor2.set( 0.0 );
    	}
    	else
    	{
    		lslideMotor1.set( -operatorStick.getY() );
    		lslideMotor2.set( -operatorStick.getY() );
    	}
    	
    	
    	
        Scheduler.getInstance().run();
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
        LiveWindow.run();
    }
}
