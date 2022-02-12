package org.firstinspires.ftc.teamcode.comp.better;


import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import javax.lang.model.type.NoType;

public class thabot {

    private final ElapsedTime runtime = new ElapsedTime(); // getting a warning to make it final

    private Servo servo0;
    private DcMotor arm;

    public double rx;


    public final String SINGLEPLAYER_CONTROL = "SINGLEPLAYER";
    public final String MULTIPLAYER_CONTROL = "MULTIPLAYER";

    public final double NORMAL_SPEED = 0.2; // preference and feel for best
    public final double SERVO_FULLY_CLOSED = 0.0; // need arm+hub to test this left
    public final double SERVO_FULLY_OPENED = 0.5; // need arm+hub to test this right
    public final double HALF_SERVO_ANGLE = 0.5; // need arm+hub to test for this, can probably just average full open/close   left
    public final double BACK_SERVO_ANGLE = Math.PI; // need arm+hub to test for this, can probably just average full open/close   left
    public final double ARM_MAX_SPEED = -   0.5; // preference? or maybe to be precise
    public final double HIGH_SPINNER_POWER = 1; // probably max, may need to adjust later
    public final double OPTIMAL_SPINNER_POWER = 0.5; // need spinner+hub to test this
    public final double MOTOR_STOP = 0; // its just 0 cuz full stop
    public final double SPIN_MOTORS_SPEED = 0.3;

    public boolean opModeActive = false;

    private DcMotor spinner;

    private BNO055IMU imu;

    private DcMotor motorFrontRight;
    private DcMotor motorBackRight;
    private DcMotor motorFrontLeft;
    private DcMotor motorBackLeft;

    private Orientation angles;

    public HardwareMap hw; // no idea if volatile means anything (impactful) in this context, but it makes me seem like I know what im doing

    private float INITIAL_ANGLE;


    public void init(@NonNull HardwareMap hardwareMap){
        // internal IMU setup (copied and pasted, idk what it really does, but it works)
        opModeActive = true;
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
        angles   = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS);

        INITIAL_ANGLE = getAngles().firstAngle;

        // Meccanum Motors Definition and setting prefs
        motorFrontLeft = hardwareMap.dcMotor.get("motorFrontLeft");
        motorBackLeft = hardwareMap.dcMotor.get("motorBackLeft");
        motorFrontRight = hardwareMap.dcMotor.get("motorFrontRight");
        motorBackRight = hardwareMap.dcMotor.get("motorBackRight");

        // Reverse the left side motors and set behaviors to stop instead of coast
        motorFrontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        motorBackLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        motorFrontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBackLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorFrontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBackRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //define arm and servo objects and also spinner
        servo0 = hardwareMap.get(Servo.class, "servo-0");
        arm = hardwareMap.get(DcMotor.class, "arm");
        spinner = hardwareMap.get(DcMotor.class, "spinner");

        //set prefs for arm and servo
        servo0.setDirection(Servo.Direction.FORWARD);
        arm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // define hw as the hardware map for possible access later in this class
        hw = hardwareMap;

