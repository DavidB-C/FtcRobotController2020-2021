/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.comp.driver;

import android.content.Context;

import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.internal.android.dx.cf.attrib.AttEnclosingMethod;
import org.firstinspires.ftc.teamcode.comp.chassis.Meccanum;
import org.firstinspires.ftc.teamcode.comp.controller.ControllerMap;
import org.firstinspires.ftc.teamcode.comp.controller.ControllerMapSINGLE;

import java.io.File;

// this is just single player controls, kinda useless tbh

@TeleOp(name="cts", group="Demos")
@Disabled
public class cts extends LinearOpMode {

    // Declare OpMode members.
    Meccanum meccanum = new Meccanum();

    ControllerMapSINGLE cms = new ControllerMapSINGLE();
    //ControllerMapSINGLE cms = new ControllerMapSINGLE();

    private int startupID;
    private Context appContext;



    enum controlModes {
        SINGLE,
        MULTI
    }
    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // internal IMU setup
        meccanum.init(hardwareMap);

        cms.init(meccanum, gamepad1);
        //cms.init(meccanum, gamepad1);

        controlModes mode = controlModes.MULTI;

        startupID = hardwareMap.appContext.getResources().getIdentifier("startup", "raw", hardwareMap.appContext.getPackageName());
        appContext = hardwareMap.appContext;
        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        SoundPlayer.getInstance().stopPlayingAll();
        SoundPlayer.getInstance().play(appContext, startupID);


        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            Orientation angles = meccanum.getAngles();
            cms.checkControls();

            //telemetry.addData("test", "test!");
            //telemetry.addData("servo ", meccanum.getServo().getPosition());
            //telemetry.addData("rotation ", angles.axesReference);
            if (gamepad1.dpad_down){
                startupID = hardwareMap.appContext.getResources().getIdentifier("scree", "raw", hardwareMap.appContext.getPackageName());
                appContext = hardwareMap.appContext;
                SoundPlayer.getInstance().stopPlayingAll();
                SoundPlayer.getInstance().play(appContext, startupID);
                telemetry.addData("play", "scree");

            }
            if (gamepad1.dpad_up){
                startupID = hardwareMap.appContext.getResources().getIdentifier("car", "raw", hardwareMap.appContext.getPackageName());
                appContext = hardwareMap.appContext;
                SoundPlayer.getInstance().stopPlayingAll();
                SoundPlayer.getInstance().play(appContext, startupID);
                telemetry.addData("play", "car");

            }
            // MECCANUM MATH

            // drives bottom steering motors






            telemetry.update();
        }

    }
}