        runtime.reset();
    }
    public void motorDrive(double motorFrontLeftPower, double motorBackLeftPower, double motorFrontRightPower, double motorBackRightPower){
        motorBackLeft.setPower(motorBackLeftPower);
        motorFrontLeft.setPower(motorFrontLeftPower);
        motorBackRight.setPower(motorBackRightPower);
        motorFrontRight.setPower(motorFrontRightPower);
    }

    private void motorDriveEncoded(double motorFrontLeftPower, double motorBackLeftPower, double motorFrontRightPower, double motorBackRightPower, int ticks){
        // private I think bcuz only ever accessed inside the class
        // motors need to use encoder

        motorBackLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBackRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorFrontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorFrontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // only checking one motor for ticks rotated rn, should probably check all of them

        // NOTE: not using DcMotor.setTarget() method despite it's built in pid functionality, maybe implement in future, but I dont know if it will stall the program or not like the while loop
        // idk if the blp type vars will be static or change :/

        int blp = motorBackLeft.getCurrentPosition(); // back left initial position :)
        int brp = motorBackRight.getCurrentPosition();
        int flp = motorFrontLeft.getCurrentPosition();
        int frp = motorFrontRight.getCurrentPosition();

        while(abs(motorBackLeft.getCurrentPosition() - blp) < ticks ||
                abs(motorFrontRight.getCurrentPosition() - brp) < ticks ||
                abs(motorFrontLeft.getCurrentPosition() - flp) < ticks ||
                abs(motorBackRight.getCurrentPosition() - frp) < ticks) { // hopefully checks that it is within the positive or negative threshold of target ticks
            motorDrive(motorFrontLeftPower, motorBackLeftPower, motorFrontRightPower, motorBackRightPower);
        }
        motorStop();
    }
    public void motorDriveEncodedReg(double motorFrontLeftPower, double motorBackLeftPower, double motorFrontRightPower, double motorBackRightPower, double ticks, Telemetry telemetry){
        // private I think bcuz only ever accessed inside the class
        // motors need to use encoder

        motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        /*
        final int blp = motorBackLeft.getCurrentPosition(); // idk if this will stay a static value or if it will change with the motor pos, hmm...
        final int brp = motorBackRight.getCurrentPosition();
        final int frp = motorFrontRight.getCurrentPosition();
        final int flp = motorFrontLeft.getCurrentPosition();
        */
        // only checking one motor for ticks rotated rn, should probably check all of them

        // NOTE: not using DcMotor.setTarget() method despite it's built in pid functionality, maybe implement in future, but I dont know if it will stall the program or not like the while loop
        // idk if the blp type vars will be static or change :/

        int blp = motorBackLeft.getCurrentPosition(); // back left initial position :)
        int brp = motorBackRight.getCurrentPosition();
        int flp = motorFrontLeft.getCurrentPosition();
        int frp = motorFrontRight.getCurrentPosition();

        double motorFrontLeftPowerCorrected = motorFrontLeftPower;
        double motorBackLeftPowerCorrected = motorBackLeftPower;
        double motorBackRightPowerCorrected = motorBackRightPower;
        double motorFrontRightPowerCorrected = motorFrontRightPower;
        float startDegrees = getAngles().firstAngle;
        double LOCAL_PROPORTION = 0.04 ;

        while((abs(motorBackLeft.getCurrentPosition() - blp) + abs(motorFrontRight.getCurrentPosition() - frp) + abs(motorFrontLeft.getCurrentPosition() - flp) + abs(motorBackRight.getCurrentPosition() - brp))/4  < ticks) { // hopefully checks that it is within the positive or negative threshold of target ticks
            double difference = AngleUnit.normalizeDegrees(getAngles().firstAngle - startDegrees);
            motorDrive(motorFrontLeftPower + (difference * LOCAL_PROPORTION), motorBackLeftPower + (difference * LOCAL_PROPORTION), motorFrontRightPower - (difference * LOCAL_PROPORTION), motorBackRightPower - (difference * LOCAL_PROPORTION));


            //motorFrontLeftPowerCorrected = motorFrontLeftPower - (difference * LOCAL_PROPORTION);
            //motorBackLeftPowerCorrected = motorBackLeftPower - (difference * LOCAL_PROPORTION);
            //motorBackRightPowerCorrected = motorBackRightPower + (difference * LOCAL_PROPORTION);
            //motorFrontLeftPower - (difference * LOCAL_PROPORTION)
            //motorFrontRightPowerCorrected = motorFrontRightPower + (difference * LOCAL_PROPORTION);

            telemetry.addData("fl", motorFrontLeftPower + (difference * LOCAL_PROPORTION));
            telemetry.addData("bl", motorBackLeftPower + (difference * LOCAL_PROPORTION));
            telemetry.addData("fr", motorFrontRightPower - (difference * LOCAL_PROPORTION));
            telemetry.addData("br", motorBackRightPower - (difference * LOCAL_PROPORTION));
            telemetry.update();

        }
        motorStop();
    }

    public void motorDriveTime(double motorFrontLeftPower, double motorBackLeftPower, double motorFrontRightPower, double motorBackRightPower, double time){
        motorDrive(motorFrontLeftPower, motorBackLeftPower, motorFrontRightPower, motorBackRightPower);
        delay(time);
        motorStop();
    }

    public void motorDriveRelativeFieldAngleEncoded(double radians, double speed, double ticks){
        //test on monday 11/29/2021
        //NOTE
        // im not sure how to acurately do this using encoders, because some wheels are going to spin at different powers (I think)
        // this will cause the ticks to be difficult to calculate, and I dont really want to deal with that rn

        float startAngle = getAngles().firstAngle;
        double maxSpeed = 0.8;
        double spinvec = 0; // not spinning
        double yvec = min(speed, maxSpeed) * sin(radians); // ima be honest, i did this math for a js project 6 months ago and am just hopin it actually works in this context
        double xvec = min(speed, maxSpeed) * cos(radians)+1;

        double y = pow(-yvec,3); // Remember, this is reversed!
        double x = pow(xvec * 1.1,3); // Counteract imperfect strafing
        double rx = pow(spinvec,3);

        double CORRECTION_FACTOR = 0.01;

        //denominator is the largest motor power (absolute value) or 1
        //this ensures all the powers maintain the same ratio, but only when
        //at least one is out of the range [-1, 1]

        motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        int blip = motorBackLeft.getCurrentPosition(); // back left initial position :)
        int brip = motorBackRight.getCurrentPosition();
        int flip = motorFrontLeft.getCurrentPosition();
        int frip = motorFrontRight.getCurrentPosition();

        double denominator = Math.max(abs(y) + abs(x) + abs(rx), maxSpeed);
        double frontLeftPower = (y + x + rx) / denominator;
        double backLeftPower = (y - x + rx) / denominator;
        double frontRightPower = (y - x - rx) / denominator;
        double backRightPower = (y + x - rx) / denominator;


        while (abs(motorBackLeft.getCurrentPosition() - blip) < ticks ||
                abs(motorFrontRight.getCurrentPosition() - frip) < ticks ||
                abs(motorFrontLeft.getCurrentPosition() - flip) < ticks ||
                abs(motorBackRight.getCurrentPosition() - brip) < ticks) {

            rx = (INITIAL_ANGLE-getAngles().firstAngle * CORRECTION_FACTOR);
            denominator = Math.max(abs(y) + abs(x) + abs(rx), maxSpeed);
            frontLeftPower = (y + x + rx) / denominator;
            backLeftPower = (y - x + rx) / denominator;
            frontRightPower = (y - x - rx) / denominator;
            backRightPower = (y + x - rx) / denominator;
            motorDrive(frontLeftPower, backLeftPower, frontRightPower, backRightPower);

        }
    }

    public void motorDriveXYVectors(double xvec, double yvec, double spinvec){
        // used for teleop mode
        //NOTE
        // im not sure how to acurately do this using encoders, because some wheels are going to spin at different powers (I think)
        // this will cause the ticks to be difficult to calculate, and I dont really want to deal with that


        double y = pow(yvec,1); // Remember, this is reversed!
        double x = pow(xvec * 1.1,1); // Counteract imperfect strafing
        rx = pow(spinvec,1);


        //denominator is the largest motor power (absolute value) or 1
        //this ensures all the powers maintain the same ratio, but only when
        //at least one is out of the range [-1, 1]
        double denominator = Math.max(abs(y) + abs(x) + abs(rx), 1);
        double frontLeftPower = (y + x + rx) / denominator;
        double backLeftPower = (y - x + rx) / denominator;
        double frontRightPower = (y - x - rx) / denominator;
        double backRightPower = (y + x - rx) / denominator;

        motorDrive(frontLeftPower,backLeftPower, frontRightPower, backRightPower);

    }

    public void motorDriveRelativeAngleTime(double radians, double speed, double time){
        motorDriveRelativeFieldAngleEncoded(radians, speed, 1); //t his is so no error, NEED CHANGE NOT RIGHT
        delay(time);
        motorStop();
    }

    public void motorStop(){
        motorBackLeft.setPower(0);
        motorFrontLeft.setPower(0);
        motorBackRight.setPower(0);
        motorFrontRight.setPower(0);
    }

    public void motorDriveForward(double speed){
        motorDrive(speed, speed, speed, speed);
    }
    public void motorDriveLeft(double speed){
        motorDrive(speed, -speed, speed, -speed);
    }
    public void motorDriveRight(double speed){
        motorDrive(-speed, speed, -speed, speed);
    }
    public void motorDriveBack(double speed){
        motorDrive(-speed, -speed, -speed, -speed);
    }

    public void motorDriveForwardEncoded(double speed, int distance){
        motorDriveEncoded(-speed, -speed, -speed, -speed, distance);
    }
    public void motorDriveLeftEncoded(double speed, int distance){
        motorDriveEncoded(-speed, speed, speed, -speed, distance);
    }
    public void motorDriveRightEncoded(double speed, int distance){
        motorDriveEncoded(speed, -speed, -speed, speed, distance);
    }
    public void motorDriveBackEncoded(double speed, int distance){
        motorDriveEncoded(speed, speed, speed, speed, distance);
    }
    public void motorDriveForwardTime(double speed, double time){
        motorDriveTime(speed, speed, speed, speed, time);
    }
    public void motorDriveLeftTime(double speed, double time){
        motorDriveTime(speed, -speed, speed, -speed, time);
    }
    public void motorDriveRightTime(double speed, double time){
        motorDriveTime(-speed, speed, -speed, speed, time);
    }
    public void motorDriveBackTime(double speed, double time){
        motorDriveTime(-speed, -speed, -speed, -speed, time);
    }

    public void delay(double time){
        ElapsedTime e = new ElapsedTime();
        e.reset();
        while(e.milliseconds() < time){
            // stal program


        }
    }

    public void opModeOn(boolean b){
        opModeActive = b;
    }

    public void motorSpinLeft(double speed){
        motorDrive(-speed, -speed, speed, speed);
    }
    public void motorSpinRight(double speed){
        motorDrive(speed, speed, -speed, -speed);
    }

    public void motorSpinLeftEncoded(double speed, int distance){
        motorDriveEncoded(-speed, -speed, speed, speed, distance);
    }
    public void motorSpinRightEncoded(double speed, int distance){
        motorDriveEncoded(speed, speed, -speed, -speed, distance);
    }

    public void motorSpinLeftTime(double speed, double time){
        motorDriveTime(-speed, -speed, speed, speed, time);
    }
    public void motorSpinRightTime(double speed, double time){
        motorDriveTime(speed, speed, -speed, -speed, time);
    }


    public void spinnySpin(double speed){
        spinner.setPower(speed);
    }

    public void spinnySpinEncoded(double speed, double target){
        spinnySpinEncoded(speed, target, spinner.getCurrentPosition());
    }
    public void spinnySpinEncoded(double speed, double target, int start){

        while (abs(spinner.getCurrentPosition()-start) < target){
            spinnySpin(speed);
        }
        spinnyStop();
    }

    public void spinnyStop() {
        spinner.setPower(0);
    }

    public void spinnySpinTime(double speed, double time){
        spinnySpin(speed);
        delay(time);
        spinnyStop();
    }

    public void moveArm(double power){
        arm.setPower(power);
    }
    public void moveArmTime(double power, double time){
        moveArm(power);
        delay(time);
        moveArm(0);

    }
    public void regulateArm(double scale){
        arm.setTargetPosition(arm.getCurrentPosition());
    }

    public double turnRadians(double radians, double speed) { // private I think bcuz only ever accessed inside the class
        float startRadians = angles.firstAngle;
        double target = startRadians + radians;
        double minSpeed = 0.1;
        while(angles.firstAngle < target){
            motorSpinRight(Math.max(target - angles.firstAngle, minSpeed));
            angles = getAngles();
        }
        motorStop();
        return target-startRadians;


    }
    public void turnToDeg(double degrees, double speed) { // private I think bcuz only ever accessed inside the class
        double threshold = .5;
        double startDegrees = getAngles().firstAngle;
        while(Math.abs(getAngles().firstAngle - AngleUnit.normalizeDegrees(degrees)) > threshold){ // make sure that the difference between the normalized degree to turn to and current degrees is withing a threshold
            if (getAngles().firstAngle - AngleUnit.normalizeDegrees(degrees) >= 0){ // turn the correct way
                motorSpinRight(speed);
            } else {
                motorSpinLeft(speed);
            }
        }
        motorStop();

    }
    public void turnDeg(double degrees, double speed, Telemetry telemetry) { // private I think bcuz only ever accessed inside the class
        double threshold = .1;
        double PROP_CONST = 0.02;
        double startDegrees = getAngles().firstAngle;
        while(Math.abs(getAngles().firstAngle - AngleUnit.normalizeDegrees(startDegrees + degrees)) > threshold){ // start degrees + degrees is the actual target postition

            double ns = Math.max(0.06, speed * Math.abs(getAngles().firstAngle - AngleUnit.normalizeDegrees(startDegrees + degrees))*PROP_CONST);

            if (getAngles().firstAngle - AngleUnit.normalizeDegrees(startDegrees + degrees) >= 0){
                motorSpinRight(ns);
                telemetry.addData("direct", "right");
            } else {
                motorSpinLeft(ns);
                telemetry.addData("direct", "left");
            }
            telemetry.addData("togo", Math.abs(getAngles().firstAngle - AngleUnit.normalizeDegrees(startDegrees + degrees)));
            telemetry.addData("fif", getAngles().firstAngle - AngleUnit.normalizeDegrees(startDegrees + degrees));
            telemetry.addData("fang", getAngles().firstAngle );
            telemetry.update();
        }
        motorStop();

    }


    public void setServo(double angle){
        servo0.setPosition(angle);
    }

    public void openServoHalf(){
        servo0.setPosition(HALF_SERVO_ANGLE);
    }
    public double getServo(){
        return servo0.getPosition();
    }
    public void openServoFull(){
        servo0.setPosition(SERVO_FULLY_OPENED);
    }

    public void closeServoFull(){
        servo0.setPosition(SERVO_FULLY_CLOSED);
    }

    public Orientation getAngles() {
        return imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
    }
    public double getAngle360() {
        return imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle + 360;
    }
}
